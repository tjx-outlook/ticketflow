import request from './index'

export const permissionApi = {
  getTree() { return request.get('/permission/tree') },
  create(data) { return request.post('/permission', data) },
  assignPermissions(roleId, permIds) { return request.post(`/permission/assign?roleId=${roleId}`, permIds) },
  getRolePermissions(roleId) { return request.get(`/permission/role/${roleId}`) }
}
