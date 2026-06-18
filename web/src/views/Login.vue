<template>
  <div class="auth-page min-h-[calc(100vh-var(--header-height))] flex items-center justify-center py-12 px-4">
    <div class="w-full max-w-[400px]">
      <!-- 标题 -->
      <div class="text-center mb-8">
        <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-[#0052D9] to-[#618DFF] flex items-center justify-center text-white font-bold text-xl mx-auto mb-4">
          N
        </div>
        <h1 class="text-2xl font-bold text-[var(--color-text-primary)]">欢迎回来</h1>
        <p class="text-sm text-[var(--color-text-tertiary)] mt-2">登录您的 NeoApp 账号</p>
      </div>

      <!-- 表单 -->
      <div class="bg-white rounded-2xl p-8 border border-gray-100 shadow-sm">
        <t-form ref="formRef" :data="form" :rules="rules" label-align="top" @submit="handleLogin">
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
              placeholder="请输入密码"
              size="large"
            >
              <template #prefix-icon><Lock class="w-4 h-4" /></template>
            </t-input>
          </t-form-item>

          <t-button
            type="submit"
            theme="primary"
            block
            size="large"
            :loading="loading"
            class="mt-2"
          >
            登 录
          </t-button>
        </t-form>

        <div class="flex items-center gap-2 my-4"><div class="flex-1 h-px bg-gray-100" /><span class="text-xs text-[var(--color-text-tertiary)]">或</span><div class="flex-1 h-px bg-gray-100" /></div>
        <t-button block variant="outline" size="large" @click="openWechatQR" :loading="wechatLoading" class="!border-green-500 !text-green-500">微信登录</t-button>

        <div v-if="wxNewUser" class="mt-4 p-4 bg-gray-50 rounded-lg">
          <p class="text-sm mb-2">微信授权成功，请补充手机号和密码完成注册</p>
          <t-input v-model="form.phone" placeholder="手机号" maxlength="11" class="mb-2" size="large" />
          <t-input v-model="form.password" type="password" placeholder="密码(6-32位)" class="mb-2" size="large" />
          <t-button theme="primary" block :loading="loading" @click="completeWxRegister">完成注册</t-button>
        </div>

        <div class="text-center mt-6">
          <span class="text-sm text-[var(--color-text-tertiary)]">还没有账号？</span>
          <router-link to="/register" class="text-sm text-[var(--color-primary)] hover:underline ml-1">
            立即注册
          </router-link>
        </div>
      </div>
    </div>

    <!-- QR Code Dialog -->
    <t-dialog v-model:visible="qrVisible" header="微信扫码登录" :footer="false" width="360px" :close-on-overlay-click="false">
      <div class="text-center py-4">
        <div v-if="qrStatus === 'pending'">
          <canvas ref="qrCanvas" class="mx-auto border border-gray-100 rounded-lg"></canvas>
          <p class="text-sm text-[var(--color-text-secondary)] mt-4">请使用微信扫描二维码</p>
          <t-loading size="small" class="mt-2" />
          <p class="text-xs text-[var(--color-text-tertiary)] mt-1">等待扫码...</p>
        </div>
        <div v-else-if="qrStatus === 'scanned'">
          <div class="text-green-500 text-5xl mb-3">✓</div>
          <p class="text-base font-semibold">扫码成功</p>
          <p class="text-sm text-[var(--color-text-secondary)] mt-1" v-if="qrNickname">{{ qrNickname }}</p>
          <t-loading size="small" class="mt-3" />
          <p class="text-xs text-[var(--color-text-tertiary)]">正在登录...</p>
        </div>
        <div v-else-if="qrStatus === 'expired'">
          <div class="text-gray-300 text-5xl mb-3">⏱</div>
          <p class="text-base text-[var(--color-text-secondary)]">二维码已过期</p>
          <t-button variant="outline" class="mt-4" @click="refreshQR">重新获取</t-button>
        </div>
        <div v-else-if="qrStatus === 'newUser'">
          <div class="text-blue-400 text-5xl mb-3">✦</div>
          <p class="text-base font-semibold">首次登录</p>
          <p class="text-sm text-[var(--color-text-secondary)] mt-1" v-if="qrNickname">{{ qrNickname }}</p>
          <p class="text-xs text-[var(--color-text-tertiary)] mt-2">请填写手机号和密码完成注册</p>
          <t-input v-model="form.phone" placeholder="手机号" maxlength="11" class="mt-3" size="large" />
          <t-input v-model="form.password" type="password" placeholder="密码(6-32位)" class="mt-2" size="large" />
          <t-button theme="primary" block class="mt-3" :loading="loading" @click="completeQRRegister">完成注册</t-button>
        </div>
        <div v-else-if="qrStatus === 'error'">
          <div class="text-red-400 text-5xl mb-3">✗</div>
          <p class="text-base text-[var(--color-text-secondary)]">获取二维码失败</p>
          <t-button variant="outline" class="mt-4" @click="refreshQR">重试</t-button>
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { Phone, Lock } from 'lucide-vue-next'
import QRCode from 'qrcode'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const wechatLoading = ref(false)
const wxNewUser = ref(false)
const wxOpenid = ref('')
const wxNickname = ref('')

// QR code state
const qrVisible = ref(false)
const qrStatus = ref('pending')
const qrState = ref('')
const qrNickname = ref('')
const qrCanvas = ref(null)
let qrTimer = null

const form = reactive({ phone: '', password: '' })

const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin({ validateResult }) {
  if (validateResult !== true) return
  loading.value = true
  try {
    const token = await request.post('/auth/login', { phone: form.phone, password: form.password })
    localStorage.setItem('token', token)
    MessagePlugin.success('登录成功')
    router.push('/profile')
  } catch {} finally { loading.value = false }
}

// --- QR Code WeChat Login ---
const isMobile = /Android|iPhone|iPad|iPod|HarmonyOS|Mobile/i.test(navigator.userAgent)

async function openWechatQR() {
  if (isMobile) {
    // 手机端直接跳转微信
    wechatLoading.value = true
    try {
      const { url } = await request.get('/auth/wechat/url')
      window.location.href = url
    } catch {} finally { wechatLoading.value = false }
    return
  }
  // PC端弹二维码
  wechatLoading.value = true
  qrStatus.value = 'pending'
  qrVisible.value = true
  await generateQRCode()
  wechatLoading.value = false
}

async function generateQRCode() {
  try {
    const { state, qrUrl } = await request.post('/auth/wechat/qrcode/generate')
    qrState.value = state
    await QRCode.toCanvas(qrCanvas.value, qrUrl, { width: 220, margin: 1 })
    qrStatus.value = 'pending'
    startPolling()
  } catch {
    qrStatus.value = 'error'
  }
}

function startPolling() {
  stopPolling()
  qrTimer = setInterval(async () => {
    try {
      if (!qrState.value) return
      const res = await request.get('/auth/wechat/qrcode/status', { params: { state: qrState.value } })
      if (res.status === 'scanned') {
        qrStatus.value = 'scanned'
        qrNickname.value = res.nickname || ''
        stopPolling()
        // 自动调用登录
        await doQRLogin()
      } else if (res.status === 'expired') {
        qrStatus.value = 'expired'
        stopPolling()
      }
    } catch { /* keep polling */ }
  }, 2000)
}

function stopPolling() {
  if (qrTimer) { clearInterval(qrTimer); qrTimer = null }
}

async function doQRLogin() {
  try {
    const res = await request.post('/auth/wechat/qrcode/login', { state: qrState.value })
    if (res.bound) {
      localStorage.setItem('token', res.token)
      MessagePlugin.success('登录成功')
      qrVisible.value = false
      router.push('/profile')
    } else {
      qrStatus.value = 'newUser'
      qrNickname.value = res.nickname || ''
      wxOpenid.value = res.openid || ''
      form.phone = ''
      form.password = ''
    }
  } catch {
    qrStatus.value = 'error'
  }
}

async function completeQRRegister() {
  if (!form.phone || !form.password) { MessagePlugin.warning('请填写手机号和密码'); return }
  loading.value = true
  try {
    const res = await request.post('/auth/wechat/qrcode/register', {
      state: qrState.value,
      phone: form.phone,
      password: form.password,
      nickname: qrNickname.value,
    })
    if (res.bound) {
      localStorage.setItem('token', res.token)
      MessagePlugin.success('注册成功')
      qrVisible.value = false
      router.push('/profile')
    }
  } catch {} finally { loading.value = false }
}

function refreshQR() {
  qrStatus.value = 'pending'
  generateQRCode()
}

// Old mobile redirect callback
async function handleWechatCode(code) {
  try {
    const res = await request.post('/auth/wechat/login', { code })
    if (res.bound) {
      localStorage.setItem('token', res.token)
      MessagePlugin.success('登录成功')
      router.push('/profile')
    } else {
      wxOpenid.value = res.openid
      wxNickname.value = res.nickname || ''
      wxNewUser.value = true
      form.phone = ''
      form.password = ''
    }
  } catch {}
}

async function completeWxRegister() {
  if (!form.phone || !form.password) { MessagePlugin.warning('请填写手机号和密码'); return }
  loading.value = true
  try {
    const res = await request.post('/auth/wechat/login', {
      code: route.query.code,
      phone: form.phone,
      password: form.password,
      nickname: wxNickname.value,
    })
    if (res.bound) {
      localStorage.setItem('token', res.token)
      MessagePlugin.success('注册成功')
      router.push('/profile')
    }
  } catch {} finally { loading.value = false }
}

onMounted(() => {
  const code = route.query.code
  if (code) handleWechatCode(code)
})

onUnmounted(() => {
  stopPolling()
})
</script>
