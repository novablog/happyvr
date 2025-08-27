<template>
  <el-dialog
    v-model="visible"
    :title="hotspot?.content?.title || '热点信息'"
    :width="modalWidth"
    :before-close="handleClose"
    :show-close="true"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    class="hotspot-modal"
  >
    <div class="hotspot-content" v-if="hotspot">
      <!-- 信息热点 -->
      <div v-if="hotspot.type === 'info'" class="info-content">
        <div v-if="hotspot.content.image" class="content-image">
          <img :src="hotspot.content.image" :alt="hotspot.content.title" />
        </div>
        
        <div class="content-text">
          <p v-if="hotspot.content.description">
            {{ hotspot.content.description }}
          </p>
          
          <div v-if="hotspot.content.details" class="content-details">
            <div v-for="(detail, index) in hotspot.content.details" :key="index" class="detail-item">
              <strong>{{ detail.label }}:</strong>
              <span>{{ detail.value }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 媒体热点 -->
      <div v-if="hotspot.type === 'media'" class="media-content">
        <!-- 图片媒体 -->
        <div v-if="hotspot.content.mediaType === 'image'" class="image-media">
          <img 
            :src="hotspot.content.mediaUrl" 
            :alt="hotspot.content.title"
            @load="handleImageLoad"
            @error="handleImageError"
          />
        </div>
        
        <!-- 视频媒体 -->
        <div v-if="hotspot.content.mediaType === 'video'" class="video-media">
          <video 
            :src="hotspot.content.mediaUrl"
            controls
            :autoplay="hotspot.content.autoPlay"
            :loop="hotspot.content.loop"
            @loadedmetadata="handleVideoLoad"
            @error="handleVideoError"
          >
            您的浏览器不支持视频播放
          </video>
        </div>
        
        <div v-if="hotspot.content.description" class="media-description">
          <p>{{ hotspot.content.description }}</p>
        </div>
      </div>

      <!-- 链接热点 -->
      <div v-if="hotspot.type === 'link'" class="link-content">
        <div v-if="hotspot.content.image" class="link-preview">
          <img :src="hotspot.content.image" :alt="hotspot.content.title" />
        </div>
        
        <div class="link-info">
          <p v-if="hotspot.content.description">
            {{ hotspot.content.description }}
          </p>
          
          <div class="link-url">
            <el-icon><Link /></el-icon>
            <span>{{ hotspot.content.url }}</span>
          </div>
        </div>
      </div>

      <!-- 音频热点 -->
      <div v-if="hotspot.type === 'audio'" class="audio-content">
        <div class="audio-player">
          <audio 
            ref="audioRef"
            :src="hotspot.content.audioUrl"
            :loop="hotspot.content.loop"
            :volume="hotspot.content.volume || 1.0"
            @loadedmetadata="handleAudioLoad"
            @error="handleAudioError"
            @timeupdate="handleTimeUpdate"
            @ended="handleAudioEnd"
          />
          
          <div class="player-controls">
            <el-button 
              circle 
              :icon="isPlaying ? VideoPause : VideoPlay"
              @click="togglePlay"
              :loading="audioLoading"
            />
            
            <div class="progress-container">
              <el-slider
                v-model="currentTime"
                :max="duration"
                :show-tooltip="false"
                @change="handleSeek"
                class="audio-progress"
              />
              
              <div class="time-display">
                <span>{{ formatTime(currentTime) }}</span>
                <span>/</span>
                <span>{{ formatTime(duration) }}</span>
              </div>
            </div>
            
            <div class="volume-control">
              <el-icon><Headphones /></el-icon>
              <el-slider
                v-model="volume"
                :max="1"
                :step="0.1"
                :show-tooltip="false"
                @change="handleVolumeChange"
                class="volume-slider"
              />
            </div>
          </div>
        </div>
        
        <div v-if="hotspot.content.description" class="audio-description">
          <p>{{ hotspot.content.description }}</p>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="modal-footer">
        <!-- 链接热点的操作按钮 -->
        <template v-if="hotspot?.type === 'link'">
          <el-button @click="handleClose">关闭</el-button>
          <el-button type="primary" @click="openLink">
            <el-icon><ExternalLink /></el-icon>
            访问链接
          </el-button>
        </template>
        
        <!-- 其他热点的操作按钮 -->
        <template v-else>
          <el-button @click="handleClose">关闭</el-button>
          <el-button v-if="hotspot?.content?.shareUrl" @click="shareContent">
            <el-icon><Share /></el-icon>
            分享
          </el-button>
        </template>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Link, VideoPlay, VideoPause, Headphones, 
  ExternalLink, Share 
} from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  hotspot: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'close', 'link-click', 'share'])

