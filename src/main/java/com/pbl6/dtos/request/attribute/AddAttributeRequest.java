package com.pbl6.dtos.request.attribute;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAttributeRequest {
    @NotEmpty
    private String code;
    @NotEmpty
    private String value;
    @NotEmpty
    private String label;
}
