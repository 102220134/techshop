package com.pbl6.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum ReservationStatus {

    DRAFT("draft", "Bản nháp"),
    PENDING("pending","Chờ xác nhận"),
    AVAILABLE("available", "Có sẵn"),
    TRANSFER("transfer", "Vân chuyển"),
    COMPLETED("completed", "Hoàn thành"),
    CANCELLED("cancelled", "Đã hủy");

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
