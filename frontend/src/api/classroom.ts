import request from '@/utils/request'

export function getClassroomList(params: Record<string, unknown>) {
  return request.get('/classroom/list', { params })
}

export function getClassroomDetail(id: number) {
  return request.get(`/classroom/${id}`)
}

export function getAvailableSlots(id: number, date: string) {
  return request.get(`/classroom/${id}/slots`, { params: { date } })
}

export function addClassroom(data: Record<string, unknown>) {
  return request.post('/classroom', data)
}

export function updateClassroom(id: number, data: Record<string, unknown>) {
  return request.put(`/classroom/${id}`, data)
}

export function deleteClassroom(id: number) {
  return request.delete(`/classroom/${id}`)
}
