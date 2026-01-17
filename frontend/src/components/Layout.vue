<template>
  <div class="layout-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <div class="logo">
            <h2>AI创作平台</h2>
          </div>
          <div class="nav-menu">
            <el-menu
              :default-active="activeMenu"
              mode="horizontal"
              router
              class="menu"
            >
              <el-menu-item index="/home">
                <el-icon><HomeFilled /></el-icon>
                <span>首页</span>
              </el-menu-item>
              <el-menu-item index="/workbench">
                <el-icon><EditPen /></el-icon>
                <span>创作工作台</span>
              </el-menu-item>
              <el-menu-item index="/history">
                <el-icon><Document /></el-icon>
                <span>创作库</span>
              </el-menu-item>
              <el-menu-item index="/calendar">
                <el-icon><Calendar /></el-icon>
                <span>创作日历</span>
              </el-menu-item>
              <el-menu-item v-if="isAdmin" index="/admin">
                <el-icon><Setting /></el-icon>
                <span>管理后台</span>
              </el-menu-item>
            </el-menu>
          </div>
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="user-name">
                <el-icon><User /></el-icon>
                {{ username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { HomeFilled, EditPen, Document, Calendar, User, ArrowDown, SwitchButton, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const username = ref('')
const isAdmin = ref(false)

const activeMenu = computed(() => {
  return route.path
})

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }).catch(() => {})
  }
}

onMounted(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      username.value = user.username || '用户'
      isAdmin.value = user.role === 'ADMIN'
    } catch (e) {
      username.value = '用户'
      isAdmin.value = false
    }
  }
})
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background: #f0f2f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0;
  height: 60px !important;
  line-height: 60px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo h2 {
  margin: 0;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.menu {
  background: transparent;
  border: none;
}

:deep(.el-menu--horizontal) {
  border-bottom: none;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 2px solid transparent;
}

:deep(.el-menu-item:hover),
:deep(.el-menu-item.is-active) {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  border-bottom-color: #fff;
}

.user-info {
  color: #fff;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #fff;
  font-size: 14px;
}

.user-name:hover {
  opacity: 0.8;
}

.main-content {
  min-height: calc(100vh - 60px);
  padding: 0;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 10px;
  }
  
  .logo h2 {
    font-size: 16px;
  }
  
  .nav-menu {
    flex: none;
  }
}
</style>
