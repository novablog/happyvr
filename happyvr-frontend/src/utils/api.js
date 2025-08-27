import { API_ENDPOINTS, FILE_TYPES, FILE_SIZE_LIMITS } from '@/api/config'

/**
 * API工具类
 */
export class APIUtils {
  /**
   * 构建URL，替换路径参数
   * @param {string} template - URL模板
   * @param {object} params - 路径参数
   * @returns {string} 构建后的URL
   */
  static buildURL(template, params = {}) {
    let url = template
    for (const [key, value] of Object.entries(params)) {
      url = url.replace(`:${key}`, encodeURIComponent(value))
    }
    return url
  }

  /**
   * 构建查询参数
   * @param {object} params - 参数对象
   * @returns {string} 查询字符串
   */
  static buildQuery(params = {}) {
    const query = new URLSearchParams()
    for (const [key, value] of Object.entries(params)) {
      if (value !== null && value !== undefined && value !== '') {
        if (Array.isArray(value)) {
          value.forEach(item => query.append(key, item))
        } else {
          query.append(key, value)
        }
      }
    }
    return query.toString()
  }

  /**
   * 验证文件类型
   * @param {File} file - 文件对象
   * @param {string[]} allowedTypes - 允许的文件类型
   * @returns {boolean} 是否有效
   */
  static validateFileType(file, allowedTypes) {
    if (!file || !file.name) return false
    const extension = file.name.split('.').pop()?.toLowerCase()
    return allowedTypes.includes(extension)
  }

  /**
   * 验证文件大小
   * @param {File} file - 文件对象
   * @param {number} maxSize - 最大大小（字节）
   * @returns {boolean} 是否有效
   */
  static validateFileSize(file, maxSize) {
    return file && file.size <= maxSize
  }

