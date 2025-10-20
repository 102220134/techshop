package com.pbl6.exceptions;

import com.pbl6.dtos.response.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ErrorResponseApi> handleRuntimeException(Exception ex) {
        ErrorResponseApi response = new ErrorResponseApi();
        response.setCode(ErrorCode.UNCATEGORIZED.getCode());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED.getHttpStatus()).body(response);
    }
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ErrorResponseApi> handleAppException(final AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponseApi response = new ErrorResponseApi();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());
        response.setDetail(ex.getDetail());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponseApi> handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        String error = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        ErrorResponseApi response = new ErrorResponseApi();
        response.setCode(errorCode.getCode());
        response.setMessage(error);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public  ResponseEntity<ErrorResponseApi> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponseApi response = new ErrorResponseApi();
        response.setCode(ErrorCode.FORBIDDEN.getCode());
        response.setMessage(ErrorCode.FORBIDDEN.getMessage());
        return ResponseEntity.status(ErrorCode.FORBIDDEN.getHttpStatus()).body(response);
    }
}
