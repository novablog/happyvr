import request from '@/utils/request'

// 获取系统配置
export function getSystemConfig() {
  return request({
    url: '/common/config',
    method: 'get'
  })
}

// 获取系统公告
export function getSystemNotices() {
  return request({
    url: '/common/notices',
    method: 'get'
  })
}

// 获取帮助文档
export function getHelpDocs(params) {
  return request({
    url: '/common/help',
    method: 'get',
    params
  })
}

// 获取FAQ
export function getFAQ() {
  return request({
    url: '/common/faq',
    method: 'get'
  })
}

// 提交反馈
export function submitFeedback(data) {
  return request({
    url: '/common/feedback',
    method: 'post',
    data
  })
}

// 举报内容
export function reportContent(data) {
  return request({
    url: '/common/report',
    method: 'post',
    data
  })
}

// 获取标签列表
export function getTags(params) {
  // 临时mock数据，避免500错误
  if (import.meta.env.DEV) {
    return Promise.resolve({
      code: 200,
      success: true,
      data: [
        { id: 1, name: '风景', color: '#409eff' },
        { id: 2, name: '建筑', color: '#67c23a' },
        { id: 3, name: '室内', color: '#e6a23c' },
        { id: 4, name: '展览', color: '#f56c6c' },
        { id: 5, name: '旅游', color: '#909399' }
      ]
    })
  }

  return request({
    url: '/common/tags',
    method: 'get',
    params
  })
}

// 获取分类列表
export function getCategories() {
  // 临时mock数据，避免500错误
  if (import.meta.env.DEV) {
    return Promise.resolve({
      code: 200,
      success: true,
      data: [
        { id: 1, name: '自然风光', description: '自然景观VR体验' },
        { id: 2, name: '城市建筑', description: '城市和建筑VR展示' },
        { id: 3, name: '室内空间', description: '室内环境VR漫游' },
        { id: 4, name: '文化展览', description: '博物馆和展览VR参观' },
        { id: 5, name: '商业展示', description: '商业空间VR展示' }
      ]
    })
  }

  return request({
    url: '/common/categories',
    method: 'get'
  })
}

// 搜索建议
export function getSearchSuggestions(keyword) {
  return request({
    url: '/common/search/suggestions',
    method: 'get',
    params: { keyword }
  })
}

// 获取热门搜索
export function getHotSearches() {
  return request({
    url: '/common/search/hot',
    method: 'get'
  })
}

// 记录搜索
export function recordSearch(keyword) {
  return request({
    url: '/common/search/record',
    method: 'post',
    data: { keyword }
  })
}

// 获取地区列表
export function getRegions() {
  return request({
    url: '/common/regions',
    method: 'get'
  })
}

// 获取语言列表
export function getLanguages() {
  return request({
    url: '/common/languages',
    method: 'get'
  })
}

// 获取时区列表
export function getTimezones() {
  return request({
    url: '/common/timezones',
    method: 'get'
  })
}

// 发送邮件验证码
export function sendEmailCode(email) {
  return request({
    url: '/common/send-email-code',
    method: 'post',
    data: { email }
  })
}

// 发送短信验证码
export function sendSMSCode(phone) {
  return request({
    url: '/common/send-sms-code',
    method: 'post',
    data: { phone }
  })
}

// 验证验证码
export function verifyCode(data) {
  return request({
    url: '/common/verify-code',
    method: 'post',
    data
  })
}

// 获取文件访问URL
export function getFileUrl(fileId) {
  return request({
    url: `/common/files/${fileId}/url`,
    method: 'get'
  })
}

// 获取缩略图URL
export function getThumbnailUrl(fileId, size) {
  return request({
    url: `/common/files/${fileId}/thumbnail`,
    method: 'get',
    params: { size }
  })
}

// 检查文件是否存在
export function checkFileExists(fileId) {
  return request({
    url: `/common/files/${fileId}/exists`,
    method: 'get'
  })
}

// 获取统计数据
export function getPublicStats() {
  return request({
    url: '/common/stats',
    method: 'get'
  })
}

// 获取版本信息
export function getVersionInfo() {
  return request({
    url: '/common/version',
    method: 'get'
  })
}

// 健康检查
export function healthCheck() {
  return request({
    url: '/common/health',
    method: 'get'
  })
}