<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getPosts, createPost, toggleLike, uploadImage } from '../api'
import { useUserStore } from '../store/user'
import Icon from '../components/Icon.vue'

const router = useRouter()
const userStore = useUserStore()
const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const newPostContent = ref('')
const posting = ref(false)
const selectedImages = ref([])
const imagePreviewUrls = ref([])
const textareaRef = ref(null)
const hasUnsavedContent = ref(false)

// 字数统计
const charCount = computed(() => newPostContent.value.length)
const isOverLimit = computed(() => charCount.value > 280)
const canPublish = computed(() => {
  return newPostContent.value.trim().length > 0 && !isOverLimit.value && !posting.value
})

// 监听内容变化
watch(newPostContent, (val) => {
  hasUnsavedContent.value = val.trim().length > 0
})

const fetchPosts = async () => {
  loading.value = true
  try {
    const res = await getPosts({ page: currentPage.value, pageSize: pageSize.value })
    posts.value = res.posts || []
    total.value = res.total || 0
  } catch (e) {
    console.error('获取微博列表失败', e)
  } finally {
    loading.value = false
  }
}

const publishPost = async () => {
  if (!canPublish.value) return
  
  posting.value = true
  try {
    await createPost({
      title: newPostContent.value.substring(0, 30),
      content: newPostContent.value,
      summary: newPostContent.value,
      images: imagePreviewUrls.value,
    })
    
    // 成功提示
    showToast('发布成功！')
    
    // 清空内容
    newPostContent.value = ''
    selectedImages.value = []
    imagePreviewUrls.value = []
    hasUnsavedContent.value = false
    
    // 刷新列表
    currentPage.value = 1
    await fetchPosts()
  } catch (e) {
    showToast('发布失败，请重试', 'error')
  } finally {
    posting.value = false
  }
}

const handleImageSelect = async (event) => {
  const files = Array.from(event.target.files)
  if (files.length + selectedImages.value.length > 9) {
    showToast('最多只能上传9张图片', 'error')
    event.target.value = ''
    return
  }
  
  for (const file of files) {
    if (!file.type.startsWith('image/')) {
      showToast('只能上传图片文件', 'error')
      continue
    }
    
    if (file.size > 5 * 1024 * 1024) {
      showToast('图片大小不能超过5MB', 'error')
      continue
    }
    
    // 先显示本地预览
    const reader = new FileReader()
    reader.onload = (e) => {
      selectedImages.value.push({
        file,
        preview: e.target.result,
        uploading: true,
        url: null
      })
    }
    reader.readAsDataURL(file)
    
    // 上传到服务器
    try {
      const res = await uploadImage(file)
      const uploadedUrl = res.url || res.data?.url || res
      
      // 更新为服务器URL
      const index = selectedImages.value.findIndex(img => img.file === file)
      if (index !== -1) {
        selectedImages.value[index].uploading = false
        selectedImages.value[index].url = uploadedUrl
        imagePreviewUrls.value.push(uploadedUrl)
      }
    } catch (e) {
      console.error('图片上传失败', e)
      showToast('图片上传失败，请重试', 'error')
      // 移除上传失败的图片
      const index = selectedImages.value.findIndex(img => img.file === file)
      if (index !== -1) {
        selectedImages.value.splice(index, 1)
      }
    }
  }
  
  event.target.value = ''
}

const removeImage = (index) => {
  const image = selectedImages.value[index]
  if (image && image.url) {
    const urlIndex = imagePreviewUrls.value.indexOf(image.url)
    if (urlIndex !== -1) {
      imagePreviewUrls.value.splice(urlIndex, 1)
    }
  }
  selectedImages.value.splice(index, 1)
}

const triggerImageUpload = () => {
  document.getElementById('image-upload').click()
}

// 处理图片URL
const getImageUrl = (url) => {
  console.log('处理图片URL:', url)
  if (!url) {
    console.log('URL为空')
    return ''
  }
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http')) {
    console.log('完整URL，直接返回:', url)
    return url
  }
  // 如果是相对路径，拼接后端地址
  if (url.startsWith('/')) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000'
    const fullUrl = baseUrl + url
    console.log('相对路径，拼接后:', fullUrl)
    return fullUrl
  }
  console.log('其他格式，原样返回:', url)
  return url
}

