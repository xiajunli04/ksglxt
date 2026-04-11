<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  House,
  Tickets,
  Calendar,
  Document,
  DataLine,
  CircleCheck,
  Bell,
  Warning,
  ArrowRight,
} from '@element-plus/icons-vue'
import {
  getDashboardStats,
  getRecentAnnouncements,
  getHotClassrooms,
  getAvailableClassrooms,
  getRecentBookings,
} from '@/api/dashboard'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const stats = ref<Record<string, number>>({})
const announcements = ref<Array<{
  id: number
  title: string
  isTop: string
  createTime: string
}>>([])
const hotClassrooms = ref<Array<{
  id: number
  roomCode: string
  roomName: string
  capacity: number
  roomType: string
  bookingCount: number
}>>([])
const availableClassrooms = ref<Array<{
  id: number
  roomCode: string
  roomName: string
  capacity: number
  roomType: string
  availableSlots: string
}>>([])
const recentBookings = ref<Array<{
  id: number
  roomName: string
  bookingDate: string
  startTime: string
  endTime: string
  status: string
  purpose: string
  applicantName?: string
}>>([])

const isAdmin = computed(() => userStore.isAdmin)
const isTeacher = computed(() => userStore.userRole === 'TEACHER')
const isStudent = computed(() => userStore.userRole === 'STUDENT')

const statusMap: Record<string, { label: string; type: string }> = {
  '0': { label: '审批中', type: 'warning' },
  '1': { label: '已通过', type: 'success' },
  '2': { label: '已驳回', type: 'danger' },
  '3': { label: '已取消', type: 'info' },
  '4': { label: '已完成', type: '' },
}

const getStatusTag = (status: string) => statusMap[status] || { label: '未知', type: 'info' }

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  return timeStr.substring(0, 5)
}

