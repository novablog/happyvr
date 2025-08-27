package com.happyvr.repository;

import com.happyvr.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置数据访问接口
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    /**
     * 根据配置键查找配置
     */
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKey(String configKey);
    
    /**
     * 根据配置键前缀查找配置
     */
    List<SystemConfig> findByConfigKeyStartingWith(String prefix);
    
    /**
     * 根据配置键列表查找配置
     */
    List<SystemConfig> findByConfigKeyIn(List<String> configKeys);
    
    /**
     * 获取所有配置的键值对
     */
    @Query("SELECT sc.configKey, sc.configValue FROM SystemConfig sc")
    List<Object[]> findAllKeyValuePairs();
    
    /**
     * 根据描述搜索配置
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "sc.configKey LIKE %:keyword% OR " +
           "sc.description LIKE %:keyword%")
    List<SystemConfig> searchConfigs(@Param("keyword") String keyword);
    
    /**
     * 获取配置值
     */
    @Query("SELECT sc.configValue FROM SystemConfig sc WHERE sc.configKey = :configKey")
    Optional<String> findValueByConfigKey(@Param("configKey") String configKey);
}