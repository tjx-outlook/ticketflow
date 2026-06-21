<template>
  <div>
    <div class="page-header">
      <h2>座位管理</h2>
      <el-button type="primary" @click="dialogVisible = true">批量创建座位</el-button>
    </div>

    <div class="search-bar">
      <span style="line-height:32px;color:var(--slate-600);font-weight:500">选择活动：</span>
      <el-select v-model="selectedEventId" placeholder="请选择活动" @change="fetchSeats" style="width:320px" size="large">
        <el-option v-for="e in events" :key="e.id" :label="e.eventName" :value="e.id" />
      </el-select>
    </div>

    <!-- Seat Map -->
    <div v-if="seats.length" class="seat-panel">
      <h3 class="seat-panel-title">座位分布图</h3>
      <div v-for="section in sections" :key="section.name" class="seat-section">
        <div class="section-head">
          <h4>{{ section.name }}</h4>
          <el-tag size="small" type="warning" effect="plain">¥{{ section.price }}</el-tag>
          <span class="section-stats">
            可售 <strong>{{ section.seats.filter(s => s.status === 0).length }}</strong> / {{ section.seats.length }}
          </span>
        </div>
        <div class="seat-grid">
          <div
            v-for="seat in section.seats" :key="seat.id"
            class="seat"
            :class="{
              'seat-available': seat.status === 0,
              'seat-locked': seat.status === 1,
              'seat-sold': seat.status === 2
            }"
            :title="`${seat.rowNum}排${seat.seatNum}座 ¥${seat.price}`"
          >
            {{ seat.rowNum }}-{{ seat.seatNum }}
          </div>
        </div>
      </div>
    </div>

    <!-- Batch Create Dialog -->
    <el-dialog v-model="dialogVisible" title="批量创建座位" width="480px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="所属活动">
          <el-select v-model="batchForm.eventId" placeholder="请选择活动" style="width:100%">
            <el-option v-for="e in events" :key="e.id" :label="e.eventName" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域名称"><el-input v-model="batchForm.sectionName" placeholder="如: VIP区 / A区 / B区" /></el-form-item>
        <el-form-item label="排数"><el-input-number v-model="batchForm.rowCount" :min="1" :max="50" /></el-form-item>
        <el-form-item label="每排座位数"><el-input-number v-model="batchForm.seatsPerRow" :min="1" :max="50" /></el-form-item>
        <el-form-item label="票价(元)"><el-input-number v-model="batchForm.price" :min="0" :precision="2" :step="10" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchCreate">确定创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { eventApi } from '../../api/event'
import { seatApi } from '../../api/seat'
import { ElMessage } from 'element-plus'

const events = ref([])
const selectedEventId = ref(null)
const seats = ref([])
const dialogVisible = ref(false)
const batchForm = ref({ eventId: null, sectionName: '', rowCount: 5, seatsPerRow: 5, price: 380 })

onMounted(async () => {
  const res = await eventApi.getList({ current: 1, size: 100 })
  events.value = res.records
  if (events.value.length) {
    selectedEventId.value = events.value[0].id
    fetchSeats()
  }
})

async function fetchSeats() {
  if (!selectedEventId.value) return
  const data = await seatApi.getByEvent(selectedEventId.value)
  seats.value = data || []
}

const sections = computed(() => {
  const map = {}
  seats.value.forEach(s => {
    if (!map[s.sectionName]) {
      map[s.sectionName] = { name: s.sectionName, price: s.price, seats: [] }
    }
    map[s.sectionName].seats.push(s)
  })
  Object.values(map).forEach(sec => {
    sec.seats.sort((a, b) => a.rowNum - b.rowNum || a.seatNum - b.seatNum)
  })
  return Object.values(map)
})

async function submitBatchCreate() {
  if (!batchForm.value.eventId || !batchForm.value.sectionName) {
    ElMessage.warning('请填写完整信息'); return
  }
  await seatApi.batchCreate({ ...batchForm.value })
  ElMessage.success(`成功创建 ${batchForm.value.rowCount * batchForm.value.seatsPerRow} 个座位`)
  dialogVisible.value = false
  if (selectedEventId.value === batchForm.value.eventId) fetchSeats()
}
</script>

<style scoped>
.seat-panel {
  margin-top: 24px;
  background: var(--white);
  border-radius: var(--radius-xl);
  border: 1px solid var(--slate-100);
  padding: 24px 28px;
}

.seat-panel-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--slate-800);
  margin-bottom: 20px;
}

.seat-section { margin-bottom: 24px; }

.section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.section-head h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--slate-700);
}

.section-stats {
  margin-left: auto;
  font-size: 13px;
  color: var(--slate-400);
}

.section-stats strong {
  color: var(--blue-600);
  font-weight: 700;
}

.seat-grid { display: flex; flex-wrap: wrap; gap: 8px; }

.seat {
  width: 52px; height: 36px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; font-weight: 500; cursor: default; transition: all 0.2s;
}

.seat-available { background: #ecfdf5; border: 1px solid #a7f3d0; color: var(--green-700); }
.seat-locked { background: #fffbeb; border: 1px solid #fde68a; color: #b45309; }
.seat-sold { background: #fef2f2; border: 1px solid #fecaca; color: #dc2626; }
</style>
