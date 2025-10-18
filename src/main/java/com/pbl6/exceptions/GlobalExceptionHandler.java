package com.pbl6.exceptions;

import com.pbl6.dtos.response.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponseDto<?>> handleRuntimeException(Exception ex) {
        ApiResponseDto<?> response = new ApiResponseDto<>();
        response.setCode(ErrorCode.UNCATEGORIZED.getCode());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED.getHttpStatus()).body(response);
    }
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponseDto<?>> handleAppException(final AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponseDto<?> response = new ApiResponseDto<>();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponseDto<?>> handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        String error = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        ApiResponseDto<?> response = new ApiResponseDto<>();
        response.setCode(errorCode.getCode());
        response.setMessage(error);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}
