package com.happyvr.controller;

import com.happyvr.dto.common.ApiResponse;
import com.happyvr.dto.common.PageResponse;
import com.happyvr.dto.project.VrProjectCreateRequest;
import com.happyvr.dto.project.VrProjectListResponse;
import com.happyvr.dto.project.VrProjectResponse;
import com.happyvr.dto.project.VrProjectUpdateRequest;
import com.happyvr.security.UserPrincipal;
import com.happyvr.service.VrProjectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * VR项目控制器
 */
@RestController
@RequestMapping("/v1/projects")
public class VrProjectController {
    
    private static final Logger logger = LoggerFactory.getLogger(VrProjectController.class);
    
    @Autowired
    private VrProjectService vrProjectService;
    
    /**
     * 创建VR项目
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VrProjectResponse>> createProject(
            @Valid @RequestBody VrProjectCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("创建VR项目请求: userId={}, title={}", userPrincipal.getId(), request.getTitle());
        
        VrProjectResponse response = vrProjectService.createProject(request, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success("项目创建成功", response));
    }
    
    /**
     * 更新VR项目
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<VrProjectResponse>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody VrProjectUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("更新VR项目请求: userId={}, projectId={}", userPrincipal.getId(), projectId);
        
        VrProjectResponse response = vrProjectService.updateProject(projectId, request, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success("项目更新成功", response));
    }
    
    /**
     * 获取VR项目详情
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<VrProjectResponse>> getProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        VrProjectResponse response = vrProjectService.getProject(projectId, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 删除VR项目
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("删除VR项目请求: userId={}, projectId={}", userPrincipal.getId(), projectId);
        
        vrProjectService.deleteProject(projectId, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success("项目删除成功"));
    }
    
    /**
     * 获取当前用户的项目列表
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<VrProjectListResponse>>> getMyProjects(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Page<VrProjectListResponse> projects = vrProjectService.getUserProjects(
            userPrincipal.getId(), keyword, status, page, size, sortBy, sortDir);
        
        PageResponse<VrProjectListResponse> response = new PageResponse<>(projects);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 获取公开项目列表
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<PageResponse<VrProjectListResponse>>> getPublicProjects(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "latest") String type, // latest, popular, featured
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<VrProjectListResponse> projects = vrProjectService.getPublicProjects(
            keyword, type, page, size, sortBy, sortDir);
        
        PageResponse<VrProjectListResponse> response = new PageResponse<>(projects);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 发布项目
     */
    @PostMapping("/{projectId}/publish")
    public ResponseEntity<ApiResponse<VrProjectResponse>> publishProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("发布VR项目请求: userId={}, projectId={}", userPrincipal.getId(), projectId);
        
        VrProjectResponse response = vrProjectService.publishProject(projectId, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success("项目发布成功", response));
    }
    
    /**
     * 重新生成分享令牌
     */
    @PostMapping("/{projectId}/regenerate-token")
    public ResponseEntity<ApiResponse<VrProjectResponse>> regenerateShareToken(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("重新生成分享令牌请求: userId={}, projectId={}", userPrincipal.getId(), projectId);
        
        VrProjectResponse response = vrProjectService.regenerateShareToken(projectId, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success("分享链接已更新", response));
    }
    
    /**
     * 获取项目统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getProjectStatistics(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Object statistics = vrProjectService.getProjectStatistics(userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}