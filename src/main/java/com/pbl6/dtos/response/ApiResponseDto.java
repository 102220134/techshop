package com.pbl6.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {
    private int code=1000;
    private String message;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private T data;
}
