package com.pbl6.dtos.request.user;

import lombok.Getter;

import java.util.List;
@Getter
public class UserUpdateRoleRequest {
    private List<String> roles;
}
