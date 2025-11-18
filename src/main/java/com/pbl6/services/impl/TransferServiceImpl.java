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
import com.pbl6.enums.ReceiveMethod;
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

    @Override
    @Transactional
    public TransferDto createTransfer(CreateTransferRequest req) {

        // 1. Lấy thông tin kho nguồn và đích
        InventoryLocationEntity source = inventoryLocationRepository.findById(req.getSourceLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Source location not found"));

        InventoryLocationEntity destination = inventoryLocationRepository.findById(req.getTargetLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Target location not found"));

        // 2. Tạo Transfer Header
        InventoryTransferEntity transfer = new InventoryTransferEntity();
        transfer.setSource(source);
        transfer.setDestination(destination);
        transfer.setStatus(TransferStatus.DRAFT);
        transfer.setCreatedAt(LocalDateTime.now());

        // Save transfer trước để có ID gán cho items (nếu không dùng Cascade)
        transfer = transferRepository.save(transfer);

        List<InventoryTransferItemEntity> items = new ArrayList<>();

        // 3. Duyệt qua từng dòng sản phẩm (Item)
        for (CreateTransferRequest.Item it : req.getItems()) {

            Long variantId = it.getVariantId();
            List<String> requestedSerials = it.getSerials();

            VariantEntity variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Variant not found"));

            // --- FIX LOGIC: Kiểm tra tồn kho tại KHO NGUỒN (thay vì check variant chung chung) ---
            // (Giả sử bạn có hàm lấy tồn kho theo location)
            InventoryEntity inventory = inventoryRepository.findByInventoryLocationIdAndVariantId(source.getId(), variantId)
                    .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Sản phẩm không có trong kho nguồn"));

            if (inventory.getStock() < requestedSerials.size()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Kho nguồn không đủ số lượng cho sản phẩm: " + variant.getSku());
            }

            // --- FIX PERFORMANCE & LOGIC: Query 1 lần và check kỹ điều kiện ---
            List<ProductSerialEntity> validSerials = productSerialRepository.findAvailableSerialsInLocation(
                    requestedSerials,
                    variantId,
                    source.getId() // Quan trọng: Phải check xem serial có ở kho nguồn không
            );

            // Nếu số lượng tìm thấy < số lượng yêu cầu -> Có serial không hợp lệ
            if (validSerials.size() != requestedSerials.size()) {
                // Logic tìm ra serial nào lỗi để báo cho user (Optional)
                List<String> foundSerialNos = validSerials.stream().map(ProductSerialEntity::getSerialNo).toList();
                List<String> invalidSerials = requestedSerials.stream().filter(s -> !foundSerialNos.contains(s)).toList();

                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        "Các serial sau không hợp lệ (Không tồn tại, sai kho, hoặc đã bán): " + String.join(", ", invalidSerials));
            }

            // Tạo Transfer Item
            InventoryTransferItemEntity item = new InventoryTransferItemEntity();
            item.setTransfer(transfer);
            item.setVariant(variant);
            item.setQuantity(requestedSerials.size());

            // Lưu ý: Tùy vào thiết kế DB của bạn, nếu TransferItem có quan hệ OneToMany với ProductSerial
            // bạn cần set quan hệ ở đây. Code cũ của bạn đang add vào list.
            item.setProductSerials(validSerials);

            items.add(item);
        }

        // Save All Items một lần (Tối ưu hơn save trong vòng lặp)
        inventoryTransferItemRepository.saveAll(items);

        // Set lại items cho transfer để trả về DTO đầy đủ
        transfer.setItems(items);

        return toDto(transfer);
    }

    @Override
    @Transactional
    public TransferDto createTransfer(List<Long> reservationIds) {
        if (reservationIds == null || reservationIds.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Danh sách Reservation không được rỗng");
        }

        // 1. Lấy danh sách Reservation
        List<ReservationEntity> reservations = reservationRepository.findAllById(reservationIds);
        if (reservations.size() != reservationIds.size()) {
            throw new AppException(ErrorCode.NOT_FOUND, "Một số Reservation không tồn tại");
        }

        // === [MỚI] VALIDATE STATUS: Chỉ cho phép PENDING ===
        boolean hasInvalidStatus = reservations.stream()
                .anyMatch(r -> r.getStatus() != ReservationStatus.PENDING);

        if (hasInvalidStatus) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ được phép tạo phiếu chuyển cho các yêu cầu đang ở trạng thái PENDING");
        }
        // ===================================================

        // 2. Validate: Tất cả phải cùng Kho Nguồn
        InventoryLocationEntity sourceLoc = reservations.get(0).getLocation();
        boolean sameSource = reservations.stream().allMatch(r -> r.getLocation().getId().equals(sourceLoc.getId()));
        if (!sameSource) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Các yêu cầu phải đến từ cùng một kho nguồn");
        }

        // 3. Validate: Tất cả phải cùng Cửa Hàng Đích
        StoreEntity targetStore = reservations.get(0).getOrder().getStore();
        if (targetStore == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Đơn hàng không có thông tin cửa hàng đích");
        }
        boolean sameDest = reservations.stream().allMatch(r ->
                r.getOrder().getStore() != null &&
                r.getOrder().getStore().getId().equals(targetStore.getId())
        );
        if (!sameDest) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Các yêu cầu phải cùng về một cửa hàng đích");
        }

        InventoryLocationEntity destLoc = targetStore.getInventoryLocation();
        if (destLoc == null) {
            throw new AppException(ErrorCode.NOT_FOUND, "Cửa hàng đích chưa được cấu hình kho nhận hàng");
        }

        // 4. Tạo Transfer Header
        InventoryTransferEntity transfer = new InventoryTransferEntity();
        transfer.setSource(sourceLoc);
        transfer.setDestination(destLoc);
        transfer.setStatus(TransferStatus.DRAFT);
        transfer.setCreatedAt(LocalDateTime.now());
        transfer = transferRepository.save(transfer);

        List<InventoryTransferItemEntity> transferItems = new ArrayList<>();

        // 5. Gom nhóm Variant
        Map<VariantEntity, List<ReservationEntity>> groupedByVariant = reservations.stream()
                .collect(Collectors.groupingBy(r -> r.getOrderItem().getVariant()));

        for (Map.Entry<VariantEntity, List<ReservationEntity>> entry : groupedByVariant.entrySet()) {
            VariantEntity variant = entry.getKey();
            List<ReservationEntity> resList = entry.getValue();

            List<ProductSerialEntity> allSerials = new ArrayList<>();
            resList.forEach(r -> allSerials.addAll(r.getProductSerials()));

            InventoryTransferItemEntity item = new InventoryTransferItemEntity();
            item.setTransfer(transfer);
            item.setVariant(variant);
            item.setQuantity(allSerials.size());
            item.setProductSerials(allSerials);

            inventoryTransferItemRepository.save(item);
            transferItems.add(item);
        }

        // 6. Cập nhật lại Reservation
        for (ReservationEntity res : reservations) {
            res.setTransfer(transfer);
            // QUAN TRỌNG: Phải chuyển sang CONFIRMED ngay lập tức.
            // Nếu không, lần gọi API tiếp theo vẫn thấy nó là PENDING và tạo tiếp phiếu chuyển thứ 2 -> Lỗi trùng lặp.
            res.setStatus(ReservationStatus.CONFIRMED);
            res.setUpdatedAt(LocalDateTime.now());
        }
        reservationRepository.saveAll(reservations);

        transfer.setItems(transferItems);
        return toDto(transfer);
    }

    @Override
    public PageDto<TransferDto> getTransfers(ListTransferRequest req) {
        Sort sort = Sort.by(
                req.getDir().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                req.getOrder()
        );
        PageRequest pageable = PageRequest.of(req.getPage() - 1, req.getSize(), sort);
        Page<TransferDto> pageResult = transferRepository.findByStatus(req.getStatus(), pageable).map(this::toDto);
        return new PageDto<>(pageResult);
    }

    @Override
    public PageDto<TransferItemDto> getTransferItems(long id, TransferDetailRequest req) {
        InventoryTransferEntity transfer = transferRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Transfer not found"));
        Sort sort = Sort.by(
                req.getDir().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                req.getOrder()
        );
        PageRequest pageable = PageRequest.of(req.getPage() - 1, req.getSize(), sort);
        Page<TransferItemDto> pageResult = inventoryTransferItemRepository.findByTransferId(transfer.getId(), pageable).map(this::toTransferItemDto);
        return new PageDto<>(pageResult);
    }

    @Override
    @Transactional
    public void confirmTransfer(long transferId) {
        InventoryTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Transfer not found"));

        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Chỉ được Confirm phiếu DRAFT");
        }

        // 1. Kiểm tra xem Transfer này có gắn với Reservation nào không
        List<ReservationEntity> linkedReservations = reservationRepository.findByTransferId(transferId);
        boolean isReservationTransfer = !linkedReservations.isEmpty();

        // Lấy danh sách serial cần xử lý
        List<String> allSerials = new ArrayList<>();
        for (InventoryTransferItemEntity item : transfer.getItems()) {
            item.getProductSerials().forEach(s -> allSerials.add(s.getSerialNo()));
        }

        if (isReservationTransfer) {
            // ====================================================================
            // CASE A: TRANSFER TỪ ĐƠN HÀNG (Reservation)
            // ====================================================================
            // Đặc điểm: Serial ĐÃ LÀ RESERVED, Inventory ĐÃ TĂNG ReservedStock từ lúc tạo đơn.
            // Hành động: Chỉ cần kiểm tra tính hợp lệ (Validation), KHÔNG cập nhật lại trạng thái/kho.

            // Kiểm tra xem các serial này có đúng là đang RESERVED không
            // (Đề phòng trường hợp lỗi dữ liệu hoặc bị hủy ngang)
            long countReserved = productSerialRepository.countReservedSerials(
                    allSerials,
                    transfer.getSource().getId()
            );

            if (countReserved != allSerials.size()) {
                throw new AppException(ErrorCode.INTERNAL_ERROR,
                        "Dữ liệu không đồng bộ: Một số sản phẩm trong đơn hàng chưa được giữ chỗ (RESERVED). Vui lòng kiểm tra lại.");
            }

            // Cập nhật trạng thái các Reservation liên quan
            for (ReservationEntity res : linkedReservations) {
                res.setStatus(ReservationStatus.CONFIRMED);
                res.setUpdatedAt(LocalDateTime.now());
            }
            reservationRepository.saveAll(linkedReservations);

        } else {
            // ====================================================================
            // CASE B: TRANSFER THỦ CÔNG (Manual)
            // ====================================================================
            // Đặc điểm: Serial đang là AVAILABLE.
            // Hành động: Phải chuyển sang RESERVED và TĂNG ReservedStock.

            // 1. Tăng ReservedStock trong Inventory
            for (InventoryTransferItemEntity item : transfer.getItems()) {
                InventoryEntity inventory = inventoryRepository.findByInventoryLocationIdAndVariantId(transfer.getSource().getId(), item.getVariant().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Sản phẩm không có trong kho nguồn"));

                inventory.addReservedStock(item.getQuantity());
                inventoryRepository.save(inventory);
            }

            // 2. Lock Serial: Chuyển từ AVAILABLE -> RESERVED
            int updatedRows = productSerialRepository.reserveSerials(allSerials, transfer.getSource().getId());
            if (updatedRows != allSerials.size()) {
                throw new AppException(ErrorCode.INTERNAL_ERROR,
                        "Xung đột dữ liệu: Một số sản phẩm đã bị lấy mất trước khi Confirm.");
            }
        }

        // Cuối cùng: Cập nhật trạng thái Transfer
        transfer.setStatus(TransferStatus.CONFIRMED);
        transferRepository.save(transfer);
    }

    // ========================================================================
    // 1. START TRANSFER: Xuất kho đi -> Trừ tồn kho -> Serial ra đường
    // ========================================================================
    @Override
    @Transactional
    public void startTransfer(Long transferId) {
        InventoryTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy phiếu chuyển"));

        if (transfer.getStatus() != TransferStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Phiếu phải được CONFIRMED trước khi xuất kho");
        }

        InventoryLocationEntity sourceLoc = transfer.getSource();

        for (InventoryTransferItemEntity item : transfer.getItems()) {
            VariantEntity variant = item.getVariant();
            int qty = item.getQuantity();

            // A. TRỪ TỒN KHO (Inventory)
            InventoryEntity inv = inventoryRepository.findByInventoryLocationIdAndVariantId(sourceLoc.getId(), variant.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Dữ liệu tồn kho bị lỗi"));

            if (inv.getStock() < qty) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Tồn kho thực tế không đủ để xuất đi (Lỗi bất thường)");
            }
            inv.setStock(inv.getStock() - qty);
            inv.setReservedStock(inv.getReservedStock() - qty);
            inventoryRepository.save(inv);

            // B. GHI LOG MOVEMENT (OUT)
            createMovement(sourceLoc, variant, -qty, "TRANSFER_OUT", transfer.getId());

            // C. UPDATE SERIAL (RESERVED -> IN_TRANSFER, Location -> NULL)
            List<String> serials = item.getProductSerials().stream()
                    .map(ProductSerialEntity::getSerialNo).toList();

            productSerialRepository.updateSerialsForShipping(serials, sourceLoc.getId());
        }

        List<ReservationEntity> linkedReservations = reservationRepository.findByTransferId(transferId);
        if (!linkedReservations.isEmpty()) {
            for (ReservationEntity res : linkedReservations) {
                res.setStatus(ReservationStatus.TRANSFERRING);
                res.setUpdatedAt(LocalDateTime.now());
            }
            reservationRepository.saveAll(linkedReservations);
        }

        // D. UPDATE TRANSFER STATUS
        transfer.setStatus(TransferStatus.TRANSFERRING);
        transfer.setUpdatedAt(LocalDateTime.now());
        transferRepository.save(transfer);
    }

    // ========================================================================
    // 2. COMPLETE TRANSFER: Nhập kho đến -> Cộng tồn kho -> Serial về kho mới
    // ========================================================================
    @Override
    @Transactional
    public void completeTransfer(Long transferId) {
        InventoryTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy phiếu chuyển"));

        if (transfer.getStatus() != TransferStatus.TRANSFERRING) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Phiếu chưa được xuất đi (TRANSFERRING)");
        }

        InventoryLocationEntity destLoc = transfer.getDestination();

        // KIỂM TRA: Phiếu chuyển có gắn với đơn hàng không?
        List<ReservationEntity> linkedReservations = reservationRepository.findByTransferId(transferId);
        boolean isReservationTransfer = !linkedReservations.isEmpty();

        for (InventoryTransferItemEntity item : transfer.getItems()) {
            VariantEntity variant = item.getVariant();
            int qty = item.getQuantity();

            // --- A. CỘNG TỒN KHO ---
            InventoryEntity inv = inventoryRepository.findByInventoryLocationIdAndVariantId(destLoc.getId(), variant.getId())
                    .orElseGet(() -> {
                        InventoryEntity newInv = new InventoryEntity();
                        newInv.setInventoryLocation(destLoc);
                        newInv.setVariant(variant);
                        newInv.setStock(0);
                        return newInv;
                    });

            inv.setStock(inv.getStock() + qty);
            inventoryRepository.save(inv);

            // --- B. MOVEMENT ---
            createMovement(destLoc, variant, qty, "TRANSFER_IN", transfer.getId());

            // --- C. UPDATE SERIAL ---
            List<String> serials = item.getProductSerials().stream()
                    .map(ProductSerialEntity::getSerialNo).toList();

            if (isReservationTransfer) {
                // SERIAL PHẢI GIỮ LẠI CHO ĐƠN HÀNG
                productSerialRepository.updateSerialsForStoreReservation(serials, destLoc);

            } else {
                // CHUYỂN KHO BÌNH THƯỜNG → serial available
                productSerialRepository.updateSerialsForReceiving(serials, destLoc);
            }
        }

        // --- D. CẬP NHẬT RESERVATION ---
        if (isReservationTransfer) {
            for (ReservationEntity res : linkedReservations) {
                res.setStatus(ReservationStatus.AVAILABLE); // mới đúng nghiệp vụ
                res.setUpdatedAt(LocalDateTime.now());
            }
            reservationRepository.saveAll(linkedReservations);
        }

        // --- E. HOÀN THÀNH PHIẾU ---
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setUpdatedAt(LocalDateTime.now());
        transferRepository.save(transfer);
    }


    // ========================================================================
    // 3. DELETE TRANSFER: Chỉ xoá khi DRAFT (Chưa đụng vào kho/serial)
    // ========================================================================
    @Override
    @Transactional
    public void deleteTransfer(Long transferId) {
        InventoryTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy phiếu chuyển"));

        // Kiểm tra điều kiện tiên quyết
        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Chỉ được xoá phiếu khi đang ở trạng thái DRAFT");
        }

        // Vì ở trạng thái DRAFT:
        // 1. Serial đang là AVAILABLE (chưa bị RESERVED) -> Không cần làm gì bảng ProductSerial.
        // 2. Inventory chưa bị trừ -> Không cần hoàn kho.

        // Xoá các Items trước (Nếu JPA Cascade không được config là DELETE/ALL)
        inventoryTransferItemRepository.deleteAll(transfer.getItems());
        List<ReservationEntity> linkedReservations = reservationRepository.findByTransferId(transferId);
        if (!linkedReservations.isEmpty()) {
            for (ReservationEntity res : linkedReservations) {
                res.setTransfer(null);
                res.setStatus(ReservationStatus.PENDING);
                res.setUpdatedAt(LocalDateTime.now());
            }
            reservationRepository.saveAll(linkedReservations);
        }
        // Xoá phiếu chuyển
        transferRepository.delete(transfer);
    }

    // --- Helper Method ---
    private void createMovement(InventoryLocationEntity loc, VariantEntity variant, int qtyDelta, String reason, Long refId) {
        StockMovementEntity mov = new StockMovementEntity();
        mov.setInventoryLocation(loc);
        mov.setVariant(variant);
        mov.setQuantityDelta(qtyDelta);
        mov.setReason(reason);
        mov.setRefType("TRANSFER");
        mov.setRefId(refId);
        mov.setCreatedAt(LocalDateTime.now());
        stockMovementRepository.save(mov);
    }

    public TransferDto toDto(InventoryTransferEntity e){
        return TransferDto.builder()
                .id(e.getId())
                .source(inventoryLocationService.toDto(e.getSource()))
                .destination(inventoryLocationService.toDto(e.getDestination()))
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
    public TransferItemDto toTransferItemDto(InventoryTransferItemEntity e){
        VariantEntity variant = e.getVariant();
        return TransferItemDto.builder()
                .id(e.getId())
                .variantId(variant.getId())
                .sku(variant.getSku())
                .thumbnail(variant.getThumbnail())
                .quantity(e.getQuantity())
                .attributes(
                        variant.getVariantAttributeValues().stream()
                                .map(vav -> VariantDto.AttributeDto.builder()
                                        .code(vav.getAttribute().getCode())
                                        .label(vav.getAttribute().getLabel())
                                        .value(vav.getAttributeValue().getLabel())
                                        .build())
                                .toList()
                )
                .build();
    }
    @Override
    @Transactional
    public void updateTransferStatus(Long transferId, TransferStatus newStatus) {
        InventoryTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy phiếu chuyển"));

        if (transfer.getStatus() == newStatus) {
            log.warn("Transfer {} đã ở trạng thái {}. Bỏ qua.", transferId, newStatus);
            return;
        }

        // Dùng switch-case để gọi hàm xử lý tương ứng
        switch (newStatus) {
            case CONFIRMED:
                handleConfirm(transfer);
                break;
            case TRANSFERRING:
                handleStartTransfer(transfer);
                break;
            case COMPLETED:
                handleCompleteTransfer(transfer);
                break;
            case CANCELLED:
                handleCancel(transfer);
                break;
            default:
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Trạng thái cập nhật không được hỗ trợ: " + newStatus);
        }

        // Cập nhật trạng thái và thời gian cuối cùng
        transfer.setStatus(newStatus);
        transfer.setUpdatedAt(LocalDateTime.now());
        transferRepository.save(transfer);

        // Tự động cập nhật Order mẹ (nếu có)
        List<ReservationEntity> linkedReservations = reservationRepository.findByTransferId(transferId);
        if (!linkedReservations.isEmpty()) {
            OrderEntity order = linkedReservations.get(0).getOrder();
            checkAndUpdateOrderStatus(order);
        }
    }

    // ========================================================================
    // CÁC HÀM XỬ LÝ LOGIC (HELPER)
    // ========================================================================

    /**
     * Xử lý logic DRAFT -> CONFIRMED
     */
    private void handleConfirm(InventoryTransferEntity transfer) {
        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Chỉ được Confirm phiếu DRAFT");
        }

        List<ReservationEntity> linkedReservations = reservationRepository.findByTransferId(transfer.getId());
        boolean isReservationTransfer = !linkedReservations.isEmpty();

        List<String> allSerials = new ArrayList<>();
        transfer.getItems().forEach(item ->
                item.getProductSerials().forEach(s -> allSerials.add(s.getSerialNo()))
        );

        if (isReservationTransfer) {
            // CASE A: TRANSFER TỪ ĐƠN HÀNG (Serial đã RESERVED)
            long countReserved = productSerialRepository.countReservedSerials(allSerials, transfer.getSource().getId());
            if (countReserved != allSerials.size()) {
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi đồng bộ: Serial không ở trạng thái RESERVED.");
            }
            // Cập nhật Reservation -> CONFIRMED
            linkedReservations.forEach(res -> {
                res.setStatus(ReservationStatus.CONFIRMED);
                res.setUpdatedAt(LocalDateTime.now());
            });
            reservationRepository.saveAll(linkedReservations);

        } else {
            // CASE B: TRANSFER THỦ CÔNG (Serial đang IN_STOCK)
            // 1. Tăng ReservedStock
            for (InventoryTransferItemEntity item : transfer.getItems()) {
                InventoryEntity inventory = inventoryRepository.findByInventoryLocationIdAndVariantId(transfer.getSource().getId(), item.getVariant().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Sản phẩm không có trong kho nguồn"));
                inventory.addReservedStock(item.getQuantity());
                inventoryRepository.save(inventory);
            }
            // 2. Lock Serial
            int updatedRows = productSerialRepository.reserveSerials(allSerials, transfer.getSource().getId());
            if (updatedRows != allSerials.size()) {
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Xung đột: Một số sản phẩm đã bị lấy mất.");
            }
        }
    }

    /**
     * Xử lý logic CONFIRMED -> TRANSFERRING
     */
    private void handleStartTransfer(InventoryTransferEntity transfer) {
        if (transfer.getStatus() != TransferStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Phiếu phải được CONFIRMED trước khi xuất kho");
        }

        for (InventoryTransferItemEntity item : transfer.getItems()) {
            // 1. Trừ tồn kho (Stock & ReservedStock)
            InventoryEntity inv = inventoryRepository.findByInventoryLocationIdAndVariantId(transfer.getSource().getId(), item.getVariant().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Dữ liệu tồn kho bị lỗi"));
            inv.setStock(inv.getStock() - item.getQuantity());
            inv.setReservedStock(inv.getReservedStock() - item.getQuantity());
            inventoryRepository.save(inv);

            // 2. Ghi Movement
            createMovement(transfer.getSource(), item.getVariant(), -item.getQuantity(), "TRANSFER_OUT", transfer.getId());

            // 3. Update Serial: RESERVED -> IN_TRANSFER
            List<String> serials = item.getProductSerials().stream()
                    .map(ProductSerialEntity::getSerialNo).toList();

            int updatedRows = productSerialRepository.updateSerialsForShipping(serials, transfer.getSource().getId());
            if (updatedRows != serials.size()) {
                log.warn("Lỗi khi xuất kho. Yêu cầu: {}, Thực tế: {}", serials.size(), updatedRows);
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi dữ liệu: Serial không ở trạng thái RESERVED.");
            }
        }

        // 4. Cập nhật Reservation (Nếu có)
        List<ReservationEntity> linkedReservations = reservationRepository.findByTransferId(transfer.getId());
        if (!linkedReservations.isEmpty()) {
            linkedReservations.forEach(res -> {
                res.setStatus(ReservationStatus.TRANSFERRING);
                res.setUpdatedAt(LocalDateTime.now());
            });
            reservationRepository.saveAll(linkedReservations);
        }
    }

    /**
     * Xử lý logic TRANSFERRING -> COMPLETED
     */
    private void handleCompleteTransfer(InventoryTransferEntity transfer) {
        if (transfer.getStatus() != TransferStatus.TRANSFERRING) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Phiếu chưa được xuất đi (TRANSFERRING)");
        }

        List<ReservationEntity> linkedReservations = reservationRepository.findByTransferId(transfer.getId());
        boolean isReservationTransfer = !linkedReservations.isEmpty();

        for (InventoryTransferItemEntity item : transfer.getItems()) {
            // 1. Cộng Tồn kho
            InventoryEntity inv = inventoryRepository.findByInventoryLocationIdAndVariantId(transfer.getDestination().getId(), item.getVariant().getId())
                    .orElseGet(() -> InventoryEntity.builder()
                            .inventoryLocation(transfer.getDestination())
                            .variant(item.getVariant())
                            .build());
            inv.setStock(inv.getStock() + item.getQuantity());
            inventoryRepository.save(inv);

            // 2. Ghi Movement
            createMovement(transfer.getDestination(), item.getVariant(), item.getQuantity(), "TRANSFER_IN", transfer.getId());

            // 3. Update Serial
            List<String> serials = item.getProductSerials().stream()
                    .map(ProductSerialEntity::getSerialNo).toList();

            int updatedRows;
            if (isReservationTransfer) {
                // CASE A: Đơn hàng -> Hàng về kho cửa hàng (IN_TRANSFER -> RESERVED)
                updatedRows = productSerialRepository.updateSerialsForStoreReservation(serials, transfer.getDestination());
            } else {
                // CASE B: Chuyển kho thủ công -> Hàng về kho (IN_TRANSFER -> IN_STOCK)
                updatedRows = productSerialRepository.updateSerialsForReceiving(serials, transfer.getDestination());
            }
            if (updatedRows != serials.size()) {
                log.warn("Lỗi khi nhập kho. Yêu cầu: {}, Thực tế: {}", serials.size(), updatedRows);
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi dữ liệu: Serial không ở trạng thái IN_TRANSFER.");
            }
        }

        // 4. Cập nhật Reservation
        if (isReservationTransfer) {
            linkedReservations.forEach(res -> {
                res.setStatus(ReservationStatus.AVAILABLE);
                res.setUpdatedAt(LocalDateTime.now());
            });
            reservationRepository.saveAll(linkedReservations);
        }
    }

    /**
     * Xử lý logic -> CANCELLED (Chỉ từ DRAFT)
     */
    private void handleCancel(InventoryTransferEntity transfer) {
        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Chỉ được Hủy phiếu khi đang ở trạng thái DRAFT");
        }

        // (Logic giống hệt hàm deleteTransfer, nhưng không xóa)
        List<ReservationEntity> linkedReservations = reservationRepository.findByTransferId(transfer.getId());
        if (!linkedReservations.isEmpty()) {
            for (ReservationEntity res : linkedReservations) {
                res.setTransfer(null);
                res.setStatus(ReservationStatus.PENDING); // Trả về PENDING
                res.setUpdatedAt(LocalDateTime.now());
            }
            reservationRepository.saveAll(linkedReservations);
        }
    }


    /**
     * Kiểm tra và cập nhật trạng thái Order mẹ dựa trên các Reservation con.
     */
    private void checkAndUpdateOrderStatus(OrderEntity order) {
        // Tải lại trạng thái mới nhất của Order và các Reservation của nó
        OrderEntity freshOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Order not found during status check"));

        List<ReservationEntity> allReservations = freshOrder.getReservations();
        if (allReservations.isEmpty()) return;

        boolean allReadyForPickup = true;
        boolean anyTransferring = false;
        boolean allCompletedOrCancelled = true;
        boolean hasActiveItems = false; // Check xem có item nào PENDING/CONFIRMED/TRANSFERRING/READY

        for (ReservationEntity res : allReservations) {
            if (res.getStatus() == ReservationStatus.TRANSFERRING) {
                anyTransferring = true;
            }
            if (res.getStatus() != ReservationStatus.AVAILABLE && res.getStatus() != ReservationStatus.CANCELLED) {
                allReadyForPickup = false;
            }
            if (res.getStatus() != ReservationStatus.COMPLETED && res.getStatus() != ReservationStatus.CANCELLED) {
                allCompletedOrCancelled = false;
            }
            if (res.getStatus() == ReservationStatus.PENDING || res.getStatus() == ReservationStatus.CONFIRMED ||
                res.getStatus() == ReservationStatus.TRANSFERRING || res.getStatus() == ReservationStatus.AVAILABLE) {
                hasActiveItems = true;
            }
        }

        // Logic cập nhật trạng thái
        if (anyTransferring) {
            freshOrder.setStatus(OrderStatus.DELIVERING);
            log.info("Order ID {} Cập nhật: DELIVERING (Đang vận chuyển)", freshOrder.getId());
        }
        else if (allReadyForPickup && order.getReceiveMethod() == ReceiveMethod.PICKUP) {
            freshOrder.setStatus(OrderStatus.READY_FOR_PICKUP);
            log.info("Order ID {} Cập nhật: READY_FOR_PICKUP (Sẵn sàng lấy hàng)", freshOrder.getId());
        }
        else if (!hasActiveItems && !allCompletedOrCancelled) {
            // (Logic cũ của bạn có vẻ hơi xung đột, tôi sửa lại)
            // Nếu TẤT CẢ đã COMPLETED hoặc CANCELLED
            if(allCompletedOrCancelled){
                freshOrder.setStatus(OrderStatus.COMPLETED);
                log.info("Order ID {} Cập nhật: COMPLETED (Hoàn thành)", freshOrder.getId());
            }
        }

        orderRepository.save(freshOrder);
    }
}

