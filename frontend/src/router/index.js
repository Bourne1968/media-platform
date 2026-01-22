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
    meta: { requiresAuth: true }
  },
  {
    path: '/workbench',
    name: 'Workbench',
    component: Workbench,
    meta: { requiresAuth: true }
  },
  {
    path: '/cover-design',
    name: 'CoverDesign',
    component: CoverDesign,
    meta: { requiresAuth: true }
  },
  {
    path: '/history',
    name: 'History',
    component: History,
    meta: { requiresAuth: true }
  },
  {
    path: '/calendar',
    name: 'Calendar',
    component: Calendar,
    meta: { requiresAuth: true }
  },
  {
    path: '/inspiration',
    name: 'Inspiration',
    component: Inspiration,
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { requiresAuth: true }
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
  const token = localStorage.getItem('token')
  
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
    
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        if (user.role === 'ADMIN') {
          next()
        } else {
          next('/workbench')
        }
      } catch (e) {
        next('/')
      }
    } else {
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
