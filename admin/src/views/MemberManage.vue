<template>
  <div class="member-page">
    <div class="mb-6 flex items-center justify-between">
      <div><h1 class="text-2xl font-bold">套餐管理</h1><p class="text-sm text-[var(--color-text-tertiary)] mt-1">管理会员充值套餐</p></div>
      <t-button theme="primary" @click="openCreate"><Plus class="w-4 h-4 mr-1" />新增套餐</t-button>
    </div>
    <div class="bg-white rounded-xl border border-gray-100 overflow-hidden p-6">
      <t-table :data="pkgData" :columns="pkgCols" :loading="loading" row-key="id" hover stripe size="small">
        <template #price="{ row }"><span class="text-orange-500 font-semibold">¥{{ row.price }}</span></template>
        <template #status="{ row }"><t-tag :theme="row.status===1?'success':'default'" size="small">{{ row.status===1?'启用':'禁用' }}</t-tag></template>
        <template #operation="{ row }">
          <t-space size="small">
            <t-button variant="text" theme="primary" size="small" @click="openEdit(row)">编辑</t-button>
            <t-popconfirm content="确定删除？" @confirm="delPkg(row.id)"><t-button variant="text" theme="danger" size="small">删除</t-button></t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </div>

    <t-drawer v-model:visible="drawer" :header="isEdit?'编辑套餐':'新增套餐'" size="400px" :footer="false">
      <t-form :data="form" label-align="top">
        <t-form-item label="套餐名称"><t-input v-model="form.packageName" /></t-form-item>
        <t-form-item label="价格"><t-input-number v-model="form.price" :min="0" :decimalPlaces="2" /></t-form-item>
        <t-form-item label="会员天数"><t-input-number v-model="form.dayNum" :min="1" /></t-form-item>
        <t-form-item label="赠送积分"><t-input-number v-model="form.givePoint" :min="0" /></t-form-item>
        <t-form-item label="排序"><t-input-number v-model="form.sort" :min="0" /></t-form-item>
        <t-form-item label="状态"><t-switch v-model="form.status" :custom-value="[1,0]" /></t-form-item>
        <t-button block theme="primary" :loading="saving" @click="savePkg">保存</t-button>
      </t-form>
    </t-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { Plus } from 'lucide-vue-next'
import request from '@/utils/request'

const drawer = ref(false); const isEdit = ref(false); const editId = ref(null); const saving = ref(false)
const pkgData = ref([]); const loading = ref(false)
const initForm = () => ({ packageName:'',price:0,dayNum:30,givePoint:0,sort:0,status:1 })
const form = reactive(initForm())
const pkgCols = [{colKey:'id',title:'ID',width:70},{colKey:'packageName',title:'名称'},{colKey:'price',title:'价格',width:80},{colKey:'dayNum',title:'天数',width:70},{colKey:'givePoint',title:'送积分',width:80},{colKey:'sort',title:'排序',width:60},{colKey:'status',title:'状态',width:70},{colKey:'operation',title:'操作',width:140}]

async function fetchPkgs() { loading.value=true; try{const r=await request.get('/admin/member/packages');pkgData.value=r.records||[]}catch(e){}finally{loading.value=false} }
function openCreate(){isEdit.value=false;editId.value=null;Object.assign(form,initForm());drawer.value=true}
function openEdit(row){isEdit.value=true;editId.value=row.id;Object.assign(form,row);drawer.value=true}
async function savePkg(){
  saving.value=true
  try{if(isEdit.value){await request.put(`/admin/member/packages/${editId.value}`,form);MessagePlugin.success('已更新')}else{await request.post('/admin/member/packages',form);MessagePlugin.success('已创建')}
    drawer.value=false;fetchPkgs()}catch(e){}finally{saving.value=false}
}
async function delPkg(id){await request.delete(`/admin/member/packages/${id}`);MessagePlugin.success('已删除');fetchPkgs()}
onMounted(fetchPkgs)
</script>
