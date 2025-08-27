import request from '@/utils/request'

// 获取统计数据
export function getStatistics() {
  return request({
    url: '/api/v1/admin/statistics',
    method: 'get'
  })
}

// 获取项目列表
export function getProjectList(params) {
  return request({
    url: '/api/v1/admin/projects',
    method: 'get',
    params
  })
}

// 审核项目
export function auditProject(id, data) {
  return request({
    url: `/api/v1/admin/projects/${id}/audit`,
    method: 'put',
    data
  })
}

// 批量审核项目
export function batchAuditProjects(data) {
  return request({
    url: '/api/v1/admin/projects/batch-audit',
    method: 'put',
    data
  })
}

// 设置项目推荐状态
export function setProjectFeatured(id, featured) {
  return request({
    url: `/api/v1/admin/projects/${id}/featured`,
    method: 'put',
    data: { featured }
  })
}

// 批量设置推荐
export function batchSetFeatured(data) {
  return request({
    url: '/api/v1/admin/projects/batch-featured',
    method: 'put',
    data
  })
}

// 更新项目信息
export function updateProject(id, data) {
  return request({
    url: `/api/v1/admin/projects/${id}`,
    method: 'put',
    data
  })
}

// 删除项目
export function deleteProject(id) {
  return request({
    url: `/api/v1/admin/projects/${id}`,
    method: 'delete'
  })
}

// 批量删除项目
export function batchDeleteProjects(data) {
  return request({
    url: '/api/v1/admin/projects/batch-delete',
    method: 'delete',
    data
  })
}

// 获取项目详情
export function getProjectDetail(id) {
  return request({
    url: `/api/v1/admin/projects/${id}`,
    method: 'get'
  })
}

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/api/v1/admin/users',
    method: 'get',
    params
  })
}

// 获取角色列表
export function getRoleList(params) {
  return request({
    url: '/api/v1/admin/roles',
    method: 'get',
    params
  })
}

// 创建角色
export function createRole(data) {
  return request({
    url: '/api/v1/admin/roles',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(id, data) {
  return request({
    url: `/api/v1/admin/roles/${id}`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/api/v1/admin/roles/${id}`,
    method: 'delete'
  })
}

// 获取系统配置
export function getSystemConfig() {
  return request({
    url: '/api/v1/admin/system/config',
    method: 'get'
  })
}

// 更新系统配置
export function updateSystemConfig(data) {
  return request({
    url: '/api/v1/admin/system/config',
    method: 'put',
    data
  })
}

// 获取操作日志
export function getOperationLogs(params) {
  return request({
    url: '/api/v1/admin/system/logs',
    method: 'get',
    params
  })
}

// 获取系统状态
export function getSystemStatus() {
  return request({
    url: '/api/v1/admin/system/status',
    method: 'get'
  })
}

// 切换维护模式
export function toggleMaintenanceMode(enabled) {
  return request({
    url: '/api/v1/admin/system/maintenance',
    method: 'put',
    data: { enabled }
  })
}

// 获取备份列表
export function getBackupList() {
  return request({
    url: '/api/v1/admin/system/backups',
    method: 'get'
  })
}

// 创建备份
export function createBackup() {
  return request({
    url: '/api/v1/admin/system/backups',
    method: 'post'
  })
}

// 删除备份
export function deleteBackup(id) {
  return request({
    url: `/api/v1/admin/system/backups/${id}`,
    method: 'delete'
  })
}

// 恢复备份
export function restoreBackup(id) {
  return request({
    url: `/api/v1/admin/system/backups/${id}/restore`,
    method: 'post'
  })
}

// 导出日志
export function exportLogs(params) {
  return request({
    url: '/api/v1/admin/system/logs/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}