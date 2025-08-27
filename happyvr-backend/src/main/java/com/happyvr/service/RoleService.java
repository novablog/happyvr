package com.happyvr.service;

import com.happyvr.dto.role.RoleRequest;
import com.happyvr.dto.role.RoleResponse;
import com.happyvr.entity.Role;
import com.happyvr.entity.User;
import com.happyvr.exception.ResourceNotFoundException;
import com.happyvr.exception.ValidationException;
import com.happyvr.repository.RoleRepository;
import com.happyvr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable)
                .map(this::convertToResponse);
    }
    
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));
        return convertToResponse(role);
    }
    
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        validateRoleRequest(request);
        
        if (roleRepository.existsByName(request.getName())) {
            throw new ValidationException("角色名称已存在");
        }
        
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(request.getPermissions());
        
        Role savedRole = roleRepository.save(role);
        log.info("创建角色成功: {}", savedRole.getName());
        
        return convertToResponse(savedRole);
    }
    
    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        validateRoleRequest(request);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));
        
        // 检查角色名称是否被其他角色使用
        if (!role.getName().equals(request.getName()) && 
            roleRepository.existsByName(request.getName())) {
            throw new ValidationException("角色名称已存在");
        }
        
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(request.getPermissions());
        
        Role savedRole = roleRepository.save(role);
        log.info("更新角色成功: {}", savedRole.getName());
        
        return convertToResponse(savedRole);
    }
    
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));
        
        // 检查是否有用户使用该角色
        List<User> usersWithRole = userRepository.findByRolesContaining(role);
        if (!usersWithRole.isEmpty()) {
            throw new ValidationException("该角色正在被 " + usersWithRole.size() + " 个用户使用，无法删除");
        }
        
        roleRepository.delete(role);
        log.info("删除角色成功: {}", role.getName());
    }
    
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));
        
        user.getRoles().add(role);
        userRepository.save(user);
        
        log.info("为用户 {} 分配角色 {}", user.getUsername(), role.getName());
    }
    
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));
        
        user.getRoles().remove(role);
        userRepository.save(user);
        
        log.info("移除用户 {} 的角色 {}", user.getUsername(), role.getName());
    }
    
    public List<String> getAllPermissions() {
        return List.of(
            "user:view", "user:create", "user:edit", "user:delete",
            "role:view", "role:create", "role:edit", "role:delete",
            "project:view", "project:create", "project:edit", "project:delete", "project:audit",
            "system:config", "system:log", "system:monitor"
        );
    }
    
    private void validateRoleRequest(RoleRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException("角色名称不能为空");
        }
        
        if (request.getName().length() > 50) {
            throw new ValidationException("角色名称长度不能超过50个字符");
        }
        
        if (request.getDescription() != null && request.getDescription().length() > 200) {
            throw new ValidationException("角色描述长度不能超过200个字符");
        }
        
        if (request.getPermissions() != null) {
            List<String> validPermissions = getAllPermissions();
            for (String permission : request.getPermissions()) {
                if (!validPermissions.contains(permission)) {
                    throw new ValidationException("无效的权限: " + permission);
                }
            }
        }
    }
    
    private RoleResponse convertToResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        response.setPermissions(role.getPermissions());
        response.setCreatedAt(role.getCreatedAt());
        response.setUpdatedAt(role.getUpdatedAt());
        
        // 统计使用该角色的用户数量
        long userCount = userRepository.countByRolesContaining(role);
        response.setUserCount(userCount);
        
        return response;
    }
}