<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPostDetail, getComments, createComment, toggleLike } from '../api'
import { useUserStore } from '../store/user'
import Icon from '../components/Icon.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const comments = ref([])
const loading = ref(false)
const commentContent = ref('')
const submitting = ref(false)
const commentInputRef = ref(null)

const fetchPostDetail = async () => {
  loading.value = true
  try {
    const res = await getPostDetail(route.params.id)
    post.value = res
  } catch (e) {
    console.error('获取微博详情失败', e)
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await getComments({ postId: route.params.id })
    comments.value = res.comments || []
  } catch (e) {
    console.error('获取评论失败', e)
  }
}

const handleSubmitComment = async () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录', 'error')
    return
  }
  if (!commentContent.value.trim()) {
    showToast('请输入评论内容', 'error')
    return
  }
  submitting.value = true
  try {
    await createComment({
      postId: route.params.id,
      content: commentContent.value,
    })
    commentContent.value = ''
    showToast('评论成功')
    // 更新评论数
    if (post.value) {
      post.value.commentsCount = (post.value.commentsCount || 0) + 1
    }
    await fetchComments()
  } catch (e) {
    showToast('评论失败', 'error')
  } finally {
    submitting.value = false
  }
}

// 点赞功能
const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录', 'error')
    return
  }
  
  // 乐观更新：立即更新 UI
  const originalLiked = post.value.liked
  const originalLikes = post.value.likes
  post.value.liked = !post.value.liked
  post.value.likes += post.value.liked ? 1 : -1
  
  try {
    const res = await toggleLike(post.value.id)
    // 如果服务器返回不同结果，修正 UI
    if (res.liked !== post.value.liked) {
      post.value.liked = res.liked
      post.value.likes = res.likes
    }
  } catch (e) {
    // 请求失败，恢复原状
    post.value.liked = originalLiked
    post.value.likes = originalLikes
    showToast('操作失败', 'error')
  }
}

// 转发功能
const handleRepost = () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录', 'error')
    return
  }
  showToast('转发功能开发中')
}

// 聚焦评论输入框
const focusCommentInput = () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录', 'error')
    return
  }
  commentInputRef.value?.focus()
}

// 跳转到用户个人页
const goToProfile = (userId) => {
  if (userId) {
    router.push(`/profile/${userId}`)
  }
}

// Toast 提示
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
  return date.toLocaleString('zh-CN')
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchPostDetail()
  fetchComments()
})
</script>

