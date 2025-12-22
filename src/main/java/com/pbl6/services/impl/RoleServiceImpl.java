package com.pbl6.services.impl;

import com.pbl6.dtos.request.role.CreateRoleReq;
import com.pbl6.dtos.request.role.EditRoleReq;
import com.pbl6.dtos.response.role.RoleDto;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.PermissionRepository;
import com.pbl6.repositories.RoleRepository;
import com.pbl6.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .filter(r->!r.getName().equals("CUSTOMER") && !r.getName().equals("ADMIN"))
                .map(r->
                RoleDto.builder()
                        .name(r.getName())
                        .displayName(r.getDisplayName())
                        .id(r.getId())
                        .permissions(r.getPermissions().stream().map(p->p.getName()).toList())
                        .build()
        ).toList();
    }

    @Override
    public void addRole(CreateRoleReq req) {
        if(roleRepository.existsByName(req.getName())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,"role name exists");
        }
        roleRepository.save(
                com.pbl6.entities.RoleEntity.builder()
                        .name(req.getName())
                        .displayName(req.getDisplayName())
                        .build()
        );
    }

    @Override
    public void editRole(long id, EditRoleReq req) {
        var role = roleRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR,"role not found"));
        if(req.getName()!=null && !req.getName().isEmpty()) {
            role.setName(req.getName());
        }
        if(req.getDisplayName()!=null && !req.getDisplayName().isEmpty()) {
            role.setDisplayName(req.getDisplayName());
        }
        role.setPermissions(permissionRepository.findByNameIn(req.getPermissions()));
        roleRepository.save(role);
    }

    @Override
    public List<String> getAllPermission() {
        return permissionRepository.findAll().stream().map(p->p.getName()).toList();
    }
}
