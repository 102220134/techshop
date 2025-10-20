package com.pbl6.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class ErrorResponseApi {
    private int code;
    private String message;
    private String detail;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
}
