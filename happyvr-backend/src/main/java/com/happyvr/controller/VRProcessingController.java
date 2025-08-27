package com.happyvr.controller;

import com.happyvr.dto.common.ApiResponse;
import com.happyvr.dto.vr.VRProcessingRequest;
import com.happyvr.dto.vr.VRProcessingResponse;
import com.happyvr.security.UserPrincipal;
import com.happyvr.service.VRProcessingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VR处理控制器
 */
@RestController
@RequestMapping("/v1/vr")
public class VRProcessingController {
    
    @Autowired
    private VRProcessingService vrProcessingService;
    
    /**
     * 开始VR处理
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<VRProcessingResponse>> startVRProcessing(
            @Valid @RequestBody VRProcessingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        VRProcessingResponse response = vrProcessingService.startVRProcessing(request);
        return ResponseEntity.ok(ApiResponse.success("VR处理任务已启动", response));
    }
    
    /**
     * 获取处理进度
     */
    @GetMapping("/progress/{taskId}")
    public ResponseEntity<ApiResponse<VRProcessingResponse>> getProcessingProgress(
            @PathVariable String taskId) {
        
        VRProcessingResponse response = vrProcessingService.getProcessingProgress(taskId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 获取所有处理任务（管理员功能）
     */
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<VRProcessingResponse>>> getAllProcessingTasks(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        // 这里可以添加权限检查，只允许管理员查看所有任务
        List<VRProcessingResponse> tasks = vrProcessingService.getAllProcessingTasks();
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
    
    /**
     * 取消处理任务
     */
    @PostMapping("/cancel/{taskId}")
    public ResponseEntity<ApiResponse<Boolean>> cancelProcessingTask(
            @PathVariable String taskId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        boolean cancelled = vrProcessingService.cancelProcessingTask(taskId);
        
        if (cancelled) {
            return ResponseEntity.ok(ApiResponse.success("任务取消成功", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("任务取消失败，任务可能不存在或已完成", false));
        }
    }
    
    /**
     * 清理过期任务
     */
    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<Void>> cleanupExpiredTasks(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        // 这里可以添加权限检查，只允许管理员执行清理
        vrProcessingService.cleanupExpiredTasks();
        return ResponseEntity.ok(ApiResponse.success("过期任务清理完成"));
    }
}