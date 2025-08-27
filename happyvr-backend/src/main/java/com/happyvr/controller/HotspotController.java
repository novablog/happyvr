package com.happyvr.controller;

import com.happyvr.dto.common.ApiResponse;
import com.happyvr.dto.hotspot.HotspotCreateRequest;
import com.happyvr.dto.hotspot.HotspotResponse;
import com.happyvr.dto.hotspot.HotspotUpdateRequest;
import com.happyvr.security.UserPrincipal;
import com.happyvr.service.HotspotService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * VR热点管理控制器
 */
@RestController
@RequestMapping("/v1/projects/{projectId}/hotspots")
public class HotspotController {
    
    private static final Logger logger = LoggerFactory.getLogger(HotspotController.class);
    
    @Autowired
    private HotspotService hotspotService;
    
    /**
     * 创建热点
     */
    @PostMapping
    public ResponseEntity<ApiResponse<HotspotResponse>> createHotspot(
            @PathVariable Long projectId,
            @Valid @RequestBody HotspotCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("创建热点请求: userId={}, projectId={}, name={}", 
            userPrincipal.getId(), projectId, request.getName());
        
        HotspotResponse response = hotspotService.createHotspot(projectId, request, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success("热点创建成功", response));
    }
    
    /**
     * 更新热点
     */
    @PutMapping("/{hotspotId}")
    public ResponseEntity<ApiResponse<HotspotResponse>> updateHotspot(
            @PathVariable Long projectId,
            @PathVariable Long hotspotId,
            @Valid @RequestBody HotspotUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("更新热点请求: userId={}, projectId={}, hotspotId={}", 
            userPrincipal.getId(), projectId, hotspotId);
        
        HotspotResponse response = hotspotService.updateHotspot(projectId, hotspotId, request, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success("热点更新成功", response));
    }
    
    /**
     * 删除热点
     */
    @DeleteMapping("/{hotspotId}")
    public ResponseEntity<ApiResponse<Void>> deleteHotspot(
            @PathVariable Long projectId,
            @PathVariable Long hotspotId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("删除热点请求: userId={}, projectId={}, hotspotId={}", 
            userPrincipal.getId(), projectId, hotspotId);
        
        hotspotService.deleteHotspot(projectId, hotspotId, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success("热点删除成功"));
    }
    
    /**
     * 获取项目的所有热点
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<HotspotResponse>>> getProjectHotspots(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<HotspotResponse> hotspots = hotspotService.getProjectHotspots(projectId, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success(hotspots));
    }
    
    /**
     * 获取热点详情
     */
    @GetMapping("/{hotspotId}")
    public ResponseEntity<ApiResponse<HotspotResponse>> getHotspot(
            @PathVariable Long projectId,
            @PathVariable Long hotspotId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        HotspotResponse response = hotspotService.getHotspot(projectId, hotspotId, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 获取项目热点统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHotspotStatistics(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Map<String, Object> statistics = hotspotService.getHotspotStatistics(projectId, userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}