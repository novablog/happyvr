import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAPIStore = defineStore('api', () => {
    // 状态
    const loading = ref(false)
    const loadingCount = ref(0)
    const errors = ref([])
    const requestHistory = ref([])
    const networkStatus = ref('online')

    // 计算属性
    const isLoading = computed(() => loadingCount.value > 0)
    const hasErrors = computed(() => errors.value.length > 0)
    const latestError = computed(() => errors.value[errors.value.length - 1])

    // 方法
    const startLoading = (requestId) => {
        loadingCount.value++
        loading.value = true

        // 记录请求历史
        requestHistory.value.push({
            id: requestId,
            timestamp: Date.now(),
            status: 'loading'
        })
    }

    const stopLoading = (requestId) => {
        if (loadingCount.value > 0) {
            loadingCount.value--
        }

        if (loadingCount.value === 0) {
            loading.value = false
        }

        // 更新请求历史
        const request = requestHistory.value.find(r => r.id === requestId)
        if (request) {
            request.status = 'completed'
            request.duration = Date.now() - request.timestamp
        }
    }

    const addError = (error) => {
        const errorInfo = {
            id: Date.now() + Math.random(),
            message: error.message || '未知错误',
            code: error.code || 'UNKNOWN_ERROR',
            status: error.status || 0,
            timestamp: Date.now(),
            url: error.config?.url || '',
            method: error.config?.method || ''
        }

        errors.value.push(errorInfo)

        // 限制错误记录数量
        if (errors.value.length > 50) {
            errors.value = errors.value.slice(-50)
        }

        // 更新请求历史
        const requestId = error.config?.requestId
        if (requestId) {
            const request = requestHistory.value.find(r => r.id === requestId)
            if (request) {
                request.status = 'error'
                request.error = errorInfo
                request.duration = Date.now() - request.timestamp
            }
        }
    }

    const clearErrors = () => {
        errors.value = []
    }

    const removeError = (errorId) => {
        const index = errors.value.findIndex(e => e.id === errorId)
        if (index > -1) {
            errors.value.splice(index, 1)
        }
    }

    const setNetworkStatus = (status) => {
        networkStatus.value = status
    }

    const clearHistory = () => {
        requestHistory.value = []
    }

    const getRequestStats = () => {
        const total = requestHistory.value.length
        const completed = requestHistory.value.filter(r => r.status === 'completed').length
        const errors = requestHistory.value.filter(r => r.status === 'error').length
        const loading = requestHistory.value.filter(r => r.status === 'loading').length

        const avgDuration = requestHistory.value
            .filter(r => r.duration)
            .reduce((sum, r) => sum + r.duration, 0) /
            (requestHistory.value.filter(r => r.duration).length || 1)

        return {
            total,
            completed,
            errors,
            loading,
            avgDuration: Math.round(avgDuration),
            successRate: total > 0 ? Math.round((completed / total) * 100) : 0
        }
    }

    // 网络状态监听
    const initNetworkListener = () => {
        const updateNetworkStatus = () => {
            setNetworkStatus(navigator.onLine ? 'online' : 'offline')
        }

        window.addEventListener('online', updateNetworkStatus)
        window.addEventListener('offline', updateNetworkStatus)

        // 初始状态
        updateNetworkStatus()

        return () => {
            window.removeEventListener('online', updateNetworkStatus)
            window.removeEventListener('offline', updateNetworkStatus)
        }
    }

    return {
        // 状态
        loading,
        loadingCount,
        errors,
        requestHistory,
        networkStatus,

        // 计算属性
        isLoading,
        hasErrors,
        latestError,

        // 方法
        startLoading,
        stopLoading,
        addError,
        clearErrors,
        removeError,
        setNetworkStatus,
        clearHistory,
        getRequestStats,
        initNetworkListener
    }
})