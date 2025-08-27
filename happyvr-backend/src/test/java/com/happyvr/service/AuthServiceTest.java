package com.happyvr.service;

import com.happyvr.dto.auth.LoginRequest;
import com.happyvr.dto.auth.RegisterRequest;
import com.happyvr.entity.Role;
import com.happyvr.entity.User;
import com.happyvr.repository.RoleRepository;
import com.happyvr.repository.UserRepository;
import com.happyvr.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 认证服务测试类
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    private Role userRole;
    
    @BeforeEach
    void setUp() {
        // 创建测试角色
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");
        userRole.setPermissions(Arrays.asList("project:create", "project:edit"));
        
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setStatus(1);
    }
    
    @Test
    void testRegisterSuccess() {
        // 准备测试数据
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");
        
        // Mock行为
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // 执行测试
        User result = authService.register(registerRequest);
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        
        // 验证方法调用
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testRegisterWithExistingUsername() {
        // 准备测试数据
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existinguser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");
        
        // Mock行为
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);
        
        // 执行测试并验证异常
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("用户名已存在");
        
        // 验证方法调用
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testRegisterWithPasswordMismatch() {
        // 准备测试数据
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("differentpassword");
        
        // 执行测试并验证异常
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("密码和确认密码不匹配");
        
        // 验证没有调用数据库操作
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testIsUsernameAvailable() {
        // Mock行为
        when(userRepository.existsByUsername("availableuser")).thenReturn(false);
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);
        
        // 执行测试
        boolean available = authService.isUsernameAvailable("availableuser");
        boolean notAvailable = authService.isUsernameAvailable("existinguser");
        
        // 验证结果
        assertThat(available).isTrue();
        assertThat(notAvailable).isFalse();
    }
    
    @Test
    void testIsEmailAvailable() {
        // Mock行为
        when(userRepository.existsByEmail("available@example.com")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        // 执行测试
        boolean available = authService.isEmailAvailable("available@example.com");
        boolean notAvailable = authService.isEmailAvailable("existing@example.com");
        
        // 验证结果
        assertThat(available).isTrue();
        assertThat(notAvailable).isFalse();
    }
}