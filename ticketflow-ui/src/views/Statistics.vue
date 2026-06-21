<template>
  <div class="stats-page">
    <div class="page-header"><h2>数据统计</h2></div>

    <!-- Global Stats -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon-box" style="background:#ecfdf5;color:var(--green-600)">
          <el-icon size="26"><Document /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">总订单数</div>
          <div class="stat-value">{{ totalStats.totalOrders || 0 }}</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-box" style="background:#eff6ff;color:var(--blue-600)">
          <el-icon size="26"><Money /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">总营收</div>
          <div class="stat-value">¥{{ totalStats.totalRevenue || 0 }}</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-box" style="background:#f5f3ff;color:#7c3aed">
          <el-icon size="26"><TrendCharts /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">活动数量</div>
          <div class="stat-value">{{ events.length }}</div>
        </div>
      </div>
    </div>

    <!-- By Event -->
    <div class="event-stats-panel">
      <div class="panel-header">
        <h3>按活动统计</h3>
        <el-select v-model="selectedEventId" placeholder="选择活动" @change="fetchEventStats" style="width:300px" size="large">
          <el-option v-for="e in events" :key="e.id" :label="e.eventName" :value="e.id" />
        </el-select>
      </div>
      <div v-if="eventStats" class="event-stats-grid">
        <div class="mini-stat">
          <span class="mini-value text-success">{{ eventStats.totalOrders }}</span>
          <span class="mini-label">订单数</span>
        </div>
        <div class="mini-stat">
          <span class="mini-value" style="color:var(--blue-600)">{{ eventStats.totalTickets }}</span>
          <span class="mini-label">出票数</span>
        </div>
        <div class="mini-stat">
          <span class="mini-value text-warning">¥{{ eventStats.totalRevenue }}</span>
          <span class="mini-label">营收</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { eventApi } from '../api/event'
import { statisticsApi } from '../api/statistics'

const events = ref([])
const selectedEventId = ref(null)
const eventStats = ref(null)
const totalStats = ref({ totalOrders: 0, totalRevenue: 0 })

onMounted(async () => {
  const [evtRes, totalRes] = await Promise.all([
    eventApi.getList({ current: 1, size: 100 }),
    statisticsApi.getTotal()
  ])
  events.value = evtRes.records
  totalStats.value = totalRes
  if (events.value.length) {
    selectedEventId.value = events.value[0].id
    fetchEventStats()
  }
})

async function fetchEventStats() {
  if (!selectedEventId.value) return
  eventStats.value = await statisticsApi.getByEvent(selectedEventId.value)
}
</script>

<style scoped>
.stats-page { max-width: 1200px; margin: 0 auto; }

/* Global Cards */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  background: var(--white);
  border-radius: var(--radius-xl);
  padding: 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  border: 1px solid var(--slate-100);
  transition: all var(--transition);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-icon-box {
  width: 56px;
  height: 56px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body { flex: 1; }

.stat-label {
  font-size: 13px;
  color: var(--slate-400);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 30px;
  font-weight: 700;
  color: var(--slate-800);
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

/* By Event */
.event-stats-panel {
  background: var(--white);
  border-radius: var(--radius-xl);
  border: 1px solid var(--slate-100);
  padding: 24px 28px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.panel-header h3 {
  font-size: 17px;
  font-weight: 600;
  color: var(--slate-800);
}

.event-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.mini-stat {
  text-align: center;
  padding: 28px 20px;
  background: var(--slate-50);
  border-radius: var(--radius-lg);
  border: 1px solid var(--slate-100);
}

.mini-value {
  font-size: 38px;
  font-weight: 700;
  display: block;
  line-height: 1;
  margin-bottom: 8px;
}

.mini-label {
  font-size: 13px;
  color: var(--slate-400);
  font-weight: 500;
}
</style>
