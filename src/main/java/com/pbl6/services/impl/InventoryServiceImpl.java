package com.pbl6.services.impl;

import com.pbl6.entities.*;
import com.pbl6.enums.InventoryLocationType;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.InventoryRepository;
import com.pbl6.services.InventoryService;
import com.pbl6.services.ProductSerialService;
import com.pbl6.services.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final TransferService transferService;
    private final ProductSerialService serialService;

    /**
     * Xử lý khi khách hàng chọn nhận hàng tại cửa hàng
     */
    @Transactional
    @Override
    public void handlePickupAtStore(StoreEntity store, List<OrderItemEntity> orderItems) {
        List<OrderItemEntity> remainingNeeds = new ArrayList<>();

        for (OrderItemEntity orderItem : orderItems) {
            Long variantId = orderItem.getVariant().getId();
            int requestedQty = orderItem.getQuantity();

            InventoryEntity storeInventory = inventoryRepository
                    .findByInventoryLocationIdAndVariantId(store.getInventoryLocation().getId(), variantId)
                    .orElse(null);

            int available = (storeInventory == null) ? 0 : storeInventory.getAvailableStock();

            // ✅ Case 1: đủ hàng
            if (available >= requestedQty) {
                storeInventory.addReservedStock(requestedQty);
                serialService.reserveSerial(orderItem, store.getInventoryLocation().getId());
                log.info("[Pickup][Store:{}] Đủ hàng cho variant {} ({} pcs)", store.getName(), variantId, requestedQty);
                continue;
            }

            // ✅ Case 2: thiếu hàng một phần
            if (available > 0) {
                int needTransfer = requestedQty - available;

                storeInventory.addReservedStock(available);
                serialService.reserveSerial(sliceItem(orderItem, available), store.getInventoryLocation().getId());
                log.info("[Pickup][Store:{}] Thiếu hàng variant {}, cần chuyển thêm {} pcs", store.getName(), variantId, needTransfer);

                remainingNeeds.add(sliceItem(orderItem, needTransfer));
                continue;
            }

            // ✅ Case 3: hết hàng hoàn toàn
            log.info("[Pickup][Store:{}] Hết hàng variant {}, cần bổ sung toàn bộ {} pcs", store.getName(), variantId, requestedQty);
            remainingNeeds.add(orderItem);
        }

        // ✅ Nếu có hàng cần bổ sung
        if (!remainingNeeds.isEmpty()) {
            handleWarehouseFulfillment(store.getInventoryLocation(), remainingNeeds);
        }
    }

    /**
     * Xử lý khi đơn hàng cần giao tận nơi
     */
    @Transactional
    @Override
    public void handleShip(List<OrderItemEntity> orderItems) {
        if (orderItems.isEmpty()) return;
        handleWarehouseFulfillment(null, orderItems);
    }

    /**
     * Xử lý tìm kho và thực hiện transfer hoặc shipment tùy mục đích
     */
    private void handleWarehouseFulfillment(InventoryLocationEntity targetStore, List<OrderItemEntity> items) {
        Optional<InventoryLocationEntity> singleSource = findWarehouseThatCanFulfillAll(items);

        if (singleSource.isPresent()) {
            InventoryLocationEntity source = singleSource.get();

            // ✅ Nếu là pickup (có targetStore)
            if (targetStore != null) {
                transferService.createTransferForOrder(source, targetStore, items);
                log.info("[Transfer] Kho {} đủ toàn bộ hàng, tạo 1 transfer duy nhất → {}", source.getId(), targetStore.getId());
            } else {
                // ✅ Nếu là ship
                items.forEach(orderItem -> reserveFromInventory(source, orderItem));
                log.info("[Ship] Giao hàng trực tiếp từ kho {}", source.getId());
            }
        } else {
            log.info("[Fulfillment] Không có kho đủ tất cả → chia nhỏ thông minh...");
            handleFromMultipleSources(items, targetStore);
        }
    }

    /**
     * Tìm kho duy nhất có thể đáp ứng toàn bộ nhu cầu
     */
    private Optional<InventoryLocationEntity> findWarehouseThatCanFulfillAll(List<OrderItemEntity> needs) {
        // Gộp nhu cầu theo variant
        Map<Long, Integer> totalNeeds = needs.stream()
                .collect(Collectors.toMap(
                        i -> i.getVariant().getId(),
                        OrderItemEntity::getQuantity,
                        Integer::sum
                ));

        List<InventoryEntity> inventories = inventoryRepository.findByVariantIdIn(totalNeeds.keySet());
        List<InventoryEntity> warehouseInventories = inventories.stream()
                .filter(i -> InventoryLocationType.WAREHOUSE.getCode().equals(i.getInventoryLocation().getType()))
                .toList();

        if (warehouseInventories.isEmpty()) return Optional.empty();

        // Gom stock theo kho
        Map<InventoryLocationEntity, Map<Long, Integer>> stockByWarehouse = warehouseInventories.stream()
                .collect(Collectors.groupingBy(
                        InventoryEntity::getInventoryLocation,
                        Collectors.toMap(
                                i -> i.getVariant().getId(),
                                InventoryEntity::getAvailableStock,
                                Integer::sum
                        )
                ));

        // Tìm kho có đủ tất cả
        return stockByWarehouse.entrySet().stream()
                .filter(entry -> totalNeeds.entrySet().stream()
                        .allMatch(n -> entry.getValue().getOrDefault(n.getKey(), 0) >= n.getValue()))
                .max(Comparator.comparingInt(e -> e.getValue().values().stream().mapToInt(Integer::intValue).sum()))
                .map(Map.Entry::getKey);
    }

    /**
     * Chia nhỏ fulfillment từ nhiều kho (cho pickup hoặc ship)
     */
    private void handleFromMultipleSources(List<OrderItemEntity> items, InventoryLocationEntity targetStore) {
        for (OrderItemEntity orderItem : items) {
            Long variantId = orderItem.getVariant().getId();
            int required = orderItem.getQuantity();

            List<InventoryEntity> allInventories = inventoryRepository.findByVariantId(variantId);
            List<InventoryEntity> warehouses = allInventories.stream()
                    .filter(this::isWarehouse)
                    .sorted(Comparator.comparingInt(InventoryEntity::getAvailableStock).reversed())
                    .toList();

            List<InventoryEntity> stores = allInventories.stream()
                    .filter(this::isStore)
                    .sorted(Comparator.comparingInt(InventoryEntity::getAvailableStock).reversed())
                    .toList();

            int remaining = required;
            remaining = fulfillFromSources(orderItem, warehouses, targetStore, remaining);
            if (remaining > 0) remaining = fulfillFromSources(orderItem, stores, targetStore, remaining);

            if (remaining > 0) {
                log.error("[Inventory] Không đủ hàng cho variant {} (thiếu {})", variantId, remaining);
                throw new AppException(ErrorCode.STOCK_NOT_AVAILABLE);
            }
        }
    }

    /**
     * Fulfill hàng từ danh sách kho (warehouse/store)
     */
    private int fulfillFromSources(OrderItemEntity item, List<InventoryEntity> sources,
                                   InventoryLocationEntity targetStore, int remaining) {
        for (InventoryEntity src : sources) {
            int available = src.getAvailableStock();
            if (available <= 0) continue;

            int toUse = Math.min(available, remaining);
            OrderItemEntity sliced = sliceItem(item, toUse);

            if (targetStore != null) {
                // case pickup → transfer về cửa hàng
                transferService.createTransferForOrder(src.getInventoryLocation(), targetStore, List.of(sliced));
                log.info("[Transfer] {} pcs variant {} từ {} → {}",
                        toUse, item.getVariant().getId(),
                        src.getInventoryLocation().getId(),
                        targetStore.getId());
            } else {
                // case ship → giữ serial và reserve stock
                reserveFromInventory(src.getInventoryLocation(), sliced);
                log.info("[Ship] Reserve {} pcs variant {} từ {}",
                        toUse, item.getVariant().getId(), src.getInventoryLocation().getId());
            }

            remaining -= toUse;
            if (remaining <= 0) break;
        }
        return remaining;
    }

    /**
     * Đặt giữ hàng (reserved) và giữ serial
     */
    private void reserveFromInventory(InventoryLocationEntity location, OrderItemEntity orderItem) {
        InventoryEntity inv = inventoryRepository
                .findByInventoryLocationIdAndVariantId(location.getId(), orderItem.getVariant().getId())
                .orElseThrow(() -> new AppException(ErrorCode.STOCK_NOT_AVAILABLE));

        if (inv.getAvailableStock() < orderItem.getQuantity()) {
            throw new AppException(ErrorCode.STOCK_NOT_AVAILABLE);
        }

        inv.addReservedStock(orderItem.getQuantity());
        serialService.reserveSerial(orderItem, location.getId());
    }

    /**
     * Tạo bản sao order item với số lượng cắt
     */
    private OrderItemEntity sliceItem(OrderItemEntity src, int qty) {
        OrderItemEntity tmp = new OrderItemEntity();
        tmp.setId(src.getId());
        tmp.setVariant(src.getVariant());
        tmp.setQuantity(qty);
        return tmp;
    }

    private boolean isWarehouse(InventoryEntity i) {
        return InventoryLocationType.WAREHOUSE.getCode().equals(i.getInventoryLocation().getType());
    }

    private boolean isStore(InventoryEntity i) {
        return InventoryLocationType.STORE.getCode().equals(i.getInventoryLocation().getType());
    }
}
