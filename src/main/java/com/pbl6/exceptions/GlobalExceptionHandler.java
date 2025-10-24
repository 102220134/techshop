package com.pbl6.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ErrorResponseApi> handleRuntimeException(Exception ex, WebRequest request) {
        log.error("Uncategorized error: ", ex);

        ErrorResponseApi response = new ErrorResponseApi();
        response.setErrorCode(ErrorCode.UNCATEGORIZED.getCode());
        response.setMessage(ex.getMessage());
        response.setPath(getRequestPath(request));
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED.getHttpStatus()).body(response);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ErrorResponseApi> handleAppException(final AppException ex, WebRequest request) {
        ErrorCode errorCode = ex.getErrorCode();

        ErrorResponseApi response = new ErrorResponseApi();
        response.setErrorCode(errorCode.getCode());
        response.setMessage(ex.getMessage() != null ? ex.getMessage() : errorCode.getMessage());
        response.setPath(getRequestPath(request));
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponseApi> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException ex, WebRequest request) {

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        ErrorResponseApi response = new ErrorResponseApi();
        response.setErrorCode(errorCode.getCode());
        response.setMessage("Validation failed");
        response.setPath(getRequestPath(request));

        // Thêm chi tiết lỗi validation
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        response.setDetails(errors);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponseApi> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        ErrorResponseApi response = new ErrorResponseApi();
        response.setErrorCode(ErrorCode.FORBIDDEN.getCode());
        response.setMessage(ErrorCode.FORBIDDEN.getMessage());
        response.setPath(getRequestPath(request));
        return ResponseEntity.status(ErrorCode.FORBIDDEN.getHttpStatus()).body(response);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    ResponseEntity<ErrorResponseApi> handleUsernameNotFound(UsernameNotFoundException ex, WebRequest request) {
        ErrorResponseApi response = new ErrorResponseApi();
        response.setErrorCode(ErrorCode.NOT_FOUND.getCode());
        response.setMessage("User not found");
        response.setPath(getRequestPath(request));
        return ResponseEntity.status(ErrorCode.NOT_FOUND.getHttpStatus()).body(response);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    ResponseEntity<ErrorResponseApi> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex, WebRequest request) {

        ErrorResponseApi response = new ErrorResponseApi();
        response.setErrorCode(ErrorCode.UNAUTHORIZED.getCode());
        response.setMessage("Invalid credentials");
        response.setPath(getRequestPath(request));
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getHttpStatus()).body(response);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponseApi> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex, WebRequest request) {

        log.error("Data integrity violation: ", ex);

        ErrorResponseApi response = new ErrorResponseApi();
        response.setErrorCode(ErrorCode.ALREADY_EXISTS.getCode());
        response.setMessage("Data conflict - resource may already exist");
        response.setPath(getRequestPath(request));
        return ResponseEntity.status(ErrorCode.ALREADY_EXISTS.getHttpStatus()).body(response);
    }

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    ResponseEntity<ErrorResponseApi> handleNoHandlerFound(
            org.springframework.web.servlet.NoHandlerFoundException ex, WebRequest request) {

        ErrorResponseApi response = new ErrorResponseApi();
        response.setErrorCode(ErrorCode.NOT_FOUND.getCode());
        response.setMessage("Endpoint not found: " + ex.getRequestURL());
        response.setPath(getRequestPath(request));
        return ResponseEntity.status(ErrorCode.NOT_FOUND.getHttpStatus()).body(response);
    }

    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}