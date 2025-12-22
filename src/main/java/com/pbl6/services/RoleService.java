package com.pbl6.services;

import com.pbl6.dtos.request.role.CreateRoleReq;
import com.pbl6.dtos.request.role.EditRoleReq;
import com.pbl6.dtos.response.role.RoleDto;

import java.util.List;

public interface RoleService {
    List<RoleDto> getAllRoles();
    void addRole(CreateRoleReq req);
    void editRole(long id , EditRoleReq req);

    List<String> getAllPermission();
}
