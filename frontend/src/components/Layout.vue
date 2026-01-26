<template>
  <div class="layout-container">
    <div class="layout-shell">
      <!-- 侧边栏 -->
      <aside class="side-nav">
        <div class="side-logo">
          <el-icon><Lightning /></el-icon>
          <span>AI Creator Studio</span>
        </div>
        <nav class="side-menu">
          <div
            v-for="item in filteredNavItems"
            :key="item.key"
            class="side-item"
            :class="{ active: activeNav === item.key }"
            @click="handleNavClick(item)"
          >
            <div class="item-left">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </div>
            <span v-if="activeNav === item.key" class="item-dot"></span>
          </div>
        </nav>
        <div class="side-footer">
          <el-button 
            text 
            class="achievement-btn"
            @click="showAchievement = true"
          >
            <el-icon><Trophy /></el-icon>
            <span>成就系统</span>
          </el-button>
          <el-dropdown placement="top-start" @command="handleUserCommand" trigger="click">
            <div class="user-card" style="cursor: pointer;">
              <div class="user-avatar">
                <img v-if="userAvatar" :src="userAvatar" alt="头像" class="avatar-img" />
                <span v-else>{{ userInitial }}</span>
              </div>
              <div>
                <p class="user-name">{{ username || '用户' }}</p>
                <p class="user-tier">{{ userTier }}</p>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="isAdmin" command="admin" divided>
                  <el-icon><User /></el-icon>
                  管理员后台
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  个人设置
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </aside>

      <!-- 内容区域 -->
      <div class="content-area">
        <router-view v-if="route.path" />
      </div>
    </div>
    
    <!-- 成就系统 -->
    <AchievementSystem v-if="isLoggedIn" v-model="showAchievement" />
    
    <!-- 新手引导 -->
    <Guide v-if="isLoggedIn" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { 
  HomeFilled,
  EditPen,
  Document,
  Calendar,
  User,
  ArrowDown,
  SwitchButton,
  Setting,
  Trophy,
  Lightning,
  Picture,
  Star
} from '@element-plus/icons-vue'
import AchievementSystem from './AchievementSystem.vue'
import Guide from './Guide.vue'

const router = useRouter()
const route = useRoute()

const username = ref('')
const isAdmin = ref(false)
const showAchievement = ref(false)
const isLoggedIn = ref(false)
const userTier = ref('普通用户')
const activeNav = ref('home')
const userAvatar = ref('')

const navItems = [
  { key: 'home', label: '首页', icon: HomeFilled, route: '/home' },
  { key: 'workbench', label: 'AI文案生成', icon: EditPen, route: '/workbench' },
  { key: 'cover-design', label: 'AI封面设计', icon: Picture, route: '/cover-design' },
  { key: 'inspiration', label: '灵感中心', icon: Star, route: '/inspiration' },
  { key: 'history', label: '创作库', icon: Document, route: '/history' },
  { key: 'calendar', label: '创作日历', icon: Calendar, route: '/calendar' },
  { key: 'settings', label: '设置', icon: Setting, route: '/settings' }
]

const userInitial = computed(() => {
  if (username.value) {
    return username.value.charAt(0).toUpperCase()
  }
  return 'U'
})

const filteredNavItems = computed(() => {
  return navItems
})

// 根据路由更新激活的导航项
const updateActiveNav = () => {
  const path = route.path
  const matchedItem = navItems.find(item => item.route === path)
  if (matchedItem) {
    activeNav.value = matchedItem.key
  } else {
    // 根据路径匹配
    if (path.startsWith('/home')) {
      activeNav.value = 'home'
    } else if (path.startsWith('/workbench')) {
      activeNav.value = 'workbench'
    } else if (path.startsWith('/cover-design')) {
      activeNav.value = 'cover-design'
    } else if (path.startsWith('/inspiration')) {
      activeNav.value = 'inspiration'
    } else if (path.startsWith('/history')) {
      activeNav.value = 'history'
    } else if (path.startsWith('/calendar')) {
      activeNav.value = 'calendar'
    } else if (path.startsWith('/settings')) {
      activeNav.value = 'settings'
    }
    // 管理员页面不显示在侧边栏，所以不需要设置 activeNav
  }
}

