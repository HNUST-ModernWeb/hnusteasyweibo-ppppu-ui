import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const token = ref(localStorage.getItem('token') || '')
  const unreadNotifications = ref(0)

  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  function setUserInfo(info, tokenStr) {
    userInfo.value = info
    token.value = tokenStr
    localStorage.setItem('userInfo', JSON.stringify(info))
    localStorage.setItem('token', tokenStr)
  }

  function updateUserInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  function setUnreadNotifications(count) {
    unreadNotifications.value = count
  }

  function logout() {
    userInfo.value = null
    token.value = ''
    unreadNotifications.value = 0
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    unreadNotifications,
    setUserInfo,
    updateUserInfo,
    setUnreadNotifications,
    logout,
  }
})
