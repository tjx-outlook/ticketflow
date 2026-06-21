<template>
  <div>
    <div class="page-header">
      <h2>我的订单</h2>
    </div>

    <!-- Status filter tabs -->
    <div class="order-filters">
      <button
        v-for="f in statusFilters"
        :key="f.value"
        class="order-filter-chip"
        :class="{ active: statusFilter === f.value }"
        @click="statusFilter = f.value; fetchData()"
      >
        {{ f.label }}
        <span v-if="f.count !== null" class="filter-count">{{ f.count }}</span>
      </button>
    </div>

    <el-table :data="tableData" v-loading="loading" class="modern-table" empty-text="暂无订单">
      <el-table-column prop="orderNo" label="订单号" width="210" />
      <el-table-column prop="eventId" label="活动ID" width="90" />
      <el-table-column prop="totalAmount" label="金额" width="110">
        <template #default="{ row }">
          <span class="amount">¥{{ row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="seatCount" label="数量" width="70" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <span class="order-status" :class="'os-' + row.status">
            {{ orderStatusMap[row.status]?.label }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="payTime" label="支付时间" width="170">
        <template #default="{ row }">
          <span v-if="row.payTime">{{ row.payTime.replace('T', ' ') }}</span>
          <span v-else class="no-time">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170">
        <template #default="{ row }">{{ row.createTime?.replace('T', ' ') }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            type="primary" size="small" link
            @click="handlePay(row)"
          >去支付</el-button>
          <el-button
            v-if="row.status === 0"
            type="danger" size="small" link
            @click="handleCancel(row)"
          >取消订单</el-button>
          <el-button
            v-if="row.status === 1"
            type="warning" size="small" link
            @click="handleRefund(row)"
          >申请退款</el-button>
          <span v-if="row.status === 2" class="cancelled-text">已取消</span>
          <span v-if="row.status === 3" class="refunded-text">已退款</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="current" :page-size="size" :total="total"
      layout="total, prev, pager, next" @current-change="fetchData"
      style="margin-top:20px;justify-content:flex-end" />

    <!-- Payment QR Dialog -->
    <el-dialog v-model="payDialogVisible" :close-on-click-modal="false" width="420px" align-center
      @close="closePayDialog">
      <div class="pay-body">
        <template v-if="paySuccess">
          <div class="pay-success-icon">
            <svg width="56" height="56" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="11" fill="#dcfce7" stroke="#22c55e" stroke-width="1.5"/>
              <path d="M7 12l3.5 3.5L17 9" stroke="#22c55e" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h3 style="color:#22c55e;text-align:center">支付成功！</h3>
        </template>
        <template v-else>
          <h3 class="pay-title">扫码支付</h3>
          <div class="pay-method-label">
            <span>支付宝</span>
          </div>
          <div class="qr-wrapper">
            <div class="qr-code">
              <img v-if="payQrUrl" :src="payQrUrl" width="200" height="200" alt="支付二维码" />
              <div v-else class="qr-loading">生成二维码中...</div>
            </div>
            <p class="qr-hint">打开支付宝扫一扫</p>
          </div>
          <div class="pay-summary">
            <div class="pay-row"><span>订单号</span><span class="pay-total">{{ payOrderNo }}</span></div>
            <div class="pay-row"><span>金额</span><span class="pay-total">¥{{ payAmount }}</span></div>
          </div>
        </template>
      </div>
      <template #footer>
        <button class="pay-btn cancel" @click="closePayDialog">关闭</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { orderApi } from '../../api/order'
import { paymentApi } from '../../api/payment'
import { ElMessage, ElMessageBox } from 'element-plus'
import QRCode from 'qrcode'

const tableData = ref([])
const current = ref(1), size = ref(10), total = ref(0), loading = ref(false)
const statusFilter = ref('all')

const orderStatusMap = {
  0: { label: '待支付', type: 'warning' },
  1: { label: '已支付', type: 'success' },
  2: { label: '已取消', type: 'info' },
  3: { label: '已退款', type: 'danger' }
}

const statusFilters = [
  { label: '全部', value: 'all', count: null },
  { label: '待支付', value: '0', count: null },
  { label: '已支付', value: '1', count: null },
  { label: '已取消', value: '2', count: null },
  { label: '已退款', value: '3', count: null },
]

// ==================== 支付弹窗 ====================
const payDialogVisible = ref(false)
const paySuccess = ref(false)
const payQrUrl = ref('')
const payOrderNo = ref('')
const payAmount = ref(0)
let payPollTimer = null

onMounted(() => fetchData())
onUnmounted(() => { if (payPollTimer) clearInterval(payPollTimer) })

async function fetchData() {
  loading.value = true
  try {
    const params = { current: current.value, size: size.value }
    if (statusFilter.value !== 'all') params.status = Number(statusFilter.value)
    const res = await orderApi.getList(params)
    tableData.value = res.records; total.value = res.total
  } finally { loading.value = false }
}

async function handlePay(row) {
  paySuccess.value = false
  payQrUrl.value = ''
  payOrderNo.value = row.orderNo
  payAmount.value = row.totalAmount
  payDialogVisible.value = true

  // 1. 调用后端获取真实支付宝二维码
  try {
    const res = await paymentApi.create({
      orderNo: row.orderNo,
      amount: row.totalAmount.toString(),
      method: 'alipay'
    })
    // 2. 生成二维码图片
    payQrUrl.value = await QRCode.toDataURL(res.qrUrl, {
      width: 200, margin: 2,
      color: { dark: '#000', light: '#fff' }
    })
  } catch(e) {
    payQrUrl.value = ''
  }

  // 3. 启动轮询
  startPayPolling(row.orderNo)
}

function startPayPolling(orderNo) {
  if (payPollTimer) clearInterval(payPollTimer)
  payPollTimer = setInterval(async () => {
    if (paySuccess.value) { clearInterval(payPollTimer); return }
    try {
      const res = await paymentApi.getStatus(orderNo)
      if (res.status === 1) {
        paySuccess.value = true
        clearInterval(payPollTimer)
        ElMessage.success('🎉 支付成功！')
        payDialogVisible.value = false
        fetchData()
      }
    } catch(e) { /* retry */ }
  }, 3000)
}

function closePayDialog() {
  payDialogVisible.value = false
  if (payPollTimer) { clearInterval(payPollTimer); payPollTimer = null }
  fetchData()
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定取消该订单吗？取消后不可恢复。', '取消订单', {
      confirmButtonText: '确定取消',
      cancelButtonText: '返回',
      type: 'warning'
    })
    await orderApi.cancel(row.orderNo)
    ElMessage.success('订单已取消')
    fetchData()
  } catch { /* cancelled */ }
}

