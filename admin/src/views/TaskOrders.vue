<template>
  <div class="task-orders">
    <h1 class="text-2xl font-bold mb-2">审核订单</h1>
    <p class="text-sm text-[var(--color-text-tertiary)] mb-6">审核用户提交的任务订单，授权打款</p>

    <div class="bg-white rounded-xl border border-gray-100 overflow-hidden">
      <div class="p-4 border-b border-gray-50">
        <t-select v-model="oStatus" placeholder="审核状态" clearable class="w-[140px]" @change="fetchOrders">
          <t-option :value="null" label="全部" /><t-option :value="2" label="待审核" /><t-option :value="3" label="已通过" /><t-option :value="4" label="已驳回" />
        </t-select>
      </div>
      <t-table :data="orderData" :columns="orderCols" :loading="loading" :pagination="pg" row-key="id" hover stripe size="small" @page-change="onPg">
        <template #auditStatus="{ row }">
          <t-tag :theme="row.auditStatus===2?'warning':row.auditStatus===3?'success':row.auditStatus===4?'danger':'default'" size="small">{{ {1:'待提交',2:'待审核',3:'已通过',4:'已驳回',5:'已过期'}[row.auditStatus] }}</t-tag>
        </template>
        <template #reward_amount="{ row }"><span class="text-orange-500">¥{{ row.rewardAmount }}</span></template>
        <template #grantPay="{ row }"><t-tag :theme="row.grantPay===1?'success':'default'" size="small">{{ row.grantPay===1?'已授权':'未授权' }}</t-tag></template>
        <template #withdrawStatus="{ row }">
          <t-tag :theme="row.withdrawStatus===2?'success':row.withdrawStatus===3?'danger':row.withdrawStatus===1?'warning':'default'" size="small">{{ {0:'未提现',1:'处理中',2:'成功',3:'失败'}[row.withdrawStatus] }}</t-tag>
        </template>
        <template #submitImages="{ row }">
          <t-image v-if="row.submitImages" :src="row.submitImages.split(',')[0]" fit="cover" class="w-10 h-10 rounded" />
          <span v-else class="text-[var(--color-text-tertiary)]">-</span>
        </template>
        <template #expireTime="{ row }"><span class="text-xs">{{ fmt(row.expireTime) }}</span></template>
        <template #createTime="{ row }"><span class="text-xs text-[var(--color-text-tertiary)]">{{ fmt(row.createTime) }}</span></template>
        <template #auditTime="{ row }"><span class="text-xs">{{ fmt(row.auditTime) || '-' }}</span></template>
        <template #operation="{ row }">
          <t-space size="small">
            <t-button v-if="row.auditStatus===2" variant="text" theme="success" size="small" @click="audit(row,3,true)">通过</t-button>
            <t-button v-if="row.auditStatus===2" variant="text" theme="danger" size="small" @click="audit(row,4,false)">驳回</t-button>
            <t-button v-if="row.auditStatus===3&&row.grantPay===0" variant="text" theme="warning" size="small" @click="grant(row.id)">授权打款</t-button>
          </t-space>
        </template>
      </t-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import request from '@/utils/request'

const orderData = ref([]); const loading = ref(false); const oStatus = ref(null)
const pg = reactive({current:1,pageSize:10,total:0})

const orderCols = [
  {colKey:'id',title:'ID',width:70},
  {colKey:'userId',title:'用户ID',width:80},
  {colKey:'taskId',title:'任务ID',width:80},
  {colKey:'openid',title:'OpenID',width:150,ellipsis:true},
  {colKey:'reward_amount',title:'奖励',width:70},
  {colKey:'gift',title:'赠品',width:80,ellipsis:true},
  {colKey:'submitImages',title:'截图',width:60},
  {colKey:'submitNote',title:'订单号/备注',width:150,ellipsis:true},
  {colKey:'auditStatus',title:'审核状态',width:80},
  {colKey:'auditNote',title:'审核备注',width:130,ellipsis:true},
  {colKey:'grantPay',title:'授权打款',width:80},
  {colKey:'withdrawStatus',title:'提现状态',width:80},
  {colKey:'expireTime',title:'过期时间',width:170},
  {colKey:'auditTime',title:'审核时间',width:170},
  {colKey:'createTime',title:'领取时间',width:170},
  {colKey:'operation',title:'操作',width:180,fixed:'right'},
]

function fmt(t){if(!t)return'';const d=new Date(t);return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`}

async function fetchOrders() {
  loading.value=true
  try{const p={page:pg.current,size:pg.pageSize};if(oStatus.value!=null)p.auditStatus=oStatus.value;const r=await request.get('/admin/tasks/orders',{params:p});orderData.value=r.records||[];pg.total=r.total||0}catch(e){}finally{loading.value=false}
}
function onPg(p){pg.current=p.current;fetchOrders()}

async function audit(row,status,grantPay){
  const note = status===3?'审核通过':(prompt('驳回原因:')||'')
  await request.put(`/admin/tasks/orders/${row.id}/audit`,{auditStatus:status,auditNote:note,grantPay:grantPay})
  MessagePlugin.success('已审核');fetchOrders()
}
async function grant(id){await request.put(`/admin/tasks/orders/${id}/grant`);MessagePlugin.success('已授权打款');fetchOrders()}

import { onMounted } from 'vue'
onMounted(fetchOrders)
</script>
