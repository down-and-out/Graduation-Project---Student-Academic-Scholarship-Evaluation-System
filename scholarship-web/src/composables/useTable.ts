/**
 * 通用表格分页 Hook
 * 用于管理表格的分页、查询、加载状态等
 */
import { ref, reactive, onMounted, onUnmounted, type Ref } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 使用表格的配置选项
 */
export interface UseTableOptions<T = any> {
  immediate?: boolean
  pageSize?: number
  pageSizes?: number[]
  defaultParams?: Record<string, any>
  errorHandler?: (error: unknown) => void
}

/**
 * 使用表格的返回值
 */
export interface UseTableReturn<T = any> {
  loading: Ref<boolean>
  tableData: T[]
  total: Ref<number>
  queryParams: API.PageParams & Record<string, any>
  loadData: (useAbort?: boolean) => Promise<boolean | undefined>
  handleQuery: () => void
  handleReset: (resetParams?: Record<string, any>) => void
  handleSizeChange: (val: number) => void
  handleCurrentChange: (val: number) => void
  refresh: () => void
}

/**
 * 通用表格分页 Hook
 * @param fetchData - 获取数据的函数
 * @param options - 配置选项
 * @returns 表格状态和方法
 */
export function useTable<T = any>(
  fetchData: (params: API.PageParams & Record<string, any>, signal?: AbortSignal) => Promise<any>,
  options: UseTableOptions<T> = {}
): UseTableReturn<T> {
  const {
    immediate = true,        // 是否在挂载时立即加载
    pageSize = 10,           // 默认每页大小
    pageSizes = [10, 20, 50, 100], // 每页大小选项
    errorHandler = null,      // 自定义错误处理函数
    defaultParams = {}        // 默认查询参数
  } = options

  // 状态
  const loading = ref(false)
  const tableData = ref<T[]>([])
  const total = ref(0)

  // 查询参数
  const queryParams = reactive<API.PageParams & Record<string, any>>({
    current: 1,
    size: pageSize,
    ...defaultParams
  })

  // 取消标记，用于防止组件卸载后仍更新状态
  let cancelled = false
  let abortController: AbortController | null = null

  /**
   * 加载数据
   * @param useAbort - 是否使用 AbortController 取消请求
   */
  async function loadData(useAbort = false): Promise<boolean | undefined> {
    // 如果组件已卸载，取消加载
    if (cancelled) return

    // 取消之前的请求
    if (abortController) {
      abortController.abort()
    }

    // 创建新的 AbortController
    if (useAbort && typeof AbortController !== 'undefined') {
      abortController = new AbortController()
    }

    loading.value = true
    try {
      const res = await fetchData(queryParams, abortController?.signal)

      // 再次检查是否已取消
      if (cancelled) return

      tableData.value = (res.data?.records || res.data?.list || res.data || []) as T[]
      total.value = res.data?.total || res.data?.count || 0
      return true
    } catch (error) {
      // 如果是取消请求导致的错误，不处理
      if ((error as Error).name === 'AbortError') return

      if (cancelled) return

      console.error('加载数据失败:', error)
      if (errorHandler) {
        errorHandler(error)
      } else if ((error as Error).message) {
        ElMessage.error((error as Error).message)
      }
      return false
    } finally {
      if (!cancelled) {
        loading.value = false
      }
      abortController = null
    }
  }

  /**
   * 处理查询
   */
  function handleQuery(): void {
    queryParams.current = 1
    loadData()
  }

  /**
   * 重置查询
   * @param resetParams - 重置参数
   */
  function handleReset(resetParams: Record<string, any> = {}): void {
    Object.assign(queryParams, {
      current: 1,
      size: pageSize,
      ...defaultParams,
      ...resetParams
    })
    loadData()
  }

  /**
   * 处理分页变化
   */
  function handleSizeChange(val: number): void {
    queryParams.size = val
    queryParams.current = 1
    loadData()
  }

  function handleCurrentChange(val: number): void {
    queryParams.current = val
    loadData()
  }

  /**
   * 刷新数据
   */
  function refresh(): void {
    loadData()
  }

  // 组件卸载时清理
  onUnmounted(() => {
    cancelled = true
    if (abortController) {
      abortController.abort()
    }
  })

  // 自动加载
  if (immediate) {
    onMounted(() => {
      if (!cancelled) {
        loadData()
      }
    })
  }

  return {
    // 状态
    loading,
    tableData: tableData as unknown as T[],
    total,
    queryParams,
    // 方法
    loadData,
    handleQuery,
    handleReset,
    handleSizeChange,
    handleCurrentChange,
    refresh
  }
}

export default useTable
