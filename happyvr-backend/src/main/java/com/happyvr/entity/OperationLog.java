package com.happyvr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志实体类
 */
@Entity
@Table(name = "operation_logs")
public class OperationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @NotBlank(message = "操作类型不能为空")
    @Size(max = 100, message = "操作类型长度不能超过100个字符")
    @Column(name = "operation", nullable = false, length = 100)
    private String operation;
    
    @Size(max = 50, message = "资源类型长度不能超过50个字符")
    @Column(name = "resource_type", length = 50)
    private String resourceType;
    
    @Column(name = "resource_id")
    private Long resourceId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "json")
    private Map<String, Object> details;
    
    @Size(max = 45, message = "IP地址长度不能超过45个字符")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // 构造函数
    public OperationLog() {
        this.createdAt = LocalDateTime.now();
    }
    
    public OperationLog(String operation, Long userId) {
        this();
        this.operation = operation;
        this.userId = userId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public Long getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
    
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // 工具方法
    public static OperationLog create(String operation, Long userId, String resourceType, Long resourceId) {
        OperationLog log = new OperationLog(operation, userId);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        return log;
    }
    
    public OperationLog withDetails(Map<String, Object> details) {
        this.details = details;
        return this;
    }
    
    public OperationLog withIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }
    
    public OperationLog withUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }
}