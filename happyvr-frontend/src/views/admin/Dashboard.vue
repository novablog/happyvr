<template>
  <div class="admin-dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.key">
        <div class="stat-icon" :style="{ backgroundColor: stat.color }">
          <el-icon>
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-change" :class="{ positive: stat.change > 0, negative: stat.change < 0 }">
            <el-icon>
              <ArrowUp v-if="stat.change > 0" />
              <ArrowDown v-else />
            </el-icon>
            {{ Math.abs(stat.change) }}%
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-row">
        <!-- 用户增长趋势 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
              <el-select v-model="userGrowthPeriod" size="small">
                <el-option label="最近7天" value="7d" />
                <el-option label="最近30天" value="30d" />
                <el-option label="最近90天" value="90d" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" ref="userGrowthChart"></div>
        </el-card>

        <!-- 项目统计 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>项目状态分布</span>
            </div>
          </template>
          <div class="chart-container" ref="projectStatsChart"></div>
        </el-card>
      </div>

      <div class="chart-row">
        <!-- 系统资源使用 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>系统资源使用</span>
              <el-tag :type="systemHealth.status === 'healthy' ? 'success' : 'danger'" size="small">
                {{ systemHealth.status === 'healthy' ? '正常' : '异常' }}
              </el-tag>
            </div>
          </template>
          <div class="resource-stats">
            <div class="resource-item" v-for="resource in systemResources" :key="resource.name">
              <div class="resource-header">
                <span class="resource-name">{{ resource.name }}</span>
                <span class="resource-value">{{ resource.value }}%</span>
              </div>
              <el-progress 
                :percentage="resource.value" 
                :color="getProgressColor(resource.value)"
                :show-text="false"
              />
            </div>
          </div>
        </el-card>

        <!-- 最近活动 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
              <el-button size="small" text @click="refreshActivities">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="activities-list">
            <div class="activity-item" v-for="activity in recentActivities" :key="activity.id">
              <div class="activity-icon" :class="activity.type">
                <el-icon>
                  <component :is="activity.icon" />
                </el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-time">{{ formatTime(activity.time) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <el-card>
        <template #header>
          <span>快捷操作</span>
        </template>
        <div class="actions-grid">
          <div class="action-item" v-for="action in quickActions" :key="action.key" @click="handleQuickAction(action.key)">
            <div class="action-icon" :style="{ backgroundColor: action.color }">
              <el-icon>
                <component :is="action.icon" />
              </el-icon>
            </div>
            <div class="action-content">
              <div class="action-title">{{ action.title }}</div>
              <div class="action-desc">{{ action.description }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User, Folder, DataAnalysis, Setting, ArrowUp, ArrowDown,
  Refresh, Plus, Edit, View, Warning
} from '@element-plus/icons-vue'

const router = useRouter()

// 响应式数据
const userGrowthPeriod = ref('30d')
const userGrowthChart = ref()
const projectStatsChart = ref()

// 统计数据
const stats = ref([
  {
    key: 'users',
    label: '总用户数',
    value: '1,234',
    change: 12.5,
    color: '#409eff',
    icon: User
  },
  {
    key: 'projects',
    label: '总项目数',
    value: '856',
    change: 8.3,
    color: '#67c23a',
    icon: Folder
  },
  {
    key: 'views',
    label: '总浏览量',
    value: '45.2K',
    change: -2.1,
    color: '#e6a23c',
    icon: View
  },
  {
    key: 'storage',
    label: '存储使用',
    value: '2.3TB',
    change: 15.7,
    color: '#f56c6c',
    icon: DataAnalysis
  }
])

// 系统健康状态
const systemHealth = ref({
  status: 'healthy'
})

// 系统资源使用
const systemResources = ref([
  { name: 'CPU使用率', value: 45 },
  { name: '内存使用率', value: 68 },
  { name: '磁盘使用率', value: 32 },
  { name: '网络带宽', value: 23 }
])

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    type: 'user',
    icon: User,
    title: '新用户注册：张三',
    time: new Date(Date.now() - 5 * 60 * 1000)
  },
  {
    id: 2,
    type: 'project',
    icon: Folder,
    title: '项目发布：虚拟展厅',
    time: new Date(Date.now() - 15 * 60 * 1000)
  },
  {
    id: 3,
    type: 'system',
    icon: Warning,
    title: '系统维护完成',
    time: new Date(Date.now() - 30 * 60 * 1000)
  },
  {
    id: 4,
    type: 'user',
    icon: Edit,
    title: '用户资料更新：李四',
    time: new Date(Date.now() - 45 * 60 * 1000)
  }
])

