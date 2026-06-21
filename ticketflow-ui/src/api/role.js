import request from './index'

export const roleApi = {
  getList(params) { return request.get('/role/list', { params }) },
  getAll() { return request.get('/role/all') },
  create(data) { return request.post('/role', data) },
  update(id, data) { return request.put(`/role/${id}`, data) },
  assignRoles(userId, roleIds) { return request.post(`/role/assign?userId=${userId}`, roleIds) },
  getUserRoles(userId) { return request.get(`/role/user/${userId}`) }
}
