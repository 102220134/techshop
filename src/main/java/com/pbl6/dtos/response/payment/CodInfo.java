package com.pbl6.dtos.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
@AllArgsConstructor
@Builder
public class CodInfo extends PaymentInfo{
    private String type;
    private String label;
}
