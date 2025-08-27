package com.happyvr.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VR热点实体类
 */
@Entity
@Table(name = "hotspots")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Hotspot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private VrProject project;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "position_x", nullable = false, precision = 10, scale = 6)
    private BigDecimal positionX;
    
    @Column(name = "position_y", nullable = false, precision = 10, scale = 6)
    private BigDecimal positionY;
    
    @Column(name = "position_z", nullable = false, precision = 10, scale = 6)
    private BigDecimal positionZ;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private HotspotType type;
    
    @Column(name = "content", columnDefinition = "JSON")
    private String content;
    
    @Column(name = "style", columnDefinition = "JSON")
    private String style;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 热点类型枚举
    public enum HotspotType {
        INFO("info", "信息弹框"),
        MEDIA("media", "媒体内容"),
        LINK("link", "链接跳转"),
        AUDIO("audio", "音频播放");
        
        private final String code;
        private final String description;
        
        HotspotType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 构造函数
    public Hotspot() {}
    
    public Hotspot(VrProject project, String name, BigDecimal positionX, BigDecimal positionY, 
                   BigDecimal positionZ, HotspotType type) {
        this.project = project;
        this.name = name;
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.type = type;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public VrProject getProject() {
        return project;
    }
    
    public void setProject(VrProject project) {
        this.project = project;
    }
    
    // 便利方法获取项目ID
    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getPositionX() {
        return positionX;
    }
    
    public void setPositionX(BigDecimal positionX) {
        this.positionX = positionX;
    }
    
    public BigDecimal getPositionY() {
        return positionY;
    }
    
    public void setPositionY(BigDecimal positionY) {
        this.positionY = positionY;
    }
    
    public BigDecimal getPositionZ() {
        return positionZ;
    }
    
    public void setPositionZ(BigDecimal positionZ) {
        this.positionZ = positionZ;
    }
    
    public HotspotType getType() {
        return type;
    }
    
    public void setType(HotspotType type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getStyle() {
        return style;
    }
    
    public void setStyle(String style) {
        this.style = style;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}