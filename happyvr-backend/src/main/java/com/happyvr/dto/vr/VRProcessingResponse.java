package com.happyvr.dto.vr;

import java.time.LocalDateTime;

/**
 * VR处理响应DTO
 */
public class VRProcessingResponse {
    
    private String taskId;
    private Long projectId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private Integer progress; // 0-100
    private String message;
    private String resultUrl;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long processingDuration; // 处理时长（毫秒）
    
    // 构造函数
    public VRProcessingResponse() {}
    
    public VRProcessingResponse(String taskId, Long projectId, String status) {
        this.taskId = taskId;
        this.projectId = projectId;
        this.status = status;
        this.startTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
    
    public String getResultUrl() {
        return resultUrl;
    }
    
    public void setResultUrl(String resultUrl) {
        this.resultUrl = resultUrl;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Long getProcessingDuration() {
        return processingDuration;
    }
    
    public void setProcessingDuration(Long processingDuration) {
        this.processingDuration = processingDuration;
    }
}