<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getUserList, updateUserStatus } from '@/api/user'

const loading = ref(false)
const tableData = ref<Record<string, unknown>[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: '',
})

const statusOptions = [
  { label: '正常', value: '0' },
  { label: '停用', value: '1' },
]

const roleMap: Record<string, string> = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生',
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
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

const handleToggleStatus = async (row: Record<string, unknown>) => {
  const newStatus = row.status === '0' ? '1' : '0'
  const label = newStatus === '0' ? '启用' : '停用'
  try {
    await updateUserStatus(row.id as number, newStatus)
    ElMessage.success(`${label}成功`)
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
  <div class="user-list">
    <el-card shadow="hover">
      <template #header>
        <span class="card-title">用户管理</span>
      </template>

      <div class="search-bar">
        <el-input
          v-model="queryParams.keyword"
          placeholder="用户名/手机号"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
          <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="userName" label="用户名" min-width="100" />
        <el-table-column prop="nickName" label="昵称" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column label="角色" width="80">
          <template #default="{ row }">
            {{ roleMap[(row.roles as string[])[0]] || (row.roles as string[])[0] }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="(row.status === '0' ? 'success' : 'danger') as 'success' | 'danger'" size="small">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="150" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.status === '0' ? 'danger' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === '0' ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
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
