import request from './index'

export const ticketApi = {
  getList(params) { return request.get('/ticket/list', { params }) },
  getDetail(id) { return request.get(`/ticket/${id}`) },
  checkIn(ticketNo) { return request.put(`/ticket/check/${ticketNo}`) }
}
