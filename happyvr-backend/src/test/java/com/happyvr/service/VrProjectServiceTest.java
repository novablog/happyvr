package com.happyvr.service;

import com.happyvr.dto.project.VrProjectCreateRequest;
import com.happyvr.dto.project.VrProjectListResponse;
import com.happyvr.dto.project.VrProjectResponse;
import com.happyvr.dto.project.VrProjectUpdateRequest;
import com.happyvr.entity.User;
import com.happyvr.entity.VrProject;
import com.happyvr.exception.ResourceNotFoundException;
import com.happyvr.exception.UnauthorizedException;
import com.happyvr.exception.ValidationException;
import com.happyvr.repository.UserRepository;
import com.happyvr.repository.VrProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VrProjectServiceTest {
    
    @Mock
    private VrProjectRepository vrProjectRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private VrProjectService vrProjectService;
    
    private User testUser;
    private VrProject testProject;
    private VrProjectCreateRequest createRequest;
    private VrProjectUpdateRequest updateRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        testProject = new VrProject();
        testProject.setId(1L);
        testProject.setTitle("测试VR项目");
        testProject.setDescription("这是一个测试项目");
        testProject.setUser(testUser);
        testProject.setStatus(0);
        testProject.setShareToken("test-share-token");
        
        createRequest = new VrProjectCreateRequest();
        createRequest.setTitle("新VR项目");
        createRequest.setDescription("新项目描述");
        
        updateRequest = new VrProjectUpdateRequest();
        updateRequest.setTitle("更新的VR项目");
        updateRequest.setDescription("更新的项目描述");
        updateRequest.setStatus(1);
    }
    
    @Test
    void createProject_ShouldCreateProject_WhenValidRequest() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(vrProjectRepository.existsByShareToken(anyString())).thenReturn(false);
        when(vrProjectRepository.save(any(VrProject.class))).thenReturn(testProject);
        
        // When
        VrProjectResponse response = vrProjectService.createProject(createRequest, 1L);
        
        // Then
        assertNotNull(response);
        assertEquals(testProject.getId(), response.getId());
        assertEquals(testProject.getTitle(), response.getTitle());
        assertEquals(testProject.getDescription(), response.getDescription());
        
        verify(userRepository).findById(1L);
        verify(vrProjectRepository).save(any(VrProject.class));
    }
    
    @Test
    void createProject_ShouldThrowException_WhenUserNotExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> vrProjectService.createProject(createRequest, 1L));
        
        verify(userRepository).findById(1L);
        verify(vrProjectRepository, never()).save(any(VrProject.class));
    }
    
    @Test
    void updateProject_ShouldUpdateProject_WhenValidRequest() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(vrProjectRepository.save(any(VrProject.class))).thenReturn(testProject);
        
        // When
        VrProjectResponse response = vrProjectService.updateProject(1L, updateRequest, 1L);
        
        // Then
        assertNotNull(response);
        assertEquals(testProject.getId(), response.getId());
        
        verify(vrProjectRepository).findById(1L);
        verify(vrProjectRepository).save(any(VrProject.class));
    }
    
    @Test
    void updateProject_ShouldThrowException_WhenProjectNotExists() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> vrProjectService.updateProject(1L, updateRequest, 1L));
        
        verify(vrProjectRepository).findById(1L);
        verify(vrProjectRepository, never()).save(any(VrProject.class));
    }
    
    @Test
    void updateProject_ShouldThrowException_WhenUnauthorized() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // When & Then
        assertThrows(UnauthorizedException.class, 
            () -> vrProjectService.updateProject(1L, updateRequest, 2L));
        
        verify(vrProjectRepository).findById(1L);
        verify(vrProjectRepository, never()).save(any(VrProject.class));
    }
    
    @Test
    void getProject_ShouldReturnProject_WhenOwnerAccess() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // When
        VrProjectResponse response = vrProjectService.getProject(1L, 1L);
        
        // Then
        assertNotNull(response);
        assertEquals(testProject.getId(), response.getId());
        assertEquals(testProject.getTitle(), response.getTitle());
        
        verify(vrProjectRepository).findById(1L);
        verify(vrProjectRepository, never()).incrementViewCount(anyLong());
    }
    
    @Test
    void getProject_ShouldIncrementViewCount_WhenNonOwnerAccess() {
        // Given
        testProject.setStatus(1); // 已发布
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // When
        VrProjectResponse response = vrProjectService.getProject(1L, 2L);
        
        // Then
        assertNotNull(response);
        assertEquals(testProject.getId(), response.getId());
        
        verify(vrProjectRepository).findById(1L);
        verify(vrProjectRepository).incrementViewCount(1L);
    }
    
    @Test
    void getProject_ShouldThrowException_WhenUnauthorizedAccess() {
        // Given
        testProject.setStatus(0); // 草稿状态
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // When & Then
        assertThrows(UnauthorizedException.class, 
            () -> vrProjectService.getProject(1L, 2L));
        
        verify(vrProjectRepository).findById(1L);
    }
    
    @Test
    void getProjectByShareToken_ShouldReturnProject_WhenValidToken() {
        // Given
        testProject.setStatus(1); // 已发布
        when(vrProjectRepository.findByShareToken("test-token")).thenReturn(Optional.of(testProject));
        
        // When
        VrProjectResponse response = vrProjectService.getProjectByShareToken("test-token");
        
        // Then
        assertNotNull(response);
        assertEquals(testProject.getId(), response.getId());
        
        verify(vrProjectRepository).findByShareToken("test-token");
        verify(vrProjectRepository).incrementViewCount(1L);
    }
    
    @Test
    void getProjectByShareToken_ShouldThrowException_WhenTokenNotExists() {
        // Given
        when(vrProjectRepository.findByShareToken("invalid-token")).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> vrProjectService.getProjectByShareToken("invalid-token"));
        
        verify(vrProjectRepository).findByShareToken("invalid-token");
    }
    
    @Test
    void getProjectByShareToken_ShouldThrowException_WhenProjectNotPublished() {
        // Given
        testProject.setStatus(0); // 草稿状态
        when(vrProjectRepository.findByShareToken("test-token")).thenReturn(Optional.of(testProject));
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> vrProjectService.getProjectByShareToken("test-token"));
        
        verify(vrProjectRepository).findByShareToken("test-token");
    }
    
    @Test
    void deleteProject_ShouldDeleteProject_WhenValidRequest() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // When
        vrProjectService.deleteProject(1L, 1L);
        
        // Then
        verify(vrProjectRepository).findById(1L);
        verify(vrProjectRepository).delete(testProject);
    }
    
    @Test
    void deleteProject_ShouldThrowException_WhenUnauthorized() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // When & Then
        assertThrows(UnauthorizedException.class, 
            () -> vrProjectService.deleteProject(1L, 2L));
        
        verify(vrProjectRepository).findById(1L);
        verify(vrProjectRepository, never()).delete(any(VrProject.class));
    }
    
    @Test
    void getUserProjects_ShouldReturnProjects_WhenValidRequest() {
        // Given
        Page<VrProject> projectPage = new PageImpl<>(Arrays.asList(testProject));
        when(vrProjectRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(projectPage);
        
        // When
        Page<VrProjectListResponse> response = vrProjectService.getUserProjects(
            1L, null, null, 0, 10, "createdAt", "desc");
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(testProject.getId(), response.getContent().get(0).getId());
        
        verify(vrProjectRepository).findByUserId(eq(1L), any(Pageable.class));
    }
    
    @Test
    void publishProject_ShouldPublishProject_WhenValidRequest() {
        // Given
        Map<String, Object> sceneData = new HashMap<>();
        sceneData.put("type", "panorama");
        testProject.setSceneData(sceneData);
        
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(vrProjectRepository.save(any(VrProject.class))).thenReturn(testProject);
        
        // When
        VrProjectResponse response = vrProjectService.publishProject(1L, 1L);
        
        // Then
        assertNotNull(response);
        assertEquals(testProject.getId(), response.getId());
        
        verify(vrProjectRepository).findById(1L);
        verify(vrProjectRepository).save(any(VrProject.class));
    }
    
    @Test
    void publishProject_ShouldThrowException_WhenNoSceneData() {
        // Given
        testProject.setSceneData(null);
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> vrProjectService.publishProject(1L, 1L));
        
        verify(vrProjectRepository).findById(1L);
        verify(vrProjectRepository, never()).save(any(VrProject.class));
    }
    
    @Test
    void regenerateShareToken_ShouldGenerateNewToken_WhenValidRequest() {
        // Given
        when(vrProjectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(vrProjectRepository.existsByShareToken(anyString())).thenReturn(false);
        when(vrProjectRepository.save(any(VrProject.class))).thenReturn(testProject);
        
        // When
        VrProjectResponse response = vrProjectService.regenerateShareToken(1L, 1L);
        
        // Then
        assertNotNull(response);
        assertEquals(testProject.getId(), response.getId());
        
        verify(vrProjectRepository).findById(1L);
        verify(vrProjectRepository).save(any(VrProject.class));
    }
}