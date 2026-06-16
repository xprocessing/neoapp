<template>
  <div class="settings-page">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-[var(--color-text-primary)]">系统设置</h1>
      <p class="text-sm text-[var(--color-text-tertiary)] mt-1">管理全局配置项</p>
    </div>

    <!-- 搜索区 -->
    <div class="bg-white rounded-xl p-4 mb-6 border border-gray-100">
      <div class="flex flex-wrap gap-3 items-center justify-between">
        <div class="flex flex-wrap gap-3 items-center">
          <t-input
            v-model="keyword"
            placeholder="搜索配置键名/名称"
            clearable
            class="w-[240px]"
            @enter="fetchData"
            @clear="fetchData"
          >
            <template #prefix-icon><Search class="w-4 h-4" /></template>
          </t-input>
          <t-button theme="primary" @click="fetchData">
            <Search class="w-4 h-4 mr-1" />搜索
          </t-button>
          <t-button variant="outline" @click="keyword='';fetchData()">重置</t-button>
        </div>
        <t-button theme="primary" @click="openCreate">
          <Plus class="w-4 h-4 mr-1" />新增配置
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
        <template #configKey="{ row }">
          <code class="text-xs bg-gray-100 px-2 py-0.5 rounded text-[var(--color-primary)]">{{ row.configKey }}</code>
        </template>

        <template #status="{ row }">
          <t-tag :theme="row.status === 1 ? 'success' : 'default'" variant="light" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </t-tag>
        </template>

        <template #createTime="{ row }">
          <span class="text-sm text-[var(--color-text-tertiary)]">{{ formatTime(row.createTime) }}</span>
        </template>

        <template #operation="{ row }">
          <div class="flex items-center gap-2">
            <t-button variant="text" theme="primary" size="small" @click="openEdit(row)">编辑</t-button>
            <t-popconfirm content="确定删除该配置吗？" @confirm="handleDelete(row.id)">
              <t-button variant="text" theme="danger" size="small">删除</t-button>
            </t-popconfirm>
          </div>
        </template>
      </t-table>
    </div>

    <!-- 编辑抽屉 -->
    <t-drawer
      v-model:visible="drawerVisible"
      :header="isCreate ? '新增配置' : '编辑配置'"
      :footer="false"
      size="400px"
    >
      <t-form ref="formRef" :data="form" :rules="formRules" label-align="top" class="edit-form">
        <t-form-item label="配置键名" name="configKey">
          <t-input v-model="form.configKey" placeholder="如 site_name" :disabled="!isCreate" />
        </t-form-item>
        <t-form-item label="配置名称" name="configName">
          <t-input v-model="form.configName" placeholder="如 站点名称" />
        </t-form-item>
        <t-form-item label="配置值" name="configValue">
          <t-textarea v-model="form.configValue" placeholder="配置值" :autosize="{ minRows: 2, maxRows: 4 }" />
        </t-form-item>
        <t-form-item label="备注">
          <t-input v-model="form.remark" placeholder="备注说明" />
        </t-form-item>
        <t-form-item label="状态">
          <t-switch v-model="form.status" :custom-value="[1, 0]" />
        </t-form-item>
        <t-button block theme="primary" :loading="saving" @click="handleSave">保存</t-button>
      </t-form>
    </t-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { Search, Plus } from 'lucide-vue-next'
import request from '@/utils/request'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const drawerVisible = ref(false)
const isCreate = ref(false)
const keyword = ref('')
const formRef = ref(null)
const editId = ref(null)
const page = ref(1)
const size = ref(10)

const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const initForm = () => ({
  configKey: '',
  configName: '',
  configValue: '',
  remark: '',
  status: 1,
})

const form = reactive(initForm())
const formRules = {
  configKey: [{ required: true, message: '请输入配置键名', trigger: 'blur' }],
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
}

const columns = [
  { colKey: 'id', title: 'ID', width: 70 },
  { colKey: 'configKey', title: '键名', width: 150 },
  { colKey: 'configName', title: '名称', width: 130 },
  { colKey: 'configValue', title: '值', ellipsis: true },
  { colKey: 'remark', title: '备注', width: 150, ellipsis: true },
  { colKey: 'status', title: '状态', width: 70 },
  { colKey: 'createTime', title: '创建时间', width: 170 },
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
    const params = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await request.get('/admin/configs', { params })
    tableData.value = res.records || []
    pagination.current = res.current || 1
    pagination.pageSize = res.size || 10
    pagination.total = res.total || 0
  } catch { /* handled */ } finally { loading.value = false }
}

function onPageChange(pageInfo) {
  page.value = pageInfo.current
  size.value = pageInfo.pageSize
  fetchData()
}

function openCreate() {
  isCreate.value = true
  Object.assign(form, initForm())
  editId.value = null
  drawerVisible.value = true
}

function openEdit(row) {
  isCreate.value = false
  editId.value = row.id
  form.configKey = row.configKey
  form.configName = row.configName
  form.configValue = row.configValue || ''
  form.remark = row.remark || ''
  form.status = row.status ?? 1
  drawerVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate()
  if (valid !== true) return
  saving.value = true
  try {
    const payload = { ...form }
    if (isCreate.value) {
      await request.post('/admin/configs', payload)
      MessagePlugin.success('创建成功')
    } else {
      await request.put(`/admin/configs/${editId.value}`, payload)
      MessagePlugin.success('更新成功')
    }
    drawerVisible.value = false
    fetchData()
  } catch { /* handled */ } finally { saving.value = false }
}

async function handleDelete(id) {
  try {
    await request.delete(`/admin/configs/${id}`)
    MessagePlugin.success('删除成功')
    fetchData()
  } catch { /* handled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.settings-page { max-width: 1000px; }
.edit-form { padding-top: 8px; }
</style>
