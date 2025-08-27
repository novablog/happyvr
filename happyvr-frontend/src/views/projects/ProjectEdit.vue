<template>
  <div class="project-edit">
    <div class="page-header">
      <h1 class="page-title">编辑项目</h1>
      <p class="page-description">修改项目信息和设置</p>
    </div>

    <div class="edit-content">
      <el-empty description="项目编辑功能即将上线">
        <el-button type="primary" @click="goBack">
          返回项目列表
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/projects')
}
</script>

<style lang="scss" scoped>
.project-edit {
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

  .edit-content {
    padding: 60px 20px;
  }
}
</style>