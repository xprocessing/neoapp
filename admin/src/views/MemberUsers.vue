<template>
  <div class="member-users">
    <h1 class="text-2xl font-bold mb-2">用户会员</h1>
    <p class="text-sm text-[var(--color-text-tertiary)] mb-6">查看用户会员状态与到期时间</p>

    <div class="bg-white rounded-xl border border-gray-100 overflow-hidden p-6">
      <div class="flex gap-3 items-center mb-3">
        <t-input v-model="keyword" placeholder="搜索昵称/手机号" clearable class="w-[220px]" @enter="fetchData" @clear="fetchData">
          <template #prefix-icon><Search class="w-4 h-4" /></template>
        </t-input>
        <t-button theme="primary" size="small" @click="fetchData"><Search class="w-4 h-4 mr-1" />搜索</t-button>
      </div>
      <t-table :data="data" :columns="cols" :loading="loading" :pagination="pg" row-key="userId" hover stripe size="small" @page-change="onPg">
        <template #isMember="{ row }"><t-tag :theme="row.isMember===1?'success':'default'" size="small">{{ row.isMember===1?'会员':'非会员' }}</t-tag></template>
        <template #expireTime="{ row }"><span class="text-xs">{{ fmt(row.expireTime) || '-' }}</span></template>
        <template #createTime="{ row }"><span class="text-xs text-[var(--color-text-tertiary)]">{{ fmt(row.createTime) }}</span></template>
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
const cols = [{colKey:'userId',title:'用户ID',width:80},{colKey:'nickname',title:'昵称'},{colKey:'phone',title:'手机号',width:130},{colKey:'isMember',title:'会员',width:80},{colKey:'expireTime',title:'到期时间',width:170},{colKey:'createTime',title:'开通时间',width:170}]

function fmt(t){if(!t)return'';const d=new Date(t);return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`}

async function fetchData() {
  loading.value=true
  try{const p={page:pg.current,size:pg.pageSize};if(keyword.value)p.keyword=keyword.value;const r=await request.get('/admin/member/users',{params:p});data.value=r.records||[];pg.total=r.total||0}catch(e){}finally{loading.value=false}
}
function onPg(p){pg.current=p.current;fetchData()}

import { onMounted } from 'vue'
onMounted(fetchData)
</script>