  /**
   * 格式化文件大小
   * @param {number} bytes - 字节数
   * @returns {string} 格式化后的大小
   */
  static formatFileSize(bytes) {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  /**
   * 获取文件类型
   * @param {string} filename - 文件名
   * @returns {string} 文件类型
   */
  static getFileType(filename) {
    const extension = filename.split('.').pop()?.toLowerCase()
    
    if (FILE_TYPES.IMAGES.includes(extension)) return 'image'
    if (FILE_TYPES.VIDEOS.includes(extension)) return 'video'
    if (FILE_TYPES.AUDIOS.includes(extension)) return 'audio'
    if (FILE_TYPES.DOCUMENTS.includes(extension)) return 'document'
    
    return 'unknown'
  }

  /**
   * 创建FormData
   * @param {object} data - 数据对象
   * @returns {FormData} FormData对象
   */
  static createFormData(data) {
    const formData = new FormData()
    
    for (const [key, value] of Object.entries(data)) {
      if (value instanceof File) {
        formData.append(key, value)
      } else if (Array.isArray(value)) {
        value.forEach((item, index) => {
          if (item instanceof File) {
            formData.append(`${key}[${index}]`, item)
          } else {
            formData.append(`${key}[${index}]`, JSON.stringify(item))
          }
        })
      } else if (typeof value === 'object' && value !== null) {
        formData.append(key, JSON.stringify(value))
      } else {
        formData.append(key, value)
      }
    }
    
    return formData
  }

  /**
   * 深度合并对象
   * @param {object} target - 目标对象
   * @param {object} source - 源对象
   * @returns {object} 合并后的对象
   */
  static deepMerge(target, source) {
    const result = { ...target }
    
    for (const key in source) {
      if (source.hasOwnProperty(key)) {
        if (typeof source[key] === 'object' && source[key] !== null && !Array.isArray(source[key])) {
          result[key] = this.deepMerge(result[key] || {}, source[key])
        } else {
          result[key] = source[key]
        }
      }
    }
    
    return result
  }

  /**
   * 防抖函数
   * @param {Function} func - 要防抖的函数
   * @param {number} delay - 延迟时间
   * @returns {Function} 防抖后的函数
   */
  static debounce(func, delay) {
    let timeoutId
    return function (...args) {
      clearTimeout(timeoutId)
      timeoutId = setTimeout(() => func.apply(this, args), delay)
    }
  }

  /**
   * 节流函数
   * @param {Function} func - 要节流的函数
   * @param {number} delay - 延迟时间
   * @returns {Function} 节流后的函数
   */
  static throttle(func, delay) {
    let lastCall = 0
    return function (...args) {
      const now = Date.now()
      if (now - lastCall >= delay) {
        lastCall = now
        return func.apply(this, args)
      }
    }
  }

  /**
   * 重试函数
   * @param {Function} func - 要重试的函数
   * @param {number} maxRetries - 最大重试次数
   * @param {number} delay - 重试延迟
   * @returns {Promise} Promise对象
   */
  static async retry(func, maxRetries = 3, delay = 1000) {
    let lastError
    
    for (let i = 0; i <= maxRetries; i++) {
      try {
        return await func()
      } catch (error) {
        lastError = error
        if (i < maxRetries) {
          await new Promise(resolve => setTimeout(resolve, delay * Math.pow(2, i)))
        }
      }
    }
    
    throw lastError
  }

  /**
   * 并发控制
   * @param {Function[]} tasks - 任务数组
   * @param {number} concurrency - 并发数
   * @returns {Promise} Promise对象
   */
  static async concurrent(tasks, concurrency = 3) {
    const results = []
    const executing = []
    
    for (const task of tasks) {
      const promise = Promise.resolve().then(() => task()).then(result => {
        executing.splice(executing.indexOf(promise), 1)
        return result
      })
      
      results.push(promise)
      
      if (results.length >= concurrency) {
        executing.push(promise)
      }
      
      if (executing.length >= concurrency) {
        await Promise.race(executing)
      }
    }
    
    return Promise.all(results)
  }

  /**
   * 生成唯一ID
   * @returns {string} 唯一ID
   */
  static generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2)
  }

  /**
   * 格式化日期
   * @param {Date|string|number} date - 日期
   * @param {string} format - 格式
   * @returns {string} 格式化后的日期
   */
  static formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
    const d = new Date(date)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    const seconds = String(d.getSeconds()).padStart(2, '0')
    
    return format
      .replace('YYYY', year)
      .replace('MM', month)
      .replace('DD', day)
      .replace('HH', hours)
      .replace('mm', minutes)
      .replace('ss', seconds)
  }

  /**
   * 规范化后端返回的文件 URL：
   * - 如果已经是完整的 http(s) URL，直接返回
   * - 如果以 /api 开头，直接返回（已包含上下文）
   * - 如果以 /files 开头，前端开发环境需要加上 '/api' 前缀以走 Vite 代理
   * - 其他相对路径按原样返回
   * @param {string} url
   * @returns {string}
   */
  static resolveFileUrl(url) {
    if (!url) return url
    try {
      if (/^https?:\/\//i.test(url)) return url
    } catch (e) {
      // ignore
    }

    if (url.startsWith('/api')) {
      return url
    }

    // 后端默认将文件返回为 /files/xxx 的路径，开发时需要代理到 /api/files
    if (url.startsWith('/files')) {
      return '/api' + url
    }

    return url
  }

  /**
   * 解析错误信息
   * @param {Error} error - 错误对象
   * @returns {object} 解析后的错误信息
   */
  static parseError(error) {
    return {
      message: error.message || '未知错误',
      code: error.code || 'UNKNOWN_ERROR',
      status: error.status || 0,
      data: error.data || null,
      stack: error.stack || null
    }
  }

  /**
   * 检查是否为移动设备
   * @returns {boolean} 是否为移动设备
   */
  static isMobile() {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
  }

  /**
   * 获取浏览器信息
   * @returns {object} 浏览器信息
   */
  static getBrowserInfo() {
    const ua = navigator.userAgent
    const browsers = {
      chrome: /Chrome/i.test(ua),
      firefox: /Firefox/i.test(ua),
      safari: /Safari/i.test(ua) && !/Chrome/i.test(ua),
      edge: /Edge/i.test(ua),
      ie: /MSIE|Trident/i.test(ua)
    }
    
    return {
      userAgent: ua,
      ...browsers,
      mobile: this.isMobile()
    }
  }
}

export default APIUtils