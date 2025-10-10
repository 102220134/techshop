package com.pbl6.services.impl;

import com.pbl6.entities.*;
import com.pbl6.enums.TransferStatus;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.InventoryRepository;
import com.pbl6.repositories.InventoryTransferItemRepository;
import com.pbl6.repositories.TransferRepository;
import com.pbl6.services.ProductSerialService;
import com.pbl6.services.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {
    private final InventoryRepository inventoryRepository;
    private final TransferRepository transferRepository;
    private final ProductSerialService productSerialService;
    private final InventoryTransferItemRepository inventoryTransferItemRepository;

    @Override
    @Transactional
    public void createTransferForOrder(InventoryLocationEntity source, InventoryLocationEntity target, List<OrderItemEntity> remainingNeeds) {
        if (source == null) {
            throw new AppException(ErrorCode.INVENTORY_NOT_FOUND);
        }
        InventoryTransferEntity transfer = transferRepository.save(
                InventoryTransferEntity.builder()
                        .source(source)
                        .destination(target)
                        .status(TransferStatus.PENDING.getCode())
                        .build()
        );

        List<InventoryTransferItemEntity> transferItems = remainingNeeds.stream()
                .map(item -> createTransferItem(transfer, item, source.getId()))
                .toList();

        inventoryTransferItemRepository.saveAll(transferItems);

    }
    private InventoryTransferItemEntity createTransferItem(
            InventoryTransferEntity transfer,
            OrderItemEntity orderItem,
            Long sourceId) {

        InventoryEntity inventory = inventoryRepository.findByInventoryLocationIdAndVariantId(sourceId, orderItem.getVariant().getId())
                .orElseThrow(() -> new AppException(ErrorCode.STOCK_NOT_AVAILABLE));

        if (inventory.getAvailableStock() < orderItem.getQuantity()) {
            throw new AppException(ErrorCode.STOCK_NOT_AVAILABLE);
        }

        List<ProductSerialEntity> serials = productSerialService.reserveSerial(orderItem, sourceId);
        inventory.addReservedStock(orderItem.getQuantity());
        inventoryRepository.save(inventory);

        return InventoryTransferItemEntity.builder()
                .transfer(transfer)
                .variant(orderItem.getVariant())
                .quantity(orderItem.getQuantity())
                .productSerials(serials)
                .build();
    }

}

