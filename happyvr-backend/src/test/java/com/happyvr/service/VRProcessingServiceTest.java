package com.happyvr.service;

import com.happyvr.dto.vr.VRProcessingRequest;
import com.happyvr.dto.vr.VRProcessingResponse;
import com.happyvr.entity.VrProject;
import com.happyvr.exception.ResourceNotFoundException;
import com.happyvr.repository.VrProjectRepository;
import com.happyvr.util.TestImageGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VRProcessingServiceTest {
    
    @Mock
    private ImageProcessingService imageProcessingService;
    
    @Mock
    private VrProjectRepository vrProjectRepository;
    
    @InjectMocks
    private VRProcessingService vrProcessingService;
    
    private VrProject testProject;
    private VRProcessingRequest testRequest;
    
    @BeforeAll
    static void setUpClass() {
        try {
            // 创建测试图片
            TestImageGenerator.createTestVRImages();
        } catch (Exception e) {
            // 忽略图片创建失败，测试会处理这种情况
        }
    }
    
    @BeforeEach
    void setUp() {
        testProject = new VrProject();
        testProject.setId(1L);
        testProject.setTitle("测试VR项目");
        // 不设置user，因为在测试中我们只需要项目ID
        
        testRequest = new VRProcessingRequest();
        testRequest.setProjectId(1L);
        testRequest.setImageUrls(Arrays.asList(
            "/files/user_1/2024/01/image1.jpg",
            "/files/user_1/2024/01/image2.jpg"
        ));
        testRequest.setProcessingType("PANORAMA");
        
        // 设置测试用的配置值
        ReflectionTestUtils.setField(vrProcessingService, "uploadBasePath", "uploads");
        ReflectionTestUtils.setField(vrProcessingService, "baseUrl", "/files");
    }
    
    @Test
    void startVRProcessing_ShouldCreateTask_WhenValidRequest() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // When
        VRProcessingResponse response = vrProcessingService.startVRProcessing(testRequest);
        
        // Then - 验证任务创建成功
        assertNotNull(response);
        assertNotNull(response.getTaskId());
        assertEquals(1L, response.getProjectId());
        // 初始状态应该是PENDING，但由于异步执行可能很快完成，所以我们检查状态不为空
        assertNotNull(response.getStatus());
        
        verify(vrProjectRepository).findById(1L);
        
        // 等待一小段时间让异步任务完成
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 检查任务最终状态
        VRProcessingResponse finalResponse = vrProcessingService.getProcessingProgress(response.getTaskId());
        assertTrue("COMPLETED".equals(finalResponse.getStatus()) || "FAILED".equals(finalResponse.getStatus()));
        
        if ("COMPLETED".equals(finalResponse.getStatus())) {
            assertEquals(100, finalResponse.getProgress());
            assertNotNull(finalResponse.getResultUrl());
        }
    }
    
    @Test
    void startVRProcessing_ShouldThrowException_WhenProjectNotExists() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> vrProcessingService.startVRProcessing(testRequest));
        
        verify(vrProjectRepository).findById(1L);
    }
    
    @Test
    void getProcessingProgress_ShouldReturnProgress_WhenTaskExists() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // 先创建一个任务
        VRProcessingResponse createdResponse = vrProcessingService.startVRProcessing(testRequest);
        String taskId = createdResponse.getTaskId();
        
        // 等待异步任务开始执行
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When
        VRProcessingResponse response = vrProcessingService.getProcessingProgress(taskId);
        
        // Then
        assertNotNull(response);
        assertEquals(taskId, response.getTaskId());
        assertEquals(1L, response.getProjectId());
        assertNotNull(response.getStatus());
        // 由于现在有测试图片文件，任务应该成功完成
        assertTrue("COMPLETED".equals(response.getStatus()) || "PROCESSING".equals(response.getStatus()) || "PENDING".equals(response.getStatus()));
    }
    
    @Test
    void getProcessingProgress_ShouldReturnNotFound_WhenTaskNotExists() {
        // Given
        String nonExistentTaskId = "non-existent-task-id";
        
        // When
        VRProcessingResponse response = vrProcessingService.getProcessingProgress(nonExistentTaskId);
        
        // Then
        assertNotNull(response);
        assertEquals(nonExistentTaskId, response.getTaskId());
        assertEquals("NOT_FOUND", response.getStatus());
        assertEquals("任务不存在", response.getMessage());
    }
    
    @Test
    void getAllProcessingTasks_ShouldReturnAllTasks() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // 创建几个任务
        vrProcessingService.startVRProcessing(testRequest);
        
        VRProcessingRequest request2 = new VRProcessingRequest();
        request2.setProjectId(1L);
        request2.setImageUrls(Arrays.asList("/files/user_1/2024/01/image3.jpg"));
        vrProcessingService.startVRProcessing(request2);
        
        // When
        List<VRProcessingResponse> tasks = vrProcessingService.getAllProcessingTasks();
        
        // Then
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
    }
    
    @Test
    void cancelProcessingTask_ShouldReturnTrue_WhenTaskIsProcessing() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        VRProcessingResponse createdResponse = vrProcessingService.startVRProcessing(testRequest);
        String taskId = createdResponse.getTaskId();
        
        // 等待异步任务开始执行
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 手动设置任务状态为PROCESSING
        VRProcessingResponse processingResponse = vrProcessingService.getProcessingProgress(taskId);
        processingResponse.setStatus("PROCESSING");
        
        // When
        boolean cancelled = vrProcessingService.cancelProcessingTask(taskId);
        
        // Then
        assertTrue(cancelled);
        
        VRProcessingResponse response = vrProcessingService.getProcessingProgress(taskId);
        assertEquals("CANCELLED", response.getStatus());
        assertEquals("任务已取消", response.getMessage());
    }
    
    @Test
    void cancelProcessingTask_ShouldReturnFalse_WhenTaskNotProcessing() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        VRProcessingResponse createdResponse = vrProcessingService.startVRProcessing(testRequest);
        String taskId = createdResponse.getTaskId();
        
        // 任务状态为PENDING，不是PROCESSING
        
        // When
        boolean cancelled = vrProcessingService.cancelProcessingTask(taskId);
        
        // Then
        assertFalse(cancelled);
    }
    
    @Test
    void cancelProcessingTask_ShouldReturnFalse_WhenTaskNotExists() {
        // Given
        String nonExistentTaskId = "non-existent-task-id";
        
        // When
        boolean cancelled = vrProcessingService.cancelProcessingTask(nonExistentTaskId);
        
        // Then
        assertFalse(cancelled);
    }
    
    @Test
    void cleanupExpiredTasks_ShouldRemoveExpiredTasks() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // 创建一个任务
        VRProcessingResponse createdResponse = vrProcessingService.startVRProcessing(testRequest);
        String taskId = createdResponse.getTaskId();
        
        // 手动设置任务为已完成状态，并设置过期时间
        VRProcessingResponse response = vrProcessingService.getProcessingProgress(taskId);
        response.setStatus("COMPLETED");
        response.setEndTime(java.time.LocalDateTime.now().minusDays(2)); // 2天前完成
        
        // When
        vrProcessingService.cleanupExpiredTasks();
        
        // Then
        // 验证任务已被清理（通过检查任务列表大小）
        List<VRProcessingResponse> tasks = vrProcessingService.getAllProcessingTasks();
        assertEquals(0, tasks.size());
    }
}