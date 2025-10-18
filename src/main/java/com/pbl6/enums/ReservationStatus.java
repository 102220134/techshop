package com.pbl6.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum ReservationStatus {

    DRAFT("draft", "Bản nháp"),
    ACTIVE("active", "Hoạt động"),
    RELEASED("released", "Đã phát hành"),
    CANCELLED("cancelled", "Đã hủy"),
    FULFILLED("fulfilled", "Đã hoàn thành"),
    EXPIRED("expired", "Đã hết hạn");

    private final String code;
    private final String label;

    ReservationStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
    public static Optional<ReservationStatus> fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.getCode().equalsIgnoreCase(code))
                .findFirst();
    }
}
