package com.pbl6.dtos.request.role;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EditRoleReq {
    private  String name;
    private String displayName;
    private List<String> permissions;
}
