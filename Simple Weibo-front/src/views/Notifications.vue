<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { getNotifications, markNotificationAsRead, markAllNotificationsAsRead } from '../api'
import Icon from '../components/Icon.vue'

const router = useRouter()
const userStore = useUserStore()

const notifications = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const unreadCount = ref(0)
let pollingInterval = null

const hasUnread = computed(() => unreadCount.value > 0)

const fetchNotifications = async (silent = false) => {
  if (!silent) {
    loading.value = true
  }
  try {
    const res = await getNotifications({ page: currentPage.value, pageSize: pageSize.value })
    notifications.value = res.notifications || []
    total.value = res.total || 0
    unreadCount.value = res.unreadCount || 0
    userStore.setUnreadNotifications(unreadCount.value)
  } catch (e) {
    console.error('获取通知失败', e)
  } finally {
    if (!silent) {
      loading.value = false
    }
  }
}

const startPolling = () => {
  if (pollingInterval) return
  pollingInterval = setInterval(() => {
    fetchNotifications(true)
  }, 10000)
}

const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval)
    pollingInterval = null
  }
}

const handleNotificationClick = async (notification) => {
  if (!notification.read) {
    try {
      await markNotificationAsRead(notification.id)
      notification.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      userStore.setUnreadNotifications(unreadCount.value)
    } catch (e) {
      console.error('标记通知已读失败', e)
    }
  }

  if (notification.postId) {
    router.push(`/post/${notification.postId}`)
  } else if (notification.userId) {
    router.push(`/profile/${notification.userId}`)
  }
}

const handleMarkAllRead = async () => {
  if (!hasUnread.value) return
  
  try {
    await markAllNotificationsAsRead()
    notifications.value.forEach(n => n.read = true)
    unreadCount.value = 0
    userStore.setUnreadNotifications(0)
    showToast('已全部标记为已读')
  } catch (e) {
    console.error('标记全部已读失败', e)
    showToast('操作失败，请重试', 'error')
  }
}

const getNotificationIcon = (type) => {
  switch (type) {
    case 'like':
      return 'like'
    case 'comment':
      return 'comment'
    case 'repost':
      return 'repost'
    case 'follow':
      return 'user'
    default:
      return 'logo'
  }
}