// 图片加载失败处理
const handleImageError = (e) => {
  e.target.style.display = 'none'
  // 显示默认图片或占位符
  const wrapper = e.target.parentElement
  if (wrapper) {
    wrapper.classList.add('image-error')
    wrapper.innerHTML = '<span class="image-placeholder">图片加载失败</span>'
  }
}

// 快捷键发布
const handleKeydown = (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
    e.preventDefault()
    publishPost()
  }
}

// 简单的 Toast 提示
const showToast = (message, type = 'success') => {
  const toast = document.createElement('div')
  toast.className = `toast toast-${type}`
  toast.textContent = message
  document.body.appendChild(toast)
  
  setTimeout(() => {
    toast.classList.add('show')
  }, 10)
  
  setTimeout(() => {
    toast.classList.remove('show')
    setTimeout(() => {
      document.body.removeChild(toast)
    }, 300)
  }, 2000)
}

const goToDetail = (postId) => {
  router.push(`/post/${postId}`)
}

const goToProfile = (userId) => {
  if (userId) {
    router.push(`/profile/${userId}`)
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

// 点赞功能
const handleLike = async (post) => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录', 'error')
    return
  }
  
  // 乐观更新：立即更新 UI
  const originalLiked = post.liked
  const originalLikes = post.likes
  post.liked = !post.liked
  post.likes += post.liked ? 1 : -1
  
  try {
    const res = await toggleLike(post.id)
    // 如果服务器返回不同结果，修正 UI
    if (res.liked !== post.liked) {
      post.liked = res.liked
      post.likes = res.likes
    }
  } catch (e) {
    // 请求失败，恢复原状
    post.liked = originalLiked
    post.likes = originalLikes
    console.error('点赞失败:', e)
    const errorMsg = e.response?.data?.message || e.message || '操作失败'
    showToast(errorMsg, 'error')
  }
}

// 转发功能
const handleRepost = (post) => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录', 'error')
    return
  }
  showToast('转发功能开发中')
}

// 评论功能
const handleComment = (postId) => {
  goToDetail(postId)
}

const prevPage = () => {
  currentPage.value--
  fetchPosts()
  window.scrollTo(0, 0)
}

const nextPage = () => {
  currentPage.value++
  fetchPosts()
  window.scrollTo(0, 0)
}

// 页面离开确认
const handleBeforeUnload = (e) => {
  if (hasUnsavedContent.value) {
    e.preventDefault()
    e.returnValue = '确定放弃编辑？'
    return e.returnValue
  }
}

onMounted(() => {
  fetchPosts()
  window.addEventListener('beforeunload', handleBeforeUnload)
})

</script>

