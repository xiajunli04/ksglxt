<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAllList } from '@/api/approval'

const loading = ref(false)
const tableData = ref<Record<string, unknown>[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  startDate: '',
  endDate: '',
  classroomId: '',
})

const statusOptions = [
  { label: '待审批', value: '0' },
  { label: '已通过', value: '1' },
  { label: '已驳回', value: '2' },
  { label: '已取消', value: '3' },
  { label: '已完成', value: '4' },
]

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
    const res = await getAllList(queryParams)
    const data = res.data as { records: Record<string, unknown>[]; total: number }
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchData()
}

const handlePageChange = (val: number) => {
  queryParams.pageNum = val
  fetchData()
}

const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  queryParams.pageNum = 1
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="approval-all">
    <el-card shadow="hover">
      <template #header>
        <span class="card-title">全部申请</span>
      </template>

      <div class="search-bar">
        <el-select v-model="queryParams.status" placeholder="选择状态" clearable style="width: 120px">
          <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-date-picker
          v-model="queryParams.startDate"
          type="date"
          placeholder="开始日期"
          value-format="YYYY-MM-DD"
          style="width: 160px"
        />
        <el-date-picker
          v-model="queryParams.endDate"
          type="date"
          placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 160px"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="applicantName" label="申请人" min-width="80" />
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
        <el-table-column prop="approverId" label="审批人" width="80" />
        <el-table-column prop="createTime" label="申请时间" min-width="150" />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
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

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
