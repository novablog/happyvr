import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import authService from '@/services/authService'

export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref({
    id: null,
    username: '',
    email: '',
    avatar: '',
    roles: []
  })

  const token = ref(localStorage.getItem('token') || sessionStorage.getItem('token') || '')
  const isLoggedIn = computed(() => !!token.value)

  // 登录
  const login = async (credentials) => {
    try {
      const response = await authService.login(credentials)
      console.log('登录响应:', response)

      // response.data 就是 JwtResponse 对象（经过request拦截器处理）
      const jwtResponse = response.data

      // 从JwtResponse中提取token和用户信息
      const newToken = jwtResponse.token
      const user = {
        id: jwtResponse.id,
        username: jwtResponse.username,
        email: jwtResponse.email,
        avatar: '', // 默认头像
        roles: jwtResponse.roles || []
      }

      console.log('提取的token:', newToken)
      console.log('提取的用户信息:', user)

      // 保存token和用户信息
      token.value = newToken
      userInfo.value = user

      // 根据记住我选项决定存储方式
      if (credentials.rememberMe) {
        localStorage.setItem('token', newToken)
      } else {
        sessionStorage.setItem('token', newToken)
      }

      return { success: true }
    } catch (error) {
      const message = error.response?.data?.message || '登录失败'
      ElMessage.error(message)
      return { success: false, message }
    }
  }

  // 注册
  const register = async (userData) => {
    try {
      const response = await authService.register(userData)
      return { success: true, data: response.data }
    } catch (error) {
      const message = error.response?.data?.message || '注册失败'
      ElMessage.error(message)
      return { success: false, message }
    }
  }

  // 退出登录
  const logout = async () => {
    try {
      await authService.logout()
    } catch (error) {
      console.warn('退出登录请求失败:', error)
    } finally {
      // 清除本地数据
      token.value = ''
      userInfo.value = {
        id: null,
        username: '',
        email: '',
        avatar: '',
        roles: []
      }
      localStorage.removeItem('token')
      sessionStorage.removeItem('token')
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    if (!token.value) return

    try {
      const response = await authService.getCurrentUser()
      console.log('获取用户信息响应:', response)

      // 根据后端返回的数据结构调整
      const userData = response.data
      userInfo.value = {
        id: userData.id,
        username: userData.username,
        email: userData.email,
        avatar: userData.avatar || '', // 确保avatar有默认值
        roles: userData.roles || []
      }

      console.log('用户信息已更新:', userInfo.value)
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果token无效，清除登录状态
      if (error.response?.status === 401) {
        await logout()
        throw error
      }
    }
  }

  // 更新用户信息
  const updateUserInfo = async (updateData) => {
    try {
      const response = await authService.updateProfile(updateData)
      userInfo.value = { ...userInfo.value, ...response.data }
      ElMessage.success('个人信息更新成功')
      return { success: true }
    } catch (error) {
      const message = error.response?.data?.message || '更新失败'
      ElMessage.error(message)
      return { success: false, message }
    }
  }

  // 修改密码
  const changePassword = async (passwordData) => {
    try {
      await authService.changePassword(passwordData)
      ElMessage.success('密码修改成功')
      return { success: true }
    } catch (error) {
      const message = error.response?.data?.message || '密码修改失败'
      ElMessage.error(message)
      return { success: false, message }
    }
  }

  // 检查权限
  const hasPermission = (permission) => {
    if (!userInfo.value.roles || userInfo.value.roles.length === 0) {
      return false
    }

    return userInfo.value.roles.some(role =>
      role.permissions && role.permissions.includes(permission)
    )
  }

  // 检查角色
  const hasRole = (roleName) => {
    if (!userInfo.value.roles || userInfo.value.roles.length === 0) {
      return false
    }

    return userInfo.value.roles.some(role => role.name === roleName)
  }

  // 是否为管理员
  const isAdmin = computed(() => hasRole('ADMIN'))

  return {
    // 状态
    userInfo,
    token,
    isLoggedIn,
    isAdmin,

    // 方法
    login,
    register,
    logout,
    fetchUserInfo,
    updateUserInfo,
    changePassword,
    hasPermission,
    hasRole
  }
})