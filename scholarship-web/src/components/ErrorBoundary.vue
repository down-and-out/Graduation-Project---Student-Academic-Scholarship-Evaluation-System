<!--
  错误边界组件
  用于捕获子组件渲染期间的错误，防止整个应用白屏

  使用示例:
  ```vue
  <ErrorBoundary fallback="组件加载失败">
    <SomeComponent />
  </ErrorBoundary>
  ```
-->
<template>
  <template v-if="!hasError">
    <slot :error="error" :error-info="errorInfo" :retry="retry" />
  </template>
  <template v-else>
    <!-- 使用自定义回退组件 -->
    <component
      :is="fallbackComponent"
      v-if="fallbackComponent"
      :error="error"
      :error-info="errorInfo"
      @retry="retry"
    />
    <!-- 使用插槽中的回退内容 -->
    <slot
      v-else-if="$slots.fallback"
      name="fallback"
      :error="error"
      :error-info="errorInfo"
      :retry="retry"
    />
    <!-- 默认回退 UI -->
    <div
      v-else
      class="error-boundary"
    >
      <el-result
        icon="warning"
        title="组件加载失败"
        :sub-title="fallback"
      >
        <template #extra>
          <el-button type="primary" @click="retry">重新加载</el-button>
        </template>
      </el-result>

      <!-- 开发环境显示错误详情 -->
      <div v-if="showDetails && error" class="error-details">
        <div class="error-details__title">错误详情：</div>
        <div class="error-details__item">错误类型：{{ error.name }}</div>
        <div class="error-details__item">错误信息：{{ error.message }}</div>
        <div class="error-details__item">触发阶段：{{ errorInfo }}</div>
        <pre v-if="error.stack" class="error-details__stack">{{ error.stack }}</pre>
      </div>
    </div>
  </template>
</template>

<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue'
import { ElResult, ElButton } from 'element-plus'

interface Props {
  // 错误发生时的回退内容
  fallback?: string
  // 是否显示错误详情（开发环境）
  showDetails?: boolean
  // 自定义回退组件
  fallbackComponent?: any
}

withDefaults(defineProps<Props>(), {
  fallback: '组件加载失败，请稍后重试',
  showDetails: () => process.env.NODE_ENV === 'development',
  fallbackComponent: null
})

const emit = defineEmits<{
  error: [value: { err: unknown; instance: unknown; info: string }]
}>()

// 定义插槽类型
interface SlotProps {
  error: Error | null
  errorInfo: string
  retry: () => void
}

defineSlots<{
  default?: (props: SlotProps) => any
  fallback?: (props: SlotProps) => any
}>()

// 错误状态
const hasError = ref(false)
const error = ref<Error | null>(null)
const errorInfo = ref<string>('')

// 捕获错误
onErrorCaptured((err, instance, info) => {
  hasError.value = true
  error.value = err instanceof Error ? err : new Error(String(err))
  errorInfo.value = info

  // 通知父组件
  emit('error', { err, instance, info })

  // 在控制台输出错误
  console.error('ErrorBoundary 捕获到错误:', err)
  console.error('错误信息:', info)
  console.error('组件实例:', instance)

  // 阻止错误继续向上传播
  return false
})

// 重试
function retry() {
  hasError.value = false
  error.value = null
  errorInfo.value = ''
}
</script>

<style scoped>
.error-boundary {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  padding: 40px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.error-details {
  margin-top: 20px;
  padding: 16px;
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  width: 100%;
  max-width: 600px;
  font-size: 12px;
  color: #606266;
}

.error-details__title {
  margin-bottom: 8px;
  font-weight: bold;
}

.error-details__item {
  margin-bottom: 8px;
}

.error-details__stack {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow: auto;
  max-height: 200px;
}
</style>
