<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getClassroomList, deleteClassroom, updateClassroom } from '@/api/classroom'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref<Record<string, unknown>[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  building: '',
  roomType: '',
  status: '',
})

const buildingOptions = [
  { label: 'A栋', value: 'A栋' },
  { label: 'B栋', value: 'B栋' },
  { label: 'C栋', value: 'C栋' },
]

const typeOptions = [
  { label: '多媒体教室', value: '多媒体教室' },
  { label: '普通教室', value: '普通教室' },
  { label: '实验室', value: '实验室' },
  { label: '会议室', value: '会议室' },
]

const statusOptions = [
  { label: '可用', value: '0' },
  { label: '停用', value: '1' },
  { label: '维修中', value: '2' },
]

const isAdmin = ref(userStore.isAdmin)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getClassroomList(queryParams)
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

const handlePageChange = (page: number) => {
  queryParams.pageNum = page
  fetchData()
}

const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  fetchData()
}

const goDetail = (id: number) => {
  router.push(`/admin/classroom/${id}`)
}

const goEdit = (id: number) => {
  router.push(`/admin/classroom/${id}?edit=true`)
}

const handleDelete = (row: Record<string, unknown>) => {
  ElMessageBox.confirm(`确定要删除课室"${row.roomName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteClassroom(row.id as number)
      ElMessage.success('删除成功')
      fetchData()
    } catch {
      // handled
    }
  }).catch(() => {})
}

const goAdd = () => {
  router.push('/admin/classroom/add')
}

const handleStatusChange = async (row: Record<string, unknown>, newStatus: string) => {
  const label = getStatusLabel(newStatus)
  await ElMessageBox.confirm(`确定将课室"${row.roomName}"状态改为"${label}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
  try {
    await updateClassroom(row.id as number, { status: newStatus })
    ElMessage.success('状态已更新')
    fetchData()
  } catch {
    // handled
  }
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    '0': 'success',
    '2': 'warning',
    '1': 'danger',
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    '0': '可用',
    '2': '维修中',
    '1': '停用',
  }
  return map[status] || status
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="classroom-list">
    <el-card shadow="hover">
      <!-- Search bar -->
      <div class="search-bar">
        <el-select v-model="queryParams.building" placeholder="选择楼栋" clearable style="width: 140px">
          <el-option v-for="opt in buildingOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-select v-model="queryParams.roomType" placeholder="选择类型" clearable style="width: 140px">
          <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-select v-model="queryParams.status" placeholder="选择状态" clearable style="width: 140px">
          <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button v-if="isAdmin" type="success" :icon="Plus" @click="goAdd">新增课室</el-button>
      </div>

      <!-- Table -->
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="roomCode" label="编号" width="80" />
        <el-table-column prop="roomName" label="名称" min-width="120" />
        <el-table-column prop="building" label="楼栋" width="100" />
        <el-table-column prop="roomType" label="类型" width="120" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-dropdown v-if="isAdmin" trigger="click" @command="(cmd: string) => handleStatusChange(row, cmd)">
              <el-tag
                :type="(getStatusType(row.status as string) as 'success' | 'warning' | 'danger' | 'info')"
                size="small"
                style="cursor: pointer"
              >
                {{ getStatusLabel(row.status as string) }}
              </el-tag>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="opt in statusOptions"
                    :key="opt.value"
                    :command="opt.value"
                    :disabled="opt.value === row.status"
                  >
                    <el-tag :type="(getStatusType(opt.value) as 'success' | 'warning' | 'danger' | 'info')" size="small">
                      {{ opt.label }}
                    </el-tag>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-tag v-else :type="(getStatusType(row.status as string) as 'success' | 'warning' | 'danger' | 'info')" size="small">
              {{ getStatusLabel(row.status as string) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goDetail(row.id as number)">
              详情
            </el-button>
            <el-button v-if="isAdmin" type="warning" link size="small" @click="goEdit(row.id as number)">
              编辑
            </el-button>
            <el-button v-if="isAdmin" type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
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
