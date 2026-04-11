<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookingDetail, cancelBooking } from '@/api/booking'
import { approve, reject } from '@/api/approval'
import { downloadFile } from '@/api/file'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref<Record<string, unknown>>({})
const bookingId = Number(route.params.id)
const rejectReason = ref('')
const rejectDialogVisible = ref(false)

const isAdmin = computed(() => userStore.isAdmin)

const statusMap: Record<string, { type: string; label: string }> = {
  '0': { type: 'warning', label: '待审批' },
  '1': { type: 'success', label: '已通过' },
  '2': { type: 'danger', label: '已驳回' },
  '3': { type: 'info', label: '已取消' },
  '4': { type: 'primary', label: '已完成' },
}

const getStatusTag = (status: string) => {
  return statusMap[status] || { type: 'info', label: status }
}

const fetchDetail = async () => {
  if (!bookingId) return
  loading.value = true
  try {
    const res = await getBookingDetail(bookingId)
    detail.value = (res.data as Record<string, unknown>) || {}
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const handleApprove = async () => {
  await ElMessageBox.confirm('确定通过此申请吗？', '确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info',
  })
  try {
    await approve(bookingId)
    ElMessage.success('审批通过')
    fetchDetail()
  } catch {
    // handled
  }
}

const showRejectDialog = () => {
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const handleReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    await reject(bookingId, rejectReason.value)
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    fetchDetail()
  } catch {
    // handled
  }
}

const handleCancel = async () => {
  await ElMessageBox.confirm('确定要取消该申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
  try {
    await cancelBooking(bookingId)
    ElMessage.success('取消成功')
    fetchDetail()
  } catch {
    // handled
  }
}

const handleDownload = async (filename: string) => {
  try {
    const res = await downloadFile(filename)
    const blob = new Blob([res as BlobPart])
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    window.URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('下载失败')
  }
}

const getAttachmentDisplay = (url: string) => {
  if (!url) return ''
  // If it's just a UUID filename, return a friendly name
  if (!url.includes('/')) return '附件文件'
  return url.split('/').pop() || '附件文件'
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div v-loading="loading" class="booking-detail">
    <el-card shadow="hover">
      <template #header>
        <div class="header-bar">
          <span class="card-title">申请详情</span>
          <div>
            <!-- Admin actions -->
            <template v-if="isAdmin && detail.status === '0'">
              <el-button type="success" @click="handleApprove">通过</el-button>
              <el-button type="danger" @click="showRejectDialog">驳回</el-button>
            </template>
            <!-- User cancel -->
            <el-button
              v-if="detail.status === '0' && !isAdmin"
              type="warning"
              @click="handleCancel"
            >
              取消申请
            </el-button>
            <el-button @click="router.back()">返回</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请人">{{ detail.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="课室">{{ detail.classroomName }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ detail.bookingDate }}</el-descriptions-item>
        <el-descriptions-item label="时间段">{{ detail.timeSlotName }}</el-descriptions-item>
        <el-descriptions-item label="用途" :span="2">{{ detail.purpose }}</el-descriptions-item>
        <el-descriptions-item label="人数">{{ detail.expectedAttendance }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="(getStatusTag(detail.status as string).type as 'success' | 'warning' | 'danger' | 'info' | 'primary')">
            {{ getStatusTag(detail.status as string).label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.rejectReason" label="驳回原因" :span="2">
          <span style="color: #F56C6C">{{ detail.rejectReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="附件">
          <template v-if="detail.attachmentUrl">
            <el-button type="primary" link @click="handleDownload(detail.attachmentUrl as string)">
              下载附件
            </el-button>
          </template>
          <template v-else>无</template>
        </el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detail.approverId || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- Reject dialog -->
    <el-dialog v-model="rejectDialogVisible" title="驳回申请" width="400px">
      <el-form>
        <el-form-item label="驳回原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
