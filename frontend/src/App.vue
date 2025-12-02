<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  // 尝试从本地存储恢复登录状态
  userStore.initFromStorage()
})
</script>

<style>
#app {
  min-height: 100vh;
  background-color: #f5f7fa;
}
</style>
