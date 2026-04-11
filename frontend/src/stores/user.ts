import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { getUserInfo as getUserInfoApi } from '@/api/user'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<Record<string, unknown> | null>(null)
  const permissions = ref<string[]>([])

  const isLoggedIn = computed(() => !!token.value)
  const userRole = computed(() => {
    const roles = userInfo.value?.roles as string[] | undefined
    return roles && roles.length > 0 ? roles[0] : ''
  })
  const isAdmin = computed(() => userRole.value === 'ADMIN')

  async function login(userName: string, password: string) {
    const res = await loginApi({ userName, password })
    const data = res.data as { token: string; userInfo: Record<string, unknown> }
    token.value = data.token
    userInfo.value = data.userInfo
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
  }

  async function getUserInfo() {
    const res = await getUserInfoApi()
    const info = (res as Record<string, unknown>).data as Record<string, unknown>
    if (info) {
      info.roles = info.roles || ((info as Record<string, unknown>).role ? [(info as Record<string, unknown>).role] : [])
      userInfo.value = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      // ignore
    }
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/')
  }

  function loadFromLocal() {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        userInfo.value = JSON.parse(stored)
      } catch {
        userInfo.value = null
      }
    }
  }

  // Initialize from localStorage
  loadFromLocal()

  return {
    token,
    userInfo,
    permissions,
    isLoggedIn,
    userRole,
    isAdmin,
    login,
    getUserInfo,
    logout,
  }
})
