<template>
  <div class="vr-generator">
    <div class="generator-header">
      <h3>VR全景生成</h3>
      <p>将上传的图片生成为VR全景体验</p>
    </div>

    <!-- 生成配置 -->
    <div class="generation-config">
      <el-card>
        <template #header>
          <span>生成配置</span>
        </template>
        <el-form :model="configForm" label-width="120px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="生成质量">
                <el-select v-model="configForm.quality" :disabled="isGenerating">
                  <el-option label="标准质量" value="standard" />
                  <el-option label="高质量" value="high" />
                  <el-option label="超高质量" value="ultra" />
                </el-select>
              </el-form-item>
              <el-form-item label="输出格式">
                <el-select v-model="configForm.format" :disabled="isGenerating">
                  <el-option label="全景图片" value="panorama" />
                  <el-option label="立方体贴图" value="cubemap" />
                  <el-option label="球面投影" value="equirectangular" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="分辨率">
                <el-select v-model="configForm.resolution" :disabled="isGenerating">
                  <el-option label="2K (2048x1024)" value="2k" />
                  <el-option label="4K (4096x2048)" value="4k" />
                  <el-option label="8K (8192x4096)" value="8k" />
                </el-select>
              </el-form-item>
              <el-form-item label="优化选项">
                <el-checkbox-group v-model="configForm.optimizations" :disabled="isGenerating">
                  <el-checkbox label="auto-stitch">自动拼接</el-checkbox>
                  <el-checkbox label="color-correction">色彩校正</el-checkbox>
                  <el-checkbox label="noise-reduction">降噪处理</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </div>

    <!-- 图片预览 -->
    <div v-if="sourceImages.length > 0" class="source-images">
      <el-card>
        <template #header>
          <span>源图片 ({{ sourceImages.length }})</span>
        </template>
        <div class="image-grid">
          <div
            v-for="(image, index) in sourceImages"
            :key="image.id"
            class="image-item"
          >
            <div class="image-preview">
              <img :src="APIUtils.resolveFileUrl(image.url)" :alt="image.name" />
              <div class="image-overlay">
                <el-button size="small" @click="previewImage(image)">
                  <el-icon><View /></el-icon>
                </el-button>
                <el-button size="small" @click="removeImage(index)" :disabled="isGenerating">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="image-info">
              <div class="image-name">{{ image.name }}</div>
              <div class="image-size">{{ formatFileSize(image.size) }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 生成状态 -->
    <div v-if="generationTask" class="generation-status">
      <el-card>
        <template #header>
          <span>生成状态</span>
        </template>
        <div class="status-content">
          <div class="status-info">
            <div class="status-icon" :class="getStatusClass(generationTask.status)">
              <el-icon v-if="generationTask.status === 'completed'"><Check /></el-icon>
              <el-icon v-else-if="generationTask.status === 'failed'"><Close /></el-icon>
              <el-icon v-else class="rotating"><Loading /></el-icon>
            </div>
            <div class="status-details">
              <div class="status-title">{{ getStatusText(generationTask.status) }}</div>
              <div class="status-message">{{ generationTask.message }}</div>
              <div v-if="generationTask.estimatedTime" class="status-time">
                预计剩余时间: {{ formatTime(generationTask.estimatedTime) }}
              </div>
            </div>
          </div>
          
          <div class="progress-section">
            <el-progress
              :percentage="generationTask.progress"
              :status="getProgressStatus(generationTask.status)"
              :stroke-width="8"
            />
            <div class="progress-steps">
              <div
                v-for="step in generationSteps"
                :key="step.key"
                class="step-item"
                :class="{ 
                  'active': step.key === generationTask.currentStep,
                  'completed': isStepCompleted(step.key),
                  'failed': generationTask.status === 'failed' && step.key === generationTask.currentStep
                }"
              >
                <div class="step-icon">
                  <el-icon v-if="isStepCompleted(step.key)"><Check /></el-icon>
                  <el-icon v-else-if="generationTask.status === 'failed' && step.key === generationTask.currentStep"><Close /></el-icon>
                  <span v-else>{{ step.order }}</span>
                </div>
                <div class="step-label">{{ step.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 生成结果 -->
    <div v-if="generationResult" class="generation-result">
      <el-card>
        <template #header>
          <span>生成结果</span>
        </template>
        <div class="result-content">
          <div class="result-preview">
            <div class="vr-preview-container">
              <img :src="APIUtils.resolveFileUrl(generationResult.previewUrl)" alt="VR预览" class="vr-preview-image" />
              <div class="preview-overlay">
                <el-button type="primary" @click="openVRPreview">
                  <el-icon><View /></el-icon>
                  VR预览
                </el-button>
              </div>
            </div>
          </div>
          <div class="result-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="生成时间">
                {{ formatDate(generationResult.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="处理耗时">
                {{ formatTime(generationResult.processingTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="输出格式">
                {{ generationResult.format }}
              </el-descriptions-item>
              <el-descriptions-item label="分辨率">
                {{ generationResult.resolution }}
              </el-descriptions-item>
              <el-descriptions-item label="文件大小">
                {{ formatFileSize(generationResult.fileSize) }}
              </el-descriptions-item>
              <el-descriptions-item label="质量评分">
                <el-rate v-model="generationResult.qualityScore" disabled show-score />
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
        <div class="result-actions">
          <el-button type="primary" @click="saveToProject">
            <el-icon><FolderAdd /></el-icon>
            保存到项目
          </el-button>
          <el-button @click="downloadResult">
            <el-icon><Download /></el-icon>
            下载文件
          </el-button>
          <el-button @click="shareResult">
            <el-icon><Share /></el-icon>
            分享链接
          </el-button>
          <el-button @click="regenerate">
            <el-icon><RefreshRight /></el-icon>
            重新生成
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 操作按钮 -->
    <div class="generator-actions">
      <el-button
        type="primary"
        size="large"
        @click="startGeneration"
        :loading="isGenerating"
        :disabled="!canGenerate"
      >
        <el-icon><Star /></el-icon>
        {{ isGenerating ? '生成中...' : '开始生成VR' }}
      </el-button>
      <el-button
        v-if="isGenerating"
        size="large"
        @click="cancelGeneration"
      >
        取消生成
      </el-button>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="imagePreviewDialog.visible" title="图片预览" width="60%">
      <div v-if="imagePreviewDialog.image" class="image-preview-dialog">
        <img :src="APIUtils.resolveFileUrl(imagePreviewDialog.image.url)" :alt="imagePreviewDialog.image.name" />
      </div>
    </el-dialog>

    <!-- VR预览对话框 -->
    <el-dialog v-model="vrPreviewDialog.visible" title="VR预览" width="80%" top="5vh">
      <div class="vr-preview-dialog">
        <VRViewer
          v-if="vrPreviewDialog.visible && generationResult"
          :vr-url="APIUtils.resolveFileUrl(generationResult.vrUrl)"
          :hotspots="[]"
          :auto-rotate="true"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  View,
  Delete,
  Check,
  Close,
  Loading,
  Star,
  FolderAdd,
  Download,
  Share,
  RefreshRight
} from '@element-plus/icons-vue'
import {
  generateVR,
  getVRGenerationProgress,
  cancelVRGeneration
} from '@/api/vr'
import { APIUtils } from '@/utils/api'
import VRViewer from './VRViewer.vue'

const props = defineProps({
  // 源图片列表
  images: {
    type: Array,
    default: () => []
  },
  // 项目ID
  projectId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits([
  'generation-start',
  'generation-progress',
  'generation-complete',
  'generation-error'
])

// 响应式数据
const sourceImages = ref([])
const isGenerating = ref(false)
const generationTask = ref(null)
const generationResult = ref(null)
const pollTimer = ref(null)

// 配置表单
const configForm = ref({
  quality: 'high',
  format: 'panorama',
  resolution: '4k',
  optimizations: ['auto-stitch', 'color-correction']
})

// 生成步骤
const generationSteps = ref([
  { key: 'upload', label: '上传图片', order: 1 },
  { key: 'analyze', label: '分析图片', order: 2 },
  { key: 'stitch', label: '图片拼接', order: 3 },
  { key: 'optimize', label: '优化处理', order: 4 },
  { key: 'render', label: '渲染输出', order: 5 },
  { key: 'complete', label: '生成完成', order: 6 }
])

// 开发模式下的模拟轮询实现（用于 DEV 环境）
const startMockProgressPolling = () => {
  if (pollTimer.value) {
    clearInterval(pollTimer.value)
  }

  let progress = generationTask.value?.progress || 0

  pollTimer.value = setInterval(() => {
    if (!generationTask.value || !isGenerating.value) {
      clearInterval(pollTimer.value)
      return
    }

    // 增量进度，带一点随机性以显得真实
    progress += Math.floor(Math.random() * 12) + 6
    if (progress > 100) progress = 100

    // 根据进度决定当前步骤
    const totalSteps = generationSteps.value.length
    const stepIndex = Math.min(totalSteps - 1, Math.floor((progress / 100) * totalSteps))
    const currentStep = generationSteps.value[stepIndex]?.key || 'upload'

    generationTask.value = {
      ...generationTask.value,
      progress,
      currentStep,
      message: progress < 100 ? '处理中...' : '处理完成',
      status: progress < 100 ? 'processing' : 'completed'
    }

    emit('generation-progress', generationTask.value)

    if (progress >= 100) {
      clearInterval(pollTimer.value)

      // 小延迟模拟后端返回结果
      setTimeout(() => {
        handleGenerationComplete({
          resultId: 'mock_result_' + Date.now(),
          previewUrl: '/files/mock_preview.jpg',
          vrUrl: '/files/mock_vr/',
          downloadUrl: '/files/mock_result.zip',
          fileSize: 1024 * 1024 * 20,
          processingTime: 25,
          qualityScore: 4
        })
      }, 600)
    }
  }, 1000)
}

// 对话框状态
const imagePreviewDialog = ref({
  visible: false,
  image: null
})

const vrPreviewDialog = ref({
  visible: false
})

// 计算属性
const canGenerate = computed(() => {
  return sourceImages.value.length > 0 && !isGenerating.value
})

// 监听props变化
watch(() => props.images, (newImages) => {
  sourceImages.value = [...newImages]
}, { immediate: true })

// 方法
const startGeneration = async () => {
  if (!canGenerate.value) {
    ElMessage.warning('请先上传图片')
    return
  }

  try {
    isGenerating.value = true
    generationResult.value = null

    // 调试信息
    console.log('VR生成请求数据:', {
      projectId: props.projectId,
      projectIdType: typeof props.projectId,
      imageCount: sourceImages.value.length,
      imageUrls: sourceImages.value.map(img => img.url)
    })

    const requestData = {
      projectId: Number(props.projectId),
      imageUrls: sourceImages.value.map(img => img.url),
      processingType: getProcessingType(configForm.value.format),
      options: {
        outputWidth: getResolutionWidth(configForm.value.resolution),
        outputHeight: getResolutionHeight(configForm.value.resolution),
        quality: getQualityValue(configForm.value.quality),
        enableOptimization: configForm.value.optimizations.includes('color-correction')
      }
    }

    console.log('发送到后端的请求数据:', requestData)

    // 开发环境使用mock数据
    if (import.meta.env.DEV) {
      // 模拟VR生成响应
      const mockTaskId = 'mock_task_' + Date.now()
      const mockResponse = {
        code: 200,
        success: true,
        data: {
          taskId: mockTaskId,
          status: 'processing',
          progress: 0,
          message: '开始处理图片...',
          estimatedTime: 30
        }
      }
      
      generationTask.value = {
        id: mockResponse.data.taskId,
        status: 'processing',
        progress: 0,
        message: '开始处理图片...',
        currentStep: 'upload',
        estimatedTime: mockResponse.data.estimatedTime
      }

      emit('generation-start', generationTask.value)
      
      // 模拟进度更新
      startMockProgressPolling()
      
      ElMessage.success('VR生成任务已启动（开发模式）')
      return
    }

    const response = await generateVR(requestData)
    
    generationTask.value = {
      id: response.data.taskId,
      status: 'processing',
      progress: 0,
      message: '开始处理图片...',
      currentStep: 'upload',
      estimatedTime: response.data.estimatedTime
    }

    emit('generation-start', generationTask.value)
    
    // 开始轮询进度
    startProgressPolling()
    
    ElMessage.success('VR生成任务已启动')
  } catch (error) {
    isGenerating.value = false
    ElMessage.error('启动VR生成失败: ' + error.message)
    emit('generation-error', error)
  }
}

const startProgressPolling = () => {
  if (pollTimer.value) {
    clearInterval(pollTimer.value)
  }

  pollTimer.value = setInterval(async () => {
    if (!generationTask.value || !isGenerating.value) {
      clearInterval(pollTimer.value)
      return
    }

    try {
      const response = await getVRGenerationProgress(generationTask.value.id)
      const progress = response.data

      generationTask.value = {
        ...generationTask.value,
        ...progress
      }

      emit('generation-progress', generationTask.value)

      if (progress.status === 'completed') {
        handleGenerationComplete(progress)
      } else if (progress.status === 'failed') {
        handleGenerationError(progress)
      }
    } catch (error) {
      console.error('获取生成进度失败:', error)
    }
  }, 2000) // 每2秒轮询一次
}

const handleGenerationComplete = (progress) => {
  isGenerating.value = false
  clearInterval(pollTimer.value)

  generationResult.value = {
    id: progress.resultId,
    previewUrl: progress.previewUrl,
    vrUrl: progress.vrUrl,
    downloadUrl: progress.downloadUrl,
    format: configForm.value.format,
    resolution: configForm.value.resolution,
    fileSize: progress.fileSize,
    processingTime: progress.processingTime,
    qualityScore: progress.qualityScore || 4,
    createdAt: new Date().toISOString()
  }

  emit('generation-complete', generationResult.value)
  ElMessage.success('VR生成完成！')

  // 自动预览
  setTimeout(() => {
    openVRPreview()
  }, 1000)
}

const handleGenerationError = (progress) => {
  isGenerating.value = false
  clearInterval(pollTimer.value)

  const error = new Error(progress.message || 'VR生成失败')
  emit('generation-error', error)
  ElMessage.error('VR生成失败: ' + progress.message)
}

const cancelGeneration = async () => {
  try {
    await ElMessageBox.confirm('确认取消VR生成？', '取消确认', {
      type: 'warning'
    })

    if (generationTask.value) {
      await cancelVRGeneration(generationTask.value.id)
    }

    isGenerating.value = false
    generationTask.value = null
    clearInterval(pollTimer.value)

    ElMessage.success('已取消VR生成')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消生成失败')
    }
  }
}

const removeImage = (index) => {
  sourceImages.value.splice(index, 1)
}

const previewImage = (image) => {
  imagePreviewDialog.value.image = image
  imagePreviewDialog.value.visible = true
}

const openVRPreview = () => {
  vrPreviewDialog.value.visible = true
}

const saveToProject = async () => {
  try {
    // 这里实现保存到项目的逻辑
    ElMessage.success('已保存到项目')
  } catch (error) {
    ElMessage.error('保存失败: ' + error.message)
  }
}

const downloadResult = () => {
  if (generationResult.value?.downloadUrl) {
  const link = document.createElement('a')
  link.href = APIUtils.resolveFileUrl(generationResult.value.downloadUrl)
    link.download = `vr_result_${Date.now()}.zip`
    link.click()
  }
}

const shareResult = async () => {
  if (generationResult.value?.vrUrl) {
    try {
  await navigator.clipboard.writeText(APIUtils.resolveFileUrl(generationResult.value.vrUrl))
      ElMessage.success('分享链接已复制到剪贴板')
    } catch (error) {
      ElMessage.error('复制链接失败')
    }
  }
}

const regenerate = () => {
  generationResult.value = null
  generationTask.value = null
  startGeneration()
}

// 工具方法
const getStatusClass = (status) => {
  return {
    'status-processing': status === 'processing',
    'status-completed': status === 'completed',
    'status-failed': status === 'failed'
  }
}

const getStatusText = (status) => {
  const statusMap = {
    'processing': '正在生成',
    'completed': '生成完成',
    'failed': '生成失败'
  }
  return statusMap[status] || '未知状态'
}

const getProgressStatus = (status) => {
  if (status === 'completed') return 'success'
  if (status === 'failed') return 'exception'
  return null
}

const isStepCompleted = (stepKey) => {
  if (!generationTask.value) return false
  
  const currentStepIndex = generationSteps.value.findIndex(s => s.key === generationTask.value.currentStep)
  const stepIndex = generationSteps.value.findIndex(s => s.key === stepKey)
  
  return stepIndex < currentStepIndex || generationTask.value.status === 'completed'
}

const formatFileSize = (bytes) => {
  return APIUtils.formatFileSize(bytes)
}

const formatTime = (seconds) => {
  if (!seconds) return '0秒'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟${secs}秒`
  } else {
    return `${secs}秒`
  }
}

const formatDate = (dateString) => {
  return APIUtils.formatDate(dateString)
}

// 配置转换辅助函数
const getResolutionWidth = (resolution) => {
  const resolutionMap = {
    '2k': 2048,
    '4k': 4096,
    '8k': 8192
  }
  return resolutionMap[resolution] || 4096
}

const getResolutionHeight = (resolution) => {
  const resolutionMap = {
    '2k': 1024,
    '4k': 2048,
    '8k': 4096
  }
  return resolutionMap[resolution] || 2048
}

const getQualityValue = (quality) => {
  const qualityMap = {
    'standard': 70,
    'high': 85,
    'ultra': 95
  }
  return qualityMap[quality] || 85
}

const getProcessingType = (format) => {
  const formatMap = {
    'panorama': 'PANORAMA',
    'cubemap': 'CUBE_MAP',
    'equirectangular': 'SPHERE_MAP'
  }
  return formatMap[format] || 'PANORAMA'
}

// 生命周期
onMounted(() => {
  // 初始化
})

onUnmounted(() => {
  if (pollTimer.value) {
    clearInterval(pollTimer.value)
  }
})

// 暴露方法
defineExpose({
  startGeneration,
  cancelGeneration,
  setImages: (images) => {
    sourceImages.value = [...images]
  },
  getResult: () => generationResult.value
})
</script>

<style lang="scss" scoped>
.vr-generator {
  .generator-header {
    text-align: center;
    margin-bottom: 30px;
    
    h3 {
      color: var(--primary-color);
      margin-bottom: 10px;
    }
    
    p {
      color: var(--text-secondary);
      margin: 0;
    }
  }

  .generation-config {
    margin-bottom: 30px;
  }

  .source-images {
    margin-bottom: 30px;
    
    .image-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 20px;
      
      .image-item {
        .image-preview {
          position: relative;
          aspect-ratio: 16/9;
          border-radius: 8px;
          overflow: hidden;
          margin-bottom: 10px;
          
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
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            opacity: 0;
            transition: opacity 0.3s;
            
            &:hover {
              opacity: 1;
            }
          }
        }
        
        .image-info {
          .image-name {
            font-size: 14px;
            color: var(--text-primary);
            margin-bottom: 5px;
            word-break: break-all;
          }
          
          .image-size {
            font-size: 12px;
            color: var(--text-secondary);
          }
        }
      }
    }
  }

  .generation-status {
    margin-bottom: 30px;
    
    .status-content {
      .status-info {
        display: flex;
        align-items: center;
        margin-bottom: 20px;
        
        .status-icon {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 20px;
          font-size: 24px;
          
          &.status-processing {
            background: rgba(64, 158, 255, 0.1);
            color: #409eff;
          }
          
          &.status-completed {
            background: rgba(103, 194, 58, 0.1);
            color: #67c23a;
          }
          
          &.status-failed {
            background: rgba(245, 108, 108, 0.1);
            color: #f56c6c;
          }
          
          .rotating {
            animation: rotate 2s linear infinite;
          }
        }
        
        .status-details {
          flex: 1;
          
          .status-title {
            font-size: 18px;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 5px;
          }
          
          .status-message {
            color: var(--text-secondary);
            margin-bottom: 5px;
          }
          
          .status-time {
            font-size: 12px;
            color: var(--text-tertiary);
          }
        }
      }
      
      .progress-section {
        .progress-steps {
          display: flex;
          justify-content: space-between;
          margin-top: 20px;
          
          .step-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            flex: 1;
            
            &.active {
              .step-icon {
                background: var(--primary-color);
                color: white;
              }
            }
            
            &.completed {
              .step-icon {
                background: #67c23a;
                color: white;
              }
            }
            
            &.failed {
              .step-icon {
                background: #f56c6c;
                color: white;
              }
            }
            
            .step-icon {
              width: 32px;
              height: 32px;
              border-radius: 50%;
              background: var(--bg-secondary);
              border: 2px solid var(--border-primary);
              display: flex;
              align-items: center;
              justify-content: center;
              margin-bottom: 8px;
              font-size: 14px;
              font-weight: 600;
            }
            
            .step-label {
              font-size: 12px;
              color: var(--text-secondary);
              text-align: center;
            }
          }
        }
      }
    }
  }

  .generation-result {
    margin-bottom: 30px;
    
    .result-content {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      
      .result-preview {
        flex: 1;
        
        .vr-preview-container {
          position: relative;
          aspect-ratio: 2/1;
          border-radius: 8px;
          overflow: hidden;
          
          .vr-preview-image {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
          
          .preview-overlay {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
          }
        }
      }
      
      .result-info {
        flex: 1;
      }
    }
    
    .result-actions {
      display: flex;
      gap: 10px;
      justify-content: center;
    }
  }

  .generator-actions {
    text-align: center;
    padding: 30px 0;
  }

  .image-preview-dialog {
    text-align: center;
    
    img {
      max-width: 100%;
      max-height: 70vh;
      border-radius: 8px;
    }
  }

  .vr-preview-dialog {
    height: 60vh;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>