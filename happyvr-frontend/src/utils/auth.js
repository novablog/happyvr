/**
 * 认证相关工具函数
 */

// Token管理
export const tokenManager = {
  // 获取token
  getToken() {
    return localStorage.getItem('token')
  },
  
  // 设置token
  setToken(token) {
    localStorage.setItem('token', token)
  },
  
  // 移除token
  removeToken() {
    localStorage.removeItem('token')
  },
  
  // 检查token是否存在
  hasToken() {
    return !!this.getToken()
  }
}

// 用户信息管理
export const userManager = {
  // 获取用户信息
  getUserInfo() {
    const userInfo = localStorage.getItem('userInfo')
    return userInfo ? JSON.parse(userInfo) : null
  },
  
  // 设置用户信息
  setUserInfo(userInfo) {
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  },
  
  // 移除用户信息
  removeUserInfo() {
    localStorage.removeItem('userInfo')
  },
  
  // 检查是否为管理员
  isAdmin() {
    const userInfo = this.getUserInfo()
    return userInfo?.roles?.includes('ADMIN') || false
  }
}

// 登出函数
export const logout = () => {
  tokenManager.removeToken()
  userManager.removeUserInfo()
  window.location.href = '/login'
}