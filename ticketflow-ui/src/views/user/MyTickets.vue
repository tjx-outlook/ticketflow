<template>
  <div>
    <div class="page-header"><h2>我的票</h2></div>
    <div class="ticket-grid">
      <div v-for="ticket in tableData" :key="ticket.id" class="ticket-card">
        <div class="ticket-header">
          <span class="ticket-no">{{ ticket.ticketNo }}</span>
          <el-tag size="small" :type="ticket.status === 0 ? 'success' : 'info'">
            {{ ticket.status === 0 ? '未使用' : '已使用' }}
          </el-tag>
        </div>
        <div class="ticket-body">
          <p><strong>活动ID:</strong> {{ ticket.eventId }}</p>
          <p><strong>座位ID:</strong> {{ ticket.seatId }}</p>
          <p><strong>订单号:</strong> {{ ticket.orderNo }}</p>
        </div>
        <div class="ticket-qr">
          <div class="qr-placeholder">{{ ticket.qrCode }}</div>
        </div>
        <div class="ticket-footer">
          <span>{{ ticket.createTime?.replace('T', ' ') }}</span>
        </div>
      </div>
    </div>
    <el-pagination v-model:current-page="current" :page-size="size" :total="total"
      layout="total, prev, pager, next" @current-change="fetchData" style="margin-top:24px;justify-content:center" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ticketApi } from '../../api/ticket'

const tableData = ref([])
const current = ref(1), size = ref(12), total = ref(0)

onMounted(() => fetchData())

async function fetchData() {
  const res = await ticketApi.getList({ current: current.value, size: size.value })
  tableData.value = res.records; total.value = res.total
}
</script>

<style scoped>
.ticket-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}
.ticket-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}
.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.ticket-no { font-size: 12px; opacity: 0.8; word-break: break-all; }
.ticket-body { font-size: 14px; line-height: 1.8; }
.ticket-qr { margin: 12px 0; }
.qr-placeholder {
  background: rgba(255,255,255,0.2);
  padding: 8px;
  border-radius: 4px;
  font-size: 10px;
  text-align: center;
  word-break: break-all;
}
.ticket-footer { font-size: 12px; opacity: 0.6; text-align: right; }
</style>
