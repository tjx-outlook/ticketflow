<template>
  <div class="login-page">
    <!-- Left: Brand Panel -->
    <div class="login-brand">
      <div class="brand-overlay"></div>
      <div class="brand-content">
        <div class="brand-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="rgba(255,255,255,.15)"/>
            <path d="M14 16h20M14 24h20M14 32h12" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="34" cy="31" r="5" fill="rgba(255,255,255,.25)" stroke="#fff" stroke-width="1.5"/>
          </svg>
        </div>
        <h2 class="brand-title">TicketFlow</h2>
        <p class="brand-desc">企业级高并发票务平台<br/>为每一张票保驾护航</p>
        <div class="brand-features">
          <div class="feat-item">
            <span class="feat-dot"></span> 毫秒级秒杀响应
          </div>
          <div class="feat-item">
            <span class="feat-dot"></span> 分布式库存保障
          </div>
          <div class="feat-item">
            <span class="feat-dot"></span> 多租户架构支撑
          </div>
        </div>
      </div>
    </div>

    <!-- Right: Login Form -->
    <div class="login-form-panel">
      <div class="form-wrapper">
        <div class="form-header">
          <span class="form-badge">Welcome back</span>
          <h1 class="form-title">登录账户</h1>
          <p class="form-sub">请输入您的凭证以继续</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" size="large" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
              class="custom-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              @click="handleLogin"
              class="login-btn"
            >
              <span v-if="!loading">登 录</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>还没有账户？</span>
          <router-link to="/register" class="register-link">创建新账户 →</router-link>
        </div>

        <div class="form-hint">
          <span>测试账户：</span>
          <code>admin</code> / <code>admin123</code>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/events')
  } catch (e) {
    // 拦截器已处理 toast
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  font-family: 'Inter', system-ui, sans-serif;
}

/* ===== Left Brand Panel ===== */
.login-brand {
  flex: 0 0 44%;
  background: linear-gradient(160deg, #1e3a5f 0%, #1a56b0 30%, #1e40af 60%, #1e3a8a 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.brand-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 30%, rgba(255,255,255,.04) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(255,255,255,.03) 0%, transparent 40%);
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 60px 48px;
  max-width: 420px;
}

.brand-icon { margin-bottom: 28px; display: inline-block; }

.brand-title {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -1px;
  margin-bottom: 12px;
}

.brand-desc {
  font-size: 15px;
  color: rgba(255,255,255,.6);
  line-height: 1.8;
  margin-bottom: 40px;
}

.brand-features {
  text-align: left;
  display: inline-flex;
  flex-direction: column;
  gap: 14px;
}

.feat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255,255,255,.7);
  font-size: 14px;
}

.feat-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255,255,255,.4);
  flex-shrink: 0;
}

/* ===== Right Form Panel ===== */
.login-form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafbfc;
  padding: 40px;
}

.form-wrapper {
  width: 100%;
  max-width: 400px;
}

.form-header { margin-bottom: 36px; }

.form-badge {
  display: inline-block;
  padding: 4px 12px;
  background: var(--blue-50);
  color: var(--blue-600);
  font-size: 12px;
  font-weight: 600;
  border-radius: 20px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  margin-bottom: 16px;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--slate-900);
  letter-spacing: -0.5px;
  margin-bottom: 6px;
}

.form-sub {
  font-size: 14px;
  color: var(--slate-400);
}

.login-form { margin-top: 8px; }

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 4px 14px;
  height: 48px;
  background: var(--white);
  border-radius: var(--radius) !important;
  box-shadow: 0 0 0 1px var(--slate-200) inset !important;
  transition: all .2s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--slate-300) inset !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--blue-400) inset !important;
}

.login-form :deep(.el-input__inner) {
  font-size: 15px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: var(--radius);
  background: var(--blue-600);
  border: none;
  transition: all var(--transition);
  margin-top: 4px;
}

.login-btn:hover {
  background: var(--blue-700);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, .3);
}

.form-footer {
  text-align: center;
  font-size: 14px;
  color: var(--slate-400);
  margin-top: 24px;
}

.register-link {
  color: var(--blue-600);
  font-weight: 600;
  text-decoration: none;
  margin-left: 2px;
  transition: color .2s;
}

.register-link:hover { color: var(--blue-700); }

.form-hint {
  margin-top: 20px;
  padding: 14px;
  background: var(--slate-50);
  border-radius: var(--radius);
  text-align: center;
  font-size: 12px;
  color: var(--slate-400);
}

.form-hint code {
  background: var(--slate-200);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: var(--slate-600);
  font-weight: 500;
}

@media (max-width: 768px) {
  .login-page { flex-direction: column; }
  .login-brand { flex: 0 0 auto; padding: 40px 20px; }
  .brand-content { padding: 20px; }
  .brand-title { font-size: 24px; }
  .login-form-panel { padding: 32px 24px; }
}
</style>
