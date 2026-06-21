<template>
  <div>
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="handleAdd">新增角色</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" class="modern-table">
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="roleName" label="角色名称" width="160" />
      <el-table-column prop="roleCode" label="角色编码" width="160" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="handleAssignPerm(row)">分配权限</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="form.roleCode" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- Assign Permission Dialog -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="520px">
      <el-tree
        ref="permTreeRef"
        :data="permTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'permissionName', children: 'children' }"
        :default-checked-keys="checkedPermIds"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPerms">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { roleApi } from '../../api/role'
import { permissionApi } from '../../api/permission'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ roleName: '', roleCode: '', description: '', sort: 0 })
const editId = ref(null)

const permDialogVisible = ref(false)
const permTree = ref([])
const permTreeRef = ref(null)
const checkedPermIds = ref([])
const currentRoleId = ref(null)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await roleApi.getList({ current: 1, size: 100 })
    tableData.value = res.records
  } finally { loading.value = false }
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  form.value = { roleName: '', roleCode: '', description: '', sort: 0 }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

async function submitForm() {
  if (isEdit.value) {
    await roleApi.update(editId.value, form.value)
  } else {
    await roleApi.create(form.value)
  }
  ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
  dialogVisible.value = false
  fetchData()
}

async function handleAssignPerm(row) {
  currentRoleId.value = row.id
  const [perms, rolePerms] = await Promise.all([
    permissionApi.getTree(),
    permissionApi.getRolePermissions(row.id)
  ])
  permTree.value = buildTree(perms)
  checkedPermIds.value = rolePerms
  permDialogVisible.value = true
}

async function submitPerms() {
  const checked = permTreeRef.value.getCheckedKeys()
  const halfChecked = permTreeRef.value.getHalfCheckedKeys()
  const allIds = [...checked, ...halfChecked]
  await permissionApi.assignPermissions(currentRoleId.value, allIds)
  ElMessage.success('权限分配成功')
  permDialogVisible.value = false
}

function buildTree(perms) {
  const map = {}, roots = []
  perms.forEach(p => { map[p.id] = { ...p, children: [] } })
  perms.forEach(p => {
    if (p.parentId && map[p.parentId]) {
      map[p.parentId].children.push(map[p.id])
    } else {
      roots.push(map[p.id])
    }
  })
  return roots
}
</script>

<style scoped>
.modern-table { font-size: 13px; }
</style>
