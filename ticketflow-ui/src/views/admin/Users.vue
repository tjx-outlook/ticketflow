<template>
  <div>
    <div class="page-header">
      <h2>用户管理</h2>
    </div>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索用户名/昵称" style="width:260px" clearable @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" class="modern-table">
      <el-table-column prop="id" label="ID" width="120" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="plain">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="userStore.hasRole('SUPER_ADMIN')" size="small" @click="handleAssignRole(row)">分配角色</el-button>
          <el-button v-if="userStore.hasRole('SUPER_ADMIN')" size="small" :type="row.status === 1 ? 'danger' : 'success'"
            @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="current" :page-size="size"
      :total="total" layout="total, prev, pager, next"
      @current-change="fetchData" style="margin-top:20px;justify-content:flex-end"
    />

    <!-- Assign Role Dialog -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="480px">
      <el-checkbox-group v-model="selectedRoleIds" class="role-checklist">
        <el-checkbox v-for="r in allRoles" :key="r.id" :value="r.id" :label="r.roleName" />
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssignRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '../../api/user'
import { roleApi } from '../../api/role'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const current = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')

const roleDialogVisible = ref(false)
const selectedRoleIds = ref([])
const allRoles = ref([])
const currentUserId = ref(null)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await userApi.getList({ current: current.value, size: size.value, keyword: keyword.value })
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

async function handleAssignRole(row) {
  currentUserId.value = row.id
  const [roles, userRoles] = await Promise.all([
    roleApi.getAll(),
    roleApi.getUserRoles(row.id)
  ])
  allRoles.value = roles
  selectedRoleIds.value = userRoles
  roleDialogVisible.value = true
}

async function submitAssignRole() {
  await roleApi.assignRoles(currentUserId.value, selectedRoleIds.value)
  ElMessage.success('角色分配成功')
  roleDialogVisible.value = false
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await userApi.updateStatus(row.id, { status: newStatus })
  ElMessage.success('状态更新成功')
  row.status = newStatus
}
</script>

<style scoped>
.modern-table { font-size: 13px; }

.role-checklist {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.role-checklist :deep(.el-checkbox) {
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--slate-100);
  margin-right: 0;
  transition: all var(--transition);
}

.role-checklist :deep(.el-checkbox:hover) {
  background: var(--slate-50);
  border-color: var(--slate-200);
}
</style>
