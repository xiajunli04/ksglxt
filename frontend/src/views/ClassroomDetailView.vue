<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Upload } from '@element-plus/icons-vue'
import { getClassroomDetail, getAvailableSlots, addClassroom, updateClassroom } from '@/api/classroom'
import { createBooking, getSlotBooking } from '@/api/booking'
import { uploadFile } from '@/api/file'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const classroom = ref<Record<string, unknown>>({})
const slotsDate = ref(new Date().toISOString().slice(0, 10))
const slots = ref<Record<string, unknown>[]>([])
const slotsLoading = ref(false)

const isAdmin = ref(userStore.isAdmin)
const classroomId = Number(route.params.id)
const isEdit = route.query.edit === 'true'
const isAdd = route.name === 'ClassroomAdd'

const editMode = ref(isAdd)
const editForm = ref<Record<string, unknown>>({
  roomName: '',
  building: '',
  floor: '',
  roomType: '',
  capacity: '',
  status: '0',
  facilities: '',
  remark: '',
})

const typeOptions = ['多媒体教室', '普通教室', '实验室', '会议室']
const statusOptions = [
  { label: '可用', value: '0' },
  { label: '停用', value: '1' },
  { label: '维修中', value: '2' },
]

// Booking dialog
const bookingDialogVisible = ref(false)
const bookingLoading = ref(false)
const bookingSlotId = ref<number | null>(null)
const bookingSlotName = ref('')
const attachmentName = ref('')

// Slot booking info dialog
const slotInfoVisible = ref(false)
const slotInfoLoading = ref(false)
const slotInfoData = ref<Record<string, unknown>>({})

const bookingForm = ref({
  purpose: '',
  attendees: '',
  phone: '',
  attachment: '',
})

const fileSize = 5 * 1024 * 1024
const allowedTypes = ['.pdf', '.doc', '.docx', '.jpg', '.png']

