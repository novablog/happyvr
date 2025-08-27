// API模块统一导出

// 认证相关API
export * as authAPI from './auth'

// 项目相关API
export * as projectsAPI from './projects'

// 文件上传API
export * as uploadAPI from './upload'

// VR相关API
export * as vrAPI from './vr'

// 用户相关API
export * as userAPI from './user'

// 管理端API
export * as adminAPI from './admin'

// 通用API
export * as commonAPI from './common'

// 默认导出所有API
export default {
  auth: () => import('./auth'),
  projects: () => import('./projects'),
  upload: () => import('./upload'),
  vr: () => import('./vr'),
  user: () => import('./user'),
  admin: () => import('./admin'),
  common: () => import('./common')
}