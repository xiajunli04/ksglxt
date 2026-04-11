import request from '@/utils/request'

export function getDashboardStats() {
  return request.get('/dashboard/stats')
}

export function getRecentAnnouncements() {
  return request.get('/dashboard/recent-announcements')
}

export function getHotClassrooms() {
  return request.get('/dashboard/hot-classrooms')
}

export function getAvailableClassrooms() {
  return request.get('/dashboard/available-classrooms')
}

export function getRecentBookings() {
  return request.get('/dashboard/recent-bookings')
}
