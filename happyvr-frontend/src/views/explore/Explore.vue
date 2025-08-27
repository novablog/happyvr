<template>
  <div class="explore-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">发现</h1>
      <p class="page-description">探索精彩的VR作品</p>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="最新" name="latest" />
        <el-tab-pane label="热门" name="popular" />
        <el-tab-pane label="推荐" name="featured" />
      </el-tabs>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索VR作品..."
        clearable
        @input="handleSearch"
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 项目网格 -->
    <div class="projects-grid" v-loading="loading">
      <div
        v-for="project in projects"
        :key="project.id"
        class="project-card"
        @click="viewProject(project)"
      >
        <div class="card-cover">
          <img
            :src="project.coverImage || '/images/default-cover.jpg'"
            :alt="project.title"
            class="cover-image"
          />
          <div class="card-overlay">
            <div class="overlay-content">
              <el-button type="primary" circle size="large">
                <el-icon><VideoPlay /></el-icon>
              </el-button>
              <div class="project-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ formatNumber(project.viewCount) }}
                </span>
              </div>
            </div>
          </div>
          
          <!-- 推荐标签 -->
          <div v-if="project.isFeatured" class="featured-badge">
            <el-icon><Star /></el-icon>
            推荐
          </div>
        </div>
        
        <div class="card-content">
          <h3 class="project-title">{{ project.title }}</h3>
          <p class="project-description">{{ project.description || '暂无描述' }}</p>
          
          <div class="project-footer">
            <div class="author-info">
              <el-avatar :size="24" :src="project.user?.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="author-name">{{ project.user?.username }}</span>
            </div>
            
            <div class="project-date">
              {{ formatDate(project.createdAt) }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="projects.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无作品" />
      </div>
    </div>

    <!-- 加载更多 -->
    <div class="load-more" v-if="hasMore && !loading">
      <el-button @click="loadMore" :loading="loadingMore">
        加载更多
      </el-button>
    </div>

    <!-- VR预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="VR预览"
      width="90%"
      :before-close="closePreview"
      class="vr-preview-dialog"
    >
      <div class="vr-preview-container">
        <iframe
          v-if="previewUrl"
          :src="previewUrl"
          frameborder="0"
          class="vr-iframe"
        ></iframe>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/api'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const loadingMore = ref(false)
const projects = ref([])
const activeTab = ref('latest')
const searchKeyword = ref('')
const currentPage = ref(0)
const hasMore = ref(true)
const previewVisible = ref(false)
const previewUrl = ref('')

// 方法
const fetchProjects = async (reset = false) => {
  if (reset) {
    loading.value = true
    currentPage.value = 0
    projects.value = []
    hasMore.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const params = {
      page: currentPage.value,
      size: 12,
      keyword: searchKeyword.value,
      type: activeTab.value,
      sortBy: getSortBy(),
      sortDir: 'desc'
    }
    
    const response = await api.get('/projects/public', params)
    const { content, totalPages } = response.data.data
    
    if (reset) {
      projects.value = content
    } else {
      projects.value.push(...content)
    }
    
    hasMore.value = currentPage.value + 1 < totalPages
    currentPage.value++
  } catch (error) {
    console.error('获取项目列表失败:', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const getSortBy = () => {
  switch (activeTab.value) {
    case 'popular':
      return 'viewCount'
    case 'featured':
      return 'isFeatured'
    default:
      return 'createdAt'
  }
}

const handleTabChange = () => {
  fetchProjects(true)
}

const handleSearch = () => {
  // 防抖处理
  clearTimeout(handleSearch.timer)
  handleSearch.timer = setTimeout(() => {
    fetchProjects(true)
  }, 500)
}

const loadMore = () => {
  fetchProjects(false)
}

const viewProject = (project) => {
  if (project.shareToken) {
    // 打开VR预览
    previewUrl.value = `/vr/${project.shareToken}`
    previewVisible.value = true
  } else {
    // 跳转到项目详情页
    router.push(`/projects/${project.id}`)
  }
}

const closePreview = () => {
  previewVisible.value = false
  previewUrl.value = ''
}

const formatNumber = (num) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 30) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 生命周期
onMounted(() => {
  fetchProjects(true)
})
</script>

<style lang="scss" scoped>
.explore-page {
  .page-header {
    margin-bottom: 24px;

    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 8px 0;
    }

    .page-description {
      color: var(--text-secondary);
      margin: 0;
    }
  }

  .filter-tabs {
    margin-bottom: 24px;

    :deep(.el-tabs__header) {
      margin: 0;
    }

    :deep(.el-tabs__nav-wrap::after) {
      background-color: var(--border-primary);
    }

    :deep(.el-tabs__item) {
      color: var(--text-secondary);

      &.is-active {
        color: var(--primary-color);
      }
    }

    :deep(.el-tabs__active-bar) {
      background-color: var(--primary-color);
    }
  }

  .search-bar {
    margin-bottom: 24px;

    .search-input {
      max-width: 400px;
    }
  }

  .projects-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 24px;
    margin-bottom: 32px;

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

        .card-overlay {
          opacity: 1;
        }

        .cover-image {
          transform: scale(1.05);
        }
      }

      .card-cover {
        position: relative;
        height: 200px;
        overflow: hidden;

        .cover-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s ease;
        }

        .card-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.7);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: opacity 0.3s ease;

          .overlay-content {
            text-align: center;

            .project-stats {
              margin-top: 12px;
              color: var(--text-primary);

              .stat-item {
                display: inline-flex;
                align-items: center;
                gap: 4px;
                font-size: 14px;
              }
            }
          }
        }

        .featured-badge {
          position: absolute;
          top: 12px;
          right: 12px;
          background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
          color: #000;
          padding: 4px 8px;
          border-radius: 12px;
          font-size: 12px;
          font-weight: 500;
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }

      .card-content {
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
          margin: 0 0 16px 0;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          line-height: 1.4;
          height: 2.8em;
        }

        .project-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .author-info {
            display: flex;
            align-items: center;
            gap: 8px;

            .author-name {
              font-size: 14px;
              color: var(--text-secondary);
            }
          }

          .project-date {
            font-size: 12px;
            color: var(--text-tertiary);
          }
        }
      }
    }
  }

  .empty-state {
    grid-column: 1 / -1;
    padding: 60px 20px;
  }

  .load-more {
    text-align: center;
    margin-bottom: 32px;
  }
}

.vr-preview-dialog {
  :deep(.el-dialog) {
    background: var(--bg-primary);
    border: 1px solid var(--border-primary);
  }

  :deep(.el-dialog__header) {
    background: var(--bg-secondary);
    border-bottom: 1px solid var(--border-primary);
  }

  :deep(.el-dialog__title) {
    color: var(--text-primary);
  }

  .vr-preview-container {
    height: 70vh;
    position: relative;

    .vr-iframe {
      width: 100%;
      height: 100%;
      border-radius: 8px;
    }
  }
}

// 响应式样式
@media (max-width: 768px) {
  .explore-page {
    .projects-grid {
      grid-template-columns: 1fr;
      gap: 16px;

      .project-card {
        .card-cover {
          height: 160px;
        }
      }
    }
  }

  .vr-preview-dialog {
    .vr-preview-container {
      height: 50vh;
    }
  }
}
</style>