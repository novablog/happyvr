package com.happyvr.security;

import com.happyvr.entity.Role;
import com.happyvr.entity.User;
import com.happyvr.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionInterceptorTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private SecurityContext securityContext;
    
    @InjectMocks
    private PermissionInterceptor permissionInterceptor;
    
    private User testUser;
    private Role testRole;
    
    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setName("TEST_ROLE");
        testRole.setPermissions(Arrays.asList("user:view", "user:create"));
        
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setRoles(new HashSet<>(Arrays.asList(testRole)));
        
        SecurityContextHolder.setContext(securityContext);
    }
    
    @Test
    void preHandle_ShouldReturnTrue_WhenNoPermissionRequired() throws Exception {
        // Given
        Object handler = new Object(); // 不是HandlerMethod
        
        // When
        boolean result = permissionInterceptor.preHandle(request, response, handler);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void preHandle_ShouldReturnTrue_WhenNoRequirePermissionAnnotation() throws Exception {
        // Given
        Method method = TestController.class.getMethod("methodWithoutPermission");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);
        
        // When
        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void preHandle_ShouldThrowException_WhenNotAuthenticated() throws Exception {
        // Given
        Method method = TestController.class.getMethod("methodWithPermission");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);
        
        when(securityContext.getAuthentication()).thenReturn(null);
        
        // When & Then
        assertThrows(AccessDeniedException.class, 
            () -> permissionInterceptor.preHandle(request, response, handlerMethod));
    }
    
    @Test
    void preHandle_ShouldThrowException_WhenUserNotFound() throws Exception {
        // Given
        Method method = TestController.class.getMethod("methodWithPermission");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(AccessDeniedException.class, 
            () -> permissionInterceptor.preHandle(request, response, handlerMethod));
    }
    
    @Test
    void preHandle_ShouldReturnTrue_WhenUserHasPermission() throws Exception {
        // Given
        Method method = TestController.class.getMethod("methodWithPermission");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void preHandle_ShouldThrowException_WhenUserLacksPermission() throws Exception {
        // Given
        Method method = TestController.class.getMethod("methodWithAdminPermission");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When & Then
        assertThrows(AccessDeniedException.class, 
            () -> permissionInterceptor.preHandle(request, response, handlerMethod));
    }
    
    @Test
    void preHandle_ShouldReturnTrue_WhenUserHasWildcardPermission() throws Exception {
        // Given
        testRole.setPermissions(Arrays.asList("*")); // 超级管理员权限
        Method method = TestController.class.getMethod("methodWithAdminPermission");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
    }
    
    // 测试用的控制器类
    static class TestController {
        
        public void methodWithoutPermission() {
            // 无权限要求的方法
        }
        
        @RequirePermission("user:view")
        public void methodWithPermission() {
            // 需要user:view权限的方法
        }
        
        @RequirePermission("admin:manage")
        public void methodWithAdminPermission() {
            // 需要admin:manage权限的方法
        }
    }
}