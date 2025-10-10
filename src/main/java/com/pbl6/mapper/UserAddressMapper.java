package com.pbl6.mapper;

import com.pbl6.dtos.request.checkout.CheckoutShipRequest;
import com.pbl6.entities.UserAddressEntity;
import org.springframework.stereotype.Component;

@Component
public class UserAddressMapper {
    public UserAddressEntity toEntity(CheckoutShipRequest req){
        return UserAddressEntity.builder()
                .country("VN")
                .province(req.getProvince())
                .district(req.getDistrict())
                .ward(req.getWard())
                .line1(req.getLine())
                .phone(req.getPhone())
                .fullName(req.getFullName())
                .build();
    }
}
