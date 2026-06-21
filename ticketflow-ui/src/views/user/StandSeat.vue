<template>
  <div class="stand-page">
    <!-- Title -->
    <div class="stand-header">
      <h2 class="stand-title">5号-激光高清-真皮沙发看台</h2>
      <div class="stage-bar">舞 台</div>
    </div>

    <!-- Seat Map -->
    <div class="seat-map">
      <svg viewBox="0 0 900 340" class="seat-svg">
        <defs>
          <!-- Dashed rectangle style -->
          <filter id="shadow-sm">
            <feDropShadow dx="0" dy="1" stdDeviation="1" flood-opacity="0.1"/>
          </filter>
        </defs>

        <!-- Premium zone red dashed rectangle (rows 3-5, middle area) -->
        <rect
          x="245" y="70" width="410" height="118"
          fill="none" stroke="#ef4444" stroke-width="1.5"
          stroke-dasharray="8 5" rx="6" opacity="0.7"
        />
        <text x="450" y="68" text-anchor="middle" font-size="10" fill="#ef4444" opacity="0.7">优选观演区</text>

        <!-- ===== Row guides and seats ===== -->
        <g v-for="(row, ri) in seatRows" :key="ri">
          <!-- Row label on the left -->
          <text
            :x="30" :y="row.y + 11" text-anchor="middle"
            font-size="16" font-weight="700" :fill="row.labelColor"
          >{{ ri + 1 }}</text>
          <text
            :x="30" :y="row.y + 26" text-anchor="middle"
            font-size="8" fill="#94a3b8"
          >排</text>

          <!-- Seats -->
          <g v-for="(seat, si) in row.seats" :key="si">
            <rect
              :x="seat.x" :y="row.y"
              width="22" height="22" rx="3"
              :fill="seat.fill"
              :stroke="seat.stroke"
              stroke-width="1.5"
              filter="url(#shadow-sm)"
              :class="{ 'seat-clickable': seat.type === 'available' }"
              @click="seat.type === 'available' ? toggleSeat(ri, si) : null"
            />
            <!-- Taken: glasses icon -->
            <g v-if="seat.type === 'taken'" :transform="`translate(${seat.x + 4}, ${row.y + 4})`">
              <circle cx="5" cy="5" r="2.5" fill="none" stroke="#fff" stroke-width="1.2"/>
              <circle cx="11" cy="5" r="2.5" fill="none" stroke="#fff" stroke-width="1.2"/>
              <line x1="3" y1="9" x2="13" y2="9" stroke="#fff" stroke-width="1.2"/>
              <line x1="8" y1="5" x2="8" y2="9" stroke="#fff" stroke-width="1"/>
            </g>
            <!-- Selected: white checkmark -->
            <g v-if="seat.type === 'selected'" :transform="`translate(${seat.x + 3}, ${row.y + 3})`">
              <polyline points="4,11 8,15 16,5" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            </g>
          </g>
        </g>
      </svg>
    </div>

    <!-- Legend -->
    <div class="stand-legend">
      <div class="legend-item">
        <span class="leg-box leg-available"></span> 未售空位
      </div>
      <div class="legend-item">
        <span class="leg-box leg-taken"></span> 已被预定
      </div>
      <div class="legend-item">
        <span class="leg-box leg-selected"></span> 已选座位
      </div>
    </div>

    <!-- Action bar -->
    <div class="stand-action" v-if="selectedCount > 0">
      <span class="action-info">已选 {{ selectedCount }} 个座位</span>
      <button class="action-btn">确认选座</button>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue'

// Row 1: 11 seats evenly spaced
// Row 2: 13 seats
// Row 3: 15 seats
// Row 4: 17 seats
// Row 5: 17 seats
// Row 6: 15 seats
// Row 7: 13 seats (split in middle)

const TOTAL_WIDTH = 780
const START_X = 60

