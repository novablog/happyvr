<template>
  <div class="project-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">我的项目</h1>
        <p class="page-description">管理和编辑您的VR项目</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="createProject">
          <el-icon><Plus /></el-icon>
          创建项目
        </el-button>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-checkbox 
          v-model="selectAll" 
          @change="handleSelectAll"
          :indeterminate="isIndeterminate"
          class="select-all-checkbox"
        >
          全选
        </el-checkbox>
        
        <el-input
          v-model="searchKeyword"
          placeholder="搜索项目..."
          clearable
          @input="handleSearch"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="statusFilter"
          placeholder="状态筛选"
          clearable
          @change="handleFilter"
          class="status-filter"
        >
          <el-option label="全部" value="" />
          <el-option label="草稿" value="0" />
          <el-option label="已发布" value="1" />
          <el-option label="审核中" value="2" />
        </el-select>

        <el-select
          v-model="sortBy"
          @change="handleSort"
          class="sort-select"
        >
          <el-option label="按更新时间" value="updatedAt" />
          <el-option label="按创建时间" value="createdAt" />
          <el-option label="按标题" value="title" />
          <el-option label="按浏览量" value="viewCount" />
        </el-select>
      </div>
      
      <div class="filter-right">
        <!-- 批量操作 -->
        <div class="batch-actions" v-if="selectedProjects.length > 0">
          <el-dropdown @command="handleBatchCommand">
            <el-button>
              批量操作 ({{ selectedProjects.length }})
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="publish">批量发布</el-dropdown-item>
                <el-dropdown-item command="unpublish">批量取消发布</el-dropdown-item>
                <el-dropdown-item command="export">批量导出</el-dropdown-item>
                <el-dropdown-item command="delete" divided>批量删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <el-radio-group v-model="viewMode" @change="handleViewModeChange">
          <el-radio-button label="grid">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
          <el-radio-button label="list">
            <el-icon><List /></el-icon>
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 项目列表 -->
    <div class="project-content" v-loading="loading">
      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="project-grid">
        <div
          v-for="project in projects"
          :key="project.id"
          class="project-card"
          :class="{ selected: selectedProjects.includes(project.id) }"
          @click="viewProject(project.id)"
        >
          <div class="card-header">
            <el-checkbox 
              :model-value="selectedProjects.includes(project.id)"
              @change="(checked) => handleProjectSelect(project.id, checked)"
              @click.stop
            />
            <el-dropdown @command="(command) => handleProjectCommand(command, project)" @click.stop>
              <el-button text>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="duplicate">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-dropdown-item>
                  <el-dropdown-item command="share">
                    <el-icon><Share /></el-icon>
                    分享
                  </el-dropdown-item>
                  <el-dropdown-item command="export">
                    <el-icon><Download /></el-icon>
                    导出
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div class="card-cover">
            <img
              :src="project.coverImage || '/images/default-cover.jpg'"
              :alt="project.title"
              class="cover-image"
            />
            <div class="card-overlay">
              <div class="overlay-actions">
                <el-button
                  type="primary"
                  circle
                  @click.stop="editProject(project.id)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button
                  type="success"
                  circle
                  @click.stop="previewProject(project.id)"
                >
                  <el-icon><View /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
          
          <div class="card-content">
            <h3 class="project-title">{{ project.title }}</h3>
            <p class="project-description">{{ project.description || '暂无描述' }}</p>
            
            <div class="project-meta">
              <el-tag :type="getStatusType(project.status)" size="small">
                {{ getStatusText(project.status) }}
              </el-tag>
              <span class="project-date">
                {{ formatDate(project.updatedAt) }}
              </span>
            </div>

            <div class="project-stats">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ project.viewCount || 0 }}
              </span>
              <span class="stat-item">
                <el-icon><Picture /></el-icon>
                {{ project.imageCount || 0 }}
              </span>
              <span class="stat-item">
                <el-icon><Location /></el-icon>
                {{ project.hotspotCount || 0 }}
              </span>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="projects.length === 0 && !loading" class="empty-state">
          <el-empty description="还没有项目">
            <el-button type="primary" @click="createProject">
              创建第一个项目
            </el-button>
          </el-empty>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="project-table">
        <el-table :data="projects" style="width: 100%">
          <el-table-column prop="title" label="项目名称" min-width="200">
            <template #default="{ row }">
              <div class="project-info">
                <img
                  :src="row.coverImage || '/images/default-cover.jpg'"
                  :alt="row.title"
                  class="table-cover"
                />
                <div>
                  <div class="project-title">{{ row.title }}</div>
                  <div class="project-description">{{ row.description || '暂无描述' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="updatedAt" label="更新时间" width="150">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="viewProject(row.id)">
                查看
              </el-button>
              <el-button size="small" type="primary" @click="editProject(row.id)">
                编辑
              </el-button>
              <el-button size="small" type="success" @click="previewProject(row.id)">
                预览
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Grid, List, Edit, View, MoreFilled, 
  CopyDocument, Share, Download, Delete, ArrowDown,
  Picture, Location 
} from '@element-plus/icons-vue'
import api from '@/utils/api'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const projects = ref([])
const searchKeyword = ref('')
const statusFilter = ref('')
const sortBy = ref('updatedAt')
const viewMode = ref('grid')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const selectedProjects = ref([])
const selectAll = ref(false)

// 计算属性
const isIndeterminate = computed(() => {
  const selectedCount = selectedProjects.value.length
  const totalCount = projects.value.length
  return selectedCount > 0 && selectedCount < totalCount
})

// 方法
const fetchProjects = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchKeyword.value,
      status: statusFilter.value,
      sortBy: sortBy.value,
      sortDir: 'desc'
    }
    
    const response = await api.get('/projects/my', params)
    const { content, totalElements } = response.data.data
    
    projects.value = content
    total.value = totalElements
    
    // 清空选择状态
    selectedProjects.value = []
    selectAll.value = false
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error('获取项目列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchProjects()
}

