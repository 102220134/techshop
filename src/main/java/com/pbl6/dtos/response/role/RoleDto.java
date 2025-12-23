package com.pbl6.dtos.response.role;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Setter
@Builder
public class RoleDto {
    private long id;
    private String displayName;
    private String name;
    private List<String> permissions;
}
