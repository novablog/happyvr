package com.happyvr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置
 */
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {
    
    private String basePath = "uploads";
    private String baseUrl = "/files";
    private Long maxFileSize = 10 * 1024 * 1024L; // 10MB
    private Long maxTotalSize = 100 * 1024 * 1024L; // 100MB
    private String[] allowedTypes = {"image/jpeg", "image/jpg", "image/png"};
    private String[] allowedExtensions = {".jpg", ".jpeg", ".png"};
    
    // Getters and Setters
    public String getBasePath() {
        return basePath;
    }
    
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public Long getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public Long getMaxTotalSize() {
        return maxTotalSize;
    }
    
    public void setMaxTotalSize(Long maxTotalSize) {
        this.maxTotalSize = maxTotalSize;
    }
    
    public String[] getAllowedTypes() {
        return allowedTypes;
    }
    
    public void setAllowedTypes(String[] allowedTypes) {
        this.allowedTypes = allowedTypes;
    }
    
    public String[] getAllowedExtensions() {
        return allowedExtensions;
    }
    
    public void setAllowedExtensions(String[] allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }
}