package com.happyvr.controller;

import com.happyvr.dto.auth.JwtResponse;
import com.happyvr.dto.auth.LoginRequest;
import com.happyvr.dto.auth.RegisterRequest;
import com.happyvr.dto.common.ApiResponse;
import com.happyvr.entity.User;
import com.happyvr.security.UserPrincipal;
import com.happyvr.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                         HttpServletRequest request) {
        try {
            logger.info("=== 登录请求开始 ===");
            logger.info("请求URL: {}", request.getRequestURL());
            logger.info("请求方法: {}", request.getMethod());
            logger.info("登录请求来自IP: {}, 用户: {}", 
                       getClientIpAddress(request), loginRequest.getUsernameOrEmail());
            logger.info("请求体 - 用户名: {}, 密码长度: {}, 记住我: {}", 
                       loginRequest.getUsernameOrEmail(),
                       loginRequest.getPassword() != null ? loginRequest.getPassword().length() : 0,
                       loginRequest.getRememberMe());
            
            JwtResponse jwtResponse = authService.login(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("登录成功", jwtResponse));
            
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("登录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest registerRequest,
                                                                    HttpServletRequest request) {
        try {
            logger.info("注册请求来自IP: {}, 用户: {}", 
                       getClientIpAddress(request), registerRequest.getUsername());
            
            User user = authService.register(registerRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            
            return ResponseEntity.ok(ApiResponse.success("注册成功", response));
            
        } catch (Exception e) {
            logger.error("注册失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("注册失败: " + e.getMessage()));
        }
    }
    
    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            JwtResponse jwtResponse = authService.refreshToken(token);
            return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", jwtResponse));
            
        } catch (Exception e) {
            logger.error("令牌刷新失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("令牌刷新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", currentUser.getId());
            userInfo.put("username", currentUser.getUsername());
            userInfo.put("email", currentUser.getEmail());
            userInfo.put("roles", currentUser.getRoles());
            userInfo.put("permissions", currentUser.getPermissions());
            
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", userInfo));
            
        } catch (Exception e) {
            logger.error("获取用户信息失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("用户登出: {}", currentUser.getUsername());
            // 在实际应用中，可以将token加入黑名单
            return ResponseEntity.ok(ApiResponse.success("登出成功"));
            
        } catch (Exception e) {
            logger.error("登出失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("登出失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsername(@RequestParam String username) {
        try {
            boolean available = authService.isUsernameAvailable(username);
            Map<String, Boolean> result = new HashMap<>();
            result.put("available", available);
            
            return ResponseEntity.ok(ApiResponse.success("检查完成", result));
            
        } catch (Exception e) {
            logger.error("检查用户名失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("检查用户名失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(@RequestParam String email) {
        try {
            boolean available = authService.isEmailAvailable(email);
            Map<String, Boolean> result = new HashMap<>();
            result.put("available", available);
            
            return ResponseEntity.ok(ApiResponse.success("检查完成", result));
            
        } catch (Exception e) {
            logger.error("检查邮箱失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("检查邮箱失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}