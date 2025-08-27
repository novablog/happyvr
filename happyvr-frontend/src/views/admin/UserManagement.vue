<template>
  <div class="user-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>用户管理</h2>
        <p>管理平台用户信息和状态</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateUser">
          <el-icon><Plus /></el-icon>
          新建用户
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="搜索用户">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名、邮箱、手机号"
            clearable
            style="width: 250px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="用户状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="正常" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="用户角色">
          <el-select v-model="searchForm.role" placeholder="全部角色" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option 
              v-for="role in roles" 
              :key="role.id" 
              :label="role.name" 
              :value="role.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="注册时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedUsers.length > 0">
      <div class="selected-info">
        已选择 {{ selectedUsers.length }} 个用户
      </div>
      <div class="actions">
        <el-button size="small" @click="batchEnable">批量启用</el-button>
        <el-button size="small" @click="batchDisable">批量禁用</el-button>
        <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
      </div>
    </div>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="users"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="40" :src="row.avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="user-details">
                <div class="username">{{ row.username }}</div>
                <div class="email">{{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="phone" label="手机号" width="130" />
        
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roles"
              :key="role.id"
              size="small"
              :type="getRoleTagType(role.name)"
            >
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="项目数" width="80">
          <template #default="{ row }">
            <span class="project-count">{{ row.projectCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="lastLoginAt" label="最后登录" width="160">
          <template #default="{ row }">
            <span v-if="row.lastLoginAt">{{ formatDateTime(row.lastLoginAt) }}</span>
            <span v-else class="never-login">从未登录</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewUser(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button size="small" @click="editUser(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-dropdown @command="(command) => handleUserAction(command, row)">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="roles">分配角色</el-dropdown-item>
                  <el-dropdown-item command="reset-password">重置密码</el-dropdown-item>
                  <el-dropdown-item command="login-log">登录日志</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除用户</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="userDetailVisible"
      :title="currentUser?.username + ' - 用户详情'"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="user-detail" v-if="currentUser">
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ currentUser.phone || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
                {{ currentUser.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatDateTime(currentUser.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="最后登录">
              {{ currentUser.lastLoginAt ? formatDateTime(currentUser.lastLoginAt) : '从未登录' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="detail-section">
          <h4>角色权限</h4>
          <div class="roles-list">
            <el-tag
              v-for="role in currentUser.roles"
              :key="role.id"
              size="large"
              :type="getRoleTagType(role.name)"
            >
              {{ role.name }}
            </el-tag>
            <span v-if="!currentUser.roles?.length" class="no-roles">未分配角色</span>
          </div>
        </div>
        
        <div class="detail-section">
          <h4>统计信息</h4>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ currentUser.projectCount || 0 }}</div>
              <div class="stat-label">创建项目</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ currentUser.viewCount || 0 }}</div>
              <div class="stat-label">浏览次数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ currentUser.loginCount || 0 }}</div>
              <div class="stat-label">登录次数</div>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="userDetailVisible = false">关闭</el-button>
        <el-button type="primary" @click="editUser(currentUser)">编辑用户</el-button>
      </template>
    </el-dialog>

    <!-- 用户编辑对话框 -->
    <el-dialog
      v-model="userEditVisible"
      :title="isCreateMode ? '新建用户' : '编辑用户'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="!isCreateMode" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="isCreateMode">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="userEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveUser">
          {{ isCreateMode ? '创建' : '保存' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 角色分配对话框 -->
    <el-dialog
      v-model="roleAssignVisible"
      title="分配角色"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="role-assign">
        <p>为用户 <strong>{{ currentUser?.username }}</strong> 分配角色：</p>
        <el-checkbox-group v-model="selectedRoles">
          <el-checkbox
            v-for="role in roles"
            :key="role.id"
            :label="role.id"
            :value="role.id"
          >
            {{ role.name }}
          </el-checkbox>
        </el-checkbox-group>
      </div>
      
      <template #footer>
        <el-button @click="roleAssignVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveUserRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, UserFilled, View, Edit, ArrowDown
} from '@element-plus/icons-vue'
import api from '@/utils/api'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const users = ref([])
const roles = ref([])
const selectedUsers = ref([])
const currentUser = ref(null)

// 对话框显示状态
const userDetailVisible = ref(false)
const userEditVisible = ref(false)
const roleAssignVisible = ref(false)
const isCreateMode = ref(false)

// 表单引用
const userFormRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  role: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 用户表单
const userForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  status: 1
})

// 角色分配
const selectedRoles = ref([])

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 方法
const loadUsers = async () => {
  try {
    loading.value = true
    
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword,
      status: searchForm.status,
      role: searchForm.role,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    }
    
    const response = await api.get('/admin/users', { params })
    const { data } = response.data
    
    users.value = data.list || []
    pagination.total = data.total || 0
    
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  try {
    const response = await api.get('/admin/roles')
    roles.value = response.data.data || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadUsers()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    role: '',
    dateRange: []
  })
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

const handleStatusChange = async (user) => {
  try {
    await api.put(`/admin/users/${user.id}/status`, {
      status: user.status
    })
    
    ElMessage.success(`用户已${user.status === 1 ? '启用' : '禁用'}`)
  } catch (error) {
    // 恢复原状态
    user.status = user.status === 1 ? 0 : 1
    console.error('更新用户状态失败:', error)
    ElMessage.error('更新用户状态失败')
  }
}

const handlePageChange = (page) => {
  pagination.page = page
  loadUsers()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadUsers()
}

const showCreateUser = () => {
  isCreateMode.value = true
  resetUserForm()
  userEditVisible.value = true
}

const viewUser = async (user) => {
  try {
    const response = await api.get(`/admin/users/${user.id}`)
    currentUser.value = response.data.data
    userDetailVisible.value = true
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
}

const editUser = (user) => {
  isCreateMode.value = false
  currentUser.value = user
  Object.assign(userForm, {
    username: user.username,
    email: user.email,
    phone: user.phone || '',
    password: '',
    status: user.status
  })
  userEditVisible.value = true
}

const resetUserForm = () => {
  Object.assign(userForm, {
    username: '',
    email: '',
    phone: '',
    password: '',
    status: 1
  })
  
  if (userFormRef.value) {
    userFormRef.value.clearValidate()
  }
}

const saveUser = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    saving.value = true
    
    if (isCreateMode.value) {
      await api.post('/admin/users', userForm)
      ElMessage.success('用户创建成功')
    } else {
      await api.put(`/admin/users/${currentUser.value.id}`, userForm)
      ElMessage.success('用户更新成功')
    }
    
    userEditVisible.value = false
    loadUsers()
    
  } catch (error) {
    console.error('保存用户失败:', error)
    ElMessage.error('保存用户失败')
  } finally {
    saving.value = false
  }
}

const handleUserAction = async (command, user) => {
  currentUser.value = user
  
  switch (command) {
    case 'roles':
      await showRoleAssign(user)
      break
    case 'reset-password':
      await resetUserPassword(user)
      break
    case 'login-log':
      showLoginLog(user)
      break
    case 'delete':
      await deleteUser(user)
      break
  }
}

const showRoleAssign = async (user) => {
  try {
    const response = await api.get(`/admin/users/${user.id}/roles`)
    selectedRoles.value = response.data.data.map(role => role.id)
    roleAssignVisible.value = true
  } catch (error) {
    console.error('获取用户角色失败:', error)
    ElMessage.error('获取用户角色失败')
  }
}

const saveUserRoles = async () => {
  try {
    saving.value = true
    
    await api.put(`/admin/users/${currentUser.value.id}/roles`, {
      roleIds: selectedRoles.value
    })
    
    ElMessage.success('角色分配成功')
    roleAssignVisible.value = false
    loadUsers()
    
  } catch (error) {
    console.error('分配角色失败:', error)
    ElMessage.error('分配角色失败')
  } finally {
    saving.value = false
  }
}

const resetUserPassword = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 ${user.username} 的密码吗？新密码将发送到用户邮箱。`,
      '重置密码',
      { type: 'warning' }
    )
    
    await api.post(`/admin/users/${user.id}/reset-password`)
    ElMessage.success('密码重置成功，新密码已发送到用户邮箱')
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

const showLoginLog = (user) => {
  // TODO: 实现登录日志查看功能
  ElMessage.info('登录日志功能即将上线')
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.username} 吗？此操作不可恢复。`,
      '删除用户',
      { type: 'warning' }
    )
    
    await api.delete(`/admin/users/${user.id}`)
    ElMessage.success('用户删除成功')
    loadUsers()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }
}

