<template>
  <div class="admin-login">
    <div class="login-container">
      <div class="login-header">
        <div class="logo">
          <el-icon class="logo-icon">
            <Monitor />
          </el-icon>
          <h1>HappyVR 管理后台</h1>
        </div>
        <p class="subtitle">管理员登录</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="管理员账号"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="captcha" v-if="showCaptcha">
          <div class="captcha-container">
            <el-input
              v-model="loginForm.captcha"
              placeholder="验证码"
              :prefix-icon="Key"
              clearable
            />
            <div class="captcha-image" @click="refreshCaptcha">
              <img :src="captchaImage" alt="验证码" />
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="login-options">
            <el-checkbox v-model="loginForm.remember">记住登录</el-checkbox>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            :loading="loginLoading"
            @click="handleLogin"
          >
            {{ loginLoading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <div class="security-notice">
          <el-icon><Warning /></el-icon>
          <span>请使用管理员账号登录，确保账号安全</span>
        </div>
        
        <div class="help-links">
          <el-button text size="small" @click="showForgotPassword">
            忘记密码？
          </el-button>
          <el-divider direction="vertical" />
          <el-button text size="small" @click="contactSupport">
            联系技术支持
          </el-button>
        </div>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog
      v-model="forgotPasswordVisible"
      title="重置密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="resetForm" :rules="resetRules" ref="resetFormRef">
        <el-form-item label="管理员邮箱" prop="email">
          <el-input
            v-model="resetForm.email"
            placeholder="请输入管理员邮箱"
            :prefix-icon="Message"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="forgotPasswordVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetLoading" @click="handleResetPassword">
          发送重置邮件
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Monitor, User, Lock, Key, Warning, Message
} from '@element-plus/icons-vue'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()

// 表单引用
const loginFormRef = ref()
const resetFormRef = ref()

// 响应式数据
const loginLoading = ref(false)
const resetLoading = ref(false)
const showCaptcha = ref(false)
const captchaImage = ref('')
const forgotPasswordVisible = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  remember: false
})

// 重置密码表单
const resetForm = reactive({
  email: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度应为3-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码长度为4位', trigger: 'blur' }
  ]
}

const resetRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 方法
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loginLoading.value = true

    const loginData = {
      username: loginForm.username,
      password: loginForm.password,
      captcha: showCaptcha.value ? loginForm.captcha : undefined,
      remember: loginForm.remember
    }

    const response = await api.post('/admin/auth/login', loginData)
    const { token, adminInfo } = response.data.data

    // 保存登录信息
    localStorage.setItem('adminToken', token)
    localStorage.setItem('adminInfo', JSON.stringify(adminInfo))

    if (loginForm.remember) {
      localStorage.setItem('adminRemember', 'true')
    }

    ElMessage.success('登录成功')

    // 跳转到管理后台
    const redirect = route.query.redirect || '/admin/dashboard'
    router.push(redirect)

  } catch (error) {
    console.error('登录失败:', error)
    
    // 如果是验证码错误，显示验证码
    if (error.response?.status === 429 || error.response?.data?.needCaptcha) {
      showCaptcha.value = true
      refreshCaptcha()
    }
    
    ElMessage.error(error.response?.data?.message || '登录失败')
  } finally {
    loginLoading.value = false
  }
}

