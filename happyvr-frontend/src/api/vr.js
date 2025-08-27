import request from '@/utils/request'

// VR生成相关API

// 开始VR生成
export function generateVR(data) {
  return request({
    url: '/vr/process',
    method: 'post',
    data
  })
}

// 获取VR生成进度
export function getVRGenerationProgress(taskId) {
  return request({
    url: `/vr/progress/${taskId}`,
    method: 'get'
  })
}

// 取消VR生成
export function cancelVRGeneration(taskId) {
  return request({
    url: `/vr/cancel/${taskId}`,
    method: 'post'
  })
}

// 重新生成VR
export function regenerateVR(projectId, data) {
  return request({
    url: `/vr/regenerate/${projectId}`,
    method: 'post',
    data
  })
}

// 热点管理相关API

// 获取项目热点列表
export function getHotspots(projectId) {
  return request({
    url: `/vr/projects/${projectId}/hotspots`,
    method: 'get'
  })
}

// 创建热点
export function createHotspot(projectId, data) {
  return request({
    url: `/vr/projects/${projectId}/hotspots`,
    method: 'post',
    data
  })
}

// 更新热点
export function updateHotspot(projectId, hotspotId, data) {
  return request({
    url: `/vr/projects/${projectId}/hotspots/${hotspotId}`,
    method: 'put',
    data
  })
}

// 删除热点
export function deleteHotspot(projectId, hotspotId) {
  return request({
    url: `/vr/projects/${projectId}/hotspots/${hotspotId}`,
    method: 'delete'
  })
}

// 批量创建热点
export function batchCreateHotspots(projectId, data) {
  return request({
    url: `/vr/projects/${projectId}/hotspots/batch`,
    method: 'post',
    data
  })
}

// 批量更新热点
export function batchUpdateHotspots(projectId, data) {
  return request({
    url: `/vr/projects/${projectId}/hotspots/batch`,
    method: 'put',
    data
  })
}

// 批量删除热点
export function batchDeleteHotspots(projectId, hotspotIds) {
  return request({
    url: `/vr/projects/${projectId}/hotspots/batch`,
    method: 'delete',
    data: { hotspotIds }
  })
}

// VR场景配置相关API

// 获取VR场景配置
export function getVRSceneConfig(projectId) {
  return request({
    url: `/vr/projects/${projectId}/scene-config`,
    method: 'get'
  })
}

// 更新VR场景配置
export function updateVRSceneConfig(projectId, data) {
  return request({
    url: `/vr/projects/${projectId}/scene-config`,
    method: 'put',
    data
  })
}

// 重置VR场景配置
export function resetVRSceneConfig(projectId) {
  return request({
    url: `/vr/projects/${projectId}/scene-config/reset`,
    method: 'post'
  })
}

// VR预览相关API

// 获取VR预览数据
export function getVRPreviewData(shareToken) {
  return request({
    url: `/vr/preview/${shareToken}`,
    method: 'get'
  })
}

// 记录VR预览访问
export function recordVRView(shareToken, data) {
  return request({
    url: `/vr/preview/${shareToken}/view`,
    method: 'post',
    data
  })
}

// 获取VR预览统计
export function getVRPreviewStats(projectId) {
  return request({
    url: `/vr/projects/${projectId}/preview-stats`,
    method: 'get'
  })
}

// VR模板相关API

// 获取VR模板列表
export function getVRTemplates(params) {
  return request({
    url: '/vr/templates',
    method: 'get',
    params
  })
}

// 获取VR模板详情
export function getVRTemplateDetail(templateId) {
  return request({
    url: `/vr/templates/${templateId}`,
    method: 'get'
  })
}

// 应用VR模板
export function applyVRTemplate(projectId, templateId) {
  return request({
    url: `/vr/projects/${projectId}/apply-template/${templateId}`,
    method: 'post'
  })
}

// VR导出相关API

// 导出VR项目
export function exportVRProject(projectId, format) {
  return request({
    url: `/vr/projects/${projectId}/export`,
    method: 'post',
    data: { format },
    responseType: 'blob'
  })
}

// 获取导出进度
export function getExportProgress(taskId) {
  return request({
    url: `/vr/export/progress/${taskId}`,
    method: 'get'
  })
}