const batchEnable = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要启用选中的 ${selectedUsers.value.length} 个用户吗？`,
      '批量启用',
      { type: 'info' }
    )
    
    const userIds = selectedUsers.value.map(user => user.id)
    await api.put('/admin/users/batch-status', {
      userIds,
      status: 1
    })
    
    ElMessage.success('批量启用成功')
    loadUsers()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量启用失败:', error)
      ElMessage.error('批量启用失败')
    }
  }
}

const batchDisable = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用选中的 ${selectedUsers.value.length} 个用户吗？`,
      '批量禁用',
      { type: 'warning' }
    )
    
    const userIds = selectedUsers.value.map(user => user.id)
    await api.put('/admin/users/batch-status', {
      userIds,
      status: 0
    })
    
    ElMessage.success('批量禁用成功')
    loadUsers()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量禁用失败:', error)
      ElMessage.error('批量禁用失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？此操作不可恢复。`,
      '批量删除',
      { type: 'warning' }
    )
    
    const userIds = selectedUsers.value.map(user => user.id)
    await api.delete('/admin/users/batch', {
      data: { userIds }
    })
    
    ElMessage.success('批量删除成功')
    loadUsers()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const getRoleTagType = (roleName) => {
  const typeMap = {
    '超级管理员': 'danger',
    '管理员': 'warning',
    '普通用户': 'info',
    '会员用户': 'success'
  }
  return typeMap[roleName] || 'info'
}

