<template>
  <div class="vr-viewer" ref="viewerContainer">
    <div class="vr-canvas" ref="canvasContainer"></div>
    
    <!-- 控制面板 -->
    <div class="vr-controls" v-if="showControls">
      <div class="control-group">
        <el-button-group>
          <el-button @click="resetView" size="small">
            <el-icon><Refresh /></el-icon>
            重置视角
          </el-button>
          <el-button @click="toggleAutoRotate" size="small">
            <el-icon><VideoPlay /></el-icon>
            {{ autoRotate ? '停止' : '自动' }}旋转
          </el-button>
          <el-button @click="toggleFullscreen" size="small">
            <el-icon><FullScreen /></el-icon>
            全屏
          </el-button>
        </el-button-group>
      </div>
      
      <div class="control-group">
        <span class="control-label">视野角度:</span>
        <el-slider
          v-model="fov"
          :min="30"
          :max="120"
          :step="5"
          @change="updateFOV"
          style="width: 120px;"
        />
      </div>
    </div>

    <!-- 热点标记 -->
    <div
      v-for="hotspot in visibleHotspots"
      :key="hotspot.id"
      class="hotspot-marker"
      :style="getHotspotStyle(hotspot)"
      @click="onHotspotClick(hotspot)"
    >
      <div class="hotspot-icon" :class="getHotspotClass(hotspot.type)">
        <el-icon v-if="hotspot.type === 'info'"><InfoFilled /></el-icon>
        <el-icon v-else-if="hotspot.type === 'media'"><Picture /></el-icon>
        <el-icon v-else-if="hotspot.type === 'link'"><Link /></el-icon>
        <el-icon v-else-if="hotspot.type === 'audio'"><Microphone /></el-icon>
      </div>
      <div class="hotspot-pulse"></div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="vr-loading">
      <el-icon class="loading-icon">
        <Loading />
      </el-icon>
      <p>加载VR内容中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-if="error" class="vr-error">
      <el-icon><Warning /></el-icon>
      <p>{{ error }}</p>
      <el-button @click="retry">重试</el-button>
    </div>

    <!-- 热点内容弹框 -->
    <el-dialog
      v-model="hotspotDialog.visible"
      :title="hotspotDialog.hotspot?.name || '热点信息'"
      width="500px"
    >
      <div v-if="hotspotDialog.hotspot" class="hotspot-content">
        <div v-if="hotspotDialog.hotspot.type === 'info'" class="info-content">
          <div v-html="hotspotDialog.hotspot.content.text"></div>
          <img
            v-if="hotspotDialog.hotspot.content.image"
            :src="hotspotDialog.hotspot.content.image"
            alt="热点图片"
            class="hotspot-image"
          />
        </div>
        
        <div v-else-if="hotspotDialog.hotspot.type === 'media'" class="media-content">
          <img
            v-if="hotspotDialog.hotspot.content.type === 'image'"
            :src="hotspotDialog.hotspot.content.url"
            alt="媒体内容"
            class="media-image"
          />
          <video
            v-else-if="hotspotDialog.hotspot.content.type === 'video'"
            :src="hotspotDialog.hotspot.content.url"
            controls
            class="media-video"
          />
        </div>
        
        <div v-else-if="hotspotDialog.hotspot.type === 'link'" class="link-content">
          <p>{{ hotspotDialog.hotspot.content.description }}</p>
          <el-button type="primary" @click="openLink(hotspotDialog.hotspot.content.url)">
            访问链接
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  VideoPlay,
  FullScreen,
  InfoFilled,
  Picture,
  Link,
  Microphone,
  Warning,
  Loading
} from '@element-plus/icons-vue'

const props = defineProps({
  // VR全景图片URL
  vrUrl: {
    type: String,
    required: true
  },
  // 热点数据
  hotspots: {
    type: Array,
    default: () => []
  },
  // 是否显示控制面板
  showControls: {
    type: Boolean,
    default: true
  },
  // 是否自动旋转
  autoRotate: {
    type: Boolean,
    default: false
  },
  // 初始视野角度
  initialFov: {
    type: Number,
    default: 75
  }
})

