package com.pbl6.dtos.response.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Builder
@Getter
@Setter
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private int totalOrders;
    private BigDecimal totalAmountSpent;
}
