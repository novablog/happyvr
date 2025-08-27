package com.happyvr.controller;

import com.happyvr.dto.upload.FileUploadResponse;
import com.happyvr.dto.upload.UploadProgressResponse;
import com.happyvr.security.UserPrincipal;
import com.happyvr.service.FileUploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileUploadController.class)
class FileUploadControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FileUploadService fileUploadService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser
    void uploadImages_ShouldReturnSuccess_WhenValidFiles() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile("files", "test1.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.png", "image/png", "content2".getBytes());
        
        List<FileUploadResponse> responses = Arrays.asList(
            new FileUploadResponse("file1.jpg", "test1.jpg", "/files/user_1/file1.jpg", 100L, "image/jpeg", "upload-id"),
            new FileUploadResponse("file2.png", "test2.png", "/files/user_1/file2.png", 200L, "image/png", "upload-id")
        );
        
        UserPrincipal userPrincipal = new UserPrincipal(1L, "testuser", "test@example.com", "password", 
            Arrays.asList(), true);
        
        when(fileUploadService.uploadFiles(any(), eq(1L))).thenReturn(responses);
        
        // When & Then
        mockMvc.perform(multipart("/api/v1/upload/images")
                .file(file1)
                .file(file2)
                .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文件上传成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
    
    @Test
    @WithMockUser
    void uploadImage_ShouldReturnSuccess_WhenValidFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        
        FileUploadResponse response = new FileUploadResponse(
            "file.jpg", "test.jpg", "/files/user_1/file.jpg", 100L, "image/jpeg", "upload-id"
        );
        
        UserPrincipal userPrincipal = new UserPrincipal(1L, "testuser", "test@example.com", "password", 
            Arrays.asList(), true);
        
        when(fileUploadService.uploadSingleFile(any(), eq(1L), any())).thenReturn(response);
        
        // When & Then
        mockMvc.perform(multipart("/api/v1/upload/image")
                .file(file)
                .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文件上传成功"))
                .andExpect(jsonPath("$.data.fileName").value("file.jpg"))
                .andExpect(jsonPath("$.data.originalFileName").value("test.jpg"));
    }
    
    @Test
    @WithMockUser
    void getUploadProgress_ShouldReturnProgress_WhenValidUploadId() throws Exception {
        // Given
        String uploadId = "test-upload-id";
        UploadProgressResponse progress = new UploadProgressResponse(uploadId, "COMPLETED", 100, "上传完成");
        
        when(fileUploadService.getUploadProgress(uploadId)).thenReturn(progress);
        
        // When & Then
        mockMvc.perform(get("/api/v1/upload/progress/{uploadId}", uploadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.uploadId").value(uploadId))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.progress").value(100));
    }
    
    @Test
    @WithMockUser
    void deleteFile_ShouldReturnSuccess_WhenFileDeleted() throws Exception {
        // Given
        String filePath = "user_1/2024/01/test.jpg";
        UserPrincipal userPrincipal = new UserPrincipal(1L, "testuser", "test@example.com", "password", 
            Arrays.asList(), true);
        
        when(fileUploadService.deleteFile(filePath)).thenReturn(true);
        
        // When & Then
        mockMvc.perform(delete("/api/v1/upload/file")
                .param("filePath", filePath)
                .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文件删除成功"))
                .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    @WithMockUser
    void deleteFile_ShouldReturnError_WhenFileNotDeleted() throws Exception {
        // Given
        String filePath = "user_1/2024/01/test.jpg";
        UserPrincipal userPrincipal = new UserPrincipal(1L, "testuser", "test@example.com", "password", 
            Arrays.asList(), true);
        
        when(fileUploadService.deleteFile(filePath)).thenReturn(false);
        
        // When & Then
        mockMvc.perform(delete("/api/v1/upload/file")
                .param("filePath", filePath)
                .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文件删除失败"))
                .andExpect(jsonPath("$.data").value(false));
    }
}