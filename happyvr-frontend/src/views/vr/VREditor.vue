<template>
  <div class="vr-editor">
    <!-- 编辑器头部 -->
    <div class="editor-header">
      <div class="header-left">
        <h1 class="project-title">{{ project.title || 'VR编辑器' }}</h1>
        <p class="project-subtitle">编辑您的VR项目</p>
      </div>
      <div class="header-right">
        <el-button @click="saveProject" :loading="saving" type="primary">
          <el-icon><DocumentAdd /></el-icon>
          保存项目
        </el-button>
        <el-button @click="previewProject">
          <el-icon><View /></el-icon>
          预览
        </el-button>
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-main">
      <!-- 工具栏 -->
      <div class="editor-toolbar">
        <div class="tool-group">
          <el-tooltip content="选择工具" placement="bottom">
            <el-button 
              :class="{ active: currentTool === 'select' }"
              @click="setTool('select')"
            >
              <el-icon><Pointer /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-tooltip content="添加热点" placement="bottom">
            <el-button 
              :class="{ active: currentTool === 'hotspot' }"
              @click="setTool('hotspot')"
            >
              <el-icon><Location /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-tooltip content="移动工具" placement="bottom">
            <el-button 
              :class="{ active: currentTool === 'move' }"
              @click="setTool('move')"
            >
              <el-icon><Rank /></el-icon>
            </el-button>
          </el-tooltip>
        </div>

        <div class="tool-group">
          <el-tooltip content="重置视角" placement="bottom">
            <el-button @click="resetView">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-tooltip content="全屏" placement="bottom">
            <el-button @click="toggleFullscreen">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>

      <!-- VR场景容器 -->
      <div class="editor-viewport" ref="vrContainer" v-loading="loading">
        <div v-if="error" class="error-state">
          <el-icon class="error-icon"><Warning /></el-icon>
          <p>加载失败</p>
          <p class="error-message">{{ error }}</p>
          <el-button type="primary" @click="retry">重试</el-button>
        </div>

        <!-- 编辑提示 -->
        <div class="edit-hint" v-if="currentTool === 'hotspot' && !selectedHotspot">
          <p>点击场景中的任意位置添加热点</p>
        </div>
      </div>

      <!-- 属性面板 -->
      <div class="editor-panel">
        <!-- 热点列表 -->
        <el-card class="hotspot-list-card">
          <template #header>
            <div class="card-header">
              <span>热点列表</span>
              <el-button size="small" @click="addHotspot">
                <el-icon><Plus /></el-icon>
                添加
              </el-button>
            </div>
          </template>
          
          <div class="hotspot-list">
            <div 
              v-for="hotspot in hotspots" 
              :key="hotspot.id"
              class="hotspot-item"
              :class="{ active: selectedHotspot?.id === hotspot.id }"
              @click="selectHotspot(hotspot)"
            >
              <div class="hotspot-icon" :style="{ backgroundColor: getHotspotColor(hotspot.type) }">
                <el-icon><Location /></el-icon>
              </div>
              <div class="hotspot-info">
                <div class="hotspot-name">{{ hotspot.content.title || '未命名热点' }}</div>
                <div class="hotspot-type">{{ getHotspotTypeName(hotspot.type) }}</div>
              </div>
              <div class="hotspot-actions">
                <el-button size="small" text @click.stop="editHotspot(hotspot)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button size="small" text @click.stop="deleteHotspot(hotspot.id)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            
            <el-empty v-if="hotspots.length === 0" description="还没有热点" :image-size="60">
              <el-button type="primary" size="small" @click="addHotspot">
                添加第一个热点
              </el-button>
            </el-empty>
          </div>
        </el-card>

        <!-- 热点编辑面板 -->
        <el-card class="hotspot-edit-card" v-if="selectedHotspot">
          <template #header>
            <div class="card-header">
              <span>热点编辑</span>
              <el-button size="small" text @click="selectedHotspot = null">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </template>
          
          <el-form :model="hotspotForm" label-width="80px" size="small">
            <el-form-item label="名称">
              <el-input v-model="hotspotForm.title" placeholder="热点名称" />
            </el-form-item>
            
            <el-form-item label="类型">
              <el-select v-model="hotspotForm.type" @change="handleTypeChange">
                <el-option label="信息热点" value="info" />
                <el-option label="媒体热点" value="media" />
                <el-option label="链接热点" value="link" />
                <el-option label="音频热点" value="audio" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="描述">
              <el-input 
                v-model="hotspotForm.description" 
                type="textarea" 
                :rows="3"
                placeholder="热点描述"
              />
            </el-form-item>
            
            <!-- 媒体热点配置 -->
            <template v-if="hotspotForm.type === 'media'">
              <el-form-item label="媒体类型">
                <el-radio-group v-model="hotspotForm.mediaType">
                  <el-radio label="image">图片</el-radio>
                  <el-radio label="video">视频</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="媒体URL">
                <el-input v-model="hotspotForm.mediaUrl" placeholder="媒体文件URL" />
              </el-form-item>
            </template>
            
            <!-- 链接热点配置 -->
            <template v-if="hotspotForm.type === 'link'">
              <el-form-item label="链接URL">
                <el-input v-model="hotspotForm.url" placeholder="链接地址" />
              </el-form-item>
              
              <el-form-item label="打开方式">
                <el-radio-group v-model="hotspotForm.target">
                  <el-radio label="_blank">新窗口</el-radio>
                  <el-radio label="_self">当前窗口</el-radio>
                </el-radio-group>
              </el-form-item>
            </template>
            
            <!-- 音频热点配置 -->
            <template v-if="hotspotForm.type === 'audio'">
              <el-form-item label="音频URL">
                <el-input v-model="hotspotForm.audioUrl" placeholder="音频文件URL" />
              </el-form-item>
              
              <el-form-item label="播放设置">
                <el-checkbox v-model="hotspotForm.loop">循环播放</el-checkbox>
              </el-form-item>
              
              <el-form-item label="音量">
                <el-slider v-model="hotspotForm.volume" :min="0" :max="1" :step="0.1" />
              </el-form-item>
            </template>
            
            <!-- 位置信息 -->
            <el-form-item label="位置">
              <div class="position-inputs">
                <el-input-number 
                  v-model="hotspotForm.position.x" 
                  :precision="2"
                  size="small"
                  placeholder="X"
                />
                <el-input-number 
                  v-model="hotspotForm.position.y" 
                  :precision="2"
                  size="small"
                  placeholder="Y"
                />
                <el-input-number 
                  v-model="hotspotForm.position.z" 
                  :precision="2"
                  size="small"
                  placeholder="Z"
                />
              </div>
            </el-form-item>
            
            <!-- 样式设置 -->
            <el-form-item label="颜色">
              <el-color-picker v-model="hotspotForm.color" />
            </el-form-item>
            
            <el-form-item label="大小">
              <el-slider v-model="hotspotForm.size" :min="1" :max="5" :step="0.1" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="updateHotspot">
                更新热点
              </el-button>
              <el-button @click="cancelEdit">
                取消
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, DocumentAdd, View, Pointer, Location, Rank, 
  Refresh, FullScreen, Warning, Plus, Edit, Delete, Close 
} from '@element-plus/icons-vue'
import VRSceneManager from '@/utils/vrSceneManager'
import VRHotspot from '@/utils/vrHotspot'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()

