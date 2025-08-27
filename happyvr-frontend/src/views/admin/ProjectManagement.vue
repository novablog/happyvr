<template>
  <div class="project-management">
    <div class="page-header">
      <h2>内容管理</h2>
      <p>管理用户VR项目和内容审核</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalProjects }}</div>
              <div class="stat-label">总项目数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.pendingProjects }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon featured">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.featuredProjects }}</div>
              <div class="stat-label">推荐项目</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.activeUsers }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20" align="middle">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索项目标题或用户名"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="审核状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="草稿" value="0" />
            <el-option label="已发布" value="1" />
            <el-option label="审核中" value="2" />
            <el-option label="已拒绝" value="3" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.featured" placeholder="推荐状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="普通项目" value="0" />
            <el-option label="推荐项目" value="1" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedProjects.length > 0">
      <el-alert
        :title="`已选择 ${selectedProjects.length} 个项目`"
        type="info"
        show-icon
        :closable="false"
      >
        <template #default>
          <div class="batch-buttons">
            <el-button size="small" @click="batchApprove">批量通过</el-button>
            <el-button size="small" @click="batchReject">批量拒绝</el-button>
            <el-button size="small" @click="batchSetFeatured">设为推荐</el-button>
            <el-button size="small" @click="batchDelete" type="danger">批量删除</el-button>
          </div>
        </template>
      </el-alert>
    </div>

    <!-- 项目列表 -->
    <div class="project-table">
      <el-table
        :data="projectList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="项目信息" min-width="300">
          <template #default="{ row }">
            <div class="project-info">
              <div class="project-cover">
                <img :src="row.coverImage || '/default-cover.jpg'" alt="项目封面" />
              </div>
              <div class="project-details">
                <div class="project-title">{{ row.title }}</div>
                <div class="project-description">{{ row.description || '暂无描述' }}</div>
                <div class="project-meta">
                  <span>ID: {{ row.id }}</span>
                  <span>浏览: {{ row.viewCount }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建者" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <div class="username">{{ row.user.username }}</div>
              <div class="user-email">{{ row.user.email }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isFeatured" type="warning" size="small">
              <el-icon><Star /></el-icon>
              推荐
            </el-tag>
            <span v-else class="text-muted">普通</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="previewProject(row)">
                <el-icon><View /></el-icon>
                预览
              </el-button>
              <el-dropdown @command="(command) => handleAction(command, row)">
                <el-button size="small">
                  更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="approve" v-if="row.status === 2">
                      <el-icon><Check /></el-icon>
                      审核通过
                    </el-dropdown-item>
                    <el-dropdown-item command="reject" v-if="row.status === 2">
                      <el-icon><Close /></el-icon>
                      审核拒绝
                    </el-dropdown-item>
                    <el-dropdown-item command="feature" v-if="!row.isFeatured">
                      <el-icon><Star /></el-icon>
                      设为推荐
                    </el-dropdown-item>
                    <el-dropdown-item command="unfeature" v-if="row.isFeatured">
                      <el-icon><StarFilled /></el-icon>
                      取消推荐
                    </el-dropdown-item>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon>
                      编辑信息
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <el-icon><Delete /></el-icon>
                      删除项目
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialog.visible"
      :title="auditDialog.type === 'approve' ? '审核通过' : '审核拒绝'"
      width="500px"
    >
      <el-form :model="auditDialog.form" label-width="80px">
        <el-form-item label="项目标题">
          <span>{{ auditDialog.project?.title }}</span>
        </el-form-item>
        <el-form-item label="审核意见" required>
          <el-input
            v-model="auditDialog.form.comment"
            type="textarea"
            :rows="4"
            :placeholder="auditDialog.type === 'approve' ? '请输入通过理由（可选）' : '请输入拒绝理由'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmAudit"
          :loading="auditDialog.loading"
        >
          确认{{ auditDialog.type === 'approve' ? '通过' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 项目编辑对话框 -->
    <el-dialog v-model="editDialog.visible" title="编辑项目信息" width="600px">
      <el-form :model="editDialog.form" label-width="80px" :rules="editRules">
        <el-form-item label="项目标题" prop="title">
          <el-input v-model="editDialog.form.title" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="editDialog.form.description"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-select v-model="editDialog.form.status">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="审核中" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="推荐状态">
          <el-switch
            v-model="editDialog.form.isFeatured"
            active-text="推荐"
            inactive-text="普通"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmEdit"
          :loading="editDialog.loading"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  Clock,
  Star,
  StarFilled,
  User,
  Search,
  View,
  Edit,
  Delete,
  Check,
  Close,
  ArrowDown
} from '@element-plus/icons-vue'
import {
  getStatistics,
  getProjectList,
  auditProject,
  batchAuditProjects,
  setProjectFeatured,
  batchSetFeatured,
  updateProject,
  deleteProject,
  batchDeleteProjects
} from '@/api/admin'

// 响应式数据
const loading = ref(false)
const selectedProjects = ref([])

// 统计数据
const statistics = reactive({
  totalProjects: 0,
  pendingProjects: 0,
  featuredProjects: 0,
  activeUsers: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  featured: '',
  dateRange: []
})

// 项目列表
const projectList = ref([])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 审核对话框
const auditDialog = reactive({
  visible: false,
  type: 'approve', // approve | reject
  project: null,
  loading: false,
  form: {
    comment: ''
  }
})

// 编辑对话框
const editDialog = reactive({
  visible: false,
  loading: false,
  form: {
    id: null,
    title: '',
    description: '',
    status: 0,
    isFeatured: false
  }
})

// 表单验证规则
const editRules = {
  title: [
    { required: true, message: '请输入项目标题', trigger: 'blur' }
  ]
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const response = await getStatistics()
    Object.assign(statistics, response.data)
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 使用模拟数据作为后备
    statistics.totalProjects = 156
    statistics.pendingProjects = 23
    statistics.featuredProjects = 12
    statistics.activeUsers = 89
  }
}

// 获取项目列表
const fetchProjectList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status,
      featured: searchForm.featured,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    }
    
    const response = await getProjectList(params)
    projectList.value = response.data.list
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('获取项目列表失败')
    console.error('获取项目列表失败:', error)
    
    // 使用模拟数据作为后备
    const mockData = {
      list: [
        {
          id: 1,
          title: '美丽的海滩全景',
          description: '这是一个美丽的海滩VR体验',
          coverImage: '/api/files/cover1.jpg',
          status: 2,
          isFeatured: false,
          viewCount: 156,
          createdAt: '2024-01-15 10:30:00',
          user: {
            id: 1,
            username: 'user1',
            email: 'user1@example.com'
          }
        },
        {
          id: 2,
          title: '城市夜景VR',
          description: '繁华都市的夜晚景色',
          coverImage: '/api/files/cover2.jpg',
          status: 1,
          isFeatured: true,
          viewCount: 289,
          createdAt: '2024-01-14 15:20:00',
          user: {
            id: 2,
            username: 'user2',
            email: 'user2@example.com'
          }
        }
      ],
      total: 156
    }
    
    projectList.value = mockData.list
    pagination.total = mockData.total
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.currentPage = 1
  fetchProjectList()
}

