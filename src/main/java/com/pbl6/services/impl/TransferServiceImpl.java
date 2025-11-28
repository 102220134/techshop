package com.pbl6.services.impl;

import com.pbl6.dtos.request.inventory.transfer.CreateTransferRequest;
import com.pbl6.dtos.request.inventory.transfer.ListTransferRequest;
import com.pbl6.dtos.request.inventory.transfer.TransferDetailRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.transfer.TransferDto;
import com.pbl6.dtos.response.inventory.transfer.TransferItemDto;
import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.entities.*;
import com.pbl6.enums.OrderStatus;
import com.pbl6.enums.ReservationStatus;
import com.pbl6.enums.TransferStatus;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.*;
import com.pbl6.services.InventoryLocationService;
import com.pbl6.services.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final InventoryLocationService inventoryLocationService;
    private final TransferItemRepository inventoryTransferItemRepository;
    private final InventoryLocationRepository inventoryLocationRepository;
    private final VariantRepository variantRepository;
    private final ProductSerialRepository productSerialRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ReservationRepository reservationRepository;
    private final OrderRepository orderRepository;

    // ========================================================================
    // CREATE TRANSFER
    // ========================================================================

    @Override
    @Transactional
    public TransferDto createTransfer(CreateTransferRequest req) {
        InventoryLocationEntity source = getLocation(req.getSourceLocationId());
        InventoryLocationEntity destination = getLocation(req.getTargetLocationId());

        InventoryTransferEntity transfer = createTransferHeader(source, destination);
        List<InventoryTransferItemEntity> items = new ArrayList<>();

        for (CreateTransferRequest.Item it : req.getItems()) {
            items.add(createTransferItem(transfer, source, it));
        }

        inventoryTransferItemRepository.saveAll(items);
        transfer.setItems(items);

        return toDto(transfer);
    }

    @Override
    @Transactional
    public TransferDto createTransfer(List<Long> reservationIds) {
        if (reservationIds == null || reservationIds.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Danh sách Reservation không được rỗng");
        }

        List<ReservationEntity> reservations = reservationRepository.findAllById(reservationIds);
        validateReservationsForTransfer(reservations, reservationIds.size());

        InventoryLocationEntity sourceLoc = reservations.get(0).getLocation();
        InventoryLocationEntity destLoc = reservations.get(0).getOrder().getStore().getInventoryLocation();

        if (destLoc == null) {
            throw new AppException(ErrorCode.NOT_FOUND, "Cửa hàng đích chưa cấu hình kho nhận hàng");
        }

        InventoryTransferEntity transfer = createTransferHeader(sourceLoc, destLoc);
        List<InventoryTransferItemEntity> transferItems = createItemsFromReservations(transfer, reservations);

        // Update Reservations linked to this transfer
        reservations.forEach(res -> {
            res.setTransfer(transfer);
            res.setStatus(ReservationStatus.CONFIRMED);
            res.setUpdatedAt(LocalDateTime.now());
        });
        reservationRepository.saveAll(reservations);

        transfer.setItems(transferItems);
        return toDto(transfer);
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    @Override
    public PageDto<TransferDto> getTransfers(ListTransferRequest req) {
        PageRequest pageable = getPageRequest(req.getPage(), req.getSize(), req.getDir(), req.getOrder());
        Page<TransferDto> pageResult = transferRepository.findByStatus(req.getStatus(), pageable).map(this::toDto);
        return new PageDto<>(pageResult);
    }

    @Override
    public PageDto<TransferItemDto> getTransferItems(long id, TransferDetailRequest req) {
        if (!transferRepository.existsById(id)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Transfer not found");
        }
        PageRequest pageable = getPageRequest(req.getPage(), req.getSize(), req.getDir(), req.getOrder());
        Page<TransferItemDto> pageResult = inventoryTransferItemRepository.findByTransferId(id, pageable).map(this::toTransferItemDto);
        return new PageDto<>(pageResult);
    }

    // ========================================================================
    // STATE TRANSITIONS (PUBLIC & CENTRALIZED)
    // ========================================================================

    @Override
    @Transactional
    public void confirmTransfer(long transferId) {
        updateTransferStatus(transferId, TransferStatus.CONFIRMED);
    }

    @Override
    @Transactional
    public void startTransfer(Long transferId) {
        updateTransferStatus(transferId, TransferStatus.TRANSFERRING);
    }

    @Override
    @Transactional
    public void completeTransfer(Long transferId) {
        updateTransferStatus(transferId, TransferStatus.COMPLETED);
    }

    @Override
    @Transactional
    public void cancelTransfer(Long transferId) {
        updateTransferStatus(transferId, TransferStatus.CANCELLED);
    }

    @Override
    @Transactional
    public void deleteTransfer(Long transferId) {
        InventoryTransferEntity transfer = getTransferOrThrow(transferId);
        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Chỉ được xoá phiếu khi đang ở trạng thái DRAFT");
        }

        inventoryTransferItemRepository.deleteAll(transfer.getItems());

        List<ReservationEntity> linkedReservations = reservationRepository.findByTransferId(transferId);
        if (!linkedReservations.isEmpty()) {
            linkedReservations.forEach(res -> {
                res.setTransfer(null);
                res.setStatus(ReservationStatus.PENDING);
                res.setUpdatedAt(LocalDateTime.now());
            });
            reservationRepository.saveAll(linkedReservations);
        }
        transferRepository.delete(transfer);
    }

