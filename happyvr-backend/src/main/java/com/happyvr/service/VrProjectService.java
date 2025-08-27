package com.happyvr.service;

import com.happyvr.dto.project.VrProjectCreateRequest;
import com.happyvr.dto.project.VrProjectListResponse;
import com.happyvr.dto.project.VrProjectResponse;
import com.happyvr.dto.project.VrProjectUpdateRequest;
import com.happyvr.entity.User;
import com.happyvr.entity.VrProject;
import com.happyvr.exception.ResourceNotFoundException;
import com.happyvr.exception.UnauthorizedException;
import com.happyvr.exception.ValidationException;
import com.happyvr.repository.UserRepository;
import com.happyvr.repository.VrProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * VR项目服务
 */
@Service
@Transactional
public class VrProjectService {
    
    private static final Logger logger = LoggerFactory.getLogger(VrProjectService.class);
    
    @Autowired
    private VrProjectRepository vrProjectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 创建VR项目
     */
    public VrProjectResponse createProject(VrProjectCreateRequest request, Long userId) {
        logger.info("用户 {} 创建VR项目: {}", userId, request.getTitle());
        
        // 获取用户信息
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 创建项目
        VrProject project = new VrProject();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setCoverImage(request.getCoverImage());
        project.setUser(user);
        project.setStatus(0); // 默认为草稿状态
        
        // 生成分享令牌
        project.setShareToken(generateShareToken());
        
        // 保存项目
        VrProject savedProject = vrProjectRepository.save(project);
        
        logger.info("VR项目创建成功: projectId={}, title={}", savedProject.getId(), savedProject.getTitle());
        
        return new VrProjectResponse(savedProject);
    }
    
    /**
     * 更新VR项目
     */
    public VrProjectResponse updateProject(Long projectId, VrProjectUpdateRequest request, Long userId) {
        logger.info("用户 {} 更新VR项目: projectId={}", userId, projectId);
        
        // 获取项目
        VrProject project = vrProjectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("VR项目不存在"));
        
        // 检查权限
        if (!project.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("无权限修改此项目");
        }
        
        // 更新项目信息
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setCoverImage(request.getCoverImage());
        
