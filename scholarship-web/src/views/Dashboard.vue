<!--
  系统首页（Dashboard）组件
  角色说明：
  - 学生：显示统计数据卡片（论文/专利/项目数量、综合评分）、快捷操作、通知公告
  - 导师/管理员：仅显示通知公告

  功能：
  - 欢迎卡片（显示当前用户信息）
  - 统计数据（仅学生可见）：论文数量、专利数量、项目数量、综合评分
  - 快捷操作（仅学生可见）：添加论文/专利/项目、奖学金申请
  - 通知公告列表
-->
<template>
  <div class="dashboard-container">
    <!-- 欢迎卡片：显示当前用户信息和欢迎语 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎，{{ userInfo.realName || userInfo.username }}</h2>
          <p>{{ getWelcomeMessage() }}</p>
        </div>
        <el-avatar :size="80" :src="userInfo.avatar" />
      </div>
    </el-card>

    <!-- 统计卡片（仅学生可见）：论文/专利/项目数量、综合评分 -->
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

    <!-- 快捷操作（仅学生可见）：添加成果、奖学金申请 -->
    <el-card v-if="isStudent" class="quick-actions-card">
      <template #header>
        <span>快捷操作</span>
      </template>
      <div class="quick-actions">
        <!-- 添加论文 -->
        <el-button type="primary" @click="handleAction('addPaper')">
          <el-icon><Plus /></el-icon>
          添加论文
        </el-button>
        <!-- 添加专利 -->
        <el-button type="success" @click="handleAction('addPatent')">
          <el-icon><Plus /></el-icon>
          添加专利
        </el-button>
        <!-- 添加项目（暂未开放） -->
        <el-button type="warning" @click="handleAction('addProject')">
          <el-icon><Plus /></el-icon>
          添加项目
        </el-button>
        <!-- 奖学金申请 -->
        <el-button type="danger" @click="handleAction('apply')">
          <el-icon><DocumentAdd /></el-icon>
          奖学金申请
        </el-button>
      </div>
    </el-card>

    <!-- 通知公告：显示系统公告列表 -->
    <el-card class="notice-card">
      <template #header>
        <span>通知公告</span>
      </template>
      <!-- 有公告时显示时间线形式 -->
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
      <!-- 无公告时显示空状态 -->
      <el-empty v-else description="暂无通知公告" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
/**
 * 系统首页（Dashboard）
 *
 * 功能说明：
 * - 欢迎卡片：显示当前登录用户的姓名和头像，以及根据当前时间段的欢迎语
 * - 统计卡片（仅学生）：显示论文数量、专利数量、项目数量、综合评分
 * - 快捷操作（仅学生）：跳转到成果添加页面或奖学金申请页面
 * - 通知公告：显示系统公告列表
 *
 * 注意：
 * - 统计数据使用 Promise.allSettled 并发加载，单个请求失败不影响其他请求
 * - 导师/管理员登录后首页不显示统计卡片和快捷操作，仅显示欢迎卡片和通知公告
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { Ref } from 'vue'
import { Document, Medal, Briefcase, Trophy, Plus, DocumentAdd } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { UserInfo } from '@/utils/secureStorage'
import { getLatestNotifications, type Notification } from '@/api/notification'
import { extractApiData } from '@/utils/helpers'
import { USER_TYPE } from '@/constants/user'
import { countPaper } from '@/api/paper'
import { countPatent } from '@/api/patent'
import { countProject } from '@/api/project'

/** 统计数据接口 */
interface Stats {
  paperCount: number      // 论文数量
  patentCount: number     // 专利数量
  projectCount: number   // 项目数量
  score: number          // 综合评分
}

/** 通知公告接口 */
interface Notice {
  id: Notification['id']
  title: Notification['title']
  content: Notification['content']
  createTime: Notification['createTime']
}

// ==================== 路由 ====================

const router = useRouter()

// ==================== 状态管理 ====================

/** 用户状态仓库 */
const userStore = useUserStore()
/** 当前登录用户信息 */
const userInfo = computed(() => userStore.userInfo) as Ref<UserInfo>
/** 是否为学生角色 */
const isStudent = computed(() => userInfo.value.userType === USER_TYPE.STUDENT)

// ==================== 状态 ====================

/** 统计数据 */
const stats = ref<Stats>({
  paperCount: 0,
  patentCount: 0,
  projectCount: 0,
  score: 0
})

/** 通知公告列表 */
const notices = ref<Notice[]>([])

// ==================== 工具方法 ====================

/**
 * 根据当前小时获取欢迎消息
 * @returns 欢迎消息文本
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
 * @param action - 操作类型：addPaper/addPatent/addProject/apply
 */
function handleAction(action: string): void {
  switch (action) {
    case 'addPaper':
      router.push({ path: '/app/student/achievements', query: { type: 'paper', action: 'add' } })
      break
    case 'addPatent':
      router.push({ path: '/app/student/achievements', query: { type: 'patent', action: 'add' } })
      break
    case 'addProject':
      ElMessage.info('项目成果录入入口暂未开放')
      break
    case 'apply':
      router.push('/app/student/application')
      break
  }
}

// ==================== 数据加载 ====================

/**
 * 加载统计数据
 * 使用 Promise.allSettled 处理并发请求，确保单个请求失败不影响其他请求
 * 统计数据仅对学生有意义
 */
async function loadStats(): Promise<void> {
  const [paperRes, patentRes, projectRes] = await Promise.allSettled([
    countPaper(),
    countPatent(),
    countProject()
  ])

  /**
   * 从响应中提取总数，失败时返回 0
   * @param result - Promise 结果
   * @returns 总数
   */
  const extractTotal = (result: PromiseSettledResult<any>): number => {
    if (result.status === 'fulfilled') {
      return extractApiData<number>(result.value) || 0
    }
    return 0
  }

  stats.value = {
    paperCount: extractTotal(paperRes),
    patentCount: extractTotal(patentRes),
    projectCount: extractTotal(projectRes),
    score: 0  // 综合评分接口暂未提供
  }
}

/**
 * 加载通知公告列表
 * 失败时静默处理，公告列表保持为空
 */
async function loadNotices(): Promise<void> {
  try {
    const res = await getLatestNotifications()
    const noticeList = extractApiData<Notification[]>(res) || []
    notices.value = noticeList.map(item => ({
      id: item.id,
      title: item.title,
      content: item.content,
      createTime: item.createTime
    }))
  } catch {
    notices.value = []
  }
}

// ==================== 生命周期 ====================

/**
 * 组件挂载时：
 * - 如果是学生，加载统计数据
 * - 加载通知公告（所有角色都需要）
 */
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

/* 欢迎卡片 */
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

/* 统计卡片行 */
.stats-row {
  margin-bottom: 20px;
}

/* 统计卡片样式 */
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

/* 快捷操作卡片 */
.quick-actions-card {
  margin-bottom: 20px;

  .quick-actions {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
}

/* 通知公告卡片 */
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