async function handleRefund(row) {
  try {
    await ElMessageBox.confirm('确定申请退款吗？退款后座位将释放。', '申请退款', {
      confirmButtonText: '确定退款',
      cancelButtonText: '返回',
      type: 'warning'
    })
    await orderApi.cancel(row.orderNo)
    ElMessage.success('退款申请已提交')
    fetchData()
  } catch { /* cancelled */ }
}
</script>

<style scoped>
.order-filters {
  display: flex; gap: 8px; margin-bottom: 20px; flex-wrap: wrap;
}

.order-filter-chip {
  padding: 7px 18px; border-radius: 20px;
  border: 1px solid var(--slate-200); background: var(--white);
  font-size: 13px; font-weight: 600; color: var(--slate-500);
  cursor: pointer; transition: all .18s; outline: none;
  display: flex; align-items: center; gap: 6px;
}
.order-filter-chip:hover { border-color: var(--blue-300); color: var(--blue-600); }
.order-filter-chip.active {
  background: var(--blue-600); border-color: var(--blue-600);
  color: #fff; box-shadow: 0 2px 8px rgba(37,99,235,.25);
}
.filter-count {
  background: rgba(255,255,255,.25); padding: 1px 7px;
  border-radius: 10px; font-size: 11px;
}

.modern-table { font-size: 13px; }

.amount { font-weight: 600; color: var(--slate-700); }

.order-status {
  display: inline-block; padding: 3px 12px; border-radius: 12px;
  font-size: 12px; font-weight: 600;
}
.os-0 { background: #fef3c7; color: #b45309; }  /* 待支付 - amber */
.os-1 { background: #dcfce7; color: #15803d; }  /* 已支付 - green */
.os-2 { background: #f1f5f9; color: #64748b; }  /* 已取消 - gray */
.os-3 { background: #fee2e2; color: #dc2626; }  /* 已退款 - red */

.no-time { color: var(--slate-300); }
.cancelled-text { font-size: 12px; color: var(--slate-400); }
.refunded-text { font-size: 12px; color: #ef4444; }

/* ===== Payment Dialog ===== */
.pay-body { text-align: center; padding: 8px 0; }
.pay-title { font-size: 20px; font-weight: 700; color: var(--slate-800); margin-bottom: 14px; }

.pay-method-label {
  display: flex; align-items: center; justify-content: center; gap: 8px;
  margin-bottom: 16px; font-size: 15px; font-weight: 600; color: #1677ff;
}

.pay-success-icon { margin-bottom: 8px; }

.qr-wrapper { display: flex; flex-direction: column; align-items: center; gap: 10px; margin-bottom: 16px; }
.qr-code {
  padding: 12px; background: #fff; border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,.08); display: inline-block; line-height: 0;
}
.qr-loading {
  width: 200px; height: 200px; display: flex; align-items: center;
  justify-content: center; color: var(--slate-400); font-size: 13px;
}
.qr-hint { font-size: 13px; font-weight: 500; color: #1677ff; }

.pay-summary {
  background: var(--slate-50); border-radius: 10px; padding: 12px 16px; text-align: left;
}
.pay-row { display: flex; justify-content: space-between; padding: 4px 0; font-size: 13px; color: var(--slate-600); }
.pay-total { font-size: 15px; font-weight: 700; color: #2563eb; }

.pay-btn {
  height: 42px; padding: 0 24px; border: none; border-radius: 10px;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: all .2s;
}
.pay-btn.cancel { background: var(--slate-100); color: var(--slate-500); }
.pay-btn.cancel:hover { background: var(--slate-200); }
.pay-btn.confirm {
  background: linear-gradient(135deg,#2563eb,#7c3aed);
  color: #fff; box-shadow: 0 4px 14px rgba(37,99,235,.3);
}
.pay-btn.confirm:hover {
  transform: translateY(-1px); box-shadow: 0 6px 20px rgba(37,99,235,.4);
}
</style>
