import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'
import './styles/index.scss'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 应用启动时的自动登录逻辑
const initApp = async () => {
  const userStore = useUserStore()
  
  // 如果本地存储中有token，尝试获取用户信息实现自动登录
  if (userStore.token && !userStore.userInfo.username) {
    try {
      await userStore.fetchUserInfo()
      console.log('自动登录成功')
    } catch (error) {
      console.warn('自动登录失败:', error)
      // 如果自动登录失败，清除无效的token
      await userStore.logout()
    }
  }
}

// 挂载应用并初始化
app.mount('#app')
initApp()