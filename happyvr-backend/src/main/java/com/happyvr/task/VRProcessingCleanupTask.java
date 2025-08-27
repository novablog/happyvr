package com.happyvr.task;

import com.happyvr.service.VRProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * VR处理清理定时任务
 */
@Component
public class VRProcessingCleanupTask {
    
    private static final Logger logger = LoggerFactory.getLogger(VRProcessingCleanupTask.class);
    
    @Autowired
    private VRProcessingService vrProcessingService;
    
    /**
     * 每小时清理一次过期任务
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void cleanupExpiredTasks() {
        try {
            logger.debug("开始清理过期的VR处理任务");
            vrProcessingService.cleanupExpiredTasks();
            logger.debug("VR处理任务清理完成");
        } catch (Exception e) {
            logger.error("清理VR处理任务时发生错误", e);
        }
    }
}