// 响应式数据
const vrContainer = ref()
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const currentTool = ref('select')
const selectedHotspot = ref(null)

// VR场景管理器
let vrSceneManager = null

// 项目数据
const project = reactive({
  id: null,
  title: '',
  panoramaUrl: '',
  hotspots: []
})

// 热点数据
const hotspots = ref([])

// 热点表单
const hotspotForm = reactive({
  title: '',
  type: 'info',
  description: '',
  mediaType: 'image',
  mediaUrl: '',
  url: '',
  target: '_blank',
  audioUrl: '',
  loop: false,
  volume: 1.0,
  position: { x: 0, y: 0, z: 0 },
  color: '#4fc3f7',
  size: 2
})

// 方法
const loadProject = async () => {
  const projectId = route.params.id
  if (!projectId) return

  try {
    loading.value = true
    error.value = ''

    const response = await api.get(`/projects/${projectId}`)
    const projectData = response.data.data
    
    Object.assign(project, projectData)
    hotspots.value = projectData.hotspots || []

    await nextTick()
    await initVRScene()
    
    if (project.panoramaUrl) {
      await loadPanorama(project.panoramaUrl)
    }
    
    loadHotspots()

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
    vrSceneManager = new VRSceneManager(vrContainer.value, {
      enableControls: true,
      enableAutoRotate: false,
      enableZoom: true,
      enableDamping: true
    })

    // 监听场景点击事件
    vrSceneManager.on('sceneClick', handleSceneClick)
    vrSceneManager.on('hotspotClick', handleHotspotClick)
    
  } catch (error) {
    console.error('初始化VR场景失败:', error)
    throw error
  }
}

