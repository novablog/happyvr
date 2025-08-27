package com.happyvr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源配置
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置文件访问路径
        String uploadPath = new File(fileUploadConfig.getBasePath()).getAbsolutePath();
        
        registry.addResourceHandler(fileUploadConfig.getBaseUrl() + "/**")
                .addResourceLocations("file:" + uploadPath + File.separator);
    }
}