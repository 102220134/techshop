package com.pbl6.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class ErrorResponseApi {
    private String message;        // Message cụ thể
    private int errorCode;         // Mã lỗi số
    private String error;          // Tên mã lỗi (tùy chọn)
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private String path;
    private Map<String, String> details;

    public ErrorResponseApi(ErrorCode errorCode, String customMessage) {
        this.errorCode = errorCode.getCode();
        this.error = errorCode.name();
        this.message = customMessage != null ? customMessage : errorCode.getMessage();
    }

    public ErrorResponseApi(ErrorCode errorCode) {
        this(errorCode, null);
    }
}