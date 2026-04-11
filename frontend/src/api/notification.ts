import request from '@/utils/request'

export function getNotificationList(params: Record<string, unknown>) {
  return request.get('/notification/list', { params })
}

export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

export function markRead(id: number) {
  return request.put(`/notification/${id}/read`)
}

export function markAllRead() {
  return request.put('/notification/read-all')
}
