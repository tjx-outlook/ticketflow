<template>
  <div class="events-page">
    <!-- Hero Banner -->
    <section class="events-hero">
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="hero-title-cn">精彩活动</span>
          <span class="hero-title-en">Featured Events</span>
        </h1>
        <p class="hero-sub">精选优质演出，为您呈现每一场不容错过的视听盛宴</p>
      </div>
      <div class="hero-pattern"></div>
    </section>

    <!-- Search -->
    <div class="events-toolbar">
      <div class="search-box">
        <el-icon class="search-icon"><Search /></el-icon>
        <el-input
          v-model="keyword"
          placeholder="搜索您感兴趣的活动..."
          class="search-input"
          clearable
          @keyup.enter="fetchData"
        />
      </div>
      <el-button type="primary" @click="fetchData" class="search-btn">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
    </div>

    <!-- Event Grid -->
    <div class="event-grid">
      <article
        v-for="event in tableData"
        :key="event.id"
        class="event-card"
        @click="$router.push(`/events/${event.id}`)"
      >
        <!-- Poster -->
        <div class="card-poster">
          <img v-if="event.posterUrl" :src="event.posterUrl" alt="" />
          <div v-else class="poster-fallback">
            <span class="fallback-emoji">🎵</span>
          </div>
          <!-- Status badge -->
          <span class="card-status" :class="'status-' + event.status">
            {{ statusMap[event.status]?.label }}
          </span>
        </div>

        <!-- Info -->
        <div class="card-body">
          <h3 class="card-title">{{ event.eventName }}</h3>
          <div class="card-meta">
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>{{ event.venue }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDate(event.startTime) }}</span>
            </div>
          </div>
          <div class="card-footer">
            <div class="seats-info">
              <span class="seats-count">{{ event.totalSeats }}</span>
              <span class="seats-label">座位</span>
            </div>
            <div class="card-action">
              <el-button
                v-if="event.status === 1"
                type="primary"
                size="small"
                round
                class="buy-btn"
              >
                立即抢购
              </el-button>
              <span v-else class="status-text">{{ statusMap[event.status]?.label }}</span>
            </div>
          </div>
        </div>
      </article>
    </div>

    <!-- Empty -->
    <el-empty v-if="!tableData.length && !loading" description="暂无活动" />

    <!-- Pagination -->
    <div class="events-pagination">
      <el-pagination
        v-model:current-page="current"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchData"
      />
    </div>

    <!-- Guest Welcome Dialog -->
    <el-dialog
      v-model="guestDialogVisible"
      :close-on-click-modal="false"
      :show-close="false"
      width="400px"
      class="guest-dialog"
      align-center
    >
      <div class="guest-dialog-body">
        <div class="guest-icon">
          <svg width="56" height="56" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="var(--blue-600)"/>
            <path d="M14 16h20M14 24h20M14 32h12" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="34" cy="31" r="5" fill="rgba(255,255,255,.2)" stroke="#fff" stroke-width="1.5"/>
          </svg>
        </div>
        <h2 class="guest-title">欢迎来到 TicketFlow</h2>
        <p class="guest-desc">登录后即可抢购心仪活动的门票</p>
        <div class="guest-actions">
          <el-button type="primary" size="large" class="guest-btn-login" @click="goLogin">
            去登录
          </el-button>
          <el-button size="large" class="guest-btn-browse" @click="guestDialogVisible = false">
            游客浏览
          </el-button>
        </div>
        <p class="guest-register-hint">
          未有账号？<a @click="goRegister">去注册</a>
        </p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { eventApi } from '../../api/event'

const router = useRouter()
const userStore = useUserStore()

const tableData = ref([])
const current = ref(1), size = ref(12), total = ref(0), keyword = ref('')
const guestDialogVisible = ref(false)

const statusMap = {
  0: { label: '未开售', type: 'info' },
  1: { label: '热卖中', type: 'danger' },
  2: { label: '已售罄', type: 'info' },
  3: { label: '已结束', type: 'info' }
}

function formatDate(str) {
  if (!str) return ''
  return str.replace('T', ' ')
}

function goLogin() {
  router.push('/login')
}

function goRegister() {
  router.push('/register')
}

onMounted(() => {
  fetchData()
  // 未登录用户弹出欢迎对话框
  if (!userStore.isLoggedIn) {
    guestDialogVisible.value = true
  }
})

async function fetchData() {
  const res = await eventApi.getList({ current: current.value, size: size.value, keyword: keyword.value })
  tableData.value = res.records; total.value = res.total
}
</script>

<style scoped>
.events-page { max-width: 1280px; margin: 0 auto; }

/* ===== Hero Banner ===== */
.events-hero {
  position: relative;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f0fe 30%, #f8fafc 60%, #f0fdf4 100%);
  border-radius: var(--radius-xl);
  padding: 48px 56px;
  margin-bottom: 28px;
  overflow: hidden;
  border: 1px solid var(--slate-100);
}

