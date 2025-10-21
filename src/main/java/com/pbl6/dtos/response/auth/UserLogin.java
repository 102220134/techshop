package com.pbl6.dtos.response.auth;

import com.pbl6.dtos.user.UserDto;
import lombok.Getter;
import lombok.Setter;

import java.security.Permission;
import java.util.List;

@Getter
@Setter
public class UserLogin {
    private Long id;
    private String name;
    private List<String> roles;
    private List<String> permissions;
}
