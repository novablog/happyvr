package com.happyvr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色实体类
 */
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {
    
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Size(max = 200, message = "角色描述长度不能超过200个字符")
    @Column(name = "description", length = 200)
    private String description;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", columnDefinition = "json")
    private List<String> permissions;
    
    // 角色关联的用户
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
    
    // 构造函数
    public Role() {}
    
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
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
    
    public Set<User> getUsers() {
        return users;
    }
    
    public void setUsers(Set<User> users) {
        this.users = users;
    }
    
    // 工具方法
    public boolean hasPermission(String permission) {
        if (permissions == null) {
            return false;
        }
        return permissions.contains("*") || permissions.contains(permission);
    }
    
    public void addPermission(String permission) {
        if (permissions == null) {
            permissions = new java.util.ArrayList<>();
        }
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }
    
    public void removePermission(String permission) {
        if (permissions != null) {
            permissions.remove(permission);
        }
    }
}