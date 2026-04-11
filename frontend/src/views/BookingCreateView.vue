<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getClassroomList, getAvailableSlots } from '@/api/classroom'
import { createBooking } from '@/api/booking'
import { uploadFile } from '@/api/file'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const classroomOptions = ref<Record<string, unknown>[]>([])
const availableSlots = ref<Record<string, unknown>[]>([])
const slotsLoading = ref(false)

const formRef = ref()

const form = reactive({
  classroomId: '',
  bookingDate: '',
  timeSlot: '',
  purpose: '',
  attendees: '',
  phone: '',
  attachment: '',
})

const preClassroomId = route.query.classroomId as string
const preBookingDate = route.query.bookingDate as string
const preTimeSlotId = route.query.timeSlotId as string
if (preClassroomId) {
  form.classroomId = preClassroomId
}
if (preBookingDate) {
  form.bookingDate = preBookingDate
}

const rules = {
  classroomId: [{ required: true, message: '请选择课室', trigger: 'change' }],
  bookingDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时段', trigger: 'change' }],
  purpose: [{ required: true, message: '请输入用途', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

const fileSize = 5 * 1024 * 1024
const allowedTypes = ['.pdf', '.doc', '.docx', '.jpg', '.png']

const fetchClassrooms = async () => {
  try {
    const res = await getClassroomList({ pageNum: 1, pageSize: 100, status: '0' })
    const data = res.data as { records: Record<string, unknown>[] }
    classroomOptions.value = data.records || []
  } catch {
    // handled
  }
}

const fetchSlots = async () => {
  if (!form.classroomId || !form.bookingDate) {
    availableSlots.value = []
    return
  }
  slotsLoading.value = true
  try {
    const res = await getAvailableSlots(Number(form.classroomId), form.bookingDate)
    availableSlots.value = (res.data as Record<string, unknown>[]) || []
  } catch {
    // handled
  } finally {
    slotsLoading.value = false
  }
}

const handleClassroomChange = () => {
  form.timeSlot = ''
  fetchSlots()
}

const handleDateChange = () => {
  form.timeSlot = ''
  fetchSlots()
}

const availableSlotOptions = computed(() => {
  return availableSlots.value.filter((s) => s.available)
})

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

const attachmentName = ref('')

const handleUploadRequest = async (options: { file: File; onSuccess: (resp: unknown) => void; onError: (err: unknown) => void }) => {
  try {
    const res = await uploadFile(options.file)
    const data = res as Record<string, unknown>
    if (data.code === 200) {
      form.attachment = data.data as string
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

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  await ElMessageBox.confirm('确定提交此预约申请吗？', '确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info',
  })

  loading.value = true
  try {
    await createBooking({
      classroomId: Number(form.classroomId),
      bookingDate: form.bookingDate,
      timeSlotId: Number(form.timeSlot),
      purpose: form.purpose,
      expectedAttendance: Number(form.attendees) || undefined,
      contactPhone: form.phone,
      attachmentUrl: form.attachment || undefined,
    })
    ElMessage.success('申请提交成功')
    router.push('/admin/booking/my')
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchClassrooms()
  if (form.classroomId && form.bookingDate) {
    await fetchSlots()
    if (preTimeSlotId) {
      form.timeSlot = preTimeSlotId
    }
  }
})
</script>

<template>
  <div class="booking-create">
    <el-card shadow="hover">
      <template #header>
        <span class="card-title">新建预约申请</span>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 600px"
      >
        <el-form-item label="课室" prop="classroomId">
          <el-select
            v-model="form.classroomId"
            placeholder="请选择课室"
            filterable
            style="width: 100%"
            @change="handleClassroomChange"
          >
            <el-option
              v-for="item in classroomOptions"
              :key="item.id as number"
              :label="`${item.roomName} (${item.building})`"
              :value="String(item.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期" prop="bookingDate">
          <el-date-picker
            v-model="form.bookingDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="timeSlot">
          <el-radio-group v-loading="slotsLoading" v-model="form.timeSlot">
            <el-radio
              v-for="slot in availableSlotOptions"
              :key="slot.id"
              :value="String(slot.id)"
            >
              {{ slot.slotName }} ({{ slot.startTime }} - {{ slot.endTime }})
            </el-radio>
            <div v-if="availableSlotOptions.length === 0 && form.classroomId && form.bookingDate" class="no-slots">
              暂无空闲时段
            </div>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="用途" prop="purpose">
          <el-input v-model="form.purpose" type="textarea" :rows="3" placeholder="请输入用途说明" />
        </el-form-item>
        <el-form-item label="人数">
          <el-input-number v-model="form.attendees" :min="1" placeholder="预计人数" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
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
          <div class="upload-tip">支持 pdf, doc, docx, jpg, png 格式，最大 5MB</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交申请</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.no-slots {
  color: #909399;
  font-size: 14px;
  padding: 8px 0;
}

.attachment-name {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
