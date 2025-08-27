<template>
  <div class="error-message" :class="errorClass">
    <div class="error-icon">
      <el-icon v-if="type === 'error'"><CircleClose /></el-icon>
      <el-icon v-else-if="type === 'warning'"><Warning /></el-icon>
      <el-icon v-else-if="type === 'info'"><InfoFilled /></el-icon>
      <el-icon v-else><QuestionFilled /></el-icon>
    </div>
    
    <div class="error-content">
      <div class="error-title" v-if="title">{{ title }}</div>
      <div class="error-description">{{ message }}</div>
      
      <div class="error-actions" v-if="showActions">
        <el-button 
          v-if="showRetry" 
          type="primary" 
          size="small" 
          @click="handleRetry"
        >
          重试
        </el-button>
        <el-button 
          v-if="showClose" 
          size="small" 
          @click="handleClose"
        >
          关闭
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// Props
const props = defineProps({
  type: {
    type: String,
    default: 'error',
    validator: (value) => ['error', 'warning', 'info', 'default'].includes(value)
  },
  title: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    required: true
  },
  showRetry: {
    type: Boolean,
    default: false
  },
  showClose: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['retry', 'close'])

// 计算属性
const errorClass = computed(() => ({
  [`error-${props.type}`]: true
}))

const showActions = computed(() => props.showRetry || props.showClose)

// 方法
const handleRetry = () => {
  emit('retry')
}

const handleClose = () => {
  emit('close')
}
</script>

<style lang="scss" scoped>
.error-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid;
  background-color: var(--bg-secondary);

  .error-icon {
    flex-shrink: 0;
    font-size: 20px;
    margin-top: 2px;
  }

  .error-content {
    flex: 1;
    min-width: 0;

    .error-title {
      font-weight: 600;
      margin-bottom: 4px;
      font-size: 14px;
    }

    .error-description {
      font-size: 14px;
      line-height: 1.5;
      word-break: break-word;
    }

    .error-actions {
      margin-top: 12px;
      display: flex;
      gap: 8px;
    }
  }

  // 不同类型的样式
  &.error-error {
    border-color: var(--danger-color);
    
    .error-icon {
      color: var(--danger-color);
    }
    
    .error-title {
      color: var(--danger-color);
    }
  }

  &.error-warning {
    border-color: var(--warning-color);
    
    .error-icon {
      color: var(--warning-color);
    }
    
    .error-title {
      color: var(--warning-color);
    }
  }

  &.error-info {
    border-color: var(--info-color);
    
    .error-icon {
      color: var(--info-color);
    }
    
    .error-title {
      color: var(--info-color);
    }
  }

  &.error-default {
    border-color: var(--border-primary);
    
    .error-icon {
      color: var(--text-secondary);
    }
    
    .error-title {
      color: var(--text-primary);
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .error-message {
    padding: 12px;
    
    .error-content {
      .error-title {
        font-size: 13px;
      }
      
      .error-description {
        font-size: 13px;
      }
      
      .error-actions {
        margin-top: 10px;
        
        .el-button {
          font-size: 12px;
        }
      }
    }
  }
}
</style>