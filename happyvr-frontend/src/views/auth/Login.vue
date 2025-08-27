<template>
  <div class="login-container">
    <div class="login-card">
      <!-- Logo和标题 -->
      <div class="login-header">
        <div class="logo">
          <el-icon class="logo-icon"><VideoCamera /></el-icon>
          <span class="logo-text">HappyVR</span>
        </div>
        <h2 class="login-title">欢迎回来</h2>
        <p class="login-subtitle">登录您的VR创作平台</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名或邮箱"
            size="large"
            :prefix-icon="User"
            clearable
            @keyup.enter="handleLogin"
            @input="handleUsernameChange"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <div class="login-options">
            <el-checkbox v-model="loginForm.rememberMe" @change="handleRememberMeChange">
              记住我
            </el-checkbox>
            <el-button type="text" class="forgot-password" @click="showForgotPassword">
              忘记密码？
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 分割线 -->
      <div class="divider">
        <span>或</span>
      </div>

      <!-- 注册链接 -->
      <div class="register-link">
        <span>还没有账号？</span>
        <el-button type="text" @click="goToRegister">
          立即注册
        </el-button>
      </div>

      <!-- 第三方登录 -->
      <div class="social-login">
        <p class="social-title">其他登录方式</p>
        <div class="social-buttons">
          <el-button circle class="social-btn" @click="handleSocialLogin('github')">
            <el-icon><Platform /></el-icon>
          </el-button>
          <el-button circle class="social-btn" @click="handleSocialLogin('google')">
            <el-icon><ChromeFilled /></el-icon>
          </el-button>
          <el-button circle class="social-btn" @click="handleSocialLogin('wechat')">
            <el-icon><ChatDotRound /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog
      v-model="forgotPasswordVisible"
      title="重置密码"
      width="400px"
      :before-close="closeForgotPassword"
    >
      <el-form
        ref="forgotFormRef"
        :model="forgotForm"
        :rules="forgotRules"
        label-width="80px"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="forgotForm.email"
            placeholder="请输入注册邮箱"
            :prefix-icon="Message"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeForgotPassword">取消</el-button>
          <el-button
            type="primary"
            :loading="forgotLoading"
            @click="handleForgotPassword"
          >
            发送重置邮件
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, VideoCamera, Platform, ChromeFilled, ChatDotRound } from '@element-plus/icons-vue'
import authService from '@/services/authService'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const loginFormRef = ref()
const forgotFormRef = ref()
const loading = ref(false)
const forgotLoading = ref(false)
const forgotPasswordVisible = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 忘记密码表单
const forgotForm = reactive({
  email: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const forgotRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 方法
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const result = await userStore.login({
      usernameOrEmail: loginForm.username,
      password: loginForm.password,
      rememberMe: loginForm.rememberMe
    })
    
    if (result.success) {
      // 处理记住我功能
      if (loginForm.rememberMe) {
        localStorage.setItem('rememberedUsername', loginForm.username)
      } else {
        localStorage.removeItem('rememberedUsername')
      }
      
      ElMessage.success('登录成功！')
      
      // 登录成功，跳转到目标页面或工作台
      const redirect = route.query.redirect || '/dashboard'
      router.push(redirect)
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}

const showForgotPassword = () => {
  forgotPasswordVisible.value = true
}

const closeForgotPassword = () => {
  forgotPasswordVisible.value = false
  forgotForm.email = ''
  if (forgotFormRef.value) {
    forgotFormRef.value.clearValidate()
  }
}

const handleForgotPassword = async () => {
  if (!forgotFormRef.value) return
  
  try {
    await forgotFormRef.value.validate()
    forgotLoading.value = true
    
    await authService.sendResetPasswordEmail(forgotForm.email)
    
    ElMessage.success('重置密码邮件已发送，请查收邮箱')
    closeForgotPassword()
  } catch (error) {
    console.error('发送重置邮件失败:', error)
    const message = error.response?.data?.message || '发送重置邮件失败，请稍后重试'
    ElMessage.error(message)
  } finally {
    forgotLoading.value = false
  }
}

const handleSocialLogin = (provider) => {
  ElMessage.info(`${provider} 登录功能即将上线`)
  // TODO: 实现第三方登录
}

// 生命周期
onMounted(() => {
  // 如果已经登录，直接跳转
  if (userStore.isLoggedIn) {
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  }
  
  // 从URL参数获取用户名（注册成功后跳转）
  const usernameFromQuery = route.query.username
  if (usernameFromQuery) {
    loginForm.username = usernameFromQuery
  } else {
    // 从localStorage恢复记住我的状态
    const savedUsername = localStorage.getItem('rememberedUsername')
    if (savedUsername) {
      loginForm.username = savedUsername
      loginForm.rememberMe = true
    }
  }
})

// 监听记住我状态变化
const handleRememberMeChange = () => {
  if (loginForm.rememberMe && loginForm.username) {
    localStorage.setItem('rememberedUsername', loginForm.username)
  } else {
    localStorage.removeItem('rememberedUsername')
  }
}

// 监听用户名变化，自动保存记住我状态
const handleUsernameChange = () => {
  if (loginForm.rememberMe && loginForm.username) {
    localStorage.setItem('rememberedUsername', loginForm.username)
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg-primary), var(--bg-secondary));
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  padding: 20px;

  .login-card {
    background: var(--bg-secondary);
    border: 1px solid var(--border-primary);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
    padding: 40px;
    width: 100%;
    max-width: 400px;
    position: relative;
    z-index: 2;
    backdrop-filter: blur(10px);

    .login-header {
      text-align: center;
      margin-bottom: 32px;

      .logo {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        margin-bottom: 16px;

        .logo-icon {
          font-size: 32px;
          color: var(--primary-color);
        }

        .logo-text {
          font-size: 24px;
          font-weight: bold;
          background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }
      }

      .login-title {
        font-size: 24px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px 0;
      }

      .login-subtitle {
        color: var(--text-secondary);
        margin: 0;
      }
    }

    .login-form {
      .login-options {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;

        .forgot-password {
          color: var(--primary-color);
          padding: 0;

          &:hover {
            color: var(--primary-light);
          }
        }
      }

      .login-button {
        width: 100%;
        height: 44px;
        font-size: 16px;
        font-weight: 500;
        background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
        border: none;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
        }
      }
    }

    .divider {
      position: relative;
      text-align: center;
      margin: 24px 0;

      &::before {
        content: '';
        position: absolute;
        top: 50%;
        left: 0;
        right: 0;
        height: 1px;
        background: var(--border-primary);
      }

      span {
        background: var(--bg-secondary);
        color: var(--text-tertiary);
        padding: 0 16px;
        font-size: 14px;
      }
    }

    .register-link {
      text-align: center;
      color: var(--text-secondary);
      margin-bottom: 24px;

      .el-button {
        color: var(--primary-color);
        padding: 0;
        margin-left: 4px;

        &:hover {
          color: var(--primary-light);
        }
      }
    }

    .social-login {
      text-align: center;

      .social-title {
        color: var(--text-tertiary);
        font-size: 14px;
        margin: 0 0 16px 0;
      }

      .social-buttons {
        display: flex;
        justify-content: center;
        gap: 12px;

        .social-btn {
          width: 44px;
          height: 44px;
          border: 1px solid var(--border-secondary);
          background: var(--bg-tertiary);
          color: var(--text-secondary);
          transition: all 0.3s ease;

          &:hover {
            border-color: var(--primary-color);
            color: var(--primary-color);
            transform: translateY(-2px);
          }
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

    .floating-shape {
      position: absolute;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
      opacity: 0.1;
      animation: float 6s ease-in-out infinite;

      &.shape-1 {
        width: 120px;
        height: 120px;
        top: 10%;
        left: 10%;
        animation-delay: 0s;
      }

      &.shape-2 {
        width: 80px;
        height: 80px;
        top: 70%;
        right: 15%;
        animation-delay: 2s;
      }

      &.shape-3 {
        width: 100px;
        height: 100px;
        bottom: 20%;
        left: 20%;
        animation-delay: 4s;
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

// Element Plus 样式覆盖
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

:deep(.el-input__inner) {
  color: var(--text-primary);

  &::placeholder {
    color: var(--text-tertiary);
  }
}

:deep(.el-checkbox__label) {
  color: var(--text-secondary);
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

// 响应式样式
@media (max-width: 768px) {
  .login-container {
    padding: 16px;

    .login-card {
      padding: 24px;
      max-width: none;

      .login-header {
        margin-bottom: 24px;

        .logo {
          .logo-icon {
            font-size: 28px;
          }

          .logo-text {
            font-size: 20px;
          }
        }

        .login-title {
          font-size: 20px;
        }
      }

      .social-login {
        .social-buttons {
          .social-btn {
            width: 40px;
            height: 40px;
          }
        }
      }
    }
  }
}
</style>