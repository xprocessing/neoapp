<template>
  <div class="task-page">
    <div class="mb-6 flex items-center justify-between">
      <div><h1 class="text-2xl font-bold">任务管理</h1><p class="text-sm text-[var(--color-text-tertiary)] mt-1">发布与管理任务</p></div>
      <t-button theme="primary" @click="openCreate"><Plus class="w-4 h-4 mr-1" />发布任务</t-button>
    </div>

    <div class="bg-white rounded-xl border border-gray-100 overflow-hidden">
      <div class="flex gap-3 items-center p-4 border-b border-gray-50">
        <t-input v-model="keyword" placeholder="搜索任务标题" clearable class="w-[260px]" @enter="search" @clear="search">
          <template #prefix-icon><Search class="w-4 h-4" /></template>
        </t-input>
        <t-button theme="primary" @click="search"><Search class="w-4 h-4 mr-1" />搜索</t-button>
        <t-button variant="outline" @click="keyword='';search()">重置</t-button>
      </div>
      <t-table :data="taskData" :columns="taskCols" :loading="loading" :pagination="pg" row-key="id" hover stripe size="small" @page-change="onPg">
        <template #taskCover="{ row }"><t-image v-if="row.taskCover" :src="row.taskCover" fit="cover" class="w-10 h-10 rounded" /></template>
        <template #reward_amount="{ row }"><span class="text-orange-500 font-semibold">¥{{ row.rewardAmount }}</span></template>
        <template #status="{ row }"><t-tag :theme="row.status===1?'success':'default'" size="small">{{ row.status===1?'上架':'下架' }}</t-tag></template>
        <template #createTime="{ row }"><span class="text-xs text-[var(--color-text-tertiary)]">{{ fmt(row.createTime) }}</span></template>
        <template #operation="{ row }">
          <t-space size="small">
            <t-button variant="text" theme="primary" size="small" @click="openEdit(row)">编辑</t-button>
            <t-button variant="text" :theme="row.status===1?'warning':'success'" size="small" @click="toggleStatus(row)">{{ row.status===1?'下架':'上架' }}</t-button>
            <t-popconfirm content="确定删除？" @confirm="delTask(row.id)"><t-button variant="text" theme="danger" size="small">删除</t-button></t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </div>

    <t-drawer v-model:visible="drawer" :header="isEdit?'编辑任务':'发布任务'" size="900px" :footer="false">
      <t-form :data="form" label-align="top">
        <t-form-item label="标题"><t-input v-model="form.taskTitle" /></t-form-item>
        <t-form-item label="封面图">
          <div class="flex items-start gap-3">
            <div class="flex-shrink-0">
              <t-image v-if="form.taskCover" :src="form.taskCover" fit="cover" class="w-[120px] h-[80px] rounded border" />
              <div v-else class="w-[120px] h-[80px] rounded border border-dashed border-gray-300 flex items-center justify-center text-[var(--color-text-tertiary)] text-xs">暂无图片</div>
            </div>
            <div class="flex-1">
              <t-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                accept="image/*"
                theme="file"
                :max="1"
                :size-limit="{ size: 5, unit: 'MB' }"
                @success="onCoverUploaded"
                @fail="onCoverFail"
              />
              <p class="text-xs text-[var(--color-text-tertiary)] mt-1">或手动填写URL</p>
              <t-input v-model="form.taskCover" placeholder="https://..." size="small" class="mt-1" />
            </div>
          </div>
        </t-form-item>
        <t-form-item label="简介"><t-textarea v-model="form.taskIntro" :maxlength="200" /></t-form-item>
        <t-form-item label="关键词"><t-input v-model="form.keyword" /></t-form-item>
        <t-form-item label="操作步骤详情">
          <div class="quill-wrapper border rounded">
            <QuillEditor ref="quillRef" v-model:content="form.taskContent" contentType="html" theme="snow" />
          </div>
          <t-button size="small" variant="outline" class="mt-2" @click="uploadImage"><Upload class="w-4 h-4 mr-1" />插入图片</t-button>
          <input ref="imgInput" type="file" accept="image/*" class="hidden" @change="onImageSelected" />
        </t-form-item>
        <div class="grid grid-cols-2 gap-3">
          <t-form-item label="奖励(元)"><t-input-number v-model="form.rewardAmount" :min="0" :decimalPlaces="2" /></t-form-item>
          <t-form-item label="有效时长(分钟)"><t-input-number v-model="form.expireMinute" :min="1" /></t-form-item>
        </div>
        <t-form-item label="24小时任务数量">
          <div class="grid grid-cols-6 gap-2">
            <div v-for="(v, i) in hourValues" :key="i" class="flex items-center gap-1">
              <span class="text-xs text-[var(--color-text-tertiary)] w-5 text-right">{{ i }}</span>
              <t-input-number v-model="hourValues[i]" :min="0" size="small" class="flex-1" @change="onHourChange" />
            </div>
          </div>
        </t-form-item>
        <t-form-item label="总数量(自动计算)"><t-input-number v-model="form.totalNum" disabled /></t-form-item>
        <t-form-item label="排序"><t-input-number v-model="form.sort" :min="0" /></t-form-item>
        <t-form-item label="赠品"><t-input v-model="form.gift" /></t-form-item>
        <t-form-item label="状态"><t-switch v-model="form.status" :custom-value="[1,0]" /></t-form-item>
        <t-button block theme="primary" :loading="saving" @click="saveTask">保存</t-button>
      </t-form>
    </t-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { Plus, Upload, Search } from 'lucide-vue-next'
