<template>
  <div class="profile">
    <div class="page-header">
      <h1 class="page-title">个人资料</h1>
      <p class="page-description">管理您的个人信息</p>
    </div>

    <div class="profile-content">
      <el-empty description="个人资料功能即将上线">
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
.profile {
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

  .profile-content {
    padding: 60px 20px;
  }
}
</style>