package com.happyvr.controller;

import com.happyvr.dto.common.ApiResponse;
import com.happyvr.dto.upload.FileUploadResponse;
import com.happyvr.dto.upload.UploadProgressResponse;
import com.happyvr.security.UserPrincipal;
import com.happyvr.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/upload")
public class FileUploadController {
    
    @Autowired
    private FileUploadService fileUploadService;
    
    /**
     * 批量上传图片
     */
    @PostMapping("/images")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<FileUploadResponse> responses = fileUploadService.uploadFiles(files, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("文件上传成功", responses));
    }
    
    /**
     * 上传单个图片
     */
    @PostMapping("/image")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        String uploadId = java.util.UUID.randomUUID().toString();
        FileUploadResponse response = fileUploadService.uploadSingleFile(file, userPrincipal.getId(), uploadId);
        return ResponseEntity.ok(ApiResponse.success("文件上传成功", response));
    }
    
    /**
     * 获取上传进度
     */
    @GetMapping("/progress/{uploadId}")
    public ResponseEntity<ApiResponse<UploadProgressResponse>> getUploadProgress(
            @PathVariable String uploadId) {
        
        UploadProgressResponse progress = fileUploadService.getUploadProgress(uploadId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public ResponseEntity<ApiResponse<Boolean>> deleteFile(
            @RequestParam String filePath,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        // 这里应该验证用户是否有权限删除该文件
        boolean deleted = fileUploadService.deleteFile(filePath);
        
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("文件删除成功", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("文件删除失败", false));
        }
    }
}