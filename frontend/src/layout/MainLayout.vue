<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  House,
  Document,
  Bell,
  Setting,
  User,
  Calendar,
  Plus,
  List,
  Tickets,
  Notification,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const isCollapse = ref(false)

const isAdmin = computed(() => userStore.isAdmin)

const nickName = computed(() => (userStore.userInfo?.nickName as string) || '用户')

const unreadCount = computed(() => notificationStore.unreadCount)
const hasUnread = computed(() => notificationStore.hasUnread)

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/admin/profile')
  } else if (command === 'password') {
    router.push('/admin/profile?tab=password')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      userStore.logout()
    }).catch(() => {
      // cancel
    })
  }
}

const goNotification = () => {
  router.push('/admin/notification')
}

onMounted(() => {
  notificationStore.startPolling()
})

onUnmounted(() => {
  notificationStore.stopPolling()
})
</script>

<template>
  <el-container class="main-layout">
    <el-header class="main-header">
      <div class="header-left" @click="router.push('/')" style="cursor: pointer">
        <img src="/logo.jpg" alt="logo" class="header-logo-img" />
        <span class="logo-text">广软课室申请系统</span>
      </div>
      <div class="header-right">
        <el-badge :value="unreadCount" :hidden="!hasUnread" :max="99" class="notification-badge">
          <el-button :icon="Bell" circle size="small" @click="goNotification" />
        </el-badge>
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-dropdown">
            <el-icon><User /></el-icon>
            <span class="user-name">{{ nickName }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container class="main-body">
      <el-aside :width="isCollapse ? '64px' : '220px'" class="main-aside">
        <el-menu
          :default-active="route.path"
          :collapse="isCollapse"
          router
          class="aside-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <el-menu-item index="/admin/classroom">
            <el-icon><House /></el-icon>
            <template #title>课室管理</template>
          </el-menu-item>

          <!-- Admin menus -->
          <template v-if="isAdmin">
            <el-sub-menu index="approval">
              <template #title>
                <el-icon><Tickets /></el-icon>
                <span>审批管理</span>
              </template>
              <el-menu-item index="/admin/approval/pending">
                <el-icon><Document /></el-icon>
                <template #title>待审批</template>
              </el-menu-item>
              <el-menu-item index="/admin/approval/all">
                <el-icon><List /></el-icon>
                <template #title>全部申请</template>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/admin/announcement">
              <el-icon><Notification /></el-icon>
              <template #title>公告管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/user/list">
              <el-icon><Setting /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
          </template>

          <!-- Teacher/Student menus -->
          <template v-if="!isAdmin">
            <el-sub-menu index="booking">
              <template #title>
                <el-icon><Calendar /></el-icon>
                <span>我的预约</span>
              </template>
              <el-menu-item index="/admin/booking/my">
                <el-icon><List /></el-icon>
                <template #title>我的申请</template>
              </el-menu-item>
              <el-menu-item index="/admin/booking/create">
                <el-icon><Plus /></el-icon>
                <template #title>新建申请</template>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/admin/announcement">
              <el-icon><Notification /></el-icon>
              <template #title>公告通知</template>
            </el-menu-item>
            <el-menu-item index="/admin/notification">
              <el-icon><Bell /></el-icon>
              <template #title>我的消息</template>
            </el-menu-item>
          </template>
        </el-menu>
        <div class="collapse-btn" @click="isCollapse = !isCollapse">
          <el-icon v-if="isCollapse"><HomeFilled /></el-icon>
          <el-icon v-else><HomeFilled /></el-icon>
        </div>
      </el-aside>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.main-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-logo-img {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  object-fit: cover;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-badge {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}

.user-dropdown:hover {
  color: #409EFF;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-body {
  overflow: hidden;
}

.main-aside {
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.aside-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.aside-menu:not(.el-menu--collapse) {
  width: 220px;
}

.collapse-btn {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #bfcbd9;
  border-top: 1px solid #3a4a5b;
}

.collapse-btn:hover {
  color: #409EFF;
}

.main-content {
  background: #f0f2f5;
  overflow-y: auto;
  padding: 20px;
}
</style>
