import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { API_CONFIG, HTTP_STATUS, ERROR_CODES } from '@/api/config'

// 请求缓存
const requestCache = new Map()
// 正在进行的请求
const pendingRequests = new Map()
// 加载实例
let loadingInstance = null
// 加载计数器
let loadingCount = 0

// 创建axios实例
const request = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  }
})

// 生成请求唯一标识
const generateRequestKey = (config) => {
  const { method, url, params, data } = config
  return `${method}:${url}:${JSON.stringify(params)}:${JSON.stringify(data)}`
}

// 显示加载
const showLoading = (config) => {
  if (config.loading !== false) {
    loadingCount++
    if (loadingCount === 1) {
      loadingInstance = ElLoading.service({
        lock: true,
        text: config.loadingText || '加载中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    }
  }
}

// 隐藏加载
const hideLoading = () => {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
  }
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加认证token
    const token = localStorage.getItem('admin_token') || localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加请求ID
    config.requestId = Date.now() + Math.random()
    
    // 请求去重
    if (API_CONFIG.ENABLE_DEDUPE && config.method === 'get') {
      const requestKey = generateRequestKey(config)
      if (pendingRequests.has(requestKey)) {
        const cancelToken = axios.CancelToken.source()
        config.cancelToken = cancelToken.token
        cancelToken.cancel('重复请求')
        return config
      }
      pendingRequests.set(requestKey, config.requestId)
    }
    
    // 缓存检查
    if (API_CONFIG.ENABLE_CACHE && config.method === 'get' && config.cache !== false) {
      const cacheKey = generateRequestKey(config)
      const cached = requestCache.get(cacheKey)
      if (cached && Date.now() - cached.timestamp < API_CONFIG.CACHE_EXPIRE) {
        return Promise.resolve(cached.data)
      }
    }
    
    // 显示加载动画
    showLoading(config)
    
    // 请求日志
    if (API_CONFIG.ENABLE_LOG) {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, {
        params: config.params,
        data: config.data,
        headers: config.headers
      })
    }
    
    return config
  },
  error => {
    hideLoading()
    console.error('请求配置错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { config, data } = response
    
    // 隐藏加载动画
    hideLoading()
    
    // 清除请求记录
    if (API_CONFIG.ENABLE_DEDUPE && config.method === 'get') {
      const requestKey = generateRequestKey(config)
      pendingRequests.delete(requestKey)
    }
    
    // 响应日志
    if (API_CONFIG.ENABLE_LOG) {
      console.log(`[API Response] ${config.method?.toUpperCase()} ${config.url}`, {
        status: response.status,
        data: data
      })
    }
    
    // 如果是文件下载等特殊响应，直接返回
    if (config.responseType === 'blob') {
      return response
    }
    
    // 缓存响应数据
    if (API_CONFIG.ENABLE_CACHE && config.method === 'get' && config.cache !== false) {
      const cacheKey = generateRequestKey(config)
      requestCache.set(cacheKey, {
        data: data,
        timestamp: Date.now()
      })
    }
    
    // 统一处理响应格式
    if (data.code === 200 || data.success === true || response.status === HTTP_STATUS.OK) {
      return data
    } else {
      // 业务错误
      const message = data.message || '请求失败'
      if (config.showError !== false) {
        ElMessage.error(message)
      }
      return Promise.reject(new Error(message))
    }
  },
  error => {
    // 隐藏加载动画
    hideLoading()
    
    // 清除请求记录
    if (error.config && API_CONFIG.ENABLE_DEDUPE) {
      const requestKey = generateRequestKey(error.config)
      pendingRequests.delete(requestKey)
    }
    
    // 如果是取消的请求，不显示错误
    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }
    
    console.error('响应错误:', error)
    
    let message = '网络错误'
    let errorCode = ERROR_CODES.UNKNOWN_ERROR
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case HTTP_STATUS.UNAUTHORIZED:
          message = '登录已过期，请重新登录'
          errorCode = ERROR_CODES.TOKEN_EXPIRED
          // 清除token并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('admin_token')
          // 延迟跳转，避免重复跳转
          setTimeout(() => {
            if (window.location.pathname.startsWith('/admin')) {
              window.location.href = '/admin/login'
            } else {
              window.location.href = '/login'
            }
          }, 1000)
          break
        case HTTP_STATUS.FORBIDDEN:
          message = '权限不足'
          errorCode = ERROR_CODES.PERMISSION_DENIED
          break
        case HTTP_STATUS.NOT_FOUND:
          message = '请求的资源不存在'
          errorCode = ERROR_CODES.RESOURCE_NOT_FOUND
          break
        case HTTP_STATUS.UNPROCESSABLE_ENTITY:
          message = data?.message || '数据验证失败'
          errorCode = ERROR_CODES.VALIDATION_ERROR
          break
        case HTTP_STATUS.TOO_MANY_REQUESTS:
          message = '请求过于频繁，请稍后再试'
          errorCode = ERROR_CODES.TOO_MANY_REQUESTS
          break
        case HTTP_STATUS.INTERNAL_SERVER_ERROR:
          message = '服务器内部错误'
          errorCode = ERROR_CODES.INTERNAL_SERVER_ERROR
          break
        case HTTP_STATUS.BAD_GATEWAY:
        case HTTP_STATUS.SERVICE_UNAVAILABLE:
        case HTTP_STATUS.GATEWAY_TIMEOUT:
          message = '服务暂时不可用，请稍后再试'
          errorCode = ERROR_CODES.SERVICE_UNAVAILABLE
          break
        default:
          message = data?.message || `请求失败 (${status})`
          errorCode = data?.code || ERROR_CODES.UNKNOWN_ERROR
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络设置'
      errorCode = ERROR_CODES.NETWORK_ERROR
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后再试'
      errorCode = ERROR_CODES.TIMEOUT_ERROR
    } else {
      message = error.message || '未知错误'
      errorCode = ERROR_CODES.UNKNOWN_ERROR
    }
    
    // 显示错误信息
    if (error.config?.showError !== false) {
      ElMessage.error(message)
    }
    
    // 创建增强的错误对象
    const enhancedError = new Error(message)
    enhancedError.code = errorCode
    enhancedError.status = error.response?.status
    enhancedError.data = error.response?.data
    enhancedError.config = error.config
    
    return Promise.reject(enhancedError)
  }
)

// 请求重试功能
const retryRequest = (error) => {
  const config = error.config
  if (!config || !config.retry) return Promise.reject(error)
  
  config.retryCount = config.retryCount || 0
  if (config.retryCount >= API_CONFIG.RETRY_COUNT) {
    return Promise.reject(error)
  }
  
  config.retryCount++
  
  return new Promise(resolve => {
    setTimeout(() => {
      resolve(request(config))
    }, API_CONFIG.RETRY_DELAY)
  })
}

// 清除缓存
export const clearCache = (pattern) => {
  if (pattern) {
    for (const key of requestCache.keys()) {
      if (key.includes(pattern)) {
        requestCache.delete(key)
      }
    }
  } else {
    requestCache.clear()
  }
}

// 取消所有请求
export const cancelAllRequests = () => {
  for (const [key, requestId] of pendingRequests.entries()) {
    // 这里可以实现具体的取消逻辑
    pendingRequests.delete(key)
  }
}

export default request