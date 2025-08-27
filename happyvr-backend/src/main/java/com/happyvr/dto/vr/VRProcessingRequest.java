package com.happyvr.dto.vr;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * VR处理请求DTO
 */
public class VRProcessingRequest {
    
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    
    @NotEmpty(message = "图片列表不能为空")
    private List<String> imageUrls;
    
    private String processingType = "PANORAMA"; // PANORAMA, CUBE_MAP, SPHERE_MAP
    
    private VRProcessingOptions options;
    
    // 构造函数
    public VRProcessingRequest() {}
    
    public VRProcessingRequest(Long projectId, List<String> imageUrls) {
        this.projectId = projectId;
        this.imageUrls = imageUrls;
    }
    
    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    
    public List<String> getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public String getProcessingType() {
        return processingType;
    }
    
    public void setProcessingType(String processingType) {
        this.processingType = processingType;
    }
    
    public VRProcessingOptions getOptions() {
        return options;
    }
    
    public void setOptions(VRProcessingOptions options) {
        this.options = options;
    }
    
    /**
     * VR处理选项
     */
    public static class VRProcessingOptions {
        private Integer outputWidth = 4096;
        private Integer outputHeight = 2048;
        private Integer quality = 90;
        private Boolean enableOptimization = true;
        
        // Getters and Setters
        public Integer getOutputWidth() {
            return outputWidth;
        }
        
        public void setOutputWidth(Integer outputWidth) {
            this.outputWidth = outputWidth;
        }
        
        public Integer getOutputHeight() {
            return outputHeight;
        }
        
        public void setOutputHeight(Integer outputHeight) {
            this.outputHeight = outputHeight;
        }
        
        public Integer getQuality() {
            return quality;
        }
        
        public void setQuality(Integer quality) {
            this.quality = quality;
        }
        
        public Boolean getEnableOptimization() {
            return enableOptimization;
        }
        
        public void setEnableOptimization(Boolean enableOptimization) {
            this.enableOptimization = enableOptimization;
        }
    }
}