const loadPanorama = async (imageUrl) => {
  if (!vrSceneManager) return
  await vrSceneManager.loadPanorama(imageUrl)
}

const loadHotspots = () => {
  if (!vrSceneManager) return
  
  hotspots.value.forEach(hotspotData => {
    const hotspot = vrSceneManager.addHotspot(hotspotData)
    // 添加拖拽功能
    enableHotspotDrag(hotspot)
  })
}

const enableHotspotDrag = (hotspot) => {
  if (vrSceneManager) {
    vrSceneManager.enableHotspotDrag(hotspot)
  }
}

const handleSceneClick = (event) => {
  if (currentTool.value === 'hotspot') {
    addHotspotAtPosition(event.mouse)
  }
}

const handleHotspotClick = (hotspot) => {
  if (currentTool.value === 'select') {
    selectHotspot(hotspot)
  }
}

const addHotspotAtPosition = (mousePosition) => {
  // 将屏幕坐标转换为3D世界坐标
  const worldPosition = screenToWorldPosition(mousePosition)
  
  const newHotspot = {
    id: Date.now().toString(),
    type: 'info',
    position: worldPosition,
    content: {
      title: '新热点',
      description: ''
    },
    style: {
      color: 0x4fc3f7,
      size: 2
    }
  }
  
  hotspots.value.push(newHotspot)
  
  if (vrSceneManager) {
    const hotspot = vrSceneManager.addHotspot(newHotspot)
    enableHotspotDrag(hotspot)
    selectHotspot(newHotspot)
  }
  
  ElMessage.success('热点添加成功')
}

const screenToWorldPosition = (mousePosition) => {
  // 简化的坐标转换，实际应该使用射线投射
  const distance = 100
  const x = mousePosition.x * distance
  const y = mousePosition.y * distance
  const z = Math.sqrt(distance * distance - x * x - y * y)
  
  return { x, y, z }
}

const setTool = (tool) => {
  currentTool.value = tool
  
  // 更新鼠标样式
  if (vrContainer.value) {
    const cursor = tool === 'hotspot' ? 'crosshair' : 'default'
    vrContainer.value.style.cursor = cursor
  }
}

const selectHotspot = (hotspot) => {
  selectedHotspot.value = hotspot
  
  // 填充表单数据
  Object.assign(hotspotForm, {
    title: hotspot.content.title || '',
    type: hotspot.type || 'info',
    description: hotspot.content.description || '',
    mediaType: hotspot.content.mediaType || 'image',
    mediaUrl: hotspot.content.mediaUrl || '',
    url: hotspot.content.url || '',
    target: hotspot.content.target || '_blank',
    audioUrl: hotspot.content.audioUrl || '',
    loop: hotspot.content.loop || false,
    volume: hotspot.content.volume || 1.0,
    position: { ...hotspot.position },
    color: hotspot.style?.color ? `#${hotspot.style.color.toString(16).padStart(6, '0')}` : '#4fc3f7',
    size: hotspot.style?.size || 2
  })
}

const addHotspot = () => {
  setTool('hotspot')
  ElMessage.info('请在场景中点击位置添加热点')
}

const editHotspot = (hotspot) => {
  selectHotspot(hotspot)
}

const deleteHotspot = async (hotspotId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个热点吗？', '确认删除', {
      type: 'warning'
    })
    
    // 从数组中移除
    const index = hotspots.value.findIndex(h => h.id === hotspotId)
    if (index !== -1) {
      hotspots.value.splice(index, 1)
    }
    
    // 从场景中移除
    if (vrSceneManager) {
      vrSceneManager.removeHotspot(hotspotId)
    }
    
    // 清除选择
    if (selectedHotspot.value?.id === hotspotId) {
      selectedHotspot.value = null
    }
    
    ElMessage.success('热点删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除热点失败')
    }
  }
}

const handleTypeChange = () => {
  // 重置类型相关的字段
  hotspotForm.mediaType = 'image'
  hotspotForm.mediaUrl = ''
  hotspotForm.url = ''
  hotspotForm.audioUrl = ''
}