const emit = defineEmits([
  'hotspot-click',
  'view-change',
  'load-complete',
  'load-error'
])

// 响应式数据
const viewerContainer = ref(null)
const canvasContainer = ref(null)
const loading = ref(true)
const error = ref(null)
const fov = ref(props.initialFov)
const autoRotateEnabled = ref(props.autoRotate)
const visibleHotspots = ref([])

// VR相关变量
let scene = null
let camera = null
let renderer = null
let controls = null
let sphere = null
let animationId = null

// 热点对话框
const hotspotDialog = ref({
  visible: false,
  hotspot: null
})

// 初始化VR场景
const initVRScene = async () => {
  try {
    loading.value = true
    error.value = null

    // 这里使用简化的Three.js实现
    // 在实际项目中需要引入Three.js库
    
    // 模拟加载过程
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 创建场景
    createScene()
    
    // 加载全景图片
    await loadPanorama()
    
    // 设置控制器
    setupControls()
    
    // 开始渲染
    startRender()
    
    loading.value = false
    emit('load-complete')
    
  } catch (err) {
    loading.value = false
    error.value = err.message || 'VR场景加载失败'
    emit('load-error', err)
  }
}

const createScene = () => {
  // 模拟Three.js场景创建
  console.log('Creating VR scene...')
  
  // 在实际实现中，这里会创建Three.js的Scene, Camera, Renderer等
  scene = { type: 'scene' }
  camera = { 
    type: 'camera',
    fov: fov.value,
    position: { x: 0, y: 0, z: 0 }
  }
  renderer = { type: 'renderer' }
  
  // 设置渲染器大小
  if (canvasContainer.value) {
    const rect = canvasContainer.value.getBoundingClientRect()
    console.log('Canvas size:', rect.width, rect.height)
  }
}

const loadPanorama = async () => {
  // 模拟加载全景图片
  console.log('Loading panorama:', props.vrUrl)
  
  // 在实际实现中，这里会加载纹理并应用到球体几何体
  sphere = {
    type: 'sphere',
    texture: props.vrUrl
  }
  
  return new Promise(resolve => setTimeout(resolve, 500))
}

const setupControls = () => {
  // 模拟设置轨道控制器
  controls = {
    type: 'controls',
    autoRotate: autoRotateEnabled.value,
    enableDamping: true
  }
  
  console.log('Controls setup complete')
}

const startRender = () => {
  const animate = () => {
    animationId = requestAnimationFrame(animate)
    
    // 模拟渲染循环
    if (controls?.autoRotate) {
      // 自动旋转逻辑
    }
    
    // 更新热点位置
    updateHotspotPositions()
  }
  
  animate()
}

const updateHotspotPositions = () => {
  // 将3D热点位置转换为2D屏幕坐标
  visibleHotspots.value = props.hotspots.map(hotspot => ({
    ...hotspot,
    screenPosition: projectToScreen(hotspot.position)
  })).filter(hotspot => hotspot.screenPosition.visible)
}

const projectToScreen = (position) => {
  // 模拟3D到2D投影
  // 在实际实现中，这里会使用Three.js的投影矩阵
  return {
    x: Math.random() * 100, // 模拟X坐标百分比
    y: Math.random() * 100, // 模拟Y坐标百分比
    visible: Math.random() > 0.3 // 模拟可见性
  }
}

// 控制方法
const resetView = () => {
  if (camera) {
    camera.position = { x: 0, y: 0, z: 0 }
    console.log('View reset')
  }
}

