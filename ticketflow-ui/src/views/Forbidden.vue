<template>
  <div class="forbidden-page">
    <div class="forbidden-card">
      <div class="error-code">403</div>
      <div class="error-icon">
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
          <rect x="12" y="10" width="40" height="44" rx="4" stroke="var(--slate-300)" stroke-width="2"/>
          <rect x="18" y="18" width="28" height="4" rx="1" fill="var(--slate-200)"/>
          <rect x="18" y="26" width="20" height="3" rx="1" fill="var(--slate-200)"/>
          <rect x="18" y="33" width="24" height="3" rx="1" fill="var(--slate-200)"/>
          <circle cx="44" cy="44" r="10" fill="#fef2f2" stroke="#fca5a5" stroke-width="1.5"/>
          <path d="M41 41l6 6M47 41l-6 6" stroke="#ef4444" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <h1>访问受限</h1>
      <p>抱歉，您没有权限访问此页面</p>
      <el-button type="primary" @click="$router.push('/events')" round>
        返回活动列表
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.forbidden-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 108px);
}

.forbidden-card {
  text-align: center;
  padding: 60px 40px;
  max-width: 420px;
}

.error-code {
  font-size: 96px;
  font-weight: 800;
  color: var(--slate-200);
  line-height: 1;
  margin-bottom: 8px;
  font-family: 'Inter', sans-serif;
  letter-spacing: -4px;
}

.error-icon { margin-bottom: 24px; }

.forbidden-card h1 {
  font-size: 22px;
  font-weight: 700;
  color: var(--slate-800);
  margin-bottom: 8px;
}

.forbidden-card p {
  font-size: 15px;
  color: var(--slate-400);
  margin-bottom: 28px;
}

.forbidden-card .el-button {
  padding: 10px 28px;
  font-weight: 500;
}
</style>
