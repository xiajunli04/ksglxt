<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyBookings, cancelBooking } from '@/api/booking'

const router = useRouter()

const loading = ref(false)
const activeTab = ref('all')
const tableData = ref<Record<string, unknown>[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

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

const fetchData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      pageNum: page.value,
      pageSize: pageSize.value,
    }
    if (activeTab.value !== 'all') {
      params.status = activeTab.value
    }
    const res = await getMyBookings(params)
    const data = res.data as { records: Record<string, unknown>[]; total: number }
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  page.value = 1
  fetchData()
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
  router.push(`/admin/booking/${id}`)
}

const handleReapply = (row: Record<string, unknown>) => {
  router.push({
    path: '/admin/booking/create',
    query: {
      classroomId: String(row.classroomId),
      bookingDate: String(row.bookingDate),
      timeSlotId: String(row.timeSlotId),
    },
  })
}

const handleCancel = (row: Record<string, unknown>) => {
  ElMessageBox.confirm('确定要取消该申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await cancelBooking(row.id as number)
      ElMessage.success('取消成功')
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
  <div class="my-booking">
    <el-card shadow="hover">
      <template #header>
        <span class="card-title">我的申请</span>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待审批" name="0" />
        <el-tab-pane label="已通过" name="1" />
        <el-tab-pane label="已驳回" name="2" />
        <el-tab-pane label="已取消" name="3" />
      </el-tabs>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="classroomName" label="课室" min-width="100" />
        <el-table-column prop="bookingDate" label="日期" min-width="100" />
        <el-table-column prop="timeSlotName" label="时段" min-width="120" />
        <el-table-column prop="purpose" label="用途" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="(getStatusTag(row.status as string).type as 'success' | 'warning' | 'danger' | 'info' | 'primary')" size="small">
              {{ getStatusTag(row.status as string).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goDetail(row.id as number)">
              查看
            </el-button>
            <el-button
              v-if="row.status === '0' || row.status === '1'"
              type="danger"
              link
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.status === '2' || row.status === '3'"
              type="warning"
              link
              size="small"
              @click="handleReapply(row)"
            >
              重新申请
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
