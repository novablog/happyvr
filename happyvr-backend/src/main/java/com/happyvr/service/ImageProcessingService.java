package com.happyvr.service;

import com.happyvr.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 图片处理服务
 */
@Service
public class ImageProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);
    
    /**
     * 调整图片大小
     */
    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        logger.debug("调整图片大小: {}x{} -> {}x{}", 
            originalImage.getWidth(), originalImage.getHeight(), targetWidth, targetHeight);
        
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }
    
    /**
     * 优化图片质量
     */
    public BufferedImage optimizeImage(BufferedImage image, int quality) {
        logger.debug("优化图片质量: quality={}", quality);
        
        // 如果质量设置为100，直接返回原图
        if (quality >= 100) {
            return image;
        }
        
        // 创建优化后的图片
        BufferedImage optimizedImage = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = optimizedImage.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        
        return optimizedImage;
    }
    
    /**
     * 转换图片格式
     */
    public void convertImageFormat(String inputPath, String outputPath, String format) throws IOException {
        logger.debug("转换图片格式: {} -> {} ({})", inputPath, outputPath, format);
        
        try {
            BufferedImage image = ImageIO.read(new File(inputPath));
            if (image == null) {
                throw new ValidationException("无法读取图片文件: " + inputPath);
            }
        
        // 如果是JPEG格式，需要移除透明度
        if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
            BufferedImage jpegImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = jpegImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            image = jpegImage;
        }
        
            boolean success = ImageIO.write(image, format, new File(outputPath));
            if (!success) {
                throw new ValidationException("不支持的图片格式: " + format);
            }
            
            logger.info("图片格式转换成功: {}", outputPath);
        } catch (IOException e) {
            logger.error("转换图片格式失败: " + inputPath, e);
            throw new ValidationException("转换图片格式失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建全景图片拼接
     */
    public BufferedImage createPanorama(List<BufferedImage> images, int outputWidth, int outputHeight) {
        logger.debug("创建全景图片拼接: {} 张图片 -> {}x{}", images.size(), outputWidth, outputHeight);
        
        if (images.isEmpty()) {
            throw new ValidationException("图片列表不能为空");
        }
        
        // 如果只有一张图片，直接调整大小返回
        if (images.size() == 1) {
            return resizeImage(images.get(0), outputWidth, outputHeight);
        }
        
        // 创建全景图片
        BufferedImage panorama = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = panorama.createGraphics();
        
        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // 简单的水平拼接算法
        int imageWidth = outputWidth / images.size();
        int x = 0;
        
        for (BufferedImage image : images) {
            BufferedImage resizedImage = resizeImage(image, imageWidth, outputHeight);
            g2d.drawImage(resizedImage, x, 0, null);
            x += imageWidth;
        }
        
        g2d.dispose();
        
        logger.info("全景图片拼接完成");
        return panorama;
    }
    
    /**
     * 创建球面映射
     */
    public BufferedImage createSphericalMapping(BufferedImage panorama) {
        logger.debug("创建球面映射: {}x{}", panorama.getWidth(), panorama.getHeight());
        
        int width = panorama.getWidth();
        int height = panorama.getHeight();
        
        BufferedImage sphericalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // 球面映射算法
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 将像素坐标转换为球面坐标
                double theta = (double) x / width * 2 * Math.PI; // 经度
                double phi = (double) y / height * Math.PI; // 纬度
                
                // 球面坐标转换为笛卡尔坐标
                double cartX = Math.sin(phi) * Math.cos(theta);
                double cartY = Math.cos(phi);
                double cartZ = Math.sin(phi) * Math.sin(theta);
                
                // 将笛卡尔坐标映射回图片坐标
                int sourceX = (int) ((Math.atan2(cartZ, cartX) + Math.PI) / (2 * Math.PI) * width);
                int sourceY = (int) (Math.acos(cartY) / Math.PI * height);
                
                // 边界检查
                sourceX = Math.max(0, Math.min(width - 1, sourceX));
                sourceY = Math.max(0, Math.min(height - 1, sourceY));
                
                // 复制像素
                int rgb = panorama.getRGB(sourceX, sourceY);
                sphericalImage.setRGB(x, y, rgb);
            }
        }
        
        logger.info("球面映射完成");
        return sphericalImage;
    }
    
    /**
     * 保存图片到文件
     */
    public void saveImage(BufferedImage image, String outputPath, String format) throws IOException {
        logger.debug("保存图片: {} ({})", outputPath, format);
        
        // 确保输出目录存在
        Path path = Paths.get(outputPath);
        File parentDir = path.getParent().toFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        boolean success = ImageIO.write(image, format, new File(outputPath));
        if (!success) {
            throw new ValidationException("保存图片失败: " + outputPath);
        }
        
        logger.info("图片保存成功: {}", outputPath);
    }
    
    /**
     * 获取图片信息
     */
    public ImageInfo getImageInfo(String imagePath) throws IOException {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            if (image == null) {
                throw new ValidationException("无法读取图片文件: " + imagePath);
            }
            
            return new ImageInfo(image.getWidth(), image.getHeight(), 
                image.getColorModel().hasAlpha(), new File(imagePath).length());
        } catch (IOException e) {
            logger.error("获取图片信息失败: " + imagePath, e);
            throw new ValidationException("获取图片信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 图片信息类
     */
    public static class ImageInfo {
        private final int width;
        private final int height;
        private final boolean hasAlpha;
        private final long fileSize;
        
        public ImageInfo(int width, int height, boolean hasAlpha, long fileSize) {
            this.width = width;
            this.height = height;
            this.hasAlpha = hasAlpha;
            this.fileSize = fileSize;
        }
        
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public boolean hasAlpha() { return hasAlpha; }
        public long getFileSize() { return fileSize; }
    }
}