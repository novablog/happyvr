-- 创建热点表
CREATE TABLE hotspots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    position_x DECIMAL(10,6) NOT NULL,
    position_y DECIMAL(10,6) NOT NULL,
    position_z DECIMAL(10,6) NOT NULL,
    type ENUM('INFO', 'MEDIA', 'LINK', 'AUDIO') NOT NULL,
    content JSON,
    style JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES vr_projects(id) ON DELETE CASCADE,
    INDEX idx_project_id (project_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
);

-- 添加热点名称唯一性约束（在同一项目内）
ALTER TABLE hotspots ADD CONSTRAINT uk_project_hotspot_name UNIQUE (project_id, name);