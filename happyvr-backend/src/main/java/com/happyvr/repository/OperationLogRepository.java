package com.happyvr.repository;

import com.happyvr.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志数据访问接口
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    
    /**
     * 根据用户ID查找操作日志
     */
    Page<OperationLog> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据操作类型查找日志
     */
    Page<OperationLog> findByOperation(String operation, Pageable pageable);
    
    /**
     * 根据资源类型查找日志
     */
    Page<OperationLog> findByResourceType(String resourceType, Pageable pageable);
    
    /**
     * 根据资源类型和资源ID查找日志
     */
    Page<OperationLog> findByResourceTypeAndResourceId(String resourceType, Long resourceId, Pageable pageable);
    
    /**
     * 根据时间范围查找日志
     */
    Page<OperationLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 根据IP地址查找日志
     */
    Page<OperationLog> findByIpAddress(String ipAddress, Pageable pageable);
    
    /**
     * 搜索操作日志
     */
    @Query("SELECT ol FROM OperationLog ol WHERE " +
           "(:userId IS NULL OR ol.userId = :userId) AND " +
           "(:operation IS NULL OR ol.operation LIKE %:operation%) AND " +
           "(:resourceType IS NULL OR ol.resourceType = :resourceType) AND " +
           "(:startTime IS NULL OR ol.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR ol.createdAt <= :endTime)")
    Page<OperationLog> searchLogs(@Param("userId") Long userId,
                                 @Param("operation") String operation,
                                 @Param("resourceType") String resourceType,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 Pageable pageable);
    
    /**
     * 统计操作类型分布
     */
    @Query("SELECT ol.operation, COUNT(ol) FROM OperationLog ol GROUP BY ol.operation")
    List<Object[]> countByOperation();
    
    /**
     * 统计用户操作次数
     */
    @Query("SELECT ol.userId, COUNT(ol) FROM OperationLog ol WHERE ol.userId IS NOT NULL GROUP BY ol.userId")
    List<Object[]> countByUserId();
    
    /**
     * 统计今日操作数量
     */
    @Query("SELECT COUNT(ol) FROM OperationLog ol WHERE DATE(ol.createdAt) = CURRENT_DATE")
    long countTodayOperations();
    
    /**
     * 删除指定时间之前的日志
     */
    void deleteByCreatedAtBefore(LocalDateTime dateTime);
    
    /**
     * 获取最近的操作日志
     */
    @Query("SELECT ol FROM OperationLog ol ORDER BY ol.createdAt DESC")
    Page<OperationLog> findRecentLogs(Pageable pageable);
}