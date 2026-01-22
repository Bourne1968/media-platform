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
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/',
    redirect: '/home'
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

  // 如果是访客专用页面且已登录，跳转到首页
  if (to.meta.guestOnly && token) {
    next('/home')
    return
  }

  // 需要认证的页面检查
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 管理员页面检查
  if (to.meta.requiresAdmin) {
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
        next('/login')
      }
    } else {
      next('/login')
    }
    return
  }

  next()
})

export default router
