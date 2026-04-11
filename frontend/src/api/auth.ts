import request from '@/utils/request'

export function login(data: { userName: string; password: string }) {
  return request.post('/auth/login', data)
}

export function register(data: {
  userName: string
  password: string
  nickName: string
  phone: string
  email: string
  roleCode: string
}) {
  return request.post('/auth/register', data)
}

export function logout() {
  return request.post('/auth/logout')
}
