import { createRouter, createWebHistory } from 'vue-router'
import Landing from '@/views/Landing.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Home from '@/views/Home.vue'
import Workbench from '@/views/Workbench.vue'
import CoverDesign from '@/views/CoverDesign.vue'
import History from '@/views/History.vue'
import Calendar from '@/views/Calendar.vue'
import Inspiration from '@/views/Inspiration.vue'
import Settings from '@/views/Settings.vue'
import Admin from '@/views/Admin.vue'

const routes = [
  {
    path: '/',
    name: 'Landing',
    component: Landing,
    meta: { guestOnly: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { guestOnly: true }
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true, title: '首页' }
  },
  {
    path: '/workbench',
    name: 'Workbench',
    component: Workbench,
    meta: { requiresAuth: true, title: 'AI文案生成页' }
  },
  {
    path: '/cover-design',
    name: 'CoverDesign',
    component: CoverDesign,
    meta: { requiresAuth: true, title: 'AI封面设计' }
  },
  {
    path: '/history',
    name: 'History',
    component: History,
    meta: { requiresAuth: true, title: '创作库' }
  },
  {
    path: '/calendar',
    name: 'Calendar',
    component: Calendar,
    meta: { requiresAuth: true, title: '创作日历' }
  },
  {
    path: '/inspiration',
    name: 'Inspiration',
    component: Inspiration,
    meta: { requiresAuth: true, title: '灵感中心' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { requiresAuth: true, title: '个人设置' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  
  // 定义允许未登录访问的页面
  const guestPaths = ['/', '/login', '/register']
  const isGuestPath = guestPaths.includes(to.path)
  
  // 如果已登录用户访问访客页面，跳转到首页
  if (isGuestPath && token) {
    if (to.path === '/') {
      next('/home')
    } else {
      // 已登录用户访问登录/注册页，跳转到首页
      next('/home')
    }
    return
  }
  
  // 如果未登录用户访问需要认证的页面，跳转到landing页面
  if (!isGuestPath && !token) {
    // 未登录用户只能访问landing页面
    next('/')
    return
  }
  
  // 管理员页面检查
  if (to.meta.requiresAdmin) {
    if (!token) {
      next('/')
      return
    }
    
    // 确保能正确读取用户信息
    const userInfo = sessionStorage.getItem('userInfo')
    if (!userInfo) {
      console.warn('访问管理员页面但 userInfo 不存在，跳转到首页')
      next('/')
      return
    }
    
      try {
        const user = JSON.parse(userInfo)
      // 严格检查角色
        if (user.role === 'ADMIN') {
          next()
        } else {
        console.warn('访问管理员页面但用户角色不是 ADMIN，当前角色：', user.role)
          next('/workbench')
        }
      } catch (e) {
      console.error('解析 userInfo 失败：', e)
      next('/')
      }
    return
  }
  
  // 需要认证的页面，确保有token
  if (to.meta.requiresAuth && !token) {
    next('/')
    return
  }
  
    next()
})

export default router
