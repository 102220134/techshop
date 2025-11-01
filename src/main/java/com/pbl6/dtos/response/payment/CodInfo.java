package com.pbl6.dtos.response.payment;

import com.pbl6.enums.PaymentMethod;
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
    private PaymentMethod type;
    private String label;
}
