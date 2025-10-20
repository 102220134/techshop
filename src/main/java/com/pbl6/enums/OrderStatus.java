package com.pbl6.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public enum OrderStatus {

    PENDING("pending","Chờ xử lý"),
    CONFIRMED("confirmed","Đã xác nhận"),
    DELIVERING("delivered","Đã giao"),
    COMPLETED("completed","Hoàn tất"),
    CANCELLED("cancelled","Hủy"),
    REFUSED("refused","Từ chối"),
    RETURNED("returned","Trả hàng");

    private final String code;
    private final String label;

    OrderStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
