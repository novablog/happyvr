<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { provide } from 'vue'
import { useUserStore } from '@/stores/user'

// 提供全局用户状态
const userStore = useUserStore()
provide('userStore', userStore)

// HappyVR主应用组件
</script>

<style lang="scss">
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  margin: 0;
  padding: 0;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  background-color: var(--el-bg-color);
}
</style>