// 快捷操作
const quickActions = ref([
  {
    key: 'create-user',
    title: '创建用户',
    description: '添加新的系统用户',
    icon: Plus,
    color: '#409eff'
  },
  {
    key: 'review-projects',
    title: '审核项目',
    description: '查看待审核的项目',
    icon: View,
    color: '#67c23a'
  },
  {
    key: 'system-settings',
    title: '系统设置',
    description: '配置系统参数',
    icon: Setting,
    color: '#e6a23c'
  },
  {
    key: 'data-backup',
    title: '数据备份',
    description: '执行数据备份操作',
    icon: DataAnalysis,
    color: '#f56c6c'
  }
])

// 方法
const getProgressColor = (value) => {
  if (value < 50) return '#67c23a'
  if (value < 80) return '#e6a23c'
  return '#f56c6c'
}

const formatTime = (time) => {
  const now = new Date()
  const diff = now - time
  const minutes = Math.floor(diff / (1000 * 60))
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  
  const days = Math.floor(hours / 24)
  return `${days}天前`
}

const refreshActivities = () => {
  ElMessage.success('活动列表已刷新')
  // TODO: 实际刷新逻辑
}

const handleQuickAction = (actionKey) => {
  switch (actionKey) {
    case 'create-user':
      router.push('/admin/users/create')
      break
    case 'review-projects':
      router.push('/admin/projects?status=pending')
      break
    case 'system-settings':
      router.push('/admin/system/settings')
      break
    case 'data-backup':
      ElMessage.info('数据备份功能即将上线')
      break
  }
}

// 初始化图表
const initCharts = () => {
  // TODO: 使用ECharts或其他图表库初始化图表
  console.log('初始化图表')
}

// 生命周期
onMounted(() => {
  initCharts()
})

onUnmounted(() => {
  // 清理图表实例
})
</script>

<style lang="scss" scoped>
.admin-dashboard {
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-bottom: 24px;

    .stat-card {
      background: var(--bg-secondary);
      border: 1px solid var(--border-primary);
      border-radius: 8px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      transition: all 0.3s ease;

      &:hover {
        border-color: var(--primary-color);
        box-shadow: 0 4px 12px rgba(212, 175, 55, 0.1);
      }

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 20px;
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: var(--text-primary);
          line-height: 1.2;
        }

        .stat-label {
          font-size: 14px;
          color: var(--text-secondary);
          margin: 4px 0;
        }

        .stat-change {
          font-size: 12px;
          display: flex;
          align-items: center;
          gap: 4px;

          &.positive {
            color: #67c23a;
          }

          &.negative {
            color: #f56c6c;
          }
        }
      }
    }
  }

  .charts-section {
    margin-bottom: 24px;

    .chart-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .chart-card {
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

        .chart-container {
          height: 300px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: var(--text-tertiary);
          font-size: 14px;
        }

        .resource-stats {
          .resource-item {
            margin-bottom: 16px;

            &:last-child {
              margin-bottom: 0;
            }

            .resource-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 8px;

              .resource-name {
                font-size: 14px;
                color: var(--text-primary);
              }

              .resource-value {
                font-size: 14px;
                font-weight: 500;
                color: var(--text-secondary);
              }
            }
          }
        }

        .activities-list {
          max-height: 300px;
          overflow-y: auto;

          .activity-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px 0;
            border-bottom: 1px solid var(--border-secondary);

            &:last-child {
              border-bottom: none;
            }

            .activity-icon {
              width: 32px;
              height: 32px;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 14px;
              color: white;

              &.user {
                background: #409eff;
              }

              &.project {
                background: #67c23a;
              }

              &.system {
                background: #e6a23c;
              }
            }

            .activity-content {
              flex: 1;

              .activity-title {
                font-size: 14px;
                color: var(--text-primary);
                line-height: 1.4;
              }

              .activity-time {
                font-size: 12px;
                color: var(--text-tertiary);
                margin-top: 2px;
              }
            }
          }
        }
      }
    }
  }

  .quick-actions {
    .el-card {
      background: var(--bg-secondary);
      border: 1px solid var(--border-primary);

      :deep(.el-card__header) {
        background: var(--bg-tertiary);
        border-bottom: 1px solid var(--border-primary);
        font-weight: 500;
        color: var(--text-primary);
      }

      .actions-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 16px;

        .action-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 16px;
          border: 1px solid var(--border-secondary);
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            border-color: var(--primary-color);
            background: var(--bg-tertiary);
          }

          .action-icon {
            width: 40px;
            height: 40px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 18px;
          }

          .action-content {
            flex: 1;

            .action-title {
              font-size: 14px;
              font-weight: 500;
              color: var(--text-primary);
              line-height: 1.4;
            }

            .action-desc {
              font-size: 12px;
              color: var(--text-tertiary);
              margin-top: 2px;
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .admin-dashboard {
    .charts-section {
      .chart-row {
        grid-template-columns: 1fr;
      }
    }
  }
}

@media (max-width: 768px) {
  .admin-dashboard {
    .stats-grid {
      grid-template-columns: 1fr;
    }

    .quick-actions {
      .actions-grid {
        grid-template-columns: 1fr;
      }
    }
  }
}
</style>