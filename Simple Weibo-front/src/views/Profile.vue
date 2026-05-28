<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { getPosts, getUserInfo, toggleLike, updateUserInfo, uploadImage } from '../api'
import Icon from '../components/Icon.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 判断是否是查看其他用户的资料
const isOtherUser = computed(() => !!route.params.id)
const userId = computed(() => isOtherUser.value ? parseInt(route.params.id) : userStore.userInfo?.id)

// 用户资料（可能是当前登录用户，也可能是其他用户）
const profileUser = ref(null)

const myPosts = ref([])
const likedPosts = ref([])
const loading = ref(false)
const activeTab = ref('posts') // 'posts' 或 'liked'
const showBio = ref(false)
const showEditProfile = ref(false)
const editForm = ref({
  username: '',
  bio: ''
})
const editAvatar = ref(null)
const editAvatarPreview = ref('')
const isUploading = ref(false)
const uploadError = ref('')

// 计算总互动数（点赞+评论+转发）
const totalInteractions = computed(() => {
  return myPosts.value.reduce((sum, post) => {
    return sum + (post.likes || 0) + (post.commentsCount || 0) + (post.reposts || 0)
  }, 0)
})

// 获取用户资料
const fetchUserProfile = async () => {
  if (!userId.value) return

  if (isOtherUser.value) {
    // 查看其他用户，需要调用 API 获取用户信息
    try {
      const res = await getUserInfo(userId.value)
      profileUser.value = res
    } catch (e) {
      console.error('获取用户信息失败', e)
      alert('用户不存在')
      router.push('/')
    }
  } else {
    // 查看自己的资料 - 使用浅拷贝避免引用问题
    console.log('fetchUserProfile - 当前 userStore.userInfo:', userStore.userInfo)
    profileUser.value = { ...userStore.userInfo }
  }
}

const fetchMyPosts = async () => {
  if (!userId.value) return
  loading.value = true
  try {
    const res = await getPosts({ authorId: userId.value })
    myPosts.value = res.posts || []
  } catch (e) {
    console.error('获取文章失败', e)
  } finally {
    loading.value = false
  }
}

// 获取赞过的微博
const fetchLikedPosts = async () => {
  loading.value = true
  try {
    const res = await getPosts({ page: 1, pageSize: 100 })
    // 过滤出当前用户赞过的微博
    likedPosts.value = (res.posts || []).filter(post => post.liked)
  } catch (e) {
    console.error('获取赞过的微博失败', e)
  } finally {
    loading.value = false
  }
}

// 切换标签
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'liked' && likedPosts.value.length === 0) {
    fetchLikedPosts()
  }
}

const goToPost = (postId) => {
  router.push(`/post/${postId}`)
}

const goToHome = () => {
  router.push('/')
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
    showToast('操作失败', 'error')
  }
}

// 评论功能 - 跳转到详情页
const handleComment = (postId) => {
  router.push(`/post/${postId}`)
}

// 转发功能
const handleRepost = (post) => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录', 'error')
    return
  }
  showToast('转发功能开发中')
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
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

// 处理头像URL，确保正确显示
const getAvatarUrl = (avatar) => {
  console.log('getAvatarUrl 输入:', avatar, '类型:', typeof avatar)
  if (!avatar) {
    console.log('avatar 为空，返回默认头像')
    return '/default-avatar.png'
  }
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http')) {
    console.log('完整URL，直接返回:', avatar)
    return avatar
  }
  // 如果是相对路径，拼接后端地址
  if (avatar.startsWith('/')) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000'
    const result = baseUrl + avatar
    console.log('相对路径，拼接后:', result)
    return result
  }
  console.log('其他格式，原样返回:', avatar)
  return avatar
}

// 头像加载失败处理
const handleAvatarError = (e) => {
  e.target.src = '/default-avatar.png'
}

