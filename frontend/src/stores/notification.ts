import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getUnreadCount, markRead as markReadApi, markAllRead as markAllReadApi } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const hasUnread = computed(() => unreadCount.value > 0)

  let pollTimer: ReturnType<typeof setInterval> | null = null

  async function fetchUnreadCount() {
    try {
      const res = await getUnreadCount()
      unreadCount.value = (res.data as number) || 0
    } catch {
      // silently fail
    }
  }

  function startPolling(interval = 30000) {
    stopPolling()
    fetchUnreadCount()
    pollTimer = setInterval(fetchUnreadCount, interval)
  }

  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  async function markRead(id: number) {
    await markReadApi(id)
    await fetchUnreadCount()
  }

  async function markAllRead() {
    await markAllReadApi()
    unreadCount.value = 0
  }

  return {
    unreadCount,
    hasUnread,
    fetchUnreadCount,
    startPolling,
    stopPolling,
    markRead,
    markAllRead,
  }
})
