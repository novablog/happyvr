package com.happyvr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * 项目图片实体类
 */
@Entity
@Table(name = "project_images")
public class ProjectImage extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private VrProject project;
    
    @NotBlank(message = "原始图片URL不能为空")
    @Column(name = "original_url", nullable = false)
    private String originalUrl;
    
    @Column(name = "processed_url")
    private String processedUrl;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "width")
    private Integer width;
    
    @Column(name = "height")
    private Integer height;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    // 构造函数
    public ProjectImage() {}
    
    public ProjectImage(String originalUrl, VrProject project) {
        this.originalUrl = originalUrl;
        this.project = project;
    }
    
    // Getters and Setters
    public VrProject getProject() {
        return project;
    }
    
    public void setProject(VrProject project) {
        this.project = project;
    }
    
    public String getOriginalUrl() {
        return originalUrl;
    }
    
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    
    public String getProcessedUrl() {
        return processedUrl;
    }
    
    public void setProcessedUrl(String processedUrl) {
        this.processedUrl = processedUrl;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public Integer getWidth() {
        return width;
    }
    
    public void setWidth(Integer width) {
        this.width = width;
    }
    
    public Integer getHeight() {
        return height;
    }
    
    public void setHeight(Integer height) {
        this.height = height;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    // 工具方法
    public boolean isProcessed() {
        return processedUrl != null && !processedUrl.isEmpty();
    }
    
    public String getDisplayUrl() {
        return isProcessed() ? processedUrl : originalUrl;
    }
    
    public String getFileSizeFormatted() {
        if (fileSize == null) {
            return "未知";
        }
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
    
    public String getDimensions() {
        if (width != null && height != null) {
            return width + " × " + height;
        }
        return "未知";
    }
}