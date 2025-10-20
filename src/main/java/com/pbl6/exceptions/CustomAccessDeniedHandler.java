package com.pbl6.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbl6.dtos.response.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("handle bằng CustomAccessDeniedHandler");
        ErrorResponseApi res = new ErrorResponseApi();
        res.setCode(ErrorCode.FORBIDDEN.getCode());
        res.setMessage(ErrorCode.FORBIDDEN.getMessage());

        response.setStatus(ErrorCode.FORBIDDEN.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), res);
    }
}

