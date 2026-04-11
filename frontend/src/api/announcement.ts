import request from '@/utils/request'

export function getAnnouncementList(params: Record<string, unknown>) {
  return request.get('/announcement/list', { params })
}

export function getAnnouncementDetail(id: number) {
  return request.get(`/announcement/${id}`)
}

export function addAnnouncement(data: Record<string, unknown>) {
  return request.post('/announcement', data)
}

export function updateAnnouncement(id: number, data: Record<string, unknown>) {
  return request.put(`/announcement/${id}`, data)
}

export function deleteAnnouncement(id: number) {
  return request.delete(`/announcement/${id}`)
}
