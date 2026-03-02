<!--
  主应用布局组件
  包含顶部导航栏、侧边菜单和主内容区域
-->
<template>
  <el-container class="app-layout">
    <!-- 侧边菜单 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <!-- Logo区域 -->
      <div class="sidebar-logo">
        <span v-if="!isCollapse">奖学金评定系统</span>
        <span v-else>奖学金</span>
      </div>

      <!-- 菜单 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="sidebar-menu"
      >
        <!-- 首页 -->
        <el-menu-item index="/app/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>系统首页</template>
        </el-menu-item>

        <!-- 研究生菜单 -->
        <el-sub-menu index="student" v-if="isStudent">
          <template #title>
            <el-icon><User /></el-icon>
            <span>研究生管理</span>
          </template>
          <el-menu-item index="/app/student/profile">个人信息</el-menu-item>
          <el-menu-item index="/app/student/achievements">科研成果</el-menu-item>
          <el-menu-item index="/app/student/application">奖学金申请</el-menu-item>
          <el-menu-item index="/app/student/result">评定结果</el-menu-item>
        </el-sub-menu>

        <!-- 导师菜单 -->
        <el-sub-menu index="tutor" v-if="isTutor">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>导师管理</span>
          </template>
          <el-menu-item index="/app/tutor/review">科研成果审核</el-menu-item>
          <el-menu-item index="/app/tutor/students">指导学生管理</el-menu-item>
        </el-sub-menu>

        <!-- 管理员菜单 -->
        <el-sub-menu index="admin" v-if="isAdmin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/app/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/app/admin/students">研究生信息管理</el-menu-item>
          <el-menu-item index="/app/admin/rules">评分规则管理</el-menu-item>
          <el-menu-item index="/app/admin/evaluation">评定管理</el-menu-item>
          <el-menu-item index="/app/admin/results">结果管理</el-menu-item>
          <el-menu-item index="/app/admin/system">系统设置</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 主体区域 -->
    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="app-header">
        <!-- 折叠按钮 -->
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>

        <!-- 右侧操作区 -->
        <div class="header-right">
          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar" />
              <span class="user-name">{{ userInfo.realName || userInfo.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { ComputedRef } from 'vue'
import {
  HomeFilled, User, Reading, Setting, Fold, Expand,
  Lock, SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { UserInfo } from '@/utils/secureStorage'

/**
 * 路由实例
 */
const route = useRoute()
const router = useRouter()

/**
 * 用户状态管理
 */
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo) as ComputedRef<UserInfo>

/**
 * 侧边栏折叠状态
 */
const isCollapse = ref(false)

/**
 * 当前激活的菜单路径
 */
const activeMenu = computed(() => route.path)

/**
 * 用户类型
 */
const userType = computed(() => userInfo.value.userType)

/**
 * 是否为学生角色
 */
const isStudent = computed(() => userType.value === 1)

/**
 * 是否为导师角色
 */
const isTutor = computed(() => userType.value === 2)

/**
 * 是否为管理员角色
 */
const isAdmin = computed(() => userType.value === 3)

/**
 * 切换侧边栏折叠状态
 */
function toggleCollapse(): void {
  isCollapse.value = !isCollapse.value
}

/**
 * 处理下拉菜单命令
 * @param command - 命令类型
 */
function handleCommand(command: string): void {
  switch (command) {
    case 'profile':
      router.push('/app/student/profile')
      break
    case 'password':
      ElMessage.info('修改密码功能开发中')
      break
    case 'logout':
      handleLogout()
      break
  }
}

/**
 * 处理退出登录
 */
async function handleLogout(): Promise<void> {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    })
    .catch(() => {
      // 用户取消
    })
}
</script>

<style scoped lang="scss">
.app-layout {
  width: 100%;
  height: 100vh;
}

.sidebar {
  background: #304156;
  transition: width 0.3s;
  overflow-x: hidden;

  .sidebar-logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    font-weight: 500;
    color: #ffffff;
    background: #2b3a4b;
  }

  .sidebar-menu {
    border: none;
    background: #304156;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      color: #bfcbd9;

      &:hover {
        background: #263445;
      }
    }

    :deep(.el-menu-item.is-active) {
      background: #409eff !important;
      color: #ffffff !important;
    }

    :deep(.el-sub-menu .el-menu-item) {
      background: #1f2d3d !important;

      &:hover {
        background: #001528 !important;
      }
    }
  }
}

.main-container {
  display: flex;
  flex-direction: column;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;

  .header-left {
    display: flex;
    align-items: center;

    .collapse-icon {
      font-size: 20px;
      cursor: pointer;
      color: #909399;

      &:hover {
        color: #409eff;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .user-name {
        font-size: 14px;
        color: #303133;
      }
    }
  }
}

.app-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
