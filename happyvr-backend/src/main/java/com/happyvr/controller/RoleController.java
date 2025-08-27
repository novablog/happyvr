package com.happyvr.controller;

import com.happyvr.dto.common.ApiResponse;
import com.happyvr.dto.role.RoleRequest;
import com.happyvr.dto.role.RoleResponse;
import com.happyvr.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    
    private final RoleService roleService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('role:view')")
    public ResponseEntity<ApiResponse<Page<RoleResponse>>> getAllRoles(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<RoleResponse> roles = roleService.getAllRoles(pageable);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:view')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse role = roleService.createRole(request);
        return ResponseEntity.ok(ApiResponse.success("角色创建成功", role));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:edit')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id, 
            @Valid @RequestBody RoleRequest request) {
        RoleResponse role = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("角色更新成功", role));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("角色删除成功"));
    }
    
    @PostMapping("/{roleId}/users/{userId}")
    @PreAuthorize("hasAuthority('role:edit')")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(
            @PathVariable Long roleId, 
            @PathVariable Long userId) {
        roleService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("角色分配成功"));
    }
    
    @DeleteMapping("/{roleId}/users/{userId}")
    @PreAuthorize("hasAuthority('role:edit')")
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(
            @PathVariable Long roleId, 
            @PathVariable Long userId) {
        roleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("角色移除成功"));
    }
    
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('role:view')")
    public ResponseEntity<ApiResponse<List<String>>> getAllPermissions() {
        List<String> permissions = roleService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}