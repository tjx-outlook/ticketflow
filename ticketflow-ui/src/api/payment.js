import request from './index'

export const paymentApi = {
  /** 创建支付 → 返回支付宝真实二维码 */
  create(data) { return request.post('/payment/create', data) },
  /** 查询支付状态 → 前端轮询用（静默，不弹错误提示） */
  getStatus(orderNo) { return request.get(`/payment/status/${orderNo}`, { _silent: true }) }
}
