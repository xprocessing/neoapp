<template>
  <div class="section-container py-8">
    <div class="max-w-[720px] mx-auto" v-if="task">
      <t-button variant="text" @click="$router.back()" class="mb-4"><ChevronLeft class="w-4 h-4 mr-1" />返回</t-button>

      <!-- 未领取 -->
      <div v-if="!claimed&&!checking" class="bg-white rounded-2xl border border-gray-100 overflow-hidden">
        <t-image :src="task.taskCover" fit="cover" class="w-full h-[200px]" />
        <div class="p-6 text-center">
          <h1 class="text-xl font-bold">{{ task.taskTitle }}</h1>
          <div class="flex items-center justify-center gap-4 mt-3">
            <span class="text-orange-500 font-bold text-2xl">¥{{ task.rewardAmount }}</span>
            <t-tag v-if="task.gift" size="small">{{ task.gift }}</t-tag>
          </div>
          <p class="text-sm text-[var(--color-text-tertiary)] mt-3">{{ task.taskIntro }}</p>
          <t-button block theme="primary" class="mt-6" :loading="claiming" @click="claim">立即领取并查看详情</t-button>
        </div>
      </div>

      <!-- 已领取 -->
      <div v-else-if="claimed" class="bg-white rounded-2xl border border-gray-100 overflow-hidden">
        <t-image :src="task.taskCover" fit="cover" class="w-full h-[200px]" />
        <div class="p-6">
          <h1 class="text-xl font-bold">{{ task.taskTitle }}</h1>
          <div class="flex items-center gap-4 mt-3">
            <span class="text-orange-500 font-bold text-2xl">¥{{ task.rewardAmount }}</span>
            <t-tag v-if="task.gift" size="small">{{ task.gift }}</t-tag>
          </div>
          <p class="text-sm text-[var(--color-text-tertiary)] mt-3">{{ task.taskIntro }}</p>
          <div class="mt-4 p-4 bg-gray-50 rounded-lg text-sm task-content" v-html="task.taskContent || '暂无详细说明'" />
          <p class="text-xs text-[var(--color-text-tertiary)] mt-4">有效时长：{{ task.expireMinute }} 分钟</p>

          <!-- 待提交 -->
          <div v-if="order&&order.auditStatus===1" class="mt-6 border-t border-gray-100 pt-4">
            <h3 class="font-semibold mb-3">提交任务</h3>
            <t-form label-align="top">
              <t-form-item label="订单号">
                <t-input v-model="submitNote" placeholder="请输入订单号或说明" />
              </t-form-item>
              <t-form-item label="上传截图（最多3张）">
                <div class="flex flex-wrap gap-2">
                  <div v-for="(url,i) in uploadUrls" :key="i" class="relative w-20 h-20 rounded-lg overflow-hidden border">
                    <img :src="url" class="w-full h-full object-cover" />
                    <button class="absolute top-0 right-0 w-5 h-5 bg-black/50 text-white rounded-bl text-xs flex items-center justify-center" @click="removeImage(i)">×</button>
                  </div>
                  <label v-if="uploadUrls.length<3" class="w-20 h-20 rounded-lg border border-dashed border-gray-300 flex items-center justify-center cursor-pointer hover:border-[var(--color-primary)] transition-colors">
                    <Upload class="w-5 h-5 text-[var(--color-text-tertiary)]" />
                    <input type="file" accept="image/*" class="hidden" @change="onImageUpload" />
                  </label>
                </div>
              </t-form-item>
              <t-button block theme="primary" :loading="submitting" @click="doSubmit">提交审核</t-button>
            </t-form>
          </div>

          <!-- 已提交 -->
          <div v-if="order&&order.auditStatus===2" class="mt-6 border-t border-gray-100 pt-4">
            <h3 class="font-semibold text-orange-500 mb-2">审核中</h3>
            <p class="text-sm text-[var(--color-text-tertiary)]">订单号：{{ order.submitNote }}</p>
            <div v-if="order.submitImages" class="flex gap-2 mt-2">
              <img v-for="(url,i) in order.submitImages.split(',')" :key="i" :src="url" class="w-16 h-16 rounded object-cover" />
            </div>
          </div>

          <!-- 已通过 -->
          <div v-if="order&&order.auditStatus===3" class="mt-6 border-t border-gray-100 pt-4">
            <div class="flex items-center gap-2"><CheckCircle class="w-5 h-5 text-green-500" /><span class="font-semibold text-green-500">审核通过</span></div>
            <p class="text-sm text-[var(--color-text-tertiary)] mt-1">订单号：{{ order.submitNote }}</p>
            <div v-if="order.submitImages" class="flex gap-2 mt-2">
              <img v-for="(url,i) in order.submitImages.split(',')" :key="i" :src="url" class="w-16 h-16 rounded object-cover" />
            </div>
            <t-button v-if="order.grantPay===1&&order.withdrawStatus===0" block theme="warning" class="mt-4" :loading="withdrawing" @click="doWithdraw">领取奖励 ¥{{ order.rewardAmount }}</t-button>
            <t-tag v-else-if="order.withdrawStatus===1" theme="warning" class="mt-2">提现处理中</t-tag>
            <t-tag v-else-if="order.withdrawStatus===2" theme="success" class="mt-2">已到账</t-tag>
            <t-tag v-else-if="order.grantPay===0" class="mt-2">等待授权打款</t-tag>
          </div>

          <!-- 已驳回 -->
          <div v-if="order&&order.auditStatus===4" class="mt-6 border-t border-gray-100 pt-4">
            <div class="flex items-center gap-2"><XCircle class="w-5 h-5 text-red-500" /><span class="font-semibold text-red-500">已驳回</span></div>
            <p class="text-sm text-red-500 mt-1">{{ order.auditNote }}</p>
          </div>

          <!-- 已过期 -->
          <div v-if="order&&order.auditStatus===5" class="mt-6 border-t border-gray-100 pt-4 text-center text-sm text-[var(--color-text-tertiary)]">任务已过期</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { ChevronLeft, Upload, CheckCircle, XCircle } from 'lucide-vue-next'