// 响应式数据
const audioRef = ref()
const isPlaying = ref(false)
const audioLoading = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(1.0)

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const modalWidth = computed(() => {
  if (!props.hotspot) return '500px'
  
  switch (props.hotspot.type) {
    case 'media':
      return '80%'
    case 'audio':
      return '600px'
    case 'link':
      return '500px'
    default:
      return '500px'
  }
})

// 监听热点变化
watch(() => props.hotspot, (newHotspot) => {
  if (newHotspot && newHotspot.type === 'audio') {
    nextTick(() => {
      initAudioPlayer()
    })
  }
}, { immediate: true })

// 监听弹窗显示状态
watch(visible, (newVisible) => {
  if (!newVisible) {
    // 弹窗关闭时停止音频播放
    if (audioRef.value && isPlaying.value) {
      audioRef.value.pause()
      isPlaying.value = false
    }
  }
})

// 方法
const handleClose = () => {
  visible.value = false
  emit('close')
}

const initAudioPlayer = () => {
  if (!audioRef.value) return
  
  const audio = audioRef.value
  volume.value = props.hotspot?.content?.volume || 1.0
  audio.volume = volume.value
  
  // 如果设置了自动播放
  if (props.hotspot?.content?.autoPlay) {
    togglePlay()
  }
}

const togglePlay = async () => {
  if (!audioRef.value) return
  
  try {
    audioLoading.value = true
    
    if (isPlaying.value) {
      audioRef.value.pause()
      isPlaying.value = false
    } else {
      await audioRef.value.play()
      isPlaying.value = true
    }
  } catch (error) {
    console.error('音频播放失败:', error)
    ElMessage.error('音频播放失败')
  } finally {
    audioLoading.value = false
  }
}

const handleSeek = (value) => {
  if (audioRef.value) {
    audioRef.value.currentTime = value
  }
}

const handleVolumeChange = (value) => {
  if (audioRef.value) {
    audioRef.value.volume = value
  }
}

const handleTimeUpdate = () => {
  if (audioRef.value) {
    currentTime.value = audioRef.value.currentTime
  }
}

const handleAudioLoad = () => {
  if (audioRef.value) {
    duration.value = audioRef.value.duration || 0
  }
}

const handleAudioError = (error) => {
  console.error('音频加载失败:', error)
  ElMessage.error('音频加载失败')
  audioLoading.value = false
}

const handleAudioEnd = () => {
  isPlaying.value = false
  currentTime.value = 0
}

const handleImageLoad = () => {
  // 图片加载成功
}

const handleImageError = () => {
  ElMessage.error('图片加载失败')
}

const handleVideoLoad = () => {
  // 视频加载成功
}

const handleVideoError = () => {
  ElMessage.error('视频加载失败')
}

const openLink = () => {
  if (props.hotspot?.content?.url) {
    const target = props.hotspot.content.target || '_blank'
    window.open(props.hotspot.content.url, target)
    emit('link-click', props.hotspot)
  }
}

const shareContent = () => {
  if (props.hotspot?.content?.shareUrl) {
    // 复制分享链接到剪贴板
    navigator.clipboard.writeText(props.hotspot.content.shareUrl).then(() => {
      ElMessage.success('分享链接已复制到剪贴板')
    }).catch(() => {
      ElMessage.error('复制失败')
    })
    
    emit('share', props.hotspot)
  }
}