// 打开编辑资料对话框（仅自己可见）
const openEditProfile = () => {
  if (isOtherUser.value) return
  editForm.value.username = profileUser.value?.username || ''
  editForm.value.bio = profileUser.value?.bio || ''
  editAvatarPreview.value = profileUser.value?.avatar || ''
  showEditProfile.value = true
}

// 处理头像选择
const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    uploadError.value = '请选择图片文件'
    return
  }

  // 验证文件大小（限制 5MB）
  if (file.size > 5 * 1024 * 1024) {
    uploadError.value = '图片大小不能超过 5MB'
    return
  }

  uploadError.value = ''
  editAvatar.value = file

  // 本地预览
  const reader = new FileReader()
  reader.onload = (e) => {
    editAvatarPreview.value = e.target.result
  }
  reader.readAsDataURL(file)

  // 立即上传到服务器
  await uploadAvatar(file)
}

// 上传头像到服务器
const uploadAvatar = async (file) => {
  isUploading.value = true
  uploadError.value = ''

  console.log('开始上传头像:', file.name, file.type, file.size)

  try {
    // 检查用户登录状态
    if (!userStore.isLoggedIn) {
      throw new Error('请先登录')
    }

    console.log('调用 uploadImage API...')
    const uploadRes = await uploadImage(file)
    console.log('上传响应:', uploadRes)

    if (!uploadRes) {
      throw new Error('上传失败，服务器未返回数据')
    }

    // 处理不同的响应格式
    let avatarUrl = null
    if (uploadRes.url) {
      avatarUrl = uploadRes.url
    } else if (typeof uploadRes === 'string') {
      avatarUrl = uploadRes
    } else if (uploadRes.data && uploadRes.data.url) {
      avatarUrl = uploadRes.data.url
    }

    if (!avatarUrl) {
      console.error('无法从响应中提取图片URL:', uploadRes)
      throw new Error('上传失败，服务器返回数据格式错误')
    }

    console.log('获取到头像URL:', avatarUrl)

    // 调用后端 API 更新用户头像
    console.log('更新用户信息...')
    const updatedUser = await updateUserInfo(userStore.userInfo.id, {
      username: userStore.userInfo.username,
      bio: userStore.userInfo.bio || '',
      avatar: avatarUrl
    })
    console.log('用户信息更新成功:', updatedUser)

    // 合并更新后的数据 - 确保 avatar 不会被覆盖
    const mergedUser = {
      ...userStore.userInfo,
      ...updatedUser,
      avatar: avatarUrl  // 最后设置 avatar，确保不会被覆盖
    }

    console.log('准备更新 userStore:', mergedUser)

    // 先更新 profileUser，再更新 userStore（避免 watch 触发时覆盖）
    profileUser.value = { ...profileUser.value, ...mergedUser }
    editAvatarPreview.value = avatarUrl

    // 更新前端状态
    userStore.updateUserInfo(mergedUser)

    showToast('头像上传成功')
  } catch (e) {
    console.error('头像上传失败:', e)
    console.error('错误详情:', e.response?.data || e.message)
    uploadError.value = e.response?.data?.message || e.message || '头像上传失败，请重试'
    showToast(uploadError.value, 'error')
  } finally {
    isUploading.value = false
  }
}

// 保存编辑 - 只保存用户名和简介，头像已在上传时自动保存
const saveProfile = async () => {
  if (!editForm.value.username.trim()) {
    alert('用户名不能为空')
    return
  }

  try {
    // 调用后端 API 更新用户信息（只更新用户名和简介）
    const updatedUser = await updateUserInfo(userStore.userInfo.id, {
      username: editForm.value.username,
      bio: editForm.value.bio,
      avatar: userStore.userInfo.avatar
    })

    // 合并更新后的数据
    const mergedUser = {
      username: editForm.value.username,
      bio: editForm.value.bio,
      avatar: userStore.userInfo.avatar,
      ...updatedUser
    }

    // 更新前端状态
    userStore.updateUserInfo(mergedUser)
    profileUser.value = { ...profileUser.value, ...mergedUser }

    // 清空临时文件
    editAvatar.value = null
    editAvatarPreview.value = ''

    showEditProfile.value = false
    alert('资料更新成功')
  } catch (e) {
    console.error('更新失败', e)
    alert('资料更新失败，请重试')
  }
}

