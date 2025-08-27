<template>
  <div class="user-layout">
    <!-- 顶部导航栏 -->
    <header class="layout-header">
      <div class="header-content">
        <!-- Logo和标题 -->
        <div class="header-left">
          <router-link to="/dashboard" class="logo-link">
            <div class="logo">
              <el-icon class="logo-icon"><VideoCamera /></el-icon>
              <span class="logo-text">HappyVR</span>
            </div>
          </router-link>
        </div>

        <!-- 导航菜单 -->
        <nav class="header-nav desktop-only">
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            background-color="transparent"
            text-color="#cccccc"
            active-text-color="#d4af37"
            class="header-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <span>工作台</span>
            </el-menu-item>
            <el-menu-item index="/projects">
              <el-icon><Folder /></el-icon>
              <span>我的项目</span>
            </el-menu-item>
            <el-menu-item index="/explore">
              <el-icon><Search /></el-icon>
              <span>发现</span>
            </el-menu-item>
          </el-menu>
        </nav>

        <!-- 右侧用户操作区 -->
        <div class="header-right">
          <!-- 创建项目按钮 -->
          <el-button type="primary" class="create-btn" @click="handleCreateProject">
            <el-icon><Plus /></el-icon>
            <span class="desktop-only">创建项目</span>
          </el-button>

          <!-- 用户菜单 -->
          <el-dropdown @command="handleUserCommand" class="user-dropdown">
            <div class="user-avatar">
              <el-avatar :size="36" :src="userInfo?.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username desktop-only">{{ userInfo?.username || '用户' }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 移动端菜单按钮 -->
          <el-button
            class="mobile-menu-btn mobile-only"
            text
            @click="toggleMobileMenu"
          >
            <el-icon size="20"><Menu /></el-icon>
          </el-button>
        </div>
      </div>
    </header>

    <!-- 移动端侧边栏 -->
    <el-drawer
      v-model="mobileMenuVisible"
      direction="rtl"
      size="280px"
      class="mobile-drawer"
    >
      <template #header>
        <div class="mobile-header">
          <div class="logo">
            <el-icon class="logo-icon"><VideoCamera /></el-icon>
            <span class="logo-text">HappyVR</span>
          </div>
        </div>
      </template>
      
      <div class="mobile-menu">
        <el-menu
          :default-active="activeMenu"
          background-color="transparent"
          text-color="#cccccc"
          active-text-color="#d4af37"
          @select="handleMobileMenuSelect"
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/projects">
            <el-icon><Folder /></el-icon>
            <span>我的项目</span>
          </el-menu-item>
          <el-menu-item index="/explore">
            <el-icon><Search /></el-icon>
            <span>发现</span>
          </el-menu-item>
        </el-menu>

        <div class="mobile-user-info">
          <div class="user-card">
            <el-avatar :size="48" :src="userInfo?.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="user-details">
              <div class="username">{{ userInfo?.username || '用户' }}</div>
              <div class="user-email">{{ userInfo?.email || '' }}</div>
            </div>
          </div>
          
          <div class="mobile-actions">
            <el-button text @click="handleUserCommand('profile')">
              <el-icon><User /></el-icon>
              个人资料
            </el-button>
            <el-button text @click="handleUserCommand('settings')">
              <el-icon><Setting /></el-icon>
              设置
            </el-button>
            <el-button text @click="handleUserCommand('logout')">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 主内容区域 -->
    <main class="layout-main">
      <div class="main-content">
        <router-view v-slot="{ Component, route }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- 全局加载组件 -->
    <GlobalLoading v-if="globalLoading" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import GlobalLoading from '@/components/common/GlobalLoading.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const mobileMenuVisible = ref(false)
const globalLoading = ref(false)

// 计算属性
const activeMenu = computed(() => route.path)
const userInfo = computed(() => userStore.userInfo)

// 方法
const handleMenuSelect = (index) => {
  router.push(index)
}

const handleMobileMenuSelect = (index) => {
  mobileMenuVisible.value = false
  router.push(index)
}

const toggleMobileMenu = () => {
  mobileMenuVisible.value = !mobileMenuVisible.value
}

const handleCreateProject = () => {
  router.push('/projects/create')
}

const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        await userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      } catch (error) {
        // 用户取消操作
      }
      break
  }
}

