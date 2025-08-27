package com.happyvr.service;

import com.happyvr.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImageProcessingServiceTest {
    
    @InjectMocks
    private ImageProcessingService imageProcessingService;
    
    @TempDir
    Path tempDir;
    
    private BufferedImage testImage;
    
    @BeforeEach
    void setUp() {
        // 创建测试用的图片
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = testImage.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();
    }
    
    @Test
    void resizeImage_ShouldResizeCorrectly() {
        // Given
        int targetWidth = 200;
        int targetHeight = 150;
        
        // When
        BufferedImage resizedImage = imageProcessingService.resizeImage(testImage, targetWidth, targetHeight);
        
        // Then
        assertNotNull(resizedImage);
        assertEquals(targetWidth, resizedImage.getWidth());
        assertEquals(targetHeight, resizedImage.getHeight());
    }
    
    @Test
    void optimizeImage_ShouldReturnOriginal_WhenQuality100() {
        // Given
        int quality = 100;
        
        // When
        BufferedImage optimizedImage = imageProcessingService.optimizeImage(testImage, quality);
        
        // Then
        assertNotNull(optimizedImage);
        assertEquals(testImage.getWidth(), optimizedImage.getWidth());
        assertEquals(testImage.getHeight(), optimizedImage.getHeight());
    }
    
    @Test
    void optimizeImage_ShouldOptimize_WhenQualityLess100() {
        // Given
        int quality = 80;
        
        // When
        BufferedImage optimizedImage = imageProcessingService.optimizeImage(testImage, quality);
        
        // Then
        assertNotNull(optimizedImage);
        assertEquals(testImage.getWidth(), optimizedImage.getWidth());
        assertEquals(testImage.getHeight(), optimizedImage.getHeight());
    }
    
    @Test
    void createPanorama_ShouldThrowException_WhenImagesEmpty() {
        // Given
        List<BufferedImage> emptyImages = Arrays.asList();
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> imageProcessingService.createPanorama(emptyImages, 400, 200));
    }
    
    @Test
    void createPanorama_ShouldReturnResized_WhenSingleImage() {
        // Given
        List<BufferedImage> singleImage = Arrays.asList(testImage);
        int outputWidth = 400;
        int outputHeight = 200;
        
        // When
        BufferedImage panorama = imageProcessingService.createPanorama(singleImage, outputWidth, outputHeight);
        
        // Then
        assertNotNull(panorama);
        assertEquals(outputWidth, panorama.getWidth());
        assertEquals(outputHeight, panorama.getHeight());
    }
    
    @Test
    void createPanorama_ShouldStitchImages_WhenMultipleImages() {
        // Given
        BufferedImage image2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image2.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();
        
        List<BufferedImage> images = Arrays.asList(testImage, image2);
        int outputWidth = 400;
        int outputHeight = 200;
        
        // When
        BufferedImage panorama = imageProcessingService.createPanorama(images, outputWidth, outputHeight);
        
        // Then
        assertNotNull(panorama);
        assertEquals(outputWidth, panorama.getWidth());
        assertEquals(outputHeight, panorama.getHeight());
    }
    
    @Test
    void createSphericalMapping_ShouldCreateMapping() {
        // Given
        BufferedImage panorama = new BufferedImage(400, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = panorama.createGraphics();
        g2d.setColor(Color.GREEN);
        g2d.fillRect(0, 0, 400, 200);
        g2d.dispose();
        
        // When
        BufferedImage sphericalImage = imageProcessingService.createSphericalMapping(panorama);
        
        // Then
        assertNotNull(sphericalImage);
        assertEquals(panorama.getWidth(), sphericalImage.getWidth());
        assertEquals(panorama.getHeight(), sphericalImage.getHeight());
    }
    
    @Test
    void saveImage_ShouldSaveSuccessfully() throws IOException {
        // Given
        String outputPath = tempDir.resolve("test_output.jpg").toString();
        String format = "jpg";
        
        // When
        assertDoesNotThrow(() -> imageProcessingService.saveImage(testImage, outputPath, format));
        
        // Then
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
    
    @Test
    void saveImage_ShouldThrowException_WhenInvalidFormat() {
        // Given
        String outputPath = tempDir.resolve("test_output.invalid").toString();
        String format = "invalid";
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> imageProcessingService.saveImage(testImage, outputPath, format));
    }
    
    @Test
    void convertImageFormat_ShouldThrowException_WhenFileNotExists() {
        // Given
        String inputPath = "non_existent_file.jpg";
        String outputPath = tempDir.resolve("output.png").toString();
        String format = "png";
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> imageProcessingService.convertImageFormat(inputPath, outputPath, format));
    }
    
    @Test
    void getImageInfo_ShouldThrowException_WhenFileNotExists() {
        // Given
        String imagePath = "non_existent_file.jpg";
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> imageProcessingService.getImageInfo(imagePath));
    }
}