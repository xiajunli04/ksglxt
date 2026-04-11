import request from '@/utils/request'

export function createBooking(data: Record<string, unknown>) {
  return request.post('/booking', data)
}

export function getMyBookings(params: Record<string, unknown>) {
  return request.get('/booking/my', { params })
}

export function getBookingDetail(id: number) {
  return request.get(`/booking/${id}`)
}

export function cancelBooking(id: number) {
  return request.put(`/booking/${id}/cancel`)
}

export function getSlotBooking(classroomId: number, date: string, timeSlotId: number) {
  return request.get('/booking/slot-info', { params: { classroomId, date, timeSlotId } })
}
