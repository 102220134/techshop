package com.pbl6.services.impl;

import com.pbl6.entities.InventoryLocationEntity;
import com.pbl6.entities.InventoryTransferItemEntity;
import com.pbl6.entities.OrderItemEntity;
import com.pbl6.entities.ProductSerialEntity;
import com.pbl6.enums.ProductSerialStatus;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.ProductSerialRepository;
import com.pbl6.services.ProductSerialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSerialServiceImpl implements ProductSerialService {

    private final ProductSerialRepository serialRepository;

    @Override
    @Transactional
    public List<ProductSerialEntity> reserveSerial( OrderItemEntity orderItem, Long locationId) {
        List<ProductSerialEntity> serials = serialRepository.findByVariantIdAndInventoryLocationIdAndStatus(
                orderItem.getVariant().getId(),
                locationId,
                ProductSerialStatus.IN_STOCK
        );

        if (serials.size() < orderItem.getQuantity()) {
            log.error("Product serial không đủ để giữ hàng");
            throw new AppException(ErrorCode.OVERSELL_PRODUCT_SERIAL);
        }

        List<ProductSerialEntity> updatedSerials = serials.stream()
                .limit(orderItem.getQuantity())
                .peek(ps -> {
                    ps.setOrderItem(orderItem);
                    ps.setStatus(ProductSerialStatus.RESERVED);
                })
                .toList();

        return serialRepository.saveAll(updatedSerials);
    }
}
