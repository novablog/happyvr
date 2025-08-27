<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="admin-logo">
        <div class="logo-content">
          <div class="logo-icon">
            <el-icon><Monitor /></el-icon>
          </div>
          <h2 v-show="!sidebarCollapsed">HappyVR 管理</h2>
        </div>
      </div>
      
      <nav class="admin-nav">
        <template v-for="item in menuItems" :key="item.path">
          <router-link 
            v-if="hasPermission(item.permission)"
            :to="item.path" 
            class="nav-item"
            :class="{ active: isActiveRoute(item.path) }"
          >
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <span v-show="!sidebarCollapsed" class="nav-text">{{ item.title }}</span>
            <el-badge 
              v-if="item.badge && !sidebarCollapsed" 
              :value="item.badge" 
              class="nav-badge"
            />
          </router-link>
        </template>
      </nav>
      
      <!-- 侧边栏底部 -->
      <div class="sidebar-footer">
        <el-button 
          text 
          @click="toggleSidebar"
          class="collapse-btn"
        >
          <el-icon>
            <Expand v-if="sidebarCollapsed" />
            <Fold v-else />
          </el-icon>
        </el-button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶部导航栏 -->
      <div class="admin-header">
        <div class="header-left">
          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/admin' }">管理后台</el-breadcrumb-item>
            <el-breadcrumb-item 
              v-for="crumb in breadcrumbs" 
              :key="crumb.path"
              :to="crumb.path ? { path: crumb.path } : undefined"
            >
              {{ crumb.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 系统状态指示器 -->
          <div class="system-status">
            <el-tooltip content="系统状态" placement="bottom">
              <el-badge :value="systemAlerts" :hidden="systemAlerts === 0">
                <el-icon class="status-icon" :class="{ online: systemOnline }">
                  <Connection />
                </el-icon>
              </el-badge>
            </el-tooltip>
          </div>
          
          <!-- 管理员信息 -->
          <el-dropdown @command="handleUserCommand" class="admin-dropdown">
            <div class="admin-info">
              <el-avatar :size="32" :src="adminInfo.avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="admin-details" v-show="!sidebarCollapsed">
                <span class="admin-name">{{ adminInfo.name }}</span>
                <span class="admin-role">{{ adminInfo.role }}</span>
              </div>
              <el-icon class="dropdown-icon">
                <ArrowDown />
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 内容区域 -->
      <div class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Monitor, User, Key, Folder, Setting, UserFilled, ArrowDown,
  SwitchButton, Connection, Expand, Fold, DataAnalysis
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const sidebarCollapsed = ref(false)
const systemOnline = ref(true)
const systemAlerts = ref(0)

// 管理员信息
const adminInfo = ref({
  name: '管理员',
  role: '超级管理员',
  avatar: ''
})

// 菜单项配置
const menuItems = [
  {
    path: '/admin/dashboard',
    title: '仪表盘',
    icon: DataAnalysis,
    permission: 'dashboard:view'
  },
  {
    path: '/admin/users',
    title: '用户管理',
    icon: User,
    permission: 'user:view'
  },
  {
    path: '/admin/roles',
    title: '角色权限',
    icon: Key,
    permission: 'role:view'
  },
  {
    path: '/admin/projects',
    title: '内容管理',
    icon: Folder,
    permission: 'project:view',
    badge: 5 // 待审核数量
  },
  {
    path: '/admin/system',
    title: '系统管理',
    icon: Setting,
    permission: 'system:view'
  }
]

// 面包屑导航
const breadcrumbs = computed(() => {
  const pathSegments = route.path.split('/').filter(Boolean)
  const crumbs = []
  
  // 根据当前路由生成面包屑
  if (pathSegments.length > 1) {
    const adminPath = pathSegments[1] // admin
    if (adminPath === 'admin' && pathSegments.length > 2) {
      const modulePath = pathSegments[2]
      const menuItem = menuItems.find(item => item.path.includes(modulePath))
      
      if (menuItem) {
        crumbs.push({
          title: menuItem.title,
          path: menuItem.path
        })
        
        // 如果有子页面，添加子页面面包屑
        if (pathSegments.length > 3) {
          const subPath = pathSegments[3]
          const subPageTitles = {
            'create': '新建',
            'edit': '编辑',
            'detail': '详情'
          }
          
          if (subPageTitles[subPath]) {
            crumbs.push({
              title: subPageTitles[subPath],
              path: null // 当前页面不需要链接
            })
          }
        }
      }
    }
  }
  
  return crumbs
})

// 权限检查
const hasPermission = (permission) => {
  // TODO: 实现真实的权限检查逻辑
  // 这里暂时返回true，后续会根据用户角色进行权限验证
  return true
}

// 检查当前激活路由
const isActiveRoute = (path) => {
  return route.path.startsWith(path)
}

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 处理用户下拉菜单命令
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/admin/profile')
      break
    case 'settings':
      router.push('/admin/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '确认退出', {
      type: 'warning'
    })
    
    // 清除登录状态
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminInfo')
    localStorage.removeItem('adminRemember')
    
    ElMessage.success('已退出登录')
    router.push('/admin/login')
  } catch (error) {
    // 用户取消退出
  }
}

