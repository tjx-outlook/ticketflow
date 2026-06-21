<template>
  <div v-loading="loading" class="event-detail" :style="{ background: 'transparent' }">
    <!-- ===== Top Bar ===== -->
    <header class="top-bar">
      <div class="tb-left">
        <el-breadcrumb separator="›">
          <el-breadcrumb-item :to="{ path: '/events' }">活动列表</el-breadcrumb-item>
          <el-breadcrumb-item class="tb-current">{{ event?.eventName }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="tb-center">
        <span class="tb-pill"><el-icon><Clock /></el-icon> {{ event?.startTime?.replace('T', ' ') }}</span>
        <span class="tb-pill"><el-icon><Location /></el-icon> {{ event?.venue }}</span>
      </div>
      <div class="tb-right">
        <template v-if="selectedSeats.length">
          <span class="sel-badge">{{ selectedSeats.length }} 张已选</span>
          <span class="sel-price">¥{{ selectedTotalPrice }}</span>
        </template>
        <span v-else class="tb-hint">点击座位进行选择</span>
      </div>
    </header>

    <!-- ===== Main Layout ===== -->
    <div class="arena-layout" v-if="standRows.length">
      <!-- SVG Arena -->
      <div class="arena-stage-wrap">
        <svg :viewBox="`0 0 ${standViewW} ${standViewH}`" class="arena-svg" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <filter id="ss">
              <feDropShadow dx="0" dy="1" stdDeviation="1" flood-opacity="0.08"/>
            </filter>
          </defs>

          <!-- Stage -->
          <rect :x="standViewW/2-180" y="8" width="360" height="34" rx="8"
            fill="#e2e8f0" stroke="#cbd5e1" stroke-width="1"/>
          <text :x="standViewW/2" y="30" text-anchor="middle" font-size="14" font-weight="700"
            fill="#64748b" letter-spacing="6">舞 台</text>

          <!-- Zone labels -->
          <text x="40" y="88" font-size="11" font-weight="700" fill="#d97706" opacity="0.6">VIP</text>
          <line x1="55" y1="94" x2="55" y2="128" stroke="#d97706" stroke-width="1" opacity="0.25"/>

          <text x="40" y="176" font-size="11" font-weight="700" fill="#16a34a" opacity="0.6">内场</text>
          <line x1="55" y1="182" x2="55" y2="272" stroke="#16a34a" stroke-width="1" opacity="0.25"/>

          <text x="40" y="334" font-size="11" font-weight="700" fill="#2563eb" opacity="0.6">看台</text>
          <line x1="55" y1="340" x2="55" y2="430" stroke="#2563eb" stroke-width="1" opacity="0.25"/>

          <!-- Zone dividers (horizontal dashed lines) -->
          <line :x1="70" :y1="132" :x2="standViewW-30" :y2="132" stroke="#e2e8f0" stroke-width="1" stroke-dasharray="4 6"/>
          <line :x1="70" :y1="284" :x2="standViewW-30" :y2="284" stroke="#e2e8f0" stroke-width="1" stroke-dasharray="4 6"/>

          <!-- Row labels + seats -->
          <g v-for="(row, ri) in standRows" :key="ri">
            <text :x="30" :y="row.y+17" text-anchor="middle" font-size="15" font-weight="700" fill="#475569">{{ ri+1 }}</text>
            <text :x="30" :y="row.y+30" text-anchor="middle" font-size="7" fill="#94a3b8">排</text>

            <g v-for="(s, si) in row.seats" :key="si">
              <rect :x="s.x" :y="row.y" width="24" height="24" rx="4"
                :fill="standSeatFill(s)" :stroke="standSeatStroke(s)" stroke-width="1.5"
                filter="url(#ss)" :class="{ clickable: s.status===0 && !isSeatSelected(s.id) }"
                @click="s.status===0 ? onSeatClick(s) : null"
              />
            </g>
          </g>
        </svg>

        <!-- Tooltip -->
        <div v-if="hoverSeat" class="seat-tooltip"
          :style="{ left: (hoverX || 0) + 'px', top: (hoverY || 0) - 40 + 'px' }">
          {{ hoverSeat.section }} · {{ hoverSeat.row }}排{{ hoverSeat.col }}号
          <span class="tt-price">¥{{ hoverSeat.price }}</span>
        </div>
      </div>

      <!-- ===== Right Panel ===== -->
      <aside class="side-panel">
        <!-- Beijing Time -->
        <div class="sp-card clock-card">
          <div class="cd-header">
            <el-icon><Clock /></el-icon>
            <span>北京时间</span>
          </div>
          <div class="cd-time">{{ beijingTime }}</div>
          <div class="cd-date">{{ beijingDate }}</div>
        </div>

        <!-- Section filter -->
        <div class="sp-card">
          <h4 class="sp-title">区域筛选</h4>
          <div class="chip-row">
            <button v-for="z in ['全部','VIP','内场','看台']" :key="z"
              class="chip" :class="{ on: zoneFilter===z }"
              @click="zoneFilter = z">{{ z }}</button>
          </div>
        </div>

        <!-- Status filter -->
        <div class="sp-card">
          <h4 class="sp-title">状态</h4>
          <div class="chip-row">
            <button v-for="st in ['全部','可选','已售']" :key="st"
              class="chip" :class="{ on: statusFilter===st }"
              @click="statusFilter = st">{{ st }}</button>
          </div>
        </div>

        <!-- Legend -->
        <div class="sp-card">
          <h4 class="sp-title">图例</h4>
          <div class="legend-grid">
            <div class="lg"><span class="ld ld-vip"></span>VIP ¥580</div>
            <div class="lg"><span class="ld ld-inner"></span>内场 ¥380</div>
            <div class="lg"><span class="ld ld-stand"></span>看台 ¥180</div>
            <div class="lg"><span class="ld ld-sold"></span>已售</div>
            <div class="lg"><span class="ld ld-selected"></span>已选</div>
          </div>
        </div>

        <!-- Selected seats -->
        <div class="sp-card sp-summary">
          <h4 class="sp-title">已选座位</h4>
          <div v-if="selectedSeats.length" class="sel-list">
            <div v-for="s in selectedSeats" :key="s.id" class="sel-row">
              <div class="sr-info">
                <span class="sr-pos">{{ s.section }} {{ s.row }}排{{ s.col }}号</span>
                <span class="sr-price">¥{{ s.price }}</span>
              </div>
              <button class="sr-rm" @click="onSeatClick(s)">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            <div class="sel-total">
              <span>合计 {{ selectedSeats.length }} 张</span>
              <span class="st-price">¥{{ selectedTotalPrice }}</span>
            </div>
          </div>
          <div v-else class="sel-empty">
            <el-icon size="20"><InfoFilled /></el-icon>
            <span>点击左侧座位进行选择<br/>最多可选 {{ MAX_SEATS }} 个</span>
          </div>
        </div>

        <!-- Buy button -->
        <button class="buy-btn" :disabled="event?.status !== 1 || !selectedSeats.length || seckilling"
          @click="handleBuy">
          <span v-if="seckilling" class="buy-loading">
            <el-icon class="loading-spin"><Loading /></el-icon> 处理中...
          </span>
          <span v-else>{{ buyText }}</span>
        </button>

        <!-- Result -->
        <div v-if="seckillResult" class="buy-msg" :class="seckillResult.success?'ok':'fail'">
          {{ seckillResult.message }}</div>
      </aside>
    </div>

    <!-- Login Required Dialog -->
    <el-dialog v-model="loginDialogVisible" :close-on-click-modal="false" width="400px" align-center
      class="login-dialog-dark">
      <div class="login-req-body">
        <div class="lr-icon">
          <el-icon size="40"><WarningFilled /></el-icon>
        </div>
        <h3>当前账号未登录</h3>
        <p>所选座位将为您锁定 10 分钟，请尽快登录完成支付</p>
        <el-button type="primary" size="large" style="width:100%;height:44px;font-weight:600" @click="goToLogin">
          去登录（已锁定 {{ selectedSeats.length }} 个座位）
        </el-button>
      </div>
    </el-dialog>

    <!-- Payment QR Dialog -->
    <el-dialog v-model="paymentDialogVisible" :close-on-click-modal="false" width="440px" align-center
      class="payment-dialog-dark" @close="cancelPayment">
      <div class="payment-body">
        <!-- 支付成功状态 -->
        <template v-if="paymentSuccess">
          <div class="pay-success-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="11" fill="#dcfce7" stroke="#22c55e" stroke-width="1.5"/>
              <path d="M7 12l3.5 3.5L17 9" stroke="#22c55e" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h3 class="pay-title" style="color:#22c55e">支付成功！</h3>
          <p class="pay-desc">门票已出票，可在「我的订单」中查看</p>
        </template>

        <!-- 正常支付状态 -->
        <template v-else>
          <h3 class="pay-title">扫码支付</h3>

          <!-- Payment method -->
          <div class="pay-method-label">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="#1677ff"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15l-4-4 1.41-1.41L11 14.17l5.59-5.59L18 10l-7 7z"/></svg>
            <span>支付宝支付</span>
          </div>

          <!-- QR Code -->
          <div class="qr-wrapper">
            <div class="qr-code qr-alipay">
              <img v-if="qrDataUrl" :src="qrDataUrl" width="200" height="200" alt="支付二维码" />
              <div v-else class="qr-loading">生成二维码中...</div>
            </div>
            <p class="qr-hint">打开支付宝扫一扫</p>
          </div>

          <!-- Order summary -->
          <div class="pay-summary">
            <div class="pay-row"><span>支付方式</span><span style="color:#1677ff;font-weight:600">支付宝</span></div>
            <div class="pay-row"><span>座位</span><span>{{ selectedSeats.length }} 张</span></div>
            <div class="pay-row"><span>金额</span><span class="pay-total">¥{{ selectedTotalPrice }}</span></div>
            <div class="pay-row"><span>剩余时间</span><span class="pay-countdown">{{ payCountdown }}</span></div>
          </div>
        </template>
      </div>
      <template #footer>
        <button class="pay-btn cancel" @click="cancelPayment">取消支付</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { eventApi } from '../../api/event'
import { seatApi } from '../../api/seat'
import { orderApi } from '../../api/order'
import { ElMessage } from 'element-plus'
import { paymentApi } from '../../api/payment'
import QRCode from 'qrcode'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const event = ref(null)
const apiSeats = ref([])
const loading = ref(false)
const seckilling = ref(false)
const seckillResult = ref(null)
const loginDialogVisible = ref(false)
const paymentDialogVisible = ref(false)
const payMethod = ref('alipay')
const qrDataUrl = ref('')
const MAX_SEATS = 5
const selectedSeats = reactive([])
const zoneFilter = ref('全部')
const statusFilter = ref('全部')
const hoverSeat = ref(null)
const hoverX = ref(0)
const hoverY = ref(0)
const lockedSeatIds = reactive(new Set())
const paymentPollTimer = ref(null)
const paymentSuccess = ref(false)
const selectedTotalPrice = computed(() => selectedSeats.reduce((s, x) => s + (x.price || 0), 0))
function isSeatSelected(id) { return selectedSeats.some(s => s.id === id) }
function onSeatClick(seat) {
  if (seat.status === 'sold' || seat.status === 'locked') return
  const idx = selectedSeats.findIndex(s => s.id === seat.id)
  if (idx >= 0) { selectedSeats.splice(idx, 1) }
  else if (selectedSeats.length < MAX_SEATS) { selectedSeats.push({ ...seat }) }
}
const buyText = computed(() => {
  if (!event.value) return '加载中...'
  if (event.value.status === 0) return '暂未开售'
  if (event.value.status === 2) return '已售罄'
  if (!selectedSeats.length) return '请选择座位后下单'
  return `确认购票 · ¥${selectedTotalPrice.value}`
})
function goToLogin() {
  // Lock selected seats and persist to localStorage
  selectedSeats.forEach(s => lockedSeatIds.add(s.id))
  const lockData = { ids: [...lockedSeatIds], expiry: Date.now() + 600000 }
  localStorage.setItem('ticketflow_locked_seats', JSON.stringify(lockData))
  loginDialogVisible.value = false
  router.push('/login')
}
function seatFill(s) {
  if (isSeatSelected(s.id)) return '#22c55e'
  if (lockedSeatIds.has(s.id) || s.status === 'locked') return '#fbbf24'
  if (s.status === 'sold') return '#ef4444'
  return s._tint || '#3b82f6'
}
function seatStroke(s) {
  if (isSeatSelected(s.id)) return '#16a34a'
  if (lockedSeatIds.has(s.id) || s.status === 'locked') return '#d97706'
  if (s.status === 'sold') return '#b91c1c'
  return s._stroke || '#2563eb'
}

// Beijing time
const beijingTime = ref('')
const beijingDate = ref('')
let clockInterval = null
function updateBeijingTime() {
  const now = new Date()
  beijingTime.value = now.toLocaleTimeString('zh-CN', { hour12: false, timeZone: 'Asia/Shanghai' })
  beijingDate.value = now.toLocaleDateString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit', weekday:'short', timeZone: 'Asia/Shanghai' })
}
onMounted(() => {
  fetchData()
  // Restore locked seats from localStorage
  try {
    const saved = JSON.parse(localStorage.getItem('ticketflow_locked_seats') || '{}')
    if (saved.ids && saved.expiry && Date.now() < saved.expiry) {
      saved.ids.forEach(id => lockedSeatIds.add(id))
    } else {
      localStorage.removeItem('ticketflow_locked_seats')
    }
  } catch(e) { localStorage.removeItem('ticketflow_locked_seats') }

  updateBeijingTime()
  clockInterval = setInterval(updateBeijingTime, 1000)
})
onUnmounted(() => {
  if (clockInterval) clearInterval(clockInterval)
  stopPaymentPolling()
  if (payTimer) clearInterval(payTimer)
})

// ==================== Fan-Sector Semicircle Layout ====================
// Stand layout with VIP / 内场 / 看台 zones
const standViewW = 920, standViewH = 460
const seatW = 26, seatGapX = 8

const STAND_ZONES = [
  { label: 'VIP',  rows: 2, perRow: [6, 8],  y0: 60,  gap: 36, tint: '#fef3c7', stroke: '#d97706', price: 580 },
  { label: '内场', rows: 4, perRow: [10,12,14,16], y0: 142, gap: 36, tint: '#dcfce7', stroke: '#16a34a', price: 380 },
  { label: '看台', rows: 5, perRow: [14,16,18,20,20], y0: 296, gap: 36, tint: '#eff6ff', stroke: '#2563eb', price: 180 },
]

const standRows = computed(() => {
  const rows = []
  const all = apiSeats.value || []
  let globalRi = 0, sid = 1

  STAND_ZONES.forEach(zone => {
    if (zoneFilter.value !== '全部' && zoneFilter.value !== zone.label) return
    for (let ri = 0; ri < zone.rows; ri++) {
      const n = zone.perRow[ri] || 10
      const rowW = n * (seatW + seatGapX)
      const startX = standViewW/2 - rowW/2 + seatGapX/2
      const seats = []
      for (let ci = 0; ci < n; ci++) {
        const real = all.find(s => Number(s.rowNum) === globalRi + 1 && Number(s.seatNum) === ci + 1)
        let st = real?.status ?? (Math.random() < 0.06 ? 2 : Math.random() < 0.08 ? 1 : 0)
        if (statusFilter.value === '可选') st = (st === 0 ? 0 : -1)
        if (statusFilter.value === '已售') st = (st === 2 ? 2 : -1)
        if (st === -1) { /* skip filtered seat */ }
        else {
          seats.push({
            id: real?.id || `S${sid++}`,
            x: startX + ci * (seatW + seatGapX),
            status: st,
            price: real?.price || zone.price,
            section: real?.sectionName || zone.label,
            rowNum: globalRi + 1, col: ci + 1,
            _tint: zone.tint, _stroke: zone.stroke
          })
        }
      }
      if (seats.length > 0) {
        rows.push({ y: zone.y0 + ri * zone.gap, seats, zone: zone.label })
      }
      globalRi++
    }
  })
  return rows
})

function standSeatFill(s) {
  if (isSeatSelected(s.id)) return '#22c55e'
  if (s.status === 1) return '#fbbf24'
  if (s.status === 2) return '#fecaca'
  return s._tint || '#fff'
}
function standSeatStroke(s) {
  if (isSeatSelected(s.id)) return '#16a34a'
  if (s.status === 1) return '#f59e0b'
  if (s.status === 2) return '#ef4444'
  return s._stroke || '#cbd5e1'
}

async function fetchData() {
  loading.value = true
  try {
    const id = route.params.id
    const [eventData, seatData] = await Promise.all([
      eventApi.getDetail(id), seatApi.getByEvent(id)
    ])
    event.value = eventData
    apiSeats.value = seatData || []
  } finally { loading.value = false }
}

// ==================== 支付流程 ====================
const payCountdown = ref('10:00')
const pendingOrderId = ref(null)
let payTimer = null
let payRemainingSec = 600

// Step 1: 点击购票 → 秒杀创建订单 → 调用支付宝预下单 → 展示真实二维码 → 轮询支付状态
async function handleBuy() {
  seckillResult.value = null
  paymentSuccess.value = false
  if (!userStore.isLoggedIn) {
    selectedSeats.forEach(s => lockedSeatIds.add(s.id))
    const lockData = { ids: [...lockedSeatIds], expiry: Date.now() + 600000 }
    localStorage.setItem('ticketflow_locked_seats', JSON.stringify(lockData))
    loginDialogVisible.value = true
    return
  }
  if (!selectedSeats.length) return

  // 检查是否已购买过该活动
  if (event.value?.id) {
    try {
      const paid = await orderApi.checkPaid(event.value.id)
      if (paid === true) {
        ElMessage.warning('每人每场限成功购票1单，您已购买过该活动')
        return
      }
    } catch(e) { /* ignore */ }
  }

  seckilling.value = true
  try {
    // 1. 秒杀抢票 → 创建待支付订单
    // 只传真正的数字ID（数据库座位），过滤掉前端生成的假ID（S1, S2...）
    const seatIds = selectedSeats.map(s => s.id).filter(id => !isNaN(Number(id)))
    if (event.value?.id) {
      const res = await orderApi.seckill(event.value.id, seatIds.length ? seatIds : null)
      pendingOrderId.value = res.orderNo
    }

    // 2. 锁定座位 UI 状态
    selectedSeats.forEach(s => { s.status = 'locked'; lockedSeatIds.add(s.id) })

    // 3. 调用后端创建支付宝预下单 → 获取真实 QR 码
    let qrUrl = ''
    try {
      const payRes = await paymentApi.create({
        orderNo: pendingOrderId.value,
        amount: selectedTotalPrice.value.toString(),
        method: payMethod.value
      })
      qrUrl = payRes.qrUrl
    } catch(e) {
      // payment create 失败时降级为前端本地 URL
      const base = window.location.origin
      qrUrl = `${base}/pay?order=${pendingOrderId.value}&amount=${selectedTotalPrice.value}&method=${payMethod.value}`
    }

    // 4. 生成二维码图片
    try {
      qrDataUrl.value = await QRCode.toDataURL(qrUrl, { width: 200, margin: 2, color: { dark: '#000', light: '#fff' } })
    } catch(e) { qrDataUrl.value = '' }

    // 5. 启动倒计时
    payRemainingSec = 600; payCountdown.value = '10:00'
    if (payTimer) clearInterval(payTimer)
    payTimer = setInterval(() => {
      payRemainingSec--
      if (payRemainingSec < 0) { payRemainingSec = 0; clearInterval(payTimer); stopPaymentPolling() }
      payCountdown.value = `${String(Math.floor(payRemainingSec/60)).padStart(2,'0')}:${String(payRemainingSec%60).padStart(2,'0')}`
    }, 1000)

    paymentDialogVisible.value = true

    // 6. 启动支付状态轮询（每3秒查询一次）
    startPaymentPolling()

  } catch(e) {
    ElMessage.error(e.message || '下单失败')
  } finally {
    seckilling.value = false
  }
}

// 轮询支付状态 → 检测到已支付自动关闭弹窗
function startPaymentPolling() {
  stopPaymentPolling()
  paymentPollTimer.value = setInterval(async () => {
    if (!pendingOrderId.value || paymentSuccess.value) {
      stopPaymentPolling()
      return
    }
    try {
      const statusRes = await paymentApi.getStatus(pendingOrderId.value)
      if (statusRes.status === 1) {
        // 支付成功！
        stopPaymentPolling()
        paymentSuccess.value = true
        ElMessage.success('🎉 支付成功！')
        seckillResult.value = { success: true, message: '🎉 支付成功！' }
        selectedSeats.forEach(s => { s.status = 'sold'; lockedSeatIds.delete(s.id) })
        selectedSeats.length = 0
        if (payTimer) { clearInterval(payTimer); payTimer = null }
        localStorage.removeItem('ticketflow_locked_seats')
        // 关闭弹窗并跳转到订单页
        paymentDialogVisible.value = false
        pendingOrderId.value = null
        router.push('/my-orders')
      }
    } catch(e) { /* 轮询失败静默重试 */ }
  }, 3000)
}

function stopPaymentPolling() {
  if (paymentPollTimer.value) {
    clearInterval(paymentPollTimer.value)
    paymentPollTimer.value = null
  }
}

// Step 2: 取消支付 → 解锁座位 + 取消订单
function cancelPayment() {
  stopPaymentPolling()
  paymentDialogVisible.value = false
  if (payTimer) { clearInterval(payTimer); payTimer = null }
  selectedSeats.forEach(s => lockedSeatIds.delete(s.id))
  localStorage.removeItem('ticketflow_locked_seats')
  if (pendingOrderId.value) {
    orderApi.cancel(pendingOrderId.value).catch(() => {})
    pendingOrderId.value = null
  }
}

</script>

<style scoped>
/* ===== Dark Theme Base ===== */
.event-detail {
  max-width: 1360px; margin: 0 auto; padding: 8px;
  min-height: calc(100vh - 60px);
  background: linear-gradient(160deg, #0a0e1a 0%, #111827 30%, #0f172a 60%, #0c0f1d 100%);
  position: relative;
}
.event-detail::before {
  content: ''; position: fixed; inset: 0; z-index: -1; pointer-events: none;
  background:
    radial-gradient(ellipse at 30% 50%, rgba(59,130,246,0.06) 0%, transparent 60%),
    radial-gradient(ellipse at 70% 40%, rgba(139,92,246,0.05) 0%, transparent 55%),
    radial-gradient(ellipse at 50% 80%, rgba(245,158,11,0.04) 0%, transparent 50%);
}

/* ===== Top Bar ===== */
.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 24px; margin-bottom: 16px;
  background: rgba(15,23,42,0.7); border: 1px solid rgba(148,163,184,0.15);
  border-radius: 16px; backdrop-filter: blur(16px);
}
.tb-left{flex-shrink:0}
.tb-left :deep(.el-breadcrumb__inner){color:rgba(255,255,255,0.6)!important}
.tb-left :deep(.el-breadcrumb__inner:hover){color:rgba(255,255,255,0.9)!important}
.tb-current :deep(.el-breadcrumb__inner){color:rgba(255,255,255,0.9)!important;font-weight:600}
.tb-center{display:flex;gap:10px}
.tb-pill{
  display:flex;align-items:center;gap:5px;font-size:12px;
  color:rgba(255,255,255,0.6);font-weight:500;padding:5px 14px;
  background:rgba(255,255,255,0.06);border-radius:20px;
  border:1px solid rgba(255,255,255,0.08);
}
.tb-right{text-align:right}
.sel-badge{
  font-size:12px;color:#22c55e;font-weight:600;
  background:rgba(34,197,94,0.15);padding:3px 10px;border-radius:12px;margin-right:8px;
}
.sel-price{font-size:24px;font-weight:700;color:#fff}
.tb-hint{font-size:13px;color:rgba(255,255,255,0.35)}

/* ===== Layout ===== */
.arena-layout{display:flex;gap:16px;align-items:flex-start}

/* ===== SVG Arena ===== */
.arena-stage-wrap{
  flex:1;min-width:0;position:relative;
  background:rgba(15,23,42,0.6);border-radius:20px;
  border:1px solid rgba(148,163,184,0.12);
  overflow:hidden;backdrop-filter:blur(8px);
  contain:layout style;
}
.arena-svg{display:block;width:100%;height:auto;user-select:none}

/* Seat styles */
.seat-g{cursor:default}
.seat-g.clickable{cursor:pointer}
.seat-g.clickable:hover rect{filter:brightness(1.4) drop-shadow(0 0 5px rgba(255,255,255,0.6))!important}
.seat-hovered{filter:brightness(1.5)!important}
.hit-area{pointer-events:all}
.selected-ring{animation:pulse-ring 2s ease-in-out infinite}
@keyframes pulse-ring{
  0%,100%{opacity:.7;r:9}
  50%{opacity:.3;r:11}
}

/* Tooltip */
.seat-tooltip{
  position:absolute;pointer-events:none;z-index:50;
  padding:6px 12px;background:rgba(15,23,42,0.95);
  border:1px solid rgba(148,163,184,0.3);border-radius:8px;
  font-size:11px;color:#fff;white-space:nowrap;
  box-shadow:0 8px 24px rgba(0,0,0,.5);
  transform:translate(-50%,-120%);
  will-change:left,top;
}
.tt-price{color:#22c55e;font-weight:700;margin-left:6px}

/* ===== Side Panel ===== */
.side-panel{
  width:260px;flex-shrink:0;display:flex;flex-direction:column;
  gap:10px;position:sticky;top:20px;
}
.sp-card{
  background:rgba(15,23,42,0.7);border-radius:14px;
  border:1px solid rgba(148,163,184,0.12);padding:14px 16px;
  backdrop-filter:blur(12px);
}
.sp-title{
  font-size:11px;font-weight:600;color:rgba(255,255,255,0.4);
  margin-bottom:10px;text-transform:uppercase;letter-spacing:1px;
}

/* Beijing clock */
.clock-card{text-align:center}
.cd-header{display:flex;align-items:center;justify-content:center;gap:6px;font-size:12px;color:rgba(255,255,255,0.4);margin-bottom:4px}
.cd-time{font-size:32px;font-weight:700;color:#fff;font-variant-numeric:tabular-nums;letter-spacing:2px}
.cd-date{font-size:11px;color:rgba(255,255,255,0.35);margin-top:2px}

/* Chip */
.chip-row{display:flex;flex-wrap:wrap;gap:6px}
.chip{
  padding:6px 15px;border-radius:16px;
  border:1px solid rgba(148,163,184,0.2);background:rgba(255,255,255,0.04);
  font-size:11px;font-weight:600;color:rgba(255,255,255,0.55);
  cursor:pointer;transition:all .18s;outline:none;
}
.chip:hover{border-color:rgba(59,130,246,0.5);color:#60a5fa;background:rgba(59,130,246,0.1)}
.chip.on{
  background:rgba(59,130,246,0.25);border-color:rgba(59,130,246,0.6);
  color:#93c5fd;box-shadow:0 0 12px rgba(59,130,246,0.2);
}

/* Legend */
.legend-grid{display:flex;flex-wrap:wrap;gap:8px}
.lg{display:flex;align-items:center;gap:5px;font-size:10px;color:rgba(255,255,255,0.5)}
.ld{width:14px;height:8px;border-radius:2px;flex-shrink:0}
.ld-vip{background:#fbbf24;border:1px solid #d97706}
.ld-inner{background:#60a5fa;border:1px solid #3b82f6}
.ld-stand{background:#818cf8;border:1px solid #6366f1}
.ld-sold{background:#ef4444;border:1px solid #b91c1c}
.ld-selected{background:#22c55e;border:1px solid #16a34a}

/* Selected seats */
.sp-summary{flex:1}
.sel-list{display:flex;flex-direction:column;gap:5px}
.sel-row{
  display:flex;align-items:center;gap:8px;
  padding:8px 10px;background:rgba(255,255,255,0.04);
  border-radius:10px;border:1px solid rgba(255,255,255,0.06);
  transition:all .15s;
}
.sel-row:hover{background:rgba(255,255,255,0.08);border-color:rgba(34,197,94,0.3)}
.sr-info{flex:1;min-width:0}
.sr-pos{display:block;font-size:11px;color:rgba(255,255,255,0.8)}
.sr-price{font-size:12px;font-weight:700;color:#22c55e}
.sr-rm{
  border:none;background:rgba(255,255,255,0.06);color:rgba(255,255,255,0.4);
  cursor:pointer;width:24px;height:24px;border-radius:6px;
  display:flex;align-items:center;justify-content:center;transition:all .15s;
}
.sr-rm:hover{background:rgba(239,68,68,0.2);color:#ef4444}
.sel-total{
  display:flex;justify-content:space-between;
  padding-top:10px;border-top:1px solid rgba(255,255,255,0.08);
  font-size:14px;font-weight:600;color:rgba(255,255,255,0.7);
}
.st-price{font-size:20px;font-weight:700;color:#fff}
.sel-empty{
  display:flex;flex-direction:column;align-items:center;gap:8px;
  font-size:11px;color:rgba(255,255,255,0.3);text-align:center;padding:16px 0;line-height:1.6;
}

/* ===== Buy Button ===== */
.buy-btn{
  width:100%;height:50px;border:none;border-radius:12px;
  background:linear-gradient(135deg,#2563eb,#7c3aed);
  color:#fff;font-size:16px;font-weight:700;letter-spacing:1px;
  cursor:pointer;transition:all .25s;
  box-shadow:0 4px 16px rgba(37,99,235,.3);
  position:relative;overflow:hidden;
}
.buy-btn::before{
  content:'';position:absolute;inset:0;
  background:linear-gradient(135deg,#3b82f6,#8b5cf6);opacity:0;transition:opacity .25s;
}
.buy-btn:hover:not(:disabled)::before{opacity:1}
.buy-btn:hover:not(:disabled){
  transform:translateY(-2px);
  box-shadow:0 8px 28px rgba(37,99,235,.45);
}
.buy-btn:active:not(:disabled){transform:scale(.98)}
.buy-btn:disabled{
  background:rgba(148,163,184,0.2);color:rgba(255,255,255,0.25);
  cursor:not-allowed;box-shadow:none;
}
.buy-btn span{position:relative;z-index:1}
.buy-loading{display:flex;align-items:center;justify-content:center;gap:8px}
.loading-spin{animation:spin 1s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}

.buy-msg{padding:10px 12px;border-radius:10px;text-align:center;font-size:12px;font-weight:600}
.buy-msg.ok{background:rgba(34,197,94,.15);color:#4ade80;border:1px solid rgba(34,197,94,.3)}
.buy-msg.fail{background:rgba(239,68,68,.15);color:#f87171;border:1px solid rgba(239,68,68,.3)}

/* Login dialog */
.login-req-body{text-align:center;padding:20px 8px}
.lr-icon{color:#f59e0b;margin-bottom:16px}
.login-req-body h3{font-size:18px;font-weight:700;color:var(--slate-800);margin-bottom:8px}
.login-req-body p{font-size:14px;color:var(--slate-400);margin-bottom:24px}

/* Override loading mask for dark theme */
:deep(.el-loading-mask){background:rgba(15,23,42,0.7)!important;backdrop-filter:blur(4px)}

/* ===== Payment QR Dialog ===== */
.payment-body{text-align:center;padding:8px 0}
.pay-title{font-size:20px;font-weight:700;color:var(--slate-800);margin-bottom:16px}

/* Success */
.pay-success-icon{margin-bottom:12px}
.pay-desc{font-size:14px;color:var(--slate-400);margin-bottom:8px}

/* Payment tabs */
.pay-method-label{
  display:flex;align-items:center;justify-content:center;gap:8px;
  margin-bottom:18px;font-size:15px;font-weight:600;color:var(--slate-700);
}

/* QR */
.qr-wrapper{display:flex;flex-direction:column;align-items:center;gap:10px;margin-bottom:18px}
.qr-code{padding:12px;background:#fff;border-radius:12px;box-shadow:0 4px 16px rgba(0,0,0,.08);display:inline-block;transition:box-shadow .3s;line-height:0}
.qr-code img{display:block;border-radius:4px}
.qr-code.qr-alipay{box-shadow:0 4px 20px rgba(22,119,255,.2)}
.qr-loading{width:200px;height:200px;display:flex;align-items:center;justify-content:center;color:var(--slate-400);font-size:13px}
.qr-hint{font-size:13px;font-weight:500;color:#1677ff}

/* Summary */
.pay-summary{background:var(--slate-50);border-radius:10px;padding:14px 16px;text-align:left;margin-bottom:16px}
.pay-row{display:flex;justify-content:space-between;padding:4px 0;font-size:13px;color:var(--slate-600)}
.pay-total{font-size:20px;font-weight:700;color:#2563eb}
.pay-countdown{font-weight:700;color:#ef4444;font-variant-numeric:tabular-nums}

/* Pay buttons */
.pay-btn{
  height:42px;padding:0 24px;border:none;border-radius:10px;
  font-size:14px;font-weight:600;cursor:pointer;transition:all .2s;
}
.pay-btn.cancel{
  background:var(--slate-100);color:var(--slate-500);
}
.pay-btn.cancel:hover{background:var(--slate-200)}
.pay-btn.confirm{
  background:linear-gradient(135deg,#2563eb,#7c3aed);
  color:#fff;box-shadow:0 4px 14px rgba(37,99,235,.3);
}
.pay-btn.confirm:hover:not(:disabled){
  transform:translateY(-1px);box-shadow:0 6px 20px rgba(37,99,235,.4);
}
.pay-btn.confirm:disabled{opacity:.5;cursor:not-allowed}
</style>
