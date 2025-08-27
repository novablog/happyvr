package com.happyvr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 数据库配置类
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.happyvr.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    
    // JPA审计功能已通过@EnableJpaAuditing启用
    // 这将自动填充BaseEntity中的createdAt和updatedAt字段
}