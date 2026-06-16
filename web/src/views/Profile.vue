<template>
  <div class="profile-page">
    <div class="section-container py-12">
      <div class="max-w-[640px] mx-auto">
        <!-- Page Title -->
        <div class="flex items-center justify-between mb-8">
          <h1 class="text-2xl font-bold text-[var(--color-text-primary)]">个人中心</h1>
          <t-button
            v-if="!editing"
            variant="outline"
            theme="primary"
            size="small"
            @click="startEdit"
          >
            <Edit3 class="w-4 h-4 mr-1" />编辑资料
          </t-button>
        </div>

        <!-- Profile Card -->
        <div
          class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden transition-all duration-300"
        >
          <!-- Header Section -->
          <div class="relative bg-gradient-to-br from-[#0052D9]/8 via-[#618DFF]/5 to-[#0052D9]/3 px-8 py-10">
            <!-- Avatar Area -->
            <div class="flex flex-col items-center">
              <div
                class="relative group cursor-pointer"
                @click="editing && handleAvatarClick()"
              >
                <!-- Avatar -->
                <div class="relative">
                  <t-avatar
                    size="88px"
                    :image="form.avatar || user.avatar"
                    class="ring-4 ring-white/80 shadow-lg transition-transform duration-300 group-hover:scale-105"
                  >
                    {{ user.nickname?.charAt(0) || 'U' }}
                  </t-avatar>

                  <!-- Upload Loading Overlay -->
                  <div
                    v-if="uploading"
                    class="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 z-10"
                  >
                    <t-loading size="medium" />
                  </div>

                  <!-- Edit Overlay -->
                  <div
                    v-if="editing && !uploading"
                    class="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity duration-200 cursor-pointer"
                  >
                    <Camera class="w-5 h-5 text-white" />
                  </div>
                </div>

                <!-- Hidden file input -->
                <input
                  ref="fileInput"
                  type="file"
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  class="hidden"
                  @change="handleFileChange"
                />
              </div>

              <!-- Edit hint -->
              <p
                v-if="editing && !uploading"
                class="text-xs text-[var(--color-text-tertiary)] mt-2 transition-opacity"
              >
                点击头像更换图片
              </p>

              <!-- User Name & Status -->
              <h2 class="text-xl font-semibold text-[var(--color-text-primary)] mt-4">
                {{ user.nickname || '-' }}
              </h2>
              <p class="text-xs text-[var(--color-text-tertiary)] mt-1">ID: {{ user.id }}</p>
              <t-tag
                :theme="user.status === 1 ? 'success' : 'danger'"
                variant="light"
                size="small"
                class="mt-2"
              >
                {{ user.status === 1 ? '正常' : '已禁用' }}
              </t-tag>
            </div>
          </div>

          <!-- Content Section -->
          <div class="px-8 py-6">
            <!-- View Mode: Details List -->
            <div v-if="!editing" class="divide-y divide-gray-50">
              <div class="flex items-center justify-between py-4">
                <span class="text-sm text-[var(--color-text-tertiary)]">手机号</span>
                <span class="text-sm text-[var(--color-text-primary)] font-medium">{{ user.phone || '-' }}</span>
              </div>
              <div class="flex items-center justify-between py-4">
                <span class="text-sm text-[var(--color-text-tertiary)]">性别</span>
                <span class="text-sm text-[var(--color-text-primary)]">
                  {{ sexLabel(user.sex) }}
                </span>
              </div>
              <div class="flex items-center justify-between py-4">
                <span class="text-sm text-[var(--color-text-tertiary)]">邀请码</span>
                <span class="text-sm text-[var(--color-text-primary)] font-mono bg-gray-50 px-2 py-1 rounded">
                  {{ user.inviteCode || '-' }}
                </span>
              </div>
              <div class="flex items-center justify-between py-4">
                <span class="text-sm text-[var(--color-text-tertiary)]">已邀请人数</span>
                <span class="text-sm font-semibold text-[var(--color-primary)]">{{ inviteNum }}</span>
              </div>
              <div class="py-4">
                <span class="text-sm text-[var(--color-text-tertiary)] block mb-2">邀请链接</span>
                <div class="flex gap-2">
                  <t-input :value="inviteUrl" readonly size="small" class="flex-1" />
                  <t-button
                    size="small"
                    theme="primary"
                    variant="outline"
                    @click="copyInviteUrl"
                  >
                    <Copy v-if="!copied" class="w-4 h-4" />
                    <Check v-else class="w-4 h-4" />
                    {{ copied ? '已复制' : '' }}
                  </t-button>
                </div>
              </div>
              <div class="flex items-center justify-between py-4">
                <span class="text-sm text-[var(--color-text-tertiary)]">注册时间</span>
                <span class="text-sm text-[var(--color-text-primary)]">
                  {{ formatTime(user.createTime) }}
                </span>
              </div>
            </div>

            <!-- Edit Mode: Form -->
            <div v-else>
              <t-form
                ref="formRef"
                :data="form"
                :rules="formRules"
                label-align="top"
                class="space-y-1"
              >
                <t-form-item label="昵称" name="nickname">
                  <t-input
                    v-model="form.nickname"
                    placeholder="请输入昵称"
                    :maxlength="50"
                    show-limit-number
                  />
                </t-form-item>

                <t-form-item label="性别" name="sex">
                  <t-radio-group v-model="form.sex">
                    <t-radio :value="0">未知</t-radio>
                    <t-radio :value="1">男</t-radio>
                    <t-radio :value="2">女</t-radio>
                  </t-radio-group>
                </t-form-item>
              </t-form>

              <!-- Action Buttons -->
              <div class="flex gap-3 mt-6 pt-4 border-t border-gray-50">
                <t-button
                  variant="outline"
                  class="flex-1"
                  @click="cancelEdit"
                >
                  <X class="w-4 h-4 mr-1" />取消
                </t-button>
                <t-button
                  theme="primary"
                  class="flex-1"
                  :loading="saving"
                  @click="handleSave"
                >
                  <Check class="w-4 h-4 mr-1" />保存
                </t-button>
              </div>
            </div>
          </div>
        </div>

        <!-- Points -->
        <div class="mt-6 bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div class="px-8 py-5 border-b border-gray-50 flex items-center justify-between">
            <h2 class="text-base font-semibold text-[var(--color-text-primary)]">我的积分</h2>
            <t-button size="small" variant="outline" @click="showPointLogs = !showPointLogs">
              {{ showPointLogs ? '收起' : '积分明细' }}
            </t-button>
          </div>
          <div class="px-8 py-6 flex items-center gap-8">
            <div class="text-center">
              <p class="text-3xl font-bold text-[var(--color-primary)]">{{ pointData.usablePoint ?? 0 }}</p>
              <p class="text-xs text-[var(--color-text-tertiary)] mt-1">可用积分</p>
            </div>
            <div class="text-center">
              <p class="text-xl font-semibold text-[var(--color-text-primary)]">{{ pointData.totalPoint ?? 0 }}</p>
              <p class="text-xs text-[var(--color-text-tertiary)] mt-1">累计积分</p>
            </div>
          </div>
          <!-- Point Logs -->
          <div v-if="showPointLogs" class="border-t border-gray-50 divide-y divide-gray-50 max-h-[360px] overflow-y-auto">
            <div v-if="pointLogs.length === 0" class="px-8 py-8 text-center text-sm text-[var(--color-text-tertiary)]">
              暂无积分记录
            </div>
            <div v-for="log in pointLogs" :key="log.id" class="px-8 py-3 flex items-center justify-between">
              <div>
                <p class="text-sm text-[var(--color-text-primary)]">{{ log.remark || log.source || '积分变动' }}</p>
                <p class="text-xs text-[var(--color-text-tertiary)]">{{ formatTime(log.createTime) }}</p>
              </div>
              <div class="text-right">
                <p :class="log.type === 1 ? 'text-green-500' : 'text-red-500'" class="text-sm font-semibold">
                  {{ log.type === 1 ? '+' : '' }}{{ log.point }}
                </p>
                <p class="text-xs text-[var(--color-text-tertiary)]">余额 {{ log.balance }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Address Management -->
        <div class="mt-6 bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div class="flex items-center justify-between px-8 py-5 border-b border-gray-50">
            <h2 class="text-base font-semibold text-[var(--color-text-primary)]">收货地址</h2>
            <t-button size="small" theme="primary" @click="openAddressCreate">
              <Plus class="w-4 h-4 mr-1" />新增地址
            </t-button>
          </div>

          <!-- Empty State -->
          <div v-if="addresses.length === 0 && !addressLoading" class="px-8 py-12 text-center">
            <MapPin class="w-10 h-10 text-gray-200 mx-auto mb-3" />
            <p class="text-sm text-[var(--color-text-tertiary)]">暂无收货地址</p>
          </div>

          <!-- Address List -->
          <div v-else class="divide-y divide-gray-50">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              class="px-8 py-4 flex items-start justify-between gap-4 hover:bg-gray-50/50 transition-colors"
            >
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <span class="text-sm font-medium text-[var(--color-text-primary)]">{{ addr.name }}</span>
                  <span class="text-sm text-[var(--color-text-secondary)]">{{ addr.phone }}</span>
                  <t-tag
                    v-if="addr.isDefault === 1"
                    theme="primary"
                    variant="light"
                    size="small"
                  >
                    默认
                  </t-tag>
                </div>
                <p class="text-sm text-[var(--color-text-tertiary)] truncate">
                  {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
                </p>
              </div>
              <div class="flex items-center gap-1 flex-shrink-0">
                <t-button
                  v-if="addr.isDefault !== 1"
                  variant="text"
                  size="small"
                  @click="handleSetDefault(addr.id)"
                >
                  设为默认
                </t-button>
                <t-button variant="text" theme="primary" size="small" @click="openAddressEdit(addr)">
                  编辑
                </t-button>
                <t-popconfirm content="确定删除该地址吗？" @confirm="handleAddressDelete(addr.id)">
                  <t-button variant="text" theme="danger" size="small">删除</t-button>
                </t-popconfirm>
              </div>
            </div>
          </div>

          <div v-if="addressLoading" class="flex justify-center py-8">
            <t-loading size="small" />
          </div>
        </div>

        <!-- Address Edit Drawer -->
        <t-drawer
          v-model:visible="addressDrawer"
          :header="addressIsEdit ? '编辑地址' : '新增地址'"
          :footer="false"
          size="400px"
        >
          <t-form ref="addrFormRef" :data="addressForm" :rules="addrRules" label-align="top">
            <t-form-item label="收货人" name="name">
              <t-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
            </t-form-item>
            <t-form-item label="手机号" name="phone">
              <t-input v-model="addressForm.phone" placeholder="请输入收货人电话" />
            </t-form-item>
            <t-form-item label="所在地区" name="district">
              <div class="flex gap-2">
                <t-input v-model="addressForm.province" placeholder="省份" class="flex-1" />
                <t-input v-model="addressForm.city" placeholder="城市" class="flex-1" />
                <t-input v-model="addressForm.district" placeholder="区县" class="flex-1" />
              </div>
            </t-form-item>
            <t-form-item label="详细地址" name="detail">
              <t-textarea v-model="addressForm.detail" placeholder="街道、门牌号等" :maxlength="100" />
            </t-form-item>
            <t-form-item label="默认地址" name="isDefault">
              <t-switch v-model="addressForm.isDefault" :custom-value="[1, 0]" />
            </t-form-item>
            <t-button block theme="primary" :loading="addressSaving" @click="handleAddressSave">
              保存
            </t-button>
          </t-form>
        </t-drawer>

        <!-- Wechat Bind -->
        <div class="mt-6 flex justify-center">
          <t-button variant="outline" class="!border-green-500 !text-green-500" @click="bindWechat" :loading="wxBinding">
            <svg class="w-4 h-4 mr-1" viewBox="0 0 24 24"><path fill="currentColor" d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18z"/></svg>绑定微信
          </t-button>
        </div>

        <!-- Logout Button -->
        <div class="mt-3 flex justify-center">
          <t-button variant="outline" theme="danger" @click="handleLogout">
            <LogOut class="w-4 h-4 mr-1" />退出登录
          </t-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { LogOut, Edit3, Camera, Check, X, Plus, MapPin, Copy } from 'lucide-vue-next'
import request from '@/utils/request'

const router = useRouter()

// --- State ---
const user = ref({})
const editing = ref(false)
const saving = ref(false)
const uploading = ref(false)
const fileInput = ref(null)
const formRef = ref(null)

const form = reactive({
  nickname: '',
  sex: 0,
  avatar: ''
})

const formRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '昵称不能超过50个字符', trigger: 'blur' }
  ]
}

