<template>
  <div class="project-detail" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="header-info">
          <h1 class="page-title">{{ project.title || '项目详情' }}</h1>
          <p class="page-description">{{ project.description || '查看项目信息和管理设置' }}</p>
        </div>
      </div>
      <div class="header-right">
        <el-button @click="previewProject">
          <el-icon><View /></el-icon>
          预览
        </el-button>
        <el-button type="primary" @click="editProject">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-dropdown @command="handleCommand">
          <el-button>
            更多
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="share">
                <el-icon><Share /></el-icon>
                分享项目
              </el-dropdown-item>
              <el-dropdown-item command="duplicate">
                <el-icon><CopyDocument /></el-icon>
                复制项目
              </el-dropdown-item>
              <el-dropdown-item command="export">
                <el-icon><Download /></el-icon>
                导出项目
              </el-dropdown-item>
              <el-dropdown-item command="delete" divided>
                <el-icon><Delete /></el-icon>
                删除项目
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 项目信息卡片 -->
    <div class="content-grid">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        
        <div class="project-cover">
          <img 
            :src="project.coverImage || '/images/default-cover.jpg'" 
            :alt="project.title"
            class="cover-image"
          />
          <div class="cover-overlay">
            <el-button type="primary" @click="changeCover">
              <el-icon><Camera /></el-icon>
              更换封面
            </el-button>
          </div>
        </div>

        <div class="info-list">
          <div class="info-item">
            <span class="label">项目名称：</span>
            <span class="value">{{ project.title }}</span>
          </div>
          <div class="info-item">
            <span class="label">项目描述：</span>
            <span class="value">{{ project.description || '暂无描述' }}</span>
          </div>
          <div class="info-item">
            <span class="label">项目状态：</span>
            <el-tag :type="getStatusType(project.status)">
              {{ getStatusText(project.status) }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ formatDateTime(project.createdAt) }}</span>
          </div>
          <div class="info-item">
            <span class="label">更新时间：</span>
            <span class="value">{{ formatDateTime(project.updatedAt) }}</span>
          </div>
          <div class="info-item">
            <span class="label">浏览次数：</span>
            <span class="value">{{ project.viewCount || 0 }}</span>
          </div>
        </div>
      </el-card>

      <!-- 项目统计 -->
      <el-card class="stats-card">
        <template #header>
          <div class="card-header">
            <span>项目统计</span>
          </div>
        </template>
        
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-icon images-icon">
              <el-icon><Picture /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ project.imageCount || 0 }}</div>
              <div class="stat-label">图片数量</div>
            </div>
          </div>
          
          <div class="stat-item">
            <div class="stat-icon hotspots-icon">
              <el-icon><Location /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ project.hotspotCount || 0 }}</div>
              <div class="stat-label">热点数量</div>
            </div>
          </div>
          
          <div class="stat-item">
            <div class="stat-icon size-icon">
              <el-icon><FolderOpened /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ formatFileSize(project.totalSize || 0) }}</div>
              <div class="stat-label">项目大小</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 项目图片 -->
      <el-card class="images-card">
        <template #header>
          <div class="card-header">
            <span>项目图片</span>
            <el-button size="small" @click="uploadImages">
              <el-icon><Plus /></el-icon>
              添加图片
            </el-button>
          </div>
        </template>
        
        <div class="images-grid" v-if="project.images && project.images.length > 0">
          <div 
            v-for="image in project.images" 
            :key="image.id"
            class="image-item"
          >
            <img :src="image.processedUrl || image.originalUrl" :alt="image.name" />
            <div class="image-overlay">
              <el-button size="small" circle @click="previewImage(image)">
                <el-icon><View /></el-icon>
              </el-button>
              <el-button size="small" circle type="danger" @click="deleteImage(image.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
        
        <el-empty v-else description="还没有上传图片" :image-size="80">
          <el-button type="primary" @click="uploadImages">
            上传第一张图片
          </el-button>
        </el-empty>
      </el-card>

      <!-- 热点管理 -->
      <el-card class="hotspots-card">
        <template #header>
          <div class="card-header">
            <span>热点管理</span>
            <el-button size="small" @click="manageHotspots">
              <el-icon><Setting /></el-icon>
              管理热点
            </el-button>
          </div>
        </template>
        
        <div class="hotspots-list" v-if="project.hotspots && project.hotspots.length > 0">
          <div 
            v-for="hotspot in project.hotspots" 
            :key="hotspot.id"
            class="hotspot-item"
          >
            <div class="hotspot-icon">
              <el-icon><Location /></el-icon>
            </div>
            <div class="hotspot-info">
              <div class="hotspot-name">{{ hotspot.name }}</div>
              <div class="hotspot-type">{{ getHotspotTypeText(hotspot.type) }}</div>
            </div>
            <div class="hotspot-actions">
              <el-button size="small" @click="editHotspot(hotspot.id)">
                编辑
              </el-button>
            </div>
          </div>
        </div>
        
        <el-empty v-else description="还没有添加热点" :image-size="80">
          <el-button type="primary" @click="manageHotspots">
            添加第一个热点
          </el-button>
        </el-empty>
      </el-card>
    </div>

    <!-- 分享对话框 -->
    <el-dialog v-model="shareDialogVisible" title="分享项目" width="500px">
      <div class="share-content">
        <div class="share-item">
          <label>分享链接：</label>
          <el-input 
            v-model="shareUrl" 
            readonly
            class="share-input"
          >
            <template #append>
              <el-button @click="copyShareUrl">复制</el-button>
            </template>
          </el-input>
        </div>
        
        <div class="share-item">
          <label>二维码：</label>
          <div class="qr-code">
            <canvas ref="qrCanvas" width="150" height="150"></canvas>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="imagePreviewVisible" title="图片预览" width="80%">
      <div class="image-preview">
        <img :src="previewImageUrl" alt="预览图片" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, View, Edit, ArrowDown, Share, CopyDocument, 
  Download, Delete, Camera, Picture, Location, FolderOpened, 
  Plus, Setting 
} from '@element-plus/icons-vue'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const shareDialogVisible = ref(false)
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')
const shareUrl = ref('')
const qrCanvas = ref(null)

