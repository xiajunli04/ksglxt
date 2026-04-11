import request from '@/utils/request'

export function getPendingList(params: Record<string, unknown>) {
  return request.get('/approval/pending', { params })
}

export function getAllList(params: Record<string, unknown>) {
  return request.get('/approval/all', { params })
}

export function approve(id: number) {
  return request.put(`/approval/${id}/approve`)
}

export function reject(id: number, reason: string) {
  return request.put(`/approval/${id}/reject`, null, { params: { reason } })
}