const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return '00:00'
  
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.hotspot-modal {
  :deep(.el-dialog) {
    background: var(--bg-secondary);
    border: 1px solid var(--border-primary);
  }

  :deep(.el-dialog__header) {
    background: var(--bg-tertiary);
    border-bottom: 1px solid var(--border-primary);
    padding: 20px 24px;

    .el-dialog__title {
      color: var(--text-primary);
      font-weight: 600;
    }

    .el-dialog__headerbtn {
      color: var(--text-secondary);

      &:hover {
        color: var(--text-primary);
      }
    }
  }

  :deep(.el-dialog__body) {
    padding: 24px;
    color: var(--text-primary);
  }

  :deep(.el-dialog__footer) {
    background: var(--bg-tertiary);
    border-top: 1px solid var(--border-primary);
    padding: 16px 24px;
  }
}

.hotspot-content {
  .info-content {
    .content-image {
      margin-bottom: 16px;

      img {
        width: 100%;
        max-height: 300px;
        object-fit: cover;
        border-radius: 8px;
      }
    }

    .content-text {
      p {
        color: var(--text-secondary);
        line-height: 1.6;
        margin-bottom: 16px;
      }

      .content-details {
        .detail-item {
          display: flex;
          margin-bottom: 8px;

          strong {
            color: var(--text-primary);
            min-width: 80px;
            margin-right: 12px;
          }

          span {
            color: var(--text-secondary);
            flex: 1;
          }
        }
      }
    }
  }

  .media-content {
    .image-media {
      text-align: center;
      margin-bottom: 16px;

      img {
        max-width: 100%;
        max-height: 60vh;
        object-fit: contain;
        border-radius: 8px;
      }
    }

    .video-media {
      margin-bottom: 16px;

      video {
        width: 100%;
        max-height: 60vh;
        border-radius: 8px;
      }
    }

    .media-description {
      p {
        color: var(--text-secondary);
        line-height: 1.6;
        margin: 0;
      }
    }
  }

  .link-content {
    .link-preview {
      margin-bottom: 16px;

      img {
        width: 100%;
        max-height: 200px;
        object-fit: cover;
        border-radius: 8px;
      }
    }

    .link-info {
      p {
        color: var(--text-secondary);
        line-height: 1.6;
        margin-bottom: 16px;
      }

      .link-url {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px;
        background: var(--bg-tertiary);
        border-radius: 8px;
        border: 1px solid var(--border-secondary);

        .el-icon {
          color: var(--primary-color);
        }

        span {
          color: var(--text-secondary);
          font-family: monospace;
          word-break: break-all;
        }
      }
    }
  }

  .audio-content {
    .audio-player {
      background: var(--bg-tertiary);
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 16px;

      .player-controls {
        display: flex;
        align-items: center;
        gap: 16px;

        .el-button {
          width: 48px;
          height: 48px;
          border-radius: 50%;
          background: var(--primary-color);
          border-color: var(--primary-color);
          color: white;

          &:hover {
            background: var(--primary-light);
            border-color: var(--primary-light);
          }
        }

        .progress-container {
          flex: 1;

          .audio-progress {
            margin-bottom: 8px;

            :deep(.el-slider__runway) {
              background-color: var(--border-secondary);
            }

            :deep(.el-slider__bar) {
              background-color: var(--primary-color);
            }

            :deep(.el-slider__button) {
              border-color: var(--primary-color);
            }
          }

          .time-display {
            display: flex;
            justify-content: space-between;
            font-size: 12px;
            color: var(--text-tertiary);
            font-family: monospace;
          }
        }

        .volume-control {
          display: flex;
          align-items: center;
          gap: 8px;
          min-width: 120px;

          .el-icon {
            color: var(--text-secondary);
          }

          .volume-slider {
            flex: 1;

            :deep(.el-slider__runway) {
              background-color: var(--border-secondary);
            }

            :deep(.el-slider__bar) {
              background-color: var(--primary-color);
            }

            :deep(.el-slider__button) {
              border-color: var(--primary-color);
            }
          }
        }
      }
    }

    .audio-description {
      p {
        color: var(--text-secondary);
        line-height: 1.6;
        margin: 0;
      }
    }
  }
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式样式
@media (max-width: 768px) {
  .hotspot-content {
    .audio-content {
      .audio-player {
        padding: 16px;

        .player-controls {
          flex-direction: column;
          gap: 12px;

          .progress-container {
            width: 100%;
          }

          .volume-control {
            width: 100%;
            min-width: auto;
          }
        }
      }
    }
  }
}
</style>