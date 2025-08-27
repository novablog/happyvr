<template>
  <div class="role-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>角色权限管理</h2>
        <p>管理系统角色和权限配置</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateRole">
          <el-icon><Plus /></el-icon>
          新建角色
        </el-button>
      </div>
    </div>

    <!-- 角色列表 -->
    <div class="content-layout">
      <div class="roles-section">
        <el-card class="roles-card">
          <template #header>
            <div class="card-header">
              <span>角色列表</span>
              <el-input
                v-model="roleSearchKeyword"
                placeholder="搜索角色"
                clearable
                style="width: 200px"
                @input="filterRoles"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </template>

          <div class="roles-list" v-loading="rolesLoading">
            <div
              v-for="role in filteredRoles"
              :key="role.id"
              class="role-item"
              :class="{ active: selectedRole?.id === role.id }"
              @click="selectRole(role)"
            >
              <div class="role-info">
                <div class="role-header">
                  <h4 class="role-name">{{ role.name }}</h4>
                  <el-tag :type="getRoleTagType(role.name)" size="small">
                    {{ role.type || '自定义' }}
                  </el-tag>
                </div>
                <p class="role-description">{{ role.description || '暂无描述' }}</p>
                <div class="role-stats">
                  <span class="user-count">{{ role.userCount || 0 }} 个用户</span>
                  <span class="permission-count">{{ role.permissionCount || 0 }} 个权限</span>
                </div>
              </div>
              <div class="role-actions">
                <el-button size="small" @click.stop="editRole(role)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-dropdown @command="(command) => handleRoleAction(command, role)" @click.stop>
                  <el-button size="small">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="copy">复制角色</el-dropdown-item>
                      <el-dropdown-item command="users">查看用户</el-dropdown-item>
                      <el-dropdown-item command="delete" divided :disabled="role.isSystem">
                        删除角色
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <el-empty v-if="filteredRoles.length === 0" description="暂无角色数据" />
          </div>
        </el-card>
      </div>

      <!-- 权限配置 -->
      <div class="permissions-section">
        <el-card class="permissions-card">
          <template #header>
            <div class="card-header">
              <span v-if="selectedRole">{{ selectedRole.name }} - 权限配置</span>
              <span v-else>请选择角色</span>
              <div class="header-actions" v-if="selectedRole">
                <el-button size="small" @click="expandAllPermissions">
                  <el-icon><Expand /></el-icon>
                  展开全部
                </el-button>
                <el-button size="small" @click="collapseAllPermissions">
                  <el-icon><Fold /></el-icon>
                  收起全部
                </el-button>
                <el-button type="primary" size="small" @click="saveRolePermissions" :loading="savingPermissions">
                  <el-icon><Check /></el-icon>
                  保存权限
                </el-button>
              </div>
            </div>
          </template>

          <div class="permissions-content" v-if="selectedRole" v-loading="permissionsLoading">
            <!-- 权限统计 -->
            <div class="permissions-stats">
              <div class="stat-item">
                <span class="stat-label">已选权限：</span>
                <span class="stat-value">{{ selectedPermissions.length }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">总权限数：</span>
                <span class="stat-value">{{ totalPermissions }}</span>
              </div>
              <div class="stat-item">
                <el-button size="small" @click="selectAllPermissions">全选</el-button>
                <el-button size="small" @click="clearAllPermissions">清空</el-button>
              </div>
            </div>

            <!-- 权限树 -->
            <div class="permissions-tree">
              <el-tree
                ref="permissionTreeRef"
                :data="permissionTree"
                :props="treeProps"
                :default-expand-all="false"
                :check-strictly="false"
                show-checkbox
                node-key="id"
                @check="handlePermissionCheck"
              >
                <template #default="{ node, data }">
                  <div class="permission-node">
                    <div class="node-content">
                      <el-icon class="node-icon">
                        <component :is="getPermissionIcon(data.type)" />
                      </el-icon>
                      <span class="node-label">{{ data.name }}</span>
                      <el-tag v-if="data.code" size="small" type="info" class="node-code">
                        {{ data.code }}
                      </el-tag>
                    </div>
                    <div class="node-description" v-if="data.description">
                      {{ data.description }}
                    </div>
                  </div>
                </template>
              </el-tree>
            </div>
          </div>

          <div class="empty-permissions" v-else>
            <el-empty description="请选择一个角色来配置权限" />
          </div>
        </el-card>
      </div>
    </div>

    <!-- 角色编辑对话框 -->
    <el-dialog
      v-model="roleEditVisible"
      :title="isCreateMode ? '新建角色' : '编辑角色'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-width="80px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>

        <el-form-item label="角色类型" prop="type">
          <el-select v-model="roleForm.type" placeholder="请选择角色类型">
            <el-option label="系统角色" value="system" />
            <el-option label="业务角色" value="business" />
            <el-option label="自定义角色" value="custom" />
          </el-select>
        </el-form-item>

        <el-form-item label="角色描述">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="roleEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRole">
          {{ isCreateMode ? '创建' : '保存' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 角色用户列表对话框 -->
    <el-dialog
      v-model="roleUsersVisible"
      :title="currentRole?.name + ' - 用户列表'"
      width="600px"
    >
      <el-table :data="roleUsers" v-loading="roleUsersLoading">
        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="32" :src="row.avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="user-details">
                <div class="username">{{ row.username }}</div>
                <div class="email">{{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="分配时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.roleAssignedAt || row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="roleUsersVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Edit, MoreFilled, Expand, Fold, Check, UserFilled,
  User, Setting, Folder, DataAnalysis, Key, Monitor, Shield
} from '@element-plus/icons-vue'
import api from '@/utils/api'

// 响应式数据
const rolesLoading = ref(false)
const permissionsLoading = ref(false)
const roleUsersLoading = ref(false)
const saving = ref(false)
const savingPermissions = ref(false)

const roles = ref([])
const permissions = ref([])
const selectedRole = ref(null)
const currentRole = ref(null)
const roleUsers = ref([])

// 搜索和筛选
const roleSearchKeyword = ref('')
const filteredRoles = computed(() => {
  if (!roleSearchKeyword.value) return roles.value
  return roles.value.filter(role => 
    role.name.toLowerCase().includes(roleSearchKeyword.value.toLowerCase()) ||
    (role.description && role.description.toLowerCase().includes(roleSearchKeyword.value.toLowerCase()))
  )
})

// 对话框显示状态
const roleEditVisible = ref(false)
const roleUsersVisible = ref(false)
const isCreateMode = ref(false)

// 表单引用
const roleFormRef = ref()
const permissionTreeRef = ref()

// 角色表单
const roleForm = reactive({
  name: '',
  type: 'custom',
  description: '',
  status: 1
})

// 权限相关
const selectedPermissions = ref([])
const permissionTree = ref([])

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 计算属性
const totalPermissions = computed(() => {
  const countPermissions = (nodes) => {
    let count = 0
    nodes.forEach(node => {
      if (!node.children || node.children.length === 0) {
        count++
      } else {
        count += countPermissions(node.children)
      }
    })
    return count
  }
  return countPermissions(permissionTree.value)
})

// 表单验证规则
const roleRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度应为2-20位', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择角色类型', trigger: 'change' }
  ]
}

// 方法
const loadRoles = async () => {
  try {
    rolesLoading.value = true
    const response = await api.get('/admin/roles')
    roles.value = response.data.data || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('加载角色列表失败')
  } finally {
    rolesLoading.value = false
  }
}

const loadPermissions = async () => {
  try {
    const response = await api.get('/admin/permissions')
    permissions.value = response.data.data || []
    buildPermissionTree()
  } catch (error) {
    console.error('加载权限列表失败:', error)
    ElMessage.error('加载权限列表失败')
  }
}

const buildPermissionTree = () => {
  // 构建权限树结构
  const tree = []
  const permissionMap = new Map()
  
  // 预定义的权限结构
  const permissionStructure = [
    {
      id: 'dashboard',
      name: '仪表盘',
      type: 'module',
      children: [
        { id: 'dashboard:view', name: '查看仪表盘', type: 'action', code: 'dashboard:view' }
      ]
    },
    {
      id: 'user',
      name: '用户管理',
      type: 'module',
      children: [
        { id: 'user:view', name: '查看用户', type: 'action', code: 'user:view' },
        { id: 'user:create', name: '创建用户', type: 'action', code: 'user:create' },
        { id: 'user:edit', name: '编辑用户', type: 'action', code: 'user:edit' },
        { id: 'user:delete', name: '删除用户', type: 'action', code: 'user:delete' },
        { id: 'user:status', name: '管理用户状态', type: 'action', code: 'user:status' }
      ]
    },
    {
      id: 'role',
      name: '角色权限',
      type: 'module',
      children: [
        { id: 'role:view', name: '查看角色', type: 'action', code: 'role:view' },
        { id: 'role:create', name: '创建角色', type: 'action', code: 'role:create' },
        { id: 'role:edit', name: '编辑角色', type: 'action', code: 'role:edit' },
        { id: 'role:delete', name: '删除角色', type: 'action', code: 'role:delete' },
        { id: 'role:assign', name: '分配权限', type: 'action', code: 'role:assign' }
      ]
    },
    {
      id: 'project',
      name: '内容管理',
      type: 'module',
      children: [
        { id: 'project:view', name: '查看项目', type: 'action', code: 'project:view' },
        { id: 'project:audit', name: '审核项目', type: 'action', code: 'project:audit' },
        { id: 'project:feature', name: '推荐项目', type: 'action', code: 'project:feature' },
        { id: 'project:delete', name: '删除项目', type: 'action', code: 'project:delete' }
      ]
    },
    {
      id: 'system',
      name: '系统管理',
      type: 'module',
      children: [
        { id: 'system:config', name: '系统配置', type: 'action', code: 'system:config' },
        { id: 'system:log', name: '查看日志', type: 'action', code: 'system:log' },
        { id: 'system:monitor', name: '系统监控', type: 'action', code: 'system:monitor' },
        { id: 'system:backup', name: '数据备份', type: 'action', code: 'system:backup' }
      ]
    }
  ]
  
  permissionTree.value = permissionStructure
}

const selectRole = async (role) => {
  selectedRole.value = role
  await loadRolePermissions(role.id)
}

const loadRolePermissions = async (roleId) => {
  try {
    permissionsLoading.value = true
    const response = await api.get(`/admin/roles/${roleId}/permissions`)
    const rolePermissions = response.data.data || []
    
    // 设置选中的权限
    selectedPermissions.value = rolePermissions.map(p => p.id)
    
    // 等待DOM更新后设置树的选中状态
    await nextTick()
    if (permissionTreeRef.value) {
      permissionTreeRef.value.setCheckedKeys(selectedPermissions.value)
    }
    
  } catch (error) {
    console.error('加载角色权限失败:', error)
    ElMessage.error('加载角色权限失败')
  } finally {
    permissionsLoading.value = false
  }
}

const handlePermissionCheck = (data, checked) => {
  if (permissionTreeRef.value) {
    selectedPermissions.value = permissionTreeRef.value.getCheckedKeys()
  }
}

const expandAllPermissions = () => {
  if (permissionTreeRef.value) {
    const allKeys = []
    const collectKeys = (nodes) => {
      nodes.forEach(node => {
        allKeys.push(node.id)
        if (node.children) {
          collectKeys(node.children)
        }
      })
    }
    collectKeys(permissionTree.value)
    
    allKeys.forEach(key => {
      permissionTreeRef.value.store.nodesMap[key]?.expand()
    })
  }
}

const collapseAllPermissions = () => {
  if (permissionTreeRef.value) {
    const allKeys = []
    const collectKeys = (nodes) => {
      nodes.forEach(node => {
        allKeys.push(node.id)
        if (node.children) {
          collectKeys(node.children)
        }
      })
    }
    collectKeys(permissionTree.value)
    
    allKeys.forEach(key => {
      permissionTreeRef.value.store.nodesMap[key]?.collapse()
    })
  }
}

const selectAllPermissions = () => {
  if (permissionTreeRef.value) {
    const allKeys = []
    const collectLeafKeys = (nodes) => {
      nodes.forEach(node => {
        if (!node.children || node.children.length === 0) {
          allKeys.push(node.id)
        } else {
          collectLeafKeys(node.children)
        }
      })
    }
    collectLeafKeys(permissionTree.value)
    
    permissionTreeRef.value.setCheckedKeys(allKeys)
    selectedPermissions.value = allKeys
  }
}

const clearAllPermissions = () => {
  if (permissionTreeRef.value) {
    permissionTreeRef.value.setCheckedKeys([])
    selectedPermissions.value = []
  }
}

const saveRolePermissions = async () => {
  if (!selectedRole.value) return
  
  try {
    savingPermissions.value = true
    
    await api.put(`/admin/roles/${selectedRole.value.id}/permissions`, {
      permissionIds: selectedPermissions.value
    })
    
    ElMessage.success('权限保存成功')
    
    // 更新角色的权限数量
    selectedRole.value.permissionCount = selectedPermissions.value.length
    
  } catch (error) {
    console.error('保存权限失败:', error)
    ElMessage.error('保存权限失败')
  } finally {
    savingPermissions.value = false
  }
}

const filterRoles = () => {
  // 搜索功能已通过计算属性实现
}

const showCreateRole = () => {
  isCreateMode.value = true
  resetRoleForm()
  roleEditVisible.value = true
}

const editRole = (role) => {
  isCreateMode.value = false
  currentRole.value = role
  Object.assign(roleForm, {
    name: role.name,
    type: role.type || 'custom',
    description: role.description || '',
    status: role.status
  })
  roleEditVisible.value = true
}

const resetRoleForm = () => {
  Object.assign(roleForm, {
    name: '',
    type: 'custom',
    description: '',
    status: 1
  })
  
  if (roleFormRef.value) {
    roleFormRef.value.clearValidate()
  }
}

const saveRole = async () => {
  if (!roleFormRef.value) return
  
  try {
    await roleFormRef.value.validate()
    saving.value = true
    
    if (isCreateMode.value) {
      await api.post('/admin/roles', roleForm)
      ElMessage.success('角色创建成功')
    } else {
      await api.put(`/admin/roles/${currentRole.value.id}`, roleForm)
      ElMessage.success('角色更新成功')
    }
    
    roleEditVisible.value = false
    loadRoles()
    
  } catch (error) {
    console.error('保存角色失败:', error)
    ElMessage.error('保存角色失败')
  } finally {
    saving.value = false
  }
}

const handleRoleAction = async (command, role) => {
  currentRole.value = role
  
  switch (command) {
    case 'copy':
      await copyRole(role)
      break
    case 'users':
      await showRoleUsers(role)
      break
    case 'delete':
      await deleteRole(role)
      break
  }
}

const copyRole = async (role) => {
  try {
    await api.post(`/admin/roles/${role.id}/copy`, {
      name: `${role.name}_副本`
    })
    ElMessage.success('角色复制成功')
    loadRoles()
  } catch (error) {
    console.error('复制角色失败:', error)
    ElMessage.error('复制角色失败')
  }
}

const showRoleUsers = async (role) => {
  try {
    roleUsersLoading.value = true
    roleUsersVisible.value = true
    
    const response = await api.get(`/admin/roles/${role.id}/users`)
    roleUsers.value = response.data.data || []
    
  } catch (error) {
    console.error('获取角色用户失败:', error)
    ElMessage.error('获取角色用户失败')
  } finally {
    roleUsersLoading.value = false
  }
}

const deleteRole = async (role) => {
  try {
    // 检查角色是否有用户
    const response = await api.get(`/admin/roles/${role.id}/check-delete`)
    const { canDelete, userCount } = response.data.data
    
    if (!canDelete) {
      ElMessage.warning(`该角色下还有 ${userCount} 个用户，无法删除`)
      return
    }
    
    await ElMessageBox.confirm(
      `确定要删除角色 ${role.name} 吗？此操作不可恢复。`,
      '删除角色',
      { type: 'warning' }
    )
    
    await api.delete(`/admin/roles/${role.id}`)
    ElMessage.success('角色删除成功')
    
    // 如果删除的是当前选中的角色，清空选择
    if (selectedRole.value?.id === role.id) {
      selectedRole.value = null
      selectedPermissions.value = []
    }
    
    loadRoles()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除角色失败:', error)
      ElMessage.error('删除角色失败')
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

const getPermissionIcon = (type) => {
  const iconMap = {
    'module': Folder,
    'action': Key,
    'dashboard': DataAnalysis,
    'user': User,
    'role': Shield,
    'project': Monitor,
    'system': Setting
  }
  return iconMap[type] || Key
}

const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadRoles()
  loadPermissions()
})
</script>

<style lang="scss" scoped>
.role-management {
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

  .content-layout {
    display: grid;
    grid-template-columns: 400px 1fr;
    gap: 20px;
    height: calc(100vh - 200px);

    .roles-section {
      .roles-card {
        height: 100%;
        background: var(--bg-secondary);
        border: 1px solid var(--border-primary);

        :deep(.el-card__header) {
          background: var(--bg-tertiary);
          border-bottom: 1px solid var(--border-primary);

          .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-weight: 500;
            color: var(--text-primary);
          }
        }

        :deep(.el-card__body) {
          padding: 0;
          height: calc(100% - 60px);
          overflow: hidden;
        }

        .roles-list {
          height: 100%;
          overflow-y: auto;
          padding: 16px;

          .role-item {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            padding: 16px;
            margin-bottom: 12px;
            background: var(--bg-primary);
            border: 1px solid var(--border-secondary);
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.3s ease;

            &:hover {
              border-color: var(--primary-color);
              box-shadow: 0 2px 8px rgba(212, 175, 55, 0.1);
            }

            &.active {
              border-color: var(--primary-color);
              background: rgba(212, 175, 55, 0.05);
            }

            &:last-child {
              margin-bottom: 0;
            }

            .role-info {
              flex: 1;

              .role-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 8px;

                .role-name {
                  font-size: 16px;
                  font-weight: 500;
                  color: var(--text-primary);
                  margin: 0;
                }
              }

              .role-description {
                font-size: 14px;
                color: var(--text-secondary);
                margin: 0 0 8px 0;
                line-height: 1.4;
              }

              .role-stats {
                display: flex;
                gap: 16px;
                font-size: 12px;
                color: var(--text-tertiary);

                .user-count,
                .permission-count {
                  display: flex;
                  align-items: center;
                  gap: 4px;
                }
              }
            }

            .role-actions {
              display: flex;
              gap: 4px;
              margin-left: 12px;
            }
          }
        }
      }
    }

    .permissions-section {
      .permissions-card {
        height: 100%;
        background: var(--bg-secondary);
        border: 1px solid var(--border-primary);

        :deep(.el-card__header) {
          background: var(--bg-tertiary);
          border-bottom: 1px solid var(--border-primary);

          .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-weight: 500;
            color: var(--text-primary);

            .header-actions {
              display: flex;
              gap: 8px;
            }
          }
        }

        :deep(.el-card__body) {
          padding: 0;
          height: calc(100% - 60px);
          overflow: hidden;
        }

        .permissions-content {
          height: 100%;
          display: flex;
          flex-direction: column;

          .permissions-stats {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 16px;
            background: var(--bg-tertiary);
            border-bottom: 1px solid var(--border-secondary);

            .stat-item {
              display: flex;
              align-items: center;
              gap: 8px;
              font-size: 14px;

              .stat-label {
                color: var(--text-secondary);
              }

              .stat-value {
                color: var(--primary-color);
                font-weight: 500;
              }
            }
          }

          .permissions-tree {
            flex: 1;
            padding: 16px;
            overflow-y: auto;

            :deep(.el-tree) {
              background: transparent;
              color: var(--text-primary);

              .el-tree-node {
                .el-tree-node__content {
                  height: auto;
                  padding: 8px 0;

                  &:hover {
                    background: var(--bg-tertiary);
                  }

                  .el-tree-node__expand-icon {
                    color: var(--text-secondary);
                  }

                  .el-checkbox {
                    .el-checkbox__input.is-checked .el-checkbox__inner {
                      background-color: var(--primary-color);
                      border-color: var(--primary-color);
                    }
                  }
                }

                .el-tree-node__children {
                  .el-tree-node__content {
                    padding-left: 24px;
                  }
                }
              }
            }

            .permission-node {
              width: 100%;

              .node-content {
                display: flex;
                align-items: center;
                gap: 8px;
                margin-bottom: 4px;

                .node-icon {
                  font-size: 16px;
                  color: var(--primary-color);
                }

                .node-label {
                  font-size: 14px;
                  color: var(--text-primary);
                  font-weight: 500;
                }

                .node-code {
                  margin-left: auto;
                }
              }

              .node-description {
                font-size: 12px;
                color: var(--text-tertiary);
                margin-left: 24px;
                line-height: 1.4;
              }
            }
          }
        }

        .empty-permissions {
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
    }
  }

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
}

// 响应式设计
@media (max-width: 1200px) {
  .role-management {
    .content-layout {
      grid-template-columns: 1fr;
      height: auto;

      .roles-section {
        .roles-card {
          height: 400px;
        }
      }

      .permissions-section {
        .permissions-card {
          height: 600px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .role-management {
    .page-header {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;
    }

    .content-layout {
      gap: 16px;

      .roles-section {
        .roles-card {
          height: 300px;

          .roles-list {
            .role-item {
              flex-direction: column;
              gap: 12px;
              align-items: stretch;

              .role-actions {
                margin-left: 0;
                justify-content: flex-end;
              }
            }
          }
        }
      }

      .permissions-section {
        .permissions-card {
          height: 500px;

          .permissions-content {
            .permissions-stats {
              flex-direction: column;
              gap: 8px;
              align-items: stretch;

              .stat-item {
                justify-content: space-between;
              }
            }
          }
        }
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
</style>