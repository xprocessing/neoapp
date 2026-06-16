<template>
  <div class="admin-user-manage">
    <!-- 页面标题 -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-[var(--color-text-primary)]">管理员管理</h1>
      <p class="text-sm text-[var(--color-text-tertiary)] mt-1">管理后台管理员账号</p>
    </div>

    <!-- 搜索区 -->
    <div class="bg-white rounded-xl p-4 mb-6 border border-gray-100">
      <div class="flex flex-wrap gap-3 items-center justify-between">
        <div class="flex flex-wrap gap-3 items-center">
          <t-input
            v-model="query.keyword"
            placeholder="搜索用户名/昵称/手机号"
            clearable
            class="w-[240px]"
            @enter="search"
            @clear="search"
          >
            <template #prefix-icon><Search class="w-4 h-4" /></template>
          </t-input>
          <t-select
            v-model="query.status"
            placeholder="状态"
            clearable
            class="w-[120px]"
            @change="search"
          >
            <t-option :value="undefined" label="全部" />
            <t-option :value="1" label="启用" />
            <t-option :value="0" label="禁用" />
          </t-select>
          <t-button theme="primary" @click="search">
            <Search class="w-4 h-4 mr-1" />搜索
          </t-button>
          <t-button variant="outline" @click="reset">重置</t-button>
        </div>
        <t-button theme="primary" @click="openCreate">
          <Plus class="w-4 h-4 mr-1" />新增管理员
        </t-button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="bg-white rounded-xl border border-gray-100 overflow-hidden">
      <t-table
        :data="tableData"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        hover
        stripe
        table-layout="auto"
        @page-change="onPageChange"
      >
        <!-- 头像 -->
        <template #avatar="{ row }">
          <t-avatar :image="row.avatar" size="medium">
            {{ (row.nickname || row.username)?.charAt(0) || 'A' }}
          </t-avatar>
        </template>

        <!-- 性别 -->
        <template #gender="{ row }">
          <t-tag
            :theme="row.gender === 1 ? 'primary' : row.gender === 2 ? 'danger' : 'default'"
            variant="light"
            size="small"
          >
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </t-tag>
        </template>

        <!-- 状态 -->
        <template #status="{ row }">
          <t-switch
            :value="row.status === 1"
            size="small"
            @change="(val) => handleStatusChange(row, val)"
          />
        </template>

        <!-- 最后登录 -->
        <template #lastLoginTime="{ row }">
          <span class="text-sm text-[var(--color-text-tertiary)]">
            {{ formatTime(row.lastLoginTime) || '未登录' }}
          </span>
        </template>

        <!-- 最后登录IP -->
        <template #lastLoginIp="{ row }">
          <span class="text-sm text-[var(--color-text-tertiary)]">
            {{ row.lastLoginIp || '-' }}
          </span>
        </template>

        <!-- 操作 -->
        <template #operation="{ row }">
          <div class="flex items-center gap-2">
            <t-button variant="text" theme="primary" size="small" @click="openEdit(row)">
              编辑
            </t-button>
            <t-popconfirm
              content="确定要删除该管理员吗？"
              @confirm="handleDelete(row.id)"
            >
              <t-button
                variant="text"
                theme="danger"
                size="small"
                :disabled="row.username === 'admin'"
              >
                删除
              </t-button>
            </t-popconfirm>
          </div>
        </template>
      </t-table>
    </div>

    <!-- 编辑/新建抽屉 -->
    <t-drawer
      v-model:visible="editVisible"
      :header="isCreate ? '新增管理员' : '编辑管理员'"
      :footer="false"
      size="400px"
    >
      <t-form ref="formRef" :data="editForm" :rules="formRules" label-align="top" class="edit-form">
        <t-form-item label="用户名" name="username">
          <t-input v-model="editForm.username" placeholder="请输入用户名" />
        </t-form-item>
        <t-form-item label="密码" :name="isCreate ? 'password' : undefined">
          <t-input
            v-model="editForm.password"
            type="password"
            :placeholder="isCreate ? '请输入密码' : '留空则不修改密码'"
          />
        </t-form-item>
        <t-form-item label="昵称">
          <t-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </t-form-item>
        <t-form-item label="手机号">
          <t-input v-model="editForm.phone" placeholder="请输入手机号" />
        </t-form-item>
        <t-form-item label="邮箱">
          <t-input v-model="editForm.email" placeholder="请输入邮箱" />
        </t-form-item>
        <t-form-item label="性别">
          <t-radio-group v-model="editForm.gender">
            <t-radio :value="0">未知</t-radio>
            <t-radio :value="1">男</t-radio>
            <t-radio :value="2">女</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="状态">
          <t-switch v-model="editForm.status" :custom-value="[1, 0]" />
        </t-form-item>
        <t-button block theme="primary" :loading="submitting" @click="submitEdit">
          保存
        </t-button>
      </t-form>
    </t-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { Search, Plus } from 'lucide-vue-next'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const editVisible = ref(false)
