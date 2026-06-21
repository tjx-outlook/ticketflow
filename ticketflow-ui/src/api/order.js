import request from './index'

export const orderApi = {
  seckill(eventId, seatIds) {
    const params = seatIds?.length ? { seatIds: seatIds.join(',') } : {}
    return request.post(`/order/seckill/${eventId}`, null, { params })
  },
  getList(params) { return request.get('/order/list', { params }) },
  getDetail(id) { return request.get(`/order/${id}`) },
  cancel(orderNo) { return request.put(`/order/cancel/${orderNo}`) },
  confirmPay(orderNo) { return request.put(`/order/pay/${orderNo}`) },
  checkPaid(eventId) { return request.get('/order/check-paid', { params: { eventId } }) }
}
