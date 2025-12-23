package com.pbl6.services.impl;

import com.pbl6.dtos.request.promotion.PromotionRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.promotion.PromotionResponse;
import com.pbl6.dtos.response.promotion.PromotionTargetDto;
import com.pbl6.entities.PromotionEntity;
import com.pbl6.entities.PromotionTargetEntity;
import com.pbl6.enums.TargetType;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.PromotionMapper;
import com.pbl6.repositories.PromotionRepository;
import com.pbl6.repositories.PromotionTargetRepository;
import com.pbl6.services.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final PromotionTargetRepository promotionTargetRepository;

    @Override
    public Map<Long, List<PromotionEntity>> getActivePromotionsGroupedByProduct(List<Long> productIds) {
        List<PromotionEntity> entities = promotionRepository.findActivePromotionsForProducts(productIds);

        // Map productId -> promotions áp dụng
        Map<Long, List<PromotionEntity>> result = new HashMap<>();

        for (PromotionEntity promo : entities) {
            for (PromotionTargetEntity target : promo.getTargets()) {

                // Nếu là GLOBAL → áp dụng cho tất cả sản phẩm
                if (target.getTargetType() == TargetType.GLOBAL) {
                    for (Long pid : productIds) {
                        result.computeIfAbsent(pid, k -> new ArrayList<>())
                                .add(promo);
                    }
                }

                // Nếu target là PRODUCT
                else if (target.getTargetType() == TargetType.PRODUCT
                         && productIds.contains(target.getTargetId())) {
                    result.computeIfAbsent(target.getTargetId(), k -> new ArrayList<>())
                            .add(promo);
                }
            }
        }

        return result;
    }

    @Override
    public BigDecimal calculateFinalPrice(BigDecimal basePrice, List<PromotionEntity> promotions) {
        if (basePrice == null || promotions == null || promotions.isEmpty()) return basePrice;

        List<PromotionEntity> sortedPromos = promotions.stream()
                .sorted(Comparator.comparingInt(p -> Optional.ofNullable(p.getPriority()).orElse(0)))
                .toList();

        BigDecimal currentPrice = basePrice;

        for (PromotionEntity promo : sortedPromos) {
            currentPrice = applyPromotion(currentPrice, promo);
            if (Boolean.TRUE.equals(promo.getExclusive())) break;
        }

        return currentPrice.max(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        PromotionEntity promotion = mapToEntity(request);
        PromotionEntity saved = promotionRepository.save(promotion);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
        PromotionEntity existing = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setDiscountType(request.getDiscountType());
        existing.setDiscountValue(request.getDiscountValue());
        existing.setMaxDiscountValue(request.getMaxDiscountValue());
        existing.setPriority(request.getPriority());
        existing.setExclusive(request.getExclusive());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setActive(request.isActive());

        existing.getTargets().clear();

        if (request.getTargets() != null && !request.getTargets().isEmpty()) {
            List<PromotionTargetEntity> newTargets = request.getTargets().stream()
                    .map(dto -> PromotionTargetEntity.builder()
                            .targetType(dto.getTargetType())
                            .targetId(dto.getTargetId())
                            .promotion(existing)
                            .build())
                    .toList();

            existing.getTargets().addAll(newTargets);
        }

        return mapToResponse(promotionRepository.save(existing));
    }

    @Override
    @Transactional
    public void deletePromotion(Long id) {
        PromotionEntity existing = promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR,"Promotion not found"));

        if (!existing.getOrderItems().isEmpty()) {
            existing.setActive(false);
            promotionRepository.save(existing);
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR,"Không thể xoá vì có đơn hàng đã áp dụng khuyến mãi");
        }

        promotionRepository.delete(existing);
    }

    @Override
    public PromotionResponse getById(Long id) {
        return promotionRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
    }

    @Override
    public PageDto<PromotionResponse> getAll(Pageable pageable) {
        return new PageDto<>( promotionRepository.findAll(pageable).map(this::mapToResponse));
    }
    private PromotionEntity mapToEntity(PromotionRequest req) {
        PromotionEntity entity = PromotionEntity.builder()
                .name(req.getName())
                .description(req.getDescription())
                .discountType(req.getDiscountType())
                .discountValue(req.getDiscountValue())
                .maxDiscountValue(req.getMaxDiscountValue())
                .priority(req.getPriority())
                .exclusive(req.getExclusive())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .isActive(req.isActive())
                .targets(new ArrayList<>())
                .build();

        if (req.getTargets() != null) {
            req.getTargets().forEach(t -> entity.getTargets().add(
                    PromotionTargetEntity.builder()
                            .targetType(t.getTargetType())
                            .targetId(t.getTargetId())
                            .promotion(entity)
                            .build()
            ));
        }
        return entity;
    }

    private PromotionResponse mapToResponse(PromotionEntity entity) {
        PromotionResponse res = new PromotionResponse();
        BeanUtils.copyProperties(entity, res); // Sử dụng Spring BeanUtils cho nhanh
        if (entity.getTargets() != null) {
            res.setTargets(entity.getTargets().stream().map(t -> {
                PromotionTargetDto dto = new PromotionTargetDto();
                dto.setTargetType(t.getTargetType());
                dto.setTargetId(t.getTargetId());
                return dto;
            }).toList());
        }
        return res;
    }

    private BigDecimal applyPromotion(BigDecimal basePrice, PromotionEntity promo) {
        if (promo == null || basePrice == null) return basePrice;

        BigDecimal discount = BigDecimal.ZERO;

        switch (promo.getDiscountType()) {
            case PERCENTAGE -> discount = basePrice
                    .multiply(promo.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            case AMOUNT-> discount = promo.getDiscountValue();
        }

        if (promo.getMaxDiscountValue() != null &&
            discount.compareTo(promo.getMaxDiscountValue()) > 0) {
            discount = promo.getMaxDiscountValue();
        }

        return basePrice.subtract(discount).max(BigDecimal.ZERO);
    }



}
