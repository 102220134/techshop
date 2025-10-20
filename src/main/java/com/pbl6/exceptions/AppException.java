package com.pbl6.exceptions;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public AppException(ErrorCode errorCode,String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }
    private final ErrorCode errorCode;
    private String detail;

}
