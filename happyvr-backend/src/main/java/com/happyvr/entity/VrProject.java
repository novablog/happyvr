package com.happyvr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * VR项目实体类
 */
@Entity
@Table(name = "vr_projects")
public class VrProject extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank(message = "项目标题不能为空")
    @Size(max = 200, message = "项目标题长度不能超过200个字符")
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "cover_image")
    private String coverImage;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scene_data", columnDefinition = "json")
    private Map<String, Object> sceneData;
    
    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0:草稿 1:已发布 2:审核中 3:已拒绝
    
    @Column(name = "is_featured", nullable = false)
    private Integer isFeatured = 0; // 1:是 0:否
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Column(name = "share_token", unique = true, length = 32)
    private String shareToken;
    
    // 项目图片
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProjectImage> images = new HashSet<>();
    
    // 项目热点
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Hotspot> hotspots = new HashSet<>();
    
    // 构造函数
    public VrProject() {}
    
    public VrProject(String title, User user) {
        this.title = title;
        this.user = user;
    }
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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
    
    public Set<ProjectImage> getImages() {
        return images;
    }
    
    public void setImages(Set<ProjectImage> images) {
        this.images = images;
    }
    
    public Set<Hotspot> getHotspots() {
        return hotspots;
    }
    
    public void setHotspots(Set<Hotspot> hotspots) {
        this.hotspots = hotspots;
    }
    
    // 工具方法
    public boolean isDraft() {
        return status != null && status == 0;
    }
    
    public boolean isPublished() {
        return status != null && status == 1;
    }
    
    public boolean isPendingReview() {
        return status != null && status == 2;
    }
    
    public boolean isRejected() {
        return status != null && status == 3;
    }
    
    public boolean isFeatured() {
        return isFeatured != null && isFeatured == 1;
    }
    
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }
    
    // 项目状态枚举
    public enum Status {
        DRAFT(0, "草稿"),
        PUBLISHED(1, "已发布"),
        PENDING_REVIEW(2, "审核中"),
        REJECTED(3, "已拒绝");
        
        private final int code;
        private final String description;
        
        Status(int code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static Status fromCode(int code) {
            for (Status status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown status code: " + code);
        }
    }
}