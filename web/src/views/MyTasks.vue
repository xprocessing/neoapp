<template>
  <div class="section-container py-8">
    <div class="max-w-[720px] mx-auto">
      <h1 class="text-2xl font-bold mb-6">我的任务</h1>
      <div v-if="orders.length===0&&!loading" class="text-center py-16 text-[var(--color-text-tertiary)]">暂无任务</div>
      <div v-else class="space-y-3">
        <div v-for="o in orders" :key="o.id" class="bg-white rounded-xl border border-gray-100 p-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm text-[var(--color-text-secondary)]">任务 #{{ o.taskId }}</span>
            <t-tag :theme="o.auditStatus===2?'warning':o.auditStatus===3?'success':o.auditStatus===4?'danger':'default'" size="small">{{ {1:'待提交',2:'待审核',3:'已通过',4:'已驳回',5:'已过期'}[o.auditStatus] }}</t-tag>
          </div>
          <p class="text-sm text-[var(--color-text-tertiary)]">奖励：<span class="text-orange-500 font-semibold">¥{{ o.rewardAmount }}</span></p>
          <p v-if="o.submitNote" class="text-xs text-[var(--color-text-tertiary)] mt-1">提交备注：{{ o.submitNote }}</p>
          <p v-if="o.auditNote" class="text-xs mt-1" :class="o.auditStatus===4?'text-red-500':'text-green-500'">审核备注：{{ o.auditNote }}</p>
          <div v-if="o.submitImages" class="flex gap-1 mt-2">
            <img v-for="(url,i) in o.submitImages.split(',')" :key="i" :src="url" class="w-12 h-12 rounded object-cover" />
          </div>
          <div class="flex gap-2 mt-3">
            <t-button v-if="o.auditStatus===1" size="small" theme="primary" @click="openSubmit(o)">提交任务</t-button>
            <t-button v-if="o.auditStatus===3&&o.grantPay===1&&o.withdrawStatus===0" size="small" theme="warning" @click="withdraw(o.id)">领取奖励</t-button>
            <t-tag v-if="o.withdrawStatus===1" size="small" theme="warning">提现处理中</t-tag>
            <t-tag v-if="o.withdrawStatus===2" size="small" theme="success">已到账</t-tag>
            <t-tag v-if="o.auditStatus===3&&o.grantPay===0" size="small">等待授权打款</t-tag>
          </div>
        </div>
      </div>
      <t-pagination v-if="total>pg.pageSize" class="mt-6 justify-center" v-model="pg.current" :total="total" :page-size="pg.pageSize" @change="fetchOrders" />
    </div>

    <!-- 提交抽屉 -->
    <t-drawer v-model:visible="subDrawer" header="提交任务" size="400px" :footer="false">
      <t-form label-align="top">
        <t-form-item label="订单号/备注"><t-input v-model="subNote" placeholder="请填写订单号或说明" /></t-form-item>
        <t-form-item label="上传截图（最多3张）">
          <div class="flex flex-wrap gap-2">
            <div v-for="(url,i) in subImgList" :key="i" class="relative w-20 h-20 rounded-lg overflow-hidden border">
              <img :src="url" class="w-full h-full object-cover" />
              <button class="absolute top-0 right-0 w-5 h-5 bg-black/50 text-white rounded-bl text-xs flex items-center justify-center" @click="subImgList.splice(i,1)">×</button>
            </div>
            <label v-if="subImgList.length<3" class="w-20 h-20 rounded-lg border border-dashed border-gray-300 flex items-center justify-center cursor-pointer hover:border-[var(--color-primary)] transition-colors">
              <Upload class="w-5 h-5 text-[var(--color-text-tertiary)]" />
              <input type="file" accept="image/*" class="hidden" @change="onSubImgUpload" />
            </label>
          </div>
        </t-form-item>
        <t-button block theme="primary" :loading="submitting" @click="doSubmit">提交审核</t-button>
      </t-form>
    </t-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { Upload } from 'lucide-vue-next'
import request from '@/utils/request'
const orders = ref([]); const loading = ref(false); const total = ref(0)
const pg = reactive({current:1,pageSize:10})
const subDrawer = ref(false); const subId = ref(null); const subNote = ref(''); const subImgList = ref([]); const submitting = ref(false)

async function fetchOrders() {
  loading.value=true
  try{const r=await request.get('/user/tasks/orders',{params:{page:pg.current,size:pg.pageSize}});orders.value=r.records||[];total.value=r.total||0}catch(e){}finally{loading.value=false}
}
function openSubmit(o){subId.value=o.id;subNote.value='';subImgList.value=[];subDrawer.value=true}

async function onSubImgUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const fd = new FormData(); fd.append('file', file)
  try { const url = await request.post('/user/upload/avatar', fd); subImgList.value.push(url) } catch {}
  e.target.value = ''
}

async function doSubmit(){
  if (!subNote.value) { MessagePlugin.warning('请填写订单号'); return }
  submitting.value=true
  try{
    await request.put(`/user/tasks/orders/${subId.value}/submit`,{submitNote:subNote.value,submitImages:subImgList.value.join(',')})
    subDrawer.value=false;MessagePlugin.success('已提交');fetchOrders()
  }catch(e){}finally{submitting.value=false}
}
async function withdraw(id){
  try{await request.post(`/user/tasks/orders/${id}/withdraw`);MessagePlugin.success('提现申请已提交');fetchOrders()}catch(e){}
}
onMounted(fetchOrders)
</script>
