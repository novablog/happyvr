-- HappyVR平台初始数据插入脚本

-- 插入默认角色
INSERT INTO roles (name, description, permissions) VALUES 
('USER', '普通用户', JSON_ARRAY('project:create', 'project:edit', 'project:delete', 'project:view')),
('ADMIN', '系统管理员', JSON_ARRAY('*')),
('MODERATOR', '内容审核员', JSON_ARRAY('project:view', 'project:audit', 'user:view'));

-- 插入默认管理员用户（密码：admin123，需要在应用中加密）
INSERT INTO users (username, email, password, status) VALUES 
('admin', 'admin@happyvr.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P2.nRuvnfVyFzu', 1);

-- 为管理员分配角色
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 2); -- admin用户分配ADMIN角色

-- 插入系统默认配置
INSERT INTO system_configs (config_key, config_value, description) VALUES 
('max_file_size', '52428800', '最大文件上传大小（50MB）'),
('max_files_per_project', '10', '每个项目最大图片数量'),
('vr_processing_timeout', '300', 'VR处理超时时间（秒）'),
('default_vr_quality', 'high', '默认VR生成质量'),
('enable_user_registration', 'true', '是否允许用户注册'),
('site_title', 'HappyVR', '网站标题'),
('site_description', 'VR在线编辑和预览平台', '网站描述'),
('contact_email', 'support@happyvr.com', '联系邮箱'),
('maintenance_mode', 'false', '维护模式开关'),
('cdn_base_url', '', 'CDN基础URL');

-- 插入示例VR项目（可选）
INSERT INTO vr_projects (user_id, title, description, status, share_token) VALUES 
(1, '示例VR全景', '这是一个示例VR全景项目，展示平台功能', 1, MD5(CONCAT('demo_project_', UNIX_TIMESTAMP())));

-- 插入示例热点数据
INSERT INTO hotspots (project_id, name, position_x, position_y, position_z, type, content, style) VALUES 
(1, '信息热点', 0.0, 0.0, -100.0, 'info', 
 JSON_OBJECT('title', '欢迎使用HappyVR', 'description', '这是一个信息热点示例', 'image', ''),
 JSON_OBJECT('color', '#d4af37', 'size', 'medium', 'animation', 'pulse'));

-- 记录初始化操作日志
INSERT INTO operation_logs (user_id, operation, resource_type, details, ip_address) VALUES 
(1, 'SYSTEM_INIT', 'SYSTEM', JSON_OBJECT('action', 'database_initialization', 'version', '1.0.0'), '127.0.0.1');