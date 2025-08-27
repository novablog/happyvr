package com.happyvr.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码测试工具类
 * 用于验证和生成密码哈希
 */
public class PasswordTestUtil {
    
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // 测试密码
        String rawPassword = "admin123";
        String storedHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P2.nRuvnfVyFzu";
        
        System.out.println("=== 密码验证测试 ===");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("存储的哈希: " + storedHash);
        
        // 验证密码是否匹配
        boolean matches = passwordEncoder.matches(rawPassword, storedHash);
        System.out.println("密码匹配结果: " + matches);
        
        // 生成新的密码哈希用于对比
        String newHash = passwordEncoder.encode(rawPassword);
        System.out.println("新生成的哈希: " + newHash);
        
        // 验证新生成的哈希
        boolean newMatches = passwordEncoder.matches(rawPassword, newHash);
        System.out.println("新哈希匹配结果: " + newMatches);
        
        System.out.println("=== 测试完成 ===");
    }
}