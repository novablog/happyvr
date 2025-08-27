<template>
  <div class="vr-preview">
    <div class="preview-header">
      <div class="header-left">
        <h1 class="project-title">{{ project.title || 'VR预览' }}</h1>
        <p class="project-author">by {{ project.author || '匿名用户' }}</p>
      </div>
      <div class="header-right">
        <el-button @click="toggleFullscreen">
          <el-icon>
            <FullScreen />
          </el-icon>
          {{ isFullscreen ? '退出全屏' : '全屏' }}
        </el-button>
        <el-button @click="goHome">
          <el-icon>
            <House />
          </el-icon>
          返回首页
        </el-button>
      </div>
    </div>

    <div class="preview-container" ref="previewContainer">
      <!-- VR渲染容器 -->
      <div class="vr-viewer" ref="vrContainer" v-loading="loading">
        <div v-if="error" class="error-state">
          <el-icon class="error-icon">
            <Warning />
          </el-icon>
          <p>加载失败</p>
          <p class="error-message">{{ error }}</p>
          <el-button type="primary" @click="retry">重试</el-button>
        </div>
      </div>

      <!-- 加载进度 -->
      <div class="loading-progress" v-if="loading && loadProgress > 0">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: loadProgress + '%' }"></div>
        </div>
        <p class="progress-text">加载中... {{ Math.round(loadProgress) }}%</p>
      </div>

      <!-- VR控制面板 -->
      <div class="vr-controls" v-if="vrSceneManager && !loading && !error">
        <div class="control-group">
          <el-tooltip content="重置视角" placement="top">
            <el-button circle @click="resetView" :class="{ active: false }">
              <el-icon>
                <Refresh />
              </el-icon>
            </el-button>
          </el-tooltip>

          <el-tooltip content="自动旋转" placement="top">
            <el-button circle @click="toggleAutoRotate" :class="{ active: isAutoRotating }">
              <el-icon>
                <VideoPlay />
              </el-icon>
            </el-button>
          </el-tooltip>

          <el-tooltip content="截图" placement="top">
            <el-button circle @click="takeScreenshot">
              <el-icon>
                <Camera />
              </el-icon>
            </el-button>
          </el-tooltip>

          <el-tooltip content="热点开关" placement="top">
            <el-button circle @click="toggleHotspots" :class="{ active: hotspotsVisible }">
              <el-icon>
                <Location />
              </el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>

      <!-- 热点弹窗 -->
      <HotspotModal 
        v-model="hotspotModalVisible"
        :hotspot="selectedHotspot"
        @close="closeHotspotModal"
        @link-click="handleHotspotLinkClick"
        @share="handleHotspotShare"
      />

      <!-- 音频控制面板 -->
      <div class="audio-controls" v-if="audioManager && (backgroundMusicUrl || hasAudioHotspots)">
        <div class="audio-panel">
          <div class="music-controls" v-if="backgroundMusicUrl">
            <el-button 
              circle 
              size="small"
              :icon="isMusicPlaying ? VideoPause : VideoPlay"
              @click="toggleBackgroundMusic"
            />
            <span class="music-title">背景音乐</span>
          </div>
          
          <div class="volume-controls">
            <el-button 
              circle 
              size="small"
              :icon="isMuted ? Mute : Headphones"
              @click="toggleMute"
            />
            <el-slider
              v-model="masterVolume"
              :min="0"
              :max="1"
              :step="0.1"
              @change="handleVolumeChange"
              class="volume-slider"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 项目信息面板 -->
    <div class="info-panel" v-if="project.description">
      <h3>项目介绍</h3>
      <p>{{ project.description }}</p>
      <div class="project-stats" v-if="project.stats">
        <span class="stat-item">
          <el-icon>
            <View />
          </el-icon>
          {{ project.stats.viewCount || 0 }} 次浏览
        </span>
        <span class="stat-item">
          <el-icon>
            <Timer />
          </el-icon>
          {{ formatDate(project.createdAt) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  FullScreen, House, Warning, Refresh, VideoPlay, VideoPause, Camera,
  Location, View, Timer, Mute, Headphones
} from '@element-plus/icons-vue'
import VRSceneManager from '@/utils/vrSceneManager'
import AudioManager from '@/utils/audioManager'
import HotspotModal from '@/components/vr/HotspotModal.vue'
import api from '@/utils/api'

const route = useRoute()
const router = useRouter()

// 响应式数据
const previewContainer = ref()
const vrContainer = ref()
const loading = ref(true)
const loadProgress = ref(0)
const error = ref('')
const isFullscreen = ref(false)
const isAutoRotating = ref(false)
const hotspotsVisible = ref(true)
const hotspotModalVisible = ref(false)
const selectedHotspot = ref(null)

// 音频相关
const backgroundMusicUrl = ref('')
const hasAudioHotspots = ref(false)
const isMusicPlaying = ref(false)
const isMuted = ref(false)
const masterVolume = ref(1.0)

// VR场景管理器和音频管理器
let vrSceneManager = null
let audioManager = null

const project = ref({
  title: '',
  author: '',
  description: '',
  panoramaUrl: '',
  hotspots: [],
  stats: null,
  createdAt: ''
})

// 方法
const loadProject = async () => {
  const shareToken = route.params.shareToken

  try {
    loading.value = true
    error.value = ''
    loadProgress.value = 0

    // 加载项目数据
    const response = await api.get(`/vr/preview/${shareToken}`)
    const projectData = response.data.data

    project.value = {
      ...projectData,
      stats: projectData.stats || { viewCount: 0 }
    }

    // 等待DOM更新
    await nextTick()

    // 初始化音频管理器
    await initAudioManager()

    // 初始化VR场景
    await initVRScene()

    // 加载全景图片
    if (project.value.panoramaUrl) {
      await loadPanorama(project.value.panoramaUrl)
    }

    // 加载热点
    if (project.value.hotspots && project.value.hotspots.length > 0) {
      await loadHotspots(project.value.hotspots)
    }

    // 加载背景音乐
    if (project.value.backgroundMusic) {
      await loadBackgroundMusic(project.value.backgroundMusic)
    }

  } catch (err) {
    console.error('加载项目失败:', err)
    error.value = err.message || '加载项目失败'
  } finally {
    loading.value = false
  }
}

const initVRScene = async () => {
  if (!vrContainer.value) return

  try {
    // 创建VR场景管理器
    vrSceneManager = new VRSceneManager(vrContainer.value, {
      enableControls: true,
      enableAutoRotate: false,
      enableZoom: true,
      enableDamping: true
    })

    // 监听场景事件
    vrSceneManager.on('loadProgress', (progress) => {
      loadProgress.value = progress.percentage
    })

    vrSceneManager.on('loadComplete', () => {
      ElMessage.success('VR场景加载完成')
    })

    vrSceneManager.on('loadError', (error) => {
      console.error('VR场景加载失败:', error)
      ElMessage.error('VR场景加载失败')
    })

    vrSceneManager.on('hotspotClick', (hotspot) => {
      selectedHotspot.value = hotspot
      hotspotModalVisible.value = true
      
      // 如果热点有音频，播放音频
      if (hotspot.type === 'audio' && audioManager) {
        audioManager.playHotspotAudio(hotspot.id)
      }
    })

    vrSceneManager.on('fullscreenChange', (fullscreen) => {
      isFullscreen.value = fullscreen
    })

    vrSceneManager.on('autoRotateToggle', (rotating) => {
      isAutoRotating.value = rotating
    })

    vrSceneManager.on('screenshot', (data) => {
      downloadScreenshot(data.dataURL)
    })

  } catch (error) {
    console.error('初始化VR场景失败:', error)
    throw error
  }
}

const loadPanorama = async (imageUrl) => {
  if (!vrSceneManager) return

  try {
    await vrSceneManager.loadPanorama(imageUrl)
  } catch (error) {
    console.error('加载全景图片失败:', error)
    throw error
  }
}

const loadHotspots = async (hotspots) => {
  if (!vrSceneManager) return

  // 检查是否有音频热点
  hasAudioHotspots.value = hotspots.some(hotspot => hotspot.type === 'audio')

  for (const hotspotData of hotspots) {
    vrSceneManager.addHotspot(hotspotData)
    
    // 如果是音频热点，预加载音频
    if (hotspotData.type === 'audio' && hotspotData.content?.audioUrl && audioManager) {
      try {
        await audioManager.createHotspotAudio(hotspotData.id, hotspotData.content.audioUrl, {
          loop: hotspotData.content.loop || false,
          volume: hotspotData.content.volume || 1.0
        })
      } catch (error) {
        console.error('加载热点音频失败:', error)
      }
    }
  }
}

const retry = () => {
  loadProject()
}

const toggleFullscreen = () => {
  if (vrSceneManager) {
    vrSceneManager.toggleFullscreen()
  }
}

const goHome = () => {
  router.push('/explore')
}

const resetView = () => {
  if (vrSceneManager) {
    vrSceneManager.resetView()
    ElMessage.success('视角已重置')
  }
}

const toggleAutoRotate = () => {
  if (vrSceneManager) {
    vrSceneManager.toggleAutoRotate()
  }
}

const takeScreenshot = () => {
  if (vrSceneManager) {
    vrSceneManager.takeScreenshot()
  }
}

const toggleHotspots = () => {
  hotspotsVisible.value = !hotspotsVisible.value

  if (vrSceneManager) {
    vrSceneManager.hotspots.forEach(hotspot => {
      hotspot.setVisible(hotspotsVisible.value)
    })
  }

  ElMessage.info(hotspotsVisible.value ? '热点已显示' : '热点已隐藏')
}

const initAudioManager = async () => {
  try {
    audioManager = new AudioManager()
    
    // 监听音频事件
    audioManager.on('musicPlay', () => {
      isMusicPlaying.value = true
    })
    
    audioManager.on('musicPause', () => {
      isMusicPlaying.value = false
    })
    
    audioManager.on('musicStop', () => {
      isMusicPlaying.value = false
    })
    
    audioManager.on('muteToggle', (muted) => {
      isMuted.value = muted
    })
    
    audioManager.on('masterVolumeChange', (volume) => {
      masterVolume.value = volume
    })
    
  } catch (error) {
    console.error('初始化音频管理器失败:', error)
  }
}

const loadBackgroundMusic = async (musicUrl) => {
  if (!audioManager || !musicUrl) return
  
  try {
    backgroundMusicUrl.value = musicUrl
    await audioManager.loadBackgroundMusic(musicUrl, {
      loop: true,
      autoPlay: false
    })
  } catch (error) {
    console.error('加载背景音乐失败:', error)
  }
}

const toggleBackgroundMusic = () => {
  if (!audioManager || !backgroundMusicUrl.value) return
  
  if (isMusicPlaying.value) {
    audioManager.pauseBackgroundMusic()
  } else {
    audioManager.playBackgroundMusic()
  }
}

const toggleMute = () => {
  if (audioManager) {
    audioManager.toggleMute()
  }
}

const handleVolumeChange = (volume) => {
  if (audioManager) {
    audioManager.setMasterVolume(volume)
  }
}

const closeHotspotModal = () => {
  hotspotModalVisible.value = false
  selectedHotspot.value = null
}

const handleHotspotLinkClick = (hotspot) => {
  console.log('热点链接点击:', hotspot)
}

const handleHotspotShare = (hotspot) => {
  console.log('热点分享:', hotspot)
}

const downloadScreenshot = (dataURL) => {
  const link = document.createElement('a')
  link.download = `vr-screenshot-${Date.now()}.png`
  link.href = dataURL
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  ElMessage.success('截图已保存')
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 监听全屏状态变化
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

// 生命周期
onMounted(() => {
  loadProject()
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)

  // 销毁VR场景管理器
  if (vrSceneManager) {
    vrSceneManager.destroy()
    vrSceneManager = null
  }
  
  // 销毁音频管理器
  if (audioManager) {
    audioManager.destroy()
    audioManager = null
  }
})
</script>

<style lang="scss" scoped>
.vr-preview {
  min-height: 100vh;
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;

  .preview-header {
    background: var(--bg-secondary);
    border-bottom: 1px solid var(--border-primary);
    padding: 16px 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      .project-title {
        font-size: 20px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 4px 0;
      }

      .project-author {
        font-size: 14px;
        color: var(--text-secondary);
        margin: 0;
      }
    }

    .header-right {
      display: flex;
      gap: 12px;
    }
  }

  .preview-container {
    flex: 1;
    position: relative;
    display: flex;
    flex-direction: column;

    .vr-viewer {
      flex: 1;
      background: var(--bg-tertiary);
      min-height: 500px;
      position: relative;

      .error-state {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        text-align: center;
        color: var(--text-secondary);

        .error-icon {
          font-size: 64px;
          color: var(--danger-color);
          margin-bottom: 16px;
        }

        p {
          font-size: 18px;
          margin: 8px 0;

          &.error-message {
            font-size: 14px;
            color: var(--danger-color);
          }
        }
      }
    }

    .loading-progress {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      text-align: center;
      z-index: 10;

      .progress-bar {
        width: 200px;
        height: 4px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 2px;
        overflow: hidden;
        margin-bottom: 12px;

        .progress-fill {
          height: 100%;
          background: var(--primary-color);
          transition: width 0.3s ease;
        }
      }

      .progress-text {
        color: var(--text-primary);
        font-size: 14px;
        margin: 0;
      }
    }

    .vr-controls {
      position: absolute;
      bottom: 24px;
      left: 50%;
      transform: translateX(-50%);
      background: rgba(0, 0, 0, 0.7);
      border-radius: 24px;
      padding: 12px;
      backdrop-filter: blur(10px);
      z-index: 100;

      .control-group {
        display: flex;
        gap: 8px;

        .el-button {
          width: 44px;
          height: 44px;
          border-radius: 50%;
          background: rgba(255, 255, 255, 0.1);
          border: 1px solid rgba(255, 255, 255, 0.2);
          color: var(--text-primary);
          transition: all 0.3s ease;

          &:hover {
            background: rgba(212, 175, 55, 0.2);
            border-color: var(--primary-color);
            color: var(--primary-color);
          }

          &.active {
            background: rgba(212, 175, 55, 0.3);
            border-color: var(--primary-color);
            color: var(--primary-color);
          }
        }
      }
    }

    .audio-controls {
      position: absolute;
      top: 24px;
      right: 24px;
      background: rgba(0, 0, 0, 0.7);
      border-radius: 12px;
      padding: 16px;
      backdrop-filter: blur(10px);
      z-index: 100;

      .audio-panel {
        display: flex;
        flex-direction: column;
        gap: 12px;
        min-width: 200px;

        .music-controls {
          display: flex;
          align-items: center;
          gap: 8px;

          .el-button {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            color: var(--text-primary);

            &:hover {
              background: rgba(212, 175, 55, 0.2);
              border-color: var(--primary-color);
              color: var(--primary-color);
            }
          }

          .music-title {
            color: var(--text-secondary);
            font-size: 14px;
          }
        }

        .volume-controls {
          display: flex;
          align-items: center;
          gap: 8px;

          .el-button {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            color: var(--text-secondary);

            &:hover {
              background: rgba(212, 175, 55, 0.2);
              border-color: var(--primary-color);
              color: var(--primary-color);
            }
          }

          .volume-slider {
            flex: 1;

            :deep(.el-slider__runway) {
              background-color: rgba(255, 255, 255, 0.2);
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

    .hotspot-info {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 200;
      backdrop-filter: blur(5px);

      .hotspot-content {
        background: var(--bg-secondary);
        border: 1px solid var(--border-primary);
        border-radius: 12px;
        max-width: 500px;
        max-height: 70vh;
        overflow-y: auto;
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);

        .hotspot-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 20px 24px 16px;
          border-bottom: 1px solid var(--border-primary);

          h3 {
            font-size: 18px;
            font-weight: 600;
            color: var(--text-primary);
            margin: 0;
          }

          .el-button {
            color: var(--text-secondary);

            &:hover {
              color: var(--text-primary);
            }
          }
        }

        .hotspot-body {
          padding: 20px 24px;

          .hotspot-image {
            width: 100%;
            max-height: 200px;
            object-fit: cover;
            border-radius: 8px;
            margin-bottom: 16px;
          }

          p {
            color: var(--text-secondary);
            line-height: 1.6;
            margin: 0 0 16px 0;
          }

          .hotspot-actions {
            display: flex;
            justify-content: flex-end;
            gap: 12px;
          }
        }
      }
    }
  }

  .info-panel {
    background: var(--bg-secondary);
    border-top: 1px solid var(--border-primary);
    padding: 24px;

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 12px 0;
    }

    p {
      color: var(--text-secondary);
      line-height: 1.6;
      margin: 0 0 16px 0;
    }

    .project-stats {
      display: flex;
      gap: 24px;
      align-items: center;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        color: var(--text-tertiary);

        .el-icon {
          font-size: 16px;
        }
      }
    }
  }
}

// 响应式样式
@media (max-width: 768px) {
  .vr-preview {
    .preview-header {
      padding: 12px 16px;
      flex-direction: column;
      gap: 12px;
      align-items: stretch;

      .header-left {
        text-align: center;
      }

      .header-right {
        justify-content: center;
      }
    }

    .preview-container {
      .vr-controls {
        bottom: 16px;
        padding: 8px;

        .control-group {
          gap: 6px;

          .el-button {
            width: 40px;
            height: 40px;
          }
        }
      }

      .audio-controls {
        top: 16px;
        right: 16px;
        padding: 12px;

        .audio-panel {
          min-width: 150px;

          .music-controls {
            .el-button {
              width: 28px;
              height: 28px;
            }

            .music-title {
              font-size: 12px;
            }
          }

          .volume-controls {
            .el-button {
              width: 28px;
              height: 28px;
            }
          }
        }
      }

      .hotspot-info {
        padding: 16px;

        .hotspot-content {
          max-width: none;
          width: 100%;
          max-height: 80vh;

          .hotspot-header {
            padding: 16px 20px 12px;

            h3 {
              font-size: 16px;
            }
          }

          .hotspot-body {
            padding: 16px 20px;

            .hotspot-image {
              max-height: 150px;
            }
          }
        }
      }

      .loading-progress {
        .progress-bar {
          width: 150px;
        }

        .progress-text {
          font-size: 12px;
        }
      }
    }

    .info-panel {
      padding: 16px;

      .project-stats {
        flex-direction: column;
        gap: 12px;
        align-items: flex-start;
      }
    }
  }
}
</style>