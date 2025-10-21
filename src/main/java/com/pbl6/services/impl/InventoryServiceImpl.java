package com.pbl6.services.impl;

import com.pbl6.entities.*;
import com.pbl6.enums.InventoryLocationType;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.InventoryRepository;
import com.pbl6.repositories.VariantRepository;
import com.pbl6.services.InventoryService;
import com.pbl6.services.ProductSerialService;
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
    private final ProductSerialService serialService;
    private final VariantRepository variantRepository;

    /**
     * --------------------------
     * 1️⃣ Xử lý pickup tại cửa hàng
     * --------------------------
     */
    @Transactional
    @Override
    public void handlePickupAtStore(StoreEntity store, List<OrderItemEntity> orderItems) {
        List<OrderItemEntity> remainingNeeds = new ArrayList<>();

        for (OrderItemEntity item : orderItems) {
            Long variantId = item.getVariant().getId();
            int requestedQty = item.getQuantity();

            InventoryEntity inv = inventoryRepository
                    .findByInventoryLocationIdAndVariantId(store.getInventoryLocation().getId(), variantId)
                    .orElse(null);

            int available = (inv == null) ? 0 : inv.getAvailableStock();

            if (available >= requestedQty) { // đủ hàng
                reserveStock(inv, item, requestedQty);
                log.info("[Pickup][{}] Đủ hàng variant {} ({} pcs)", store.getName(), variantId, requestedQty);
            } else if (available > 0) { // thiếu một phần
                reserveStock(inv, item, available);
                remainingNeeds.add(sliceItem(item, requestedQty - available));
                log.info("[Pickup][{}] Thiếu variant {}, cần chuyển thêm {}", store.getName(), variantId, requestedQty - available);
            } else { // hết hàng
                remainingNeeds.add(item);
                log.info("[Pickup][{}] Hết hàng variant {}, cần bổ sung {}", store.getName(), variantId, requestedQty);
            }
        }

        if (!remainingNeeds.isEmpty()) handleWarehouseFulfillment(store.getInventoryLocation(), remainingNeeds);
    }

    /**
     * --------------------------
     * 2️⃣ Xử lý giao tận nơi
     * --------------------------
     */
    @Transactional
    @Override
    public void handleShip(List<OrderItemEntity> orderItems) {
        if (!orderItems.isEmpty()) handleWarehouseFulfillment(null, orderItems);
    }

    @Override
    public boolean isInStock(Long variantId, Integer quantity) {
        VariantEntity variant = variantRepository.findByIdAndIsActive(variantId, true).orElseThrow(
                () -> new AppException(ErrorCode.PRODUCT_NOT_FOUND)
        );
        return variant.getAvailableStock() > 0;
    }

    /**
     * --------------------------
     * 3️⃣ Fulfillment (Transfer hoặc Ship)
     * --------------------------
     */
    private void handleWarehouseFulfillment(InventoryLocationEntity targetStore, List<OrderItemEntity> items) {
        findWarehouseThatCanFulfillAll(items).ifPresentOrElse(source -> {
            items.forEach(item -> {
                InventoryEntity inv = inventoryRepository
                        .findByInventoryLocationIdAndVariantId(source.getId(), item.getVariant().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.STOCK_NOT_AVAILABLE));
                reserveStock(inv, item, item.getQuantity());
            });

            if (targetStore != null)
                log.info("[Transfer] Kho {} đủ hàng → chuyển 1 lần về {}", source.getId(), targetStore.getId());
            else
                log.info("[Ship] Giao trực tiếp từ kho {}", source.getId());
        }, () -> {
            log.info("[Fulfillment] Không kho nào đủ tất cả → chia nhỏ...");
            handleFromMultipleSources(items, targetStore);
        });
    }

    /**
     * --------------------------
     * 4️⃣ Tìm kho đủ hàng cho toàn bộ nhu cầu
     * --------------------------
     */
    private Optional<InventoryLocationEntity> findWarehouseThatCanFulfillAll(List<OrderItemEntity> needs) {
        Map<Long, Integer> totalNeeds = needs.stream()
                .collect(Collectors.toMap(i -> i.getVariant().getId(), OrderItemEntity::getQuantity, Integer::sum));

        List<InventoryEntity> warehouseInventories = inventoryRepository.findByVariantIdIn(totalNeeds.keySet()).stream()
                .filter(this::isWarehouse)
                .toList();

        if (warehouseInventories.isEmpty()) return Optional.empty();

        Map<InventoryLocationEntity, Map<Long, Integer>> stockByWarehouse = warehouseInventories.stream()
                .collect(Collectors.groupingBy(
                        InventoryEntity::getInventoryLocation,
                        Collectors.toMap(
                                i -> i.getVariant().getId(),
                                InventoryEntity::getAvailableStock,
                                Integer::sum
                        )
                ));

        return stockByWarehouse.entrySet().stream()
                .filter(entry -> totalNeeds.entrySet().stream()
                        .allMatch(n -> entry.getValue().getOrDefault(n.getKey(), 0) >= n.getValue()))
                .max(Comparator.comparingInt(e -> e.getValue().values().stream().mapToInt(Integer::intValue).sum()))
                .map(Map.Entry::getKey);
    }

    /**
     * --------------------------
     * 5️⃣ Chia nhỏ fulfillment từ nhiều kho
     * --------------------------
     */
    private void handleFromMultipleSources(List<OrderItemEntity> items, InventoryLocationEntity targetStore) {
        for (OrderItemEntity item : items) {
            Long variantId = item.getVariant().getId();
            int remaining = item.getQuantity();

            List<InventoryEntity> sources = inventoryRepository.findByVariantId(variantId).stream()
                    .sorted(Comparator.comparingInt(InventoryEntity::getAvailableStock).reversed())
                    .toList();

            remaining = fulfillFromSources(item, filterWarehouses(sources), targetStore, remaining);
            if (remaining > 0)
                remaining = fulfillFromSources(item, filterStores(sources), targetStore, remaining);

            if (remaining > 0)
                throw new AppException(ErrorCode.STOCK_NOT_AVAILABLE);
        }
    }

    /**
     * --------------------------
     * 6️⃣ Fulfill từ nhiều nguồn
     * --------------------------
     */
    private int fulfillFromSources(OrderItemEntity item, List<InventoryEntity> sources,
                                   InventoryLocationEntity targetStore, int remaining) {
        for (InventoryEntity src : sources) {
            if (remaining <= 0 || src.getAvailableStock() <= 0) continue;

            int toUse = Math.min(src.getAvailableStock(), remaining);
            OrderItemEntity sliced = sliceItem(item, toUse);

            if (targetStore != null) { // pickup → transfer
                reserveStock(src, sliced, toUse);
                log.info("[Transfer] {} pcs variant {} từ {} → {}", toUse, item.getVariant().getId(),
                        src.getInventoryLocation().getId(), targetStore.getId());
            } else { // ship
                reserveFromInventory(src.getInventoryLocation(), sliced);
                log.info("[Ship] Reserve {} pcs variant {} từ {}", toUse, item.getVariant().getId(),
                        src.getInventoryLocation().getId());
            }

            remaining -= toUse;
        }
        return remaining;
    }

    /**
     * --------------------------
     * 7️⃣ Helper Methods
     * --------------------------
     */
    private void reserveStock(InventoryEntity inv, OrderItemEntity item, int qty) {
        inv.addReservedStock(qty);
        serialService.reserveSerial(sliceItem(item, qty), inv.getInventoryLocation());
    }

    private void reserveFromInventory(InventoryLocationEntity location, OrderItemEntity item) {
        InventoryEntity inv = inventoryRepository
                .findByInventoryLocationIdAndVariantId(location.getId(), item.getVariant().getId())
                .orElseThrow(() -> new AppException(ErrorCode.STOCK_NOT_AVAILABLE));

        if (inv.getAvailableStock() < item.getQuantity())
            throw new AppException(ErrorCode.STOCK_NOT_AVAILABLE);

        reserveStock(inv, item, item.getQuantity());
    }

    private OrderItemEntity sliceItem(OrderItemEntity src, int qty) {
        OrderItemEntity copy = new OrderItemEntity();
        copy.setOrder(src.getOrder());
        copy.setId(src.getId());
        copy.setVariant(src.getVariant());
        copy.setQuantity(qty);
        return copy;
    }

    private boolean isWarehouse(InventoryEntity i) {
        return InventoryLocationType.WAREHOUSE.getCode().equals(i.getInventoryLocation().getType());
    }

    private boolean isStore(InventoryEntity i) {
        return InventoryLocationType.STORE.getCode().equals(i.getInventoryLocation().getType());
    }

    private List<InventoryEntity> filterWarehouses(List<InventoryEntity> list) {
        return list.stream().filter(this::isWarehouse).toList();
    }

    private List<InventoryEntity> filterStores(List<InventoryEntity> list) {
        return list.stream().filter(this::isStore).toList();
    }
}
