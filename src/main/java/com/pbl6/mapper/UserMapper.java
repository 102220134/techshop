package com.pbl6.mapper;

import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.user.UserDetailDto;
import com.pbl6.dtos.user.UserDto;
import com.pbl6.entities.OrderEntity;
import com.pbl6.entities.RoleEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

    public UserDto toUserDto(UserEntity entity) {
        List<OrderEntity> ordersCompleted = Optional.ofNullable(entity.getOrders())
                .orElse(List.of())
                .stream()
                .filter(order -> order != null && OrderStatus.COMPLETED.equals(order.getStatus()))
                .toList();

        BigDecimal totalAmountSpent = ordersCompleted.stream()
                .map(order -> Optional.ofNullable(order.getTotalAmount()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return UserDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .isActive(entity.getIsActive())
                .totalOrders(ordersCompleted.size())
                .totalAmountSpent(totalAmountSpent)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public UserDetailDto toUserDetailDto(UserEntity entity) {
        List<OrderEntity> ordersCompleted = Optional.ofNullable(entity.getOrders())
                .orElse(List.of())
                .stream()
                .filter(order -> order != null && OrderStatus.COMPLETED.equals(order.getStatus()))
                .toList();

        BigDecimal totalAmountSpent = ordersCompleted.stream()
                .map(order -> Optional.ofNullable(order.getTotalAmount()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return UserDetailDto.builder()
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
                .roles(entity.getRoles().stream().map(RoleEntity::getName).toList())
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
