import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'dashboard' }
      },
      {
        path: 'admins',
        name: 'AdminManage',
        component: () => import('@/views/AdminUserManage.vue'),
        meta: { title: '管理员管理', icon: 'shield' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/UserManage.vue'),
        meta: { title: '用户管理', icon: 'users' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置', icon: 'settings' }
      },
      {
        path: 'points',
        name: 'Points',
        component: () => import('@/views/Points.vue'),
        meta: { title: '积分管理', icon: 'coins' }
      },
      {
        path: 'invites',
        name: 'Invites',
        component: () => import('@/views/Invite.vue'),
        meta: { title: '邀请管理', icon: 'users' }
      },
      {
        path: 'tasks',
        name: 'TaskManage',
        component: () => import('@/views/TaskManage.vue'),
        meta: { title: '任务管理' }
      },
      {
        path: 'task-orders',
        name: 'TaskOrders',
        component: () => import('@/views/TaskOrders.vue'),
        meta: { title: '审核订单' }
      },
      {
        path: 'task-pay-logs',
        name: 'TaskPayLogs',
        component: () => import('@/views/TaskPayLogs.vue'),
        meta: { title: '打款日志' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - NeoApp Admin` : 'NeoApp Admin'

  // 鉴权：需要登录的页面检查 token
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
