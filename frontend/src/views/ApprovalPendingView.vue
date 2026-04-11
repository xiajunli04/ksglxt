<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPendingList, approve, reject } from '@/api/approval'

const router = useRouter()

const loading = ref(false)
const tableData = ref<Record<string, unknown>[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const rejectReason = ref('')
const rejectDialogVisible = ref(false)
const currentRejectId = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPendingList({ pageNum: page.value, pageSize: pageSize.value })
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

const handleApprove = async (id: number) => {
  try {
    await approve(id)
    ElMessage.success('已通过')
    fetchData()
  } catch {
    // handled
  }
}

const showRejectDialog = (id: number) => {
  currentRejectId.value = id
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const handleReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    await reject(currentRejectId.value, rejectReason.value)
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    fetchData()
  } catch {
    // handled
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="approval-pending">
    <el-card shadow="hover">
      <template #header>
        <span class="card-title">待审批列表</span>
      </template>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="applicantName" label="申请人" min-width="80" />
        <el-table-column prop="classroomName" label="课室" min-width="100" />
        <el-table-column prop="bookingDate" label="日期" min-width="100" />
        <el-table-column prop="timeSlotName" label="时段" min-width="120" />
        <el-table-column prop="purpose" label="用途" min-width="120" show-overflow-tooltip />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="expectedAttendance" label="人数" width="70" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="router.push(`/admin/booking/${row.id}`)">
              详情
            </el-button>
            <el-popconfirm title="确定通过此申请吗？" @confirm="handleApprove(row.id as number)">
              <template #reference>
                <el-button type="success" link size="small">通过</el-button>
              </template>
            </el-popconfirm>
            <el-button type="danger" link size="small" @click="showRejectDialog(row.id as number)">
              驳回
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
