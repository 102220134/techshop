package com.pbl6.mapper;

import com.pbl6.dtos.request.checkout.CheckoutShipRequest;
import com.pbl6.dtos.user.UserAddressDto;
import com.pbl6.entities.UserAddressEntity;
import org.springframework.stereotype.Component;

@Component
public class UserAddressMapper {
    public UserAddressEntity toEntity(CheckoutShipRequest req){
        return UserAddressEntity.builder()
                .province(req.getProvince())
                .district(req.getDistrict())
                .ward(req.getWard())
                .line(req.getLine())
                .build();
    }

    public UserAddressDto toDto(UserAddressEntity addr){
        return UserAddressDto.builder()
                .id(addr.getId())
                .line(addr.getLine())
                .ward(addr.getWard())
                .district(addr.getDistrict())
                .province(addr.getProvince())
                .isDefault(addr.getIsDefault())
                .createdAt(addr.getCreatedAt())
                .updatedAt(addr.getUpdatedAt())
                .build();
    }
}
