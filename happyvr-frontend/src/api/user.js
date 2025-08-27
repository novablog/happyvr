import request from '@/utils/request'

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/api/v1/user/info',
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/api/v1/user/info',
    method: 'put',
    data
  })
}

// 获取用户设置
export function getUserSettings() {
  return request({
    url: '/api/v1/user/settings',
    method: 'get'
  })
}

// 更新用户设置
export function updateUserSettings(data) {
  return request({
    url: '/api/v1/user/settings',
    method: 'put',
    data
  })
}

// 获取用户统计信息
export function getUserStats() {
  return request({
    url: '/api/v1/user/stats',
    method: 'get'
  })
}

// 获取用户活动记录
export function getUserActivities(params) {
  return request({
    url: '/api/v1/user/activities',
    method: 'get',
    params
  })
}

// 获取用户通知
export function getUserNotifications(params) {
  return request({
    url: '/api/v1/user/notifications',
    method: 'get',
    params
  })
}

// 标记通知为已读
export function markNotificationAsRead(notificationId) {
  return request({
    url: `/api/v1/user/notifications/${notificationId}/read`,
    method: 'put'
  })
}

// 批量标记通知为已读
export function batchMarkNotificationsAsRead(notificationIds) {
  return request({
    url: '/api/v1/user/notifications/batch-read',
    method: 'put',
    data: { notificationIds }
  })
}

// 删除通知
export function deleteNotification(notificationId) {
  return request({
    url: `/api/v1/user/notifications/${notificationId}`,
    method: 'delete'
  })
}

// 获取用户收藏
export function getUserFavorites(params) {
  return request({
    url: '/api/v1/user/favorites',
    method: 'get',
    params
  })
}

// 添加收藏
export function addToFavorites(projectId) {
  return request({
    url: '/api/v1/user/favorites',
    method: 'post',
    data: { projectId }
  })
}

// 取消收藏
export function removeFromFavorites(projectId) {
  return request({
    url: `/api/v1/user/favorites/${projectId}`,
    method: 'delete'
  })
}

// 检查是否已收藏
export function checkIsFavorited(projectId) {
  return request({
    url: `/api/v1/user/favorites/check/${projectId}`,
    method: 'get'
  })
}

// 获取用户关注列表
export function getUserFollowing(params) {
  return request({
    url: '/api/v1/user/following',
    method: 'get',
    params
  })
}

// 获取用户粉丝列表
export function getUserFollowers(params) {
  return request({
    url: '/api/v1/user/followers',
    method: 'get',
    params
  })
}

// 关注用户
export function followUser(userId) {
  return request({
    url: '/api/v1/user/follow',
    method: 'post',
    data: { userId }
  })
}

// 取消关注用户
export function unfollowUser(userId) {
  return request({
    url: `/api/v1/user/unfollow/${userId}`,
    method: 'delete'
  })
}

// 检查是否已关注
export function checkIsFollowing(userId) {
  return request({
    url: `/api/v1/user/following/check/${userId}`,
    method: 'get'
  })
}

// 搜索用户
export function searchUsers(params) {
  return request({
    url: '/api/v1/user/search',
    method: 'get',
    params
  })
}

// 获取用户公开信息
export function getUserPublicInfo(userId) {
  return request({
    url: `/api/v1/user/public/${userId}`,
    method: 'get'
  })
}

// 举报用户
export function reportUser(data) {
  return request({
    url: '/api/v1/user/report',
    method: 'post',
    data
  })
}

// 屏蔽用户
export function blockUser(userId) {
  return request({
    url: '/api/v1/user/block',
    method: 'post',
    data: { userId }
  })
}

// 取消屏蔽用户
export function unblockUser(userId) {
  return request({
    url: `/api/v1/user/unblock/${userId}`,
    method: 'delete'
  })
}

// 获取屏蔽列表
export function getBlockedUsers(params) {
  return request({
    url: '/api/v1/user/blocked',
    method: 'get',
    params
  })
}