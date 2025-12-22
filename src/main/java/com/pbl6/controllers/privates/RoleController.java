package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.attribute.AddAttributeRequest;
import com.pbl6.dtos.request.attribute.EditAttributeRequest;
import com.pbl6.dtos.request.role.CreateRoleReq;
import com.pbl6.dtos.request.role.EditRoleReq;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.AttributeDto;
import com.pbl6.services.AttributeService;
import com.pbl6.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/private/role")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class RoleController {
    private final AttributeService filterService;
    private final RoleService roleService;

    @GetMapping("")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> getRoles() {
        return new ApiResponseDto<>(roleService.getAllRoles());
    }

    @GetMapping("/permissions")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> getPermission() {
        return new ApiResponseDto<>(roleService.getAllPermission());
    }

    @PostMapping("")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> addRole(@Valid @RequestBody CreateRoleReq req) {
        roleService.addRole(req);
        return new ApiResponseDto<>();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Thêm value cho filter/option", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> addFilterValue(
            @PathVariable Long id,
            @RequestBody EditRoleReq req
            ) {
        roleService.editRole(id, req);
        return new ApiResponseDto<>();
    }
}