const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadUsers()
  loadRoles()
})
</script>

<style lang="scss" scoped>
.user-management {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    
    .header-left {
      h2 {
        color: var(--text-primary);
        font-size: 24px;
        font-weight: 600;
        margin: 0 0 8px 0;
      }
      
      p {
        color: var(--text-secondary);
        margin: 0;
        font-size: 14px;
      }
    }
  }

  .search-card {
    margin-bottom: 16px;
    background: var(--bg-secondary);
    border: 1px solid var(--border-primary);

    .search-form {
      .el-form-item {
        margin-bottom: 0;
      }
    }
  }

  .batch-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: var(--bg-tertiary);
    border: 1px solid var(--border-primary);
    border-radius: 6px;
    margin-bottom: 16px;

    .selected-info {
      color: var(--text-primary);
      font-size: 14px;
    }

    .actions {
      display: flex;
      gap: 8px;
    }
  }

  .table-card {
    background: var(--bg-secondary);
    border: 1px solid var(--border-primary);

    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .user-details {
        .username {
          font-weight: 500;
          color: var(--text-primary);
          margin-bottom: 2px;
        }

        .email {
          font-size: 12px;
          color: var(--text-secondary);
        }
      }
    }

    .project-count {
      color: var(--primary-color);
      font-weight: 500;
    }

    .never-login {
      color: var(--text-tertiary);
      font-style: italic;
    }

    .pagination-container {
      display: flex;
      justify-content: center;
      padding: 20px 0;
    }
  }

  .user-detail {
    .detail-section {
      margin-bottom: 24px;

      &:last-child {
        margin-bottom: 0;
      }

      h4 {
        color: var(--text-primary);
        font-size: 16px;
        font-weight: 500;
        margin: 0 0 12px 0;
        padding-bottom: 8px;
        border-bottom: 1px solid var(--border-secondary);
      }

      .roles-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .no-roles {
          color: var(--text-tertiary);
          font-style: italic;
        }
      }

      .stats-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 16px;

        .stat-item {
          text-align: center;
          padding: 16px;
          background: var(--bg-tertiary);
          border-radius: 6px;

          .stat-value {
            font-size: 24px;
            font-weight: 600;
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

  .role-assign {
    p {
      color: var(--text-primary);
      margin-bottom: 16px;

      strong {
        color: var(--primary-color);
      }
    }

    :deep(.el-checkbox-group) {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .el-checkbox {
        margin-right: 0;

        .el-checkbox__label {
          color: var(--text-primary);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .user-management {
    .page-header {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;
    }

    .search-form {
      .el-form-item {
        display: block;
        margin-bottom: 12px;

        .el-input,
        .el-select,
        .el-date-picker {
          width: 100% !important;
        }
      }
    }

    .batch-actions {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;

      .actions {
        justify-content: center;
      }
    }

    .user-detail {
      .stats-grid {
        grid-template-columns: 1fr;
      }
    }
  }
}

// 深色主题适配
:deep(.el-table) {
  background: var(--bg-secondary);
  color: var(--text-primary);

  .el-table__header {
    background: var(--bg-tertiary);

    th {
      background: var(--bg-tertiary);
      color: var(--text-primary);
      border-bottom: 1px solid var(--border-primary);
    }
  }

  .el-table__body {
    tr {
      background: var(--bg-secondary);

      &:hover {
        background: var(--bg-tertiary);
      }

      td {
        border-bottom: 1px solid var(--border-secondary);
      }
    }
  }
}

:deep(.el-pagination) {
  .el-pagination__total,
  .el-pagination__jump {
    color: var(--text-secondary);
  }

  .btn-prev,
  .btn-next,
  .el-pager li {
    background: var(--bg-tertiary);
    color: var(--text-primary);
    border: 1px solid var(--border-secondary);

    &:hover {
      color: var(--primary-color);
    }

    &.active {
      background: var(--primary-color);
      color: white;
    }
  }
}

:deep(.el-descriptions) {
  .el-descriptions__header {
    .el-descriptions__title {
      color: var(--text-primary);
    }
  }

  .el-descriptions__body {
    .el-descriptions__table {
      .el-descriptions__cell {
        border: 1px solid var(--border-secondary);

        .el-descriptions__label {
          background: var(--bg-tertiary);
          color: var(--text-secondary);
        }

        .el-descriptions__content {
          background: var(--bg-secondary);
          color: var(--text-primary);
        }
      }
    }
  }
}
</style>