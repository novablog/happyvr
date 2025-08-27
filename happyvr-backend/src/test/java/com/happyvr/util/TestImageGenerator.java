package com.happyvr.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 测试图片生成器
 */
public class TestImageGenerator {
    
    /**
     * 创建测试图片
     */
    public static void createTestImage(String filePath, int width, int height, Color color) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置背景色
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        
        // 添加一些文字标识
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Test Image", width / 2 - 50, height / 2);
        
        g2d.dispose();
        
        // 确保目录存在
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        
        // 保存图片
        ImageIO.write(image, "jpg", file);
    }
    
    /**
     * 创建测试用的VR项目图片
     */
    public static void createTestVRImages() throws IOException {
        String basePath = "uploads/user_1/2024/01/";
        
        createTestImage(basePath + "image1.jpg", 800, 600, Color.BLUE);
        createTestImage(basePath + "image2.jpg", 800, 600, Color.RED);
        createTestImage(basePath + "image3.jpg", 800, 600, Color.GREEN);
    }
}