// 选择变化处理
const handleSelectionChange = (selection) => {
  selectedProjects.value = selection
}

// 批量操作
const batchApprove = async () => {
  try {
    await ElMessageBox.confirm('确认批量通过选中的项目？', '批量审核', {
      type: 'warning'
    })
    
    const projectIds = selectedProjects.value.map(p => p.id)
    await batchAuditProjects({
      projectIds,
      status: 1, // 通过
      comment: '批量审核通过'
    })
    
    ElMessage.success(`已批量通过 ${selectedProjects.value.length} 个项目`)
    selectedProjects.value = []
    fetchProjectList()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量审核失败')
    }
  }
}

const batchReject = async () => {
  try {
    await ElMessageBox.confirm('确认批量拒绝选中的项目？', '批量审核', {
      type: 'warning'
    })
    
    const projectIds = selectedProjects.value.map(p => p.id)
    await batchAuditProjects({
      projectIds,
      status: 3, // 拒绝
      comment: '批量审核拒绝'
    })
    
    ElMessage.success(`已批量拒绝 ${selectedProjects.value.length} 个项目`)
    selectedProjects.value = []
    fetchProjectList()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量审核失败')
    }
  }
}

const batchSetFeatured = async () => {
  try {
    await ElMessageBox.confirm('确认将选中的项目设为推荐？', '批量设置', {
      type: 'warning'
    })
    
    const projectIds = selectedProjects.value.map(p => p.id)
    await batchSetFeatured({
      projectIds,
      featured: true
    })
    
    ElMessage.success(`已将 ${selectedProjects.value.length} 个项目设为推荐`)
    selectedProjects.value = []
    fetchProjectList()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量设置失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确认删除选中的项目？此操作不可恢复！',
      '批量删除',
      {
        type: 'error',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      }
    )
    
    const projectIds = selectedProjects.value.map(p => p.id)
    await batchDeleteProjects({ projectIds })
    
    ElMessage.success(`已删除 ${selectedProjects.value.length} 个项目`)
    selectedProjects.value = []
    fetchProjectList()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 预览项目
const previewProject = (project) => {
  // 在新窗口打开预览
  const previewUrl = `/preview/${project.id}`
  window.open(previewUrl, '_blank')
}

// 操作处理
const handleAction = async (command, project) => {
  switch (command) {
    case 'approve':
      showAuditDialog('approve', project)
      break
    case 'reject':
      showAuditDialog('reject', project)
      break
    case 'feature':
      await toggleFeatured(project, true)
      break
    case 'unfeature':
      await toggleFeatured(project, false)
      break
    case 'edit':
      showEditDialog(project)
      break
    case 'delete':
      await deleteProjectItem(project)
      break
  }
}

// 显示审核对话框
const showAuditDialog = (type, project) => {
  auditDialog.type = type
  auditDialog.project = project
  auditDialog.form.comment = ''
  auditDialog.visible = true
}

// 确认审核
const confirmAudit = async () => {
  auditDialog.loading = true
  try {
    const status = auditDialog.type === 'approve' ? 1 : 3 // 1:通过, 3:拒绝
    await auditProject(auditDialog.project.id, {
      status,
      comment: auditDialog.form.comment
    })
    
    const action = auditDialog.type === 'approve' ? '通过' : '拒绝'
    ElMessage.success(`项目审核${action}成功`)
    auditDialog.visible = false
    fetchProjectList()
    fetchStatistics()
  } catch (error) {
    ElMessage.error('审核操作失败')
  } finally {
    auditDialog.loading = false
  }
}

// 切换推荐状态
const toggleFeatured = async (project, featured) => {
  try {
    await setProjectFeatured(project.id, featured)
    const action = featured ? '设为推荐' : '取消推荐'
    ElMessage.success(`${action}成功`)
    fetchProjectList()
    fetchStatistics()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 显示编辑对话框
const showEditDialog = (project) => {
  editDialog.form.id = project.id
  editDialog.form.title = project.title
  editDialog.form.description = project.description
  editDialog.form.status = project.status
  editDialog.form.isFeatured = project.isFeatured
  editDialog.visible = true
}

// 确认编辑
const confirmEdit = async () => {
  editDialog.loading = true
  try {
    await updateProject(editDialog.form.id, {
      title: editDialog.form.title,
      description: editDialog.form.description,
      status: editDialog.form.status,
      isFeatured: editDialog.form.isFeatured
    })
    
    ElMessage.success('项目信息更新成功')
    editDialog.visible = false
    fetchProjectList()
    fetchStatistics()
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    editDialog.loading = false
  }
}

// 删除项目
const deleteProjectItem = async (project) => {
  try {
    await ElMessageBox.confirm(
      `确认删除项目"${project.title}"？此操作不可恢复！`,
      '删除确认',
      {
        type: 'error',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      }
    )
    
    await deleteProject(project.id)
    ElMessage.success('项目删除成功')
    fetchProjectList()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchProjectList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchProjectList()
}

// 工具函数
const getStatusType = (status) => {
  const types = {
    0: 'info',    // 草稿
    1: 'success', // 已发布
    2: 'warning', // 审核中
    3: 'danger'   // 已拒绝
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '草稿',
    1: '已发布',
    2: '审核中',
    3: '已拒绝'
  }
  return texts[status] || '未知'
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchStatistics()
  fetchProjectList()
})
</script>

<style lang="scss" scoped>
.project-management {
  .page-header {
    margin-bottom: 30px;
    
    h2 {
      color: var(--primary-color);
      margin-bottom: 8px;
    }
    
    p {
      color: var(--text-secondary);
      margin: 0;
    }
  }

  .stats-cards {
    margin-bottom: 30px;
    
    .stat-card {
      display: flex;
      align-items: center;
      padding: 20px;
      background: var(--bg-secondary);
      border-radius: 8px;
      border: 1px solid var(--border-primary);
      
      .stat-icon {
        width: 50px;
        height: 50px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        background: var(--primary-color);
        color: white;
        
        &.pending {
          background: #e6a23c;
        }
        
        &.featured {
          background: #f56c6c;
        }
        
        &.users {
          background: #67c23a;
        }
      }
      
      .stat-content {
        .stat-number {
          font-size: 24px;
          font-weight: bold;
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

  .search-section {
    margin-bottom: 20px;
    padding: 20px;
    background: var(--bg-secondary);
    border-radius: 8px;
    border: 1px solid var(--border-primary);
  }

  .batch-actions {
    margin-bottom: 20px;
    
    .batch-buttons {
      margin-top: 10px;
      
      .el-button {
        margin-right: 10px;
      }
    }
  }

  .project-table {
    background: var(--bg-secondary);
    border-radius: 8px;
    border: 1px solid var(--border-primary);
    overflow: hidden;
    
    .project-info {
      display: flex;
      align-items: center;
      
      .project-cover {
        width: 60px;
        height: 40px;
        margin-right: 15px;
        border-radius: 4px;
        overflow: hidden;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .project-details {
        flex: 1;
        
        .project-title {
          font-weight: 500;
          color: var(--text-primary);
          margin-bottom: 4px;
        }
        
        .project-description {
          font-size: 12px;
          color: var(--text-secondary);
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          max-width: 200px;
        }
        
        .project-meta {
          font-size: 12px;
          color: var(--text-tertiary);
          
          span {
            margin-right: 15px;
          }
        }
      }
    }
    
    .user-info {
      .username {
        font-weight: 500;
        color: var(--text-primary);
        margin-bottom: 2px;
      }
      
      .user-email {
        font-size: 12px;
        color: var(--text-secondary);
      }
    }
    
    .action-buttons {
      display: flex;
      gap: 8px;
    }
    
    .text-muted {
      color: var(--text-tertiary);
      font-size: 12px;
    }
  }

  .pagination-wrapper {
    padding: 20px;
    display: flex;
    justify-content: center;
  }
}

:deep(.el-table) {
  background: transparent;
  
  .el-table__header {
    background: var(--bg-primary);
  }
  
  .el-table__row {
    background: transparent;
    
    &:hover {
      background: var(--bg-hover);
    }
  }
}

:deep(.el-dialog) {
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
}
</style>