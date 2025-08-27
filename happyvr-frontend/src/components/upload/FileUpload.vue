<template>
  <div class="file-upload">
    <div class="upload-area" :class="{ 'drag-over': isDragOver, 'uploading': isUploading }">
      <el-upload
        ref="uploadRef"
        :multiple="multiple"
        :accept="accept"
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleFileChange"
        :before-upload="beforeUpload"
        drag
        class="upload-dragger"
      >
        <div class="upload-content">
          <div v-if="!isUploading" class="upload-icon">
            <el-icon><UploadFilled /></el-icon>
          </div>
          <div v-else class="upload-progress">
            <el-progress
              type="circle"
              :percentage="uploadProgress"
              :width="80"
              :stroke-width="6"
            />
          </div>
          <div class="upload-text">
            <div v-if="!isUploading" class="upload-title">
              {{ multiple ? '选择多个文件或拖拽到此处' : '选择文件或拖拽到此处' }}
            </div>
            <div v-else class="upload-title">
              正在上传... {{ uploadProgress }}%
            </div>
            <div class="upload-hint">
              支持 {{ acceptText }} 格式，单个文件不超过 {{ maxSizeText }}
            </div>
          </div>
        </div>
      </el-upload>
    </div>

    <!-- 文件列表 -->
    <div v-if="fileList.length > 0" class="file-list">
      <div class="file-list-header">
        <span>已选择文件 ({{ fileList.length }})</span>
        <el-button size="small" @click="clearFiles" :disabled="isUploading">
          清空
        </el-button>
      </div>
      <div class="file-items">
        <div
          v-for="(file, index) in fileList"
          :key="file.uid"
          class="file-item"
          :class="{ 'upload-success': file.status === 'success', 'upload-error': file.status === 'error' }"
        >
          <div class="file-info">
            <div class="file-icon">
              <el-icon v-if="file.status === 'success'"><Check /></el-icon>
              <el-icon v-else-if="file.status === 'error'"><Close /></el-icon>
              <el-icon v-else><Document /></el-icon>
            </div>
            <div class="file-details">
              <div class="file-name">{{ file.name }}</div>
              <div class="file-meta">
                <span>{{ formatFileSize(file.size) }}</span>
                <span v-if="file.status === 'error'" class="error-text">
                  {{ file.errorMessage }}
                </span>
              </div>
            </div>
          </div>
          <div class="file-actions">
            <el-button
              v-if="file.status !== 'success'"
              size="small"
              @click="removeFile(index)"
              :disabled="isUploading"
            >
              移除
            </el-button>
            <el-button
              v-if="file.status === 'success' && file.url"
              size="small"
              @click="previewFile(file)"
            >
              预览
            </el-button>
          </div>
          <div v-if="file.status === 'uploading'" class="file-progress">
            <el-progress :percentage="file.progress || 0" :show-text="false" />
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div v-if="fileList.length > 0" class="upload-actions">
      <el-button
        type="primary"
        @click="startUpload"
        :loading="isUploading"
        :disabled="!hasValidFiles"
      >
        {{ isUploading ? '上传中...' : '开始上传' }}
      </el-button>
      <el-button @click="cancelUpload" :disabled="!isUploading">
        取消上传
      </el-button>
    </div>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewDialog.visible" title="文件预览" width="60%">
      <div v-if="previewDialog.file" class="file-preview">
        <img
          v-if="isImageFile(previewDialog.file)"
          :src="APIUtils.resolveFileUrl(previewDialog.file.url)"
          alt="预览图片"
          class="preview-image"
        />
        <div v-else class="preview-placeholder">
          <el-icon><Document /></el-icon>
          <p>{{ previewDialog.file.name }}</p>
          <p>此文件类型不支持预览</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  UploadFilled,
  Document,
  Check,
  Close
} from '@element-plus/icons-vue'
import { uploadImages, uploadSingleImage } from '@/api/upload'
import { FILE_TYPES, FILE_SIZE_LIMITS } from '@/api/config'
import { APIUtils } from '@/utils/api'

const props = defineProps({
  // 是否支持多文件上传
  multiple: {
    type: Boolean,
    default: true
  },
  // 接受的文件类型
  accept: {
    type: String,
    default: '.jpg,.jpeg,.png,.gif,.bmp,.webp'
  },
  // 最大文件大小（字节）
  maxSize: {
    type: Number,
    default: FILE_SIZE_LIMITS.IMAGE
  },
  // 最大文件数量
  maxCount: {
    type: Number,
    default: 10
  },
  // 是否自动上传
  autoUpload: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'upload-success',
  'upload-error',
  'upload-progress',
  'files-change'
])

// 响应式数据
const uploadRef = ref(null)
const fileList = ref([])
const isUploading = ref(false)
const uploadProgress = ref(0)
const isDragOver = ref(false)
const currentUploadTask = ref(null)

// 预览对话框
const previewDialog = ref({
  visible: false,
  file: null
})

// 计算属性
const acceptText = computed(() => {
  return props.accept.replace(/\./g, '').toUpperCase()
})

