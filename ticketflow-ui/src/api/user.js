import request from './index'

export const userApi = {
  login(data) { return request.post('/user/login', data) },
  register(data) { return request.post('/user/register', data) },
  getUserInfo() { return request.get('/user/info') },
  updatePassword(data) { return request.put('/user/password', data) },
  getList(params) { return request.get('/user/list', { params }) },
  updateStatus(userId, data) { return request.put(`/user/status/${userId}`, data) }
}
