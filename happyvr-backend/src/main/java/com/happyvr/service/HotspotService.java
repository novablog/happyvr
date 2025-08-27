package com.happyvr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happyvr.dto.hotspot.HotspotCreateRequest;
import com.happyvr.dto.hotspot.HotspotResponse;
import com.happyvr.dto.hotspot.HotspotUpdateRequest;
import com.happyvr.entity.Hotspot;
import com.happyvr.entity.VrProject;
import com.happyvr.exception.ResourceNotFoundException;
import com.happyvr.exception.ValidationException;
import com.happyvr.repository.HotspotRepository;
import com.happyvr.repository.VrProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 热点管理服务
 */
@Service
@Transactional
public class HotspotService {
    
    private static final Logger logger = LoggerFactory.getLogger(HotspotService.class);
    
    @Autowired
    private HotspotRepository hotspotRepository;
    
    @Autowired
    private VrProjectRepository vrProjectRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 创建热点
     */
    public HotspotResponse createHotspot(Long projectId, HotspotCreateRequest request, Long userId) {
        logger.info("用户 {} 为项目 {} 创建热点: {}", userId, projectId, request.getName());
        
        // 验证项目存在且用户有权限
        VrProject project = validateProjectAccess(projectId, userId);
        
        // 验证热点名称唯一性
        validateHotspotNameUnique(projectId, request.getName());
        
        // 验证热点内容格式
        validateHotspotContent(request.getContent(), request.getType());
        
        // 创建热点实体
        Hotspot hotspot = new Hotspot();
        hotspot.setProject(project);
        hotspot.setName(request.getName());
        hotspot.setPositionX(request.getPositionX());
        hotspot.setPositionY(request.getPositionY());
        hotspot.setPositionZ(request.getPositionZ());
        hotspot.setType(request.getType());
        hotspot.setContent(request.getContent());
        hotspot.setStyle(request.getStyle());
        
        // 保存热点
        hotspot = hotspotRepository.save(hotspot);
        
        logger.info("热点创建成功: hotspotId={}, projectId={}", hotspot.getId(), projectId);
        
        return HotspotResponse.from(hotspot);
    }
    
    /**
     * 更新热点
     */
    public HotspotResponse updateHotspot(Long projectId, Long hotspotId, HotspotUpdateRequest request, Long userId) {
        logger.info("用户 {} 更新项目 {} 的热点 {}", userId, projectId, hotspotId);
        
        // 验证项目存在且用户有权限
        validateProjectAccess(projectId, userId);
        
        // 查找热点
        Hotspot hotspot = hotspotRepository.findById(hotspotId)
            .orElseThrow(() -> new ResourceNotFoundException("热点不存在"));
        
        // 验证热点属于该项目
        if (!hotspot.getProjectId().equals(projectId)) {
            throw new ValidationException("热点不属于该项目");
        }
        
        // 验证热点名称唯一性（如果名称有变化）
        if (request.getName() != null && !request.getName().equals(hotspot.getName())) {
            validateHotspotNameUnique(projectId, request.getName());
        }
        
        // 验证热点内容格式（如果内容有变化）
        if (request.getContent() != null) {
            Hotspot.HotspotType type = request.getType() != null ? request.getType() : hotspot.getType();
            validateHotspotContent(request.getContent(), type);
        }
        
        // 更新热点属性
        updateHotspotFields(hotspot, request);
        
        // 保存更新
        hotspot = hotspotRepository.save(hotspot);
        
        logger.info("热点更新成功: hotspotId={}, projectId={}", hotspotId, projectId);
        
        return HotspotResponse.from(hotspot);
    }
    
    /**
     * 删除热点
     */
    public void deleteHotspot(Long projectId, Long hotspotId, Long userId) {
        logger.info("用户 {} 删除项目 {} 的热点 {}", userId, projectId, hotspotId);
        
        // 验证项目存在且用户有权限
        validateProjectAccess(projectId, userId);
        
        // 验证热点存在且属于该项目
        if (!hotspotRepository.existsByIdAndProject_Id(hotspotId, projectId)) {
            throw new ResourceNotFoundException("热点不存在或不属于该项目");
        }
        
        // 删除热点
        hotspotRepository.deleteById(hotspotId);
        
        logger.info("热点删除成功: hotspotId={}, projectId={}", hotspotId, projectId);
    }
    
