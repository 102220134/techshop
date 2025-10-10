package com.pbl6.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class ApiResponseDto<T> {
    private int code=1000;
    private String message;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private T data;

    public void setData(T data) {
        this.data = data;
    }

    public ApiResponseDto(T data) {
        this.data = data;
    }
}
