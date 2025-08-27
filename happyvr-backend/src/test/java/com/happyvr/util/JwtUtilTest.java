package com.happyvr.util;

import com.happyvr.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT工具类测试
 */
public class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    private UserPrincipal userPrincipal;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        
        // 设置测试配置 - 密钥必须至少64字节(512位)才能用于HS512算法
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyForJwtTokenGenerationAndValidationMustBeAtLeast64BytesLongForHS512Algorithm");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24小时
        
        // 创建测试用户主体
        userPrincipal = new UserPrincipal(
                1L,
                "testuser",
                "test@example.com",
                "password",
                Arrays.asList(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("project:create")
                ),
                true
        );
    }
    
    @Test
    void testGenerateToken() {
        // 生成token
        String token = jwtUtil.generateToken(userPrincipal);
        
        // 验证token不为空
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // 验证可以从token中提取用户名
        String username = jwtUtil.getUsernameFromToken(token);
        assertThat(username).isEqualTo("testuser");
    }
    
    @Test
    void testGenerateTokenWithClaims() {
        // 准备额外声明
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("roles", Arrays.asList("USER"));
        
        // 生成token
        String token = jwtUtil.generateToken(userPrincipal, claims);
        
        // 验证token不为空
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // 验证可以从token中提取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        assertThat(userId).isEqualTo(1L);
    }
    
    @Test
    void testValidateToken() {
        // 生成token
        String token = jwtUtil.generateToken(userPrincipal);
        
        // 验证token
        Boolean isValid = jwtUtil.validateToken(token, userPrincipal);
        assertThat(isValid).isTrue();
    }
    
    @Test
    void testValidateTokenFormat() {
        // 生成token
        String token = jwtUtil.generateToken(userPrincipal);
        
        // 验证token格式
        Boolean isValidFormat = jwtUtil.validateTokenFormat(token);
        assertThat(isValidFormat).isTrue();
        
        // 验证无效token格式
        Boolean isInvalidFormat = jwtUtil.validateTokenFormat("invalid.token.format");
        assertThat(isInvalidFormat).isFalse();
    }
    
    @Test
    void testGetExpirationDateFromToken() {
        // 生成token
        String token = jwtUtil.generateToken(userPrincipal);
        
        // 获取过期时间
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        
        // 验证过期时间在未来
        assertThat(expirationDate).isAfter(new Date());
    }
    
    @Test
    void testIsTokenExpired() {
        // 生成token
        String token = jwtUtil.generateToken(userPrincipal);
        
        // 验证token未过期
        Boolean isExpired = jwtUtil.isTokenExpired(token);
        assertThat(isExpired).isFalse();
    }
    
    @Test
    void testGetUserIdFromToken() {
        // 准备额外声明
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123L);
        
        // 生成token
        String token = jwtUtil.generateToken(userPrincipal, claims);
        
        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        assertThat(userId).isEqualTo(123L);
    }
    
    @Test
    void testGetRolesFromToken() {
        // 准备额外声明
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", Arrays.asList("USER", "ADMIN"));
        
        // 生成token
        String token = jwtUtil.generateToken(userPrincipal, claims);
        
        // 从token中获取角色
        java.util.List<String> roles = jwtUtil.getRolesFromToken(token);
        assertThat(roles).containsExactly("USER", "ADMIN");
    }
}