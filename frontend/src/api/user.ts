import request from '@/utils/request'

export function getUserInfo() {
  return request.get('/user/info')
}

export function updateProfile(data: Record<string, unknown>) {
  return request.put('/user/info', data)
}

export function updatePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/user/password', data)
}

export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getUserList(params: Record<string, unknown>) {
  return request.get('/user/list', { params })
}

export function updateUserStatus(id: number, status: string) {
  return request.put(`/user/${id}/status`, null, { params: { status } })
}
