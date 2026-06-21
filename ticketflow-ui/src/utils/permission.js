/**
 * 权限码常量 — 和后端 sys_permission 表一致
 */
export const PERM = {
  USER_LIST: 'user:list',
  USER_MANAGE: 'user:manage',
  ROLE_MANAGE: 'role:manage',
  EVENT_MANAGE: 'event:manage',
  SEAT_MANAGE: 'seat:manage',
  ORDER_MANAGE: 'order:manage',
  ORDER_SECKILL: 'order:seckill',
  TICKET_MANAGE: 'ticket:manage',
  MERCHANT_MANAGE: 'merchant:manage',
  STATISTICS_VIEW: 'statistics:view'
}

/** 从 userStore 获取权限检查函数 */
export function createPermissionGuard(userStore) {
  return {
    /** 检查是否有某个权限 */
    has(perm) {
      return userStore.hasPermission(perm)
    },
    /** 检查是否有某个角色 */
    hasRole(role) {
      return userStore.hasRole(role)
    },
    /** 检查是否有任一权限 */
    hasAny(...perms) {
      const roles = userStore.roles || []
      if (roles.includes('SUPER_ADMIN')) return true
      return perms.some(p => userStore.hasPermission(p))
    }
  }
}