const project = reactive({
  id: null,
  title: '',
  description: '',
  status: 0,
  coverImage: '',
  createdAt: '',
  updatedAt: '',
  viewCount: 0,
  imageCount: 0,
  hotspotCount: 0,
  totalSize: 0,
  images: [],
  hotspots: []
})

// 方法
const fetchProjectDetail = async () => {
  const projectId = route.params.id
  if (!projectId) return

  loading.value = true
  try {
    const response = await api.get(`/projects/${projectId}`)
    const projectData = response.data.data
    
    Object.assign(project, projectData)
    
    // 生成分享链接
    shareUrl.value = `${window.location.origin}/vr/${projectData.shareToken}`
  } catch (error) {
    console.error('获取项目详情失败:', error)
    ElMessage.error('获取项目详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/projects')
}

const previewProject = () => {
  router.push(`/projects/${project.id}/vr-editor`)
}

const editProject = () => {
  router.push(`/projects/${project.id}/edit`)
}

const handleCommand = (command) => {
  switch (command) {
    case 'share':
      shareProject()
      break
    case 'duplicate':
      duplicateProject()
      break
    case 'export':
      exportProject()
      break
    case 'delete':
      deleteProject()
      break
  }
}

const shareProject = () => {
  shareDialogVisible.value = true
  nextTick(() => {
    generateQRCode()
  })
}

const duplicateProject = async () => {
  try {
    await api.post(`/projects/${project.id}/duplicate`)
    ElMessage.success('项目复制成功')
    router.push('/projects')
  } catch (error) {
    console.error('复制项目失败:', error)
    ElMessage.error('复制项目失败')
  }
}

const exportProject = async () => {
  try {
    const response = await api.get(`/projects/${project.id}/export`, {
      responseType: 'blob'
    })
    
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${project.title}.zip`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('项目导出成功')
  } catch (error) {
    console.error('导出项目失败:', error)
    ElMessage.error('导出项目失败')
  }
}

const deleteProject = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个项目吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await api.delete(`/projects/${project.id}`)
    ElMessage.success('项目删除成功')
    router.push('/projects')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除项目失败:', error)
      ElMessage.error('删除项目失败')
    }
  }
}

const changeCover = () => {
  // TODO: 实现更换封面功能
  ElMessage.info('更换封面功能即将上线')
}

const uploadImages = () => {
  // TODO: 实现图片上传功能
  ElMessage.info('图片上传功能即将上线')
}

const previewImage = (image) => {
  previewImageUrl.value = image.processedUrl || image.originalUrl
  imagePreviewVisible.value = true
}

const deleteImage = async (imageId) => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗？', '确认删除', {
      type: 'warning'
    })
    
    await api.delete(`/projects/${project.id}/images/${imageId}`)
    ElMessage.success('图片删除成功')
    
    // 重新获取项目详情
    fetchProjectDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除图片失败:', error)
      ElMessage.error('删除图片失败')
    }
  }
}

const manageHotspots = () => {
  router.push(`/projects/${project.id}/vr-editor`)
}

const editHotspot = (hotspotId) => {
  router.push(`/projects/${project.id}/vr-editor?hotspot=${hotspotId}`)
}

const copyShareUrl = async () => {
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    ElMessage.success('分享链接已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

const generateQRCode = () => {
  // TODO: 实现二维码生成
  if (qrCanvas.value) {
    const ctx = qrCanvas.value.getContext('2d')
    ctx.fillStyle = '#f0f0f0'
    ctx.fillRect(0, 0, 150, 150)
    ctx.fillStyle = '#666'
    ctx.font = '12px Arial'
    ctx.textAlign = 'center'
    ctx.fillText('二维码生成中...', 75, 75)
  }
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

const getHotspotTypeText = (type) => {
  const typeMap = {
    'info': '信息热点',
    'media': '媒体热点',
    'link': '链接热点',
    'audio': '音频热点'
  }
  return typeMap[type] || '未知类型'
}

const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 生命周期
onMounted(() => {
  fetchProjectDetail()
})
</script>

<style lang="scss" scoped>
.project-detail {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    gap: 20px;

    .header-left {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      flex: 1;

      .back-btn {
        margin-top: 4px;
      }

      .header-info {
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
    }

    .header-right {
      display: flex;
      gap: 12px;
    }
  }

  .content-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;

    .info-card {
      background: var(--bg-secondary);
      border: 1px solid var(--border-primary);

      .project-cover {
        position: relative;
        margin-bottom: 20px;
        border-radius: 8px;
        overflow: hidden;

        .cover-image {
          width: 100%;
          height: 200px;
          object-fit: cover;
        }

        .cover-overlay {
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

          &:hover {
            opacity: 1;
          }
        }
      }

      .info-list {
        .info-item {
          display: flex;
          margin-bottom: 12px;
          align-items: center;

          .label {
            font-weight: 500;
            color: var(--text-secondary);
            min-width: 80px;
          }

          .value {
            color: var(--text-primary);
            flex: 1;
          }
        }
      }
    }

    .stats-card {
      background: var(--bg-secondary);
      border: 1px solid var(--border-primary);

      .stats-grid {
        display: grid;
        gap: 16px;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 16px;
          background: var(--bg-tertiary);
          border-radius: 8px;

          .stat-icon {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;

            &.images-icon {
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              color: white;
            }

            &.hotspots-icon {
              background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
              color: white;
            }

            &.size-icon {
              background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
              color: white;
            }
          }

          .stat-info {
            .stat-number {
              font-size: 20px;
              font-weight: bold;
              color: var(--primary-color);
              margin-bottom: 4px;
            }

            .stat-label {
              font-size: 12px;
              color: var(--text-secondary);
            }
          }
        }
      }
    }

    .images-card,
    .hotspots-card {
      grid-column: 1 / -1;
      background: var(--bg-secondary);
      border: 1px solid var(--border-primary);
    }

    .images-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
      gap: 12px;

      .image-item {
        position: relative;
        aspect-ratio: 16/9;
        border-radius: 8px;
        overflow: hidden;
        cursor: pointer;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .image-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.7);
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          opacity: 0;
          transition: opacity 0.3s ease;

          &:hover {
            opacity: 1;
          }
        }
      }
    }

    .hotspots-list {
      .hotspot-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        background: var(--bg-tertiary);
        border-radius: 8px;
        margin-bottom: 8px;

        .hotspot-icon {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background: var(--primary-color);
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .hotspot-info {
          flex: 1;

          .hotspot-name {
            font-weight: 500;
            color: var(--text-primary);
            margin-bottom: 4px;
          }

          .hotspot-type {
            font-size: 12px;
            color: var(--text-secondary);
          }
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: var(--text-primary);
  }

  .share-content {
    .share-item {
      margin-bottom: 20px;

      label {
        display: block;
        margin-bottom: 8px;
        font-weight: 500;
        color: var(--text-primary);
      }

      .share-input {
        width: 100%;
      }

      .qr-code {
        display: flex;
        justify-content: center;
        padding: 20px;

        canvas {
          border: 1px solid var(--border-primary);
          border-radius: 8px;
        }
      }
    }
  }

  .image-preview {
    text-align: center;

    img {
      max-width: 100%;
      max-height: 70vh;
      object-fit: contain;
    }
  }
}

// 响应式样式
@media (max-width: 768px) {
  .project-detail {
    .page-header {
      flex-direction: column;
      gap: 16px;

      .header-left {
        flex-direction: column;
        gap: 12px;
      }

      .header-right {
        align-self: stretch;
        justify-content: space-between;
      }
    }

    .content-grid {
      grid-template-columns: 1fr;

      .images-card,
      .hotspots-card {
        grid-column: 1;
      }
    }

    .images-grid {
      grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    }
  }
}
</style>