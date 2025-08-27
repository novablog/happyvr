import request from '@/utils/request'

// 用户注册
export function register(data) {
  return request({
    url: '/api/v1/auth/register',
    method: 'post',
    data
  })
}

// 用户登录
export function login(data) {
  return request({
    url: '/api/v1/auth/login',
    method: 'post',
    data
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/api/v1/auth/logout',
    method: 'post'
  })
}

// 刷新token
export function refreshToken() {
  return request({
    url: '/api/v1/auth/refresh',
    method: 'post'
  })
}

// 发送验证码
export function sendVerificationCode(data) {
  return request({
    url: '/api/v1/auth/send-code',
    method: 'post',
    data
  })
}

// 验证邮箱
export function verifyEmail(data) {
  return request({
    url: '/api/v1/auth/verify-email',
    method: 'post',
    data
  })
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: '/api/v1/auth/reset-password',
    method: 'post',
    data
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/api/v1/auth/change-password',
    method: 'put',
    data
  })
}

// 获取当前用户信息
export function getCurrentUser() {
  return request({
    url: '/api/v1/auth/me',
    method: 'get'
  })
}

// 更新用户信息
export function updateProfile(data) {
  return request({
    url: '/api/v1/auth/profile',
    method: 'put',
    data
  })
}

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('avatar', file)
  
  return request({
    url: '/api/v1/auth/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}