// --- Point State ---
const pointData = reactive({ usablePoint: 0, totalPoint: 0 })
const pointLogs = ref([])
const showPointLogs = ref(false)

async function fetchPoints() {
  try {
    const data = await request.get('/user/points')
    if (data) {
      pointData.usablePoint = data.usablePoint ?? 0
      pointData.totalPoint = data.totalPoint ?? 0
    }
  } catch { /* ignore */ }
}

async function fetchPointLogs() {
  try {
    const res = await request.get('/user/points/logs', { params: { page: 1, size: 50 } })
    pointLogs.value = res?.records || []
  } catch { /* ignore */ }
}

// --- Invite ---
const inviteNum = ref(0)
const inviteCode = ref('')
const copied = ref(false)
const inviteUrl = ref('')

async function fetchInviteInfo() {
  try {
    const data = await request.get('/user/invites')
    inviteNum.value = data?.inviteNum ?? 0
    inviteCode.value = data?.inviteCode ?? ''
    inviteUrl.value = inviteCode.value ? `${window.location.origin}/register?invite=${inviteCode.value}` : ''
  } catch { /* ignore */ }
}

async function copyInviteUrl() {
  try {
    await navigator.clipboard.writeText(inviteUrl.value)
    copied.value = true
    MessagePlugin.success('邀请链接已复制')
    setTimeout(() => copied.value = false, 2000)
  } catch {
    MessagePlugin.warning('复制失败，请手动复制')
  }
}

