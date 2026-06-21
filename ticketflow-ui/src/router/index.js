import { createRouter, createWebHistory } from 'vue-router'
import { PERM } from '../utils/permission'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册', noAuth: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/Forbidden.vue'),
    meta: { title: '无权限', noAuth: true }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/events',
    children: [
      // 用户端 — 无需登录即可浏览
      { path: 'events', name: 'UserEvents', component: () => import('../views/user/Events.vue'), meta: { title: '活动列表', noAuth: true } },
      { path: 'events/:id', name: 'EventDetail', component: () => import('../views/user/EventDetail.vue'), meta: { title: '活动详情', noAuth: true } },
      { path: 'stand-seat', name: 'StandSeat', component: () => import('../views/user/StandSeat.vue'), meta: { title: '看台选座', noAuth: true } },
      { path: 'my-tickets', name: 'MyTickets', component: () => import('../views/user/MyTickets.vue'), meta: { title: '我的票' } },
      { path: 'my-orders', name: 'MyOrders', component: () => import('../views/user/MyOrders.vue'), meta: { title: '我的订单' } },
      { path: 'personal-center', name: 'PersonalCenter', component: () => import('../views/user/PersonalCenter.vue'), meta: { title: '个人中心' } },
      // 管理员 — 需要对应权限
      { path: 'admin/users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue'), meta: { title: '用户管理', perm: PERM.USER_MANAGE } },
      { path: 'admin/roles', name: 'AdminRoles', component: () => import('../views/admin/Roles.vue'), meta: { title: '角色管理', perm: PERM.ROLE_MANAGE } },
      { path: 'admin/merchants', name: 'AdminMerchants', component: () => import('../views/admin/Merchants.vue'), meta: { title: '商家管理', perm: PERM.MERCHANT_MANAGE } },
      // 商家
      { path: 'merchant/events', name: 'MerchantEvents', component: () => import('../views/merchant/Events.vue'), meta: { title: '活动管理', perm: PERM.EVENT_MANAGE } },
      { path: 'merchant/seats', name: 'MerchantSeats', component: () => import('../views/merchant/Seats.vue'), meta: { title: '座位管理', perm: PERM.SEAT_MANAGE } },
      // 统计 — 需要 statistics:view 权限
      { path: 'statistics', name: 'Statistics', component: () => import('../views/Statistics.vue'), meta: { title: '数据统计', perm: PERM.STATISTICS_VIEW } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = (to.meta.title || 'TicketFlow') + ' - 票务系统'
  const token = localStorage.getItem('token')

  // 已登录 → 不进登录/注册页
  if ((to.path === '/login' || to.path === '/register') && token) {
    next('/events')
    return
  }

  // 未登录 → 只能进公开页（活动列表/详情），其余重定向回活动列表
  if (!to.meta.noAuth && !token) {
    next('/events')
    return
  }

  // 权限检查 — 从缓存读取（Layout onMounted 会从后端刷新）
  if (token && to.meta.perm) {
    try {
      const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const roles = info.roles || []
      const perms = info.permissions || []

      if (roles.includes('SUPER_ADMIN')) {
        next(); return
      }
      if (!perms.includes(to.meta.perm)) {
        next('/403'); return
      }
    } catch (e) {
      next('/403'); return
    }
  }

  next()
})

export default router
