package com.pbl6.services.impl;

import com.pbl6.dtos.request.inventory.GR.CreateGRRequest;
import com.pbl6.dtos.request.inventory.GR.GRDetailRequest;
import com.pbl6.dtos.request.inventory.GR.ListGRRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.GR.GRDto;
import com.pbl6.dtos.response.inventory.GR.GRItemDto;
import com.pbl6.dtos.response.inventory.SupplierDto;
import com.pbl6.entities.*;
import com.pbl6.enums.GRStatus;
import com.pbl6.enums.ProductSerialStatus;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.GRMapper;
import com.pbl6.repositories.*;
import com.pbl6.services.GRService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GRServiceImpl implements GRService {
    private final GRRepository gRRepository;
    private final GRMapper gRMapper;
    private final GRItemRepository gRItemRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryLocationRepository inventoryLocationRepository;
    private final VariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductSerialRepository productSerialRepository;

    @Override
    public PageDto<GRDto> getGRs(ListGRRequest req) {
        Sort sort = Sort.by(
                req.getDir().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                req.getOrder()
        );
        PageRequest pageable = PageRequest.of(req.getPage() - 1, req.getSize(), sort);
        Page<GRDto> pageResult = gRRepository.findByStatus(req.getStatus(), pageable).map(gRMapper::toDto);
        return new PageDto<>(pageResult);
    }

    @Override
    public PageDto<GRItemDto> getGRItems(long id, GRDetailRequest req) {
        GoodsReceiptEntity gr = gRRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Goods receipt not found"));
        Sort sort = Sort.by(
                req.getDir().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                req.getOrder()
        );
        PageRequest pageable = PageRequest.of(req.getPage() - 1, req.getSize(), sort);
        Page<GRItemDto> pageResult = gRItemRepository.findByGoodsReceiptId(gr.getId(), pageable).map(gRMapper::grItemDto);
        return new PageDto<>(pageResult);
    }

    @Override
    public List<SupplierDto> getSupplier() {
        return supplierRepository.findAll().stream().map(s -> SupplierDto.builder()
                .id(s.getId())
                .email(s.getEmail())
                .name(s.getName())
                .displayAddress(s.getAddress())
                .taxCode(s.getTaxCode())
                .build()).toList();
    }

    @Transactional
    @Override
    public GRDto createGoodsReceipt(CreateGRRequest req) {
        SupplierEntity supplier = supplierRepository.findById(req.getSupplierId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Supplier not found"));

        InventoryLocationEntity location = inventoryLocationRepository.findById(req.getLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Location not found"));

        GoodsReceiptEntity receipt = new GoodsReceiptEntity();
        receipt.setSupplier(supplier);
        receipt.setInventoryLocation(location);
        receipt.setStatus(GRStatus.DRAFT);
        receipt.setNote(req.getNote());
        receipt.setCreatedAt(LocalDateTime.now());
        receipt.setUpdatedAt(LocalDateTime.now());

        gRRepository.save(receipt);

        List<GoodsReceiptItemEntity> itemEntities = new ArrayList<>();

        for (CreateGRRequest.CreateGRItemRequest item : req.getItems()) {
            VariantEntity variant = variantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Variant not found"));

            GoodsReceiptItemEntity itemEntity = new GoodsReceiptItemEntity();
            itemEntity.setGoodsReceipt(receipt);
            itemEntity.setVariant(variant);
            // Nếu có serial thì quantity = số lượng serial, nếu không thì dùng quantity từ request (cho sản phẩm ko serial)
//            int quantity = (item.getSerials() != null && !item.getSerials().isEmpty())
//                    ? item.getSerials().size()
//                    : item.getQuantity();
            itemEntity.setQuantity(item.getSerials().size());
            itemEntity.setUnitCost(item.getUnitCost());

            gRItemRepository.save(itemEntity);

            if (item.getSerials() != null && !item.getSerials().isEmpty()) {
                saveSerials(item.getSerials(), variant, location, itemEntity);
            }
            itemEntities.add(itemEntity);
        }

        receipt.setItems(itemEntities);
        gRRepository.save(receipt);

        return gRMapper.toDto(receipt);
    }

    @Transactional
    @Override
    public void deleteGoodsReceipt(Long id) {
        GoodsReceiptEntity gr = gRRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Goods receipt not found"));

        if (gr.getStatus() != GRStatus.DRAFT) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Chỉ được xoá phiếu nhập khi đang DRAFT");
        }
        // Xoá items (Cascade hoặc manual)
        gRItemRepository.deleteByGoodsReceiptId(gr.getId());
        gRRepository.delete(gr);
    }

    @Transactional
    @Override
    public void completeGoodsReceipt(Long id) {
        GoodsReceiptEntity gr = gRRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Goods receipt not found"));

        if (gr.getStatus() != GRStatus.DRAFT) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Phiếu nhập không ở trạng thái DRAFT");
        }

        InventoryLocationEntity location = gr.getInventoryLocation();

        for (GoodsReceiptItemEntity item : gr.getItems()) {
            int qty = item.getQuantity();
            VariantEntity variant = item.getVariant();

            // 1. Cập nhật tồn kho (Đã có Lock trong Repository)
            updateInventory(location, variant, qty);

            // 2. Thêm movement
            createMovement(variant, location, qty, "GOODS_RECEIPT", gr.getId());

            // 3. FIX CRITICAL: Kích hoạt Serial từ PENDING -> AVAILABLE
            productSerialRepository.updateStatusByGRItem(item.getId(), ProductSerialStatus.IN_STOCK);
        }

        gr.setStatus(GRStatus.COMPLETED);
        gr.setUpdatedAt(LocalDateTime.now());
        gRRepository.save(gr);
    }

    private void updateInventory(InventoryLocationEntity location, VariantEntity variant, int qty) {
        // Hàm này sẽ dùng Lock PESSIMISTIC_WRITE đã định nghĩa ở Repository
        InventoryEntity inv = inventoryRepository
                .findByInventoryLocationIdAndVariantId(location.getId(), variant.getId())
                .orElseGet(() -> {
                    InventoryEntity i = new InventoryEntity();
                    i.setVariant(variant);
                    i.setInventoryLocation(location);
                    i.setStock(0);
                    // i.setAvgCost(BigDecimal.ZERO);
                    return i;
                });

        int newQty = inv.getStock() + qty;
        inv.setStock(newQty);
        // Logic tính AvgCost có thể thêm lại ở đây nếu cần
        inventoryRepository.save(inv);
    }

    private void createMovement(VariantEntity variant, InventoryLocationEntity location,
                                Integer quantityDelta, String reason, Long refId) {
        StockMovementEntity mov = new StockMovementEntity();
        mov.setVariant(variant);
        mov.setInventoryLocation(location);
        mov.setQuantityDelta(quantityDelta);
        mov.setReason(reason);
        mov.setRefType("GOODS_RECEIPT");
        mov.setRefId(refId);
        mov.setCreatedAt(LocalDateTime.now());
        stockMovementRepository.save(mov);
    }

    // FIX: Tối ưu hiệu năng (Batch insert & Batch check)
    private void saveSerials(List<String> serials,
                             VariantEntity variant,
                             InventoryLocationEntity location,
                             GoodsReceiptItemEntity itemEntity) {

        // 1. Kiểm tra trùng lặp trong chính danh sách gửi lên
        Set<String> uniqueSerials = new HashSet<>(serials);
        if (uniqueSerials.size() < serials.size()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Danh sách serial gửi lên bị trùng lặp");
        }

        // 2. Kiểm tra trùng lặp với Database (Chỉ 1 query)
        List<String> existingSerials = productSerialRepository.findExistingSerialNos(serials);
        if (!existingSerials.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Serial đã tồn tại: " + String.join(", ", existingSerials));
        }

        // 3. Tạo Entity list và Save All (Chỉ 1 lệnh Insert batch nếu driver hỗ trợ tốt)
        List<ProductSerialEntity> entitiesToSave = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (String serial : serials) {
            ProductSerialEntity pse = new ProductSerialEntity();
            pse.setVariant(variant);
            pse.setInventoryLocation(location);
            pse.setGoodsReceiptItem(itemEntity);
            pse.setSerialNo(serial);
            pse.setStatus(ProductSerialStatus.PENDING); // Ban đầu là Pending
            pse.setCreatedAt(now);
            entitiesToSave.add(pse);
        }

        productSerialRepository.saveAll(entitiesToSave);
    }
}