import { watch } from 'vue'
watch(showPointLogs, (val) => { if (val && pointLogs.value.length === 0) fetchPointLogs() })

// --- Address State ---
const addresses = ref([])
const addressLoading = ref(false)
const addressSaving = ref(false)
const addressDrawer = ref(false)
const addressIsEdit = ref(false)
const addressEditId = ref(null)
const addrFormRef = ref(null)

const addressForm = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0,
})

const addrRules = {
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

function initAddressForm() {
  addressForm.name = ''
  addressForm.phone = ''
  addressForm.province = ''
  addressForm.city = ''
  addressForm.district = ''
  addressForm.detail = ''
  addressForm.isDefault = 0
  addressEditId.value = null
}

// --- Helpers ---
function sexLabel(sex) {
  if (sex === 1) return '男'
  if (sex === 2) return '女'
  return '未知'
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// --- Data ---
async function fetchUserInfo() {
  try {
    user.value = await request.get('/user/info')
  } catch {
    localStorage.removeItem('token')
    router.push('/login')
  }
}

// --- Edit Mode ---
function startEdit() {
  form.nickname = user.value.nickname || ''
  form.sex = user.value.sex ?? 0
  form.avatar = user.value.avatar || ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

// --- Avatar Upload ---
function handleAvatarClick() {
  fileInput.value?.click()
}

async function handleFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return

  // Validate before upload
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    MessagePlugin.warning('仅支持 JPG、PNG、GIF、WebP 格式图片')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    MessagePlugin.warning('图片大小不能超过 5MB')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const url = await request.post('/user/upload/avatar', formData)
    form.avatar = url
    MessagePlugin.success('头像上传成功')
  } catch {
    // Error handled by interceptor
  } finally {
    uploading.value = false
    // Reset input to allow re-upload of same file
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

// --- Save Profile ---
async function handleSave() {
  const valid = await formRef.value?.validate()
  if (valid !== true) return

  saving.value = true
  try {
    const updated = await request.put('/user/profile', {
      nickname: form.nickname,
      sex: form.sex,
      avatar: form.avatar
    })
    user.value = updated
    editing.value = false
    MessagePlugin.success('资料已保存')
  } catch {
    // Error handled by interceptor
  } finally {
    saving.value = false
  }
}

// --- Address ---
async function fetchAddresses() {
  addressLoading.value = true
  try {
    addresses.value = await request.get('/user/addresses')
  } catch {
    // handled by interceptor
  } finally {
    addressLoading.value = false
  }
}

function openAddressCreate() {
  addressIsEdit.value = false
  initAddressForm()
  addressDrawer.value = true
}

function openAddressEdit(addr) {
  addressIsEdit.value = true
  addressEditId.value = addr.id
  addressForm.name = addr.name
  addressForm.phone = addr.phone
  addressForm.province = addr.province
  addressForm.city = addr.city
  addressForm.district = addr.district
  addressForm.detail = addr.detail
  addressForm.isDefault = addr.isDefault ?? 0
  addressDrawer.value = true
}

async function handleAddressSave() {
  const valid = await addrFormRef.value?.validate()
  if (valid !== true) return

  addressSaving.value = true
  try {
    if (addressIsEdit.value) {
      await request.put(`/user/addresses/${addressEditId.value}`, {
        name: addressForm.name,
        phone: addressForm.phone,
        province: addressForm.province,
        city: addressForm.city,
        district: addressForm.district,
        detail: addressForm.detail,
        isDefault: addressForm.isDefault,
      })
      MessagePlugin.success('地址已更新')
    } else {
      await request.post('/user/addresses', {
        name: addressForm.name,
        phone: addressForm.phone,
        province: addressForm.province,
        city: addressForm.city,
        district: addressForm.district,
        detail: addressForm.detail,
        isDefault: addressForm.isDefault,
      })
      MessagePlugin.success('地址已添加')
    }
    addressDrawer.value = false
    fetchAddresses()
  } catch {
    // handled by interceptor
  } finally {
    addressSaving.value = false
  }
}

async function handleAddressDelete(id) {
  try {
    await request.delete(`/user/addresses/${id}`)
    MessagePlugin.success('地址已删除')
    fetchAddresses()
  } catch {
    // handled by interceptor
  }
}

async function handleSetDefault(id) {
  try {
    await request.put(`/user/addresses/${id}/default`)
    MessagePlugin.success('已设为默认地址')
    fetchAddresses()
  } catch {
    // handled by interceptor
  }
}

// --- Wechat Bind ---
const wxBinding = ref(false)

async function bindWechat() {
  try {
    const { url } = await request.get('/auth/wechat/url', { params: { state: 'bind' } })
    window.location.href = url
  } catch {}
}

// --- Logout ---
function handleLogout() {
  localStorage.removeItem('token')
  MessagePlugin.success('已退出登录')
  router.push('/')
}

// --- Init ---
import { useRoute } from 'vue-router'
const currentRoute = useRoute()
onMounted(() => {
  // 微信绑定回调
  const code = currentRoute.query.code
  const state = currentRoute.query.state
  if (code && state === 'bind') {
    request.post('/auth/wechat/bind', { code }).then(() => {
      MessagePlugin.success('微信绑定成功')
      router.replace('/profile')
    }).catch(() => {})
    return
  }

  const token = localStorage.getItem('token')
  if (!token) { router.push('/login'); return }
  fetchUserInfo()
  fetchAddresses()
  fetchPoints()
  fetchInviteInfo()
})
</script>