const handleFilter = () => {
  currentPage.value = 1
  fetchProjects()
}

const handleSort = () => {
  currentPage.value = 1
  fetchProjects()
}

const handleViewModeChange = () => {
  // 保存视图模式到本地存储
  localStorage.setItem('projectViewMode', viewMode.value)
}

const handleSelectAll = (checked) => {
  if (checked) {
    selectedProjects.value = projects.value.map(p => p.id)
  } else {
    selectedProjects.value = []
  }
}

const handleProjectSelect = (projectId, checked) => {
  if (checked) {
    selectedProjects.value.push(projectId)
  } else {
    const index = selectedProjects.value.indexOf(projectId)
    if (index > -1) {
      selectedProjects.value.splice(index, 1)
    }
  }
  
  // 更新全选状态
  selectAll.value = selectedProjects.value.length === projects.value.length
}

const handleProjectCommand = async (command, project) => {
  switch (command) {
    case 'edit':
      editProject(project.id)
      break
    case 'duplicate':
      await duplicateProject(project.id)
      break
    case 'share':
      shareProject(project)
      break
    case 'export':
      await exportProject(project.id)
      break
    case 'delete':
      await deleteProject(project.id)
      break
  }
}

const handleBatchCommand = async (command) => {
  if (selectedProjects.value.length === 0) {
    ElMessage.warning('请先选择项目')
    return
  }

  switch (command) {
    case 'publish':
      await batchUpdateStatus(1)
      break
    case 'unpublish':
      await batchUpdateStatus(0)
      break
    case 'export':
      await batchExport()
      break
    case 'delete':
      await batchDelete()
      break
  }
}

const duplicateProject = async (projectId) => {
  try {
    await api.post(`/projects/${projectId}/duplicate`)
    ElMessage.success('项目复制成功')
    fetchProjects()
  } catch (error) {
    console.error('复制项目失败:', error)
    ElMessage.error('复制项目失败')
  }
}

