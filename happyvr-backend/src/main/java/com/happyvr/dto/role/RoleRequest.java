package com.happyvr.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 角色请求DTO
 */
public class RoleRequest {
    
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;
    
    @Size(max = 200, message = "角色描述长度不能超过200个字符")
    private String description;
    
    private List<String> permissions;
    
    // 构造函数
    public RoleRequest() {}
    
    public RoleRequest(String name, String description, List<String> permissions) {
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}