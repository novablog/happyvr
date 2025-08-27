package com.happyvr.dto.upload;

/**
 * 上传进度响应DTO
 */
public class UploadProgressResponse {
    private String uploadId;
    private String status; // UPLOADING, PROCESSING, COMPLETED, FAILED
    private Integer progress; // 0-100
    private String message;
    private Long totalSize;
    private Long uploadedSize;
    
    // 构造函数
    public UploadProgressResponse() {}
    
    public UploadProgressResponse(String uploadId, String status, Integer progress, String message) {
        this.uploadId = uploadId;
        this.status = status;
        this.progress = progress;
        this.message = message;
    }
    
    // Getters and Setters
    public String getUploadId() {
        return uploadId;
    }
    
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getTotalSize() {
        return totalSize;
    }
    
    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }
    
    public Long getUploadedSize() {
        return uploadedSize;
    }
    
    public void setUploadedSize(Long uploadedSize) {
        this.uploadedSize = uploadedSize;
    }
}