const refreshCaptcha = async () => {
  try {
    const response = await api.get('/admin/auth/captcha')
    captchaImage.value = response.data.data.image
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

const showForgotPassword = () => {
  forgotPasswordVisible.value = true
  resetForm.email = ''
}

const handleResetPassword = async () => {
  if (!resetFormRef.value) return

  try {
    await resetFormRef.value.validate()
    resetLoading.value = true

    await api.post('/admin/auth/reset-password', {
      email: resetForm.email
    })

    ElMessage.success('重置邮件已发送，请查收邮箱')
    forgotPasswordVisible.value = false

  } catch (error) {
    console.error('发送重置邮件失败:', error)
    ElMessage.error(error.response?.data?.message || '发送失败')
  } finally {
    resetLoading.value = false
  }
}

const contactSupport = () => {
  ElMessageBox.alert(
    '技术支持联系方式：\n邮箱：admin@happyvr.com\n电话：400-123-4567',
    '技术支持',
    {
      confirmButtonText: '确定'
    }
  )
}

// 检查是否有记住的登录信息
const checkRememberedLogin = () => {
  const remembered = localStorage.getItem('adminRemember')
  const savedToken = localStorage.getItem('adminToken')
  
  if (remembered && savedToken) {
    // 如果有记住的登录信息，直接跳转到管理后台
    router.push('/admin/dashboard')
  }
}

// 生命周期
onMounted(() => {
  checkRememberedLogin()
})
</script>

<style lang="scss" scoped>
.admin-login {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  .login-container {
    width: 100%;
    max-width: 400px;
    padding: 40px;
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(212, 175, 55, 0.2);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    position: relative;
    z-index: 10;

    .login-header {
      text-align: center;
      margin-bottom: 32px;

      .logo {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12px;
        margin-bottom: 8px;

        .logo-icon {
          font-size: 32px;
          color: var(--primary-color);
        }

        h1 {
          font-size: 24px;
          font-weight: 600;
          color: var(--text-primary);
          margin: 0;
        }
      }

      .subtitle {
        color: var(--text-secondary);
        font-size: 14px;
        margin: 0;
      }
    }

    .login-form {
      .captcha-container {
        display: flex;
        gap: 12px;

        .el-input {
          flex: 1;
        }

        .captcha-image {
          width: 100px;
          height: 40px;
          border: 1px solid var(--border-secondary);
          border-radius: 4px;
          cursor: pointer;
          overflow: hidden;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
      }

      .login-options {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;

        :deep(.el-checkbox__label) {
          color: var(--text-secondary);
          font-size: 14px;
        }
      }

      .login-button {
        width: 100%;
        height: 44px;
        font-size: 16px;
        font-weight: 500;
        background: var(--primary-color);
        border-color: var(--primary-color);

        &:hover {
          background: var(--primary-light);
          border-color: var(--primary-light);
        }
      }
    }

    .login-footer {
      margin-top: 24px;

      .security-notice {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px;
        background: rgba(230, 162, 60, 0.1);
        border: 1px solid rgba(230, 162, 60, 0.3);
        border-radius: 6px;
        margin-bottom: 16px;

        .el-icon {
          color: #e6a23c;
          font-size: 16px;
        }

        span {
          color: var(--text-secondary);
          font-size: 12px;
        }
      }

      .help-links {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;

        .el-button {
          color: var(--text-tertiary);
          font-size: 12px;

          &:hover {
            color: var(--primary-color);
          }
        }

        :deep(.el-divider--vertical) {
          height: 12px;
          border-color: var(--border-secondary);
        }
      }
    }
  }

  .background-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;

    .decoration-circle {
      position: absolute;
      border-radius: 50%;
      background: radial-gradient(circle, rgba(212, 175, 55, 0.1) 0%, transparent 70%);

      &.circle-1 {
        width: 300px;
        height: 300px;
        top: -150px;
        right: -150px;
        animation: float 6s ease-in-out infinite;
      }

      &.circle-2 {
        width: 200px;
        height: 200px;
        bottom: -100px;
        left: -100px;
        animation: float 8s ease-in-out infinite reverse;
      }

      &.circle-3 {
        width: 150px;
        height: 150px;
        top: 50%;
        left: -75px;
        animation: float 10s ease-in-out infinite;
      }
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .admin-login {
    padding: 20px;

    .login-container {
      padding: 24px;
      max-width: none;
    }
  }
}

// 深色主题适配
:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: none;

  &:hover {
    border-color: var(--primary-color);
  }

  &.is-focus {
    border-color: var(--primary-color);
    box-shadow: 0 0 0 2px rgba(212, 175, 55, 0.2);
  }

  .el-input__inner {
    color: var(--text-primary);

    &::placeholder {
      color: var(--text-tertiary);
    }
  }
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

:deep(.el-checkbox__label) {
  color: var(--text-secondary);
}
</style>