//    @Override
    @Transactional
    public void updateTransferStatus(Long transferId, TransferStatus newStatus) {
        InventoryTransferEntity transfer = getTransferOrThrow(transferId);

        if (transfer.getStatus() == newStatus) {
            log.warn("Transfer {} đã ở trạng thái {}. Bỏ qua.", transferId, newStatus);
            return;
        }

        switch (newStatus) {
            case CONFIRMED -> processConfirm(transfer);
            case TRANSFERRING -> processStartTransfer(transfer);
            case COMPLETED -> processCompleteTransfer(transfer);
            case CANCELLED -> processCancel(transfer);
            default -> throw new AppException(ErrorCode.VALIDATION_ERROR, "Trạng thái không hỗ trợ: " + newStatus);
        }

        transfer.setStatus(newStatus);
        transfer.setUpdatedAt(LocalDateTime.now());
        transferRepository.save(transfer);
    }

    // ========================================================================
    // CORE BUSINESS LOGIC (PRIVATE)
    // ========================================================================

    private void processConfirm(InventoryTransferEntity transfer) {
        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Chỉ được Confirm phiếu DRAFT");
        }

        List<ReservationEntity> linkedReservations = reservationRepository.findByTransferId(transfer.getId());
        List<String> allSerials = transfer.getItems().stream()
                .flatMap(item -> item.getProductSerials().stream().map(ProductSerialEntity::getSerialNo))
                .toList();

        if (!linkedReservations.isEmpty()) {
            // Case: Transfer từ đơn hàng -> Serial đã RESERVED từ trước
            long countReserved = productSerialRepository.countReservedSerials(allSerials, transfer.getSource().getId());
            if (countReserved != allSerials.size()) {
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi đồng bộ: Một số Serial không ở trạng thái RESERVED.");
            }
            linkedReservations.forEach(res -> {
                res.setStatus(ReservationStatus.CONFIRMED);
                res.setUpdatedAt(LocalDateTime.now());
            });
            reservationRepository.saveAll(linkedReservations);
        } else {
            // Case: Transfer thủ công -> Lock kho và Serial
            transfer.getItems().forEach(item -> {
                InventoryEntity inventory = inventoryRepository.findByInventoryLocationIdAndVariantId(transfer.getSource().getId(), item.getVariant().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Sản phẩm không có trong kho nguồn"));
                inventory.addReservedStock(item.getQuantity());
                inventoryRepository.save(inventory);
            });

            int updatedRows = productSerialRepository.reserveSerials(allSerials, transfer.getSource().getId());
            if (updatedRows != allSerials.size()) {
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Xung đột: Một số sản phẩm đã bị lấy mất trước khi Confirm.");
            }
        }
    }

    private void processStartTransfer(InventoryTransferEntity transfer) {
        if (transfer.getStatus() != TransferStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Phiếu phải CONFIRMED trước khi xuất kho");
        }

        for (InventoryTransferItemEntity item : transfer.getItems()) {
            // 1. Trừ tồn kho
            InventoryEntity inv = inventoryRepository.findByInventoryLocationIdAndVariantId(transfer.getSource().getId(), item.getVariant().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Lỗi dữ liệu tồn kho"));

            if (inv.getStock() < item.getQuantity()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Tồn kho không đủ để xuất (Lỗi bất thường)");
            }

            inv.setStock(inv.getStock() - item.getQuantity());
            inv.setReservedStock(inv.getReservedStock() - item.getQuantity());
            inventoryRepository.save(inv);

            // 2. Ghi Movement & Update Serial
            createMovement(transfer.getSource(), item.getVariant(), -item.getQuantity(), "TRANSFER_OUT", transfer.getId());

            List<String> serials = item.getProductSerials().stream().map(ProductSerialEntity::getSerialNo).toList();
            productSerialRepository.updateSerialsForShipping(serials, transfer.getSource().getId());
        }

        updateReservationStatus(transfer.getId(), ReservationStatus.TRANSFERRING);
    }

    private void processCompleteTransfer(InventoryTransferEntity transfer) {
        if (transfer.getStatus() != TransferStatus.TRANSFERRING) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Phiếu chưa được xuất đi (TRANSFERRING)");
        }

        boolean isReservationTransfer = !reservationRepository.findByTransferId(transfer.getId()).isEmpty();

        for (InventoryTransferItemEntity item : transfer.getItems()) {
            // 1. Cộng tồn kho
            InventoryEntity inv = inventoryRepository.findByInventoryLocationIdAndVariantId(transfer.getDestination().getId(), item.getVariant().getId())
                    .orElseGet(() -> {
                        InventoryEntity newInv = new InventoryEntity();
                        newInv.setInventoryLocation(transfer.getDestination());
                        newInv.setVariant(item.getVariant());
                        newInv.setStock(0);
                        return newInv;
                    });
            inv.setStock(inv.getStock() + item.getQuantity());
            inventoryRepository.save(inv);

            // 2. Ghi Movement & Update Serial
            createMovement(transfer.getDestination(), item.getVariant(), item.getQuantity(), "TRANSFER_IN", transfer.getId());

            List<String> serials = item.getProductSerials().stream().map(ProductSerialEntity::getSerialNo).toList();

            if (isReservationTransfer) {
                productSerialRepository.updateSerialsForStoreReservation(serials, transfer.getDestination());
            } else {
                productSerialRepository.updateSerialsForReceiving(serials, transfer.getDestination());
            }
        }

        if (isReservationTransfer) {
            updateReservationStatus(transfer.getId(), ReservationStatus.AVAILABLE);
        }
    }

    private void processCancel(InventoryTransferEntity transfer) {
        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Chỉ được Hủy phiếu khi đang ở DRAFT");
        }
        updateReservationStatus(transfer.getId(), ReservationStatus.PENDING);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private InventoryLocationEntity getLocation(Long id) {
        return inventoryLocationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Location not found: " + id));
    }

    private InventoryTransferEntity getTransferOrThrow(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Transfer not found"));
    }

    private InventoryTransferEntity createTransferHeader(InventoryLocationEntity source, InventoryLocationEntity dest) {
        InventoryTransferEntity transfer = new InventoryTransferEntity();
        transfer.setSource(source);
        transfer.setDestination(dest);
        transfer.setStatus(TransferStatus.DRAFT);
        transfer.setCreatedAt(LocalDateTime.now());
        return transferRepository.save(transfer);
    }

    private InventoryTransferItemEntity createTransferItem(InventoryTransferEntity transfer, InventoryLocationEntity source, CreateTransferRequest.Item reqItem) {
        VariantEntity variant = variantRepository.findById(reqItem.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Variant not found"));

        // Validate kho nguồn
        InventoryEntity inventory = inventoryRepository.findByInventoryLocationIdAndVariantId(source.getId(), variant.getId())
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Sản phẩm không có trong kho nguồn"));

        if (inventory.getStock() < reqItem.getSerials().size()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Kho nguồn không đủ số lượng: " + variant.getSku());
        }

        // Validate Serials
        List<ProductSerialEntity> validSerials = productSerialRepository.findAvailableSerialsInLocation(reqItem.getSerials(), variant.getId(), source.getId());
        if (validSerials.size() != reqItem.getSerials().size()) {
            List<String> found = validSerials.stream().map(ProductSerialEntity::getSerialNo).toList();
            List<String> missing = reqItem.getSerials().stream().filter(s -> !found.contains(s)).toList();
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Serial không hợp lệ hoặc sai kho: " + String.join(", ", missing));
        }

        InventoryTransferItemEntity item = new InventoryTransferItemEntity();
        item.setTransfer(transfer);
        item.setVariant(variant);
        item.setQuantity(reqItem.getSerials().size());
        item.setProductSerials(validSerials);
        return item;
    }

    private List<InventoryTransferItemEntity> createItemsFromReservations(InventoryTransferEntity transfer, List<ReservationEntity> reservations) {
        Map<VariantEntity, List<ReservationEntity>> grouped = reservations.stream()
                .collect(Collectors.groupingBy(r -> r.getOrderItem().getVariant()));

        List<InventoryTransferItemEntity> items = new ArrayList<>();
        grouped.forEach((variant, resList) -> {
            List<ProductSerialEntity> serials = resList.stream()
                    .flatMap(r -> r.getProductSerials().stream())
                    .collect(Collectors.toList());

            InventoryTransferItemEntity item = new InventoryTransferItemEntity();
            item.setTransfer(transfer);
            item.setVariant(variant);
            item.setQuantity(serials.size());
            item.setProductSerials(serials);

            inventoryTransferItemRepository.save(item);
            items.add(item);
        });
        return items;
    }

    private void validateReservationsForTransfer(List<ReservationEntity> reservations, int expectedSize) {
        if (reservations.size() != expectedSize) {
            throw new AppException(ErrorCode.NOT_FOUND, "Một số Reservation không tồn tại");
        }
        if (reservations.stream().anyMatch(r -> r.getStatus() != ReservationStatus.PENDING)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ được tạo phiếu chuyển cho yêu cầu PENDING");
        }

        Long sourceLocId = reservations.get(0).getLocation().getId();
        if (reservations.stream().anyMatch(r -> !r.getLocation().getId().equals(sourceLocId))) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Các yêu cầu phải từ cùng một kho nguồn");
        }

        StoreEntity targetStore = reservations.get(0).getOrder().getStore();
        if (targetStore == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Đơn hàng thiếu thông tin cửa hàng đích");
        }
        if (reservations.stream().anyMatch(r -> r.getOrder().getStore() == null || !r.getOrder().getStore().getId().equals(targetStore.getId()))) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Các yêu cầu phải về cùng một cửa hàng đích");
        }
    }

    private void updateReservationStatus(Long transferId, ReservationStatus status) {
        List<ReservationEntity> list = reservationRepository.findByTransferId(transferId);

        if (list.isEmpty()) return;
        if (status == ReservationStatus.TRANSFERRING) {
            List<OrderEntity> distinctOrders = list.stream()
                    .map(ReservationEntity::getOrder)
                    .distinct()
                    .toList();
            List<OrderEntity> ordersToSave = new ArrayList<>();
            for (OrderEntity order : distinctOrders) {
                if (order.getStatus() != OrderStatus.DELIVERING) {
                    order.setStatus(OrderStatus.DELIVERING);
                    ordersToSave.add(order);
                }
            }

            if (!ordersToSave.isEmpty()) {
                orderRepository.saveAll(ordersToSave);
                log.info("Đã cập nhật {} đơn hàng sang trạng thái DELIVERING", ordersToSave.size());
            }
        }

        list.forEach(r -> {
            if (status == ReservationStatus.PENDING) r.setTransfer(null);
            r.setStatus(status);
            r.setUpdatedAt(LocalDateTime.now());
        });

        reservationRepository.saveAll(list);
    }

    private void createMovement(InventoryLocationEntity loc, VariantEntity variant, int qty, String reason, Long refId) {
        StockMovementEntity mov = new StockMovementEntity();
        mov.setInventoryLocation(loc);
        mov.setVariant(variant);
        mov.setQuantityDelta(qty);
        mov.setReason(reason);
        mov.setRefType("TRANSFER");
        mov.setRefId(refId);
        mov.setCreatedAt(LocalDateTime.now());
        stockMovementRepository.save(mov);
    }

    private PageRequest getPageRequest(int page, int size, String dir, String order) {
        Sort sort = Sort.by(dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, order);
        return PageRequest.of(page - 1, size, sort);
    }

    // Mapping methods kept simple
    public TransferDto toDto(InventoryTransferEntity e) {
        return TransferDto.builder()
                .id(e.getId())
                .source(inventoryLocationService.toDto(e.getSource()))
                .destination(inventoryLocationService.toDto(e.getDestination()))
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public TransferItemDto toTransferItemDto(InventoryTransferItemEntity e) {
        VariantEntity v = e.getVariant();
        return TransferItemDto.builder()
                .id(e.getId())
                .variantId(v.getId())
                .sku(v.getSku())
                .thumbnail(v.getThumbnail())
                .quantity(e.getQuantity())
                .attributes(v.getVariantAttributeValues().stream()
                        .map(val -> VariantDto.AttributeDto.builder()
                                .code(val.getAttribute().getCode())
                                .label(val.getAttribute().getLabel())
                                .value(val.getAttributeValue().getLabel()).build())
                        .toList())
                .build();
    }
}