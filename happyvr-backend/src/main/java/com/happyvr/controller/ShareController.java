package com.happyvr.controller;

import com.happyvr.dto.common.ApiResponse;
import com.happyvr.dto.project.VrProjectResponse;
import com.happyvr.service.VrProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 分享控制器
 */
@RestController
@RequestMapping("/v1/share")
public class ShareController {
    
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);
    
    @Autowired
    private VrProjectService vrProjectService;
    
    /**
     * 通过分享令牌获取项目
     */
    @GetMapping("/{shareToken}")
    public ResponseEntity<ApiResponse<VrProjectResponse>> getSharedProject(
            @PathVariable String shareToken) {
        
        logger.info("访问分享项目: shareToken={}", shareToken);
        
        VrProjectResponse response = vrProjectService.getProjectByShareToken(shareToken);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}