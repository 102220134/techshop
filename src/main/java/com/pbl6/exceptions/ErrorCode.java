package com.pbl6.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ===== Lỗi chung (General) =====
    NOT_FOUND(1001, "Resource not found", HttpStatus.NOT_FOUND),
    ALREADY_EXISTS(1002, "Resource already exists", HttpStatus.CONFLICT),
    INVALID_ID(1003, "Invalid identifier", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1004, "Unauthorized access", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1005, "Access forbidden", HttpStatus.FORBIDDEN),
    VALIDATION_ERROR(1006, "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT(1007, "Invalid format", HttpStatus.BAD_REQUEST),
    BUSINESS_RULE_VIOLATION(1008, "Business rule violation", HttpStatus.BAD_REQUEST),

    // ===== Lỗi hệ thống (System) =====
    INTERNAL_ERROR(2001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_ERROR(2002, "External service error", HttpStatus.BAD_GATEWAY),
    DATABASE_ERROR(2003, "Database error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(2004, "Service unavailable", HttpStatus.SERVICE_UNAVAILABLE),

    // ===== Lỗi không xác định =====
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