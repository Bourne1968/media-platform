<template>
  <div id="app">
    <!-- 访客页面：Landing、Login、Register -->
    <router-view v-if="isGuestPage" />
    <!-- 登录用户页面：使用Layout -->
    <Layout v-else />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Layout from '@/components/Layout.vue'

const router = useRouter()
const route = useRoute()

// 判断是否为访客页面
const isGuestPage = computed(() => {
  const guestPaths = ['/', '/login', '/register']
  return guestPaths.includes(route.path)
})

onMounted(() => {
  // 如果未登录且不在访客页面，跳转到landing页面
  const token = localStorage.getItem('token')
  if (!token && !isGuestPage.value) {
    router.push('/')
  }
  
  // 初始化深色模式
  const darkMode = localStorage.getItem('darkMode')
  if (darkMode === 'true') {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
})
</script>

<style>
#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  margin: 0;
  padding: 0;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
}
</style>
