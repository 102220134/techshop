package com.pbl6.services.impl;

import com.pbl6.entities.VariantEffectivePriceEntity;
import com.pbl6.repositories.VariantEffectivePriceRepository;
import com.pbl6.services.VariantEffectivePriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VariantEffectivePriceServiceImpl implements VariantEffectivePriceService {
    private final VariantEffectivePriceRepository variantEffectivePriceRepository;

    @Override
    public BigDecimal getDiscountedPrice(Long variantId) {
       Optional<VariantEffectivePriceEntity> vep = variantEffectivePriceRepository.findById(variantId);
       if(vep.isPresent()){
           return vep.orElseThrow().getEffectivePrice();
       }else return BigDecimal.ZERO;
    }
}
