// API配置文件

// API基础配置
export const API_CONFIG = {
  // 基础URL
  BASE_URL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  
  // 超时时间
  TIMEOUT: 10000,
  
  // 重试次数
  RETRY_COUNT: 3,
  
  // 重试延迟（毫秒）
  RETRY_DELAY: 1000,
  
  // 是否启用请求缓存
  ENABLE_CACHE: true,
  
  // 缓存过期时间（毫秒）
  CACHE_EXPIRE: 5 * 60 * 1000, // 5分钟
  
  // 是否启用请求去重
  ENABLE_DEDUPE: true,
  
  // 是否启用请求日志
  ENABLE_LOG: import.meta.env.DEV
}

// API端点配置
export const API_ENDPOINTS = {
  // 认证相关
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    ME: '/auth/me'
  },
  
  // 项目相关
  PROJECTS: {
    LIST: '/projects',
    DETAIL: '/projects/:id',
    CREATE: '/projects',
    UPDATE: '/projects/:id',
    DELETE: '/projects/:id',
    PUBLISH: '/projects/:id/publish',
    SHARE: '/projects/:id/share'
  },
  
  // 文件上传
  UPLOAD: {
    IMAGES: '/upload/images',
    IMAGE: '/upload/image',
    MEDIA: '/upload/media',
    PROGRESS: '/upload/progress/:id'
  },
  
  // VR相关
  VR: {
    GENERATE: '/vr/generate',
    PROGRESS: '/vr/generate/progress/:id',
    HOTSPOTS: '/vr/projects/:projectId/hotspots',
    SCENE_CONFIG: '/vr/projects/:projectId/scene-config',
    PREVIEW: '/vr/preview/:token'
  },
  
  // 用户相关
  USER: {
    INFO: '/user/info',
    SETTINGS: '/user/settings',
    STATS: '/user/stats',
    ACTIVITIES: '/user/activities',
    NOTIFICATIONS: '/user/notifications'
  },
  
  // 管理端
  ADMIN: {
    USERS: '/admin/users',
    ROLES: '/admin/roles',
    PROJECTS: '/admin/projects',
    SYSTEM: '/admin/system',
    LOGS: '/admin/system/logs'
  },
  
  // 通用接口
  COMMON: {
    CONFIG: '/common/config',
    NOTICES: '/common/notices',
    TAGS: '/common/tags',
    CATEGORIES: '/common/categories',
    FEEDBACK: '/common/feedback'
  }
}

// HTTP状态码配置
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  METHOD_NOT_ALLOWED: 405,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  TOO_MANY_REQUESTS: 429,
  INTERNAL_SERVER_ERROR: 500,
  BAD_GATEWAY: 502,
  SERVICE_UNAVAILABLE: 503,
  GATEWAY_TIMEOUT: 504
}

// 错误码配置
export const ERROR_CODES = {
  // 通用错误
  UNKNOWN_ERROR: 'UNKNOWN_ERROR',
  NETWORK_ERROR: 'NETWORK_ERROR',
  TIMEOUT_ERROR: 'TIMEOUT_ERROR',
  
  // 认证错误
  AUTH_FAILED: 'AUTH_FAILED',
  TOKEN_EXPIRED: 'TOKEN_EXPIRED',
  TOKEN_INVALID: 'TOKEN_INVALID',
  PERMISSION_DENIED: 'PERMISSION_DENIED',
  
  // 业务错误
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  RESOURCE_NOT_FOUND: 'RESOURCE_NOT_FOUND',
  RESOURCE_CONFLICT: 'RESOURCE_CONFLICT',
  OPERATION_FAILED: 'OPERATION_FAILED',
  
  // 文件错误
  FILE_TOO_LARGE: 'FILE_TOO_LARGE',
  FILE_TYPE_NOT_SUPPORTED: 'FILE_TYPE_NOT_SUPPORTED',
  UPLOAD_FAILED: 'UPLOAD_FAILED',
  
  // VR处理错误
  VR_GENERATION_FAILED: 'VR_GENERATION_FAILED',
  VR_PROCESSING_TIMEOUT: 'VR_PROCESSING_TIMEOUT',
  INVALID_IMAGE_FORMAT: 'INVALID_IMAGE_FORMAT'
}

// 请求头配置
export const REQUEST_HEADERS = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
  'X-Requested-With': 'XMLHttpRequest'
}

// 文件类型配置
export const FILE_TYPES = {
  IMAGES: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'],
  VIDEOS: ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm'],
  AUDIOS: ['mp3', 'wav', 'ogg', 'aac', 'flac'],
  DOCUMENTS: ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']
}

// 文件大小限制配置（字节）
export const FILE_SIZE_LIMITS = {
  IMAGE: 10 * 1024 * 1024, // 10MB
  VIDEO: 100 * 1024 * 1024, // 100MB
  AUDIO: 20 * 1024 * 1024, // 20MB
  DOCUMENT: 50 * 1024 * 1024 // 50MB
}

// 分页配置
export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_SIZE: 20,
  MAX_SIZE: 100,
  SIZE_OPTIONS: [10, 20, 50, 100]
}

// 缓存键配置
export const CACHE_KEYS = {
  USER_INFO: 'user_info',
  USER_SETTINGS: 'user_settings',
  SYSTEM_CONFIG: 'system_config',
  CATEGORIES: 'categories',
  TAGS: 'tags'
}