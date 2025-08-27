package com.happyvr.repository;

import com.happyvr.entity.Hotspot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 热点数据访问接口
 */
@Repository
public interface HotspotRepository extends JpaRepository<Hotspot, Long> {
    
    /**
     * 根据项目ID查找所有热点
     */
    List<Hotspot> findByProject_IdOrderByCreatedAtAsc(Long projectId);
    
    /**
     * 根据项目ID和热点类型查找热点
     */
    List<Hotspot> findByProject_IdAndType(Long projectId, Hotspot.HotspotType type);
    
    /**
     * 统计项目的热点数量
     */
    long countByProject_Id(Long projectId);
    
    /**
     * 根据项目ID删除所有热点
     */
    void deleteByProject_Id(Long projectId);
    
    /**
     * 检查热点是否存在于指定项目中
     */
    boolean existsByIdAndProject_Id(Long id, Long projectId);
    
    /**
     * 查找项目中指定名称的热点
     */
    @Query("SELECT h FROM Hotspot h WHERE h.project.id = :projectId AND h.name = :name")
    List<Hotspot> findByProjectIdAndName(@Param("projectId") Long projectId, @Param("name") String name);
    
    /**
     * 根据热点类型统计数量
     */
    @Query("SELECT h.type, COUNT(h) FROM Hotspot h WHERE h.project.id = :projectId GROUP BY h.type")
    List<Object[]> countByProjectIdGroupByType(@Param("projectId") Long projectId);
}