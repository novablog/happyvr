package com.happyvr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 系统配置实体类
 */
@Entity
@Table(name = "system_configs")
public class SystemConfig extends BaseEntity {
    
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    @Column(name = "config_key", unique = true, nullable = false, length = 100)
    private String configKey;
    
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
    
    @Size(max = 200, message = "配置描述长度不能超过200个字符")
    @Column(name = "description", length = 200)
    private String description;
    
    // 构造函数
    public SystemConfig() {}
    
    public SystemConfig(String configKey, String configValue, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
    }
    
    // Getters and Setters
    public String getConfigKey() {
        return configKey;
    }
    
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }
    
    public String getConfigValue() {
        return configValue;
    }
    
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // 工具方法
    public Integer getIntValue() {
        try {
            return configValue != null ? Integer.parseInt(configValue) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Long getLongValue() {
        try {
            return configValue != null ? Long.parseLong(configValue) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Boolean getBooleanValue() {
        return configValue != null ? Boolean.parseBoolean(configValue) : null;
    }
    
    public Double getDoubleValue() {
        try {
            return configValue != null ? Double.parseDouble(configValue) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}