package com.happyvr.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * VR项目创建请求DTO
 */
public class VrProjectCreateRequest {
    
    @NotBlank(message = "项目标题不能为空")
    @Size(max = 200, message = "项目标题长度不能超过200个字符")
    private String title;
    
    @Size(max = 1000, message = "项目描述长度不能超过1000个字符")
    private String description;
    
    private String coverImage;
    
    // 构造函数
    public VrProjectCreateRequest() {}
    
    public VrProjectCreateRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    // Getters and Setters
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
}