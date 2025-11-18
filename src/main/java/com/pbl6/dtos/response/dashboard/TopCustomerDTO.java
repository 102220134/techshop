package com.pbl6.dtos.response.dashboard;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerDTO {

    private Long userId;

    private String name;

    private String email;

    private Long totalOrders;

    private Long totalSpent;
}
