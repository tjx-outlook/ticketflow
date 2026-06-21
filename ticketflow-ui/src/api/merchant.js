import request from './index'

export const merchantApi = {
  getList(params) { return request.get('/merchant/list', { params }) },
  getDetail(id) { return request.get(`/merchant/${id}`) },
  create(data) { return request.post('/merchant', data) },
  update(id, data) { return request.put(`/merchant/${id}`, data) },
  delete(id) { return request.delete(`/merchant/${id}`) }
}
