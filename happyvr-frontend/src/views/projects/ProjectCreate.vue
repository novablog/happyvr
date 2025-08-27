<template>
  <div class="project-create">
    <div class="page-header">
      <h2>创建VR项目</h2>
      <p>上传图片并生成沉浸式VR体验</p>
    </div>

    <div class="create-steps">
      <el-steps :active="currentStep" align-center>
        <el-step title="项目信息" description="填写基本信息" />
        <el-step title="上传图片" description="选择图片文件" />
        <el-step title="生成VR" description="处理并生成VR" />
        <el-step title="完成" description="预览和保存" />
      </el-steps>
    </div>

    <div class="step-content">
      <!-- 步骤1: 项目信息 -->
      <div v-if="currentStep === 0" class="step-project-info">
        <el-card>
          <template #header>
            <span>项目基本信息</span>
          </template>
          <el-form
            ref="projectFormRef"
            :model="projectForm"
            :rules="projectRules"
            label-width="120px"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="项目名称" prop="title">
                  <el-input
                    v-model="projectForm.title"
                    placeholder="请输入项目名称"
                    maxlength="100"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item label="项目描述" prop="description">
                  <el-input
                    v-model="projectForm.description"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入项目描述"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item label="项目标签">
                  <el-select
                    v-model="projectForm.tags"
                    multiple
                    filterable
                    allow-create
                    placeholder="选择或创建标签"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="tag in availableTags"
                      :key="tag.id"
                      :label="tag.name"
                      :value="tag.name"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="项目分类" prop="category">
                  <el-select v-model="projectForm.category" placeholder="选择项目分类">
                    <el-option
                      v-for="category in categories"
                      :key="category.id"
                      :label="category.name"
                      :value="category.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="隐私设置" prop="privacy">
                  <el-radio-group v-model="projectForm.privacy">
                    <el-radio label="public">公开</el-radio>
                    <el-radio label="private">私有</el-radio>
                    <el-radio label="unlisted">不公开列出</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="封面图片">
                  <el-upload
                    class="cover-uploader"
                    :show-file-list="false"
                    :on-success="handleCoverSuccess"
                    :before-upload="beforeCoverUpload"
                    action="#"
                    :auto-upload="false"
                  >
                    <img v-if="projectForm.coverImage" :src="projectForm.coverImage" class="cover-image" />
                    <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </div>

      <!-- 步骤2: 上传图片 -->
      <div v-if="currentStep === 1" class="step-upload">
        <FileUpload
          ref="fileUploadRef"
          :multiple="true"
          :max-count="20"
          :auto-upload="false"
          @files-change="handleFilesChange"
          @upload-success="handleUploadSuccess"
          @upload-error="handleUploadError"
        />
        
        <div v-if="uploadedFiles.length > 0" class="upload-summary">
          <el-alert
            :title="`已上传 ${uploadedFiles.length} 个文件`"
            type="success"
            show-icon
            :closable="false"
          />
        </div>
      </div>

      <!-- 步骤3: 生成VR -->
      <div v-if="currentStep === 2" class="step-generate">
        <VRGenerator
          ref="vrGeneratorRef"
          :images="uploadedFiles"
          :project-id="createdProjectId"
          @generation-start="handleGenerationStart"
          @generation-progress="handleGenerationProgress"
          @generation-complete="handleGenerationComplete"
          @generation-error="handleGenerationError"
        />
      </div>

      <!-- 步骤4: 完成 -->
      <div v-if="currentStep === 3" class="step-complete">
        <el-result
          icon="success"
          title="VR项目创建成功！"
          sub-title="您的VR项目已经创建完成，可以进行预览和分享了"
        >
          <template #extra>
            <div class="complete-actions">
              <el-button type="primary" size="large" @click="previewProject">
                <el-icon><View /></el-icon>
                预览项目
              </el-button>
              <el-button size="large" @click="editProject">
                <el-icon><Edit /></el-icon>
                编辑项目
              </el-button>
              <el-button size="large" @click="shareProject">
                <el-icon><Share /></el-icon>
                分享项目
              </el-button>
              <el-button size="large" @click="createAnother">
                <el-icon><Plus /></el-icon>
                创建新项目
              </el-button>
            </div>
          </template>
        </el-result>

        <!-- 项目信息卡片 -->
        <div v-if="createdProject" class="project-summary">
          <el-card>
            <template #header>
              <span>项目信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="项目名称">
                {{ createdProject.title }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDate(createdProject.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="图片数量">
                {{ uploadedFiles.length }}
              </el-descriptions-item>
              <el-descriptions-item label="项目状态">
                <el-tag type="success">已完成</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="分享链接" :span="2">
                <el-input
                  :value="createdProject.shareUrl"
                  readonly
                  class="share-input"
                >
                  <template #append>
                    <el-button @click="copyShareUrl">复制</el-button>
                  </template>
                </el-input>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 步骤导航 -->
    <div class="step-navigation">
      <el-button
        v-if="currentStep > 0"
        @click="prevStep"
        :disabled="isProcessing"
      >
        上一步
      </el-button>
      <el-button
        v-if="currentStep < 3"
        type="primary"
        @click="nextStep"
        :loading="isProcessing"
        :disabled="!canProceed"
      >
        {{ getNextButtonText() }}
      </el-button>
    </div>

    <!-- 进度提示 -->
    <div v-if="isProcessing" class="processing-overlay">
      <div class="processing-content">
        <el-icon class="loading-icon">
          <Loading />
        </el-icon>
        <p>{{ processingMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  View,
  Edit,
  Share,
  Loading
} from '@element-plus/icons-vue'
import FileUpload from '@/components/upload/FileUpload.vue'
import VRGenerator from '@/components/vr/VRGenerator.vue'
import { createProject, updateProject } from '@/api/projects'
import { getTags, getCategories } from '@/api/common'
import { APIUtils } from '@/utils/api'

const router = useRouter()

// 响应式数据
const currentStep = ref(0)
const isProcessing = ref(false)
const processingMessage = ref('')

// 表单引用
const projectFormRef = ref(null)
const fileUploadRef = ref(null)
const vrGeneratorRef = ref(null)

// 项目表单
const projectForm = ref({
  title: '',
  description: '',
  category: '',
  tags: [],
  privacy: 'public',
  coverImage: ''
})

// 表单验证规则
const projectRules = {
  title: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '项目名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述不能超过 500 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择项目分类', trigger: 'change' }
  ]
}

// 数据
const availableTags = ref([])
const categories = ref([])
const uploadedFiles = ref([])
const createdProjectId = ref(null)
const createdProject = ref(null)

// 计算属性
const canProceed = computed(() => {
  switch (currentStep.value) {
    case 0:
      return projectForm.value.title && projectForm.value.category
    case 1:
      return uploadedFiles.value.length > 0
    case 2:
      return vrGeneratorRef.value?.getResult()
    default:
      return true
  }
})

// 方法
const nextStep = async () => {
  try {
    isProcessing.value = true
    
    switch (currentStep.value) {
      case 0:
        await validateProjectInfo()
        await createProjectRecord()
        break
      case 1:
        await startUpload()
        break
      case 2:
        await startVRGeneration()
        break
    }
    
    currentStep.value++
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    isProcessing.value = false
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const validateProjectInfo = async () => {
  processingMessage.value = '验证项目信息...'
  
  if (!projectFormRef.value) return
  
  return new Promise((resolve, reject) => {
    projectFormRef.value.validate((valid) => {
      if (valid) {
        resolve()
      } else {
        reject(new Error('请完善项目信息'))
      }
    })
  })
}

const createProjectRecord = async () => {
  processingMessage.value = '创建项目记录...'
  
  const projectData = {
    ...projectForm.value,
    status: 'draft'
  }
  
  const response = await createProject(projectData)
  
  console.log('项目创建响应:', response)
  console.log('项目ID:', response.data?.id)
  
  createdProjectId.value = response.data.id
  createdProject.value = response.data
  
  ElMessage.success('项目创建成功')
}

const startUpload = async () => {
  processingMessage.value = '上传图片文件...'
  
  if (!fileUploadRef.value) {
    throw new Error('文件上传组件未初始化')
  }
  
  const files = fileUploadRef.value.getFiles()
  if (files.length === 0) {
    throw new Error('请先选择要上传的图片')
  }
  
  // 开始上传
  await fileUploadRef.value.startUpload()
}

const startVRGeneration = async () => {
  processingMessage.value = '开始生成VR...'
  
  if (!vrGeneratorRef.value) {
    throw new Error('VR生成器未初始化')
  }
  
  // 开始VR生成
  await vrGeneratorRef.value.startGeneration()
}

// 文件上传处理
const handleFilesChange = (files) => {
  console.log('Files changed:', files.length)
}

const handleUploadSuccess = (result) => {
  if (result.files) {
    // 批量上传成功
    uploadedFiles.value = result.files.filter(f => f.status === 'success')
  } else if (result.file) {
    // 单个文件上传成功
    const existingIndex = uploadedFiles.value.findIndex(f => f.uid === result.file.uid)
    if (existingIndex > -1) {
      uploadedFiles.value[existingIndex] = result.file
    } else {
      uploadedFiles.value.push(result.file)
    }
  }
  
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error) => {
  ElMessage.error('文件上传失败: ' + error.error?.message)
}

// VR生成处理
const handleGenerationStart = (task) => {
  console.log('VR generation started:', task)
}

const handleGenerationProgress = (progress) => {
  console.log('VR generation progress:', progress)
}

const handleGenerationComplete = (result) => {
  console.log('VR generation completed:', result)
  
  // 更新项目状态
  updateProjectStatus('completed')
}

const handleGenerationError = (error) => {
  ElMessage.error('VR生成失败: ' + error.message)
}

const updateProjectStatus = async (status) => {
  if (createdProjectId.value) {
    try {
      await updateProject(createdProjectId.value, { status })
    } catch (error) {
      console.error('更新项目状态失败:', error)
    }
  }
}

// 封面图片处理
const handleCoverSuccess = (response, file) => {
  projectForm.value.coverImage = URL.createObjectURL(file.raw)
}

const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 完成步骤操作
const previewProject = () => {
  if (createdProject.value) {
    router.push(`/projects/${createdProject.value.id}`)
  }
}

const editProject = () => {
  if (createdProject.value) {
    router.push(`/projects/${createdProject.value.id}/edit`)
  }
}

const shareProject = async () => {
  if (createdProject.value?.shareUrl) {
    try {
      await navigator.clipboard.writeText(createdProject.value.shareUrl)
      ElMessage.success('分享链接已复制到剪贴板')
    } catch (error) {
      ElMessage.error('复制链接失败')
    }
  }
}

const createAnother = () => {
  router.push('/projects/create')
}

const copyShareUrl = async () => {
  if (createdProject.value?.shareUrl) {
    try {
      await navigator.clipboard.writeText(createdProject.value.shareUrl)
      ElMessage.success('链接已复制')
    } catch (error) {
      ElMessage.error('复制失败')
    }
  }
}

// 工具方法
const getNextButtonText = () => {
  switch (currentStep.value) {
    case 0:
      return '创建项目'
    case 1:
      return '开始上传'
    case 2:
      return '生成VR'
    default:
      return '下一步'
  }
}

const formatDate = (dateString) => {
  return APIUtils.formatDate(dateString)
}

// 初始化数据
const initData = async () => {
  try {
    // 获取标签和分类
    const [tagsResponse, categoriesResponse] = await Promise.all([
      getTags(),
      getCategories()
    ])
    
    availableTags.value = tagsResponse.data || []
    categories.value = categoriesResponse.data || []
  } catch (error) {
    console.error('初始化数据失败:', error)
  }
}

// 生命周期
onMounted(() => {
  initData()
})
</script>

<style lang="scss" scoped>
.project-create {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .page-header {
    text-align: center;
    margin-bottom: 40px;

    h2 {
      color: var(--primary-color);
      margin-bottom: 10px;
    }

    p {
      color: var(--text-secondary);
      margin: 0;
    }
  }

  .create-steps {
    margin-bottom: 40px;
  }

  .step-content {
    min-height: 400px;
    margin-bottom: 40px;

    .step-project-info {
      .cover-uploader {
        :deep(.el-upload) {
          border: 1px dashed var(--border-primary);
          border-radius: 6px;
          cursor: pointer;
          position: relative;
          overflow: hidden;
          transition: var(--el-transition-duration-fast);

          &:hover {
            border-color: var(--primary-color);
          }
        }

        .cover-uploader-icon {
          font-size: 28px;
          color: var(--text-tertiary);
          width: 178px;
          height: 178px;
          text-align: center;
          line-height: 178px;
        }

        .cover-image {
          width: 178px;
          height: 178px;
          display: block;
          object-fit: cover;
        }
      }
    }

    .step-upload {
      .upload-summary {
        margin-top: 20px;
      }
    }

    .step-complete {
      .complete-actions {
        display: flex;
        gap: 15px;
        justify-content: center;
        flex-wrap: wrap;
      }

      .project-summary {
        margin-top: 40px;

        .share-input {
          :deep(.el-input-group__append) {
            background: var(--primary-color);
            border-color: var(--primary-color);

            .el-button {
              background: transparent;
              border: none;
              color: white;
            }
          }
        }
      }
    }
  }

  .step-navigation {
    display: flex;
    justify-content: center;
    gap: 20px;
    padding: 20px 0;
  }

  .processing-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;

    .processing-content {
      text-align: center;
      color: white;

      .loading-icon {
        font-size: 32px;
        animation: rotate 2s linear infinite;
      }

      p {
        margin-top: 15px;
        font-size: 16px;
      }
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
}

:deep(.el-steps) {
  .el-step__title {
    font-size: 16px;
  }

  .el-step__description {
    font-size: 14px;
  }
}
</style>