// 取消编辑
const cancelEdit = () => {
  showEditProfile.value = false
  editAvatar.value = null
  editAvatarPreview.value = ''
}

onMounted(() => {
  fetchUserProfile()
  fetchMyPosts()
})

// 监听路由参数变化，切换用户时重新加载
watch(() => route.params.id, () => {
  fetchUserProfile()
  fetchMyPosts()
  activeTab.value = 'posts'
})

// 监听 userStore.userInfo 变化，同步更新 profileUser（仅自己资料页）
// 使用 computed 替代 watch，避免重复更新
const currentUserInfo = computed(() => {
  if (!isOtherUser.value && userStore.userInfo) {
    return { ...userStore.userInfo }
  }
  return null
})

// 只在必要时更新 profileUser
watch(currentUserInfo, (newInfo, oldInfo) => {
  if (newInfo && (!oldInfo || newInfo.id !== oldInfo.id || newInfo.avatar !== oldInfo.avatar)) {
    console.log('userInfo 变化，更新 profileUser:', newInfo)
    profileUser.value = { ...profileUser.value, ...newInfo }
  }
}, { immediate: true })
</script>

<template>
  <div class="profile">
    <!-- 封面图区域 -->
    <div class="cover-section">
      <div class="cover-image"></div>
    </div>

    <!-- 用户信息区域 -->
    <div class="user-section">
      <div class="user-main">
        <img
          :src="getAvatarUrl(profileUser?.avatar)"
          :alt="profileUser?.username"
          class="avatar"
          @error="handleAvatarError"
        />
        <div class="user-info">
          <div class="username-row">
            <h1 class="username">{{ profileUser?.username || '加载中...' }}</h1>
          </div>
          <div class="stats-row">
            <div class="stat-item">
              <span class="stat-num">{{ profileUser?.followers || 0 }}</span>
              <span class="stat-label">粉丝</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ profileUser?.following || 0 }}</span>
              <span class="stat-label">关注</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ totalInteractions }}</span>
              <span class="stat-label">转评赞</span>
            </div>
          </div>
        </div>
        <button v-if="!isOtherUser" class="edit-profile-btn" @click="openEditProfile">编辑资料</button>
      </div>

      <!-- 简介 -->
      <div class="bio-section" @click="showBio = !showBio">
        <Icon name="bio" :size="16" class="bio-icon" />
        <span class="bio-text" :class="{ expanded: showBio }">{{ profileUser?.bio || '暂无简介' }}</span>
        <Icon :name="showBio ? 'arrowUp' : 'arrowDown'" :size="12" class="bio-arrow" />
      </div>
    </div>

    <!-- 标签切换 -->
    <div class="tab-section">
      <div class="tab-header">
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'posts' }"
          @click="switchTab('posts')"
        >
          {{ isOtherUser ? 'TA的微博' : '我的微博' }}
        </button>
        <button 
          v-if="!isOtherUser"
          class="tab-btn" 
          :class="{ active: activeTab === 'liked' }"
          @click="switchTab('liked')"
        >
          我赞过
        </button>
      </div>
    </div>

    <!-- 微博列表 -->
    <div class="posts-section">
      <!-- 我的微博 -->
      <div v-if="activeTab === 'posts'">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="myPosts.length === 0" class="empty">
          <p>还没有发布微博</p>
        </div>
        <div v-else class="posts-list">
          <div v-for="post in myPosts" :key="post.id" class="post-item" @click="goToPost(post.id)">
            <div class="post-header">
              <img
                :src="profileUser?.avatar || '/default-avatar.png'"
                class="post-avatar"
              />
              <div class="post-author">
                <span class="author-name">{{ profileUser?.username }}</span>
                <span class="post-time">{{ formatDate(post.createdAt) }}</span>
              </div>
            </div>
            <div class="post-content">
              <p class="post-text">{{ post.content }}</p>
              <!-- 图片展示 -->
              <div v-if="post.images && post.images.length > 0" class="post-images" :class="`images-${post.images.length}`">
                <img
                  v-for="(img, index) in post.images.slice(0, 9)"
                  :key="index"
                  :src="img"
                  :alt="`图片${index + 1}`"
                  class="post-image"
                />
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

      <!-- 我赞过 -->
      <div v-else-if="activeTab === 'liked'">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="likedPosts.length === 0" class="empty">
          <p>还没有赞过任何微博</p>
        </div>
        <div v-else class="posts-list">
          <div v-for="post in likedPosts" :key="post.id" class="post-item" @click="goToPost(post.id)">
            <div class="post-header">
              <img
                :src="post.author?.avatar || '/default-avatar.png'"
                class="post-avatar"
              />
              <div class="post-author">
                <span class="author-name">{{ post.author?.username }}</span>
                <span class="post-time">{{ formatDate(post.createdAt) }}</span>
              </div>
            </div>
            <div class="post-content">
              <p class="post-text">{{ post.content }}</p>
              <!-- 图片展示 -->
              <div v-if="post.images && post.images.length > 0" class="post-images" :class="`images-${post.images.length}`">
                <img
                  v-for="(img, index) in post.images.slice(0, 9)"
                  :key="index"
                  :src="img"
                  :alt="`图片${index + 1}`"
                  class="post-image"
                />
              </div>
            </div>
            <div class="post-actions">
              <button class="action-btn liked" @click.stop="handleLike(post)">
                <Icon name="like" :size="16" filled />
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
    </div>

    <!-- 编辑资料对话框 -->
    <Teleport to="body">
      <div v-if="showEditProfile" class="edit-dialog-overlay" @click="cancelEdit">
        <div class="edit-dialog" @click.stop>
          <div class="edit-dialog-header">
            <h3>编辑资料</h3>
            <button class="close-btn" @click="cancelEdit">×</button>
          </div>
          <div class="edit-dialog-body">
            <!-- 头像编辑 -->
            <div class="avatar-edit">
              <div class="avatar-preview-wrapper">
                <img :src="getAvatarUrl(editAvatarPreview || userStore.userInfo?.avatar)" class="avatar-preview" @error="handleAvatarError" />
                <div v-if="isUploading" class="upload-overlay">
                  <span class="upload-spinner"></span>
                  <span class="upload-text">上传中...</span>
                </div>
              </div>
              <label class="avatar-upload-btn" :class="{ disabled: isUploading }">
                <input type="file" accept="image/*" @change="handleAvatarChange" style="display: none" :disabled="isUploading" />
                <span>{{ isUploading ? '上传中...' : '更换头像' }}</span>
              </label>
              <p v-if="uploadError" class="upload-error">{{ uploadError }}</p>
            </div>
            <!-- 用户名编辑 -->
            <div class="form-item">
              <label>用户名</label>
              <input v-model="editForm.username" type="text" placeholder="请输入用户名" />
            </div>
            <!-- 简介编辑 -->
            <div class="form-item">
              <label>简介</label>
              <textarea v-model="editForm.bio" rows="3" placeholder="介绍一下自己..."></textarea>
            </div>
          </div>
          <div class="edit-dialog-footer">
            <button class="btn-cancel" @click="cancelEdit">取消</button>
            <button class="btn-save" @click="saveProfile">保存</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 悬浮返回按钮 -->
    <button class="fab-back-btn" @click="goToHome">
      <Icon name="back" :size="16" />
      <span>返回</span>
    </button>
  </div>
