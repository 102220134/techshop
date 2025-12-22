package com.pbl6.dtos.request.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleReq {
    @NotBlank
    private  String name;
    @NotBlank
    private String displayName;
}