// 监听路由变化，更新系统状态
watch(() => route.path, () => {
  // 可以在这里根据路由变化更新系统状态
}, { immediate: true })

// 初始化管理员信息
const initAdminInfo = () => {
  const savedAdminInfo = localStorage.getItem('adminInfo')
  if (savedAdminInfo) {
    try {
      adminInfo.value = JSON.parse(savedAdminInfo)
    } catch (error) {
      console.error('解析管理员信息失败:', error)
    }
  }
}

// 初始化
initAdminInfo()
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background: var(--bg-primary);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.admin-sidebar {
  width: 250px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-primary);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  
  &.collapsed {
    width: 64px;
  }
  
  .admin-logo {
    padding: 20px;
    border-bottom: 1px solid var(--border-primary);
    
    .logo-content {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .logo-icon {
        color: var(--primary-color);
        font-size: 24px;
        flex-shrink: 0;
      }
      
      h2 {
        color: var(--primary-color);
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        white-space: nowrap;
      }
    }
  }
  
  .admin-nav {
    flex: 1;
    padding: 20px 0;
    overflow-y: auto;
    
    .nav-item {
      display: flex;
      align-items: center;
      padding: 12px 20px;
      color: var(--text-secondary);
      text-decoration: none;
      transition: all 0.3s ease;
      position: relative;
      
      .el-icon {
        font-size: 18px;
        flex-shrink: 0;
      }
      
      .nav-text {
        margin-left: 12px;
        font-size: 14px;
        white-space: nowrap;
      }
      
      .nav-badge {
        margin-left: auto;
      }
      
      &:hover {
        background: rgba(212, 175, 55, 0.1);
        color: var(--primary-color);
      }
      
      &.active,
      &.router-link-active {
        background: rgba(212, 175, 55, 0.15);
        color: var(--primary-color);
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 3px;
          background: var(--primary-color);
        }
      }
    }
  }
  
  .sidebar-footer {
    padding: 16px;
    border-top: 1px solid var(--border-primary);
    
    .collapse-btn {
      width: 100%;
      color: var(--text-secondary);
      
      &:hover {
        color: var(--primary-color);
      }
    }
  }
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.admin-header {
  height: 64px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-primary);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  
  .header-left {
    .breadcrumb {
      :deep(.el-breadcrumb__item) {
        .el-breadcrumb__inner {
          color: var(--text-secondary);
          font-weight: 400;
          
          &:hover {
            color: var(--primary-color);
          }
        }
        
        &:last-child .el-breadcrumb__inner {
          color: var(--text-primary);
          font-weight: 500;
        }
      }
      
      :deep(.el-breadcrumb__separator) {
        color: var(--text-tertiary);
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .system-status {
      .status-icon {
        font-size: 18px;
        color: var(--text-tertiary);
        cursor: pointer;
        
        &.online {
          color: #67c23a;
        }
        
        &:hover {
          color: var(--primary-color);
        }
      }
    }
    
    .admin-dropdown {
      .admin-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 6px;
        transition: background-color 0.3s ease;
        
        &:hover {
          background: var(--bg-tertiary);
        }
        
        .admin-details {
          display: flex;
          flex-direction: column;
          align-items: flex-start;
          
          .admin-name {
            font-size: 14px;
            font-weight: 500;
            color: var(--text-primary);
            line-height: 1.2;
          }
          
          .admin-role {
            font-size: 12px;
            color: var(--text-tertiary);
            line-height: 1.2;
          }
        }
        
        .dropdown-icon {
          font-size: 12px;
          color: var(--text-tertiary);
          transition: transform 0.3s ease;
        }
        
        &:hover .dropdown-icon {
          transform: rotate(180deg);
        }
      }
    }
  }
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: var(--bg-primary);
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 响应式设计
@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transform: translateX(-100%);
    
    &.collapsed {
      width: 250px;
      transform: translateX(0);
    }
  }
  
  .admin-main {
    margin-left: 0;
  }
  
  .admin-header {
    padding: 0 16px;
    
    .header-right {
      .admin-details {
        display: none;
      }
    }
  }
  
  .admin-content {
    padding: 16px;
  }
}

// 深色主题适配
:deep(.el-dropdown-menu) {
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  
  .el-dropdown-menu__item {
    color: var(--text-secondary);
    
    &:hover {
      background: var(--bg-tertiary);
      color: var(--primary-color);
    }
  }
}

:deep(.el-badge__content) {
  background: var(--primary-color);
  border-color: var(--primary-color);
}
</style>