const shareProject = (project) => {
  const shareUrl = `${window.location.origin}/vr/${project.shareToken}`
  
  // 复制到剪贴板
  navigator.clipboard.writeText(shareUrl).then(() => {
    ElMessage.success('分享链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const exportProject = async (projectId) => {
  try {
    const response = await api.get(`/projects/${projectId}/export`, {
      responseType: 'blob'
    })
    
    const project = projects.value.find(p => p.id === projectId)
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${project?.title || 'project'}.zip`
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

const deleteProject = async (projectId) => {
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
    
    await api.delete(`/projects/${projectId}`)
    ElMessage.success('项目删除成功')
    fetchProjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除项目失败:', error)
      ElMessage.error('删除项目失败')
    }
  }
}

const batchUpdateStatus = async (status) => {
  try {
    const statusText = status === 1 ? '发布' : '取消发布'
    await ElMessageBox.confirm(
      `确定要${statusText}选中的 ${selectedProjects.value.length} 个项目吗？`,
      `批量${statusText}`,
      {
        confirmButtonText: `确定${statusText}`,
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await api.put('/projects/batch/status', {
      projectIds: selectedProjects.value,
      status
    })
    
    ElMessage.success(`批量${statusText}成功`)
    fetchProjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量更新状态失败:', error)
      ElMessage.error('批量更新状态失败')
    }
  }
}

const batchExport = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要导出选中的 ${selectedProjects.value.length} 个项目吗？`,
      '批量导出',
      {
        confirmButtonText: '确定导出',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    const response = await api.post('/projects/batch/export', {
      projectIds: selectedProjects.value
    }, {
      responseType: 'blob'
    })
    
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `projects_${Date.now()}.zip`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('批量导出成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量导出失败:', error)
      ElMessage.error('批量导出失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedProjects.value.length} 个项目吗？删除后无法恢复。`,
      '批量删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await api.delete('/projects/batch', {
      data: { projectIds: selectedProjects.value }
    })
    
    ElMessage.success('批量删除成功')
    fetchProjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchProjects()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchProjects()
}

const createProject = () => {
  router.push('/projects/create')
}

const viewProject = (id) => {
  router.push(`/projects/${id}`)
}

const editProject = (id) => {
  router.push(`/projects/${id}/edit`)
}

const previewProject = (id) => {
  router.push(`/projects/${id}/vr-editor`)
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
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  // 恢复视图模式
  const savedViewMode = localStorage.getItem('projectViewMode')
  if (savedViewMode) {
    viewMode.value = savedViewMode
  }
  
  fetchProjects()
})
</script>

<style lang="scss" scoped>
.project-list {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;

    .header-left {
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

  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    gap: 16px;

    .filter-left {
      display: flex;
      gap: 12px;
      flex: 1;
      align-items: center;

      .select-all-checkbox {
        white-space: nowrap;
      }

      .search-input {
        max-width: 300px;
      }

      .status-filter,
      .sort-select {
        width: 120px;
      }
    }

    .filter-right {
      display: flex;
      gap: 12px;
      align-items: center;

      .batch-actions {
        .el-dropdown {
          margin-right: 12px;
        }
      }

      .el-radio-group {
        :deep(.el-radio-button__inner) {
          padding: 8px 12px;
        }
      }
    }
  }

  .project-grid {
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

      &.selected {
        border-color: var(--primary-color);
        box-shadow: 0 0 0 2px rgba(212, 175, 55, 0.2);
      }

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
        border-color: var(--primary-color);

        .card-overlay {
          opacity: 1;
        }
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px 0;
      }

      .card-cover {
        position: relative;
        height: 180px;
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

          .overlay-actions {
            display: flex;
            gap: 12px;
          }
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
          margin: 0 0 12px 0;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .project-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;

          .project-date {
            font-size: 12px;
            color: var(--text-tertiary);
          }
        }

        .project-stats {
          display: flex;
          gap: 16px;

          .stat-item {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            color: var(--text-tertiary);

            .el-icon {
              font-size: 14px;
            }
          }
        }
      }
    }
  }

  .project-table {
    .project-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .table-cover {
        width: 48px;
        height: 32px;
        object-fit: cover;
        border-radius: 4px;
      }

      .project-title {
        font-weight: 500;
        color: var(--text-primary);
        margin-bottom: 4px;
      }

      .project-description {
        font-size: 12px;
        color: var(--text-secondary);
      }
    }
  }

  .empty-state {
    grid-column: 1 / -1;
    padding: 60px 20px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 32px;
  }
}

// 响应式样式
@media (max-width: 768px) {
  .project-list {
    .page-header {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;
    }

    .filter-bar {
      flex-direction: column;
      gap: 12px;

      .filter-left {
        flex-wrap: wrap;
        gap: 8px;

        .search-input {
          max-width: none;
          flex: 1;
          min-width: 200px;
        }

        .status-filter,
        .sort-select {
          width: auto;
          min-width: 100px;
        }
      }

      .filter-right {
        justify-content: space-between;

        .batch-actions {
          .el-dropdown {
            margin-right: 0;
          }
        }
      }
    }

    .project-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>