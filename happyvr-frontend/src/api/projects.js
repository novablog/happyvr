import request from '@/utils/request'

// 获取项目列表
export function getProjectList(params) {
  return request({
    url: '/projects',
    method: 'get',
    params
  })
}

// 获取项目详情
export function getProjectDetail(id) {
  return request({
    url: `/projects/${id}`,
    method: 'get'
  })
}

// 创建项目
export function createProject(data) {
  // 默认优先调用真实后端接口，这样项目会被持久化到数据库并返回真实 projectId。
  // 仅当在开发环境并且显式设置 VITE_USE_MOCK_PROJECTS=true 时才使用 mock 响应。
  if (import.meta.env.DEV && import.meta.env.VITE_USE_MOCK_PROJECTS === 'true') {
    const mockId = Math.floor(Math.random() * 1000) + 1
    return Promise.resolve({
      code: 200,
      success: true,
      data: {
        id: mockId,
        title: data.title,
        description: data.description,
        category: data.category,
        tags: data.tags,
        privacy: data.privacy,
        status: 'draft',
        shareUrl: `${window.location.origin}/share/${mockId}`,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
    })
  }

  return request({
    url: '/projects',
    method: 'post',
    data
  })
}

// 更新项目
export function updateProject(id, data) {
  // 默认调用真实后端接口以确保数据持久化。
  // 仅在开发环境并显式设置 VITE_USE_MOCK_PROJECTS=true 时使用 mock 响应。
  if (import.meta.env.DEV && import.meta.env.VITE_USE_MOCK_PROJECTS === 'true') {
    return Promise.resolve({
      code: 200,
      success: true,
      data: {
        id: id,
        ...data,
        updatedAt: new Date().toISOString()
      }
    })
  }

  return request({
    url: `/projects/${id}`,
    method: 'put',
    data
  })
}

// 删除项目
export function deleteProject(id) {
  return request({
    url: `/projects/${id}`,
    method: 'delete'
  })
}

// 发布项目
export function publishProject(id) {
  return request({
    url: `/projects/${id}/publish`,
    method: 'put'
  })
}

// 取消发布项目
export function unpublishProject(id) {
  return request({
    url: `/projects/${id}/unpublish`,
    method: 'put'
  })
}

// 复制项目
export function duplicateProject(id) {
  return request({
    url: `/projects/${id}/duplicate`,
    method: 'post'
  })
}

// 获取项目分享信息
export function getProjectShareInfo(id) {
  return request({
    url: `/projects/${id}/share`,
    method: 'get'
  })
}

// 生成分享链接
export function generateShareLink(id, data) {
  return request({
    url: `/projects/${id}/share`,
    method: 'post',
    data
  })
}

// 获取项目统计信息
export function getProjectStats(id) {
  return request({
    url: `/projects/${id}/stats`,
    method: 'get'
  })
}

// 获取用户项目统计
export function getUserProjectStats() {
  return request({
    url: '/projects/stats',
    method: 'get'
  })
}

// 搜索项目
export function searchProjects(params) {
  return request({
    url: '/projects/search',
    method: 'get',
    params
  })
}

// 获取推荐项目
export function getFeaturedProjects(params) {
  return request({
    url: '/projects/featured',
    method: 'get',
    params
  })
}

// 获取最新项目
export function getLatestProjects(params) {
  return request({
    url: '/projects/latest',
    method: 'get',
    params
  })
}

// 获取热门项目
export function getPopularProjects(params) {
  return request({
    url: '/projects/popular',
    method: 'get',
    params
  })
}