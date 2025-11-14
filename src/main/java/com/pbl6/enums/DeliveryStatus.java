package com.pbl6.enums;

public enum DeliveryStatus {
    PENDING,        // Mới tạo, chờ hãng đến lấy
    PICKED_UP,      // Hãng đã lấy hàng
    DELIVERING,     // Đang giao
    DELIVERED,      // Giao thành công
    FAILED,         // Giao thất bại (khách không nghe máy)
    RETURNED,       // Hoàn hàng về shop
    CANCELLED       // Hủy đơn
}