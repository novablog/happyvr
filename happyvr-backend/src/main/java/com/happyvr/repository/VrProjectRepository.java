package com.happyvr.repository;

import com.happyvr.entity.VrProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * VR项目数据访问接口
 */
@Repository
public interface VrProjectRepository extends JpaRepository<VrProject, Long> {
    
    /**
     * 根据用户ID查找项目
     */
    Page<VrProject> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和状态查找项目
     */
    Page<VrProject> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);
    
    /**
     * 根据状态查找项目
     */
    Page<VrProject> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 查找推荐项目
     */
    Page<VrProject> findByIsFeaturedAndStatus(Integer isFeatured, Integer status, Pageable pageable);
    
    /**
     * 根据分享令牌查找项目
     */
    Optional<VrProject> findByShareToken(String shareToken);
    
    /**
     * 搜索项目
     */
    @Query("SELECT p FROM VrProject p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "p.title LIKE %:keyword% OR " +
           "p.description LIKE %:keyword%) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<VrProject> searchProjects(@Param("keyword") String keyword, 
                                  @Param("status") Integer status, 
                                  Pageable pageable);
    
    /**
     * 根据用户搜索项目
     */
    @Query("SELECT p FROM VrProject p WHERE p.user.id = :userId AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "p.title LIKE %:keyword% OR " +
           "p.description LIKE %:keyword%)")
    Page<VrProject> searchUserProjects(@Param("userId") Long userId, 
                                      @Param("keyword") String keyword, 
                                      Pageable pageable);
    
    /**
     * 增加浏览次数
     */
    @Modifying
    @Query("UPDATE VrProject p SET p.viewCount = p.viewCount + 1 WHERE p.id = :projectId")
    void incrementViewCount(@Param("projectId") Long projectId);
    
    /**
     * 统计用户项目数量
     */
    long countByUserId(Long userId);
    
    /**
     * 统计各状态项目数量
     */
    @Query("SELECT p.status, COUNT(p) FROM VrProject p GROUP BY p.status")
    List<Object[]> countByStatus();
    
    /**
     * 统计推荐项目数量
     */
    long countByIsFeatured(Integer isFeatured);
    
    /**
     * 获取热门项目（按浏览量排序）
     */
    @Query("SELECT p FROM VrProject p WHERE p.status = 1 ORDER BY p.viewCount DESC")
    Page<VrProject> findPopularProjects(Pageable pageable);
    
    /**
     * 获取最新项目
     */
    @Query("SELECT p FROM VrProject p WHERE p.status = 1 ORDER BY p.createdAt DESC")
    Page<VrProject> findLatestProjects(Pageable pageable);
    
    /**
     * 检查分享令牌是否存在
     */
    boolean existsByShareToken(String shareToken);
}