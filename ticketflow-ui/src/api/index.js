import axios from 'axios'
import { ElMessage } from 'element-plus'

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器
instance.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  const tenantId = localStorage.getItem('tenantId') || '1'
  config.headers['X-Tenant-Id'] = tenantId
  return config
}, error => Promise.reject(error))

// 响应拦截器
instance.interceptors.response.use(response => {
  const { code, message, data } = response.data
  if (code === 200) {
    return data
  }
  // 401 → 清除登录状态
  if (code === 401) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    window.location.href = '/login'
    return Promise.reject(new Error(message || '未登录'))
  }
  // 403 → 无权限
  if (code === 403) {
    if (!response.config._silent) ElMessage.error('无权限执行此操作')
    return Promise.reject(new Error(message || '无权限'))
  }
  // 其他业务错误
  if (!response.config._silent) ElMessage.warning(message || '请求失败')
  return Promise.reject(new Error(message))
}, error => {
  if (error.config?._silent) return Promise.reject(error)
  if (error.response) {
    const status = error.response.status
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    } else if (status === 403) {
      ElMessage.error('无权限执行此操作')
    } else if (status >= 500) {
      ElMessage.error('服务器错误，请稍后重试')
    }
  } else {
    ElMessage.error('网络连接失败')
  }
  return Promise.reject(error)
})

export default instance