const toggleAutoRotate = () => {
  autoRotateEnabled.value = !autoRotateEnabled.value
  if (controls) {
    controls.autoRotate = autoRotateEnabled.value
  }
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    viewerContainer.value?.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const updateFOV = (newFov) => {
  if (camera) {
    camera.fov = newFov
    console.log('FOV updated to:', newFov)
  }
}

// 热点相关方法
const getHotspotStyle = (hotspot) => {
  if (!hotspot.screenPosition) return { display: 'none' }
  
  return {
    left: hotspot.screenPosition.x + '%',
    top: hotspot.screenPosition.y + '%',
    display: hotspot.screenPosition.visible ? 'block' : 'none'
  }
}

const getHotspotClass = (type) => {
  return `hotspot-${type}`
}

const onHotspotClick = (hotspot) => {
  emit('hotspot-click', hotspot)
  
  if (hotspot.type === 'audio') {
    playAudio(hotspot.content.url)
  } else {
    hotspotDialog.value.hotspot = hotspot
    hotspotDialog.value.visible = true
  }
}

const playAudio = (audioUrl) => {
  const audio = new Audio(audioUrl)
  audio.play().catch(error => {
    ElMessage.error('音频播放失败')
  })
}

const openLink = (url) => {
  window.open(url, '_blank')
  hotspotDialog.value.visible = false
}

// 错误处理
const retry = () => {
  initVRScene()
}

// 监听属性变化
watch(() => props.vrUrl, () => {
  if (props.vrUrl) {
    initVRScene()
  }
})

watch(() => props.autoRotate, (newValue) => {
  autoRotateEnabled.value = newValue
  if (controls) {
    controls.autoRotate = newValue
  }
})

// 生命周期
onMounted(async () => {
  await nextTick()
  if (props.vrUrl) {
    initVRScene()
  }
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  
  // 清理Three.js资源
  if (renderer) {
    console.log('Cleaning up VR resources')
  }
})

// 暴露方法
defineExpose({
  resetView,
  toggleAutoRotate,
  toggleFullscreen,
  updateFOV
})
</script>

<style lang="scss" scoped>
.vr-viewer {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
  border-radius: 8px;
  overflow: hidden;

  .vr-canvas {
    width: 100%;
    height: 100%;
  }

  .vr-controls {
    position: absolute;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 10px 20px;
    background: rgba(0, 0, 0, 0.7);
    border-radius: 25px;
    backdrop-filter: blur(10px);

    .control-group {
      display: flex;
      align-items: center;
      gap: 10px;

      .control-label {
        color: white;
        font-size: 12px;
        white-space: nowrap;
      }
    }
  }

  .hotspot-marker {
    position: absolute;
    width: 40px;
    height: 40px;
    cursor: pointer;
    transform: translate(-50%, -50%);
    z-index: 10;

    .hotspot-icon {
      position: relative;
      width: 100%;
      height: 100%;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 18px;
      z-index: 2;
      transition: transform 0.3s;

      &.hotspot-info {
        background: #409eff;
      }

      &.hotspot-media {
        background: #67c23a;
      }

      &.hotspot-link {
        background: #e6a23c;
      }

      &.hotspot-audio {
        background: #f56c6c;
      }

      &:hover {
        transform: scale(1.1);
      }
    }

    .hotspot-pulse {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.3);
      animation: pulse 2s infinite;
    }
  }

  .vr-loading {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    color: white;

    .loading-icon {
      font-size: 32px;
      animation: rotate 2s linear infinite;
    }

    p {
      margin-top: 15px;
      font-size: 14px;
    }
  }

  .vr-error {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    color: white;

    .el-icon {
      font-size: 48px;
      color: #f56c6c;
      margin-bottom: 15px;
    }

    p {
      margin-bottom: 20px;
      font-size: 14px;
    }
  }

  .hotspot-content {
    .info-content {
      .hotspot-image {
        width: 100%;
        max-width: 400px;
        border-radius: 8px;
        margin-top: 15px;
      }
    }

    .media-content {
      text-align: center;

      .media-image,
      .media-video {
        width: 100%;
        max-width: 400px;
        border-radius: 8px;
      }
    }

    .link-content {
      text-align: center;

      p {
        margin-bottom: 20px;
      }
    }
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
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

:deep(.el-slider) {
  .el-slider__runway {
    background: rgba(255, 255, 255, 0.3);
  }

  .el-slider__bar {
    background: var(--primary-color);
  }

  .el-slider__button {
    border-color: var(--primary-color);
  }
}
</style>