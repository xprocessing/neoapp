<template>
  <div class="section-container py-8">
    <div class="max-w-[800px] mx-auto">
      <h1 class="text-2xl font-bold mb-2">会员中心</h1>
      <div class="flex items-center gap-3 mb-6">
        <t-tag v-if="myMember.isMember" theme="success" size="medium">会员</t-tag>
        <t-tag v-else size="medium">非会员</t-tag>
        <span v-if="myMember.expireTime" class="text-sm text-[var(--color-text-tertiary)]">到期：{{ fmt(myMember.expireTime) }}</span>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div v-for="p in packages" :key="p.id" class="bg-white rounded-xl border border-gray-100 p-6 hover:shadow-lg transition-shadow">
          <h3 class="text-lg font-bold">{{ p.packageName }}</h3>
          <div class="mt-2"><span class="text-3xl font-bold text-orange-500">¥{{ p.price }}</span></div>
          <div class="mt-3 space-y-1 text-sm text-[var(--color-text-secondary)]">
            <p>会员时长：{{ p.dayNum }} 天</p>
            <p v-if="p.givePoint>0">赠送积分：+{{ p.givePoint }}</p>
          </div>
          <t-button block theme="primary" class="mt-4" :loading="buying" @click="buy(p.id)">立即购买</t-button>
        </div>
      </div>
      <div v-if="!packages.length" class="text-center py-16 text-[var(--color-text-tertiary)]">暂无可用套餐</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import request from '@/utils/request'

const router = useRouter()
const packages = ref([]); const buying = ref(false)
const myMember = reactive({ isMember: false, expireTime: null })

function fmt(t) { if(!t) return ''; const d=new Date(t); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}` }

async function fetchData() {
  try { packages.value = await request.get('/user/member/packages') } catch {}
  try { const r = await request.get('/user/member/info'); Object.assign(myMember, r) } catch {}
}

async function buy(id) {
  if (!localStorage.getItem('token')) { router.push('/login'); return }
  buying.value=true
  try {
    const r = await request.post(`/user/member/buy/${id}`)
    if (r.h5_url) {
      // 微信支付：跳转 H5 支付页面
      window.location.href = r.h5_url
    } else if (r.expireTime) {
      // 无微信支付：直接开通
      myMember.isMember = true; myMember.expireTime = r.expireTime
      MessagePlugin.success(`购买成功！${r.givePoint>0?'赠送'+r.givePoint+'积分':''}`)
    }
  } catch {} finally { buying.value=false }
}

onMounted(fetchData)
</script>
