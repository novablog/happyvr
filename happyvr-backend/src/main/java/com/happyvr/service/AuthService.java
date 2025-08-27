package com.happyvr.service;

import com.happyvr.dto.auth.JwtResponse;
import com.happyvr.dto.auth.LoginRequest;
import com.happyvr.dto.auth.RegisterRequest;
import com.happyvr.entity.Role;
import com.happyvr.entity.User;
import com.happyvr.repository.RoleRepository;
import com.happyvr.repository.UserRepository;
import com.happyvr.security.UserPrincipal;
import com.happyvr.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 认证服务类
 */
@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户登录
     */
    public JwtResponse login(LoginRequest loginRequest) {
        logger.info("用户登录尝试: {}", loginRequest.getUsernameOrEmail());
        
        // 添加密码验证调试
        try {
            User user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail())
                    .orElse(null);
            if (user != null) {
                logger.info("找到用户: {}", user.getUsername());
                logger.info("数据库中的密码哈希: {}", user.getPassword());
                logger.info("输入的原始密码: {}", loginRequest.getPassword());
                
                boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
                logger.info("密码匹配结果: {}", passwordMatches);
                
                if (!passwordMatches) {
                    // 生成新的密码哈希用于对比
                    String newHash = passwordEncoder.encode(loginRequest.getPassword());
                    logger.info("为输入密码生成的新哈希: {}", newHash);
                }
            } else {
                logger.warn("未找到用户: {}", loginRequest.getUsernameOrEmail());
            }
        } catch (Exception e) {
            logger.error("密码验证调试失败", e);
        }
        
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userPrincipal.getId());
        claims.put("roles", userPrincipal.getRoles());
        claims.put("permissions", userPrincipal.getPermissions());
        
        String jwt = jwtUtil.generateToken(userPrincipal, claims);
        
        logger.info("用户登录成功: {}", userPrincipal.getUsername());
        
        return new JwtResponse(
                jwt,
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getEmail(),
                userPrincipal.getRoles(),
                userPrincipal.getPermissions()
        );
    }
    
    /**
     * 用户注册
     */
    public User register(RegisterRequest registerRequest) {
        logger.info("用户注册尝试: {}", registerRequest.getUsername());
        
        // 验证密码匹配
        if (!registerRequest.isPasswordMatching()) {
            throw new RuntimeException("密码和确认密码不匹配");
        }
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhone(registerRequest.getPhone());
        user.setStatus(1); // 启用状态
        
        // 分配默认用户角色
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("默认用户角色不存在"));
        user.setRoles(Set.of(userRole));
        
        User savedUser = userRepository.save(user);
        logger.info("用户注册成功: {}", savedUser.getUsername());
        
        return savedUser;
    }
    
    /**
     * 刷新令牌
     */
    public JwtResponse refreshToken(String token) {
        logger.info("刷新令牌请求");
        
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (!user.isEnabled()) {
                throw new RuntimeException("用户已被禁用");
            }
            
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            
            // 生成新的JWT令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userPrincipal.getId());
            claims.put("roles", userPrincipal.getRoles());
            claims.put("permissions", userPrincipal.getPermissions());
            
            String newJwt = jwtUtil.generateToken(userPrincipal, claims);
            
            logger.info("令牌刷新成功: {}", username);
            
            return new JwtResponse(
                    newJwt,
                    userPrincipal.getId(),
                    userPrincipal.getUsername(),
                    userPrincipal.getEmail(),
                    userPrincipal.getRoles(),
                    userPrincipal.getPermissions()
            );
        } catch (Exception e) {
            logger.error("令牌刷新失败", e);
            throw new RuntimeException("令牌刷新失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户名是否可用
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    /**
     * 检查邮箱是否可用
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    /**
     * 获取当前认证用户
     */
    public UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId() {
        UserPrincipal currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }
}