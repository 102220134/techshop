package com.pbl6.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ===== Lỗi người dùng (User) =====
    USER_EXISTED(1001, "User already exists", HttpStatus.CONFLICT),
    USER_NOT_FOUND(1002, "User not found", HttpStatus.NOT_FOUND),
    INVALID_USER_ID(1003, "Invalid user ID", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_USED(1004, "Email already in use", HttpStatus.CONFLICT),
    PHONE_ALREADY_USED(1005, "Phone number already in use", HttpStatus.CONFLICT),

    // ===== Lỗi xác thực & phân quyền (Auth & Security) =====
    UNAUTHORIZED(2001, "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(2002, "Forbidden", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(2003, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(2004, "Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(2005, "Invalid token", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(2006, "Access denied", HttpStatus.FORBIDDEN),

    // ===== Lỗi dữ liệu (Data & Validation) =====
    VALIDATION_ERROR(3001, "Validation error", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(3002, "Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT(3003, "Invalid phone number format", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD_MISSING(3004, "Required field is missing", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_FORMAT(3005, "Invalid password format", HttpStatus.BAD_REQUEST),
    DATA_NOT_FOUND(3006, "Data not found", HttpStatus.NOT_FOUND),
    DUPLICATE_DATA(3007, "Duplicate data", HttpStatus.CONFLICT),
    INVALID_STATUS(3008, "Invalid status value", HttpStatus.BAD_REQUEST),
    INVALID_PARAM(3009, "Invalid param", HttpStatus.BAD_REQUEST),

    // ===== Lỗi sản phẩm & kho (Product & Inventory) =====
    PRODUCT_NOT_FOUND(4001, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_OUT_OF_STOCK(4002, "Product out of stock", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_ID(4003, "Invalid product ID", HttpStatus.BAD_REQUEST),
    INVENTORY_NOT_FOUND(4004, "Inventory not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(4005, "Insufficient stock quantity", HttpStatus.BAD_REQUEST),

    // ===== Lỗi đơn hàng (Order) =====
    ORDER_NOT_FOUND(5001, "Order not found", HttpStatus.NOT_FOUND),
    INVALID_ORDER_ID(5002, "Invalid order ID", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_PROCESSED(5003, "Order already processed", HttpStatus.BAD_REQUEST),
    ORDER_CANCELLED(5004, "Order has been cancelled", HttpStatus.BAD_REQUEST),

    // ===== Lỗi Filestorge =====
    FILE_EMPTY(6001, "File empty", HttpStatus.BAD_REQUEST),
    INVALID_TYPE_FILE(6002, "Invalid type file", HttpStatus.BAD_REQUEST),
    SECURITY_PATH_TRAVERSAL(6003, "Security path traversal", HttpStatus.BAD_REQUEST),

    // ===== Lỗi hệ thống (System) =====
    INTERNAL_SERVER_ERROR(9001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(9002, "Database error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(9003, "Service unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    TIMEOUT_ERROR(9004, "Request timeout", HttpStatus.REQUEST_TIMEOUT),

    // ===== Lỗi chung =====
    UNCATEGORIZED(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
