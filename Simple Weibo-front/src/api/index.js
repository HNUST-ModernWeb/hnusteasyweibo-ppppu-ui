import request from '../utils/request'

// ==================== 认证相关 ====================

/**
 * 用户登录
 * @param {Object} data - { username, password }
 * @returns {Promise} { token, userInfo }
 */
export const login = (data) => request.post('/api/login', data)

/**
 * 用户注册
 * @param {Object} data - { username, password, avatar? }
 * @returns {Promise} { token, userInfo }
 */
export const register = (data) => request.post('/api/register', data)

/**
 * 获取用户信息
 * @param {number} userId
 * @returns {Promise} 用户信息
 */
export const getUserInfo = (userId) => request.get(`/api/user/${userId}`)

/**
 * 更新用户信息
 * @param {number} userId
 * @param {Object} data - { username, bio, avatar }
 * @returns {Promise} 更新后的用户信息
 */
export const updateUserInfo = (userId, data) => request.put(`/api/user/${userId}`, data)

// ==================== 微博相关 ====================

/**
 * 获取微博列表
 * @param {Object} params - { page?, pageSize?, authorId? }
 * @returns {Promise} { posts, total, page, pageSize }
 */
export const getPosts = (params) => request.get('/api/posts', { params })

/**
 * 获取微博详情
 * @param {number} postId
 * @returns {Promise} 微博详情
 */
export const getPostDetail = (postId) => request.get(`/api/post/${postId}`)

/**
 * 创建微博
 * @param {Object} data - { content, images? }
 * @returns {Promise} 创建的微博
 */
export const createPost = (data) => request.post('/api/post', data)

/**
 * 更新微博
 * @param {number} postId
 * @param {Object} data - { content, images? }
 * @returns {Promise} 更新后的微博
 */
export const updatePost = (postId, data) => request.put(`/api/post/${postId}`, data)

/**
 * 删除微博
 * @param {number} postId
 * @returns {Promise} { success: true }
 */
export const deletePost = (postId) => request.delete(`/api/post/${postId}`)

// ==================== 评论相关 ====================

/**
 * 获取评论列表
 * @param {Object} params - { postId, page?, pageSize? }
 * @returns {Promise} { comments, total }
 */
export const getComments = (params) => request.get('/api/comments', { params })

/**
 * 创建评论
 * @param {Object} data - { postId, content }
 * @returns {Promise} 创建的评论
 */
export const createComment = (data) => request.post('/api/comment', data)

// ==================== 互动相关 ====================

/**
 * 点赞/取消点赞
 * @param {number} postId
 * @returns {Promise} { liked, likes }
 */
export const toggleLike = (postId) => request.post(`/api/post/${postId}/like`)

// ==================== 文件上传 ====================

/**
 * 上传图片
 * @param {File} file - 图片文件
 * @returns {Promise} { url: 图片URL }
 */
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/**
 * 批量上传图片
 * @param {File[]} files - 图片文件数组
 * @returns {Promise} { urls: 图片URL数组 }
 */
export const uploadImages = (files) => {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })
  return request.post('/api/upload/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// ==================== 通知相关 ====================

/**
 * 获取通知列表
 * @param {Object} params - { page?, pageSize?, type? }
 * @returns {Promise} { notifications, total, unreadCount }
 */
export const getNotifications = (params) => request.get('/api/notifications', { params })

/**
 * 标记通知为已读
 * @param {number} notificationId
 * @returns {Promise} { success: true }
 */
export const markNotificationAsRead = (notificationId) => request.put(`/api/notification/${notificationId}/read`)

/**
 * 标记所有通知为已读
 * @returns {Promise} { success: true }
 */
export const markAllNotificationsAsRead = () => request.put('/api/notifications/read-all')

/**
 * 获取未读通知数量
 * @returns {Promise} { unreadCount: number }
 */
export const getUnreadNotificationsCount = () => request.get('/api/notifications/unread-count')
