package com.happyvr.service;

import com.happyvr.dto.role.RoleRequest;
import com.happyvr.dto.role.RoleResponse;
import com.happyvr.entity.Role;
import com.happyvr.entity.User;
import com.happyvr.exception.ResourceNotFoundException;
import com.happyvr.exception.ValidationException;
import com.happyvr.repository.RoleRepository;
import com.happyvr.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private RoleService roleService;
    
    private Role testRole;
    private RoleRequest roleRequest;
    
    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("TEST_ROLE");
        testRole.setDescription("测试角色");
        testRole.setPermissions(Arrays.asList("user:view", "user:create"));
        testRole.setCreatedAt(LocalDateTime.now());
        testRole.setUpdatedAt(LocalDateTime.now());
        
        roleRequest = new RoleRequest();
        roleRequest.setName("TEST_ROLE");
        roleRequest.setDescription("测试角色");
        roleRequest.setPermissions(Arrays.asList("user:view", "user:create"));
    }
    
    @Test
    void getAllRoles_ShouldReturnPageOfRoles() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Role> rolePage = new PageImpl<>(Arrays.asList(testRole));
        when(roleRepository.findAll(pageable)).thenReturn(rolePage);
        when(userRepository.countByRolesContaining(testRole)).thenReturn(5L);
        
        // When
        Page<RoleResponse> result = roleService.getAllRoles(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        RoleResponse response = result.getContent().get(0);
        assertEquals(testRole.getId(), response.getId());
        assertEquals(testRole.getName(), response.getName());
        assertEquals(5L, response.getUserCount());
    }
    
    @Test
    void getRoleById_ShouldReturnRole_WhenRoleExists() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRepository.countByRolesContaining(testRole)).thenReturn(3L);
        
        // When
        RoleResponse result = roleService.getRoleById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(testRole.getId(), result.getId());
        assertEquals(testRole.getName(), result.getName());
        assertEquals(3L, result.getUserCount());
    }
    
    @Test
    void getRoleById_ShouldThrowException_WhenRoleNotExists() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> roleService.getRoleById(1L));
    }
    
    @Test
    void createRole_ShouldCreateRole_WhenValidRequest() {
        // Given
        when(roleRepository.existsByName(roleRequest.getName())).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        when(userRepository.countByRolesContaining(testRole)).thenReturn(0L);
        
        // When
        RoleResponse result = roleService.createRole(roleRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(testRole.getName(), result.getName());
        verify(roleRepository).save(any(Role.class));
    }
    
    @Test
    void createRole_ShouldThrowException_WhenRoleNameExists() {
        // Given
        when(roleRepository.existsByName(roleRequest.getName())).thenReturn(true);
        
        // When & Then
        assertThrows(ValidationException.class, () -> roleService.createRole(roleRequest));
        verify(roleRepository, never()).save(any(Role.class));
    }
    
    @Test
    void createRole_ShouldThrowException_WhenRoleNameIsEmpty() {
        // Given
        roleRequest.setName("");
        
        // When & Then
        assertThrows(ValidationException.class, () -> roleService.createRole(roleRequest));
    }
    
    @Test
    void createRole_ShouldThrowException_WhenInvalidPermission() {
        // Given
        roleRequest.setPermissions(Arrays.asList("invalid:permission"));
        
        // When & Then
        assertThrows(ValidationException.class, () -> roleService.createRole(roleRequest));
    }
    
    @Test
    void updateRole_ShouldUpdateRole_WhenValidRequest() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        when(userRepository.countByRolesContaining(testRole)).thenReturn(2L);
        
        roleRequest.setName("UPDATED_ROLE");
        roleRequest.setDescription("更新的角色");
        
        // When
        RoleResponse result = roleService.updateRole(1L, roleRequest);
        
        // Then
        assertNotNull(result);
        verify(roleRepository).save(any(Role.class));
    }
    
    @Test
    void updateRole_ShouldThrowException_WhenRoleNotExists() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> roleService.updateRole(1L, roleRequest));
    }
    
    @Test
    void deleteRole_ShouldDeleteRole_WhenNoUsersAssigned() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRepository.findByRolesContaining(testRole)).thenReturn(Collections.emptyList());
        
        // When
        roleService.deleteRole(1L);
        
        // Then
        verify(roleRepository).delete(testRole);
    }
    
    @Test
    void deleteRole_ShouldThrowException_WhenUsersAssigned() {
        // Given
        User user = new User();
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRepository.findByRolesContaining(testRole)).thenReturn(Arrays.asList(user));
        
        // When & Then
        assertThrows(ValidationException.class, () -> roleService.deleteRole(1L));
        verify(roleRepository, never()).delete(any(Role.class));
    }
    
    @Test
    void assignRoleToUser_ShouldAssignRole_WhenValidUserAndRole() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setRoles(new HashSet<>());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRepository.save(user)).thenReturn(user);
        
        // When
        roleService.assignRoleToUser(1L, 1L);
        
        // Then
        assertTrue(user.getRoles().contains(testRole));
        verify(userRepository).save(user);
    }
    
    @Test
    void assignRoleToUser_ShouldThrowException_WhenUserNotExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> roleService.assignRoleToUser(1L, 1L));
    }
    
    @Test
    void removeRoleFromUser_ShouldRemoveRole_WhenValidUserAndRole() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setRoles(new HashSet<>(Arrays.asList(testRole)));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRepository.save(user)).thenReturn(user);
        
        // When
        roleService.removeRoleFromUser(1L, 1L);
        
        // Then
        assertFalse(user.getRoles().contains(testRole));
        verify(userRepository).save(user);
    }
    
    @Test
    void getAllPermissions_ShouldReturnAllPermissions() {
        // When
        List<String> permissions = roleService.getAllPermissions();
        
        // Then
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());
        assertTrue(permissions.contains("user:view"));
        assertTrue(permissions.contains("role:create"));
        assertTrue(permissions.contains("system:config"));
    }
}