const updateHotspot = () => {
  if (!selectedHotspot.value) return
  
  // 更新热点数据
  const hotspotIndex = hotspots.value.findIndex(h => h.id === selectedHotspot.value.id)
  if (hotspotIndex !== -1) {
    const updatedHotspot = {
      ...hotspots.value[hotspotIndex],
      type: hotspotForm.type,
      position: { ...hotspotForm.position },
      content: {
        title: hotspotForm.title,
        description: hotspotForm.description,
        mediaType: hotspotForm.mediaType,
        mediaUrl: hotspotForm.mediaUrl,
        url: hotspotForm.url,
        target: hotspotForm.target,
        audioUrl: hotspotForm.audioUrl,
        loop: hotspotForm.loop,
        volume: hotspotForm.volume
      },
      style: {
        color: parseInt(hotspotForm.color.replace('#', ''), 16),
        size: hotspotForm.size
      }
    }
    
    hotspots.value[hotspotIndex] = updatedHotspot
    
    // 更新场景中的热点
    if (vrSceneManager) {
      vrSceneManager.removeHotspot(selectedHotspot.value.id)
      const newHotspot = vrSceneManager.addHotspot(updatedHotspot)
      enableHotspotDrag(newHotspot)
    }
    
    selectedHotspot.value = updatedHotspot
    ElMessage.success('热点更新成功')
  }
}

const cancelEdit = () => {
  selectedHotspot.value = null
}

const saveProject = async () => {
  try {
    saving.value = true
    
    const projectData = {
      ...project,
      hotspots: hotspots.value
    }
    
    await api.put(`/projects/${project.id}`, projectData)
    ElMessage.success('项目保存成功')
  } catch (error) {
    console.error('保存项目失败:', error)
    ElMessage.error('保存项目失败')
  } finally {
    saving.value = false
  }
}

const previewProject = () => {
  const shareToken = project.shareToken || project.id
  window.open(`/vr/${shareToken}`, '_blank')
}

const resetView = () => {
  if (vrSceneManager) {
    vrSceneManager.resetView()
  }
}

const toggleFullscreen = () => {
  if (vrSceneManager) {
    vrSceneManager.toggleFullscreen()
  }
}

const retry = () => {
  loadProject()
}

const goBack = () => {
  router.push(`/projects/${project.id}`)
}

const getHotspotColor = (type) => {
  const colorMap = {
    info: '#4fc3f7',
    media: '#ff7043',
    link: '#66bb6a',
    audio: '#ffd54f'
  }
  return colorMap[type] || '#4fc3f7'
}

const getHotspotTypeName = (type) => {
  const nameMap = {
    info: '信息热点',
    media: '媒体热点',
    link: '链接热点',
    audio: '音频热点'
  }
  return nameMap[type] || '未知类型'
}

// 监听位置变化
watch(() => hotspotForm.position, (newPosition) => {
  if (selectedHotspot.value && vrSceneManager) {
    // 实时更新热点位置
    const hotspot = vrSceneManager.hotspots.find(h => h.id === selectedHotspot.value.id)
    if (hotspot) {
      hotspot.setPosition(newPosition.x, newPosition.y, newPosition.z)
    }
  }
}, { deep: true })

// 生命周期
onMounted(() => {
  loadProject()
})

onUnmounted(() => {
  if (vrSceneManager) {
    vrSceneManager.destroy()
    vrSceneManager = null
  }
})
</script>

