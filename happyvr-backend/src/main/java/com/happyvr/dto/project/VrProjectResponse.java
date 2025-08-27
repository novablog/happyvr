package com.happyvr.dto.project;

import com.happyvr.entity.VrProject;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * VR项目响应DTO
 */
public class VrProjectResponse {
    
    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private Map<String, Object> sceneData;
    private Integer status;
    private String statusText;
    private Integer isFeatured;
    private Integer viewCount;
    private String shareToken;
    private String shareUrl;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 构造函数
    public VrProjectResponse() {}
    
    public VrProjectResponse(VrProject project) {
        this.id = project.getId();
        this.title = project.getTitle();
        this.description = project.getDescription();
        this.coverImage = project.getCoverImage();
        this.sceneData = project.getSceneData();
        this.status = project.getStatus();
        this.statusText = getStatusText(project.getStatus());
        this.isFeatured = project.getIsFeatured();
        this.viewCount = project.getViewCount();
        this.shareToken = project.getShareToken();
        this.shareUrl = project.getShareToken() != null ? "/share/" + project.getShareToken() : null;
        this.userId = project.getUser() != null ? project.getUser().getId() : null;
        this.username = project.getUser() != null ? project.getUser().getUsername() : null;
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
    }
    
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "已发布";
            case 2 -> "审核中";
            case 3 -> "已拒绝";
            default -> "未知";
        };
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCoverImage() {
        return coverImage;
    }
    
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    
    public Map<String, Object> getSceneData() {
        return sceneData;
    }
    
    public void setSceneData(Map<String, Object> sceneData) {
        this.sceneData = sceneData;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getStatusText() {
        return statusText;
    }
    
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
    
    public Integer getIsFeatured() {
        return isFeatured;
    }
    
    public void setIsFeatured(Integer isFeatured) {
        this.isFeatured = isFeatured;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public String getShareToken() {
        return shareToken;
    }
    
    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }
    
    public String getShareUrl() {
        return shareUrl;
    }
    
    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}