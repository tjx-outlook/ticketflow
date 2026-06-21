<template>
  <div class="layout">
    <!-- Top Navigation Bar -->
    <header class="header">
      <div class="header-inner">
        <!-- Left: Logo + Nav -->
        <div class="header-left">
          <a class="logo" @click="$router.push('/events')">
            <svg width="28" height="28" viewBox="0 0 48 48" fill="none" class="logo-icon">
              <rect width="48" height="48" rx="10" fill="var(--blue-600)"/>
              <path d="M14 16h20M14 24h20M14 32h12" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
              <circle cx="34" cy="31" r="5" fill="rgba(255,255,255,.2)" stroke="#fff" stroke-width="1.5"/>
            </svg>
            <span class="logo-text">TicketFlow</span>
          </a>
          <nav class="main-nav">
            <router-link to="/events" class="nav-item" :class="{ active: route.path === '/events' }">
              活动列表
            </router-link>
            <!-- Admin nav items -->
            <template v-if="isAdmin">
              <span class="nav-sep">·</span>
              <router-link v-if="can('perm','user:manage')" to="/admin/users" class="nav-item" :class="{ active: route.path.startsWith('/admin/users') }">用户</router-link>
              <router-link v-if="can('perm','role:manage')" to="/admin/roles" class="nav-item" :class="{ active: route.path.startsWith('/admin/roles') }">角色</router-link>
              <router-link v-if="can('perm','merchant:manage')" to="/admin/merchants" class="nav-item" :class="{ active: route.path.startsWith('/admin/merchants') }">商家</router-link>
              <router-link v-if="can('perm','event:manage')" to="/merchant/events" class="nav-item" :class="{ active: route.path.startsWith('/merchant/events') }">活动</router-link>
              <router-link v-if="can('perm','seat:manage')||can('perm','event:manage')" to="/merchant/seats" class="nav-item" :class="{ active: route.path.startsWith('/merchant/seats') }">座位</router-link>
              <router-link v-if="can('perm','ticket:manage')" to="/ticket-check" class="nav-item" :class="{ active: route.path === '/ticket-check' }">检票</router-link>
              <router-link v-if="can('perm','statistics:view')" to="/statistics" class="nav-item" :class="{ active: route.path.startsWith('/statistics') }">统计</router-link>
            </template>
          </nav>
        </div>

        <!-- Right: User -->
        <div class="header-right">
          <!-- Not logged in → Login button -->
          <template v-if="!userStore.isLoggedIn">
            <el-button type="primary" size="small" round @click="$router.push('/login')">
              登录
            </el-button>
          </template>

          <!-- Logged in → User dropdown -->
          <el-dropdown v-else trigger="click" placement="bottom-end">
            <div class="user-trigger">
              <el-avatar :size="34" icon="UserFilled" class="user-avatar" />
              <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</span>
              <el-icon class="user-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <div class="dropdown-header">
                <el-avatar :size="40" icon="UserFilled" />
                <div>
                  <div class="dropdown-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</div>
                  <div class="dropdown-role">{{ userStore.roles[0] || '普通用户' }}</div>
                </div>
              </div>
              <el-divider style="margin:8px 0" />
              <el-dropdown-item @click="$router.push('/personal-center')">
                <el-icon><User /></el-icon> 个人中心
              </el-dropdown-item>
              <el-dropdown-item @click="handleLogout" class="logout-item">
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- Page Content -->
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isAdmin = computed(() =>
  userStore.hasRole('SUPER_ADMIN') ||
  userStore.permissions.some(p =>
    ['user:manage','role:manage','merchant:manage','event:manage','seat:manage','ticket:manage','statistics:view'].includes(p)
  )
)

onMounted(() => { userStore.fetchUserInfo() })

function can(type, value) {
  if (userStore.hasRole('SUPER_ADMIN')) return true
  if (type === 'perm') return userStore.hasPermission(value)
  return false
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; background: var(--slate-50); }

/* ===== Header ===== */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255,255,255,.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--slate-200);
}

.header-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 28px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* Logo */
.header-left { display: flex; align-items: center; gap: 32px; }
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-icon { flex-shrink: 0; }

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--slate-800);
  letter-spacing: -0.5px;
}

/* Nav */
.main-nav { display: flex; align-items: center; gap: 4px; }

.nav-item {
  padding: 6px 14px;
  border-radius: var(--radius);
  font-size: 13px;
  font-weight: 500;
  color: var(--slate-500);
  text-decoration: none;
  transition: all var(--transition);
  white-space: nowrap;
}

.nav-item:hover { color: var(--slate-700); background: var(--slate-100); }

.nav-item.active {
  color: var(--blue-600);
  background: var(--blue-50);
  font-weight: 600;
}

.nav-sep {
  color: var(--slate-300);
  font-size: 18px;
  margin: 0 -2px;
  user-select: none;
}

/* User */
.header-right { display: flex; align-items: center; }

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  cursor: pointer;
  transition: all var(--transition);
  background: transparent;
}

.user-trigger:hover { background: var(--slate-100); }

.user-avatar { flex-shrink: 0; }

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--slate-700);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-arrow {
  font-size: 12px;
  color: var(--slate-400);
  transition: transform .2s;
}

/* Dropdown custom */
.dropdown-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px 4px;
}

.dropdown-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--slate-800);
}

.dropdown-role {
  font-size: 12px;
  color: var(--slate-400);
  margin-top: 2px;
}

.logout-item { color: var(--red-500) !important; }
.logout-item:hover { background: #fef2f2 !important; color: var(--red-600) !important; }

/* ===== Main ===== */
.main {
  flex: 1;
  padding: 24px 28px 40px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}
</style>
