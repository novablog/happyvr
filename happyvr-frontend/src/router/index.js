import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      redirect: '/dashboard'
    },
    // 认证相关路由（无布局）
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { hideLayout: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/Register.vue'),
      meta: { hideLayout: true }
    },
    // 管理端登录
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('@/views/admin/Login.vue'),
      meta: { hideLayout: true }
    },
    // 用户端路由（使用UserLayout布局）
    {
      path: '/',
      component: () => import('@/components/layout/UserLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/Dashboard.vue'),
          meta: { title: '工作台' }
        },
        {
          path: 'projects',
          name: 'projects',
          component: () => import('@/views/projects/ProjectList.vue'),
          meta: { title: '我的项目' }
        },
        {
          path: 'projects/create',
          name: 'project-create',
          component: () => import('@/views/projects/ProjectCreate.vue'),
          meta: { title: '创建项目' }
        },
        {
          path: 'projects/:id',
          name: 'project-detail',
          component: () => import('@/views/projects/ProjectDetail.vue'),
          meta: { title: '项目详情' }
        },
        {
          path: 'projects/:id/edit',
          name: 'project-edit',
          component: () => import('@/views/projects/ProjectEdit.vue'),
          meta: { title: '编辑项目' }
        },
        {
          path: 'projects/:id/vr-editor',
          name: 'vr-editor',
          component: () => import('@/views/vr/VREditor.vue'),
          meta: { title: 'VR编辑器' }
        },
        {
          path: 'explore',
          name: 'explore',
          component: () => import('@/views/explore/Explore.vue'),
          meta: { title: '发现' }
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/user/Profile.vue'),
          meta: { title: '个人资料' }
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/user/Settings.vue'),
          meta: { title: '设置' }
        }
      ]
    },
    // 管理端路由
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '管理后台' }
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('@/views/admin/UserManagement.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'roles',
          name: 'admin-roles',
          component: () => import('@/views/admin/RoleManagement.vue'),
          meta: { title: '角色管理' }
        },
        {
          path: 'projects',
          name: 'admin-projects',
          component: () => import('@/views/admin/ProjectManagement.vue'),
          meta: { title: '内容管理' }
        },
        {
          path: 'system',
          name: 'admin-system',
          component: () => import('@/views/admin/SystemManagement.vue'),
          meta: { title: '系统管理' }
        }
      ]
    },
    // 公共VR预览页面（无需登录）
    {
      path: '/vr/:shareToken',
      name: 'vr-preview',
      component: () => import('@/views/vr/VRPreview.vue'),
      meta: { hideLayout: true, title: 'VR预览' }
    },
    // 404页面
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/error/NotFound.vue'),
      meta: { hideLayout: true }
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - HappyVR`
  } else {
    document.title = 'HappyVR - VR内容创作平台'
  }

  // 检查认证
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 如果有token但没有用户信息，尝试获取用户信息
    if (!userStore.userInfo.username) {
      try {
        await userStore.fetchUserInfo()
      } catch (error) {
        // 获取用户信息失败，跳转到登录页
        next('/login')
        return
      }
    }
  }

  // 检查管理员权限
  if (to.meta.requiresAdmin) {
    // 检查是否有管理员token
    const adminToken = localStorage.getItem('adminToken')
    if (!adminToken) {
      next({
        path: '/admin/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // TODO: 验证管理员token有效性
    // 这里可以添加token验证逻辑
  }

  // 如果已登录用户访问登录/注册页面，重定向到工作台
  if ((to.name === 'login' || to.name === 'register') && userStore.isLoggedIn) {
    next('/dashboard')
    return
  }

  next()
})

export default router