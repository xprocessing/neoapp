<template>
  <div class="web-layout min-h-screen flex flex-col">
    <!-- 顶栏 -->
    <header class="fixed top-0 left-0 right-0 h-[var(--header-height)] bg-white/80 backdrop-blur-md border-b border-gray-100 z-50 transition-shadow">
      <div class="section-container h-full flex items-center justify-between">
        <!-- Logo -->
        <router-link to="/" class="flex items-center gap-2.5 no-underline">
          <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-[#0052D9] to-[#618DFF] flex items-center justify-center text-white font-bold text-base">
            N
          </div>
          <span class="text-xl font-semibold text-[var(--color-text-primary)]">
            NeoApp
          </span>
        </router-link>

        <!-- PC 导航 -->
        <nav class="hidden md:flex items-center gap-1">
          <a
            v-for="item in navItems"
            :key="item.label"
            :href="item.href"
            class="px-4 py-2 text-sm text-[var(--color-text-secondary)] hover:text-[var(--color-primary)] rounded-[var(--radius-btn)] hover:bg-blue-50/50 transition-all"
          >
            {{ item.label }}
          </a>
          <router-link to="/tasks" class="px-4 py-2 text-sm text-[var(--color-text-secondary)] hover:text-[var(--color-primary)] rounded-[var(--radius-btn)] hover:bg-blue-50/50 transition-all">任务中心</router-link>
        </nav>

        <!-- 右侧操作 -->
        <div class="flex items-center gap-3">
          <a
            href="/admin"
            class="hidden sm:inline-flex items-center gap-1.5 px-4 py-2 text-sm text-[var(--color-primary)] border border-[var(--color-primary)] rounded-[var(--radius-btn)] hover:bg-[var(--color-primary)] hover:text-white transition-all font-medium"
          >
            管理后台
          </a>
          <!-- 已登录 -->
          <template v-if="isLoggedIn">
            <router-link to="/tasks" class="hidden sm:inline-flex items-center px-3 py-2 text-sm text-[var(--color-text-secondary)] hover:text-[var(--color-primary)] rounded-[var(--radius-btn)] hover:bg-gray-50 transition-all">任务中心</router-link>
            <router-link to="/my-tasks" class="hidden sm:inline-flex items-center px-3 py-2 text-sm text-[var(--color-text-secondary)] hover:text-[var(--color-primary)] rounded-[var(--radius-btn)] hover:bg-gray-50 transition-all">我的任务</router-link>
            <router-link
              to="/profile"
              class="hidden sm:inline-flex items-center gap-1.5 px-3 py-2 text-sm text-[var(--color-text-secondary)] hover:text-[var(--color-primary)] rounded-[var(--radius-btn)] hover:bg-gray-50 transition-all"
            >
              <User class="w-4 h-4" />个人中心
            </router-link>
            <button
              class="hidden sm:inline-flex items-center gap-1.5 px-3 py-2 text-sm text-[var(--color-text-tertiary)] hover:text-[var(--color-danger)] rounded-[var(--radius-btn)] hover:bg-gray-50 transition-all"
              @click="handleLogout"
            >
              <LogOut class="w-4 h-4" />
            </button>
          </template>
          <!-- 未登录 -->
          <template v-else>
            <router-link
              to="/login"
              class="hidden sm:inline-flex items-center gap-1.5 px-4 py-2 text-sm text-[var(--color-text-primary)] rounded-[var(--radius-btn)] hover:bg-gray-50 transition-all"
            >
              登录
            </router-link>
            <router-link
              to="/register"
              class="hidden sm:inline-flex items-center gap-1.5 px-4 py-2 text-sm text-white bg-[var(--color-primary)] rounded-[var(--radius-btn)] hover:bg-[var(--color-primary-hover)] transition-all font-medium"
            >
              注册
            </router-link>
          </template>
          <!-- 移动端菜单按钮 -->
          <button
            class="md:hidden p-2 text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors"
            @click="mobileMenuOpen = !mobileMenuOpen"
          >
            <Menu v-if="!mobileMenuOpen" class="w-6 h-6" />
            <X v-else class="w-6 h-6" />
          </button>
        </div>
      </div>

      <!-- 移动端下拉菜单 -->
      <transition name="fade">
        <div v-if="mobileMenuOpen" class="md:hidden bg-white border-t border-gray-50 shadow-lg">
          <div class="section-container py-4 flex flex-col gap-2">
            <a
              v-for="item in navItems"
              :key="item.label"
              :href="item.href"
              class="px-4 py-3 text-sm text-[var(--color-text-secondary)] hover:bg-blue-50/50 rounded-lg transition-colors"
              @click="mobileMenuOpen = false"
            >
              {{ item.label }}
            </a>
            <div class="border-t border-gray-50 pt-2 mt-1">
              <router-link to="/tasks" class="block px-4 py-3 text-sm text-[var(--color-text-secondary)] hover:bg-blue-50/50 rounded-lg transition-colors" @click="mobileMenuOpen = false">任务中心</router-link>
              <template v-if="isLoggedIn">
                <router-link to="/my-tasks" class="block px-4 py-3 text-sm text-[var(--color-text-secondary)] hover:bg-blue-50/50 rounded-lg transition-colors" @click="mobileMenuOpen = false">我的任务</router-link>
                <router-link to="/profile" class="block px-4 py-3 text-sm text-[var(--color-text-secondary)] hover:bg-blue-50/50 rounded-lg transition-colors" @click="mobileMenuOpen = false">
                  个人中心
                </router-link>
                <button class="w-full text-left px-4 py-3 text-sm text-[var(--color-danger)] hover:bg-red-50/50 rounded-lg transition-colors" @click="handleLogout(); mobileMenuOpen = false">
                  退出登录
                </button>
              </template>
              <template v-else>
                <router-link to="/login" class="block px-4 py-3 text-sm text-[var(--color-text-secondary)] hover:bg-blue-50/50 rounded-lg transition-colors" @click="mobileMenuOpen = false">
                  登录
                </router-link>
                <router-link to="/register" class="block px-4 py-3 text-sm text-[var(--color-primary)] hover:bg-blue-50/50 rounded-lg transition-colors font-medium" @click="mobileMenuOpen = false">
                  注册
                </router-link>
              </template>
            </div>
          </div>
        </div>
      </transition>
    </header>

    <!-- 主内容 -->
    <main class="flex-1 pt-[var(--header-height)]">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底栏 -->
    <footer class="bg-[#1D2129] text-white/70 py-12">
      <div class="section-container">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-8 mb-8">
          <div>
            <h4 class="text-white font-medium mb-4 text-sm">产品</h4>
            <ul class="space-y-2">
              <li><a href="#" class="text-sm text-white/50 hover:text-white transition-colors">功能特性</a></li>
              <li><a href="#" class="text-sm text-white/50 hover:text-white transition-colors">定价方案</a></li>
              <li><a href="#" class="text-sm text-white/50 hover:text-white transition-colors">更新日志</a></li>
            </ul>
          </div>
          <div>
            <h4 class="text-white font-medium mb-4 text-sm">支持</h4>
            <ul class="space-y-2">
              <li><a href="#" class="text-sm text-white/50 hover:text-white transition-colors">帮助中心</a></li>
              <li><a href="#" class="text-sm text-white/50 hover:text-white transition-colors">API 文档</a></li>
              <li><a href="#" class="text-sm text-white/50 hover:text-white transition-colors">联系我们</a></li>
            </ul>
          </div>
          <div>
            <h4 class="text-white font-medium mb-4 text-sm">公司</h4>
            <ul class="space-y-2">
              <li><a href="#" class="text-sm text-white/50 hover:text-white transition-colors">关于我们</a></li>
              <li><a href="#" class="text-sm text-white/50 hover:text-white transition-colors">加入团队</a></li>
              <li><a href="#" class="text-sm text-white/50 hover:text-white transition-colors">新闻动态</a></li>
            </ul>
          </div>
          <div>
            <h4 class="text-white font-medium mb-4 text-sm">法律</h4>
            <ul class="space-y-2">
              <li><a href="#" class="text-sm text-white/50 hover:text-white transition-colors">隐私政策</a></li>
              <li><a href="#" class="text-sm text-white/50 hover:text-white transition-colors">服务条款</a></li>
            </ul>
          </div>
        </div>
        <div class="border-t border-white/10 pt-6 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div class="flex items-center gap-2">
            <div class="w-6 h-6 rounded bg-gradient-to-br from-[#0052D9] to-[#618DFF] flex items-center justify-center text-white text-xs font-bold">N</div>
            <span class="text-xs text-white/40">NeoApp. All rights reserved.</span>
          </div>
          <span class="text-xs text-white/30">京ICP备xxxxxxxx号</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Menu, X, User, LogOut } from 'lucide-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const mobileMenuOpen = ref(false)
const loggedIn = ref(!!localStorage.getItem('token'))

const isLoggedIn = computed(() => loggedIn.value)

// 监听路由变化，更新登录状态
watch(() => router.currentRoute.value.path, () => {
  loggedIn.value = !!localStorage.getItem('token')
})

function handleLogout() {
  localStorage.removeItem('token')
  loggedIn.value = false
  MessagePlugin.success('已退出登录')
  router.push('/')
}

const navItems = [
  { label: '首页', href: '/' },
  { label: '产品', href: '#features' },
  { label: '关于我们', href: '#about' },
  { label: '联系我们', href: '#contact' },
]
</script>
