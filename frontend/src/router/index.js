import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'
import Workbench from '@/views/Workbench.vue'
import CoverDesign from '@/views/CoverDesign.vue'
import History from '@/views/History.vue'
import Calendar from '@/views/Calendar.vue'
import Admin from '@/views/Admin.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
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
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.requiresAdmin) {
    // 检查是否为管理员
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
  } else {
    next()
  }
})

export default router
