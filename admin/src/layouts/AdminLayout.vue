<template>
  <div class="admin-layout h-screen flex overflow-hidden bg-[var(--color-bg)]">
    <!-- 移动端遮罩 -->
    <div
      v-if="isMobile && sidebarOpen"
      class="fixed inset-0 bg-black/40 z-40 transition-opacity duration-300"
      @click="toggleSidebar"
    />

    <!-- 侧边栏 -->
    <aside
      :class="[
        'sidebar flex-shrink-0 flex flex-col bg-white border-r border-gray-100 z-50 transition-all duration-300',
        isMobile
          ? 'fixed left-0 top-0 bottom-0 w-[240px]'
          : sidebarCollapsed
            ? 'w-[64px]'
            : 'w-[240px]'
      ]"
    >
      <!-- Logo 区 -->
      <div class="flex items-center h-14 px-4 border-b border-gray-50 flex-shrink-0">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-[#0052D9] to-[#618DFF] flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
          N
        </div>
        <transition name="fade">
          <span v-if="!sidebarCollapsed || isMobile" class="ml-3 font-semibold text-base text-[var(--color-text-primary)] whitespace-nowrap">
            NeoApp Admin
          </span>
        </transition>
      </div>

      <!-- 菜单区 -->
      <nav class="flex-1 overflow-y-auto py-3 px-2">
        <div
          v-for="item in menuItems"
          :key="item.path"
          :class="[
            'menu-item flex items-center px-3 py-2.5 rounded-lg mb-1 cursor-pointer transition-all duration-200 group',
            isActive(item.path)
              ? 'bg-[#E8F2FF] text-[var(--color-primary)]'
              : 'text-[var(--color-text-secondary)] hover:bg-gray-50 hover:text-[var(--color-text-primary)]'
          ]"
          @click="navigate(item.path)"
        >
          <component :is="item.icon" class="w-5 h-5 flex-shrink-0" />
          <transition name="fade">
            <span v-if="!sidebarCollapsed || isMobile" class="ml-3 text-sm whitespace-nowrap">
              {{ item.label }}
            </span>
          </transition>
        </div>
      </nav>

      <!-- 底部折叠按钮 (仅PC端) -->
      <div v-if="!isMobile" class="px-2 py-3 border-t border-gray-50">
        <button
          class="w-full flex items-center justify-center p-2 rounded-lg hover:bg-gray-50 text-[var(--color-text-tertiary)] hover:text-[var(--color-text-primary)] transition-colors"
          @click="sidebarCollapsed = !sidebarCollapsed"
        >
          <ChevronLeft v-if="!sidebarCollapsed" class="w-5 h-5" />
          <ChevronRight v-else class="w-5 h-5" />
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- 顶栏 -->
      <header class="h-14 flex items-center justify-between px-4 bg-white border-b border-gray-100 flex-shrink-0">
        <div class="flex items-center gap-3">
          <!-- 移动端汉堡菜单 -->
          <button
            v-if="isMobile"
            class="p-2 -ml-2 rounded-lg hover:bg-gray-50 text-[var(--color-text-secondary)]"
            @click="toggleSidebar"
          >
            <Menu class="w-5 h-5" />
          </button>
          <!-- 面包屑 -->
          <nav class="hidden sm:flex items-center gap-1 text-sm text-[var(--color-text-tertiary)]">
            <Home class="w-4 h-4" />
            <ChevronRight class="w-3 h-3" />
            <span class="text-[var(--color-text-secondary)]">{{ currentTitle }}</span>
          </nav>
        </div>

        <!-- 用户区 -->
        <div class="flex items-center gap-3">
          <button class="p-2 rounded-lg hover:bg-gray-50 text-[var(--color-text-tertiary)] transition-colors relative">
            <Bell class="w-5 h-5" />
            <span class="absolute top-1.5 right-1.5 w-2 h-2 bg-[var(--color-danger)] rounded-full" />
          </button>
          <t-dropdown :options="userDropdownOptions" @click="handleUserAction">
            <div class="flex items-center gap-2 cursor-pointer px-2 py-1 rounded-lg hover:bg-gray-50 transition-colors">
              <t-avatar size="small">A</t-avatar>
              <span class="text-sm text-[var(--color-text-primary)] hidden sm:inline">{{ userStore.userName }}</span>
              <ChevronDown class="w-4 h-4 text-[var(--color-text-tertiary)] hidden sm:block" />
            </div>
          </t-dropdown>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="flex-1 overflow-y-auto p-6">
        <router-view v-slot="{ Component }">
          <transition name="slide-left" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Home, LayoutDashboard, Users, Shield, Coins, ScrollText, UsersRound, ListTodo, ClipboardCheck, DollarSign, Crown, UserCheck, Settings, Bell,
  ChevronRight, ChevronLeft, ChevronDown, Menu
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarOpen = ref(false)
const sidebarCollapsed = ref(false)
const isMobile = ref(false)

const menuItems = [
  { path: '/dashboard', label: '仪表盘', icon: LayoutDashboard },
  { path: '/admins', label: '管理员管理', icon: Shield },
  { path: '/users', label: '用户管理', icon: Users },
  { path: '/points', label: '用户积分', icon: Coins },
  { path: '/point-logs', label: '积分流水', icon: ScrollText },
  { path: '/invites', label: '邀请管理', icon: UsersRound },
  { path: '/tasks', label: '任务管理', icon: ListTodo },
  { path: '/task-orders', label: '审核订单', icon: ClipboardCheck },
  { path: '/task-pay-logs', label: '打款日志', icon: DollarSign },
  { path: '/members', label: '套餐管理', icon: Crown },
  { path: '/member-users', label: '用户会员', icon: UserCheck },
  { path: '/settings', label: '系统设置', icon: Settings },
]

const userDropdownOptions = [
  { content: '个人信息', value: 'profile' },
  { content: '退出登录', value: 'logout' },
]

const currentTitle = computed(() => route.meta?.title || '')

function isActive(path) {
  return route.path === path
}

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function navigate(path) {
  router.push(path)
  if (isMobile.value) sidebarOpen.value = false
}

function handleUserAction({ value }) {
  if (value === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

function checkMobile() {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) sidebarOpen.value = false
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>
