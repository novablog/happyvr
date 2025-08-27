package com.happyvr.repository;

import com.happyvr.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问接口
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 根据角色名称查找角色
     */
    Optional<Role> findByName(String name);
    
    /**
     * 检查角色名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据角色名称列表查找角色
     */
    List<Role> findByNameIn(List<String> names);
    
    /**
     * 查找包含特定权限的角色
     */
    @Query(value = "SELECT * FROM roles r WHERE JSON_CONTAINS(r.permissions, :permission, '$')", nativeQuery = true)
    List<Role> findByPermission(@Param("permission") String permission);
    
    /**
     * 查找系统默认角色
     */
    @Query("SELECT r FROM Role r WHERE r.name IN ('USER', 'ADMIN', 'MODERATOR')")
    List<Role> findSystemRoles();
    
    /**
     * 检查角色是否被用户使用
     */
    @Query("SELECT COUNT(ur) > 0 FROM User u JOIN u.roles ur WHERE ur.id = :roleId")
    boolean isRoleInUse(@Param("roleId") Long roleId);
}