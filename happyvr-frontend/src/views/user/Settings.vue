<template>
  <div class="settings">
    <div class="page-header">
      <h1 class="page-title">设置</h1>
      <p class="page-description">配置您的应用偏好</p>
    </div>

    <div class="settings-content">
      <el-empty description="设置功能即将上线">
        <el-button type="primary" @click="goBack">
          返回工作台
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/dashboard')
}
</script>

<style lang="scss" scoped>
.settings {
  .page-header {
    margin-bottom: 32px;

    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 8px 0;
    }

    .page-description {
      color: var(--text-secondary);
      margin: 0;
    }
  }

  .settings-content {
    padding: 60px 20px;
  }
}
</style>