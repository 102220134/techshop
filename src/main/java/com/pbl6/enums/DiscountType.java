package com.pbl6.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum DiscountType {

    AMOUNT,
    PERCENTAGE
}