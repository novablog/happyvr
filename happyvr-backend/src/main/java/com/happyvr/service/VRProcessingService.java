package com.happyvr.service;

import com.happyvr.dto.vr.VRProcessingRequest;
import com.happyvr.dto.vr.VRProcessingResponse;
import com.happyvr.entity.VrProject;
import com.happyvr.exception.ResourceNotFoundException;
import com.happyvr.exception.ValidationException;
import com.happyvr.repository.VrProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VR处理服务
 */
@Service
public class VRProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(VRProcessingService.class);
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Autowired
    private VrProjectRepository vrProjectRepository;
    
    @Value("${file.upload.base-path:uploads}")
    private String uploadBasePath;
    
    @Value("${file.upload.base-url:/files}")
    private String baseUrl;
    
    // 存储处理任务的进度信息
    private final ConcurrentHashMap<String, VRProcessingResponse> processingTasks = new ConcurrentHashMap<>();
    
    /**
     * 开始VR处理任务
     */
    public VRProcessingResponse startVRProcessing(VRProcessingRequest request) {
        logger.info("开始VR处理任务: projectId={}, imageCount={}", 
            request.getProjectId(), request.getImageUrls().size());
        
        // 验证项目是否存在
        VrProject project = vrProjectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("VR项目不存在"));
        
        // 生成任务ID
        String taskId = UUID.randomUUID().toString();
        
        // 创建处理响应
        VRProcessingResponse response = new VRProcessingResponse(taskId, request.getProjectId(), "PENDING");
        response.setProgress(0);
        response.setMessage("任务已创建，等待处理");
        
        // 存储任务信息
        processingTasks.put(taskId, response);
        
        // 异步处理VR生成
        processVRAsync(taskId, request, project);
        
        return response;
    }
    
    /**
     * 获取处理进度
     */
    public VRProcessingResponse getProcessingProgress(String taskId) {
        VRProcessingResponse response = processingTasks.get(taskId);
        if (response == null) {
            response = new VRProcessingResponse();
            response.setTaskId(taskId);
            response.setStatus("NOT_FOUND");
            response.setMessage("任务不存在");
        }
        return response;
    }
    
    /**
     * 异步处理VR生成
     */
    @Async
    public CompletableFuture<Void> processVRAsync(String taskId, VRProcessingRequest request, VrProject project) {
        VRProcessingResponse response = processingTasks.get(taskId);
        
        try {
            logger.info("开始异步VR处理: taskId={}", taskId);
            
            // 更新状态为处理中
            response.setStatus("PROCESSING");
            response.setProgress(10);
            response.setMessage("正在加载图片");
            
            // 加载图片
            List<BufferedImage> images = loadImages(request.getImageUrls());
            response.setProgress(30);
            response.setMessage("图片加载完成，开始处理");
            
            // 获取处理选项
            VRProcessingRequest.VRProcessingOptions options = request.getOptions();
            if (options == null) {
                options = new VRProcessingRequest.VRProcessingOptions();
            }
            
            // 创建全景图片
            response.setProgress(50);
            response.setMessage("正在生成全景图片");
            
            BufferedImage panorama = imageProcessingService.createPanorama(
                images, options.getOutputWidth(), options.getOutputHeight());
            
            // 应用球面映射
            response.setProgress(70);
            response.setMessage("正在应用球面映射");
            
            BufferedImage vrImage = imageProcessingService.createSphericalMapping(panorama);
            
            // 优化图片
            if (options.getEnableOptimization()) {
                response.setProgress(80);
                response.setMessage("正在优化图片质量");
                vrImage = imageProcessingService.optimizeImage(vrImage, options.getQuality());
            }
            
            // 保存结果
            response.setProgress(90);
            response.setMessage("正在保存结果");
            
            String outputPath = generateOutputPath(project.getId(), taskId);
            String outputUrl = generateOutputUrl(project.getId(), taskId);
            
            imageProcessingService.saveImage(vrImage, outputPath, "jpg");
            
            // 更新项目信息
            updateProjectWithResult(project, outputUrl);
            
            // 完成处理
            response.setStatus("COMPLETED");
            response.setProgress(100);
            response.setMessage("VR生成完成");
            response.setResultUrl(outputUrl);
            response.setEndTime(LocalDateTime.now());
            
            // 计算处理时长
            if (response.getStartTime() != null) {
                long duration = java.time.Duration.between(response.getStartTime(), response.getEndTime()).toMillis();
                response.setProcessingDuration(duration);
            }
            
            logger.info("VR处理完成: taskId={}, duration={}ms", taskId, response.getProcessingDuration());
            
        } catch (Exception e) {
            logger.error("VR处理失败: taskId=" + taskId, e);
            
            response.setStatus("FAILED");
            response.setErrorMessage(e.getMessage());
            response.setMessage("处理失败: " + e.getMessage());
            response.setEndTime(LocalDateTime.now());
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * 加载图片列表
     */
    private List<BufferedImage> loadImages(List<String> imageUrls) throws Exception {
        List<BufferedImage> images = new ArrayList<>();
        
        for (String imageUrl : imageUrls) {
            try {
                // 将URL转换为本地文件路径
                String filePath = convertUrlToFilePath(imageUrl);
                File imageFile = new File(filePath);
                
                // 检查文件是否存在
                if (!imageFile.exists()) {
                    logger.error("图片文件不存在: {}", filePath);
                    throw new ValidationException("图片文件不存在: " + imageUrl);
                }
                
                BufferedImage image = ImageIO.read(imageFile);
                
                if (image == null) {
                    logger.error("无法读取图片文件: {}", filePath);
                    throw new ValidationException("无法读取图片: " + imageUrl);
                }
                
                images.add(image);
                logger.debug("成功加载图片: {}", imageUrl);
                
            } catch (Exception e) {
                logger.error("加载图片失败: " + imageUrl, e);
                throw new ValidationException("加载图片失败: " + imageUrl + " - " + e.getMessage());
            }
        }
        
        return images;
    }
    
    /**
     * 将URL转换为本地文件路径
     */
    private String convertUrlToFilePath(String imageUrl) {
        // 移除baseUrl前缀，获取相对路径
        String relativePath = imageUrl.replace(baseUrl + "/", "");
        return uploadBasePath + File.separator + relativePath.replace("/", File.separator);
    }
    
    /**
     * 生成输出文件路径
     */
    private String generateOutputPath(Long projectId, String taskId) {
        String fileName = "vr_" + projectId + "_" + taskId + ".jpg";
        String dirPath = uploadBasePath + File.separator + "vr" + File.separator + "project_" + projectId;
        
        // 确保目录存在
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        return dirPath + File.separator + fileName;
    }
    
    /**
     * 生成输出URL
     */
    private String generateOutputUrl(Long projectId, String taskId) {
        String fileName = "vr_" + projectId + "_" + taskId + ".jpg";
        return baseUrl + "/vr/project_" + projectId + "/" + fileName;
    }
    
    /**
     * 更新项目结果
     */
    private void updateProjectWithResult(VrProject project, String resultUrl) {
        try {
            // 更新项目的场景数据
            Map<String, Object> sceneData = new HashMap<>();
            sceneData.put("type", "panorama");
            sceneData.put("url", resultUrl);
            sceneData.put("generatedAt", LocalDateTime.now().toString());
            
            project.setSceneData(sceneData);
            project.setStatus(1); // 设置为已发布状态
            
            vrProjectRepository.save(project);
            
            logger.info("项目更新成功: projectId={}, resultUrl={}", project.getId(), resultUrl);
            
        } catch (Exception e) {
            logger.error("更新项目失败: projectId=" + project.getId(), e);
        }
    }
    
    /**
     * 清理过期的处理任务
     */
    public void cleanupExpiredTasks() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(24); // 24小时后过期
        
        processingTasks.entrySet().removeIf(entry -> {
            VRProcessingResponse response = entry.getValue();
            return response.getEndTime() != null && response.getEndTime().isBefore(expireTime);
        });
        
        logger.debug("清理过期任务完成，当前任务数: {}", processingTasks.size());
    }
    
    /**
     * 获取所有处理任务
     */
    public List<VRProcessingResponse> getAllProcessingTasks() {
        return new ArrayList<>(processingTasks.values());
    }
    
    /**
     * 取消处理任务
     */
    public boolean cancelProcessingTask(String taskId) {
        VRProcessingResponse response = processingTasks.get(taskId);
        if (response != null && "PROCESSING".equals(response.getStatus())) {
            response.setStatus("CANCELLED");
            response.setMessage("任务已取消");
            response.setEndTime(LocalDateTime.now());
            return true;
        }
        return false;
    }
}