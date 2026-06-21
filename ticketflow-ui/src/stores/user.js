import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const loaded = ref(false)

  const isLoggedIn = computed(() => !!token.value)
  const roles = computed(() => userInfo.value?.roles || [])
  const permissions = computed(() => userInfo.value?.permissions || [])

  function hasRole(role) {
    return roles.value.includes(role)
  }

  function hasPermission(perm) {
    return permissions.value.includes(perm) || roles.value.includes('SUPER_ADMIN')
  }

  // 从后端拉取最新用户信息（权限变更后不用重新登录）
  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const info = await userApi.getUserInfo()
      userInfo.value = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    } catch (e) {
      // token 过期或无效，清除登录状态
      if (e.response?.status === 401) {
        logout()
      }
    }
    loaded.value = true
  }

  async function login(username, password) {
    const data = await userApi.login({ username, password })
    token.value = data.token
    localStorage.setItem('token', data.token)
    // 登录后立即拉取完整信息（含角色/权限）
    await fetchUserInfo()
    return data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    loaded.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, loaded, isLoggedIn, roles, permissions, hasRole, hasPermission, login, logout, fetchUserInfo }
})
