package com.happyvr.repository;

import com.happyvr.entity.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目图片数据访问接口
 */
@Repository
public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {
    
    /**
     * 根据项目ID查找图片
     */
    List<ProjectImage> findByProjectIdOrderBySortOrder(Long projectId);
    
    /**
     * 根据项目ID查找图片（分页）
     */
    List<ProjectImage> findByProjectId(Long projectId);
    
    /**
     * 统计项目图片数量
     */
    long countByProjectId(Long projectId);
    
    /**
     * 根据项目ID删除所有图片
     */
    void deleteByProjectId(Long projectId);
    
    /**
     * 查找已处理的图片
     */
    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project.id = :projectId AND pi.processedUrl IS NOT NULL")
    List<ProjectImage> findProcessedImagesByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 查找未处理的图片
     */
    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project.id = :projectId AND pi.processedUrl IS NULL")
    List<ProjectImage> findUnprocessedImagesByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 获取项目的第一张图片作为封面
     */
    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project.id = :projectId ORDER BY pi.sortOrder ASC LIMIT 1")
    ProjectImage findFirstImageByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 统计所有图片的总大小
     */
    @Query("SELECT COALESCE(SUM(pi.fileSize), 0) FROM ProjectImage pi WHERE pi.project.id = :projectId")
    Long getTotalFileSizeByProjectId(@Param("projectId") Long projectId);
}