import request from '@/utils/request'
const route = useRoute(); const router = useRouter()
const task = ref(null); const order = ref(null); const claimed = ref(false)
const claiming = ref(false); const checking = ref(true)
const submitNote = ref(''); const uploadUrls = ref([]); const submitting = ref(false); const withdrawing = ref(false)

async function fetchDetail() {
  try { task.value = await request.get(`/user/tasks/${route.params.id}`) } catch(e) { router.back(); return }
  try {
    const r = await request.get('/user/tasks/orders', { params: { page:1, size:100 } })
    const orders = r?.records || []
    const match = orders.find(o => o.taskId === task.value.id)
    if (match) { order.value = match; claimed.value = true }
  } catch {} finally { checking.value = false }
}

async function claim() {
  claiming.value=true
  try {
    const r = await request.post(`/user/tasks/${route.params.id}/claim`)
    order.value = r; claimed.value = true; MessagePlugin.success('领取成功')
  } catch {} finally { claiming.value=false }
}

async function onImageUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const fd = new FormData(); fd.append('file', file)
  try {
    const url = await request.post('/user/upload/avatar', fd)
    uploadUrls.value.push(url)
  } catch {} finally { e.target.value = '' }
}
function removeImage(i) { uploadUrls.value.splice(i, 1) }

async function doSubmit() {
  if (!submitNote.value) { MessagePlugin.warning('请填写订单号'); return }
  submitting.value=true
  try {
    await request.put(`/user/tasks/orders/${order.value.id}/submit`, {
      submitNote: submitNote.value,
      submitImages: uploadUrls.value.join(','),
    })
    MessagePlugin.success('已提交')
    order.value.auditStatus = 2
    order.value.submitNote = submitNote.value
    order.value.submitImages = uploadUrls.value.join(',')
  } catch {} finally { submitting.value=false }
}

async function doWithdraw() {
  withdrawing.value=true
  try {
    await request.post(`/user/tasks/orders/${order.value.id}/withdraw`)
    order.value.withdrawStatus = 1
    MessagePlugin.success('提现申请已提交')
  } catch {} finally { withdrawing.value=false }
}

onMounted(fetchDetail)
</script>

<style scoped>
.task-content :deep(img) { max-width:100%; border-radius:8px; margin:8px 0; }
.task-content :deep(p) { margin:4px 0; }
.task-content :deep(ul),.task-content :deep(ol) { padding-left:20px; }
</style>
