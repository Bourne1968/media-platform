<template>
  <div id="app">
    <router-view v-if="route.path === '/login'" />
    <Layout v-else />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Layout from '@/components/Layout.vue'

const router = useRouter()
const route = useRoute()

onMounted(() => {
  // 如果未登录且不在登录页，跳转到登录页
  const token = localStorage.getItem('token')
  if (!token && route.path !== '/login') {
    router.push('/login')
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