<template>
  <div class="post-detail">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="!post" class="loading error">
      <p>加载失败，请稍后重试</p>
      <button class="retry-btn" @click="fetchPostDetail">重新加载</button>
    </div>
    <div v-else class="container">
      <!-- 悬浮返回按钮 -->
      <button class="fab-back-btn" @click="goBack">
        <Icon name="back" :size="16" />
        <span>返回</span>
      </button>

      <article class="article">
        <div class="meta">
          <div class="author-info" @click="goToProfile(post.author?.id)">
            <img
              :src="post.author?.avatar || '/default-avatar.png'"
              :alt="post.author?.username"
              class="avatar"
            />
            <div>
              <div class="author-name">{{ post.author?.username }}</div>
              <div class="date">{{ formatDate(post.createdAt) }}</div>
            </div>
          </div>
        </div>
        
        <div class="content">{{ post.content }}</div>
        
        <!-- 图片展示 -->
        <div v-if="post.images && post.images.length > 0" class="post-images" :class="`images-${post.images.length}`">
          <img
            v-for="(img, index) in post.images"
            :key="index"
            :src="img"
            :alt="`图片${index + 1}`"
            class="post-image"
          />
        </div>
        
        <!-- 互动数据 -->
        <div class="post-stats">
          <span>{{ post.views || 0 }} 阅读</span>
          <span>{{ post.commentsCount || 0 }} 评论</span>
          <span>{{ post.reposts || 0 }} 转发</span>
          <span>{{ post.likes || 0 }} 点赞</span>
        </div>
        
        <!-- 操作按钮 -->
        <div class="post-actions">
          <button class="action-btn" :class="{ liked: post.liked }" @click="handleLike">
            <Icon :name="post.liked ? 'like' : 'likeOutline'" :size="18" :filled="post.liked" />
            <span>点赞</span>
          </button>
          <button class="action-btn" @click="focusCommentInput">
            <Icon name="comment" :size="18" />
            <span>评论</span>
          </button>
          <button class="action-btn" @click="handleRepost">
            <Icon name="repost" :size="18" />
            <span>转发</span>
          </button>
        </div>
      </article>

      <div class="comments-section">
        <h2>评论 ({{ comments.length }})</h2>

        <div v-if="userStore.isLoggedIn" class="comment-form">
          <div class="comment-input-row">
            <input
              ref="commentInputRef"
              v-model="commentContent"
              placeholder="写下你的评论..."
              class="comment-input"
              @keyup.enter="handleSubmitComment"
            />
            <button 
              class="comment-submit-btn" 
              :class="{ 'active': commentContent.trim() && !submitting }"
              @click="handleSubmitComment" 
              :disabled="!commentContent.trim() || submitting"
            >
              {{ submitting ? '发送中...' : '发送' }}
            </button>
          </div>
        </div>
        <div v-else class="login-tip">请先登录后再评论</div>

        <div class="comments-list">
          <div v-if="comments.length === 0" class="empty">暂无评论</div>
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <img
              :src="comment.author?.avatar || '/default-avatar.png'"
              :alt="comment.author?.username"
              class="avatar"
              @click="goToProfile(comment.author?.id)"
            />
            <div class="comment-content">
              <div class="comment-header">
                <span class="author-name" @click="goToProfile(comment.author?.id)">{{ comment.author?.username }}</span>
                <span class="date">{{ formatDate(comment.createdAt) }}</span>
              </div>
              <p class="text">{{ comment.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.post-detail {
  min-height: 100vh;
  background: #f6f6f6;
  padding: 20px;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  color: #8c8c8c;
}

.loading.error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading.error p {
  color: #666;
  font-size: 16px;
}

.retry-btn {
  padding: 8px 24px;
  background: #1d9bf0;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.retry-btn:hover {
  background: #1a8cd8;
}

.container {
  max-width: 600px;
  margin: 0 auto;
  position: relative;
}

/* 悬浮返回按钮 - 固定在导航栏下方左侧 */
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

.fab-back-btn .arrow {
  font-size: 16px;
}

.article {
  background: white;
  border: 1px solid #e6e6e6;
  padding: 20px;
  margin-bottom: 10px;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.author-info:hover .author-name {
  color: #1d9bf0;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-weight: bold;
  color: #333;
  font-size: 15px;
}

.date {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 4px;
}

.content {
  line-height: 1.7;
  color: #333;
  font-size: 16px;
  margin-bottom: 16px;
  word-wrap: break-word;
  white-space: pre-wrap;
}

/* 图片展示 */
.post-images {
  display: grid;
  gap: 4px;
  margin-bottom: 16px;
  border-radius: 4px;
  overflow: hidden;
}

.post-images.images-1 {
  grid-template-columns: 1fr;
  max-width: 500px;
}

.post-images.images-2 {
  grid-template-columns: repeat(2, 1fr);
}

.post-images.images-3 {
  grid-template-columns: repeat(3, 1fr);
}

.post-images.images-4 {
  grid-template-columns: repeat(2, 1fr);
}

.post-images.images-5,
.post-images.images-6,
.post-images.images-7,
.post-images.images-8,
.post-images.images-9 {
  grid-template-columns: repeat(3, 1fr);
}

.post-image {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  cursor: pointer;
  transition: all 0.2s;
}

.post-image:hover {
  opacity: 0.9;
}

/* 互动数据 */
.post-stats {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
  color: #8c8c8c;
  font-size: 14px;
}

/* 操作按钮 */
.post-actions {
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: #8c8c8c;
  cursor: pointer;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f5f5f5;
  color: #1d9bf0;
}

.action-btn.liked {
  color: #f91880;
}

.action-btn .icon {
  font-size: 18px;
}

.comments-section {
  background: white;
  border: 1px solid #e6e6e6;
  padding: 20px;
}

.comments-section h2 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #333;
}

.comment-form {
  margin-bottom: 20px;
}

.comment-input-row {
  display: flex;
  gap: 12px;
  align-items: center;
  background: #f5f5f5;
  padding: 12px 16px;
  border-radius: 24px;
  border: 1px solid #e6e6e6;
  transition: all 0.2s;
}

.comment-input-row:focus-within {
  background: #fff;
  border-color: #1d9bf0;
}

.comment-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
  padding: 4px 0;
}

.comment-input::placeholder {
  color: #999;
}

.comment-submit-btn {
  padding: 6px 16px;
  background: #e6e6e6;
  color: #999;
  border: none;
  border-radius: 16px;
  cursor: not-allowed;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  white-space: nowrap;
}

.comment-submit-btn.active {
  background: #1d9bf0;
  color: white;
  cursor: pointer;
}

.comment-submit-btn.active:hover {
  background: #1a8cd8;
}

.login-tip {
  padding: 16px;
  background: #f5f5f5;
  border-radius: 4px;
  text-align: center;
  color: #8c8c8c;
  margin-bottom: 20px;
  font-size: 14px;
}

.comments-list .empty {
  text-align: center;
  padding: 40px;
  color: #8c8c8c;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item .avatar {
  width: 40px;
  height: 40px;
  cursor: pointer;
}

.comment-item .avatar:hover {
  opacity: 0.8;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-header .author-name {
  font-weight: bold;
  color: #333;
  font-size: 14px;
  cursor: pointer;
}

.comment-header .author-name:hover {
  color: #1d9bf0;
}

.comment-header .date {
  font-size: 13px;
  color: #8c8c8c;
}

.comment-content .text {
  margin: 0;
  color: #333;
  line-height: 1.6;
  font-size: 14px;
}

/* Toast 提示 */
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