const isCreate = ref(false)
const formRef = ref(null)

const query = reactive({
  keyword: '',
  status: undefined,
  page: 1,
  size: 10,
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

const initForm = () => ({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  status: 1,
})

const editForm = reactive(initForm())

const formRules = computed(() => {
  if (isCreate.value) {
    return {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
      ],
    }
  }
  return {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  }
})

const columns = [
  { colKey: 'avatar', title: '头像', width: 70 },
  { colKey: 'id', title: 'ID', width: 70 },
  { colKey: 'username', title: '用户名', ellipsis: true },
  { colKey: 'nickname', title: '昵称', ellipsis: true },
  { colKey: 'phone', title: '手机号', width: 130 },
  { colKey: 'email', title: '邮箱', width: 180, ellipsis: true },
  { colKey: 'gender', title: '性别', width: 70 },
  { colKey: 'status', title: '状态', width: 80 },
  { colKey: 'lastLoginTime', title: '最后登录', width: 170 },
  { colKey: 'lastLoginIp', title: '最后登录IP', width: 130 },
  { colKey: 'operation', title: '操作', width: 140, fixed: 'right' },
]

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: query.page,
      size: query.size,
    }
    if (query.keyword) params.keyword = query.keyword
    if (query.status !== undefined && query.status !== null && query.status !== '') {
      params.status = query.status
    }
    const res = await request.get('/admin/admins', { params })
    tableData.value = res.records || []
    pagination.current = res.current || 1
    pagination.pageSize = res.size || 10
    pagination.total = res.total || 0
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  fetchData()
}

function reset() {
  query.keyword = ''
  query.status = undefined
  query.page = 1
  fetchData()
}

function onPageChange(pageInfo) {
  query.page = pageInfo.current
  query.size = pageInfo.pageSize
  fetchData()
}

async function handleStatusChange(row, val) {
  try {
    await request.put(`/admin/admins/${row.id}`, { status: val ? 1 : 0 })
    row.status = val ? 1 : 0
    MessagePlugin.success('状态已更新')
  } catch {
    // 错误已在拦截器中处理
  }
}

async function handleDelete(id) {
  try {
    await request.delete(`/admin/admins/${id}`)
    MessagePlugin.success('删除成功')
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

function openCreate() {
  isCreate.value = true
  Object.assign(editForm, initForm())
  editVisible.value = true
}

function openEdit(row) {
  isCreate.value = false
  editForm.id = row.id
  editForm.username = row.username || ''
  editForm.password = ''
  editForm.nickname = row.nickname || ''
  editForm.email = row.email || ''
  editForm.phone = row.phone || ''
  editForm.gender = row.gender ?? 0
  editForm.status = row.status ?? 1
  editVisible.value = true
}

async function submitEdit() {
  if (isCreate.value) {
    const result = await formRef.value?.validate()
    if (result !== true) return
  }
  submitting.value = true
  try {
    const payload = {
      username: editForm.username,
      nickname: editForm.nickname,
      email: editForm.email,
      phone: editForm.phone,
      gender: editForm.gender,
      status: editForm.status,
    }
    if (editForm.password) payload.password = editForm.password

    if (isCreate.value) {
      await request.post('/admin/admins', payload)
      MessagePlugin.success('创建成功')
    } else {
      await request.put(`/admin/admins/${editForm.id}`, payload)
      MessagePlugin.success('更新成功')
    }
    editVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.admin-user-manage {
  max-width: 1200px;
}
.edit-form {
  padding-top: 8px;
}
</style>
