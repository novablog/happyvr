package com.happyvr.service;

import com.happyvr.config.FileUploadConfig;
import com.happyvr.dto.upload.FileUploadResponse;
import com.happyvr.dto.upload.UploadProgressResponse;
import com.happyvr.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    // 存储上传进度信息
    private final Map<String, UploadProgressResponse> uploadProgressMap = new ConcurrentHashMap<>();
    
    /**
     * 批量上传文件
     */
    public List<FileUploadResponse> uploadFiles(MultipartFile[] files, Long userId) {
        String uploadId = UUID.randomUUID().toString();
        
        // 初始化上传进度
        UploadProgressResponse progress = new UploadProgressResponse(uploadId, "UPLOADING", 0, "开始上传");
        uploadProgressMap.put(uploadId, progress);
        
        try {
            // 验证文件
            validateFiles(files);
            
            List<FileUploadResponse> responses = new ArrayList<>();
            long totalFiles = files.length;
            
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                
                // 更新进度
                int currentProgress = (int) ((i * 100) / totalFiles);
                progress.setProgress(currentProgress);
                progress.setMessage("正在上传文件: " + file.getOriginalFilename());
                
                FileUploadResponse response = uploadSingleFile(file, userId, uploadId);
                responses.add(response);
            }
            
            // 上传完成
            progress.setStatus("COMPLETED");
            progress.setProgress(100);
            progress.setMessage("上传完成");
            
            logger.info("用户 {} 成功上传 {} 个文件", userId, files.length);
            return responses;
            
        } catch (Exception e) {
            progress.setStatus("FAILED");
            progress.setMessage("上传失败: " + e.getMessage());
            logger.error("文件上传失败", e);
            throw new ValidationException("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传单个文件
     */
    public FileUploadResponse uploadSingleFile(MultipartFile file, Long userId, String uploadId) {
        try {
            // 验证单个文件
            validateSingleFile(file);
            
            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());
            
            // 创建用户目录
            String userDir = createUserDirectory(userId);
            
            // 保存文件
            Path filePath = Paths.get(userDir, fileName);
            Files.copy(file.getInputStream(), filePath);
            
            // 生成访问URL
            String fileUrl = generateFileUrl(userId, fileName);
            
            logger.info("文件上传成功: {}", fileName);
            
            return new FileUploadResponse(
                fileName,
                file.getOriginalFilename(),
                fileUrl,
                file.getSize(),
                file.getContentType(),
                uploadId
            );
            
        } catch (IOException e) {
            logger.error("文件保存失败", e);
            throw new ValidationException("文件保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取上传进度
     */
    public UploadProgressResponse getUploadProgress(String uploadId) {
        return uploadProgressMap.getOrDefault(uploadId, 
            new UploadProgressResponse(uploadId, "NOT_FOUND", 0, "上传记录不存在"));
    }
    
    /**
     * 验证文件数组
     */
    private void validateFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new ValidationException("请选择要上传的文件");
        }
        
        // 计算总大小
        long totalSize = Arrays.stream(files)
            .mapToLong(MultipartFile::getSize)
            .sum();
            
        if (totalSize > fileUploadConfig.getMaxTotalSize()) {
            throw new ValidationException("文件总大小超过限制: " + 
                (fileUploadConfig.getMaxTotalSize() / 1024 / 1024) + "MB");
        }
        
        // 验证每个文件
        for (MultipartFile file : files) {
            validateSingleFile(file);
        }
    }
    
    /**
     * 验证单个文件
     */
    private void validateSingleFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationException("文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > fileUploadConfig.getMaxFileSize()) {
            throw new ValidationException("文件 " + file.getOriginalFilename() + 
                " 大小超过限制: " + (fileUploadConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(fileUploadConfig.getAllowedTypes()).contains(contentType)) {
            throw new ValidationException("文件 " + file.getOriginalFilename() + 
                " 格式不支持，仅支持 JPG、PNG 格式");
        }
        
        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new ValidationException("文件名不能为空");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!Arrays.asList(fileUploadConfig.getAllowedExtensions()).contains(extension)) {
            throw new ValidationException("文件 " + originalFilename + 
                " 扩展名不支持，仅支持 .jpg、.jpeg、.png");
        }
    }
    
    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
    
    /**
     * 创建用户目录
     */
    private String createUserDirectory(Long userId) {
        try {
            String userDir = fileUploadConfig.getBasePath() + File.separator + 
                "user_" + userId + File.separator + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
                
            Path userPath = Paths.get(userDir);
            if (!Files.exists(userPath)) {
                Files.createDirectories(userPath);
            }
            
            return userDir;
        } catch (IOException e) {
            logger.error("创建用户目录失败", e);
            throw new ValidationException("创建用户目录失败");
        }
    }
    
    /**
     * 生成文件访问URL
     */
    private String generateFileUrl(Long userId, String fileName) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return fileUploadConfig.getBaseUrl() + "/user_" + userId + "/" + datePath + "/" + fileName;
    }
    
    /**
     * 删除文件
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(fileUploadConfig.getBasePath(), filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 清理过期的上传进度记录
     */
    public void cleanupExpiredProgress() {
        // 这里可以实现定时清理逻辑
        // 例如清理超过1小时的记录
        uploadProgressMap.entrySet().removeIf(entry -> {
            UploadProgressResponse progress = entry.getValue();
            return "COMPLETED".equals(progress.getStatus()) || "FAILED".equals(progress.getStatus());
        });
    }
}