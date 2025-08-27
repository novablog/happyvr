<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">
          欢迎回来，{{ userInfo.username || '用户' }}！
        </h1>
        <p class="welcome-subtitle">
          开始创建您的VR作品，探索无限可能
        </p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" size="large" @click="createProject">
          <el-icon>
            <Plus />
          </el-icon>
          创建新项目
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon>
            <Folder />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.projectCount }}</div>
          <div class="stat-label">我的项目</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <el-icon>
            <View />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalViews }}</div>
          <div class="stat-label">总浏览量</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <el-icon>
            <Star />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.publishedCount }}</div>
          <div class="stat-label">已发布</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <el-icon>
            <Clock />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.draftCount }}</div>
          <div class="stat-label">草稿</div>
        </div>
      </div>
    </div>

    <!-- 最近项目 -->
    <div class="recent-section">
      <div class="section-header">
        <h2 class="section-title">最近项目</h2>
        <el-button text @click="viewAllProjects">
          查看全部
          <el-icon>
            <ArrowRight />
          </el-icon>
        </el-button>
      </div>

      <div class="recent-projects" v-loading="loading">
        <div v-for="project in recentProjects" :key="project.id" class="project-card" @click="viewProject(project.id)">
          <div class="project-cover">
            <img :src="project.coverImage || '/images/default-cover.jpg'" :alt="project.title" class="cover-image" />
            <div class="project-status">
              <el-tag :type="getStatusType(project.status)" size="small">
                {{ getStatusText(project.status) }}
              </el-tag>
            </div>
          </div>

          <div class="project-info">
            <h3 class="project-title">{{ project.title }}</h3>
            <p class="project-description">{{ project.description || '暂无描述' }}</p>
            <div class="project-meta">
              <span class="project-date">{{ formatDate(project.updatedAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="recentProjects.length === 0 && !loading" class="empty-projects">
          <el-empty description="还没有项目">
            <el-button type="primary" @click="createProject">
              创建第一个项目
            </el-button>
          </el-empty>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <div class="section-header">
        <h2 class="section-title">快速操作</h2>
      </div>

      <div class="actions-grid">
        <div class="action-card" @click="createProject">
          <div class="action-icon">
            <el-icon>
              <Plus />
            </el-icon>
          </div>
          <div class="action-content">
            <h3 class="action-title">创建项目</h3>
            <p class="action-description">开始一个新的VR项目</p>
          </div>
        </div>

        <div class="action-card" @click="exploreProjects">
          <div class="action-icon">
            <el-icon>
              <Search />
            </el-icon>
          </div>
          <div class="action-content">
            <h3 class="action-title">发现作品</h3>
            <p class="action-description">浏览其他用户的精彩作品</p>
          </div>
        </div>

        <div class="action-card" @click="viewProfile">
          <div class="action-icon">
            <el-icon>
              <User />
            </el-icon>
          </div>
          <div class="action-content">
            <h3 class="action-title">个人资料</h3>
            <p class="action-description">管理您的个人信息</p>
          </div>
        </div>

        <div class="action-card" @click="viewSettings">
          <div class="action-icon">
            <el-icon>
              <Setting />
            </el-icon>
          </div>
          <div class="action-content">
            <h3 class="action-title">设置</h3>
            <p class="action-description">配置应用偏好设置</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const recentProjects = ref([])
const stats = ref({
  projectCount: 0,
  totalViews: 0,
  publishedCount: 0,
  draftCount: 0
})

// 计算属性
const userInfo = computed(() => userStore.userInfo)

// 方法
const fetchDashboardData = async () => {
  loading.value = true
  try {
    // 暂时使用模拟数据，因为后端API可能还没有实现
    console.log('开始获取工作台数据...')

    // 模拟统计数据
    stats.value = {
      projectCount: 3,
      totalViews: 156,
      publishedCount: 2,
      draftCount: 1
    }

    // 模拟最近项目数据
    recentProjects.value = [
      {
        id: 1,
        title: '示例VR全景',
        description: '这是一个示例VR全景项目',
        status: 1,
        coverImage: null,
        updatedAt: new Date().toISOString()
      }
    ]

    console.log('工作台数据加载完成')

    // TODO: 当后端API准备好后，取消注释以下代码
    /*
    // 获取统计数据
    const statsResponse = await request.get('/v1/projects/statistics')
    stats.value = statsResponse.data

    // 获取最近项目
    const projectsResponse = await request.get('/v1/projects/my', {
      params: {
        page: 0,
        size: 6,
        sortBy: 'updatedAt',
        sortDir: 'desc'
      }
    })
    recentProjects.value = projectsResponse.data.content
    */
  } catch (error) {
    console.error('获取工作台数据失败:', error)
    // 即使API失败，也显示模拟数据
    stats.value = {
      projectCount: 0,
      totalViews: 0,
      publishedCount: 0,
      draftCount: 0
    }
    recentProjects.value = []
  } finally {
    loading.value = false
  }
}

const createProject = () => {
  router.push('/projects/create')
}

const viewAllProjects = () => {
  router.push('/projects')
}

const viewProject = (id) => {
  router.push(`/projects/${id}`)
}

const exploreProjects = () => {
  router.push('/explore')
}

const viewProfile = () => {
  router.push('/profile')
}

const viewSettings = () => {
  router.push('/settings')
}

const getStatusType = (status) => {
  const statusMap = {
    0: 'info',    // 草稿
    1: 'success', // 已发布
    2: 'warning', // 审核中
    3: 'danger'   // 已拒绝
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '草稿',
    1: '已发布',
    2: '审核中',
    3: '已拒绝'
  }
  return statusMap[status] || '未知'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  console.log('Dashboard组件已挂载')
  console.log('用户信息:', userInfo.value)
  fetchDashboardData()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .welcome-section {
    background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
    border: 1px solid var(--border-primary);
    border-radius: 16px;
    padding: 32px;
    margin-bottom: 32px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .welcome-content {
      .welcome-title {
        font-size: 28px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px 0;
      }

      .welcome-subtitle {
        font-size: 16px;
        color: var(--text-secondary);
        margin: 0;
      }
    }

    .welcome-actions {
      .el-button {
        padding: 12px 24px;
        font-size: 16px;
      }
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 20px;
    margin-bottom: 32px;

    .stat-card {
      background: var(--bg-secondary);
      border: 1px solid var(--border-primary);
      border-radius: 12px;
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 16px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        border-color: var(--primary-color);
      }

      .stat-icon {
        width: 48px;
        height: 48px;
        background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        color: #000;
      }

      .stat-content {
        .stat-number {
          font-size: 24px;
          font-weight: 600;
          color: var(--text-primary);
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 14px;
          color: var(--text-secondary);
        }
      }
    }
  }

  .recent-section {
    margin-bottom: 32px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .section-title {
        font-size: 20px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0;
      }
    }

    .recent-projects {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 20px;

      .project-card {
        background: var(--bg-secondary);
        border: 1px solid var(--border-primary);
        border-radius: 12px;
        overflow: hidden;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
          border-color: var(--primary-color);
        }

        .project-cover {
          position: relative;
          height: 160px;
          overflow: hidden;

          .cover-image {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          .project-status {
            position: absolute;
            top: 12px;
            right: 12px;
          }
        }

        .project-info {
          padding: 16px;

          .project-title {
            font-size: 16px;
            font-weight: 600;
            color: var(--text-primary);
            margin: 0 0 8px 0;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .project-description {
            font-size: 14px;
            color: var(--text-secondary);
            margin: 0 0 12px 0;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .project-meta {
            .project-date {
              font-size: 12px;
              color: var(--text-tertiary);
            }
          }
        }
      }

      .empty-projects {
        grid-column: 1 / -1;
        padding: 40px 20px;
      }
    }
  }

  .quick-actions {
    .section-header {
      margin-bottom: 20px;

      .section-title {
        font-size: 20px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0;
      }
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;

      .action-card {
        background: var(--bg-secondary);
        border: 1px solid var(--border-primary);
        border-radius: 12px;
        padding: 24px;
        cursor: pointer;
        transition: all 0.3s ease;
        display: flex;
        align-items: center;
        gap: 16px;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
          border-color: var(--primary-color);
        }

        .action-icon {
          width: 48px;
          height: 48px;
          background: var(--bg-tertiary);
          border: 1px solid var(--border-secondary);
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: var(--primary-color);
        }

        .action-content {
          .action-title {
            font-size: 16px;
            font-weight: 600;
            color: var(--text-primary);
            margin: 0 0 4px 0;
          }

          .action-description {
            font-size: 14px;
            color: var(--text-secondary);
            margin: 0;
          }
        }
      }
    }
  }
}

// 响应式样式
@media (max-width: 768px) {
  .dashboard {
    .welcome-section {
      flex-direction: column;
      gap: 20px;
      text-align: center;
      padding: 24px;

      .welcome-content {
        .welcome-title {
          font-size: 24px;
        }

        .welcome-subtitle {
          font-size: 14px;
        }
      }
    }

    .stats-grid {
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;

      .stat-card {
        padding: 20px;
        flex-direction: column;
        text-align: center;
        gap: 12px;

        .stat-icon {
          width: 40px;
          height: 40px;
          font-size: 20px;
        }

        .stat-content {
          .stat-number {
            font-size: 20px;
          }
        }
      }
    }

    .recent-projects {
      grid-template-columns: 1fr;
    }

    .actions-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>