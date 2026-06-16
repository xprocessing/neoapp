<template>
  <div class="points-page">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-[var(--color-text-primary)]">积分管理</h1>
      <p class="text-sm text-[var(--color-text-tertiary)] mt-1">管理用户积分与流水记录</p>
    </div>

    <!-- 自定义 Tab -->
    <div class="bg-white rounded-xl border border-gray-100 overflow-hidden">
      <div class="flex border-b border-gray-100 px-6">
        <button
          :class="['px-4 py-3 text-sm font-medium border-b-2 transition-colors', activeTab === 'users' ? 'border-[var(--color-primary)] text-[var(--color-primary)]' : 'border-transparent text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)]']"
          @click="activeTab = 'users'"
        >用户积分</button>
        <button
          :class="['px-4 py-3 text-sm font-medium border-b-2 transition-colors', activeTab === 'logs' ? 'border-[var(--color-primary)] text-[var(--color-primary)]' : 'border-transparent text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)]']"
          @click="activeTab = 'logs'"
        >积分流水</button>
      </div>

      <!-- 用户积分面板 -->
      <div v-show="activeTab === 'users'" class="px-6">
        <div class="flex flex-wrap gap-3 items-center py-3">
          <t-input v-model="uKeyword" placeholder="搜索昵称/手机号" clearable class="w-[220px]" @enter="fetchUsers" @clear="fetchUsers">
            <template #prefix-icon><Search class="w-4 h-4" /></template>
          </t-input>
          <t-button theme="primary" @click="fetchUsers"><Search class="w-4 h-4 mr-1" />搜索</t-button>
          <t-button variant="outline" @click="uKeyword='';fetchUsers()">重置</t-button>
        </div>
        <t-table :data="userData" :columns="userColumns" :loading="userLoading" :pagination="userPagination" row-key="userId" hover stripe @page-change="onUserPage">
          <template #nickname="{ row }"><span class="text-sm">{{ row.nickname || '-' }}</span></template>
          <template #phone="{ row }"><span class="text-sm text-[var(--color-text-secondary)]">{{ row.phone || '-' }}</span></template>
          <template #totalPoint="{ row }"><span class="text-sm font-medium">{{ row.totalPoint || 0 }}</span></template>
          <template #usablePoint="{ row }"><span class="text-sm font-semibold text-[var(--color-primary)]">{{ row.usablePoint || 0 }}</span></template>
          <template #operation="{ row }">
            <t-button variant="text" theme="primary" size="small" @click="viewLogs(row.userId)">查看流水</t-button>
          </template>
        </t-table>
      </div>

      <!-- 积分流水面板 -->
      <div v-show="activeTab === 'logs'" class="px-6">
        <div class="flex flex-wrap gap-3 items-center py-3">
          <t-input v-model="lKeyword" placeholder="用户ID" clearable class="w-[140px]" @enter="filterLogs" @clear="filterLogs" />
          <t-select v-model="lType" placeholder="类型" clearable class="w-[120px]" @change="filterLogs">
            <t-option :value="null" label="全部" />
            <t-option :value="1" label="获取" />
            <t-option :value="2" label="消耗" />
          </t-select>
          <t-button theme="primary" @click="filterLogs"><Search class="w-4 h-4 mr-1" />搜索</t-button>
          <t-button variant="outline" @click="lKeyword='';lType=null;filterLogs()">重置</t-button>
        </div>
        <t-table :data="logData" :columns="logColumns" :loading="logLoading" :pagination="logPagination" row-key="id" hover stripe size="small" @page-change="onLogPage">
          <template #nickname="{ row }"><span class="text-sm">{{ row.nickname || '-' }}</span></template>
          <template #type="{ row }">
            <t-tag :theme="row.type === 1 ? 'success' : 'warning'" variant="light" size="small">{{ row.type === 1 ? '+' + row.point : row.point }}</t-tag>
          </template>
          <template #balance="{ row }"><span class="text-sm font-medium">{{ row.balance }}</span></template>
          <template #source="{ row }"><t-tag variant="outline" size="small">{{ row.source || '-' }}</t-tag></template>
          <template #remark="{ row }"><span class="text-xs text-[var(--color-text-tertiary)]">{{ row.remark || '-' }}</span></template>
          <template #createTime="{ row }"><span class="text-xs text-[var(--color-text-tertiary)]">{{ formatTime(row.createTime) }}</span></template>
        </t-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Search } from 'lucide-vue-next'
import request from '@/utils/request'

const activeTab = ref('users')

// 用户积分
const userData = ref([])
const userLoading = ref(false)
const uKeyword = ref('')
const uPage = ref(1)
const userPagination = reactive({ current: 1, pageSize: 10, total: 0 })

const userColumns = [
  { colKey: 'userId', title: '用户ID', width: 80 },
  { colKey: 'nickname', title: '昵称' },
  { colKey: 'phone', title: '手机号', width: 130 },
  { colKey: 'totalPoint', title: '累计积分', width: 100 },
  { colKey: 'usablePoint', title: '可用积分', width: 100 },
  { colKey: 'operation', title: '操作', width: 100, fixed: 'right' },
]

async function fetchUsers() {
  userLoading.value = true
  try {
    const params = { page: uPage.value, size: 10 }
    if (uKeyword.value) params.keyword = uKeyword.value
    const res = await request.get('/admin/points', { params })
    userData.value = res.records || []
    userPagination.current = res.current || 1
    userPagination.total = res.total || 0
  } catch { /* handled */ } finally { userLoading.value = false }
}

function onUserPage(p) { uPage.value = p.current; fetchUsers() }

// 积分流水
const logData = ref([])
const logLoading = ref(false)
const lKeyword = ref('')
const lType = ref(null)
const lPage = ref(1)
const logPagination = reactive({ current: 1, pageSize: 15, total: 0 })

const logColumns = [
  { colKey: 'id', title: 'ID', width: 70 },
  { colKey: 'userId', title: '用户ID', width: 80 },
  { colKey: 'nickname', title: '昵称' },
  { colKey: 'type', title: '变动', width: 80 },
  { colKey: 'balance', title: '余额', width: 80 },
  { colKey: 'source', title: '来源', width: 100 },
  { colKey: 'remark', title: '备注', ellipsis: true },
  { colKey: 'createTime', title: '时间', width: 170 },
]

async function fetchLogs(userId) {
  logLoading.value = true
  try {
    const params = { page: lPage.value, size: 15 }
    if (userId) params.userId = userId
    else if (lKeyword.value) params.userId = Number(lKeyword.value)
    if (lType.value != null) params.type = lType.value
    const res = await request.get('/admin/points/logs', { params })
    logData.value = res.records || []
    logPagination.current = res.current || 1
    logPagination.total = res.total || 0
  } catch (e) {
    console.error('fetchLogs error:', e)
  } finally {
    logLoading.value = false
  }
}

function viewLogs(userId) {
  lPage.value = 1
  lKeyword.value = String(userId)
  lType.value = null
  fetchLogs(userId)
  activeTab.value = 'logs'
}

function filterLogs() {
  lPage.value = 1
  fetchLogs(lKeyword.value ? Number(lKeyword.value) : undefined)
}
function onLogPage(p) { lPage.value = p.current; fetchLogs(lKeyword.value ? Number(lKeyword.value) : undefined) }

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

import { onMounted } from 'vue'
onMounted(fetchUsers)
</script>
