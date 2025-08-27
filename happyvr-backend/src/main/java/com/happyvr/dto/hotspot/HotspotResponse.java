package com.happyvr.dto.hotspot;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.happyvr.entity.Hotspot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 热点响应DTO
 */
public class HotspotResponse {
    
    private Long id;
    private Long projectId;
    private String name;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private BigDecimal positionZ;
    private Hotspot.HotspotType type;
    private String typeDescription;
    private String content;
    private String style;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    // 构造函数
    public HotspotResponse() {}
    
    public HotspotResponse(Hotspot hotspot) {
        this.id = hotspot.getId();
        this.projectId = hotspot.getProjectId();
        this.name = hotspot.getName();
        this.positionX = hotspot.getPositionX();
        this.positionY = hotspot.getPositionY();
        this.positionZ = hotspot.getPositionZ();
        this.type = hotspot.getType();
        this.typeDescription = hotspot.getType() != null ? hotspot.getType().getDescription() : null;
        this.content = hotspot.getContent();
        this.style = hotspot.getStyle();
        this.createdAt = hotspot.getCreatedAt();
    }
    
    // 静态工厂方法
    public static HotspotResponse from(Hotspot hotspot) {
        return new HotspotResponse(hotspot);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
    
    public Hotspot.HotspotType getType() {
        return type;
    }
    
    public void setType(Hotspot.HotspotType type) {
        this.type = type;
    }
    
    public String getTypeDescription() {
        return typeDescription;
    }
    
    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
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