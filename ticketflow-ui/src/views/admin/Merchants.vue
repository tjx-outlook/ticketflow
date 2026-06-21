<template>
  <div>
    <div class="page-header">
      <h2>商家管理</h2>
      <el-button type="primary" @click="handleAdd">新增商家</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" class="modern-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="merchantName" label="商家名称" width="160" />
      <el-table-column prop="contactName" label="联系人" width="120" />
      <el-table-column prop="contactPhone" label="联系电话" width="140" />
      <el-table-column prop="contactEmail" label="联系邮箱" min-width="180" />
      <el-table-column prop="address" label="地址" min-width="200" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="plain">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="current" :page-size="size" :total="total"
      layout="total, prev, pager, next" @current-change="fetchData" style="margin-top:20px;justify-content:flex-end" />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商家' : '新增商家'" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商家名称"><el-input v-model="form.merchantName" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
        <el-form-item label="联系邮箱"><el-input v-model="form.contactEmail" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { merchantApi } from '../../api/merchant'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const current = ref(1), size = ref(10), total = ref(0)
const dialogVisible = ref(false), isEdit = ref(false), editId = ref(null)
const form = ref({ merchantName: '', contactName: '', contactPhone: '', contactEmail: '', address: '', description: '' })

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await merchantApi.getList({ current: current.value, size: size.value })
    tableData.value = res.records; total.value = res.total
  } finally { loading.value = false }
}

function handleAdd() {
  isEdit.value = false; editId.value = null
  form.value = { merchantName: '', contactName: '', contactPhone: '', contactEmail: '', address: '', description: '' }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true; editId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

async function submitForm() {
  if (isEdit.value) {
    await merchantApi.update(editId.value, form.value)
  } else {
    await merchantApi.create(form.value)
  }
  ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
  dialogVisible.value = false; fetchData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该商家吗？', '提示', { type: 'warning' })
  await merchantApi.delete(row.id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style scoped>
.modern-table { font-size: 13px; }
</style>
