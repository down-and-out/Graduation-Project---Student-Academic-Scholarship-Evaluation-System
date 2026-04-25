<!--
  系统首页（Dashboard）组件
  展示系统概览信息、快捷操作等
-->
<template>
  <div class="dashboard-container">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎，{{ userInfo.realName || userInfo.username }}</h2>
          <p>{{ getWelcomeMessage() }}</p>
        </div>
        <el-avatar :size="80" :src="userInfo.avatar" />
      </div>
    </el-card>

    <!-- 统计卡片 - 仅学生可见 -->
    <el-row v-if="isStudent" :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <el-icon class="stats-icon" color="#409eff"><Document /></el-icon>
            <div class="stats-info">
              <p class="stats-value">{{ stats.paperCount }}</p>
              <p class="stats-label">论文数量</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <el-icon class="stats-icon" color="#67c23a"><Medal /></el-icon>
            <div class="stats-info">
              <p class="stats-value">{{ stats.patentCount }}</p>
              <p class="stats-label">专利数量</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <el-icon class="stats-icon" color="#e6a23c"><Briefcase /></el-icon>
            <div class="stats-info">
              <p class="stats-value">{{ stats.projectCount }}</p>
              <p class="stats-label">项目数量</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <el-icon class="stats-icon" color="#f56c6c"><Trophy /></el-icon>
            <div class="stats-info">
              <p class="stats-value">{{ stats.score }}</p>
              <p class="stats-label">综合评分</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 - 仅学生可见 -->
    <el-card v-if="isStudent" class="quick-actions-card">
      <template #header>
        <span>快捷操作</span>
      </template>
      <div class="quick-actions">
        <el-button type="primary" @click="handleAction('addPaper')">
          <el-icon><Plus /></el-icon>
          添加论文
        </el-button>
        <el-button type="success" @click="handleAction('addPatent')">
          <el-icon><Plus /></el-icon>
          添加专利
        </el-button>
        <el-button type="warning" @click="handleAction('addProject')">
          <el-icon><Plus /></el-icon>
          添加项目
        </el-button>
        <el-button type="danger" @click="handleAction('apply')">
          <el-icon><DocumentAdd /></el-icon>
          奖学金申请
        </el-button>
      </div>
    </el-card>

    <!-- 通知公告 -->
    <el-card class="notice-card">
      <template #header>
        <span>通知公告</span>
      </template>
      <el-timeline v-if="notices.length > 0">
        <el-timeline-item
          v-for="notice in notices"
          :key="notice.id"
          :timestamp="notice.createTime"
          placement="top"
        >
          <el-link type="primary" :underline="false">{{ notice.title }}</el-link>
          <p class="notice-content">{{ notice.content }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无通知公告" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Ref } from 'vue'
import { Document, Medal, Briefcase, Trophy, Plus, DocumentAdd } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { UserInfo } from '@/utils/secureStorage'
import { getPaperPage } from '@/api/paper'
import { getPatentPage } from '@/api/patent'
import { getProjectPage } from '@/api/project'
import { getLatestNotifications } from '@/api/notification'
import { extractPageData } from '@/utils/helpers'
import type { PaperPageParams } from '@/api/paper'
import type { PatentPageParams } from '@/api/patent'
import type { ProjectPageParams } from '@/api/project'

/**
 * 统计数据接口
 */
interface Stats {
  paperCount: number
  patentCount: number
  projectCount: number
  score: number
}

/**
 * 通知公告接口
 */
interface Notice {
  id: number
  title: string
  content: string
  createTime: string
}

// ========== 路由 ==========
const router = useRouter()

// ========== 状态管理 ==========
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo) as Ref<UserInfo>
const isStudent = computed(() => userInfo.value.userType === 1)

// ========== 状态 ==========
const stats = ref<Stats>({
  paperCount: 0,
  patentCount: 0,
  projectCount: 0,
  score: 0
})

const notices = ref<Notice[]>([])

/**
 * 根据当前小时获取欢迎消息
 * @returns 欢迎消息
 */
function getWelcomeMessage(): string {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息'
  if (hour < 12) return '早上好，新的一天加油'
  if (hour < 14) return '中午好，记得吃午饭'
  if (hour < 18) return '下午好，继续努力'
  return '晚上好，今天辛苦了'
}

/**
 * 处理快捷操作跳转
 * @param action - 操作类型
 */
function handleAction(action: string): void {
  switch (action) {
    case 'addPaper':
    case 'addPatent':
    case 'addProject':
      router.push('/app/student/achievements')
      break
    case 'apply':
      router.push('/app/student/application')
      break
  }
}

/**
 * 加载统计数据
 * 使用 Promise.allSettled 处理并发请求，确保单个请求失败不影响其他请求
 */
async function loadStats(): Promise<void> {
  const [paperRes, patentRes, projectRes] = await Promise.allSettled([
    getPaperPage({ current: 1, size: 100 } as PaperPageParams),
    getPatentPage({ current: 1, size: 100 } as PatentPageParams),
    getProjectPage({ current: 1, size: 100 } as ProjectPageParams)
  ])

  /**
   * 从响应中提取总数，失败时返回 0
   * @param result - Promise 结果
   * @returns 总数
   */
  const extractTotal = (result: PromiseSettledResult<any>): number => {
    if (result.status === 'fulfilled') {
      const pageData = extractPageData<any>(result.value)
      return pageData?.total || 0
    }
    return 0
  }

  stats.value = {
    paperCount: extractTotal(paperRes),
    patentCount: extractTotal(patentRes),
    projectCount: extractTotal(projectRes),
    score: 0
  }
}

/**
 * 加载通知公告
 */
async function loadNotices(): Promise<void> {
  const res = await getLatestNotifications()
  const noticeList = Array.isArray(res.data?.data) ? res.data.data : []
  notices.value = noticeList.map(item => ({
    id: item.id,
    title: item.title,
    content: item.content,
    createTime: item.createTime
  }))
}

// ========== 生命周期 ==========
onMounted(() => {
  // 只有学生才加载统计数据
  if (isStudent.value) {
    loadStats()
  }
  loadNotices()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;

  .welcome-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .welcome-text {
      h2 {
        font-size: 24px;
        color: #303133;
        margin-bottom: 8px;
      }

      p {
        font-size: 14px;
        color: #909399;
      }
    }
  }
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  .stats-content {
    display: flex;
    align-items: center;
    gap: 16px;

    .stats-icon {
      font-size: 40px;
    }

    .stats-info {
      .stats-value {
        font-size: 24px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 4px;
      }

      .stats-label {
        font-size: 14px;
        color: #909399;
        margin: 0;
      }
    }
  }
}

.quick-actions-card {
  margin-bottom: 20px;

  .quick-actions {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
}

.notice-card {
  :deep(.el-timeline-item__timestamp) {
    color: #909399;
  }

  .notice-content {
    margin: 8px 0 0;
    color: #606266;
    line-height: 1.6;
    white-space: pre-wrap;
  }
}
</style>