<style lang="scss" scoped>
.vr-editor {
  min-height: 100vh;
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;

  .editor-header {
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

      .project-subtitle {
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

  .editor-main {
    flex: 1;
    display: flex;
    flex-direction: column;

    .editor-toolbar {
      background: var(--bg-secondary);
      border-bottom: 1px solid var(--border-primary);
      padding: 12px 24px;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .tool-group {
        display: flex;
        gap: 8px;

        .el-button {
          width: 40px;
          height: 40px;
          border-radius: 8px;
          border: 1px solid var(--border-secondary);
          background: var(--bg-tertiary);
          color: var(--text-secondary);

          &:hover {
            border-color: var(--primary-color);
            color: var(--primary-color);
          }

          &.active {
            border-color: var(--primary-color);
            background: rgba(212, 175, 55, 0.1);
            color: var(--primary-color);
          }
        }
      }
    }

    .editor-viewport {
      flex: 1;
      position: relative;
      background: var(--bg-tertiary);

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

      .edit-hint {
        position: absolute;
        top: 20px;
        left: 50%;
        transform: translateX(-50%);
        background: rgba(0, 0, 0, 0.8);
        color: var(--text-primary);
        padding: 12px 20px;
        border-radius: 8px;
        font-size: 14px;
        z-index: 100;
        backdrop-filter: blur(10px);
      }
    }

    .editor-panel {
      width: 320px;
      background: var(--bg-secondary);
      border-left: 1px solid var(--border-primary);
      display: flex;
      flex-direction: column;
      overflow-y: auto;

      .hotspot-list-card,
      .hotspot-edit-card {
        margin: 0;
        border: none;
        border-radius: 0;
        border-bottom: 1px solid var(--border-primary);

        :deep(.el-card__header) {
          background: var(--bg-tertiary);
          border-bottom: 1px solid var(--border-primary);
          padding: 16px 20px;

          .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            color: var(--text-primary);
            font-weight: 500;
          }
        }

        :deep(.el-card__body) {
          padding: 0;
        }
      }

      .hotspot-list {
        max-height: 300px;
        overflow-y: auto;

        .hotspot-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px 20px;
          cursor: pointer;
          transition: background-color 0.3s ease;

          &:hover {
            background: var(--bg-tertiary);
          }

          &.active {
            background: rgba(212, 175, 55, 0.1);
            border-left: 3px solid var(--primary-color);
          }

          .hotspot-icon {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 14px;
          }

          .hotspot-info {
            flex: 1;

            .hotspot-name {
              font-weight: 500;
              color: var(--text-primary);
              margin-bottom: 4px;
            }

            .hotspot-type {
              font-size: 12px;
              color: var(--text-secondary);
            }
          }

          .hotspot-actions {
            display: flex;
            gap: 4px;
            opacity: 0;
            transition: opacity 0.3s ease;

            .el-button {
              color: var(--text-secondary);

              &:hover {
                color: var(--text-primary);
              }
            }
          }

          &:hover .hotspot-actions {
            opacity: 1;
          }
        }
      }

      .hotspot-edit-card {
        flex: 1;

        :deep(.el-card__body) {
          padding: 20px;
        }

        .position-inputs {
          display: flex;
          gap: 8px;

          .el-input-number {
            flex: 1;
          }
        }

        :deep(.el-form-item) {
          margin-bottom: 16px;

          .el-form-item__label {
            color: var(--text-primary);
            font-size: 12px;
          }
        }

        :deep(.el-input__wrapper) {
          background-color: var(--bg-tertiary);
          border: 1px solid var(--border-secondary);
          box-shadow: none;

          &:hover {
            border-color: var(--primary-color);
          }

          &.is-focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 2px rgba(212, 175, 55, 0.2);
          }
        }

        :deep(.el-textarea__inner) {
          background-color: var(--bg-tertiary);
          border: 1px solid var(--border-secondary);
          color: var(--text-primary);

          &:hover {
            border-color: var(--primary-color);
          }

          &:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 2px rgba(212, 175, 55, 0.2);
          }
        }

        :deep(.el-select .el-input__wrapper) {
          background-color: var(--bg-tertiary);
        }

        :deep(.el-radio__label) {
          color: var(--text-primary);
        }

        :deep(.el-radio__input.is-checked .el-radio__inner) {
          background-color: var(--primary-color);
          border-color: var(--primary-color);
        }

        :deep(.el-checkbox__label) {
          color: var(--text-primary);
        }

        :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
          background-color: var(--primary-color);
          border-color: var(--primary-color);
        }

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

// 响应式样式
@media (max-width: 1200px) {
  .vr-editor {
    .editor-main {
      flex-direction: column;

      .editor-panel {
        width: 100%;
        height: 400px;
        border-left: none;
        border-top: 1px solid var(--border-primary);

        .hotspot-list-card {
          flex: 1;
        }

        .hotspot-edit-card {
          flex: 2;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .vr-editor {
    .editor-header {
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

    .editor-main {
      .editor-toolbar {
        padding: 8px 16px;
        flex-direction: column;
        gap: 12px;

        .tool-group {
          justify-content: center;
        }
      }

      .editor-panel {
        height: 50vh;

        .hotspot-edit-card {
          :deep(.el-card__body) {
            padding: 16px;
          }

          .position-inputs {
            flex-direction: column;
          }
        }
      }
    }
  }
}
</style>