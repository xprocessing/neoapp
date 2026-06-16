<template>
  <div class="auth-page min-h-[calc(100vh-var(--header-height))] flex items-center justify-center py-12 px-4">
    <div class="w-full max-w-[400px]">
      <!-- 标题 -->
      <div class="text-center mb-8">
        <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-[#0052D9] to-[#618DFF] flex items-center justify-center text-white font-bold text-xl mx-auto mb-4">
          N
        </div>
        <h1 class="text-2xl font-bold text-[var(--color-text-primary)]">创建账号</h1>
        <p class="text-sm text-[var(--color-text-tertiary)] mt-2">注册成为 NeoApp 用户</p>
      </div>

      <!-- 表单 -->
      <div class="bg-white rounded-2xl p-8 border border-gray-100 shadow-sm">
        <t-form ref="formRef" :data="form" :rules="rules" label-align="top" @submit="handleRegister">
          <t-form-item label="手机号" name="phone">
            <t-input
              v-model="form.phone"
              placeholder="请输入手机号"
              maxlength="11"
              size="large"
            >
              <template #prefix-icon><Phone class="w-4 h-4" /></template>
            </t-input>
          </t-form-item>

          <t-form-item label="密码" name="password">
            <t-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（6-32位）"
              size="large"
            >
              <template #prefix-icon><Lock class="w-4 h-4" /></template>
            </t-input>
          </t-form-item>

          <t-form-item label="昵称" name="nickname">
            <t-input
              v-model="form.nickname"
              placeholder="给自己起个好听的名字"
              size="large"
            >
              <template #prefix-icon><User class="w-4 h-4" /></template>
            </t-input>
          </t-form-item>

          <t-form-item label="邀请码 (选填)" name="inviteCode">
            <t-input
              v-model="form.inviteCode"
              placeholder="如有邀请码可填写"
              size="large"
            />
          </t-form-item>

          <t-button
            type="submit"
            theme="primary"
            block
            size="large"
            :loading="loading"
            class="mt-2"
          >
            注 册
          </t-button>
        </t-form>

        <div class="text-center mt-6">
          <span class="text-sm text-[var(--color-text-tertiary)]">已有账号？</span>
          <router-link to="/login" class="text-sm text-[var(--color-primary)] hover:underline ml-1">
            立即登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { Phone, Lock, User } from 'lucide-vue-next'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  phone: '',
  password: '',
  nickname: '',
  inviteCode: '',
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
}

onMounted(() => {
  const invite = route.query.invite
  if (invite) form.inviteCode = invite
})

async function handleRegister({ validateResult }) {
  if (validateResult !== true) return
  loading.value = true
  try {
    await request.post('/auth/register', {
      phone: form.phone,
      password: form.password,
      nickname: form.nickname || undefined,
      inviteCode: form.inviteCode || undefined,
    })
    MessagePlugin.success('注册成功，请登录')

    // 自动登录
    const token = await request.post('/auth/login', {
      phone: form.phone,
      password: form.password,
    })
    localStorage.setItem('token', token)
    router.push('/profile')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>
