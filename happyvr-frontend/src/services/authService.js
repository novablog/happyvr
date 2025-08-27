/**
 * 认证相关API服务
 */
import request from '@/utils/request'

export const authService = {
  /**
   * 用户登录
   * @param {Object} credentials - 登录凭据
   * @param {string} credentials.username - 用户名或邮箱
   * @param {string} credentials.password - 密码
   * @param {boolean} credentials.rememberMe - 是否记住我
   * @returns {Promise} API响应
   */
  login(credentials) {
    return request.post('/auth/login', credentials)
  },

  /**
   * 用户注册
   * @param {Object} userData - 用户数据
   * @param {string} userData.username - 用户名
   * @param {string} userData.email - 邮箱
   * @param {string} userData.password - 密码
   * @param {string} userData.phone - 手机号（可选）
   * @param {string} userData.captcha - 验证码（可选）
   * @returns {Promise} API响应
   */
  register(userData) {
    return request.post('/auth/register', userData)
  },

  /**
   * 用户登出
   * @returns {Promise} API响应
   */
  logout() {
    return request.post('/auth/logout')
  },

  /**
   * 获取当前用户信息
   * @returns {Promise} API响应
   */
  getCurrentUser() {
    return request.get('/auth/me')
  },

  /**
   * 更新用户资料
   * @param {Object} profileData - 用户资料数据
   * @returns {Promise} API响应
   */
  updateProfile(profileData) {
    return request.put('/auth/profile', profileData)
  },

  /**
   * 修改密码
   * @param {Object} passwordData - 密码数据
   * @param {string} passwordData.oldPassword - 旧密码
   * @param {string} passwordData.newPassword - 新密码
   * @returns {Promise} API响应
   */
  changePassword(passwordData) {
    return request.put('/auth/password', passwordData)
  },

  /**
   * 发送验证码
   * @param {string} email - 邮箱地址
   * @returns {Promise} API响应
   */
  sendCaptcha(email) {
    return request.post('/auth/send-captcha', { email })
  },

  /**
   * 发送重置密码邮件
   * @param {string} email - 邮箱地址
   * @returns {Promise} API响应
   */
  sendResetPasswordEmail(email) {
    return request.post('/auth/forgot-password', { email })
  },

  /**
   * 重置密码
   * @param {Object} resetData - 重置数据
   * @param {string} resetData.token - 重置令牌
   * @param {string} resetData.password - 新密码
   * @returns {Promise} API响应
   */
  resetPassword(resetData) {
    return request.post('/auth/reset-password', resetData)
  },

  /**
   * 验证用户名是否可用
   * @param {string} username - 用户名
   * @returns {Promise} API响应
   */
  checkUsernameAvailability(username) {
    return request.get(`/auth/check-username/${encodeURIComponent(username)}`)
  },

  /**
   * 验证邮箱是否可用
   * @param {string} email - 邮箱地址
   * @returns {Promise} API响应
   */
  checkEmailAvailability(email) {
    return request.get(`/auth/check-email/${encodeURIComponent(email)}`)
  },

  /**
   * 刷新访问令牌
   * @returns {Promise} API响应
   */
  refreshToken() {
    return request.post('/auth/refresh-token')
  },

  /**
   * 验证令牌有效性
   * @param {string} token - 访问令牌
   * @returns {Promise} API响应
   */
  validateToken(token) {
    return request.post('/auth/validate-token', { token })
  }
}

export default authService