<template>
  <div class="register-container">
    <div class="register-card">
      <!-- Logo和标题 -->
      <div class="register-header">
        <div class="logo">
          <el-icon class="logo-icon">
            <VideoCamera />
          </el-icon>
          <span class="logo-text">HappyVR</span>
        </div>
        <h2 class="register-title">创建账号</h2>
        <p class="register-subtitle">开始您的VR创作之旅</p>
      </div>

      <!-- 注册表单 -->
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" class="register-form"
        @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名" size="large" :prefix-icon="User" clearable
            @blur="checkUsernameAvailability" />
        </el-form-item>

        <el-form-item prop="email">
          <el-input v-model="registerForm.email" placeholder="邮箱地址" size="large" :prefix-icon="Message" clearable
            @blur="checkEmailAvailability" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码" size="large" :prefix-icon="Lock"
            show-password clearable @input="updatePasswordStrength" />
          <!-- 密码强度指示器 -->
          <div class="password-strength" v-if="registerForm.password">
            <div class="strength-bar">
              <div class="strength-fill" :class="passwordStrength.level" :style="{
                width: passwordStrength.percentage + '%',
                backgroundColor: passwordStrength.color
              }"></div>
            </div>
            <div class="strength-info">
              <span class="strength-text" :style="{ color: passwordStrength.color }">
                密码强度：{{ passwordStrength.text }}
              </span>
              <span class="strength-score">{{ passwordStrength.percentage }}/100</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large"
            :prefix-icon="Lock" show-password clearable />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input v-model="registerForm.phone" placeholder="手机号码（可选）" size="large" :prefix-icon="Phone" clearable />
        </el-form-item>

        <!-- 验证码 -->
        <el-form-item prop="captcha" v-if="showCaptcha">
          <div class="captcha-input">
            <el-input v-model="registerForm.captcha" placeholder="验证码" size="large" clearable />
            <el-button class="captcha-btn" :disabled="captchaCountdown > 0" @click="sendCaptcha">
              {{ captchaCountdown > 0 ? `${captchaCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 用户协议 -->
        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意
            <el-button type="text" @click="showUserAgreement">
              《用户协议》
            </el-button>
            和
            <el-button type="text" @click="showPrivacyPolicy">
              《隐私政策》
            </el-button>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" class="register-button" :loading="loading" @click="handleRegister">
            {{ loading ? '注册中...' : '注册账号' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 分割线 -->
      <div class="divider">
        <span>或</span>
      </div>

      <!-- 登录链接 -->
      <div class="login-link">
        <span>已有账号？</span>
        <el-button type="text" @click="goToLogin">
          立即登录
        </el-button>
      </div>

      <!-- 第三方注册 -->
      <div class="social-register">
        <p class="social-title">快速注册</p>
        <div class="social-buttons">
          <el-button circle class="social-btn" @click="handleSocialRegister('github')">
            <el-icon>
              <Platform />
            </el-icon>
          </el-button>
          <el-button circle class="social-btn" @click="handleSocialRegister('google')">
            <el-icon>
              <ChromeFilled />
            </el-icon>
          </el-button>
          <el-button circle class="social-btn" @click="handleSocialRegister('wechat')">
            <el-icon>
              <ChatDotRound />
            </el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 用户协议对话框 -->
    <el-dialog v-model="agreementVisible" title="用户协议" width="80%" max-width="600px">
      <div class="agreement-content">
        <h3>HappyVR 用户协议</h3>
        <p>欢迎使用HappyVR平台！请仔细阅读以下条款：</p>

        <h4>1. 服务条款</h4>
        <p>HappyVR为用户提供VR内容创作和分享服务。用户在使用本服务时，应遵守相关法律法规。</p>

        <h4>2. 用户责任</h4>
        <p>用户应对其创作和发布的内容负责，不得发布违法、有害或侵犯他人权益的内容。</p>

        <h4>3. 知识产权</h4>
        <p>用户创作的原创内容归用户所有，但授权HappyVR在平台内展示和推广。</p>

        <h4>4. 服务变更</h4>
        <p>HappyVR保留随时修改或终止服务的权利，重要变更将提前通知用户。</p>

        <h4>5. 免责声明</h4>
        <p>HappyVR对因使用本服务而产生的任何直接或间接损失不承担责任。</p>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="agreementVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 隐私政策对话框 -->
    <el-dialog v-model="privacyVisible" title="隐私政策" width="80%" max-width="600px">
      <div class="privacy-content">
        <h3>HappyVR 隐私政策</h3>
        <p>我们重视您的隐私保护，本政策说明我们如何收集、使用和保护您的个人信息。</p>

        <h4>1. 信息收集</h4>
        <p>我们收集您主动提供的信息（如注册信息）和使用服务时自动收集的信息（如访问日志）。</p>

        <h4>2. 信息使用</h4>
        <p>我们使用收集的信息来提供服务、改进用户体验、发送重要通知等。</p>

        <h4>3. 信息共享</h4>
        <p>除法律要求外，我们不会向第三方分享您的个人信息。</p>

        <h4>4. 信息安全</h4>
        <p>我们采用行业标准的安全措施保护您的个人信息安全。</p>

        <h4>5. 权利行使</h4>
        <p>您有权访问、更正、删除您的个人信息，或撤回授权同意。</p>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="privacyVisible = false">关闭</el-button>
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
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone, VideoCamera, Platform, ChromeFilled, ChatDotRound } from '@element-plus/icons-vue'
import { calculatePasswordStrength, validatePassword, getPasswordSuggestions, isWeakPassword } from '@/utils/passwordValidator'
import authService from '@/services/authService'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const registerFormRef = ref()
const loading = ref(false)
const showCaptcha = ref(false)
const captchaCountdown = ref(0)
const agreementVisible = ref(false)
const privacyVisible = ref(false)

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  phone: '',
  captcha: '',
  agreement: false
})

// 密码强度
const passwordStrength = ref({
  level: 'weak',
  percentage: 0,
  text: '弱'
})

// 自定义验证函数
const validateUsername = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (!/^[a-zA-Z0-9_]{3,20}$/.test(value)) {
    callback(new Error('用户名只能包含字母、数字和下划线，长度3-20位'))
  } else {
    callback()
  }
}

const validatePasswordStrength = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
    return
  }

  // 检查是否为弱密码
  if (isWeakPassword(value)) {
    callback(new Error('密码过于简单，请使用更复杂的密码'))
    return
  }

  // 使用密码验证工具
  const validation = validatePassword(value)
  if (!validation.valid) {
    callback(new Error(validation.errors[0]))
    return
  }

  // 检查密码强度
  const strength = calculatePasswordStrength(value)
  if (strength.score < 30) {
    callback(new Error('密码强度太弱，请增强密码复杂度'))
    return
  }

  callback()
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}

const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议和隐私政策'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = {
  username: [
    { validator: validateUsername, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { validator: validatePasswordStrength, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

// 方法
const updatePasswordStrength = () => {
  const password = registerForm.password
  passwordStrength.value = calculatePasswordStrength(password)
}

const checkUsernameAvailability = async () => {
  if (!registerForm.username || registerForm.username.length < 3) return

  try {
    await authService.checkUsernameAvailability(registerForm.username)
    // 如果没有抛出错误，说明用户名可用
  } catch (error) {
    if (error.response?.status === 409) {
      ElMessage.warning('用户名已被使用')
    } else {
      console.error('检查用户名失败:', error)
    }
  }
}

const checkEmailAvailability = async () => {
  if (!registerForm.email) return

  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(registerForm.email)) return

  try {
    await authService.checkEmailAvailability(registerForm.email)
    // 如果没有抛出错误，说明邮箱可用
  } catch (error) {
    if (error.response?.status === 409) {
      ElMessage.warning('邮箱已被注册')
    } else {
      console.error('检查邮箱失败:', error)
    }
  }
}

const sendCaptcha = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱地址')
    return
  }

  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(registerForm.email)) {
    ElMessage.warning('请输入正确的邮箱地址')
    return
  }

  try {
    await authService.sendCaptcha(registerForm.email)

    ElMessage.success('验证码已发送到您的邮箱，请查收')

    // 开始倒计时
    captchaCountdown.value = 60
    const timer = setInterval(() => {
      captchaCountdown.value--
      if (captchaCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)

    showCaptcha.value = true
  } catch (error) {
    console.error('发送验证码失败:', error)
    const message = error.response?.data?.message || '发送验证码失败，请稍后重试'
    ElMessage.error(message)
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    const result = await userStore.register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      phone: registerForm.phone,
      captcha: showCaptcha.value ? registerForm.captcha : undefined
    })

    if (result.success) {
      ElMessage.success('注册成功！请登录您的账号')

      // 清空表单
      resetForm()

      // 跳转到登录页面，并预填用户名
      router.push({
        path: '/login',
        query: { username: registerForm.username }
      })
    }
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(registerForm, {
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    phone: '',
    captcha: '',
    agreement: false
  })

  passwordStrength.value = { level: 'weak', percentage: 0, text: '弱' }
  showCaptcha.value = false
  captchaCountdown.value = 0

  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
}

const goToLogin = () => {
  router.push('/login')
}

const showUserAgreement = () => {
  agreementVisible.value = true
}

const showPrivacyPolicy = () => {
  privacyVisible.value = true
}

const handleSocialRegister = (provider) => {
  ElMessage.info(`${provider} 注册功能即将上线`)
  // TODO: 实现第三方注册
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg-primary), var(--bg-secondary));
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  padding: 20px;

  .register-card {
    background: var(--bg-secondary);
    border: 1px solid var(--border-primary);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
    padding: 40px;
    width: 100%;
    max-width: 450px;
    position: relative;
    z-index: 2;
    backdrop-filter: blur(10px);
    max-height: 90vh;
    overflow-y: auto;

    .register-header {
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

      .register-title {
        font-size: 24px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px 0;
      }

      .register-subtitle {
        color: var(--text-secondary);
        margin: 0;
      }
    }

    .register-form {
      .password-strength {
        margin-top: 8px;

        .strength-bar {
          height: 6px;
          background: var(--border-secondary);
          border-radius: 3px;
          overflow: hidden;
          margin-bottom: 6px;
          position: relative;

          .strength-fill {
            height: 100%;
            transition: all 0.4s ease;
            border-radius: 3px;
            position: relative;

            &::after {
              content: '';
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.2) 50%, transparent 100%);
              animation: shimmer 2s infinite;
            }
          }
        }

        .strength-info {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .strength-text {
            font-size: 12px;
            font-weight: 500;
            transition: color 0.3s ease;
          }

          .strength-score {
            font-size: 11px;
            color: var(--text-tertiary);
            font-family: 'Courier New', monospace;
          }
        }
      }

      .captcha-input {
        display: flex;
        gap: 12px;

        .el-input {
          flex: 1;
        }

        .captcha-btn {
          white-space: nowrap;
          min-width: 100px;
        }
      }

      .register-button {
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

    .login-link {
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

    .social-register {
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

.agreement-content,
.privacy-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  color: var(--text-primary);

  h3 {
    color: var(--primary-color);
    margin-bottom: 16px;
  }

  h4 {
    color: var(--text-primary);
    margin: 16px 0 8px 0;
  }

  p {
    color: var(--text-secondary);
    line-height: 1.6;
    margin-bottom: 12px;
  }
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0px);
  }

  50% {
    transform: translateY(-20px);
  }
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }

  100% {
    transform: translateX(100%);
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
  .register-container {
    padding: 16px;

    .register-card {
      padding: 24px;
      max-width: none;

      .register-header {
        margin-bottom: 24px;

        .logo {
          .logo-icon {
            font-size: 28px;
          }

          .logo-text {
            font-size: 20px;
          }
        }

        .register-title {
          font-size: 20px;
        }
      }

      .social-register {
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