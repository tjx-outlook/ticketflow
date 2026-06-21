import request from './index'

export const seatApi = {
  getByEvent(eventId) { return request.get(`/seat/list/${eventId}`) },
  pageByEvent(params) { return request.get(`/seat/page/${params.eventId}`, { params }) },
  batchCreate(data) { return request.post('/seat/batch', data) }
}