const fetchAllData = async () => {
  loading.value = true
  try {
    const [statsRes, announcementsRes, bookingsRes] = await Promise.all([
      getDashboardStats(),
      getRecentAnnouncements(),
      getRecentBookings(),
    ])
    stats.value = (statsRes.data as Record<string, number>) || {}
    announcements.value = (announcementsRes.data as typeof announcements.value) || []
    recentBookings.value = (bookingsRes.data as typeof recentBookings.value) || []

    if (!isStudent.value) {
      const hotRes = await getHotClassrooms()
      hotClassrooms.value = (hotRes.data as typeof hotClassrooms.value) || []
    }
    if (isStudent.value) {
      const availRes = await getAvailableClassrooms()
      availableClassrooms.value = (availRes.data as typeof availableClassrooms.value) || []
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const goAnnouncement = (id: number) => router.push(`/admin/announcement/${id}`)
const goAllAnnouncements = () => router.push('/admin/announcement')
const goBookingDetail = (id: number) => router.push(`/admin/booking/${id}`)
const goAllBookings = () => {
  if (isAdmin.value) router.push('/admin/approval/pending')
  else router.push('/admin/booking/my')
}
const goClassroomDetail = (id: number) => router.push(`/admin/classroom/${id}`)
const goNotifications = () => router.push('/admin/notification')

onMounted(fetchAllData)
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <!-- Admin Stats -->
    <template v-if="isAdmin">
      <el-row :gutter="16" class="stat-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: #ECF5FF">
              <el-icon :size="28" color="#409EFF"><House /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalClassrooms ?? 0 }}</div>
              <div class="stat-label">课室总数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: #FDF6EC">
              <el-icon :size="28" color="#E6A23C"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingApprovals ?? 0 }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: #F0F9EB">
              <el-icon :size="28" color="#67C23A"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayBookings ?? 0 }}</div>
              <div class="stat-label">今日预约</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: #F4F4F5">
              <el-icon :size="28" color="#909399"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.monthBookings ?? 0 }}</div>
              <div class="stat-label">本月预约</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- Teacher Stats -->
    <template v-else-if="isTeacher">
      <el-row :gutter="16" class="stat-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: #ECF5FF">
              <el-icon :size="28" color="#409EFF"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.myBookings ?? 0 }}</div>
              <div class="stat-label">我的申请</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: #F0F9EB">
              <el-icon :size="28" color="#67C23A"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.approvalRate ?? 0 }}%</div>
              <div class="stat-label">已通过率</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: #FDF6EC">
              <el-icon :size="28" color="#E6A23C"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayBookings ?? 0 }}</div>
              <div class="stat-label">今日预约</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: #F4F4F5">
              <el-icon :size="28" color="#909399"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.weekBookings ?? 0 }}</div>
              <div class="stat-label">本周预约</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- Student Stats -->
    <template v-else>
      <el-row :gutter="16" class="stat-row">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: #ECF5FF">
              <el-icon :size="28" color="#409EFF"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.myBookings ?? 0 }}</div>
              <div class="stat-label">我的申请</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: #FDF6EC">
              <el-icon :size="28" color="#E6A23C"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.myPending ?? 0 }}</div>
              <div class="stat-label">待审批中</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: #F0F9EB">
              <el-icon :size="28" color="#67C23A"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.myToday ?? 0 }}</div>
              <div class="stat-label">今日预约</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- Content Panels Row 1 -->
    <el-row :gutter="16" class="content-row">
      <!-- Left: Recent Bookings / Pending Approvals -->
      <el-col :span="12">
        <el-card shadow="hover" class="content-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">{{ isAdmin ? '待审批申请' : '我的最近申请' }}</span>
              <el-button link type="primary" @click="goAllBookings">
                查看全部 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-if="recentBookings.length === 0" class="empty-tip">暂无数据</div>
          <div v-else class="booking-list">
            <div
              v-for="item in recentBookings"
              :key="item.id"
              class="booking-item"
              @click="goBookingDetail(item.id)"
            >
              <div class="booking-main">
                <span class="booking-room">{{ item.roomName }}</span>
                <span class="booking-info">
                  {{ item.purpose || '预约申请' }}
                </span>
              </div>
              <div class="booking-meta">
                <span class="booking-date">{{ formatDate(item.bookingDate) }} {{ formatTime(item.startTime) }}-{{ formatTime(item.endTime) }}</span>
                <el-tag :type="(getStatusTag(item.status).type as any)" size="small">
                  {{ getStatusTag(item.status).label }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right: Hot Classrooms / Available Classrooms -->
      <el-col :span="12">
        <el-card shadow="hover" class="content-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">{{ isStudent ? '今日推荐可用课室' : '热门课室 Top 5' }}</span>
            </div>
          </template>
          <!-- Hot Classrooms (Admin/Teacher) -->
          <template v-if="!isStudent">
            <div v-if="hotClassrooms.length === 0" class="empty-tip">暂无数据</div>
            <div v-else class="hot-list">
              <div
                v-for="(item, idx) in hotClassrooms"
                :key="item.id"
                class="hot-item"
                @click="goClassroomDetail(item.id)"
              >
                <span class="hot-rank" :class="{ 'rank-top': idx < 3 }">{{ idx + 1 }}</span>
                <div class="hot-info">
                  <span class="hot-name">{{ item.roomName }}</span>
                  <span class="hot-code">{{ item.roomCode }}</span>
                </div>
                <span class="hot-count">{{ item.bookingCount }}次</span>
              </div>
            </div>
          </template>
          <!-- Available Classrooms (Student) -->
          <template v-else>
            <div v-if="availableClassrooms.length === 0" class="empty-tip">暂无可用课室</div>
            <div v-else class="available-list">
              <div
                v-for="item in availableClassrooms"
                :key="item.id"
                class="available-item"
                @click="goClassroomDetail(item.id)"
              >
                <div class="available-info">
                  <span class="available-name">{{ item.roomName }}</span>
                  <el-tag size="small" type="info">{{ item.roomType }}</el-tag>
                </div>
                <span class="available-slots">{{ item.availableSlots }}</span>
              </div>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- Content Panels Row 2 -->
    <el-row :gutter="16" class="content-row">
      <!-- Left: Announcements -->
      <el-col :span="12">
        <el-card shadow="hover" class="content-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">最新公告</span>
              <el-button link type="primary" @click="goAllAnnouncements">
                查看全部 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-if="announcements.length === 0" class="empty-tip">暂无公告</div>
          <div v-else class="announcement-list">
            <div
              v-for="item in announcements"
              :key="item.id"
              class="announcement-item"
              @click="goAnnouncement(item.id)"
            >
              <el-tag v-if="item.isTop === '1'" type="danger" size="small" class="top-tag">置顶</el-tag>
              <span class="announcement-title">{{ item.title }}</span>
              <span class="announcement-date">{{ formatDate(item.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right: Unread Notifications -->
      <el-col :span="12">
        <el-card shadow="hover" class="content-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">未读通知</span>
              <el-button link type="primary" @click="goNotifications">
                查看全部 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="empty-tip">
            <el-icon :size="32" color="#C0C4CC"><Bell /></el-icon>
            <p>通知消息将在此处显示</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  height: 100%;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* Content Cards */
.content-row {
  margin-bottom: 16px;
}

.content-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.empty-tip {
  text-align: center;
  padding: 32px 0;
  color: #C0C4CC;
  font-size: 14px;
}

.empty-tip p {
  margin: 8px 0 0;
}

/* Booking List */
.booking-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.booking-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #F2F3F5;
  cursor: pointer;
  transition: background 0.2s;
}

.booking-item:last-child {
  border-bottom: none;
}

.booking-item:hover {
  background: #F5F7FA;
  margin: 0 -20px;
  padding: 12px 20px;
}

.booking-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.booking-room {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.booking-info {
  font-size: 12px;
  color: #909399;
}

.booking-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.booking-date {
  font-size: 12px;
  color: #C0C4CC;
}

/* Hot Classrooms */
.hot-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.hot-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #F2F3F5;
  cursor: pointer;
}

.hot-item:last-child {
  border-bottom: none;
}

.hot-item:hover {
  background: #F5F7FA;
  margin: 0 -20px;
  padding: 10px 20px;
}

.hot-rank {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #909399;
  background: #F4F4F5;
  flex-shrink: 0;
  margin-right: 12px;
}

.hot-rank.rank-top {
  color: #fff;
  background: #409EFF;
}

.hot-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.hot-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.hot-code {
  font-size: 12px;
  color: #909399;
}

.hot-count {
  font-size: 14px;
  font-weight: 600;
  color: #409EFF;
}

/* Available Classrooms */
.available-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.available-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #F2F3F5;
  cursor: pointer;
}

.available-item:last-child {
  border-bottom: none;
}

.available-item:hover {
  background: #F5F7FA;
  margin: 0 -20px;
  padding: 10px 20px;
}

.available-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.available-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.available-slots {
  font-size: 13px;
  color: #67C23A;
  font-weight: 500;
}

/* Announcements */
.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.announcement-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #F2F3F5;
  cursor: pointer;
  gap: 8px;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-item:hover {
  background: #F5F7FA;
  margin: 0 -20px;
  padding: 12px 20px;
}

.top-tag {
  flex-shrink: 0;
}

.announcement-title {
  flex: 1;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-date {
  font-size: 12px;
  color: #C0C4CC;
  flex-shrink: 0;
}
</style>