const fetchDetail = async () => {
  if (!classroomId) return
  loading.value = true
  try {
    const res = await getClassroomDetail(classroomId)
    classroom.value = (res.data as Record<string, unknown>) || {}
    if (isEdit) {
      editForm.value = { ...classroom.value }
    }
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const fetchSlots = async () => {
  if (!slotsDate.value || !classroomId) return
  slotsLoading.value = true
  try {
    const res = await getAvailableSlots(classroomId, slotsDate.value)
    slots.value = (res.data as Record<string, unknown>[]) || []
  } catch {
    // handled
  } finally {
    slotsLoading.value = false
  }
}

const goBooking = () => {
  router.push(`/admin/booking/create?classroomId=${classroomId}`)
}

const handleSlotClick = async (slot: Record<string, unknown>) => {
  if (slot.available) {
    // Available slot - show booking dialog
    await ElMessageBox.confirm(
      `确认预约此时段？\n课室：${classroom.value.roomName}\n日期：${slotsDate.value}\n时段：${slot.slotName} (${slot.startTime} - ${slot.endTime})`,
      '确认预约',
      { confirmButtonText: '确认预约', cancelButtonText: '取消', type: 'info' }
    )
    bookingSlotId.value = slot.id as number
    bookingSlotName.value = `${slot.slotName} (${slot.startTime} - ${slot.endTime})`
    bookingForm.value = { purpose: '', attendees: '', phone: '', attachment: '' }
    attachmentName.value = ''
    bookingDialogVisible.value = true
  } else {
    // Booked slot - show booking info
    slotInfoLoading.value = true
    slotInfoVisible.value = true
    try {
      const res = await getSlotBooking(classroomId, slotsDate.value, slot.id as number)
      slotInfoData.value = (res as Record<string, unknown>).data as Record<string, unknown> || {}
    } catch {
      slotInfoData.value = {}
    } finally {
      slotInfoLoading.value = false
    }
  }
}

const handleBeforeUpload = (file: File) => {
  const ext = '.' + file.name.split('.').pop()?.toLowerCase()
  if (!allowedTypes.includes(ext)) {
    ElMessage.error(`只允许上传 ${allowedTypes.join(', ')} 格式的文件`)
    return false
  }
  if (file.size > fileSize) {
    ElMessage.error('文件大小不能超过 5MB')
    return false
  }
  return true
}

const handleUploadRequest = async (options: { file: File; onSuccess: (resp: unknown) => void; onError: (err: unknown) => void }) => {
  try {
    const res = await uploadFile(options.file)
    const data = res as Record<string, unknown>
    if (data.code === 200) {
      bookingForm.value.attachment = data.data as string
      attachmentName.value = options.file.name
      options.onSuccess(res)
      ElMessage.success('上传成功')
    } else {
      options.onError(new Error(data.msg as string))
      ElMessage.error((data.msg as string) || '上传失败')
    }
  } catch (e) {
    options.onError(e)
    ElMessage.error('上传失败')
  }
}

const handleBookingSubmit = async () => {
  if (!bookingForm.value.purpose) {
    ElMessage.warning('请输入用途')
    return
  }
  if (!bookingForm.value.phone) {
    ElMessage.warning('请输入联系电话')
    return
  }
  bookingLoading.value = true
  try {
    await createBooking({
      classroomId: classroomId,
      bookingDate: slotsDate.value,
      timeSlotId: bookingSlotId.value,
      purpose: bookingForm.value.purpose,
      expectedAttendance: Number(bookingForm.value.attendees) || undefined,
      contactPhone: bookingForm.value.phone,
      attachmentUrl: bookingForm.value.attachment || undefined,
    })
    ElMessage.success('预约申请已提交')
    bookingDialogVisible.value = false
    fetchSlots()
  } catch {
    // handled
  } finally {
    bookingLoading.value = false
  }
}

const handleSave = async () => {
  try {
    if (classroomId) {
      await updateClassroom(classroomId, editForm.value)
      ElMessage.success('更新成功')
    } else {
      await addClassroom(editForm.value)
      ElMessage.success('创建成功')
      router.push('/admin/classroom')
      return
    }
    editMode.value = false
    fetchDetail()
  } catch {
    // handled
  }
}

const cancelEdit = () => {
  if (isAdd) {
    router.back()
  } else {
    editMode.value = false
    editForm.value = { ...classroom.value }
  }
}

const getSlotStatus = (available: boolean) => {
  return available ? '空闲' : '已预约'
}

const getSlotType = (available: boolean) => {
  return available ? 'success' : 'danger'
}

onMounted(() => {
  fetchDetail()
  fetchSlots()
})
</script>

<template>
  <div v-loading="loading" class="classroom-detail">
    <!-- Edit/Add mode -->
    <template v-if="editMode">
      <el-card shadow="hover">
        <template #header>
          <span class="card-title">{{ isAdd ? '新增课室' : '编辑课室' }}</span>
        </template>
        <el-form :model="editForm" label-width="100px" style="max-width: 600px">
          <el-form-item label="名称" required>
            <el-input v-model="editForm.roomName" placeholder="请输入课室名称" />
          </el-form-item>
          <el-form-item label="楼栋" required>
            <el-input v-model="editForm.building" placeholder="请输入楼栋" />
          </el-form-item>
          <el-form-item label="楼层">
            <el-input v-model="editForm.floor" placeholder="请输入楼层" />
          </el-form-item>
          <el-form-item label="类型" required>
            <el-select v-model="editForm.roomType" placeholder="请选择类型">
              <el-option v-for="opt in typeOptions" :key="opt" :label="opt" :value="opt" />
            </el-select>
          </el-form-item>
          <el-form-item label="容量" required>
            <el-input-number v-model="editForm.capacity" :min="1" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="editForm.status" placeholder="请选择状态">
              <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="设备">
            <el-input v-model="editForm.facilities" type="textarea" placeholder="请输入设备信息" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="editForm.remark" type="textarea" placeholder="请输入备注" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </template>

    <!-- View mode -->
    <template v-else>
      <el-card shadow="hover">
        <template #header>
          <div class="header-bar">
            <span class="card-title">课室详情</span>
            <div>
              <el-button v-if="isAdmin" type="warning" @click="editMode = true">编辑</el-button>
              <el-button type="primary" @click="goBooking">预约此课室</el-button>
              <el-button @click="router.back()">返回</el-button>
            </div>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="名称">{{ classroom.roomName }}</el-descriptions-item>
          <el-descriptions-item label="楼栋">{{ classroom.building }}</el-descriptions-item>
          <el-descriptions-item label="楼层">{{ classroom.floor }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ classroom.roomType }}</el-descriptions-item>
          <el-descriptions-item label="容量">{{ classroom.capacity }}</el-descriptions-item>
          <el-descriptions-item label="设备">{{ classroom.facilities }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ classroom.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Available slots -->
      <el-card shadow="hover" style="margin-top: 20px">
        <template #header>
          <div class="header-bar">
            <span class="card-title">时段预约状态</span>
            <div class="slot-search">
              <el-date-picker
                v-model="slotsDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                :icon="Calendar"
                @change="fetchSlots"
              />
            </div>
          </div>
        </template>
        <div v-loading="slotsLoading">
          <div v-if="slots.length === 0" class="empty-text">暂无时段数据</div>
          <div v-else class="slots-grid">
            <el-tag
              v-for="slot in slots"
              :key="slot.slotName as string"
              :type="(getSlotType(slot.available as boolean) as 'success' | 'danger')"
              size="large"
              class="slot-tag"
              :class="{ 'slot-clickable': slot.available }"
              @click="handleSlotClick(slot as Record<string, unknown>)"
            >
              {{ slot.slotName }} - {{ getSlotStatus(slot.available as boolean) }}
            </el-tag>
          </div>
        </div>
      </el-card>
    </template>

    <!-- Booking dialog -->
    <el-dialog v-model="bookingDialogVisible" title="预约课室" width="500px" destroy-on-close>
      <el-descriptions :column="1" border size="small" style="margin-bottom: 16px">
        <el-descriptions-item label="课室">{{ classroom.roomName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ slotsDate }}</el-descriptions-item>
        <el-descriptions-item label="时段">{{ bookingSlotName }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px">
        <el-form-item label="用途" required>
          <el-input v-model="bookingForm.purpose" type="textarea" :rows="2" placeholder="请输入用途说明" />
        </el-form-item>
        <el-form-item label="人数">
          <el-input-number v-model="bookingForm.attendees" :min="1" placeholder="预计人数" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="bookingForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            :auto-upload="true"
            :show-file-list="false"
            :before-upload="handleBeforeUpload"
            :http-request="handleUploadRequest"
            accept=".pdf,.doc,.docx,.jpg,.png"
          >
            <el-button :icon="Upload">选择文件</el-button>
          </el-upload>
          <div v-if="attachmentName" class="attachment-name">{{ attachmentName }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="bookingLoading" @click="handleBookingSubmit">提交预约</el-button>
      </template>
    </el-dialog>

    <!-- Slot booking info dialog -->
    <el-dialog v-model="slotInfoVisible" title="预约信息" width="450px" destroy-on-close>
      <div v-loading="slotInfoLoading">
        <template v-if="slotInfoData.applicantName">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="预约人">{{ slotInfoData.applicantName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ slotInfoData.contactPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用途">{{ slotInfoData.purpose || '-' }}</el-descriptions-item>
            <el-descriptions-item label="预计人数">{{ slotInfoData.expectedAttendance || '-' }}</el-descriptions-item>
            <el-descriptions-item label="预约日期">{{ slotInfoData.bookingDate }}</el-descriptions-item>
            <el-descriptions-item label="时段">{{ slotInfoData.timeSlotName }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <div v-else class="empty-text">暂无预约信息</div>
      </div>
      <template #footer>
        <el-button @click="slotInfoVisible = false">关闭</el-button>
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

.slot-search {
  display: flex;
  gap: 12px;
}

.slots-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.slot-tag {
  min-width: 200px;
  text-align: center;
}

.slot-clickable {
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}

.slot-clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.attachment-name {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}

.empty-text {
  text-align: center;
  color: #909399;
  font-size: 14px;
  padding: 20px 0;
}
</style>