</template>

<style scoped>
.profile {
  min-height: 100vh;
  background: #f5f5f5;
  color: #333;
  max-width: 480px;
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
  background: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

.fab-back-btn .arrow {
  font-size: 16px;
}

/* 封面图区域 */
.cover-section {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  background: #8d9ec4;
  background-size: cover;
  background-position: center;
}

/* 用户信息区域 */
.user-section {
  padding: 0 16px 16px;
  background: #fff;
  margin: 0 12px;
  margin-top: -20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
}

.user-main {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding-top: 12px;
}

.avatar {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  background: #f0f0f0;
  margin-top: -45px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-info {
  flex: 1;
  padding-bottom: 4px;
}

.username-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.username {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.stats-row {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-num {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

/* 简介 */
.bio-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 0;
  border-top: 1px solid #f0f0f0;
  cursor: pointer;
}

.bio-icon {
  font-size: 14px;
  color: #999;
}

.bio-text {
  flex: 1;
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all 0.2s;
}

.bio-text.expanded {
  white-space: normal;
  overflow: visible;
  word-wrap: break-word;
  word-break: break-all;
}

.bio-arrow {
  font-size: 12px;
  color: #999;
  transition: transform 0.2s;
}

.bio-arrow.expanded {
  transform: rotate(180deg);
}

/* 微博列表 */
.posts-section {
  background: #f5f5f5;
  padding: 12px;
}

.loading,
.empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  background: #fff;
  border-radius: 12px;
  margin: 0 12px;
}

.empty p {
  margin: 0;
  font-size: 14px;
}

/* 标签切换 */
.tab-section {
  background: #fff;
  margin: 12px;
  border-radius: 12px;
  padding: 0 16px;
}

.tab-header {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
}

.tab-btn {
  flex: 1;
  padding: 16px;
  background: none;
  border: none;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.tab-btn.active {
  color: #1d9bf0;
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background: #1d9bf0;
  border-radius: 2px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-item {
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.post-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.post-author {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-content {
  margin-bottom: 12px;
}

.post-text {
  margin: 0 0 12px 0;
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  word-wrap: break-word;
}

/* 微博图片展示 */
.post-images {
  display: grid;
  gap: 4px;
  border-radius: 8px;
  overflow: hidden;
}

.post-images.images-1 {
  grid-template-columns: 1fr;
  max-width: 280px;
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
}

.post-actions {
  display: flex;
  gap: 24px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: #999;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 0;
}

.action-btn:hover {
  color: #1d9bf0;
}

.action-btn .icon {
  font-size: 14px;
}

/* 编辑资料按钮 */
.edit-profile-btn {
  padding: 8px 16px;
  background: #1d9bf0;
  border: none;
  border-radius: 20px;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  margin-left: auto;
  align-self: center;
}

.edit-profile-btn:hover {
  background: #1a8cd8;
}

/* 编辑资料对话框 */
.edit-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.edit-dialog {
  background: white;
  border-radius: 16px;
  width: 360px;
  max-width: 90%;
  max-height: 80vh;
  overflow: hidden;
  animation: dialogSlideIn 0.2s ease;
}

@keyframes dialogSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.edit-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.edit-dialog-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.edit-dialog-body {
  padding: 24px 20px;
}

.avatar-edit {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.avatar-preview-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f0f0f0;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.upload-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #fff;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.upload-text {
  font-size: 11px;
  color: #fff;
}

.upload-error {
  font-size: 12px;
  color: #e74c3c;
  margin: 0;
}

.avatar-upload-btn {
  padding: 6px 16px;
  background: #f5f5f5;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.avatar-upload-btn:hover {
  background: #e8e8e8;
}

.avatar-upload-btn.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.form-item input,
.form-item textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  font-size: 15px;
  font-family: inherit;
  outline: none;
  box-sizing: border-box;
}

.form-item input:focus,
.form-item textarea:focus {
  border-color: #1d9bf0;
}

.form-item textarea {
  resize: vertical;
  min-height: 80px;
}

.edit-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.btn-cancel {
  padding: 10px 20px;
  border: 1px solid #e6e6e6;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #f5f5f5;
}

.btn-save {
  padding: 10px 24px;
  border: none;
  background: #1d9bf0;
  color: white;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.2s;
}

.btn-save:hover {
  background: #1a8cd8;
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
