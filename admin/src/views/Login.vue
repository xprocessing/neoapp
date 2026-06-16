<template>
  <div class="login-page min-h-screen flex">
    <!-- 左侧品牌区 -->
    <div class="hidden lg:flex w-[45%] bg-gradient-to-br from-[#0052D9] via-[#366EF4] to-[#618DFF] flex-col justify-center items-center text-white px-12 relative overflow-hidden">
      <div class="absolute inset-0 opacity-10">
        <div class="absolute top-20 left-10 w-64 h-64 bg-white rounded-full blur-3xl" />
        <div class="absolute bottom-20 right-10 w-96 h-96 bg-white rounded-full blur-3xl" />
      </div>
      <div class="relative z-10 text-center max-w-md">
        <div class="w-20 h-20 mx-auto mb-8 bg-white/20 backdrop-blur rounded-2xl flex items-center justify-center">
          <span class="text-4xl font-bold">N</span>
        </div>
        <h1 class="text-3xl font-bold mb-4">NeoApp 管理后台</h1>
        <p class="text-white/80 text-base leading-relaxed">
          高效管理，简洁操作。<br/>一站式数据管理平台，助力业务增长。
        </p>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="flex-1 flex items-center justify-center px-6 py-12 bg-[var(--color-bg)] sm:bg-white">
      <div class="w-full max-w-[400px]">
        <!-- Logo (移动端显示) -->
        <div class="lg:hidden text-center mb-10">
          <div class="w-14 h-14 mx-auto mb-4 bg-gradient-to-br from-[#0052D9] to-[#618DFF] rounded-2xl flex items-center justify-center">
            <span class="text-2xl font-bold text-white">N</span>
          </div>
          <h1 class="text-xl font-semibold text-[var(--color-text-primary)]">NeoApp 管理后台</h1>
        </div>

        <!-- 登录卡片 -->
        <div class="bg-white sm:border sm:border-gray-100 rounded-[var(--radius-card)] p-8 sm:shadow-[var(--shadow-card)]">
          <h2 class="text-lg font-semibold text-[var(--color-text-primary)] mb-6">账号登录</h2>

          <t-form
            ref="formRef"
            :data="formData"
            :rules="formRules"
            label-align="top"
            @submit="handleLogin"
          >
            <t-form-item label="用户名" name="username">
              <t-input
                v-model="formData.username"
                placeholder="请输入用户名"
                clearable
                size="large"
              >
                <template #prefix-icon>
                  <User class="w-4 h-4" />
                </template>
              </t-input>
            </t-form-item>

            <t-form-item label="密码" name="password">
              <t-input
                v-model="formData.password"
                type="password"
                placeholder="请输入密码"
                clearable
                size="large"
                @keyup.enter="handleLogin"
              >
                <template #prefix-icon>
                  <Lock class="w-4 h-4" />
                </template>
              </t-input>
            </t-form-item>

            <div class="flex items-center justify-between mb-6">
              <t-checkbox v-model="formData.remember">记住密码</t-checkbox>
              <a href="#" class="text-sm text-[var(--color-text-tertiary)] hover:text-[var(--color-primary)] transition-colors">忘记密码？</a>
            </div>

            <t-button
              theme="primary"
              type="submit"
              block
              size="large"
              :loading="loading"
              class="!rounded-[var(--radius-btn)] !h-11 !text-base !font-medium"
            >
              登 录
            </t-button>
          </t-form>
        </div>

        <p class="text-center text-xs text-[var(--color-text-tertiary)] mt-8">
          &copy; {{ new Date().getFullYear() }} NeoApp. All rights reserved.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { useUserStore } from '@/stores/user'
import { User, Lock } from 'lucide-vue-next'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  username: '',
  password: '',
  remember: false,
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
}

async function handleLogin({ validateResult }) {
  if (validateResult !== true) return
  loading.value = true
  try {
    const token = await request.post('/auth/admin/login', {
      username: formData.username,
      password: formData.password,
    })
    userStore.setToken(token)
    userStore.setUserInfo({ username: formData.username, nickname: '管理员' })
    MessagePlugin.success('登录成功')
    router.push('/dashboard')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>