    /**
     * 获取项目的所有热点
     */
    @Transactional(readOnly = true)
    public List<HotspotResponse> getProjectHotspots(Long projectId, Long userId) {
        // 验证项目存在且用户有权限
        validateProjectAccess(projectId, userId);
        
        List<Hotspot> hotspots = hotspotRepository.findByProject_IdOrderByCreatedAtAsc(projectId);
        
        return hotspots.stream()
            .map(HotspotResponse::from)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取热点详情
     */
    @Transactional(readOnly = true)
    public HotspotResponse getHotspot(Long projectId, Long hotspotId, Long userId) {
        // 验证项目存在且用户有权限
        validateProjectAccess(projectId, userId);
        
        // 查找热点
        Hotspot hotspot = hotspotRepository.findById(hotspotId)
            .orElseThrow(() -> new ResourceNotFoundException("热点不存在"));
        
        // 验证热点属于该项目
        if (!hotspot.getProjectId().equals(projectId)) {
            throw new ValidationException("热点不属于该项目");
        }
        
        return HotspotResponse.from(hotspot);
    }
    
    /**
     * 获取项目热点统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getHotspotStatistics(Long projectId, Long userId) {
        // 验证项目存在且用户有权限
        validateProjectAccess(projectId, userId);
        
        // 统计总数
        long totalCount = hotspotRepository.countByProject_Id(projectId);
        
        // 按类型统计
        List<Object[]> typeStats = hotspotRepository.countByProjectIdGroupByType(projectId);
        Map<String, Long> typeCountMap = typeStats.stream()
            .collect(Collectors.toMap(
                row -> ((Hotspot.HotspotType) row[0]).getDescription(),
                row -> (Long) row[1]
            ));
        
        return Map.of(
            "totalCount", totalCount,
            "typeStatistics", typeCountMap
        );
    }
    
    /**
     * 批量删除项目的所有热点
     */
    public void deleteProjectHotspots(Long projectId) {
        logger.info("删除项目 {} 的所有热点", projectId);
        hotspotRepository.deleteByProject_Id(projectId);
    }
    
    // 私有辅助方法
    
    /**
     * 验证项目访问权限
     */
    private VrProject validateProjectAccess(Long projectId, Long userId) {
        VrProject project = vrProjectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("VR项目不存在"));
        
        if (!project.getUser().getId().equals(userId)) {
            throw new ValidationException("无权限访问该项目");
        }
        
        return project;
    }
    
    /**
     * 验证热点名称唯一性
     */
    private void validateHotspotNameUnique(Long projectId, String name) {
        List<Hotspot> existingHotspots = hotspotRepository.findByProjectIdAndName(projectId, name);
        if (!existingHotspots.isEmpty()) {
            throw new ValidationException("热点名称已存在: " + name);
        }
    }
    
    /**
     * 验证热点内容格式
     */
    private void validateHotspotContent(String content, Hotspot.HotspotType type) {
        if (content == null || content.trim().isEmpty()) {
            return; // 内容可以为空
        }
        
        try {
            // 尝试解析JSON格式
            Map<String, Object> contentMap = objectMapper.readValue(content, Map.class);
            
            // 根据热点类型验证必要字段
            switch (type) {
                case INFO:
                    validateInfoContent(contentMap);
                    break;
                case MEDIA:
                    validateMediaContent(contentMap);
                    break;
                case LINK:
                    validateLinkContent(contentMap);
                    break;
                case AUDIO:
                    validateAudioContent(contentMap);
                    break;
            }
        } catch (JsonProcessingException e) {
            throw new ValidationException("热点内容格式错误，必须是有效的JSON格式");
        }
    }
    
    /**
     * 验证信息类型热点内容
     */
    private void validateInfoContent(Map<String, Object> content) {
        if (!content.containsKey("title") && !content.containsKey("text")) {
            throw new ValidationException("信息类型热点必须包含title或text字段");
        }
    }
    
    /**
     * 验证媒体类型热点内容
     */
    private void validateMediaContent(Map<String, Object> content) {
        if (!content.containsKey("mediaUrl")) {
            throw new ValidationException("媒体类型热点必须包含mediaUrl字段");
        }
    }
    
    /**
     * 验证链接类型热点内容
     */
    private void validateLinkContent(Map<String, Object> content) {
        if (!content.containsKey("url")) {
            throw new ValidationException("链接类型热点必须包含url字段");
        }
    }
    
    /**
     * 验证音频类型热点内容
     */
    private void validateAudioContent(Map<String, Object> content) {
        if (!content.containsKey("audioUrl")) {
            throw new ValidationException("音频类型热点必须包含audioUrl字段");
        }
    }
    
    /**
     * 更新热点字段
     */
    private void updateHotspotFields(Hotspot hotspot, HotspotUpdateRequest request) {
        if (request.getName() != null) {
            hotspot.setName(request.getName());
        }
        if (request.getPositionX() != null) {
            hotspot.setPositionX(request.getPositionX());
        }
        if (request.getPositionY() != null) {
            hotspot.setPositionY(request.getPositionY());
        }
        if (request.getPositionZ() != null) {
            hotspot.setPositionZ(request.getPositionZ());
        }
        if (request.getType() != null) {
            hotspot.setType(request.getType());
        }
        if (request.getContent() != null) {
            hotspot.setContent(request.getContent());
        }
        if (request.getStyle() != null) {
            hotspot.setStyle(request.getStyle());
        }
    }
}