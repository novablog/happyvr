<template>
  <div class="hotspot-config-panel">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" size="small">
      <!-- 基本信息 -->
      <el-form-item label="名称" prop="title">
        <el-input v-model="form.title" placeholder="热点名称" />
      </el-form-item>
      
      <el-form-item label="类型" prop="type">
        <el-select v-model="form.type" @change="handleTypeChange">
          <el-option 
            v-for="type in hotspotTypes" 
            :key="type.value"
            :label="type.label" 
            :value="type.value"
          >
            <div class="type-option">
              <el-icon :style="{ color: type.color }">
                <component :is="type.icon" />
              </el-icon>
              <span>{{ type.label }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="描述">
        <el-input 
          v-model="form.description" 
          type="textarea" 
          :rows="3"
          placeholder="热点描述"
        />
      </el-form-item>

      <!-- 类型特定配置 -->
      <template v-if="form.type === 'media'">
        <el-divider content-position="left">媒体设置</el-divider>
        
        <el-form-item label="媒体类型">
          <el-radio-group v-model="form.mediaType">
            <el-radio label="image">图片</el-radio>
            <el-radio label="video">视频</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="媒体URL" prop="mediaUrl">
          <el-input v-model="form.mediaUrl" placeholder="媒体文件URL">
            <template #append>
              <el-button @click="selectMediaFile">选择文件</el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item v-if="form.mediaType === 'image'" label="图片预览">
          <div class="media-preview">
            <img v-if="form.mediaUrl" :src="form.mediaUrl" alt="预览" />
            <div v-else class="no-preview">暂无预览</div>
          </div>
        </el-form-item>
      </template>
      
      <template v-if="form.type === 'link'">
        <el-divider content-position="left">链接设置</el-divider>
        
        <el-form-item label="链接URL" prop="url">
          <el-input v-model="form.url" placeholder="链接地址" />
        </el-form-item>
        
        <el-form-item label="打开方式">
          <el-radio-group v-model="form.target">
            <el-radio label="_blank">新窗口</el-radio>
            <el-radio label="_self">当前窗口</el-radio>
          </el-radio-group>
        </el-form-item>
      </template>
      
      <template v-if="form.type === 'audio'">
        <el-divider content-position="left">音频设置</el-divider>
        
        <el-form-item label="音频URL" prop="audioUrl">
          <el-input v-model="form.audioUrl" placeholder="音频文件URL">
            <template #append>
              <el-button @click="selectAudioFile">选择文件</el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="播放设置">
          <el-checkbox v-model="form.loop">循环播放</el-checkbox>
          <el-checkbox v-model="form.autoPlay">自动播放</el-checkbox>
        </el-form-item>
        
        <el-form-item label="音量">
          <el-slider 
            v-model="form.volume" 
            :min="0" 
            :max="1" 
            :step="0.1"
            show-input
            :input-size="'small'"
          />
        </el-form-item>
      </template>

      <!-- 位置设置 -->
      <el-divider content-position="left">位置设置</el-divider>
      
      <el-form-item label="坐标">
        <div class="position-inputs">
          <el-input-number 
            v-model="form.position.x" 
            :precision="2"
            size="small"
            placeholder="X"
            @change="handlePositionChange"
          />
          <el-input-number 
            v-model="form.position.y" 
            :precision="2"
            size="small"
            placeholder="Y"
            @change="handlePositionChange"
          />
          <el-input-number 
            v-model="form.position.z" 
            :precision="2"
            size="small"
            placeholder="Z"
            @change="handlePositionChange"
          />
        </div>
      </el-form-item>

      <!-- 样式设置 -->
      <el-divider content-position="left">样式设置</el-divider>
      
      <el-form-item label="颜色">
        <el-color-picker 
          v-model="form.color" 
          @change="handleStyleChange"
          show-alpha
        />
      </el-form-item>
      
      <el-form-item label="大小">
        <el-slider 
          v-model="form.size" 
          :min="1" 
          :max="5" 
          :step="0.1"
          @change="handleStyleChange"
          show-input
          :input-size="'small'"
        />
      </el-form-item>
      
      <el-form-item label="透明度">
        <el-slider 
          v-model="form.opacity" 
          :min="0" 
          :max="1" 
          :step="0.1"
          @change="handleStyleChange"
          show-input
          :input-size="'small'"
        />
      </el-form-item>

      <!-- 动画设置 -->
      <el-divider content-position="left">动画设置</el-divider>
      
      <el-form-item label="动画类型">
        <el-select v-model="form.animation" @change="handleAnimationChange">
          <el-option label="无动画" value="none" />
          <el-option label="浮动" value="float" />
          <el-option label="旋转" value="rotate" />
          <el-option label="脉冲" value="pulse" />
          <el-option label="弹跳" value="bounce" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="动画速度" v-if="form.animation !== 'none'">
        <el-slider 
          v-model="form.animationSpeed" 
          :min="0.1" 
          :max="2" 
          :step="0.1"
          @change="handleAnimationChange"
          show-input
          :input-size="'small'"
        />
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <div class="action-buttons">
          <el-button type="primary" @click="saveHotspot" :loading="saving">
            保存热点
          </el-button>
          <el-button @click="resetForm">
            重置
          </el-button>
          <el-button type="danger" @click="deleteHotspot" v-if="hotspot">
            删除热点
          </el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  InfoFilled, Picture, Link, Headphones 
} from '@element-plus/icons-vue'

