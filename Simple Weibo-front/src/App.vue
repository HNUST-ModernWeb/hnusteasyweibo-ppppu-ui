<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from './store/user'
import { getUnreadNotificationsCount } from './api'
import AuthDialog from './components/AuthDialog.vue'
import Icon from './components/Icon.vue'

const router = useRouter()
const userStore = useUserStore()
const showAuthDialog = ref(false)
const showUserDropdown = ref(false)
const previousUnreadCount = ref(0)
let pollingInterval = null

const goHome = () => {
  router.push('/')
}

const goToNotifications = () => {
  if (userStore.isLoggedIn) {
    router.push('/notifications')
  } else {
    showAuthDialog.value = true
  }
}

const goToProfile = () => {
  if (userStore.isLoggedIn) {
    router.push('/profile')
    showUserDropdown.value = false
  } else {
    showAuthDialog.value = true
  }
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    userStore.logout()
    showUserDropdown.value = false
    router.push('/')
    stopPolling()
  }
}

const handleAuthSuccess = () => {
  fetchUnreadCount()
  startPolling()
}

const fetchUnreadCount = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getUnreadNotificationsCount()
    const newCount = res.unreadCount || 0
    
    if (newCount > previousUnreadCount.value && previousUnreadCount.value > 0) {
      showNewNotificationToast(newCount - previousUnreadCount.value)
    }
    
    previousUnreadCount.value = newCount
    userStore.setUnreadNotifications(newCount)
  } catch (e) {
    console.error('获取未读通知数量失败', e)
  }
}

const showNewNotificationToast = (count) => {
  const toast = document.createElement('div')
  toast.className = 'toast toast-notification'
  toast.innerHTML = `
    <div class="toast-content">
      <span class="toast-icon">🔔</span>
      <span class="toast-text">你有 ${count} 条新通知</span>
    </div>
  `
  document.body.appendChild(toast)
  
  setTimeout(() => toast.classList.add('show'), 10)
  
  toast.addEventListener('click', () => {
    goToNotifications()
    toast.classList.remove('show')
    setTimeout(() => document.body.removeChild(toast), 300)
  })
  
  setTimeout(() => {
    toast.classList.remove('show')
    setTimeout(() => {
      if (document.body.contains(toast)) {
        document.body.removeChild(toast)
      }
    }, 300)
  }, 5000)
}

const startPolling = () => {
  if (pollingInterval) return
  pollingInterval = setInterval(fetchUnreadCount, 10000)
}

const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval)
    pollingInterval = null
  }
}

watch(() => userStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn) {
    fetchUnreadCount()
    startPolling()
  } else {
    stopPolling()
    previousUnreadCount.value = 0
  }
})

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchUnreadCount()
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div id="app">
    <header class="header">
      <div class="container">
        <div class="logo" @click="goHome">
          <Icon name="logo" :size="24" />
          <span class="text">微博</span>
        </div>
        <nav class="nav">
          <a @click="goHome">首页</a>
          <a v-if="userStore.isLoggedIn" @click="goToNotifications" class="notification-link">
            通知
            <span v-if="userStore.unreadNotifications > 0" class="notification-badge">
              {{ userStore.unreadNotifications > 99 ? '99+' : userStore.unreadNotifications }}
            </span>
          </a>
          <div v-if="userStore.isLoggedIn" class="user-dropdown">
            <a class="user-link" @mouseenter="showUserDropdown = true">
              <img
                :src="userStore.userInfo?.avatar || '/default-avatar.png'"
                :alt="userStore.userInfo?.username"
                class="avatar"
              />
              <span>{{ userStore.userInfo?.username }}</span>
              <Icon name="arrowDown" :size="10" class="dropdown-arrow" />
            </a>
            <div 
              v-show="showUserDropdown" 
              class="dropdown-menu"
              @mouseenter="showUserDropdown = true"
              @mouseleave="showUserDropdown = false"
            >
              <a @click="goToProfile" class="dropdown-item">个人主页</a>
              <a @click="handleLogout" class="dropdown-item logout">退出登录</a>
            </div>
          </div>
          <a v-else @click="showAuthDialog = true" class="login-btn">登录</a>
        </nav>
      </div>
    </header>

    <main class="main">
      <RouterView />
    </main>

    <footer class="footer">
      <div class="container">
        <p>&copy; 2026 微博. All rights reserved.</p>
        <p class="links">
          <a href="#">关于</a>
          <a href="#">帮助中心</a>
          <a href="#">隐私政策</a>
        </p>
      </div>
    </footer>

    <AuthDialog v-model:visible="showAuthDialog" @success="handleAuthSuccess" />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.logo .icon {
  font-size: 24px;
}

.nav {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav a {
  color: #666;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.3s;
  font-size: 15px;
  position: relative;
}

.nav a:hover {
  color: #ff8200;
}

.nav a.active {
  color: #ff8200;
  font-weight: 500;
}

.nav a.active::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 0;
  right: 0;
  height: 2px;
  background: #ff8200;
  border-radius: 1px;
}

.notification-link {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0;
}

.notification-badge {
  position: relative;
  top: auto;
  right: auto;
  background: #ff4757;
  color: white;
  font-size: 10px;
  font-weight: bold;
  padding: 2px 5px;
  border-radius: 10px;
  min-width: 16px;
  height: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  box-shadow: 0 2px 4px rgba(255, 71, 87, 0.3);
  margin-left: 2px;
}

.nav .user-link {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav .avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.nav .login-btn {
  padding: 8px 16px;
  background: #ff8200;
  color: white !important;
  border-radius: 20px;
  transition: background 0.3s;
}

.nav .login-btn:hover {
  background: #ff9a33;
  color: white !important;
}

/* 用户下拉菜单 */
.user-dropdown {
  position: relative;
}

.user-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.dropdown-arrow {
  font-size: 10px;
  margin-left: 4px;
  transition: transform 0.2s;
}

.user-dropdown:hover .dropdown-arrow {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  min-width: 120px;
  padding: 8px 0;
  z-index: 1000;
}

.dropdown-item {
  display: block;
  padding: 10px 16px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.dropdown-item:hover {
  background: #f5f5f5;
  color: #333;
}

.dropdown-item.logout {
  color: #666;
  border-top: 1px solid #f0f0f0;
  margin-top: 4px;
}

.dropdown-item.logout:hover {
  background: #fff5f5;
  color: #ff4757;
}

.main {
  flex: 1;
}

.footer {
  background: #333;
  color: white;
  padding: 30px 20px;
  margin-top: 60px;
}

.footer .container {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer p {
  margin: 8px 0;
  font-size: 14px;
}

.footer .links {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.footer a {
  color: #999;
  text-decoration: none;
  transition: color 0.3s;
}

.footer a:hover {
  color: white;
}

/* 新通知提示 */
:global(.toast-notification) {
  position: fixed;
  top: 80px;
  right: 20px;
  background: white;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 0;
  min-width: 280px;
  cursor: pointer;
  z-index: 9999;
  opacity: 0;
  transform: translateX(20px);
  transition: all 0.3s ease;
}

:global(.toast-notification.show) {
  opacity: 1;
  transform: translateX(0);
}

:global(.toast-notification:hover) {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

:global(.toast-notification .toast-content) {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}

:global(.toast-notification .toast-icon) {
  font-size: 24px;
  flex-shrink: 0;
}

:global(.toast-notification .toast-text) {
  color: #333;
  font-size: 15px;
  font-weight: 500;
}
</style>
