import { ref } from 'vue'

/**
 * 请求状态管理 Composable
 * @param {Function} requestFn - 请求函数
 * @param {Object} options - 配置选项
 * @returns {Object} { data, loading, error, execute, reset }
 */
export function useRequest(requestFn, options = {}) {
  const { immediate = false, defaultData = null, onSuccess, onError } = options

  const data = ref(defaultData)
  const loading = ref(false)
  const error = ref(null)

  const execute = async (...args) => {
    loading.value = true
    error.value = null

    try {
      const result = await requestFn(...args)
      data.value = result
      onSuccess?.(result)
      return result
    } catch (err) {
      error.value = err.message || '请求失败'
      onError?.(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    data.value = defaultData
    loading.value = false
    error.value = null
  }

  // 立即执行
  if (immediate) {
    execute()
  }

  return {
    data,
    loading,
    error,
    execute,
    reset,
  }
}

/**
 * 列表请求管理（带分页）
 * @param {Function} requestFn - 请求函数
 * @param {Object} options - 配置选项
 */
export function useListRequest(requestFn, options = {}) {
  const {
    immediate = false,
    defaultParams = {},
    pageSize = 10,
    onSuccess,
    onError,
  } = options

  const list = ref([])
  const loading = ref(false)
  const error = ref(null)
  const pagination = ref({
    page: 1,
    pageSize,
    total: 0,
    totalPages: 0,
  })
  const finished = ref(false)

  const fetchList = async (params = {}) => {
    loading.value = true
    error.value = null

    try {
      const result = await requestFn({
        page: pagination.value.page,
        pageSize: pagination.value.pageSize,
        ...defaultParams,
        ...params,
      })

      const { posts = [], total = 0, page, pageSize } = result

      if (page === 1) {
        list.value = posts
      } else {
        list.value.push(...posts)
      }

      pagination.value = {
        page,
        pageSize,
        total,
        totalPages: Math.ceil(total / pageSize),
      }

      finished.value = page >= Math.ceil(total / pageSize)

      onSuccess?.(result)
      return result
    } catch (err) {
      error.value = err.message || '加载失败'
      onError?.(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const refresh = () => {
    pagination.value.page = 1
    finished.value = false
    return fetchList()
  }

  const loadMore = () => {
    if (loading.value || finished.value) return
    pagination.value.page++
    return fetchList()
  }

  const reset = () => {
    list.value = []
    pagination.value = {
      page: 1,
      pageSize,
      total: 0,
      totalPages: 0,
    }
    finished.value = false
    loading.value = false
    error.value = null
  }

  if (immediate) {
    fetchList()
  }

  return {
    list,
    loading,
    error,
    pagination,
    finished,
    fetchList,
    refresh,
    loadMore,
    reset,
  }
}
