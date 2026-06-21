import request from './index'

export const statisticsApi = {
  getByEvent(eventId) { return request.get(`/statistics/sales/${eventId}`) },
  getTotal() { return request.get('/statistics/sales/total') }
}
