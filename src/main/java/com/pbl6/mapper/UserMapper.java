package com.pbl6.mapper;

import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.user.UserAddressDto;
import com.pbl6.dtos.user.UserDto;
import com.pbl6.entities.OrderEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class UserMapper {
    private final UserAddressMapper userAddressMapper;

    public UserMapper(UserAddressMapper userAddressMapper) {
        this.userAddressMapper = userAddressMapper;
    }

    public UserEntity toUserEntity(RegisterRequest registerRequest){
        UserEntity userEntity = new UserEntity();
        userEntity.setName(registerRequest.getName());
        userEntity.setEmail(registerRequest.getEmail());
        userEntity.setPassword(registerRequest.getPassword());
        userEntity.setPhone(registerRequest.getPhone());
        return userEntity;
    };

    public UserDto toDto(UserEntity entity) {
        if (entity == null) return null;
        List<OrderEntity> ordersCompleted = entity.getOrders().stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .toList();
        BigDecimal totalAmountSpent = ordersCompleted.stream()
                .map(order -> order.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return UserDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .gender(entity.getGender())
                .birth(entity.getBirth())
                .avatar(entity.getAvatar())
                .isActive(entity.getIsActive())
                .isGuest(entity.getIsGuest())
                .totalOrders(ordersCompleted.size())
                .totalAmountSpent(totalAmountSpent)
                .roleName(entity.getRole() != null ? entity.getRole().getName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .addresses(
                        entity.getAddresses() != null
                                ? entity.getAddresses().stream()
                                .map(userAddressMapper::toDto)
                                .toList()
                                : null
                )
                .build();
    }


}
