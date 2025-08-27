<template>
  <div class="admin-dashboard">
    <!-- 统计卡片区域 -->
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon user-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon project-icon">
            <el-icon><VideoCamera /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalProjects }}</div>
            <div class="stat-label">VR项目数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon active-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.activeUsers }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon storage-icon">
            <el-icon><FolderOpened /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ formatFileSize(stats.storageUsed) }}</div>
            <div class="stat-label">存储使用</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>用户增长趋势</span>
            <el-select v-model="userChartPeriod" size="small" style="width: 120px">
              <el-option label="最近7天" value="7d" />
              <el-option label="最近30天" value="30d" />
              <el-option label="最近90天" value="90d" />
            </el-select>
          </div>
        </template>
        <div class="chart-container" ref="userChartRef"></div>
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>项目创建统计</span>
            <el-select v-model="projectChartPeriod" size="small" style="width: 120px">
              <el-option label="最近7天" value="7d" />
              <el-option label="最近30天" value="30d" />
              <el-option label="最近90天" value="90d" />
            </el-select>
          </div>
        </template>
        <div class="chart-container" ref="projectChartRef"></div>
      </el-card>
    </div>

    <!-- 最新活动 -->
    <div class="activity-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>最新活动</span>
            <el-button type="primary" size="small" @click="refreshActivity">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>
        <el-table :data="recentActivities" style="width: 100%" v-loading="activityLoading">
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getActivityTagType(row.type)">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="user" label="用户" width="120" />
          <el-table-column prop="createdAt" label="时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="viewActivityDetail(row)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User, VideoCamera, TrendCharts, FolderOpened, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 响应式数据
const stats = reactive({
  totalUsers: 0,
  totalProjects: 0,
  activeUsers: 0,
  storageUsed: 0
})

const userChartPeriod = ref('30d')
const projectChartPeriod = ref('30d')
const recentActivities = ref([])
const activityLoading = ref(false)

// 图表引用
const userChartRef = ref(null)
const projectChartRef = ref(null)
let userChart = null
let projectChart = null

// 生命周期
onMounted(async () => {
  await loadDashboardData()
  await nextTick()
  initCharts()
})

// 方法
const loadDashboardData = async () => {
  try {
    // 模拟数据加载
    stats.totalUsers = 1248
    stats.totalProjects = 356
    stats.activeUsers = 89
    stats.storageUsed = 2.5 * 1024 * 1024 * 1024 // 2.5GB

    // 模拟最新活动数据
    recentActivities.value = [
      {
        id: 1,
        type: '用户注册',
        description: '新用户注册成功',
        user: 'user123',
        createdAt: new Date(Date.now() - 1000 * 60 * 30) // 30分钟前
      },
      {
        id: 2,
        type: 'VR创建',
        description: '创建了新的VR项目"办公室全景"',
        user: 'designer01',
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2) // 2小时前
      },
      {
        id: 3,
        type: '项目分享',
        description: '分享了VR项目"展厅漫游"',
        user: 'creator99',
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 4) // 4小时前
      }
    ]
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

const initCharts = () => {
  // 初始化用户增长图表
  if (userChartRef.value) {
    userChart = echarts.init(userChartRef.value)
    updateUserChart()
  }

  // 初始化项目统计图表
  if (projectChartRef.value) {
    projectChart = echarts.init(projectChartRef.value)
    updateProjectChart()
  }
}

const updateUserChart = () => {
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: '#d4af37',
      textStyle: { color: '#fff' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
      axisLine: { lineStyle: { color: '#666' } },
      axisLabel: { color: '#999' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#666' } },
      axisLabel: { color: '#999' },
      splitLine: { lineStyle: { color: '#333' } }
    },
    series: [{
      name: '新增用户',
      type: 'line',
      data: [120, 132, 101, 134, 90, 230, 210],
      smooth: true,
      lineStyle: { color: '#d4af37' },
      itemStyle: { color: '#d4af37' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(212, 175, 55, 0.3)' },
            { offset: 1, color: 'rgba(212, 175, 55, 0.1)' }
          ]
        }
      }
    }]
  }
  userChart.setOption(option)
}

const updateProjectChart = () => {
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: '#d4af37',
      textStyle: { color: '#fff' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
      axisLine: { lineStyle: { color: '#666' } },
      axisLabel: { color: '#999' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#666' } },
      axisLabel: { color: '#999' },
      splitLine: { lineStyle: { color: '#333' } }
    },
    series: [{
      name: '新增项目',
      type: 'bar',
      data: [45, 52, 38, 67, 43, 89, 76],
      itemStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#d4af37' },
            { offset: 1, color: 'rgba(212, 175, 55, 0.6)' }
          ]
        }
      }
    }]
  }
  projectChart.setOption(option)
}

const refreshActivity = async () => {
  activityLoading.value = true
  try {
    await loadDashboardData()
    ElMessage.success('活动数据已刷新')
  } catch (error) {
    ElMessage.error('刷新失败')
  } finally {
    activityLoading.value = false
  }
}

const getActivityTagType = (type) => {
  const typeMap = {
    '用户注册': 'success',
    'VR创建': 'primary',
    '项目分享': 'warning',
    '系统操作': 'info'
  }
  return typeMap[type] || 'info'
}

const viewActivityDetail = (activity) => {
  ElMessage.info(`查看活动详情: ${activity.description}`)
}

const formatTime = (date) => {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  background: #0a0a0a;
  min-height: 100vh;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: #1a1a1a;
  border: 1px solid #333;
  border-radius: 8px;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.project-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.active-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.storage-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #d4af37;
  margin-bottom: 5px;
}

.stat-label {
  color: #999;
  font-size: 14px;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.chart-card {
  background: #1a1a1a;
  border: 1px solid #333;
  border-radius: 8px;
}

.chart-card :deep(.el-card__header) {
  background: #1a1a1a;
  border-bottom: 1px solid #333;
}

.chart-card :deep(.el-card__body) {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.activity-section {
  margin-top: 30px;
}

.activity-section .el-card {
  background: #1a1a1a;
  border: 1px solid #333;
  border-radius: 8px;
}

.activity-section :deep(.el-card__header) {
  background: #1a1a1a;
  border-bottom: 1px solid #333;
}

.activity-section :deep(.el-table) {
  background: transparent;
}

.activity-section :deep(.el-table th) {
  background: #2a2a2a;
  color: #fff;
  border-bottom: 1px solid #333;
}

.activity-section :deep(.el-table td) {
  background: transparent;
  color: #ccc;
  border-bottom: 1px solid #333;
}

.activity-section :deep(.el-table tr:hover > td) {
  background: #2a2a2a;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>