const props = defineProps({
  hotspot: {
    type: Object,
    default: null
  },
  saving: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['save', 'delete', 'position-change', 'style-change', 'animation-change'])

const formRef = ref()

// 热点类型配置
const hotspotTypes = [
  { value: 'info', label: '信息热点', icon: InfoFilled, color: '#4fc3f7' },
  { value: 'media', label: '媒体热点', icon: Picture, color: '#ff7043' },
  { value: 'link', label: '链接热点', icon: Link, color: '#66bb6a' },
  { value: 'audio', label: '音频热点', icon: Headphones, color: '#ffd54f' }
]

// 表单数据
const form = reactive({
  title: '',
  type: 'info',
  description: '',
  // 媒体相关
  mediaType: 'image',
  mediaUrl: '',
  // 链接相关
  url: '',
  target: '_blank',
  // 音频相关
  audioUrl: '',
  loop: false,
  autoPlay: false,
  volume: 1.0,
  // 位置
  position: { x: 0, y: 0, z: 0 },
  // 样式
  color: '#4fc3f7',
  size: 2,
  opacity: 0.8,
  // 动画
  animation: 'float',
  animationSpeed: 1.0
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入热点名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择热点类型', trigger: 'change' }
  ],
  mediaUrl: [
    { required: true, message: '请输入媒体URL', trigger: 'blur' }
  ],
  url: [
    { required: true, message: '请输入链接地址', trigger: 'blur' },
    { type: 'url', message: '请输入正确的URL格式', trigger: 'blur' }
  ],
  audioUrl: [
    { required: true, message: '请输入音频URL', trigger: 'blur' }
  ]
}

// 监听热点变化
watch(() => props.hotspot, (newHotspot) => {
  if (newHotspot) {
    loadHotspotData(newHotspot)
  } else {
    resetForm()
  }
}, { immediate: true })

// 方法
const loadHotspotData = (hotspot) => {
  Object.assign(form, {
    title: hotspot.content?.title || '',
    type: hotspot.type || 'info',
    description: hotspot.content?.description || '',
    mediaType: hotspot.content?.mediaType || 'image',
    mediaUrl: hotspot.content?.mediaUrl || '',
    url: hotspot.content?.url || '',
    target: hotspot.content?.target || '_blank',
    audioUrl: hotspot.content?.audioUrl || '',
    loop: hotspot.content?.loop || false,
    autoPlay: hotspot.content?.autoPlay || false,
    volume: hotspot.content?.volume || 1.0,
    position: { ...hotspot.position },
    color: hotspot.style?.color ? `#${hotspot.style.color.toString(16).padStart(6, '0')}` : '#4fc3f7',
    size: hotspot.style?.size || 2,
    opacity: hotspot.style?.opacity || 0.8,
    animation: hotspot.animation?.type || 'float',
    animationSpeed: hotspot.animation?.speed || 1.0
  })
}

const handleTypeChange = () => {
  // 重置类型相关的字段
  form.mediaType = 'image'
  form.mediaUrl = ''
  form.url = ''
  form.audioUrl = ''
  
  // 更新颜色
  const typeConfig = hotspotTypes.find(t => t.value === form.type)
  if (typeConfig) {
    form.color = typeConfig.color
  }
}

const handlePositionChange = () => {
  emit('position-change', { ...form.position })
}

const handleStyleChange = () => {
  emit('style-change', {
    color: parseInt(form.color.replace('#', ''), 16),
    size: form.size,
    opacity: form.opacity
  })
}

const handleAnimationChange = () => {
  emit('animation-change', {
    type: form.animation,
    speed: form.animationSpeed
  })
}

const selectMediaFile = () => {
  // TODO: 实现文件选择器
  ElMessage.info('文件选择功能即将上线')
}

const selectAudioFile = () => {
  // TODO: 实现音频文件选择器
  ElMessage.info('音频文件选择功能即将上线')
}

const saveHotspot = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    const hotspotData = {
      id: props.hotspot?.id || Date.now().toString(),
      type: form.type,
      position: { ...form.position },
      content: {
        title: form.title,
        description: form.description,
        mediaType: form.mediaType,
        mediaUrl: form.mediaUrl,
        url: form.url,
        target: form.target,
        audioUrl: form.audioUrl,
        loop: form.loop,
        autoPlay: form.autoPlay,
        volume: form.volume
      },
      style: {
        color: parseInt(form.color.replace('#', ''), 16),
        size: form.size,
        opacity: form.opacity
      },
      animation: {
        type: form.animation,
        speed: form.animationSpeed
      }
    }
    
    emit('save', hotspotData)
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const resetForm = () => {
  Object.assign(form, {
    title: '',
    type: 'info',
    description: '',
    mediaType: 'image',
    mediaUrl: '',
    url: '',
    target: '_blank',
    audioUrl: '',
    loop: false,
    autoPlay: false,
    volume: 1.0,
    position: { x: 0, y: 0, z: 0 },
    color: '#4fc3f7',
    size: 2,
    opacity: 0.8,
    animation: 'float',
    animationSpeed: 1.0
  })
  
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const deleteHotspot = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个热点吗？', '确认删除', {
      type: 'warning'
    })
    
    emit('delete', props.hotspot.id)
  } catch (error) {
    // 用户取消删除
  }
}
</script>

<style lang="scss" scoped>
.hotspot-config-panel {
  .type-option {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .position-inputs {
    display: flex;
    gap: 8px;

    .el-input-number {
      flex: 1;
    }
  }

  .media-preview {
    width: 100%;
    height: 120px;
    border: 1px solid var(--border-secondary);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    img {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
    }

    .no-preview {
      color: var(--text-tertiary);
      font-size: 14px;
    }
  }

  .action-buttons {
    display: flex;
    gap: 12px;
    width: 100%;

    .el-button {
      flex: 1;
    }
  }

  :deep(.el-divider__text) {
    color: var(--text-primary);
    font-weight: 500;
  }

  :deep(.el-form-item__label) {
    color: var(--text-primary);
    font-size: 12px;
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
</style>