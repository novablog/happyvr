<template>
  <div class="not-found-page">
    <div class="error-container">
      <div class="error-animation">
        <div class="error-number">404</div>
        <div class="error-vr-icon">
          <el-icon><VideoCamera /></el-icon>
        </div>
      </div>
      
      <div class="error-content">
        <h1 class="error-title">页面未找到</h1>
        <p class="error-description">
          抱歉，您访问的页面不存在或已被移除。
        </p>
        
        <div class="error-actions">
          <el-button type="primary" @click="goHome">
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
          <el-button @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回上页
          </el-button>
        </div>
        
        <div class="error-suggestions">
          <h3>您可以尝试：</h3>
          <ul>
            <li>检查网址是否正确</li>
            <li>返回上一页面</li>
            <li>访问我们的首页</li>
            <li>联系客服获取帮助</li>
          </ul>
        </div>
      </div>
    </div>
    
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/dashboard')
  }
}
</script>

<style lang="scss" scoped>
.not-found-page {
  min-height: 100vh;
  background: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 20px;

  .error-container {
    text-align: center;
    max-width: 600px;
    position: relative;
    z-index: 2;

    .error-animation {
      position: relative;
      margin-bottom: 40px;

      .error-number {
        font-size: 120px;
        font-weight: 900;
        background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        line-height: 1;
        margin-bottom: 20px;
        animation: pulse 2s ease-in-out infinite;
      }

      .error-vr-icon {
        font-size: 48px;
        color: var(--primary-color);
        animation: float 3s ease-in-out infinite;
      }
    }

    .error-content {
      .error-title {
        font-size: 32px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 16px 0;
      }

      .error-description {
        font-size: 16px;
        color: var(--text-secondary);
        margin: 0 0 32px 0;
        line-height: 1.6;
      }

      .error-actions {
        display: flex;
        gap: 16px;
        justify-content: center;
        margin-bottom: 40px;
        flex-wrap: wrap;

        .el-button {
          padding: 12px 24px;
          font-size: 14px;
        }
      }

      .error-suggestions {
        text-align: left;
        background: var(--bg-secondary);
        border: 1px solid var(--border-primary);
        border-radius: 12px;
        padding: 24px;

        h3 {
          font-size: 16px;
          color: var(--text-primary);
          margin: 0 0 16px 0;
        }

        ul {
          margin: 0;
          padding-left: 20px;
          color: var(--text-secondary);

          li {
            margin-bottom: 8px;
            line-height: 1.5;

            &:last-child {
              margin-bottom: 0;
            }
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
        width: 100px;
        height: 100px;
        top: 20%;
        left: 10%;
        animation-delay: 0s;
      }

      &.shape-2 {
        width: 150px;
        height: 150px;
        top: 60%;
        right: 15%;
        animation-delay: 2s;
      }

      &.shape-3 {
        width: 80px;
        height: 80px;
        bottom: 20%;
        left: 20%;
        animation-delay: 4s;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
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

// 响应式样式
@media (max-width: 768px) {
  .not-found-page {
    padding: 16px;

    .error-container {
      .error-animation {
        .error-number {
          font-size: 80px;
        }

        .error-vr-icon {
          font-size: 36px;
        }
      }

      .error-content {
        .error-title {
          font-size: 24px;
        }

        .error-description {
          font-size: 14px;
        }

        .error-actions {
          flex-direction: column;
          align-items: center;

          .el-button {
            width: 200px;
          }
        }

        .error-suggestions {
          padding: 20px;

          h3 {
            font-size: 14px;
          }

          ul {
            font-size: 14px;
          }
        }
      }
    }
  }
}
</style>