function makeRow(seatCount, y, rowIndex) {
  const totalW = (seatCount - 1) * 28
  const startX = START_X + (TOTAL_WIDTH - totalW) / 2
  const seats = []

  for (let i = 0; i < seatCount; i++) {
    const x = startX + i * 28
    let type = 'available'

    // Row 2: middle 2 adjacent red taken seats (indices around center)
    if (rowIndex === 1) {
      const mid = Math.floor(seatCount / 2)
      if (i === mid || i === mid + 1) type = 'taken'
    }
    // Row 3: middle 1 green selected seat
    if (rowIndex === 2) {
      const mid = Math.floor(seatCount / 2)
      if (i === mid) type = 'selected'
    }
    // Row 4: 1 red taken on the left inside the dashed box
    if (rowIndex === 3) {
      const leftZone = Math.floor(seatCount * 0.3)
      if (i === leftZone) type = 'taken'
    }
    // Row 7: split in middle, left and right each 1 red taken
    if (rowIndex === 6) {
      const mid = Math.floor(seatCount / 2)
      // Gap in middle: remove 1 seat (make it a gap)
      if (i === mid) {
        // skip this seat (gap)
        continue
      }
      if (i === mid - 2 || i === mid + 2) type = 'taken'
    }

    seats.push({
      x,
      type,
      fill: type === 'taken' ? '#ef4444' : type === 'selected' ? '#22c55e' : '#fff',
      stroke: type === 'taken' ? '#dc2626' : type === 'selected' ? '#16a34a' : '#cbd5e1',
    })
  }
  return { seats, y, labelColor: rowIndex === 2 ? '#22c55e' : '#475569' }
}

const seatRows = reactive([
  makeRow(11, 20, 0),   // Row 1
  makeRow(13, 52, 1),   // Row 2
  makeRow(15, 84, 2),   // Row 3
  makeRow(17, 116, 3),  // Row 4
  makeRow(17, 148, 4),  // Row 5
  makeRow(15, 180, 5),  // Row 6
  makeRow(13, 212, 6),  // Row 7
])

const selectedCount = computed(() => {
  let c = 0
  seatRows.forEach(r => r.seats.forEach(s => { if (s.type === 'selected') c++ }))
  return c
})

function toggleSeat(ri, si) {
  const seat = seatRows[ri].seats[si]
  if (seat.type === 'available') {
    seat.type = 'selected'
    seat.fill = '#22c55e'
    seat.stroke = '#16a34a'
  } else if (seat.type === 'selected') {
    seat.type = 'available'
    seat.fill = '#fff'
    seat.stroke = '#cbd5e1'
  }
}
</script>

<style scoped>
.stand-page {
  max-width: 960px; margin: 0 auto; padding: 24px;
  background: #fafbfc; border-radius: 16px;
  border: 1px solid #e2e8f0;
}

/* Header */
.stand-header { text-align: center; margin-bottom: 20px; }
.stand-title {
  font-size: 20px; font-weight: 700; color: #1e293b;
  letter-spacing: 1px; margin-bottom: 14px;
}
.stage-bar {
  width: 400px; margin: 0 auto; padding: 12px 0;
  background: linear-gradient(180deg, #e2e8f0, #cbd5e1);
  border-radius: 6px 6px 20px 20px;
  font-size: 14px; font-weight: 700; color: #64748b;
  letter-spacing: 8px; text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,.05);
}

/* Seat SVG */
.seat-map { margin: 16px 0; }
.seat-svg { display: block; width: 100%; height: auto; }

.seat-clickable { cursor: pointer; transition: all .15s; }
.seat-clickable:hover { stroke: #3b82f6; stroke-width: 2; filter: brightness(0.95); }

/* Legend */
.stand-legend {
  display: flex; justify-content: center; gap: 24px;
  padding: 16px; background: #fff; border-radius: 10px;
  border: 1px solid #e2e8f0; margin-top: 12px;
}
.legend-item { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #475569; }
.leg-box { width: 22px; height: 22px; border-radius: 3px; border: 1.5px solid; }
.leg-available { background: #fff; border-color: #cbd5e1; }
.leg-taken { background: #ef4444; border-color: #dc2626; }
.leg-selected { background: #22c55e; border-color: #16a34a; }

/* Action bar */
.stand-action {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 16px; padding: 12px 20px;
  background: #fff; border-radius: 10px; border: 1px solid #e2e8f0;
}
.action-info { font-size: 14px; color: #475569; font-weight: 500; }
.action-btn {
  padding: 10px 32px; background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff; border: none; border-radius: 8px;
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: all .2s;
}
.action-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(37,99,235,.3); }
</style>
