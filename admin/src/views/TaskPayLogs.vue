<template>
  <div class="task-pay-logs">
    <h1 class="text-2xl font-bold mb-2">打款日志</h1>
    <p class="text-sm text-[var(--color-text-tertiary)] mb-6">查看用户提现打款记录，支持财务对账</p>

    <div class="bg-white rounded-xl border border-gray-100 overflow-hidden">
      <t-table :data="logData" :columns="logCols" :loading="loading" :pagination="pg" row-key="id" hover stripe size="small" @page-change="onPg">
        <template #payStatus="{ row }">
          <t-tag :theme="row.payStatus===2?'success':row.payStatus===3?'danger':'warning'" size="small">{{ {1:'处理中',2:'成功',3:'失败'}[row.payStatus] }}</t-tag>
        </template>
        <template #payAmount="{ row }"><span class="text-orange-500 font-semibold">¥{{ row.payAmount }}</span></template>
        <template #failReason="{ row }"><span class="text-xs text-red-500">{{ row.failReason || '-' }}</span></template>
        <template #payTime="{ row }"><span class="text-xs">{{ fmt(row.payTime) || '-' }}</span></template>
        <template #applyTime="{ row }"><span class="text-xs text-[var(--color-text-tertiary)]">{{ fmt(row.applyTime) }}</span></template>
        <template #createTime="{ row }"><span class="text-xs text-[var(--color-text-tertiary)]">{{ fmt(row.createTime) }}</span></template>
      </t-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import request from '@/utils/request'

const logData = ref([]); const loading = ref(false)
const pg = reactive({current:1,pageSize:15,total:0})

const logCols = [
  {colKey:'id',title:'ID',width:70},
  {colKey:'orderId',title:'订单ID',width:80},
  {colKey:'userId',title:'用户ID',width:80},
  {colKey:'taskTitle',title:'任务名称',width:130,ellipsis:true},
  {colKey:'tradeNo',title:'交易单号',width:180,ellipsis:true},
  {colKey:'wechatPayNo',title:'微信单号',width:200,ellipsis:true},
  {colKey:'payAmount',title:'金额',width:80},
  {colKey:'payStatus',title:'打款状态',width:80},
  {colKey:'retryCount',title:'重试',width:60},
  {colKey:'failReason',title:'失败原因',width:150,ellipsis:true},
  {colKey:'applyTime',title:'申请时间',width:170},
  {colKey:'payTime',title:'到账时间',width:170},
  {colKey:'createTime',title:'创建时间',width:170},
]

function fmt(t){if(!t)return'';const d=new Date(t);return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`}

async function fetchLogs() {
  loading.value=true
  try{const r=await request.get('/admin/tasks/pay-logs',{params:{page:pg.current,size:pg.pageSize}});logData.value=r.records||[];pg.total=r.total||0}catch(e){}finally{loading.value=false}
}
function onPg(p){pg.current=p.current;fetchLogs()}

import { onMounted } from 'vue'
onMounted(fetchLogs)
</script>