const handleNavClick = (item) => {
  activeNav.value = item.key
  if (item.route && item.route !== route.path) {
    router.push(item.route)
  }
}

const handleUserCommand = (command) => {
  if (command === 'admin') {
    // 直接导航到管理员页面，路由守卫会处理权限检查
    router.push('/admin').catch((err) => {
      console.error('导航到管理员页面失败：', err)
    })
  } else if (command === 'settings') {
    router.push('/settings')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')
      router.push('/')
    }).catch(() => {})
  }
}

// 监听路由变化
watch(() => route.path, () => {
  updateActiveNav()
}, { immediate: true })

onMounted(() => {
  const token = sessionStorage.getItem('token')
  
  // 设置登录状态
  isLoggedIn.value = !!token
  
  // 加载用户信息
  const loadUserInfo = () => {
  const userInfo = sessionStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      username.value = user.username || '用户'
      isAdmin.value = user.role === 'ADMIN'
        userTier.value = user.tier || '普通用户'
        userAvatar.value = user.avatar || ''
    } catch (e) {
      username.value = '用户'
      isAdmin.value = false
        userTier.value = '普通用户'
        userAvatar.value = ''
      }
    }
  }
  
  loadUserInfo()
  
  // 监听sessionStorage变化，以便头像更新后能实时显示
  // 注意：storage事件只在其他标签页/窗口触发，同窗口内需要使用自定义事件
  
  // 自定义事件监听（用于同窗口内的更新）
  window.addEventListener('userInfoUpdated', () => {
    loadUserInfo()
  })
  
  // 初始化激活的导航项
  updateActiveNav()
})
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  width: 100%;
  background: var(--bg-color);
  overflow-x: hidden;
  transition: background-color 0.3s ease;
}

.layout-shell {
  min-height: 100vh;
  position: relative;
  width: 100%;
}

.side-nav {
  width: 220px;
  border-right: 1px solid var(--border-color);
  background: var(--card-bg);
  display: flex;
  flex-direction: column;
  padding: 18px 12px;
  transition: background-color 0.3s ease, border-color 0.3s ease;
  gap: 12px;
  height: 100vh;
  overflow-y: auto;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
}

.side-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-radius: 16px;
  font-weight: 700;
  color: var(--primary-color);
  background: var(--primary-light);
  font-size: 14px;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.side-logo .el-icon {
  font-size: 20px;
}

.side-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 4px;
}

.side-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 14px;
  color: var(--text-regular);
  cursor: pointer;
  transition: all 0.2s ease;
}

.side-item:hover {
  background: var(--border-light);
}

.side-item.active {
  background: var(--primary-light);
  color: var(--primary-color);
  font-weight: 700;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.item-dot {
  width: 6px;
  height: 6px;
  border-radius: 9999px;
  background: var(--primary-color);
  flex-shrink: 0;
}

.side-footer {
  padding: 8px;
  border-top: 1px solid var(--border-color);
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: border-color 0.3s ease;
}

.achievement-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  color: var(--text-regular);
  transition: all 0.2s ease;
}

.achievement-btn:hover {
  background: var(--border-light);
  color: var(--primary-color);
}

.user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-card:hover {
  background: var(--border-light);
}

.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c5dfa, #6fa8ff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
  overflow: hidden;
}

.user-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.user-name {
  font-weight: 700;
  color: var(--text-primary);
  font-size: 14px;
  margin: 0;
  line-height: 1.2;
}

.user-tier {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.2;
}

.content-area {
  margin-left: 220px;
  min-height: 100vh;
  width: calc(100% - 220px);
  max-width: calc(100vw - 220px);
  background: var(--bg-color);
  position: relative;
  overflow-x: hidden;
  transition: background-color 0.3s ease;
}

@media (max-width: 768px) {
  .side-nav {
    width: 200px;
  }
  
  .content-area {
    margin-left: 200px;
    width: calc(100% - 200px);
  }
  
  .side-logo span {
    display: none;
  }
  
  .side-logo {
    justify-content: center;
  }
}
</style>