const maxSizeText = computed(() => {
  return APIUtils.formatFileSize(props.maxSize)
})

const hasValidFiles = computed(() => {
  return fileList.value.some(file => file.status !== 'error' && file.status !== 'success')
})

// 文件处理方法
const handleFileChange = (file, files) => {
  // 验证文件数量
  if (fileList.value.length >= props.maxCount) {
    ElMessage.warning(`最多只能上传 ${props.maxCount} 个文件`)
    return false
  }

  // 验证文件类型
  if (!validateFileType(file.raw)) {
    ElMessage.error(`不支持的文件类型: ${file.name}`)
    return false
  }

  // 验证文件大小
  if (!validateFileSize(file.raw)) {
    ElMessage.error(`文件大小超出限制: ${file.name}`)
    return false
  }

  // 添加到文件列表
  const fileItem = {
    uid: file.uid,
    name: file.name,
    size: file.size,
    raw: file.raw,
    status: 'ready',
    progress: 0,
    url: null,
    errorMessage: null
  }

  fileList.value.push(fileItem)
  emit('files-change', fileList.value)

  // 自动上传
  if (props.autoUpload) {
    uploadSingleFile(fileItem)
  }
}

const beforeUpload = (file) => {
  return false // 阻止自动上传
}

const validateFileType = (file) => {
  const acceptTypes = props.accept.split(',').map(type => type.trim().toLowerCase())
  const fileName = file.name.toLowerCase()
  return acceptTypes.some(type => {
    if (type.startsWith('.')) {
      return fileName.endsWith(type)
    }
    return fileName.includes(type)
  })
}

const validateFileSize = (file) => {
  return file.size <= props.maxSize
}

const removeFile = (index) => {
  fileList.value.splice(index, 1)
  emit('files-change', fileList.value)
}

const clearFiles = () => {
  fileList.value = []
  emit('files-change', fileList.value)
}

