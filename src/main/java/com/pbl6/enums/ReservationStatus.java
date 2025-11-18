package com.pbl6.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    DRAFT,
    PENDING,
    CONFIRMED,
    AVAILABLE,
    TRANSFERRING,
    COMPLETED,
    CANCELLED
}
