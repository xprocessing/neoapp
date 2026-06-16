<template>
  <div class="dashboard">
    <h2 class="text-lg font-semibold text-[var(--color-text-primary)] mb-6">仪表盘</h2>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
      <div class="stat-card bg-white rounded-xl p-5 border border-gray-50 cursor-pointer">
        <div class="flex items-start justify-between mb-3">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center bg-blue-50">
            <Users class="w-5 h-5 text-[var(--color-primary)]" />
          </div>
          <span v-if="stats.todayUsers" class="text-xs font-medium px-2 py-1 rounded-full bg-green-50 text-green-500">+{{ stats.todayUsers }}</span>
        </div>
        <p class="text-2xl font-bold text-[var(--color-text-primary)] mb-1">{{ stats.totalUsers ?? '-' }}</p>
        <p class="text-sm text-[var(--color-text-tertiary)]">总用户数 <span class="text-xs text-green-500 ml-1">今日 +{{ stats.todayUsers ?? 0 }}</span></p>
      </div>

      <div class="stat-card bg-white rounded-xl p-5 border border-gray-50 cursor-pointer">
        <div class="flex items-start justify-between mb-3">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center bg-green-50">
            <ClipboardList class="w-5 h-5 text-green-600" />
          </div>
        </div>
        <p class="text-2xl font-bold text-[var(--color-text-primary)] mb-1">{{ stats.activeTasks ?? '-' }}</p>
        <p class="text-sm text-[var(--color-text-tertiary)]">上架任务数</p>
      </div>

      <div class="stat-card bg-white rounded-xl p-5 border border-gray-50 cursor-pointer">
        <div class="flex items-start justify-between mb-3">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center bg-orange-50">
            <FileText class="w-5 h-5 text-orange-500" />
          </div>
          <span v-if="stats.todayOrders" class="text-xs font-medium px-2 py-1 rounded-full bg-orange-50 text-orange-500">+{{ stats.todayOrders }}</span>
        </div>
        <p class="text-2xl font-bold text-[var(--color-text-primary)] mb-1">{{ stats.totalOrders ?? '-' }}</p>
        <p class="text-sm text-[var(--color-text-tertiary)]">任务领取总量 <span class="text-xs text-orange-500 ml-1">今日 +{{ stats.todayOrders ?? 0 }}</span></p>
      </div>

      <div class="stat-card bg-white rounded-xl p-5 border border-gray-50 cursor-pointer">
        <div class="flex items-start justify-between mb-3">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center bg-purple-50">
            <DollarSign class="w-5 h-5 text-purple-600" />
          </div>
        </div>
        <p class="text-2xl font-bold text-[var(--color-text-primary)] mb-1">¥{{ formatAmount(stats.todayPayAmount) }}</p>
        <p class="text-sm text-[var(--color-text-tertiary)]">今日打款 <span class="text-xs text-[var(--color-text-tertiary)] ml-1">累计 ¥{{ formatAmount(stats.totalPayAmount) }}</span></p>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="bg-white rounded-xl border border-gray-50 p-6">
      <h3 class="text-base font-medium text-[var(--color-text-primary)] mb-4">快捷操作</h3>
      <div class="flex flex-wrap gap-3">
        <router-link to="/tasks"><t-button variant="outline" size="small"><Plus class="w-3 h-3 mr-1" />发布任务</t-button></router-link>
        <router-link to="/task-orders"><t-button variant="outline" size="small"><CheckCircle class="w-3 h-3 mr-1" />审核订单</t-button></router-link>
        <router-link to="/task-pay-logs"><t-button variant="outline" size="small"><DollarSign class="w-3 h-3 mr-1" />打款日志</t-button></router-link>
        <router-link to="/points"><t-button variant="outline" size="small"><Coins class="w-3 h-3 mr-1" />用户积分</t-button></router-link>
        <router-link to="/point-logs"><t-button variant="outline" size="small"><ScrollText class="w-3 h-3 mr-1" />积分流水</t-button></router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Users, ClipboardList, FileText, DollarSign, Plus, CheckCircle, Coins, ScrollText } from 'lucide-vue-next'
import request from '@/utils/request'

const stats = reactive({
  totalUsers: 0, todayUsers: 0,
  activeTasks: 0,
  totalOrders: 0, todayOrders: 0,
  todayPayAmount: 0, totalPayAmount: 0,
})

function formatAmount(val) {
  if (!val) return '0'
  const n = Number(val)
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toFixed(2)
}

onMounted(async () => {
  try {
    const data = await request.get('/admin/dashboard/stats')
    Object.assign(stats, data)
  } catch {}
})
</script>
