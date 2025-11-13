package com.pbl6.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    DRAFT,
    PENDING,
    CONFIRMED,
    READY_FOR_PICKUP,
    TRANSFERRING,
    COMPLETED,
    CANCELLED
}