// 监听窗口大小变化
const handleResize = () => {
  if (window.innerWidth > 768 && mobileMenuVisible.value) {
    mobileMenuVisible.value = false
  }
}

// 生命周期
onMounted(() => {
  window.addEventListener('resize', handleResize)
  // 获取用户信息
  console.log('UserLayout挂载，当前用户信息:', userInfo.value)
  if (!userInfo.value?.username) {
    console.log('用户信息不完整，尝试获取用户信息')
    userStore.fetchUserInfo().catch(error => {
      console.error('获取用户信息失败:', error)
    })
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 暴露给模板的响应式引用
defineExpose({
  globalLoading
})
</script>

<style lang="scss" scoped>
.user-layout {
  min-height: 100vh;
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
}

.layout-header {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-primary);
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(10px);

  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

.header-left {
  .logo-link {
    text-decoration: none;
    color: inherit;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 20px;
    font-weight: bold;
    color: var(--primary-color);

    .logo-icon {
      font-size: 24px;
    }

    .logo-text {
      background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
}

.header-nav {
  flex: 1;
  display: flex;
  justify-content: center;
  max-width: 400px;
  margin: 0 40px;

  .header-menu {
    border: none;
    background: transparent;

    :deep(.el-menu-item) {
      border-bottom: 2px solid transparent;
      transition: all 0.3s ease;

      &:hover {
        background-color: rgba(212, 175, 55, 0.1);
        color: var(--primary-color);
      }

      &.is-active {
        border-bottom-color: var(--primary-color);
        background-color: rgba(212, 175, 55, 0.1);
      }
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;

  .create-btn {
    background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
    border: none;
    color: #000;
    font-weight: 500;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
    }
  }

  .user-dropdown {
    .user-avatar {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 6px;
      transition: background-color 0.3s ease;

      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }

      .username {
        color: var(--text-secondary);
        font-size: 14px;
      }

      .dropdown-icon {
        font-size: 12px;
        color: var(--text-tertiary);
      }
    }
  }

  .mobile-menu-btn {
    color: var(--text-primary);
  }
}

.mobile-drawer {
  :deep(.el-drawer) {
    background: var(--bg-secondary);
  }

  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 20px;
    border-bottom: 1px solid var(--border-primary);
  }

  .mobile-header {
    .logo {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: bold;
      color: var(--primary-color);

      .logo-icon {
        font-size: 22px;
      }
    }
  }

  .mobile-menu {
    padding: 20px;

    .el-menu {
      border: none;
      background: transparent;

      :deep(.el-menu-item) {
        margin-bottom: 8px;
        border-radius: 6px;
        transition: all 0.3s ease;

        &:hover {
          background-color: rgba(212, 175, 55, 0.1);
        }

        &.is-active {
          background-color: rgba(212, 175, 55, 0.2);
          color: var(--primary-color);
        }
      }
    }

    .mobile-user-info {
      margin-top: 40px;
      padding-top: 20px;
      border-top: 1px solid var(--border-primary);

      .user-card {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 20px;

        .user-details {
          .username {
            font-weight: 500;
            color: var(--text-primary);
            margin-bottom: 4px;
          }

          .user-email {
            font-size: 12px;
            color: var(--text-tertiary);
          }
        }
      }

      .mobile-actions {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .el-button {
          justify-content: flex-start;
          color: var(--text-secondary);
          padding: 8px 0;

          &:hover {
            color: var(--primary-color);
          }
        }
      }
    }
  }
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;

  .main-content {
    flex: 1;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    width: 100%;
  }
}

// 响应式样式
@media (max-width: 768px) {
  .desktop-only {
    display: none !important;
  }

  .mobile-only {
    display: block !important;
  }

  .header-nav {
    display: none;
  }

  .layout-main .main-content {
    padding: 16px;
  }
}

@media (min-width: 769px) {
  .mobile-only {
    display: none !important;
  }

  .desktop-only {
    display: block !important;
  }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>