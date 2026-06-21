import request from './index'

export const eventApi = {
  getList(params) { return request.get('/event/list', { params }) },
  getDetail(id) { return request.get(`/event/${id}`) },
  create(data) { return request.post('/event', data) },
  update(id, data) { return request.put(`/event/${id}`, data) },
  updateStatus(id, data) { return request.put(`/event/${id}/status`, data) },
  delete(id) { return request.delete(`/event/${id}`) }
}
