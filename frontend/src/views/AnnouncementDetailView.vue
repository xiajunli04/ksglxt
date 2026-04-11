<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAnnouncementDetail, addAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/announcement'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref<Record<string, unknown>>({})
const announcementId = Number(route.params.id)
const isEdit = route.query.edit === 'true'
const isAdd = route.name === 'AnnouncementAdd'

const isAdmin = ref(userStore.isAdmin)
const editMode = ref(isAdd)
const editForm = ref<Record<string, unknown>>({
  title: '',
  content: '',
  status: '1',
})

const fetchDetail = async () => {
  if (!announcementId) return
  loading.value = true
  try {
    const res = await getAnnouncementDetail(announcementId)
    detail.value = (res.data as Record<string, unknown>) || {}
    if (isEdit) {
      editForm.value = { ...detail.value }
    }
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!editForm.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!editForm.value.content) {
    ElMessage.warning('请输入内容')
    return
  }
  try {
    if (announcementId) {
      await updateAnnouncement(announcementId, editForm.value)
      ElMessage.success('更新成功')
    } else {
      await addAnnouncement(editForm.value)
      ElMessage.success('发布成功')
      router.push('/admin/announcement')
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
    editForm.value = { ...detail.value }
  }
}

const handleDelete = async () => {
  await ElMessageBox.confirm('确定要删除此公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
  try {
    await deleteAnnouncement(announcementId)
    ElMessage.success('删除成功')
    router.push('/admin/announcement')
  } catch {
    // handled
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div v-loading="loading" class="announcement-detail">
    <!-- Edit/Add mode -->
    <template v-if="editMode">
      <el-card shadow="hover">
        <template #header>
          <span class="card-title">{{ isAdd ? '新增公告' : '编辑公告' }}</span>
        </template>
        <el-form :model="editForm" label-width="80px" style="max-width: 700px">
          <el-form-item label="标题" required>
            <el-input v-model="editForm.title" placeholder="请输入公告标题" />
          </el-form-item>
          <el-form-item label="内容" required>
            <el-input v-model="editForm.content" type="textarea" :rows="10" placeholder="请输入公告内容" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="editForm.status">
              <el-radio value="1">发布</el-radio>
              <el-radio value="0">草稿</el-radio>
            </el-radio-group>
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
            <span class="card-title">{{ detail.title }}</span>
            <div>
              <el-button v-if="isAdmin" type="warning" @click="editMode = true">编辑</el-button>
              <el-button v-if="isAdmin" type="danger" @click="handleDelete">删除</el-button>
              <el-button @click="router.back()">返回</el-button>
            </div>
          </div>
        </template>
        <div class="announcement-meta">
          <span>发布人：{{ detail.publisherName }}</span>
          <span>发布时间：{{ detail.createTime }}</span>
        </div>
        <div class="announcement-content">
          {{ detail.content }}
        </div>
      </el-card>
    </template>
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

.announcement-meta {
  display: flex;
  gap: 24px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #EBEEF5;
}

.announcement-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}
</style>
