<template>
  <div class="point-logs">
    <h1 class="text-2xl font-bold mb-2">积分流水</h1>
    <p class="text-sm text-[var(--color-text-tertiary)] mb-6">查看所有积分变动记录</p>
    <div class="bg-white rounded-xl border border-gray-100 overflow-hidden p-6">
      <div class="flex flex-wrap gap-3 items-center mb-3">
        <t-input v-model="lKeyword" placeholder="用户ID" clearable class="w-[140px]" @enter="search" @clear="search" />
        <t-select v-model="lType" placeholder="类型" clearable class="w-[120px]" @change="search">
          <t-option :value="null" label="全部" /><t-option :value="1" label="获取" /><t-option :value="2" label="消耗" />
        </t-select>
        <t-button theme="primary" @click="search"><Search class="w-4 h-4 mr-1" />搜索</t-button>
        <t-button variant="outline" @click="lKeyword='';lType=null;search()">重置</t-button>
      </div>
      <t-table :data="data" :columns="cols" :loading="loading" :pagination="pg" row-key="id" hover stripe size="small" @page-change="onPg">
        <template #nickname="{ row }"><span class="text-sm">{{ row.nickname || '-' }}</span></template>
        <template #type="{ row }">
          <t-tag :theme="row.type===1?'success':'warning'" variant="light" size="small">{{ row.type===1?'+'+row.point:row.point }}</t-tag>
        </template>
        <template #balance="{ row }"><span class="text-sm font-medium">{{ row.balance }}</span></template>
        <template #source="{ row }"><t-tag variant="outline" size="small">{{ row.source||'-' }}</t-tag></template>
        <template #remark="{ row }"><span class="text-xs text-[var(--color-text-tertiary)]">{{ row.remark||'-' }}</span></template>
        <template #createTime="{ row }"><span class="text-xs text-[var(--color-text-tertiary)]">{{ fmt(row.createTime) }}</span></template>
      </t-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from 'lucide-vue-next'
import request from '@/utils/request'

const route = useRoute()
const data = ref([]); const loading = ref(false); const lKeyword = ref(''); const lType = ref(null)
const pg = reactive({current:1,pageSize:15,total:0})
const cols = [{colKey:'id',title:'ID',width:70},{colKey:'userId',title:'用户ID',width:80},{colKey:'nickname',title:'昵称'},{colKey:'type',title:'变动',width:80},{colKey:'balance',title:'余额',width:80},{colKey:'source',title:'来源',width:100},{colKey:'remark',title:'备注',ellipsis:true},{colKey:'createTime',title:'时间',width:170}]

function fmt(t){if(!t)return'';const d=new Date(t);return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`}

async function fetchData() {
  loading.value=true
  try{const p={page:pg.current,size:15};if(lKeyword.value)p.userId=Number(lKeyword.value);if(lType.value!=null)p.type=lType.value;const r=await request.get('/admin/points/logs',{params:p});data.value=r.records||[];pg.total=r.total||0}catch(e){}finally{loading.value=false}
}
function search(){pg.current=1;fetchData()}
function onPg(p){pg.current=p.current;fetchData()}

import { onMounted } from 'vue'
onMounted(() => {
  const uid = route.query.userId
  if (uid) { lKeyword.value = String(uid); fetchData() }
  else fetchData()
})
</script>
