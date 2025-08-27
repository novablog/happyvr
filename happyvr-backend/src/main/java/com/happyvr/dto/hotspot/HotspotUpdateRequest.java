package com.happyvr.dto.hotspot;

import com.happyvr.entity.Hotspot;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 更新热点请求DTO
 */
public class HotspotUpdateRequest {
    
    @Size(max = 100, message = "热点名称长度不能超过100个字符")
    private String name;
    
    @DecimalMin(value = "-1000.0", message = "X坐标值超出范围")
    @DecimalMax(value = "1000.0", message = "X坐标值超出范围")
    private BigDecimal positionX;
    
    @DecimalMin(value = "-1000.0", message = "Y坐标值超出范围")
    @DecimalMax(value = "1000.0", message = "Y坐标值超出范围")
    private BigDecimal positionY;
    
    @DecimalMin(value = "-1000.0", message = "Z坐标值超出范围")
    @DecimalMax(value = "1000.0", message = "Z坐标值超出范围")
    private BigDecimal positionZ;
    
    private Hotspot.HotspotType type;
    
    @Size(max = 5000, message = "热点内容长度不能超过5000个字符")
    private String content;
    
    @Size(max = 2000, message = "热点样式长度不能超过2000个字符")
    private String style;
    
    // 构造函数
    public HotspotUpdateRequest() {}
    
    // Getters and Setters
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
}