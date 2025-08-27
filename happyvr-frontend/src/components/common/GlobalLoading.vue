<template>
  <div class="global-loading">
    <div class="loading-backdrop" @click.stop></div>
    <div class="loading-content">
      <div class="loading-spinner">
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
      </div>
      <div class="loading-text">{{ loadingText }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// Props
const props = defineProps({
  text: {
    type: String,
    default: '加载中...'
  }
})

// 响应式数据
const loadingText = ref(props.text)

// 加载文本动画
const loadingTexts = ['加载中', '加载中.', '加载中..', '加载中...']
let textIndex = 0

onMounted(() => {
  const interval = setInterval(() => {
    loadingText.value = loadingTexts[textIndex]
    textIndex = (textIndex + 1) % loadingTexts.length
  }, 500)

  // 组件卸载时清除定时器
  return () => {
    clearInterval(interval)
  }
})
</script>

<style lang="scss" scoped>
.global-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;

  .loading-backdrop {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    backdrop-filter: blur(4px);
  }

  .loading-content {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    padding: 40px;
    background: var(--bg-secondary);
    border: 1px solid var(--border-primary);
    border-radius: 12px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  }

  .loading-spinner {
    position: relative;
    width: 60px;
    height: 60px;

    .spinner-ring {
      position: absolute;
      width: 100%;
      height: 100%;
      border: 3px solid transparent;
      border-radius: 50%;
      animation: spin 1.5s linear infinite;

      &:nth-child(1) {
        border-top-color: var(--primary-color);
        animation-delay: 0s;
      }

      &:nth-child(2) {
        border-right-color: var(--primary-light);
        animation-delay: 0.5s;
        width: 80%;
        height: 80%;
        top: 10%;
        left: 10%;
      }

      &:nth-child(3) {
        border-bottom-color: var(--primary-dark);
        animation-delay: 1s;
        width: 60%;
        height: 60%;
        top: 20%;
        left: 20%;
      }
    }
  }

  .loading-text {
    color: var(--text-primary);
    font-size: 16px;
    font-weight: 500;
    min-width: 80px;
    text-align: center;
  }
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

// 响应式调整
@media (max-width: 768px) {
  .global-loading {
    .loading-content {
      padding: 30px;
      margin: 20px;
    }

    .loading-spinner {
      width: 50px;
      height: 50px;
    }

    .loading-text {
      font-size: 14px;
    }
  }
}
</style>