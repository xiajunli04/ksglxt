<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAnnouncementList, deleteAnnouncement } from '@/api/announcement'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref<Record<string, unknown>[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const isAdmin = ref(userStore.isAdmin)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList({ pageNum: page.value, pageSize: pageSize.value })
    const data = res.data as { records: Record<string, unknown>[]; total: number }
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const handlePageChange = (val: number) => {
  page.value = val
  fetchData()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  page.value = 1
  fetchData()
}

const goDetail = (id: number) => {
  router.push(`/admin/announcement/${id}`)
}

const goAdd = () => {
  router.push('/admin/announcement/add')
}

const goEdit = (id: number) => {
  router.push(`/admin/announcement/${id}?edit=true`)
}

const handleDelete = (row: Record<string, unknown>) => {
  ElMessageBox.confirm(`确定要删除公告"${row.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteAnnouncement(row.id as number)
      ElMessage.success('删除成功')
      fetchData()
    } catch {
      // handled
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="announcement-list">
    <el-card shadow="hover">
      <template #header>
        <div class="header-bar">
          <span class="card-title">公告通知</span>
          <el-button v-if="isAdmin" type="success" :icon="Plus" @click="goAdd">新增公告</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id as number)">
              {{ row.title }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column prop="createTime" label="发布时间" min-width="150" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="(row.status === '1' ? 'success' : 'info') as 'success' | 'info'" size="small">
              {{ row.status === '1' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goDetail(row.id as number)">
              查看
            </el-button>
            <el-button type="warning" link size="small" @click="goEdit(row.id as number)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