import { QuillEditor } from '@vueup/vue-quill'
import 'quill/dist/quill.snow.css'
import request from '@/utils/request'

const uploadUrl = '/api/user/upload/avatar'
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token') || ''}` }

function onCoverUploaded({ response }) {
  if (response?.code === 200 && response.data) {
    form.taskCover = response.data
    MessagePlugin.success('封面上传成功')
  }
}
function onCoverFail() {
  MessagePlugin.error('封面上传失败')
}

const quillRef = ref(null)
const imgInput = ref(null)

function uploadImage() { imgInput.value?.click() }

async function onImageSelected(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await fetch(uploadUrl, { method: 'POST', headers: uploadHeaders, body: formData })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      const quill = quillRef.value?.getQuill()
      if (quill) {
        const range = quill.getSelection(true)
        quill.insertEmbed(range.index, 'image', data.data)
        MessagePlugin.success('图片已插入')
      }
    } else {
      MessagePlugin.error('上传失败')
    }
  } catch { MessagePlugin.error('图片上传失败') }
  e.target.value = ''
}

const hourValues = reactive(Array(24).fill(0))

function parseHourLimits() {
  if (form.hourLimits) {
    const parts = form.hourLimits.split(',')
    parts.forEach((v, i) => { if (i < 24) hourValues[i] = parseInt(v) || 0 })
  }
}
function onHourChange() {
  form.hourLimits = hourValues.join(',')
  form.totalNum = hourValues.reduce((s, v) => s + (v || 0), 0)
}
function resetHourValues() {
  for (let i = 0; i < 24; i++) hourValues[i] = 0
  form.hourLimits = ''
  form.totalNum = 0
}

const drawer = ref(false); const isEdit = ref(false); const editId = ref(null); const saving = ref(false)
const taskData = ref([]); const loading = ref(false); const keyword = ref('')
const pg = reactive({current:1,pageSize:10,total:0})

const initForm = () => ({ taskTitle:'',taskCover:'',taskIntro:'',keyword:'',taskContent:'',rewardAmount:0,totalNum:0,hourLimits:'',expireMinute:60,sort:0,gift:'',status:1 })
const form = reactive(initForm())

const taskCols = [
  {colKey:'id',title:'ID',width:70},
  {colKey:'taskCover',title:'封面',width:60},
  {colKey:'taskTitle',title:'标题',width:160,ellipsis:true},
  {colKey:'taskIntro',title:'简介',width:180,ellipsis:true},
  {colKey:'keyword',title:'关键词',width:100,ellipsis:true},
  {colKey:'reward_amount',title:'奖励',width:80},
  {colKey:'gift',title:'赠品',width:100,ellipsis:true},
  {colKey:'totalNum',title:'总量',width:60},
  {colKey:'hourLimits',title:'每小时上限',width:160,ellipsis:true},
  {colKey:'expireMinute',title:'时效(分)',width:80},
  {colKey:'sort',title:'排序',width:60},
  {colKey:'status',title:'状态',width:70},
  {colKey:'createTime',title:'创建时间',width:170},
  {colKey:'operation',title:'操作',width:180,fixed:'right'},
]

function fmt(t){if(!t)return'';const d=new Date(t);return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`}

async function fetchTasks() {
  loading.value=true
  try{
    const p = {page:pg.current,size:pg.pageSize}
    if(keyword.value) p.keyword = keyword.value
    const r=await request.get('/admin/tasks',{params:p});taskData.value=r.records||[];pg.total=r.total||0
  }catch(e){}finally{loading.value=false}
}
function search(){ pg.current=1; fetchTasks() }
function onPg(p){pg.current=p.current;fetchTasks()}
function openCreate(){isEdit.value=false;editId.value=null;Object.assign(form,initForm());resetHourValues();drawer.value=true}
function openEdit(row){isEdit.value=true;editId.value=row.id;Object.assign(form,row);parseHourLimits();drawer.value=true}
async function saveTask(){
  saving.value=true
  try{
    if(isEdit.value){await request.put(`/admin/tasks/${editId.value}`,form);MessagePlugin.success('已更新')}
    else{await request.post('/admin/tasks',form);MessagePlugin.success('已创建')}
    drawer.value=false;fetchTasks()
  }catch(e){}finally{saving.value=false}
}
async function toggleStatus(row){await request.put(`/admin/tasks/${row.id}`,{...row,status:row.status===1?0:1});MessagePlugin.success('已更新');fetchTasks()}
async function delTask(id){await request.delete(`/admin/tasks/${id}`);MessagePlugin.success('已删除');fetchTasks()}

onMounted(fetchTasks)
</script>

<style>
.quill-wrapper .ql-editor { min-height:300px;min-width: 500px; }
.quill-wrapper .ql-toolbar { border-radius:6px 6px 0 0; border:none; border-bottom:1px solid #e5e7eb; }
.quill-wrapper .ql-container { border:none; }
</style>
