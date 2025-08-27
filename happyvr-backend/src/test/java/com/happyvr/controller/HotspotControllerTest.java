package com.happyvr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.happyvr.dto.hotspot.HotspotCreateRequest;
import com.happyvr.dto.hotspot.HotspotResponse;
import com.happyvr.dto.hotspot.HotspotUpdateRequest;
import com.happyvr.entity.Hotspot;
import com.happyvr.security.UserPrincipal;
import com.happyvr.service.HotspotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 热点控制器集成测试
 */
@WebMvcTest(HotspotController.class)
class HotspotControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private HotspotService hotspotService;
    
    private UserPrincipal userPrincipal;
    private HotspotResponse hotspotResponse;
    private HotspotCreateRequest createRequest;
    private HotspotUpdateRequest updateRequest;
    
    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "testuser", "test@example.com", "password", Arrays.asList(), true);
        
        hotspotResponse = new HotspotResponse();
        hotspotResponse.setId(1L);
        hotspotResponse.setProjectId(1L);
        hotspotResponse.setName("测试热点");
        hotspotResponse.setPositionX(new BigDecimal("10.5"));
        hotspotResponse.setPositionY(new BigDecimal("20.3"));
        hotspotResponse.setPositionZ(new BigDecimal("30.7"));
        hotspotResponse.setType(Hotspot.HotspotType.INFO);
        hotspotResponse.setTypeDescription("信息弹框");
        hotspotResponse.setContent("{\"title\":\"测试标题\"}");
        hotspotResponse.setStyle("{\"color\":\"#ff0000\"}");
        hotspotResponse.setCreatedAt(LocalDateTime.now());
        
        createRequest = new HotspotCreateRequest();
        createRequest.setName("新热点");
        createRequest.setPositionX(new BigDecimal("15.5"));
        createRequest.setPositionY(new BigDecimal("25.3"));
        createRequest.setPositionZ(new BigDecimal("35.7"));
        createRequest.setType(Hotspot.HotspotType.INFO);
        createRequest.setContent("{\"title\":\"新标题\"}");
        createRequest.setStyle("{\"color\":\"#00ff00\"}");
        
        updateRequest = new HotspotUpdateRequest();
        updateRequest.setName("更新的热点");
        updateRequest.setPositionX(new BigDecimal("12.5"));
    }
    
    @Test
    @WithMockUser
    void createHotspot_ShouldReturnSuccess_WhenValidRequest() throws Exception {
        // Given
        when(hotspotService.createHotspot(eq(1L), any(HotspotCreateRequest.class), eq(1L)))
            .thenReturn(hotspotResponse);
        
        // When & Then
        mockMvc.perform(post("/api/v1/projects/1/hotspots")
                .with(user(userPrincipal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("热点创建成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试热点"))
                .andExpect(jsonPath("$.data.type").value("INFO"))
                .andExpect(jsonPath("$.data.typeDescription").value("信息弹框"));
    }
    
    @Test
    @WithMockUser
    void createHotspot_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given - 创建无效请求（缺少必要字段）
        HotspotCreateRequest invalidRequest = new HotspotCreateRequest();
        // 不设置必要字段
        
        // When & Then
        mockMvc.perform(post("/api/v1/projects/1/hotspots")
                .with(user(userPrincipal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    void updateHotspot_ShouldReturnSuccess_WhenValidRequest() throws Exception {
        // Given
        when(hotspotService.updateHotspot(eq(1L), eq(1L), any(HotspotUpdateRequest.class), eq(1L)))
            .thenReturn(hotspotResponse);
        
        // When & Then
        mockMvc.perform(put("/api/v1/projects/1/hotspots/1")
                .with(user(userPrincipal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("热点更新成功"))
                .andExpect(jsonPath("$.data.id").value(1));
    }
    
    @Test
    @WithMockUser
    void deleteHotspot_ShouldReturnSuccess_WhenValidRequest() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/projects/1/hotspots/1")
                .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("热点删除成功"));
    }
    
    @Test
    @WithMockUser
    void getProjectHotspots_ShouldReturnHotspotList_WhenValidRequest() throws Exception {
        // Given
        List<HotspotResponse> hotspots = Arrays.asList(hotspotResponse);
        when(hotspotService.getProjectHotspots(eq(1L), eq(1L))).thenReturn(hotspots);
        
        // When & Then
        mockMvc.perform(get("/api/v1/projects/1/hotspots")
                .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试热点"));
    }
    
    @Test
    @WithMockUser
    void getHotspot_ShouldReturnHotspot_WhenValidRequest() throws Exception {
        // Given
        when(hotspotService.getHotspot(eq(1L), eq(1L), eq(1L))).thenReturn(hotspotResponse);
        
        // When & Then
        mockMvc.perform(get("/api/v1/projects/1/hotspots/1")
                .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试热点"))
                .andExpect(jsonPath("$.data.positionX").value(10.5))
                .andExpect(jsonPath("$.data.positionY").value(20.3))
                .andExpect(jsonPath("$.data.positionZ").value(30.7));
    }
    
    @Test
    @WithMockUser
    void getHotspotStatistics_ShouldReturnStatistics_WhenValidRequest() throws Exception {
        // Given
        Map<String, Object> statistics = Map.of(
            "totalCount", 5L,
            "typeStatistics", Map.of(
                "信息弹框", 3L,
                "媒体内容", 2L
            )
        );
        when(hotspotService.getHotspotStatistics(eq(1L), eq(1L))).thenReturn(statistics);
        
        // When & Then
        mockMvc.perform(get("/api/v1/projects/1/hotspots/statistics")
                .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(5))
                .andExpect(jsonPath("$.data.typeStatistics.信息弹框").value(3))
                .andExpect(jsonPath("$.data.typeStatistics.媒体内容").value(2));
    }
    
    @Test
    void createHotspot_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/projects/1/hotspots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }
}