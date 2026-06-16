<template>
  <div class="section-container py-8">
    <div class="max-w-[800px] mx-auto">
      <h1 class="text-2xl font-bold mb-6">任务中心</h1>
      <div v-if="tasks.length===0&&!loading" class="text-center py-16 text-[var(--color-text-tertiary)]">暂无上架任务</div>
      <div v-else class="grid gap-4">
        <div v-for="t in tasks" :key="t.id" class="bg-white rounded-xl border border-gray-100 overflow-hidden hover:shadow transition-shadow">
          <div class="flex gap-4 p-4 cursor-pointer" @click="viewDetail(t)">
            <t-image :src="t.taskCover" fit="cover" class="w-[100px] h-[100px] rounded-lg flex-shrink-0 bg-gray-100" />
            <div class="flex-1 min-w-0">
              <h3 class="font-semibold text-[var(--color-text-primary)]">{{ t.taskTitle }}</h3>
              <p class="text-sm text-[var(--color-text-tertiary)] mt-1 line-clamp-2">{{ t.taskIntro }}</p>
              <div class="flex items-center gap-4 mt-3">
                <span class="text-orange-500 font-bold text-lg">¥{{ t.rewardAmount }}</span>
                <t-tag size="small" variant="light">{{ t.gift || '无赠品' }}</t-tag>
                <t-tag v-if="isClaimed(t.id)" size="small" theme="success" variant="light">已领取</t-tag>
              </div>
            </div>
          </div>
          <div class="flex border-t border-gray-50">
            <t-button variant="text" theme="primary" block class="!rounded-none" :loading="claimingIds.has(t.id)" @click.stop="doClaim(t)">
              {{ isClaimed(t.id) ? '查看详情' : '立即领取' }}
            </t-button>
          </div>
        </div>
      </div>
      <t-pagination v-if="total>pg.pageSize" class="mt-6 justify-center" v-model="pg.current" :total="total" :page-size="pg.pageSize" @change="fetchTasks" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import request from '@/utils/request'

const router = useRouter()
const tasks = ref([]); const loading = ref(false); const total = ref(0)
const pg = reactive({ current:1, pageSize:10 })
const claimedTaskIds = ref(new Set())
const claimingIds = ref(new Set())

async function fetchTasks() {
  loading.value=true
  try{const r=await request.get('/user/tasks',{params:{page:pg.current,size:pg.pageSize}});tasks.value=r.records||[];total.value=r.total||0}catch(e){}finally{loading.value=false}
}

async function fetchClaimed() {
  try {
    const r = await request.get('/user/tasks/orders', { params: { page:1, size:100 } })
    const ids = (r?.records || []).map(o => o.taskId)
    claimedTaskIds.value = new Set(ids)
  } catch {}
}

function isClaimed(taskId) { return claimedTaskIds.value.has(taskId) }

async function doClaim(task) {
  if (isClaimed(task.id)) { router.push(`/tasks/${task.id}`); return }
  if (!localStorage.getItem('token')) { router.push('/login'); return }
  claimingIds.value.add(task.id)
  try {
    await request.post(`/user/tasks/${task.id}/claim`)
    claimedTaskIds.value.add(task.id)
    MessagePlugin.success('领取成功')
    router.push(`/tasks/${task.id}`)
  } catch {} finally { claimingIds.value.delete(task.id) }
}

function viewDetail(task) {
  if (isClaimed(task.id)) router.push(`/tasks/${task.id}`)
  else {
    if (!localStorage.getItem('token')) router.push('/login')
    else MessagePlugin.info('请先领取任务')
  }
}

onMounted(() => { fetchTasks(); fetchClaimed() })
</script>