.hero-pattern {
  position: absolute;
  right: 40px;
  top: -20px;
  width: 200px;
  height: 200px;
  background:
    radial-gradient(circle, rgba(59,130,246,.06) 0%, transparent 70%),
    radial-gradient(circle at 60% 40%, rgba(34,197,94,.04) 0%, transparent 60%);
  border-radius: 50%;
  pointer-events: none;
}

.hero-content { position: relative; z-index: 1; }

.hero-title {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 12px;
}

.hero-title-cn {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--slate-800);
  letter-spacing: 2px;
}

.hero-title-en {
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 400;
  color: var(--slate-400);
  text-transform: uppercase;
  letter-spacing: 3px;
}

.hero-sub {
  font-size: 15px;
  color: var(--slate-500);
  max-width: 480px;
  line-height: 1.7;
}

/* ===== Toolbar ===== */
.events-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
}

.search-box {
  flex: 1;
  max-width: 420px;
  position: relative;
}

.search-box :deep(.el-input__wrapper) {
  padding-left: 40px;
  height: 44px;
  background: var(--white);
  border-radius: 24px !important;
  box-shadow: 0 0 0 1px var(--slate-200) inset !important;
}

.search-box :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--blue-300) inset !important;
}

.search-box :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--blue-400) inset !important;
}

.search-box :deep(.el-input__inner) { font-size: 14px; }

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  color: var(--slate-400);
  font-size: 16px;
}

.search-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 24px;
  font-weight: 500;
}

/* ===== Event Grid ===== */
.event-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 24px;
}

@media (min-width: 1400px) {
  .event-grid { grid-template-columns: repeat(4, 1fr); }
}

/* ===== Card ===== */
.event-card {
  background: var(--white);
  border-radius: var(--radius-xl);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--slate-100);
  transition: all 0.35s cubic-bezier(.4,0,.2,1);
}

.event-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(0,0,0,.08), 0 4px 12px rgba(0,0,0,.04);
  border-color: var(--blue-100);
}

/* Poster */
.card-poster {
  position: relative;
  height: 190px;
  overflow: hidden;
  background: linear-gradient(135deg, #e8f0fe 0%, #dbeafe 50%, #d1e7dd 100%);
}

.card-poster img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.event-card:hover .card-poster img {
  transform: scale(1.06);
}

.poster-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fallback-emoji {
  font-size: 56px;
  opacity: 0.5;
  filter: grayscale(0.3);
}

/* Status badge */
.card-status {
  position: absolute;
  top: 14px;
  right: 14px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.5px;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.status-1 {
  background: rgba(220,38,38,.85);
  color: #fff;
}

.status-0 {
  background: rgba(100,116,139,.85);
  color: #fff;
}

.status-2, .status-3 {
  background: rgba(100,116,139,.7);
  color: #fff;
}

/* Card body */
.card-body { padding: 18px 20px 20px; }

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--slate-800);
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--slate-500);
}

.meta-item .el-icon { font-size: 14px; color: var(--slate-400); flex-shrink: 0; }

/* Card footer */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid var(--slate-100);
}

.seats-info {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.seats-count {
  font-size: 20px;
  font-weight: 700;
  color: var(--slate-700);
  line-height: 1;
}

.seats-label {
  font-size: 12px;
  color: var(--slate-400);
}

.buy-btn {
  background: var(--blue-600) !important;
  border-color: var(--blue-600) !important;
  font-weight: 600;
  letter-spacing: 1px;
  padding: 6px 20px;
}

.buy-btn:hover {
  background: var(--blue-700) !important;
}

.status-text {
  font-size: 13px;
  color: var(--slate-400);
  font-weight: 500;
}

/* ===== Pagination ===== */
.events-pagination {
  display: flex;
  justify-content: center;
  margin-top: 36px;
  padding-bottom: 8px;
}

/* ===== Guest Dialog ===== */
.guest-dialog :deep(.el-dialog) {
  border-radius: var(--radius-xl);
}

.guest-dialog-body {
  text-align: center;
  padding: 12px 8px;
}

.guest-icon { margin-bottom: 20px; display: inline-block; }

.guest-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--slate-800);
  margin-bottom: 8px;
}

.guest-desc {
  font-size: 14px;
  color: var(--slate-400);
  margin-bottom: 28px;
}

.guest-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 260px;
  margin: 0 auto 20px;
}

.guest-btn-login {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius);
}

.guest-btn-browse {
  width: 100%;
  height: 44px;
  font-size: 15px;
  border-radius: var(--radius);
  border: 1px solid var(--slate-200);
  color: var(--slate-500);
}

.guest-register-hint {
  font-size: 12px;
  color: var(--slate-400);
}

.guest-register-hint a {
  color: var(--blue-600);
  cursor: pointer;
  font-weight: 500;
  text-decoration: none;
}

.guest-register-hint a:hover { color: var(--blue-700); }
</style>
