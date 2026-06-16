<template>
  <div class="points-page">
    <h1 class="text-2xl font-bold mb-2">用户积分</h1>
    <p class="text-sm text-[var(--color-text-tertiary)] mb-6">管理用户积分</p>
    <div class="bg-white rounded-xl border border-gray-100 overflow-hidden p-6">
      <div class="flex flex-wrap gap-3 items-center mb-3">
        <t-input v-model="keyword" placeholder="搜索昵称/手机号" clearable class="w-[220px]" @enter="search" @clear="search">
          <template #prefix-icon><Search class="w-4 h-4" /></template>
        </t-input>
        <t-button theme="primary" @click="search"><Search class="w-4 h-4 mr-1" />搜索</t-button>
        <t-button variant="outline" @click="keyword='';search()">重置</t-button>
      </div>
      <t-table :data="data" :columns="cols" :loading="loading" :pagination="pg" row-key="userId" hover stripe @page-change="onPg">
        <template #nickname="{ row }"><span class="text-sm">{{ row.nickname || '-' }}</span></template>
        <template #phone="{ row }"><span class="text-sm text-[var(--color-text-secondary)]">{{ row.phone || '-' }}</span></template>
        <template #totalPoint="{ row }"><span class="text-sm">{{ row.totalPoint || 0 }}</span></template>
        <template #usablePoint="{ row }"><span class="text-sm font-semibold text-[var(--color-primary)]">{{ row.usablePoint || 0 }}</span></template>
        <template #operation="{ row }">
          <t-button variant="text" theme="primary" size="small" @click="$router.push(`/point-logs?userId=${row.userId}`)">查看流水</t-button>
        </template>
      </t-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Search } from 'lucide-vue-next'
import request from '@/utils/request'
const data = ref([]); const loading = ref(false); const keyword = ref('')
const pg = reactive({current:1,pageSize:10,total:0})
const cols = [{colKey:'userId',title:'用户ID',width:80},{colKey:'nickname',title:'昵称'},{colKey:'phone',title:'手机号',width:130},{colKey:'totalPoint',title:'累计积分',width:100},{colKey:'usablePoint',title:'可用积分',width:100},{colKey:'operation',title:'操作',width:100,fixed:'right'}]

async function fetchData() {
  loading.value=true
  try{const p={page:pg.current,size:10};if(keyword.value)p.keyword=keyword.value;const r=await request.get('/admin/points',{params:p});data.value=r.records||[];pg.total=r.total||0}catch(e){}finally{loading.value=false}
}
function search(){pg.current=1;fetchData()}
function onPg(p){pg.current=p.current;fetchData()}

import { onMounted } from 'vue'
onMounted(fetchData)
</script>
