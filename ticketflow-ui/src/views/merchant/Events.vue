<template>
  <div>
    <div class="page-header">
      <h2>活动管理</h2>
      <el-button v-if="userStore.hasPermission('event:manage')" type="primary" @click="handleAdd">新增活动</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" class="modern-table">
      <el-table-column label="海报" width="90">
        <template #default="{ row }">
          <img v-if="row.posterUrl" :src="row.posterUrl" class="poster-thumb" />
          <span v-else class="no-poster">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="eventName" label="活动名称" min-width="200" />
      <el-table-column prop="venue" label="场馆" width="160" />
      <el-table-column prop="startTime" label="开始时间" width="150" />
      <el-table-column prop="onSaleTime" label="开售时间" width="150" />
      <el-table-column prop="totalSeats" label="座位数" width="75" />
      <el-table-column prop="status" label="状态" width="85">
        <template #default="{ row }">
          <el-tag size="small" :type="statusMap[row.status]?.type" effect="plain">
            {{ statusMap[row.status]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="canEdit" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="canEdit && row.status !== 1" size="small" type="success" @click="changeStatus(row, 1)">开售</el-button>
          <el-button v-if="canEdit && row.status === 1" size="small" type="warning" @click="changeStatus(row, 2)">售罄</el-button>
          <el-button v-if="canEdit" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="current" :page-size="size" :total="total"
      layout="total, prev, pager, next" @current-change="fetchData" style="margin-top:20px;justify-content:flex-end" />

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '新增活动'" width="700px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动名称"><el-input v-model="form.eventName" /></el-form-item>
            <el-form-item label="商家ID"><el-input-number v-model="form.merchantId" :min="1" /></el-form-item>
            <el-form-item label="演出场馆"><el-input v-model="form.venue" /></el-form-item>
            <el-form-item label="总座位数"><el-input-number v-model="form.totalSeats" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动海报">
              <div class="upload-area">
                <el-upload
                  :http-request="customUpload"
                  :show-file-list="false"
                  :before-upload="beforeUpload"
                  accept="image/*"
                >
                  <img v-if="form.posterUrl" :src="form.posterUrl" class="poster-preview" />
                  <div v-else class="upload-placeholder">
                    <el-icon size="28"><Plus /></el-icon>
                    <span>上传海报</span>
                  </div>
                </el-upload>
                <el-button v-if="form.posterUrl" size="small" text type="danger" @click="form.posterUrl = ''">移除图片</el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="开售时间">
          <el-date-picker v-model="form.onSaleTime" type="datetime" placeholder="选择开售时间"
            format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
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
import { eventApi } from '../../api/event'
import axios from 'axios'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const canEdit = userStore.hasPermission('event:manage') || userStore.hasRole('SUPER_ADMIN')
const loading = ref(false)
const tableData = ref([])
const current = ref(1), size = ref(10), total = ref(0)
const dialogVisible = ref(false), isEdit = ref(false), editId = ref(null)
const form = ref({ eventName: '', merchantId: 1, venue: '', description: '', posterUrl: '', startTime: '', endTime: '', onSaleTime: '', totalSeats: 0 })

const statusMap = {
  0: { label: '未开售', type: 'info' },
  1: { label: '热卖中', type: 'success' },
  2: { label: '已售罄', type: 'danger' },
  3: { label: '已结束', type: 'info' },
  4: { label: '已取消', type: 'warning' }
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await eventApi.getList({ current: current.value, size: size.value })
    tableData.value = res.records; total.value = res.total
  } finally { loading.value = false }
}

function handleAdd() {
  isEdit.value = false; editId.value = null
  form.value = { eventName: '', merchantId: 1, venue: '', description: '', posterUrl: '', startTime: '', endTime: '', onSaleTime: '', totalSeats: 0 }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true; editId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

async function customUpload(options) {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await axios.post('/api/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    })
    const body = res.data
    if (body.code === 200 && body.data?.url) {
      form.value.posterUrl = body.data.url
      ElMessage.success('海报上传成功')
      options.onSuccess()
    } else {
      ElMessage.error(body.message || '上传失败')
      options.onError(new Error(body.message))
    }
  } catch (e) {
    ElMessage.error('上传失败: ' + (e.response?.data?.message || e.message))
    options.onError(e)
  }
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) { ElMessage.error('图片大小不能超过 5MB'); return false }
  return true
}

async function submitForm() {
  if (isEdit.value) {
    await eventApi.update(editId.value, form.value)
  } else {
    await eventApi.create(form.value)
  }
  ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
  dialogVisible.value = false; fetchData()
}

async function changeStatus(row, status) {
  await eventApi.updateStatus(row.id, { status })
  ElMessage.success('状态更新成功')
  row.status = status
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该活动吗？', '提示', { type: 'warning' })
  await eventApi.delete(row.id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style scoped>
.modern-table { font-size: 13px; }
.poster-thumb { width: 60px; height: 40px; object-fit: cover; border-radius: 6px; }
.no-poster { font-size: 12px; color: var(--slate-300); }
.upload-area { text-align: center; }
.upload-placeholder {
  width: 160px; height: 100px; border: 2px dashed var(--slate-200); border-radius: var(--radius);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; color: var(--slate-400); font-size: 12px; gap: 4px;
  transition: all var(--transition);
}
.upload-placeholder:hover { border-color: var(--blue-400); color: var(--blue-500); }
.poster-preview { width: 160px; height: 100px; object-fit: cover; border-radius: var(--radius); border: 1px solid var(--slate-200); }
</style>
