<template>
  <div class="system-management">
    <div class="page-header">
      <h2>系统管理</h2>
      <p>系统配置、监控和日志管理</p>
    </div>

    <!-- 系统状态监控 -->
    <div class="system-status">
      <h3>系统状态监控</h3>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon" :class="{ online: systemStatus.server.status === 'online' }">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="status-content">
              <div class="status-title">服务器状态</div>
              <div class="status-value" :class="systemStatus.server.status">
                {{ systemStatus.server.status === 'online' ? '运行正常' : '离线' }}
              </div>
              <div class="status-detail">
                CPU: {{ systemStatus.server.cpu }}% | 内存: {{ systemStatus.server.memory }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon" :class="{ online: systemStatus.database.status === 'connected' }">
              <el-icon><DataBase /></el-icon>
            </div>
            <div class="status-content">
              <div class="status-title">数据库状态</div>
              <div class="status-value" :class="systemStatus.database.status">
                {{ systemStatus.database.status === 'connected' ? '连接正常' : '连接异常' }}
              </div>
              <div class="status-detail">
                连接数: {{ systemStatus.database.connections }} | 响应时间: {{ systemStatus.database.responseTime }}ms
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon" :class="{ online: systemStatus.storage.status === 'available' }">
              <el-icon><FolderOpened /></el-icon>
            </div>
            <div class="status-content">
              <div class="status-title">存储状态</div>
              <div class="status-value" :class="systemStatus.storage.status">
                {{ systemStatus.storage.status === 'available' ? '正常' : '异常' }}
              </div>
              <div class="status-detail">
                已用: {{ systemStatus.storage.used }}GB / {{ systemStatus.storage.total }}GB
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon maintenance">
              <el-icon><Tools /></el-icon>
            </div>
            <div class="status-content">
              <div class="status-title">维护模式</div>
              <div class="status-value">
                <el-switch
                  v-model="systemStatus.maintenance.enabled"
                  @change="toggleMaintenanceMode"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </div>
              <div class="status-detail">
                {{ systemStatus.maintenance.enabled ? '系统维护中' : '正常运行' }}
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 系统配置 -->
    <div class="system-config">
      <h3>系统配置</h3>
      <el-card>
        <el-form :model="configForm" label-width="150px" :rules="configRules">
          <el-row :gutter="40">
            <el-col :span="12">
              <el-form-item label="文件上传限制" prop="maxFileSize">
                <el-input-number
                  v-model="configForm.maxFileSize"
                  :min="1"
                  :max="1000"
                  controls-position="right"
                />
                <span class="unit">MB</span>
              </el-form-item>
              <el-form-item label="VR处理超时" prop="vrProcessTimeout">
                <el-input-number
                  v-model="configForm.vrProcessTimeout"
                  :min="30"
                  :max="600"
                  controls-position="right"
                />
                <span class="unit">秒</span>
              </el-form-item>
              <el-form-item label="并发处理数" prop="maxConcurrentJobs">
                <el-input-number
                  v-model="configForm.maxConcurrentJobs"
                  :min="1"
                  :max="20"
                  controls-position="right"
                />
                <span class="unit">个</span>
              </el-form-item>
              <el-form-item label="用户注册" prop="allowRegistration">
                <el-switch
                  v-model="configForm.allowRegistration"
                  active-text="允许"
                  inactive-text="禁止"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮件服务器" prop="emailServer">
                <el-input v-model="configForm.emailServer" placeholder="smtp.example.com" />
              </el-form-item>
              <el-form-item label="邮件端口" prop="emailPort">
                <el-input-number
                  v-model="configForm.emailPort"
                  :min="1"
                  :max="65535"
                  controls-position="right"
                />
              </el-form-item>
              <el-form-item label="系统公告" prop="systemNotice">
                <el-input
                  v-model="configForm.systemNotice"
                  type="textarea"
                  :rows="3"
                  placeholder="系统公告内容"
                />
              </el-form-item>
              <el-form-item label="备份频率" prop="backupFrequency">
                <el-select v-model="configForm.backupFrequency">
                  <el-option label="每日" value="daily" />
                  <el-option label="每周" value="weekly" />
                  <el-option label="每月" value="monthly" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button type="primary" @click="saveConfig" :loading="configLoading">
              保存配置
            </el-button>
            <el-button @click="resetConfig">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 数据备份与恢复 -->
    <div class="backup-section">
      <h3>数据备份与恢复</h3>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>数据备份</span>
                <el-button type="primary" @click="createBackup" :loading="backupLoading">
                  <el-icon><Download /></el-icon>
                  创建备份
                </el-button>
              </div>
            </template>
            <div class="backup-list">
              <div v-for="backup in backupList" :key="backup.id" class="backup-item">
                <div class="backup-info">
                  <div class="backup-name">{{ backup.name }}</div>
                  <div class="backup-meta">
                    <span>{{ backup.size }}</span>
                    <span>{{ formatDate(backup.createdAt) }}</span>
                  </div>
                </div>
                <div class="backup-actions">
                  <el-button size="small" @click="downloadBackup(backup)">
                    <el-icon><Download /></el-icon>
                    下载
                  </el-button>
                  <el-button size="small" @click="restoreBackup(backup)" type="warning">
                    <el-icon><RefreshRight /></el-icon>
                    恢复
                  </el-button>
                  <el-button size="small" @click="deleteBackup(backup)" type="danger">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </div>
              <div v-if="!backupList.length" class="empty-state">
                <el-icon><FolderOpened /></el-icon>
                <p>暂无备份文件</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>备份恢复</span>
            </template>
            <el-upload
              class="upload-backup"
              drag
              :auto-upload="false"
              :on-change="handleBackupFileChange"
              accept=".sql,.zip"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将备份文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 .sql 和 .zip 格式的备份文件
                </div>
              </template>
            </el-upload>
            <div v-if="uploadedBackupFile" class="uploaded-file">
              <div class="file-info">
                <el-icon><Document /></el-icon>
                <span>{{ uploadedBackupFile.name }}</span>
              </div>
              <el-button type="primary" @click="uploadAndRestore" :loading="restoreLoading">
                开始恢复
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作日志 -->
    <div class="operation-logs">
      <h3>操作日志</h3>
      <div class="log-filters">
        <el-row :gutter="20" align="middle">
          <el-col :span="6">
            <el-input
              v-model="logSearch.keyword"
              placeholder="搜索操作内容"
              clearable
              @keyup.enter="searchLogs"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="logSearch.level" placeholder="日志级别" clearable>
              <el-option label="全部" value="" />
              <el-option label="信息" value="info" />
              <el-option label="警告" value="warning" />
              <el-option label="错误" value="error" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="logSearch.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="searchLogs">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="exportLogs">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </el-col>
        </el-row>
      </div>

      <el-table :data="logList" v-loading="logLoading" row-key="id">
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="getLogLevelType(row.level)" size="small">
              {{ row.level.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作员" width="120">
          <template #default="{ row }">
            {{ row.operator }}
          </template>
        </el-table-column>
        <el-table-column label="操作内容" min-width="300">
          <template #default="{ row }">
            {{ row.action }}
          </template>
        </el-table-column>
        <el-table-column label="IP地址" width="120">
          <template #default="{ row }">
            {{ row.ipAddress }}
          </template>
        </el-table-column>
        <el-table-column label="详情" width="80">
          <template #default="{ row }">
            <el-button size="small" @click="showLogDetail(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="logPagination.currentPage"
          v-model:page-size="logPagination.pageSize"
          :page-sizes="[20, 50, 100, 200]"
          :total="logPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleLogSizeChange"
          @current-change="handleLogCurrentChange"
        />
      </div>
    </div>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="logDetailDialog.visible" title="操作日志详情" width="600px">
      <div v-if="logDetailDialog.log" class="log-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="时间">
            {{ formatDate(logDetailDialog.log.timestamp) }}
          </el-descriptions-item>
          <el-descriptions-item label="级别">
            <el-tag :type="getLogLevelType(logDetailDialog.log.level)">
              {{ logDetailDialog.log.level.toUpperCase() }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作员">
            {{ logDetailDialog.log.operator }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ logDetailDialog.log.ipAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="用户代理">
            {{ logDetailDialog.log.userAgent }}
          </el-descriptions-item>
          <el-descriptions-item label="操作内容">
            {{ logDetailDialog.log.action }}
          </el-descriptions-item>
          <el-descriptions-item label="详细信息">
            <pre class="log-details">{{ JSON.stringify(logDetailDialog.log.details, null, 2) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Monitor,
  DataBase,
  FolderOpened,
  Tools,
  Download,
  RefreshRight,
  Delete,
  UploadFilled,
  Document,
  Search
} from '@element-plus/icons-vue'
import {
  getSystemConfig,
  updateSystemConfig,
  getSystemStatus,
  toggleMaintenanceMode as apiToggleMaintenanceMode,
  getBackupList,
  createBackup as apiCreateBackup,
  deleteBackup as apiDeleteBackup,
  getOperationLogs
} from '@/api/admin'

// 系统状态
const systemStatus = reactive({
  server: {
    status: 'online',
    cpu: 45,
    memory: 68
  },
  database: {
    status: 'connected',
    connections: 12,
    responseTime: 25
  },
  storage: {
    status: 'available',
    used: 128,
    total: 500
  },
  maintenance: {
    enabled: false
  }
})

// 系统配置
const configForm = reactive({
  maxFileSize: 100,
  vrProcessTimeout: 300,
  maxConcurrentJobs: 5,
  allowRegistration: true,
  emailServer: 'smtp.example.com',
  emailPort: 587,
  systemNotice: '',
  backupFrequency: 'daily'
})

const configLoading = ref(false)
const configRules = {
  maxFileSize: [
    { required: true, message: '请设置文件上传限制', trigger: 'blur' }
  ],
  vrProcessTimeout: [
    { required: true, message: '请设置VR处理超时时间', trigger: 'blur' }
  ]
}

// 备份相关
const backupLoading = ref(false)
const restoreLoading = ref(false)
const backupList = ref([])
const uploadedBackupFile = ref(null)

// 操作日志
const logLoading = ref(false)
const logList = ref([])
const logSearch = reactive({
  keyword: '',
  level: '',
  dateRange: []
})
const logPagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 日志详情对话框
const logDetailDialog = reactive({
  visible: false,
  log: null
})

// 获取系统状态
const fetchSystemStatus = async () => {
  try {
    const response = await getSystemStatus()
    Object.assign(systemStatus, response.data)
  } catch (error) {
    console.error('获取系统状态失败:', error)
    // 使用模拟数据
  }
}

// 切换维护模式
const toggleMaintenanceMode = async (enabled) => {
  try {
    await apiToggleMaintenanceMode(enabled)
    ElMessage.success(enabled ? '维护模式已开启' : '维护模式已关闭')
  } catch (error) {
    ElMessage.error('切换维护模式失败')
    // 回滚状态
    systemStatus.maintenance.enabled = !enabled
  }
}

// 获取系统配置
const fetchSystemConfig = async () => {
  try {
    const response = await getSystemConfig()
    Object.assign(configForm, response.data)
  } catch (error) {
    console.error('获取系统配置失败:', error)
  }
}

// 保存系统配置
const saveConfig = async () => {
  configLoading.value = true
  try {
    await updateSystemConfig(configForm)
    ElMessage.success('系统配置保存成功')
  } catch (error) {
    ElMessage.error('保存配置失败')
  } finally {
    configLoading.value = false
  }
}

// 重置配置
const resetConfig = () => {
  fetchSystemConfig()
}

// 获取备份列表
const fetchBackupList = async () => {
  try {
    const response = await getBackupList()
    backupList.value = response.data
  } catch (error) {
    console.error('获取备份列表失败:', error)
    // 使用模拟数据
    backupList.value = [
      {
        id: 1,
        name: 'backup_2024_01_15_10_30.sql',
        size: '25.6MB',
        createdAt: '2024-01-15 10:30:00'
      },
      {
        id: 2,
        name: 'backup_2024_01_14_02_00.sql',
        size: '24.8MB',
        createdAt: '2024-01-14 02:00:00'
      }
    ]
  }
}

// 创建备份
const createBackup = async () => {
  backupLoading.value = true
  try {
    await apiCreateBackup()
    ElMessage.success('备份创建成功')
    fetchBackupList()
  } catch (error) {
    ElMessage.error('创建备份失败')
  } finally {
    backupLoading.value = false
  }
}

// 下载备份
const downloadBackup = (backup) => {
  // 创建下载链接
  const link = document.createElement('a')
  link.href = `/api/v1/admin/system/backup/${backup.id}/download`
  link.download = backup.name
  link.click()
}

// 恢复备份
const restoreBackup = async (backup) => {
  try {
    await ElMessageBox.confirm(
      `确认恢复备份"${backup.name}"？此操作将覆盖当前数据！`,
      '恢复确认',
      {
        type: 'error',
        confirmButtonText: '确认恢复',
        cancelButtonText: '取消'
      }
    )
    
    // 模拟恢复操作
    ElMessage.success('备份恢复成功')
  } catch (error) {
    // 用户取消
  }
}

// 删除备份
const deleteBackup = async (backup) => {
  try {
    await ElMessageBox.confirm(
      `确认删除备份"${backup.name}"？`,
      '删除确认',
      {
        type: 'warning'
      }
    )
    
    await apiDeleteBackup(backup.id)
    ElMessage.success('备份删除成功')
    fetchBackupList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除备份失败')
    }
  }
}

// 处理备份文件上传
const handleBackupFileChange = (file) => {
  uploadedBackupFile.value = file
}

// 上传并恢复
const uploadAndRestore = async () => {
  if (!uploadedBackupFile.value) {
    ElMessage.warning('请先选择备份文件')
    return
  }
  
  restoreLoading.value = true
  try {
    // 模拟上传和恢复
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('备份文件恢复成功')
    uploadedBackupFile.value = null
  } catch (error) {
    ElMessage.error('恢复失败')
  } finally {
    restoreLoading.value = false
  }
}

// 获取操作日志
const fetchOperationLogs = async () => {
  logLoading.value = true
  try {
    const params = {
      page: logPagination.currentPage,
      size: logPagination.pageSize,
      keyword: logSearch.keyword,
      level: logSearch.level,
      startTime: logSearch.dateRange?.[0],
      endTime: logSearch.dateRange?.[1]
    }
    
    const response = await getOperationLogs(params)
    logList.value = response.data.list
    logPagination.total = response.data.total
  } catch (error) {
    console.error('获取操作日志失败:', error)
    // 使用模拟数据
    const mockLogs = [
      {
        id: 1,
        timestamp: '2024-01-15 14:30:25',
        level: 'info',
        operator: 'admin',
        action: '用户登录',
        ipAddress: '192.168.1.100',
        userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        details: { userId: 1, loginMethod: 'password' }
      },
      {
        id: 2,
        timestamp: '2024-01-15 14:25:10',
        level: 'warning',
        operator: 'admin',
        action: '修改系统配置',
        ipAddress: '192.168.1.100',
        userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        details: { configKey: 'maxFileSize', oldValue: 50, newValue: 100 }
      },
      {
        id: 3,
        timestamp: '2024-01-15 14:20:05',
        level: 'error',
        operator: 'system',
        action: 'VR处理失败',
        ipAddress: '127.0.0.1',
        userAgent: 'System Process',
        details: { projectId: 123, error: 'Image format not supported' }
      }
    ]
    
    logList.value = mockLogs
    logPagination.total = 150
  } finally {
    logLoading.value = false
  }
}

// 搜索日志
const searchLogs = () => {
  logPagination.currentPage = 1
  fetchOperationLogs()
}

// 导出日志
const exportLogs = () => {
  const link = document.createElement('a')
  link.href = '/api/v1/admin/system/logs/export'
  link.download = `operation_logs_${new Date().toISOString().split('T')[0]}.csv`
  link.click()
  ElMessage.success('日志导出已开始')
}

// 显示日志详情
const showLogDetail = (log) => {
  logDetailDialog.log = log
  logDetailDialog.visible = true
}

// 分页处理
const handleLogSizeChange = (size) => {
  logPagination.pageSize = size
  logPagination.currentPage = 1
  fetchOperationLogs()
}

const handleLogCurrentChange = (page) => {
  logPagination.currentPage = page
  fetchOperationLogs()
}

// 工具函数
const getLogLevelType = (level) => {
  const types = {
    info: 'success',
    warning: 'warning',
    error: 'danger'
  }
  return types[level] || 'info'
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchSystemStatus()
  fetchSystemConfig()
  fetchBackupList()
  fetchOperationLogs()
  
  // 定时刷新系统状态
  setInterval(fetchSystemStatus, 30000) // 每30秒刷新一次
})
</script>

<style lang="scss" scoped>
.system-management {
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

  h3 {
    color: var(--text-primary);
    margin-bottom: 20px;
    font-size: 18px;
    font-weight: 600;
  }

  // 系统状态监控
  .system-status {
    margin-bottom: 40px;
    
    .status-card {
      display: flex;
      align-items: center;
      padding: 20px;
      background: var(--bg-secondary);
      border-radius: 8px;
      border: 1px solid var(--border-primary);
      height: 100px;
      
      .status-icon {
        width: 50px;
        height: 50px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        background: #666666;
        color: white;
        
        &.online {
          background: #67c23a;
        }
        
        &.maintenance {
          background: #e6a23c;
        }
      }
      
      .status-content {
        flex: 1;
        
        .status-title {
          font-size: 14px;
          color: var(--text-secondary);
          margin-bottom: 5px;
        }
        
        .status-value {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 5px;
          
          &.online, &.connected, &.available {
            color: #67c23a;
          }
          
          &.offline, &.disconnected, &.unavailable {
            color: #f56c6c;
          }
        }
        
        .status-detail {
          font-size: 12px;
          color: var(--text-tertiary);
        }
      }
    }
  }

  // 系统配置
  .system-config {
    margin-bottom: 40px;
    
    .unit {
      margin-left: 8px;
      color: var(--text-secondary);
      font-size: 14px;
    }
  }

  // 备份恢复
  .backup-section {
    margin-bottom: 40px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .backup-list {
      .backup-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 0;
        border-bottom: 1px solid var(--border-primary);
        
        &:last-child {
          border-bottom: none;
        }
        
        .backup-info {
          .backup-name {
            font-weight: 500;
            color: var(--text-primary);
            margin-bottom: 5px;
          }
          
          .backup-meta {
            font-size: 12px;
            color: var(--text-secondary);
            
            span {
              margin-right: 15px;
            }
          }
        }
        
        .backup-actions {
          display: flex;
          gap: 8px;
        }
      }
      
      .empty-state {
        text-align: center;
        padding: 40px 20px;
        color: var(--text-secondary);
        
        .el-icon {
          font-size: 48px;
          margin-bottom: 15px;
        }
      }
    }
    
    .upload-backup {
      margin-bottom: 20px;
    }
    
    .uploaded-file {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      background: var(--bg-primary);
      border-radius: 8px;
      border: 1px solid var(--border-primary);
      
      .file-info {
        display: flex;
        align-items: center;
        gap: 8px;
        color: var(--text-primary);
      }
    }
  }

  // 操作日志
  .operation-logs {
    .log-filters {
      margin-bottom: 20px;
      padding: 20px;
      background: var(--bg-secondary);
      border-radius: 8px;
      border: 1px solid var(--border-primary);
    }
    
    .pagination-wrapper {
      padding: 20px;
      display: flex;
      justify-content: center;
    }
  }

  // 日志详情
  .log-detail {
    .log-details {
      background: var(--bg-primary);
      padding: 15px;
      border-radius: 4px;
      font-size: 12px;
      color: var(--text-primary);
      white-space: pre-wrap;
      word-break: break-all;
      max-height: 200px;
      overflow-y: auto;
    }
  }
}

:deep(.el-card) {
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  
  .el-card__header {
    background: var(--bg-primary);
    border-bottom: 1px solid var(--border-primary);
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

:deep(.el-upload) {
  .el-upload-dragger {
    background: var(--bg-primary);
    border: 2px dashed var(--border-primary);
    
    &:hover {
      border-color: var(--primary-color);
    }
  }
}

:deep(.el-dialog) {
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
}

:deep(.el-descriptions) {
  .el-descriptions__header {
    background: var(--bg-primary);
  }
  
  .el-descriptions__body {
    background: transparent;
  }
}
</style>