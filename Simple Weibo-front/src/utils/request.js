import axios from 'axios'
import { useUserStore } from '../store/user'
import router from '../router'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器
request.interceptors.response.use(
  // 成功响应处理
  (response) => {
    const res = response.data
    
    // 如果后端返回标准格式 { code, message, data }
    if (res && typeof res.code !== 'undefined') {
      if (res.code === 200 || res.code === 0) {
        return res.data
      } else {
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    }
    
    // 直接返回数据（兼容模式）
    return res
  },
  // 错误响应处理
  (error) => {
    const { response } = error
    let message = '网络错误，请稍后重试'
    
    if (response) {
      const { status, data } = response
      
      switch (status) {
        case 400:
          message = data?.message || '请求参数错误'
          break
        case 401:
          message = '登录已过期，请重新登录'
          // 清除登录状态并跳转
          const userStore = useUserStore()
          userStore.logout()
          router.push('/')
          break
        case 403:
          message = '没有权限执行此操作'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = data?.message || `请求失败 (${status})`
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时，请检查网络'
    }
    
    // 显示错误提示（可以在这里集成 toast）
    console.error('[Request Error]', message)
    
    return Promise.reject(new Error(message))
  },
)

export default request
