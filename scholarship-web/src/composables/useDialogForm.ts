/**
 * 表单对话框 Hook
 * 用于管理添加/编辑对话框的状态和操作
 */
import { ref, reactive, computed, type Ref } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 使用对话框表单的配置选项
 */
export interface UseDialogFormOptions<T extends Record<string, any> = Record<string, any>> {
  title?: string
  addTitle?: string
  editTitle?: string
  defaultFormData?: T
  onSubmit?: (data: T, isEdit: boolean) => Promise<void>
  onSuccess?: (data: T, isEdit: boolean) => Promise<void>
  onError?: (error: unknown) => void
}

/**
 * 使用对话框表单的返回值
 */
export interface UseDialogFormReturn<T extends Record<string, any> = Record<string, any>> {
  dialogVisible: Ref<boolean>
  isEdit: Ref<boolean>
  submitting: Ref<boolean>
  formRef: Ref<any>
  formData: T
  dialogTitle: Ref<string>
  handleAdd: () => void
  handleEdit: (row: Partial<T>) => void
  closeDialog: () => void
  handleSubmit: () => Promise<boolean>
}

/**
 * 表单对话框 Hook
 * @param options - 配置选项
 * @returns 表单状态和方法
 */
export function useDialogForm<T extends Record<string, any> = Record<string, any>>(
  options: UseDialogFormOptions<T> = {} as UseDialogFormOptions<T>
): UseDialogFormReturn<T> {
  const {
    title = '表单',
    addTitle = '添加',
    editTitle = '编辑',
    defaultFormData = {} as T,
    onSubmit = null,
    onSuccess = null,
    onError = null
  } = options

  // 状态
  const dialogVisible = ref(false)
  const isEdit = ref(false)
  const submitting = ref(false)
  const formRef = ref<any>(null)

  // 表单数据
  const formData = reactive({ ...defaultFormData }) as T

  // 对话框标题
  const dialogTitle = computed(() => {
    const baseTitle = title || (isEdit.value ? editTitle : addTitle)
    return isEdit.value ? `编辑${baseTitle}` : `添加${baseTitle}`
  })

  /**
   * 打开添加对话框
   */
  function handleAdd(): void {
    isEdit.value = false
    Object.assign(formData, defaultFormData)
    dialogVisible.value = true
  }

  /**
   * 打开编辑对话框
   * @param row - 行数据
   */
  function handleEdit(row: Partial<T>): void {
    isEdit.value = true
    Object.assign(formData, row)
    dialogVisible.value = true
  }

  /**
   * 关闭对话框
   */
  function closeDialog(): void {
    dialogVisible.value = false
    formRef.value?.resetFields()
    Object.assign(formData, defaultFormData)
  }

  /**
   * 提交表单
   */
  async function handleSubmit(): Promise<boolean> {
    // 表单验证
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return false

    submitting.value = true
    try {
      if (onSubmit) {
        await onSubmit(formData, isEdit.value)
      }

      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      closeDialog()

      if (onSuccess) {
        await onSuccess(formData, isEdit.value)
      }

      return true
    } catch (error) {
      console.error('提交失败:', error)
      if (onError) {
        onError(error)
      } else if ((error as Error).message) {
        ElMessage.error((error as Error).message)
      }
      return false
    } finally {
      submitting.value = false
    }
  }

  return {
    // 状态
    dialogVisible,
    isEdit,
    submitting,
    formRef,
    formData,
    dialogTitle,
    // 方法
    handleAdd,
    handleEdit,
    closeDialog,
    handleSubmit
  }
}

export default useDialogForm
