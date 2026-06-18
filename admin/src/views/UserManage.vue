<template>
  <div class="user-manage">
    <!-- 页面标题 -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-[var(--color-text-primary)]">用户管理</h1>
      <p class="text-sm text-[var(--color-text-tertiary)] mt-1">管理平台所有注册用户</p>
    </div>

    <!-- 搜索区 -->
    <div class="bg-white rounded-xl p-4 mb-6 border border-gray-100">
      <div class="flex flex-wrap gap-3 items-center">
        <t-input
          v-model="query.keyword"
          placeholder="搜索昵称/手机号"
          clearable
          class="w-[220px]"
          @enter="search"
          @clear="search"
        >
          <template #prefix-icon><Search class="w-4 h-4" /></template>
        </t-input>
        <t-select
          v-model="query.status"
          placeholder="用户状态"
          clearable
          class="w-[140px]"
          @change="search"
        >
          <t-option :value="undefined" label="全部" />
          <t-option :value="1" label="正常" />
          <t-option :value="0" label="禁用" />
        </t-select>
        <t-button theme="primary" @click="search">
          <Search class="w-4 h-4 mr-1" />搜索
        </t-button>
        <t-button variant="outline" @click="reset">重置</t-button>
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
            {{ row.nickname?.charAt(0) || 'U' }}
          </t-avatar>
        </template>

        <!-- 性别 -->
        <template #sex="{ row }">
          <t-tag :theme="row.sex === 1 ? 'primary' : row.sex === 2 ? 'danger' : 'default'" variant="light" size="small">
            {{ row.sex === 1 ? '男' : row.sex === 2 ? '女' : '未知' }}
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

        <!-- 默认地址 -->
        <template #defaultAddress="{ row }">
          <span class="text-sm text-[var(--color-text-tertiary)]">
            {{ row.defaultAddress || '-' }}
          </span>
        </template>

        <!-- 最后登录IP -->
        <template #lastLoginIp="{ row }">
          <span class="text-sm text-[var(--color-text-tertiary)]">
            {{ row.lastLoginIp || '-' }}
          </span>
        </template>

        <!-- 注册时间 -->
        <template #createTime="{ row }">
          <span class="text-sm text-[var(--color-text-tertiary)]">
            {{ formatTime(row.createTime) }}
          </span>
        </template>

        <!-- 操作 -->
        <template #operation="{ row }">
          <div class="flex items-center gap-2">
            <t-button variant="text" theme="primary" size="small" @click="openEdit(row)">
              编辑
            </t-button>
            <t-popconfirm content="确定要删除该用户吗？" @confirm="handleDelete(row.id)">
              <t-button variant="text" theme="danger" size="small">删除</t-button>
            </t-popconfirm>
          </div>
        </template>
      </t-table>
    </div>

    <!-- 编辑抽屉 -->
    <t-drawer
      v-model:visible="editVisible"
      header="编辑用户"
      :footer="false"
      size="400px"
    >
      <t-form :data="editForm" label-align="top" class="edit-form">
        <t-form-item label="昵称">
          <t-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </t-form-item>
        <t-form-item label="手机号">
          <t-input v-model="editForm.phone" placeholder="请输入手机号" />
        </t-form-item>
        <t-form-item label="性别">
          <t-radio-group v-model="editForm.sex">
            <t-radio :value="0">未知</t-radio>
            <t-radio :value="1">男</t-radio>
            <t-radio :value="2">女</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="状态">
          <t-switch v-model="editForm.status" :custom-value="[1, 0]" />
        </t-form-item>
        <t-form-item label="邀请码">
          <t-input v-model="editForm.inviteCode" disabled />
        </t-form-item>
        <t-button block theme="primary" :loading="submitting" @click="submitEdit">
          保存修改
        </t-button>
      </t-form>
    </t-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { Search } from 'lucide-vue-next'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const editVisible = ref(false)

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

const editForm = reactive({
  id: null,
  nickname: '',
  phone: '',
  sex: 0,
  status: 1,
  inviteCode: '',
})

const columns = [
  { colKey: 'avatar', title: '头像', width: 70 },
  { colKey: 'id', title: 'ID', width: 80 },
  { colKey: 'nickname', title: '昵称', ellipsis: true },
  { colKey: 'wxNickname', title: '绑定微信', width: 130 },
  { colKey: 'phone', title: '手机号', width: 130 },
  { colKey: 'sex', title: '性别', width: 80 },
  { colKey: 'inviteCode', title: '邀请码', width: 110, ellipsis: true },
  { colKey: 'defaultAddress', title: '默认地址', width: 220, ellipsis: true },
  { colKey: 'lastLoginIp', title: '最后登录IP', width: 130 },
  { colKey: 'status', title: '状态', width: 80 },
  { colKey: 'createTime', title: '注册时间', width: 170 },
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
    const res = await request.get('/admin/users', { params })
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
    await request.put(`/admin/users/${row.id}`, { status: val ? 1 : 0 })
    row.status = val ? 1 : 0
    MessagePlugin.success('状态已更新')
  } catch {
    // 错误已在拦截器中处理
  }
}

async function handleDelete(id) {
  try {
    await request.delete(`/admin/users/${id}`)
    MessagePlugin.success('删除成功')
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

function openEdit(row) {
  editForm.id = row.id
  editForm.nickname = row.nickname || ''
  editForm.phone = row.phone || ''
  editForm.sex = row.sex ?? 0
  editForm.status = row.status ?? 1
  editForm.inviteCode = row.inviteCode || ''
  editVisible.value = true
}

async function submitEdit() {
  submitting.value = true
  try {
    await request.put(`/admin/users/${editForm.id}`, {
      nickname: editForm.nickname,
      phone: editForm.phone,
      sex: editForm.sex,
      status: editForm.status,
    })
    MessagePlugin.success('修改成功')
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
.user-manage {
  max-width: 1200px;
}
.edit-form {
  padding-top: 8px;
}
</style>
