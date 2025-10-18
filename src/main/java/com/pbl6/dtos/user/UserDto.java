package com.pbl6.dtos.user;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String gender;
    private LocalDate birth;
    private String avatar;
    private Boolean isActive;
    private Boolean isGuest;
    private String roleName;
    private int totalOrders;
    private BigDecimal totalAmountSpent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<UserAddressDto> addresses;
}
