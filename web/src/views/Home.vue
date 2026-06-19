<template>
  <div class="home-page">
    <!-- Hero 区 -->
    <section class="relative overflow-hidden bg-gradient-to-br from-[#F0F5FF] via-white to-[#E8F2FF] py-20 lg:py-28">
      <!-- 背景装饰 -->
      <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div class="absolute -top-40 -right-40 w-96 h-96 bg-[#0052D9]/5 rounded-full blur-3xl" />
        <div class="absolute -bottom-40 -left-40 w-80 h-80 bg-[#618DFF]/5 rounded-full blur-3xl" />
      </div>

      <div class="section-container relative z-10 text-center">
        <div class="inline-flex items-center gap-2 px-4 py-1.5 bg-white border border-gray-100 rounded-full shadow-sm mb-8">
          <span class="w-1.5 h-1.5 rounded-full bg-[var(--color-success)] animate-pulse" />
          <span class="text-xs text-[var(--color-text-secondary)]">v1.0.0 已发布</span>
        </div>

        <h1 class="text-4xl sm:text-5xl lg:text-6xl font-bold text-[var(--color-text-primary)] leading-tight mb-6">
          让APP开发推广<br class="sm:hidden" />
          <span class="bg-gradient-to-r from-[#0052D9] to-[#618DFF] bg-clip-text text-transparent">
            更简单高效
          </span>
        </h1>

        <p class="text-base sm:text-lg text-[var(--color-text-tertiary)] max-w-2xl mx-auto mb-10 leading-relaxed">
          NeoApp 是一站式网站/APP开发，推广管理平台<br class="hidden sm:block" />
          助力企业数字化转型，提升运营效率。
        </p>

        <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
          <a
            href="/admin"
            class="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-8 py-3.5 bg-gradient-to-r from-[#0052D9] to-[#366EF4] text-white font-medium rounded-[var(--radius-btn)] hover:shadow-lg hover:-translate-y-0.5 transition-all text-base"
          >
            立即开始
            <ArrowRight class="w-4 h-4" />
          </a>
          <a
            href="#features"
            class="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-8 py-3.5 bg-white text-[var(--color-text-primary)] font-medium rounded-[var(--radius-btn)] border border-gray-200 hover:border-[var(--color-primary)] hover:text-[var(--color-primary)] transition-all text-base"
          >
            了解更多
          </a>
        </div>

        <!-- 统计 -->
        <div class="mt-20 grid grid-cols-3 gap-8 max-w-lg mx-auto">
          <div v-for="stat in heroStats" :key="stat.label">
            <p class="text-3xl font-bold text-[var(--color-text-primary)]">{{ stat.value }}</p>
            <p class="text-xs text-[var(--color-text-tertiary)] mt-1">{{ stat.label }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 特性区 -->
    <section id="features" class="py-20 lg:py-24">
      <div class="section-container">
        <div class="text-center mb-14">
          <h2 class="text-2xl sm:text-3xl font-bold text-[var(--color-text-primary)] mb-4">强大的核心功能</h2>
          <p class="text-sm sm:text-base text-[var(--color-text-tertiary)] max-w-xl mx-auto">
            精心设计每一个功能模块，为您提供流畅的使用体验
          </p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="feature in features"
            :key="feature.title"
            class="feature-card bg-white rounded-[var(--radius-card)] p-6 border border-gray-50 card-hover cursor-pointer"
          >
            <div
              :class="['w-12 h-12 rounded-xl flex items-center justify-center mb-4', feature.bgClass]"
            >
              <component :is="feature.icon" :class="['w-6 h-6', feature.iconClass]" />
            </div>
            <h3 class="text-base font-semibold text-[var(--color-text-primary)] mb-2">
              {{ feature.title }}
            </h3>
            <p class="text-sm text-[var(--color-text-tertiary)] leading-relaxed">
              {{ feature.desc }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新任务 -->
    <section v-if="tasks.length" class="py-16 bg-[#F8FAFE]">
      <div class="section-container">
        <div class="text-center mb-10">
          <h2 class="text-2xl sm:text-3xl font-bold text-[var(--color-text-primary)] mb-4">最新任务</h2>
          <p class="text-sm text-[var(--color-text-tertiary)]">完成任务赚取奖励</p>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5 max-w-5xl mx-auto">
          <router-link v-for="t in tasks" :key="t.id" :to="`/tasks/${t.id}`" class="bg-white rounded-xl border border-gray-100 p-5 hover:shadow-lg transition-all group">
            <div class="flex items-start gap-3">
              <t-image v-if="t.taskCover" :src="t.taskCover" fit="cover" class="w-16 h-16 rounded-lg flex-shrink-0" />
              <div class="flex-1 min-w-0">
                <h4 class="font-semibold text-sm group-hover:text-[var(--color-primary)] transition-colors">{{ t.taskTitle }}</h4>
                <p class="text-xs text-[var(--color-text-tertiary)] mt-1 line-clamp-2">{{ t.taskIntro }}</p>
                <div class="flex items-center gap-2 mt-2">
                  <span class="text-orange-500 font-bold text-sm">¥{{ t.rewardAmount }}</span>
                  <t-tag v-if="t.gift" size="small" variant="light">{{ t.gift }}</t-tag>
                </div>
              </div>
            </div>
          </router-link>
        </div>
        <div class="text-center mt-8">
          <router-link to="/tasks" class="inline-flex items-center gap-1 text-sm text-[var(--color-primary)] hover:underline font-medium">查看全部任务 <ArrowRight class="w-4 h-4" /></router-link>
        </div>
      </div>
    </section>

    <!-- CTA 区 -->
    <section class="py-20 bg-gradient-to-r from-[#0052D9] to-[#366EF4]">
      <div class="section-container text-center">
        <h2 class="text-2xl sm:text-3xl font-bold text-white mb-4">
          准备好开始了吗？
        </h2>
        <p class="text-white/80 mb-8 text-sm sm:text-base max-w-md mx-auto">
          立即体验 NeoApp，提升团队工作效率
        </p>
        <a
          href="/admin"
          class="inline-flex items-center gap-2 px-8 py-3.5 bg-white text-[var(--color-primary)] font-medium rounded-[var(--radius-btn)] hover:shadow-xl hover:-translate-y-0.5 transition-all text-base"
        >
          免费试用
          <ArrowRight class="w-4 h-4" />
        </a>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ArrowRight, Zap, Shield, BarChart3, Users, Globe, Smartphone } from 'lucide-vue-next'
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const tasks = ref([])
onMounted(async () => {
  try {
    const r = await request.get('/user/tasks', { params: { page: 1, size: 6 } })
    tasks.value = r?.records || []
  } catch {}
})

const heroStats = [
  { value: '10k+', label: '活跃用户' },
  { value: '99.9%', label: '服务可用率' },
  { value: '50+', label: '企业客户' },
]

const features = [
  {
    icon: Zap,
    title: '高效管理',
    desc: '直观的操作界面，让复杂的业务管理变得简单高效，提升团队协作能力。',
    bgClass: 'bg-blue-50',
    iconClass: 'text-[var(--color-primary)]'
  },
  {
    icon: Shield,
    title: '安全可靠',
    desc: '多层安全防护机制，保障您的数据安全，通过多项权威安全认证。',
    bgClass: 'bg-green-50',
    iconClass: 'text-[var(--color-success)]'
  },
  {
    icon: BarChart3,
    title: '数据分析',
    desc: '丰富的数据可视化报表，实时监控业务指标，辅助管理层做出精准决策。',
    bgClass: 'bg-orange-50',
    iconClass: 'text-[var(--color-warning)]'
  },
  {
    icon: Users,
    title: '团队协作',
    desc: '灵活的角色权限管理，支持多团队并行协作，信息实时同步更新。',
    bgClass: 'bg-purple-50',
    iconClass: 'text-purple-600'
  },
  {
    icon: Globe,
    title: '多端适配',
    desc: 'PC、平板、手机全端完美适配，随时随地处理工作事务。',
    bgClass: 'bg-cyan-50',
    iconClass: 'text-cyan-600'
  },
  {
    icon: Smartphone,
    title: '小程序支持',
    desc: '原生微信小程序体验，轻量级使用场景全覆盖，拓展更多业务可能。',
    bgClass: 'bg-pink-50',
    iconClass: 'text-pink-600'
  },
]
</script>
