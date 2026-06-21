<template>
  <div class="personal-center">
    <!-- Profile Header -->
    <section class="profile-hero">
      <div class="profile-avatar">
        <el-avatar :size="72" icon="UserFilled" />
      </div>
      <div class="profile-info">
        <h1 class="profile-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</h1>
        <div class="profile-roles">
          <el-tag
            v-for="role in userStore.roles"
            :key="role"
            size="small"
            effect="plain"
            type="info"
          >{{ role }}</el-tag>
        </div>
        <p class="profile-meta">
          <span v-if="userStore.userInfo?.email">{{ userStore.userInfo.email }}</span>
          <span v-if="userStore.userInfo?.username">@{{ userStore.userInfo.username }}</span>
        </p>
      </div>
      <div class="profile-action">
        <el-button @click="passwordDialogVisible = true" class="pwd-btn">
          <el-icon><Lock /></el-icon> 修改密码
        </el-button>
      </div>
    </section>

    <!-- Tabs: Orders & Tickets -->
    <div class="center-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane name="orders">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon> 我的订单
            </span>
          </template>

          <el-table :data="orderData" v-loading="orderLoading" class="modern-table">
            <el-table-column prop="orderNo" label="订单号" width="200" />
            <el-table-column prop="eventId" label="活动ID" width="90" />
            <el-table-column prop="totalAmount" label="金额" width="110">
              <template #default="{ row }">
                <span class="amount">¥{{ row.totalAmount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="seatCount" label="数量" width="70" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <span class="order-status" :class="'os-' + row.status">
                  {{ orderStatusMap[row.status]?.label }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="payTime" label="支付时间" width="170">
              <template #default="{ row }">{{ formatTime(row.payTime) }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" min-width="160">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 0"
                  type="primary" size="small" link
                  @click="handlePayOrder(row)"
                >重新支付</el-button>
                <el-button
                  v-if="row.status === 0"
                  type="danger" size="small" link
                  @click="handleCancelOrder(row)"
                >取消订单</el-button>
                <el-button
                  v-if="row.status === 1"
                  type="warning" size="small" link
                  @click="handleRefundOrder(row)"
                >申请退款</el-button>
                <span v-else-if="row.status === 2" class="cancelled-text">已取消</span>
                <span v-else-if="row.status === 3" class="refunded-text">已退款</span>
                <span v-else-if="row.status === 1" class="paid-text">—</span>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-pagination">
            <el-pagination
              v-model:current-page="orderPage" :page-size="orderSize" :total="orderTotal"
              layout="total, prev, pager, next" @current-change="fetchOrders" />
          </div>
        </el-tab-pane>

        <el-tab-pane name="tickets">
          <template #label>
            <span class="tab-label">
              <el-icon><Ticket /></el-icon> 我的票
            </span>
          </template>

          <div class="ticket-grid">
            <div v-for="ticket in ticketData" :key="ticket.id" class="ticket-card">
              <div class="ticket-glow"></div>
              <div class="ticket-inner">
                <div class="ticket-top">
                  <span class="ticket-no">NO.{{ ticket.ticketNo }}</span>
                  <span class="ticket-status" :class="ticket.status === 0 ? 'ts-valid' : 'ts-used'">
                    {{ ticket.status === 0 ? '未使用' : '已使用' }}
                  </span>
                </div>
                <div class="ticket-body">
                  <div class="ticket-field">
                    <span class="field-label">活动ID</span>
                    <span class="field-value">{{ ticket.eventId }}</span>
                  </div>
                  <div class="ticket-field">
                    <span class="field-label">座位ID</span>
                    <span class="field-value">{{ ticket.seatId }}</span>
                  </div>
                  <div class="ticket-field">
                    <span class="field-label">订单号</span>
                    <span class="field-value">{{ ticket.orderNo }}</span>
                  </div>
                </div>
                <div class="ticket-qr">
                  <code>{{ ticket.qrCode }}</code>
                </div>
                <div class="ticket-time">{{ formatTime(ticket.createTime) }}</div>
              </div>
            </div>
          </div>

          <div class="table-pagination">
            <el-pagination
              v-model:current-page="ticketPage" :page-size="ticketSize" :total="ticketTotal"
              layout="total, prev, pager, next" @current-change="fetchTickets" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Change Password Dialog -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="440px" :close-on-click-modal="false" class="pwd-dialog">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="6-20位新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="handleUpdatePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { orderApi } from '../../api/order'
import { ticketApi } from '../../api/ticket'
import { userApi } from '../../api/user'

const userStore = useUserStore()
const activeTab = ref('orders')

// Orders
const orderData = ref([])
const orderPage = ref(1), orderSize = ref(10), orderTotal = ref(0), orderLoading = ref(false)

const orderStatusMap = {
  0: { label: '待支付', type: 'warning' },
  1: { label: '已支付', type: 'success' },
  2: { label: '已取消', type: 'info' },
  3: { label: '已退款', type: 'danger' }
}

function formatTime(str) {
  if (!str) return ''
  return str.replace('T', ' ')
}

async function fetchOrders() {
  orderLoading.value = true
  try {
    const res = await orderApi.getList({ current: orderPage.value, size: orderSize.value })
    orderData.value = res.records; orderTotal.value = res.total
  } finally { orderLoading.value = false }
}

async function handlePayOrder(row) {
  try {
    await orderApi.confirmPay(row.id)
    ElMessage.success('支付成功！')
    fetchOrders()
  } catch(e) { /* error handled by interceptor */ }
}

async function handleCancelOrder(row) {
  try {
    await ElMessageBox.confirm('确定取消该订单吗？取消后不可恢复。', '取消订单', {
      confirmButtonText: '确定取消',
      cancelButtonText: '返回',
      type: 'warning'
    })
    await orderApi.cancel(row.id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch { /* cancelled */ }
}

async function handleRefundOrder(row) {
  try {
    await ElMessageBox.confirm('确定申请退款吗？退款后座位将释放。', '申请退款', {
      confirmButtonText: '确定退款',
      cancelButtonText: '返回',
      type: 'warning'
    })
    await orderApi.cancel(row.id)
    ElMessage.success('退款申请已提交')
    fetchOrders()
  } catch { /* cancelled */ }
}

// Tickets
const ticketData = ref([])
const ticketPage = ref(1), ticketSize = ref(12), ticketTotal = ref(0)

async function fetchTickets() {
  const res = await ticketApi.getList({ current: ticketPage.value, size: ticketSize.value })
  ticketData.value = res.records; ticketTotal.value = res.total
}

// Password
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (rule, value, cb) => {
  if (value !== passwordForm.value.newPassword) cb(new Error('两次输入的密码不一致'))
  else cb()
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleUpdatePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  passwordLoading.value = true
  try {
    await userApi.updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordDialogVisible.value = false
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    setTimeout(() => { userStore.logout(); window.location.href = '/login' }, 1500)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '修改失败')
  } finally { passwordLoading.value = false }
}

onMounted(() => { fetchOrders(); fetchTickets() })
</script>

<style scoped>
.personal-center { max-width: 1100px; margin: 0 auto; }

/* ===== Profile Hero ===== */
.profile-hero {
  display: flex;
  align-items: center;
  gap: 24px;
  background: var(--white);
  border-radius: var(--radius-xl);
  padding: 32px 36px;
  border: 1px solid var(--slate-100);
  margin-bottom: 24px;
}

.profile-avatar :deep(.el-avatar) {
  box-shadow: 0 4px 16px rgba(0,0,0,.08);
}

.profile-info { flex: 1; }

.profile-name {
  font-size: 22px;
  font-weight: 700;
  color: var(--slate-800);
  margin-bottom: 8px;
}

.profile-roles { display: flex; gap: 6px; margin-bottom: 6px; }

.profile-meta {
  font-size: 13px;
  color: var(--slate-400);
  display: flex;
  gap: 12px;
}

.profile-action { flex-shrink: 0; }

.pwd-btn {
  border-radius: var(--radius);
  font-weight: 500;
  border: 1px solid var(--slate-200);
  color: var(--slate-600);
  transition: all var(--transition);
}

.pwd-btn:hover {
  border-color: var(--blue-400);
  color: var(--blue-600);
  background: var(--blue-50);
}

/* ===== Tabs ===== */
.center-tabs {
  background: var(--white);
  border-radius: var(--radius-xl);
  border: 1px solid var(--slate-100);
  padding: 8px 24px 24px;
}

.center-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
}

.center-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
  height: 48px;
  line-height: 48px;
}

.tab-label { display: flex; align-items: center; gap: 6px; }

/* Table */
.modern-table { font-size: 13px; }

.amount { font-weight: 600; color: var(--slate-700); }

.order-status {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.os-0 { background: #fef3c7; color: #b45309; }
.os-1 { background: #dcfce7; color: var(--green-700); }
.os-2 { background: var(--slate-100); color: var(--slate-500); }
.os-3 { background: #fee2e2; color: #dc2626; }

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* ===== Ticket Cards ===== */
.ticket-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.ticket-card {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.ticket-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, #2563eb 0%, #1d4ed8 30%, #1e40af 100%);
  border-radius: var(--radius-lg);
}

.ticket-inner {
  position: relative;
  padding: 22px;
  color: #fff;
}

.ticket-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.ticket-no { font-size: 11px; opacity: .7; font-family: monospace; letter-spacing: 0.5px; }

.ticket-status {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.ts-valid { background: rgba(255,255,255,.2); }
.ts-used { background: rgba(255,255,255,.1); opacity: .6; }

.ticket-body { margin-bottom: 14px; }

.ticket-field {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 13px;
}

.field-label { opacity: .6; font-size: 12px; }
.field-value { font-weight: 500; font-family: monospace; }

.ticket-qr {
  background: rgba(255,255,255,.12);
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 12px;
  text-align: center;
}

.ticket-qr code {
  font-size: 10px;
  word-break: break-all;
  opacity: .8;
}

.ticket-time { font-size: 11px; opacity: .5; text-align: right; }

/* ===== Order status text ===== */
.cancelled-text { font-size: 12px; color: var(--slate-400); }
.refunded-text { font-size: 12px; color: #ef4444; }
.paid-text { font-size: 12px; color: var(--slate-300); }

/* ===== Dialog ===== */
.pwd-dialog :deep(.el-form-item__label) {
  font-weight: 500;
  font-size: 13px;
  color: var(--slate-600);
}
</style>