// 上传方法
const startUpload = async () => {
  const filesToUpload = fileList.value.filter(
    file => file.status !== 'success' && file.status !== 'error'
  )

  if (filesToUpload.length === 0) {
    ElMessage.warning('没有需要上传的文件')
    return
  }

  isUploading.value = true
  uploadProgress.value = 0

  try {
    if (props.multiple && filesToUpload.length > 1) {
      await uploadMultipleFiles(filesToUpload)
    } else {
      await uploadSingleFile(filesToUpload[0])
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败: ' + error.message)
  } finally {
    isUploading.value = false
    uploadProgress.value = 0
  }
}

const uploadSingleFile = async (fileItem) => {
  fileItem.status = 'uploading'
  
  try {
    const response = await uploadSingleImage(fileItem.raw, (progress) => {
      fileItem.progress = progress
      emit('upload-progress', { file: fileItem, progress })
      
      // 更新总体进度
      if (isUploading.value) {
        updateOverallProgress()
      }
    })

    fileItem.status = 'success'
    // 兼容后端返回字段 fileUrl 与旧的 url 字段
    const singleData = response?.data || response
    fileItem.url = singleData.fileUrl || singleData.url || null
    fileItem.progress = 100

    emit('upload-success', { file: fileItem, response })
    ElMessage.success(`${fileItem.name} 上传成功`)
  } catch (error) {
    fileItem.status = 'error'
    fileItem.errorMessage = error.message || '上传失败'
    
    emit('upload-error', { file: fileItem, error })
    ElMessage.error(`${fileItem.name} 上传失败`)
  }
}

const uploadMultipleFiles = async (files) => {
  const rawFiles = files.map(f => f.raw)
  
  try {
    // 标记所有文件为上传中
    files.forEach(file => {
      file.status = 'uploading'
    })

    const response = await uploadImages(rawFiles, (progress) => {
      // 更新所有文件的进度
      files.forEach(file => {
        file.progress = progress
      })
      
      uploadProgress.value = progress
      emit('upload-progress', { files, progress })
    })

    // 处理上传结果：兼容后端返回的 files 数组或直接返回数组
    const filesResult = response?.data?.files || response?.data || response
    if (Array.isArray(filesResult)) {
      filesResult.forEach((result, index) => {
        const fileItem = files[index]
        const url = result.fileUrl || result.url || null
        if (url) {
          fileItem.status = 'success'
          fileItem.url = url
          fileItem.progress = 100
        } else {
          fileItem.status = 'error'
          fileItem.errorMessage = result.error || '上传失败'
        }
      })
    }

    const successCount = files.filter(f => f.status === 'success').length
    emit('upload-success', { files, response })
    ElMessage.success(`成功上传 ${successCount} 个文件`)
  } catch (error) {
    // 标记所有文件为失败
    files.forEach(file => {
      file.status = 'error'
      file.errorMessage = error.message || '上传失败'
    })
    
    emit('upload-error', { files, error })
    ElMessage.error('批量上传失败')
  }
}

const cancelUpload = () => {
  if (currentUploadTask.value) {
    // 这里可以实现取消上传的逻辑
    currentUploadTask.value = null
  }
  
  isUploading.value = false
  uploadProgress.value = 0
  
  // 重置上传中的文件状态
  fileList.value.forEach(file => {
    if (file.status === 'uploading') {
      file.status = 'ready'
      file.progress = 0
    }
  })
}

const updateOverallProgress = () => {
  const uploadingFiles = fileList.value.filter(f => f.status === 'uploading')
  if (uploadingFiles.length > 0) {
    const totalProgress = uploadingFiles.reduce((sum, file) => sum + (file.progress || 0), 0)
    uploadProgress.value = Math.round(totalProgress / uploadingFiles.length)
  }
}

// 预览方法
const previewFile = (file) => {
  previewDialog.value.file = file
  previewDialog.value.visible = true
}

const isImageFile = (file) => {
  const imageTypes = FILE_TYPES.IMAGES
  const extension = file.name.split('.').pop()?.toLowerCase()
  return imageTypes.includes(extension)
}

// 工具方法
const formatFileSize = (bytes) => {
  return APIUtils.formatFileSize(bytes)
}

// 拖拽事件处理
const handleDragOver = (e) => {
  e.preventDefault()
  isDragOver.value = true
}

const handleDragLeave = (e) => {
  e.preventDefault()
  isDragOver.value = false
}

const handleDrop = (e) => {
  e.preventDefault()
  isDragOver.value = false
}

// 暴露方法给父组件
defineExpose({
  startUpload,
  cancelUpload,
  clearFiles,
  getFiles: () => fileList.value,
  getSuccessFiles: () => fileList.value.filter(f => f.status === 'success')
})
</script>

<style lang="scss" scoped>
.file-upload {
  .upload-area {
    margin-bottom: 20px;
    
    &.drag-over {
      .upload-dragger {
        border-color: var(--primary-color);
        background: rgba(212, 175, 55, 0.1);
      }
    }
    
    &.uploading {
      .upload-dragger {
        pointer-events: none;
        opacity: 0.8;
      }
    }
  }

  .upload-content {
    padding: 40px 20px;
    text-align: center;
    
    .upload-icon {
      font-size: 48px;
      color: var(--primary-color);
      margin-bottom: 20px;
    }
    
    .upload-progress {
      margin-bottom: 20px;
    }
    
    .upload-title {
      font-size: 16px;
      color: var(--text-primary);
      margin-bottom: 10px;
    }
    
    .upload-hint {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }

  .file-list {
    background: var(--bg-secondary);
    border-radius: 8px;
    border: 1px solid var(--border-primary);
    margin-bottom: 20px;
    
    .file-list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 20px;
      border-bottom: 1px solid var(--border-primary);
      background: var(--bg-primary);
      
      span {
        font-weight: 500;
        color: var(--text-primary);
      }
    }
    
    .file-items {
      padding: 10px;
    }
    
    .file-item {
      display: flex;
      align-items: center;
      padding: 15px;
      border-radius: 6px;
      margin-bottom: 10px;
      background: var(--bg-primary);
      border: 1px solid var(--border-primary);
      position: relative;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      &.upload-success {
        border-color: #67c23a;
        background: rgba(103, 194, 58, 0.1);
      }
      
      &.upload-error {
        border-color: #f56c6c;
        background: rgba(245, 108, 108, 0.1);
      }
      
      .file-info {
        display: flex;
        align-items: center;
        flex: 1;
        
        .file-icon {
          width: 40px;
          height: 40px;
          border-radius: 6px;
          background: var(--bg-secondary);
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 15px;
          font-size: 18px;
          
          .el-icon {
            color: var(--text-secondary);
          }
        }
        
        .file-details {
          flex: 1;
          
          .file-name {
            font-weight: 500;
            color: var(--text-primary);
            margin-bottom: 5px;
            word-break: break-all;
          }
          
          .file-meta {
            font-size: 12px;
            color: var(--text-secondary);
            
            span {
              margin-right: 15px;
            }
            
            .error-text {
              color: #f56c6c;
            }
          }
        }
      }
      
      .file-actions {
        display: flex;
        gap: 8px;
      }
      
      .file-progress {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        
        :deep(.el-progress-bar__outer) {
          border-radius: 0 0 6px 6px;
        }
      }
    }
  }

  .upload-actions {
    display: flex;
    gap: 10px;
    justify-content: center;
  }

  .file-preview {
    text-align: center;
    
    .preview-image {
      max-width: 100%;
      max-height: 500px;
      border-radius: 8px;
    }
    
    .preview-placeholder {
      padding: 60px 20px;
      color: var(--text-secondary);
      
      .el-icon {
        font-size: 48px;
        margin-bottom: 15px;
      }
      
      p {
        margin: 10px 0;
      }
    }
  }
}

:deep(.el-upload) {
  width: 100%;
  
  .el-upload-dragger {
    width: 100%;
    background: var(--bg-primary);
    border: 2px dashed var(--border-primary);
    border-radius: 8px;
    
    &:hover {
      border-color: var(--primary-color);
    }
  }
}
</style>