package com.happyvr.repository;

import com.happyvr.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据用户名或邮箱查找用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据状态查找用户
     */
    Page<User> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据用户名、邮箱或手机号搜索用户
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "u.username LIKE %:keyword% OR " +
           "u.email LIKE %:keyword% OR " +
           "u.phone LIKE %:keyword%)")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据角色查找用户
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);
    
    /**
     * 统计用户总数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1")
    long countActiveUsers();
    
    /**
     * 统计今日新增用户数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE DATE(u.createdAt) = CURRENT_DATE")
    long countTodayNewUsers();
    
    /**
     * 查找包含指定角色的用户
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    java.util.List<User> findByRolesContaining(@Param("role") com.happyvr.entity.Role role);
    
    /**
     * 统计包含指定角色的用户数量
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRolesContaining(@Param("role") com.happyvr.entity.Role role);
}