const getNotificationText = (notification) => {
  const username = notification.fromUser?.username || '某用户'
  switch (notification.type) {
    case 'like':
      return `${username} 赞了你的微博`
    case 'comment':
      return `${username} 评论了你的微博：${notification.content || ''}`
    case 'repost':
      return `${username} 转发了你的微博`
    case 'follow':
      return `${username} 关注了你`
    default:
      return notification.content || '新通知'
  }
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

const showToast = (message, type = 'success') => {
  const toast = document.createElement('div')
  toast.className = `toast toast-${type}`
  toast.textContent = message
  document.body.appendChild(toast)
  
  setTimeout(() => toast.classList.add('show'), 10)
  
  setTimeout(() => {
    toast.classList.remove('show')
    setTimeout(() => document.body.removeChild(toast), 300)
  }, 2000)
}

const goBack = () => {
  router.push('/')
}

const prevPage = () => {
  currentPage.value--
  fetchNotifications()
  window.scrollTo(0, 0)
}

const nextPage = () => {
  currentPage.value++
  fetchNotifications()
  window.scrollTo(0, 0)
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/')
    return
  }
  fetchNotifications()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div class="notifications">
    <div class="container">
      <button class="fab-back-btn" @click="goBack">
        <Icon name="back" :size="16" />
        <span>返回</span>
      </button>

      <div class="header-section">
        <h1>通知</h1>
        <button 
          v-if="hasUnread" 
          class="mark-all-btn" 
          @click="handleMarkAllRead"
        >
          全部标记为已读
        </button>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="notifications.length === 0" class="empty">
        <Icon name="logo" :size="48" />
        <p>暂无通知</p>
      </div>
      <div v-else class="notifications-list">
        <div 
          v-for="notification in notifications" 
          :key="notification.id" 
          class="notification-item"
          :class="{ unread: !notification.read }"
          @click="handleNotificationClick(notification)"
        >
          <div class="notification-icon" :class="`type-${notification.type}`">
            <Icon :name="getNotificationIcon(notification.type)" :size="20" />
          </div>
          <div class="notification-content">
            <div class="notification-header">
              <img
                :src="notification.fromUser?.avatar || '/default-avatar.png'"
                :alt="notification.fromUser?.username"
                class="avatar"
              />
              <div class="notification-text">
                <p class="text">{{ getNotificationText(notification) }}</p>
                <span class="time">{{ formatDate(notification.createdAt) }}</span>
              </div>
            </div>
            <div v-if="notification.postContent" class="post-preview">
              {{ notification.postContent }}
            </div>
          </div>
          <div v-if="!notification.read" class="unread-dot"></div>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination">
        <button :disabled="currentPage === 1" @click="prevPage">上一页</button>
        <span>{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
        <button :disabled="currentPage >= Math.ceil(total / pageSize)" @click="nextPage">
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notifications {
  min-height: 100vh;
  background: #f6f6f6;
  padding: 20px 0;
}

.container {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
}

.fab-back-btn {
  position: fixed;
  top: 80px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: #fff;
  border: 1px solid #e6e6e6;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  z-index: 99;
  transition: all 0.2s;
}

.fab-back-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.header-section {
  background: white;
  border: 1px solid #e6e6e6;
  padding: 20px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-section h1 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.mark-all-btn {
  padding: 8px 16px;
  background: #1d9bf0;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.mark-all-btn:hover {
  background: #1a8cd8;
}

.loading,
.empty {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border: 1px solid #e6e6e6;
  color: #8c8c8c;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty p {
  margin: 0;
  font-size: 15px;
}

.notifications-list {
  display: grid;
  gap: 0;
}

.notification-item {
  background: white;
  padding: 16px;
  display: flex;
  gap: 12px;
  border: 1px solid #e6e6e6;
  border-top: none;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.notification-item:first-child {
  border-top: 1px solid #e6e6e6;
}

.notification-item:hover {
  background: #f5f5f5;
}

.notification-item.unread {
  background: #f0f8ff;
}

.notification-item.unread:hover {
  background: #e6f3ff;
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-icon.type-like {
  background: #ffe6f0;
  color: #f91880;
}

.notification-icon.type-comment {
  background: #e6f3ff;
  color: #1d9bf0;
}

.notification-icon.type-repost {
  background: #e6ffe6;
  color: #17bf63;
}

.notification-icon.type-follow {
  background: #fff0e6;
  color: #ff8200;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.notification-text {
  flex: 1;
  min-width: 0;
}

.notification-text .text {
  margin: 0 0 4px 0;
  color: #333;
  font-size: 15px;
  line-height: 1.5;
  word-wrap: break-word;
}

.notification-text .time {
  font-size: 13px;
  color: #8c8c8c;
}

.post-preview {
  background: #f5f5f5;
  padding: 10px 12px;
  border-radius: 4px;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-top: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #1d9bf0;
  border-radius: 50%;
  position: absolute;
  top: 20px;
  right: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 10px;
  padding: 16px;
  background: white;
  border: 1px solid #e6e6e6;
}

.pagination button {
  padding: 6px 16px;
  border: 1px solid #e6e6e6;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  color: #333;
  font-size: 14px;
}

.pagination button:hover:not(:disabled) {
  background: #1d9bf0;
  color: white;
  border-color: #1d9bf0;
}

.pagination button:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.pagination span {
  color: #8c8c8c;
  font-size: 14px;
}

:global(.toast) {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%) translateY(-20px);
  padding: 12px 24px;
  border-radius: 4px;
  color: white;
  font-size: 14px;
  z-index: 9999;
  opacity: 0;
  transition: all 0.3s;
}

:global(.toast.show) {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

:global(.toast-success) {
  background: #e3f2fd;
  color: #1976d2;
  border: 1px solid #bbdefb;
}

:global(.toast-error) {
  background: #ffebee;
  color: #d32f2f;
  border: 1px solid #ffcdd2;
}
</style>
