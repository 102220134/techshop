package com.pbl6.dtos.response.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class UserDto {
    private Long id;
    private String gender;
    private LocalDate birth;
    private List<String> roles;
    private List<Long> scops;
    private String name;
    private String email;
    private String phone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private int totalOrders;
    private BigDecimal totalAmountSpent;
}