<template>
  <div class="weibo">
    <div class="container">
      <!-- 发布框 - 微博风格 -->
      <div v-if="userStore.isLoggedIn" class="publish-box">
        <div class="publish-main">
          <img
            :src="userStore.userInfo?.avatar || '/default-avatar.png'"
            :alt="userStore.userInfo?.username"
            class="avatar"
          />
          <div class="publish-content">
            <textarea
              ref="textareaRef"
              v-model="newPostContent"
              placeholder="分享新鲜事..."
              @keydown="handleKeydown"
              :class="{ 'over-limit': isOverLimit }"
            ></textarea>
            
            <!-- 图片预览 -->
            <div v-if="selectedImages.length > 0" class="image-preview-grid">
              <div v-for="(image, index) in selectedImages" :key="index" class="image-preview-item">
                <img :src="image.preview" alt="预览图" />
                <div v-if="image.uploading" class="upload-overlay">
                  <span class="upload-spinner"></span>
                  <span class="upload-text">上传中...</span>
                </div>
                <button class="remove-image-btn" @click="removeImage(index)" :disabled="image.uploading">×</button>
              </div>
            </div>
            
            <!-- 工具栏 -->
            <div class="publish-toolbar">
              <div class="toolbar-left">
                <button class="tool-btn" @click="triggerImageUpload" :disabled="selectedImages.length >= 9" title="图片">
                  <Icon name="camera" :size="18" />
                </button>
                <button class="tool-btn" title="话题">
                  <span class="icon">#</span>
                </button>
                <button class="tool-btn" title="@好友">
                  <span class="icon">@</span>
                </button>
                <button class="tool-btn" title="表情">
                  <Icon name="emoji" :size="18" />
                </button>
                <input
                  id="image-upload"
                  type="file"
                  accept="image/*"
                  multiple
                  style="display: none"
                  @change="handleImageSelect"
                />
              </div>
              <div class="toolbar-right">
                <span class="char-count" :class="{ warning: charCount > 260, error: isOverLimit }">
                  {{ charCount }}/280
                </span>
                <button 
                  class="btn-publish" 
                  @click="publishPost" 
                  :disabled="!canPublish"
                  :class="{ posting: posting }"
                >
                  {{ posting ? '发布中...' : '发布' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 未登录提示 -->
      <div v-else class="login-tip">
        <p>登录后即可发布微博</p>
      </div>

      <!-- 微博列表 -->
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="posts.length === 0" class="empty">暂无微博</div>
      <div v-else class="posts-list">
        <div v-for="post in posts" :key="post.id" class="post-item">
          <img
            :src="post.author?.avatar || '/default-avatar.png'"
            :alt="post.author?.username"
            class="avatar"
            @click.stop="goToProfile(post.author?.id)"
          />
          <div class="post-content">
            <div class="post-main" @click="goToDetail(post.id)">
              <div class="post-header">
                <span class="author-name" @click.stop="goToProfile(post.author?.id)">{{ post.author?.username }}</span>
                <span class="post-time">{{ formatDate(post.createdAt) }}</span>
              </div>
              <div class="post-text">
                {{ post.content }}
              </div>
              
              <!-- 图片展示 -->
              <div v-if="post.images && post.images.length > 0" class="post-images" :class="`images-${post.images.length}`">
                <div
                  v-for="(img, index) in post.images"
                  :key="index"
                  class="post-image-wrapper"
                >
                  <img
                    :src="getImageUrl(img)"
                    :alt="`图片${index + 1}`"
                    class="post-image"
                    @error="handleImageError"
                    loading="lazy"
                  />
                </div>
              </div>
            </div>
            
            <div class="post-actions">
              <button class="action-btn" :class="{ liked: post.liked }" @click.stop="handleLike(post)">
                <Icon :name="post.liked ? 'like' : 'likeOutline'" :size="16" :filled="post.liked" />
                <span>{{ post.likes || 0 }}</span>
              </button>
              <button class="action-btn" @click.stop="handleComment(post.id)">
                <Icon name="comment" :size="16" />
                <span>{{ post.commentsCount || 0 }}</span>
              </button>
              <button class="action-btn" @click.stop="handleRepost(post)">
                <Icon name="repost" :size="16" />
                <span>{{ post.reposts || 0 }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination">
        <button :disabled="currentPage === 1" @click="prevPage">上一页</button>
        <span>{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
        <button :disabled="currentPage >= Math.ceil(total / pageSize)" @click="nextPage">
          下一页
        </button>
      </div>

      <!-- 加载更多 -->
      <div v-else-if="posts.length > 0 && posts.length < total" class="load-more">
        <button @click="nextPage" :disabled="loading">
          {{ loading ? '加载中...' : '加载更多' }}
        </button>
      </div>
      <div v-else-if="posts.length > 0" class="no-more">
        没有更多内容了
      </div>
    </div>


  </div>
</template>

<style scoped>
.weibo {
  min-height: 100vh;
  background: #f6f6f6;
  padding: 20px 0;
}

.container {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 发布框 - 微博风格 */
.publish-box {
  background: white;
  border: 1px solid #e6e6e6;
  margin-bottom: 10px;
}

.publish-main {
  display: flex;
  gap: 12px;
  padding: 16px;
}

.publish-main .avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.publish-content {
  flex: 1;
  min-width: 0;
}

.publish-content textarea {
  width: 100%;
  min-height: 40px;
  max-height: 300px;
  border: none;
  resize: vertical;
  font-size: 16px;
  line-height: 1.6;
  font-family: inherit;
  outline: none;
  color: #333;
  padding: 0;
  margin-bottom: 12px;
  transition: all 0.2s;
}

.publish-content textarea:focus {
  min-height: 80px;
}

.publish-content textarea::placeholder {
  color: #b3b3b3;
}

.publish-content textarea.over-limit {
  color: #ff4757;
}

/* 工具栏 */
.publish-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.toolbar-left {
  display: flex;
  gap: 4px;
}

.tool-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 18px;
}

.tool-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.tool-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.char-count {
  color: #8c8c8c;
  font-size: 14px;
  font-weight: 500;
}

.char-count.warning {
  color: #ff9500;
}

.char-count.error {
  color: #ff4757;
  font-weight: bold;
}

.btn-publish {
  padding: 6px 20px;
  background: #1d9bf0;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.2s;
  min-width: 70px;
  position: relative;
  overflow: hidden;
}

.btn-publish::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.btn-publish:active::before {
  width: 300px;
  height: 300px;
}

.btn-publish:hover:not(:disabled) {
  background: #1a8cd8;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(29, 155, 240, 0.3);
}

.btn-publish:disabled {
  background: #8ecdf7;
  cursor: not-allowed;
  transform: none;
}

.btn-publish.posting {
  background: #8ecdf7;
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

/* 图片预览 */
.image-preview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.image-preview-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 4px;
  overflow: hidden;
  background: #f5f5f5;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s;
}

.image-preview-item:hover img {
  transform: scale(1.05);
}

.remove-image-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  line-height: 1;
  transition: all 0.2s;
}

.remove-image-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.remove-image-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 4px;
}

.upload-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.upload-text {
  color: #fff;
  font-size: 12px;
  font-weight: 500;
}

/* 未登录提示 */
.login-tip {
  background: white;
  border: 1px solid #e6e6e6;
  padding: 40px;
  text-align: center;
  margin-bottom: 10px;
}

.login-tip p {
  margin: 0;
  color: #8c8c8c;
  font-size: 15px;
}

/* 加载和空状态 */
.loading,
.empty {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border: 1px solid #e6e6e6;
  color: #8c8c8c;
}

/* 微博列表 */
.posts-list {
  display: grid;
  gap: 0;
}

.post-item {
  background: white;
  padding: 16px;
  display: flex;
  gap: 12px;
  border: 1px solid #e6e6e6;
  border-top: none;
  transition: all 0.2s;
}

.post-item:first-child {
  border-top: 1px solid #e6e6e6;
}

.post-item:hover {
  background: #f5f5f5;
}

.post-item .avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  cursor: pointer;
}

.post-item .avatar:hover {
  opacity: 0.8;
}

.post-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.post-main {
  cursor: pointer;
}

.post-main:hover {
  opacity: 0.9;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.author-name {
  font-weight: bold;
  color: #333;
  font-size: 15px;
  cursor: pointer;
}

.author-name:hover {
  color: #1d9bf0;
}

.post-time {
  color: #8c8c8c;
  font-size: 13px;
}

.post-text {
  color: #333;
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 12px;
  cursor: pointer;
  word-wrap: break-word;
}

.post-text:hover {
  color: #1d9bf0;
}

/* 微博图片展示 */
.post-images {
  display: grid;
  gap: 4px;
  margin-bottom: 12px;
  border-radius: 8px;
  overflow: hidden;
}

.post-images.images-1 {
  grid-template-columns: 1fr;
  max-width: 400px;
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

.post-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f5f5f5;
  border-radius: 4px;
}

.post-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: all 0.3s;
}

.post-image:hover {
  transform: scale(1.05);
  z-index: 1;
}

.post-image-wrapper.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
}

.image-placeholder {
  font-size: 13px;
  color: #999;
  text-align: center;
  padding: 10px;
}

.post-actions {
  display: flex;
  gap: 40px;
  padding-top: 8px;
  align-items: center;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 6px;
  background: none;
  border: none;
  color: #8c8c8c;
  cursor: pointer;
  font-size: 13px;
  padding: 0;
  transition: all 0.2s;
  min-width: 60px;
}

.action-btn:hover {
  color: #1d9bf0;
}

.action-btn.liked {
  color: #f91880;
}

.action-btn.liked .icon {
  animation: likeAnimation 0.3s ease;
}

@keyframes likeAnimation {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.action-btn .icon {
  font-size: 16px;
}

/* 分页 */
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

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 16px;
  background: white;
  border: 1px solid #e6e6e6;
  margin-top: 10px;
}

.load-more button {
  padding: 8px 24px;
  border: none;
  background: #f5f5f5;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
}

.load-more button:hover:not(:disabled) {
  background: #1d9bf0;
  color: white;
}

.load-more button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.no-more {
  text-align: center;
  padding: 16px;
  background: white;
  border: 1px solid #e6e6e6;
  margin-top: 10px;
  color: #8c8c8c;
  font-size: 14px;
}


</style>