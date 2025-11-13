package com.pbl6.dtos.response.inventory;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class SupplierDto {
    private Long id;
    private String name;
    private String displayAddress;
    private String email;
    private String taxCode;
}
