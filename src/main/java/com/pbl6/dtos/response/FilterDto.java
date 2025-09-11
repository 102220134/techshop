package com.pbl6.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
public class FilterDto {
    private String code;
    private String name;
    private List<String> values;
}