        // 更新状态（如果提供）
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            project.setStatus(request.getStatus());
        }
        
        // 保存更新
        VrProject updatedProject = vrProjectRepository.save(project);
        
        logger.info("VR项目更新成功: projectId={}, title={}", updatedProject.getId(), updatedProject.getTitle());
        
        return new VrProjectResponse(updatedProject);
    }
    
    /**
     * 获取VR项目详情
     */
    @Transactional(readOnly = true)
    public VrProjectResponse getProject(Long projectId, Long userId) {
        VrProject project = vrProjectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("VR项目不存在"));
        
        // 检查权限（只有项目所有者或已发布的项目可以查看）
        if (!project.getUser().getId().equals(userId) && !project.isPublished()) {
            throw new UnauthorizedException("无权限查看此项目");
        }
        
        // 增加浏览次数（如果不是项目所有者）
        if (!project.getUser().getId().equals(userId)) {
            vrProjectRepository.incrementViewCount(projectId);
        }
        
        return new VrProjectResponse(project);
    }
    
    /**
     * 通过分享令牌获取项目
     */
    @Transactional(readOnly = true)
    public VrProjectResponse getProjectByShareToken(String shareToken) {
        VrProject project = vrProjectRepository.findByShareToken(shareToken)
            .orElseThrow(() -> new ResourceNotFoundException("分享链接无效或项目不存在"));
        
        // 只有已发布的项目可以通过分享链接访问
        if (!project.isPublished()) {
            throw new ValidationException("项目未发布，无法访问");
        }
        
        // 增加浏览次数
        vrProjectRepository.incrementViewCount(project.getId());
        
        return new VrProjectResponse(project);
    }
    
    /**
     * 删除VR项目
     */
    public void deleteProject(Long projectId, Long userId) {
        logger.info("用户 {} 删除VR项目: projectId={}", userId, projectId);
        
        // 获取项目
        VrProject project = vrProjectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("VR项目不存在"));
        
        // 检查权限
        if (!project.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("无权限删除此项目");
        }
        
        // 删除项目
        vrProjectRepository.delete(project);
        
        logger.info("VR项目删除成功: projectId={}", projectId);
    }
    
    /**
     * 获取用户项目列表
     */
    @Transactional(readOnly = true)
    public Page<VrProjectListResponse> getUserProjects(Long userId, String keyword, Integer status, 
                                                      int page, int size, String sortBy, String sortDir) {
        // 创建分页和排序
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<VrProject> projects;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 搜索用户项目
            projects = vrProjectRepository.searchUserProjects(userId, keyword.trim(), pageable);
        } else if (status != null) {
            // 按状态筛选
            projects = vrProjectRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            // 获取所有用户项目
            projects = vrProjectRepository.findByUserId(userId, pageable);
        }
        
        return projects.map(VrProjectListResponse::new);
    }
    
    /**
     * 获取公开项目列表
     */
    @Transactional(readOnly = true)
    public Page<VrProjectListResponse> getPublicProjects(String keyword, String type, 
                                                         int page, int size, String sortBy, String sortDir) {
        // 创建分页和排序
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<VrProject> projects;
        
        if ("popular".equals(type)) {
            // 热门项目
            projects = vrProjectRepository.findPopularProjects(pageable);
        } else if ("latest".equals(type)) {
            // 最新项目
            projects = vrProjectRepository.findLatestProjects(pageable);
        } else if ("featured".equals(type)) {
            // 推荐项目
            projects = vrProjectRepository.findByIsFeaturedAndStatus(1, 1, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 搜索公开项目
            projects = vrProjectRepository.searchProjects(keyword.trim(), 1, pageable);
        } else {
            // 所有已发布项目
            projects = vrProjectRepository.findByStatus(1, pageable);
        }
        
        return projects.map(VrProjectListResponse::new);
    }
    
    /**
     * 发布项目
     */
    public VrProjectResponse publishProject(Long projectId, Long userId) {
        logger.info("用户 {} 发布VR项目: projectId={}", userId, projectId);
        
        // 获取项目
        VrProject project = vrProjectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("VR项目不存在"));
        
        // 检查权限
        if (!project.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("无权限发布此项目");
        }
        
        // 检查项目是否可以发布
        if (project.getSceneData() == null || project.getSceneData().isEmpty()) {
            throw new ValidationException("项目缺少VR场景数据，无法发布");
        }
        
        // 更新状态为已发布
        project.setStatus(1);
        
        VrProject updatedProject = vrProjectRepository.save(project);
        
        logger.info("VR项目发布成功: projectId={}", projectId);
        
        return new VrProjectResponse(updatedProject);
    }
    
    /**
     * 生成新的分享令牌
     */
    public VrProjectResponse regenerateShareToken(Long projectId, Long userId) {
        logger.info("用户 {} 重新生成分享令牌: projectId={}", userId, projectId);
        
        // 获取项目
        VrProject project = vrProjectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("VR项目不存在"));
        
        // 检查权限
        if (!project.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("无权限修改此项目");
        }
        
        // 生成新的分享令牌
        project.setShareToken(generateShareToken());
        
        VrProject updatedProject = vrProjectRepository.save(project);
        
        logger.info("分享令牌重新生成成功: projectId={}", projectId);
        
        return new VrProjectResponse(updatedProject);
    }
    
    /**
     * 生成分享令牌
     */
    private String generateShareToken() {
        String token;
        do {
            byte[] randomBytes = new byte[24];
            secureRandom.nextBytes(randomBytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        } while (vrProjectRepository.existsByShareToken(token));
        
        return token;
    }
    
    /**
     * 验证状态值
     */
    private void validateStatus(Integer status) {
        if (status == null || status < 0 || status > 3) {
            throw new ValidationException("无效的项目状态");
        }
    }
    
    /**
     * 获取项目统计信息
     */
    @Transactional(readOnly = true)
    public Object getProjectStatistics(Long userId) {
        long totalCount = vrProjectRepository.countByUserId(userId);
        long draftCount = vrProjectRepository.findByUserIdAndStatus(userId, 0, Pageable.unpaged()).getTotalElements();
        long publishedCount = vrProjectRepository.findByUserIdAndStatus(userId, 1, Pageable.unpaged()).getTotalElements();
        long pendingCount = vrProjectRepository.findByUserIdAndStatus(userId, 2, Pageable.unpaged()).getTotalElements();
        
        return new Object() {
            public final long total = totalCount;
            public final long draft = draftCount;
            public final long published = publishedCount;
            public final long pending = pendingCount;
        };
    }
}