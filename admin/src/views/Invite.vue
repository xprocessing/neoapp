<template>
  <div class="invite-page">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-[var(--color-text-primary)]">邀请管理</h1>
      <p class="text-sm text-[var(--color-text-tertiary)] mt-1">管理用户邀请关系和奖励记录</p>
    </div>

    <t-tabs v-model="tab" class="bg-white rounded-xl border border-gray-100 px-6 pt-4">
      <t-tab-panel value="relation" label="邀请关系">
        <div class="flex gap-3 items-center py-3">
          <t-input v-model="keyword" placeholder="搜索昵称/手机号" clearable class="w-[220px]" @enter="fetchRelations" @clear="fetchRelations">
            <template #prefix-icon><Search class="w-4 h-4" /></template>
          </t-input>
          <t-button theme="primary" @click="fetchRelations"><Search class="w-4 h-4 mr-1" />搜索</t-button>
        </div>
        <t-table :data="relData" :columns="relCols" :loading="relLoading" :pagination="relPg" row-key="userId" hover stripe @page-change="onRelPage">
          <template #nickname="{ row }">{{ row.nickname || '-' }}</template>
          <template #phone="{ row }"><span class="text-[var(--color-text-secondary)]">{{ row.phone || '-' }}</span></template>
          <template #parentNickname="{ row }">{{ row.parentNickname || '-' }}</template>
          <template #inviteNum="{ row }"><span class="font-semibold text-[var(--color-primary)]">{{ row.inviteNum }}</span></template>
        </t-table>
      </t-tab-panel>
      <t-tab-pane value="rewards" label="奖励记录">
        <div class="flex gap-3 items-center py-3">
          <t-input v-model="rUserId" placeholder="用户ID" clearable class="w-[140px]" @enter="fetchRewards" @clear="fetchRewards" />
          <t-button theme="primary" @click="fetchRewards"><Search class="w-4 h-4 mr-1" />搜索</t-button>
        </div>
        <t-table :data="rewData" :columns="rewCols" :loading="rewLoading" :pagination="rewPg" row-key="id" hover stripe size="small" @page-change="onRewPage">
          <template #userNickname="{ row }">{{ row.userNickname || '-' }}</template>
          <template #inviteUserNickname="{ row }">{{ row.inviteUserNickname || '-' }}</template>
          <template #rewardNum="{ row }"><span class="font-semibold text-green-500">+{{ row.rewardNum }} 积分</span></template>
          <template #createTime="{ row }"><span class="text-xs text-[var(--color-text-tertiary)]">{{ fmt(row.createTime) }}</span></template>
        </t-table>
      </t-tab-pane>
    </t-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from 'lucide-vue-next'
import request from '@/utils/request'

const tab = ref('relation')
const keyword = ref('')
const relData = ref([]); const relLoading = ref(false); const relPg = reactive({ current:1, pageSize:10, total:0 })
const rewData = ref([]); const rewLoading = ref(false); const rewPg = reactive({ current:1, pageSize:15, total:0 })
const rUserId = ref('')
const rPage = ref(1)

const relCols = [
  { colKey:'userId', title:'用户ID', width:80 },
  { colKey:'nickname', title:'昵称' },
  { colKey:'phone', title:'手机号', width:130 },
  { colKey:'parentNickname', title:'上级', width:100 },
  { colKey:'inviteNum', title:'邀请人数', width:100 },
]
const rewCols = [
  { colKey:'id', title:'ID', width:70 },
  { colKey:'userNickname', title:'获奖用户' },
  { colKey:'inviteUserNickname', title:'被邀请用户' },
  { colKey:'rewardNum', title:'奖励', width:100 },
  { colKey:'remark', title:'备注', ellipsis:true },
  { colKey:'createTime', title:'时间', width:170 },
]

function fmt(t) { if(!t) return ''; const d=new Date(t); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}` }

async function fetchRelations() {
  relLoading.value = true
  try {
    const params = { page: relPg.current, size: relPg.pageSize }
    if (keyword.value) params.keyword = keyword.value
    const r = await request.get('/admin/invites', { params })
    relData.value = r.records || []; relPg.total = r.total || 0
  } catch {} finally { relLoading.value = false }
}
function onRelPage(p) { relPg.current = p.current; fetchRelations() }

async function fetchRewards() {
  rewLoading.value = true
  try {
    const params = { page: rPage.value, size: 15 }
    if (rUserId.value) params.userId = rUserId.value
    const r = await request.get('/admin/invites/rewards', { params })
    rewData.value = r.records || []; rewPg.current = r.current||1; rewPg.total = r.total||0
  } catch {} finally { rewLoading.value = false }
}
function onRewPage(p) { rPage.value = p.current; fetchRewards() }

onMounted(() => { fetchRelations(); fetchRewards() })
</script>
