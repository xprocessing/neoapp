<template>
  <div class="section-container py-8">
    <div class="max-w-[720px] mx-auto" v-if="task">
      <t-button variant="text" @click="$router.back()" class="mb-4"><ChevronLeft class="w-4 h-4 mr-1" />返回</t-button>
      <div class="bg-white rounded-2xl border border-gray-100 overflow-hidden">
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
          <t-button v-if="!claimed" block theme="primary" class="mt-6" :loading="claiming" @click="claim">立即领取</t-button>
          <p v-else class="mt-6 text-center text-sm text-green-500">已领取，请前往我的任务查看</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { ChevronLeft } from 'lucide-vue-next'
import request from '@/utils/request'
const route = useRoute(); const router = useRouter()
const task = ref(null); const claimed = ref(false); const claiming = ref(false)
async function fetchDetail() {
  try{task.value=await request.get(`/user/tasks/${route.params.id}`)}catch(e){router.back()}
}
async function claim() {
  claiming.value=true
  try{await request.post(`/user/tasks/${route.params.id}/claim`);claimed.value=true;MessagePlugin.success('领取成功')}catch(e){}finally{claiming.value=false}
}
onMounted(fetchDetail)
</script>

<style scoped>
.task-content :deep(img) { max-width:100%; border-radius:8px; margin:8px 0; }
.task-content :deep(p) { margin:4px 0; }
.task-content :deep(ul),.task-content :deep(ol) { padding-left:20px; }
</style>
