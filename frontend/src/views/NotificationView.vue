<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotificationList, markRead, markAllRead } from '@/api/notification'

const loading = ref(false)
const tableData = ref<Record<string, unknown>[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const typeMap: Record<string, string> = {
  '1': '审批通过通知',
  '2': '审批驳回通知',
  '3': '系统通知',
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getNotificationList({ pageNum: page.value, pageSize: pageSize.value })
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
  fetchList()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  page.value = 1
  fetchList()
}

const handleMarkRead = async (row: Record<string, unknown>) => {
  if (row.isRead === '1' || row.isRead === true) return
  try {
    await markRead(row.id as number)
    row.isRead = '1'
    ElMessage.success('已标记已读')
  } catch {
    // handled
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllRead()
    ElMessage.success('已全部标记已读')
    fetchList()
  } catch {
    // handled
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="notification-view">
    <el-card shadow="hover">
      <template #header>
        <div class="header-bar">
          <span class="card-title">我的消息</span>
          <el-button type="primary" size="small" @click="handleMarkAllRead">全部已读</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            {{ typeMap[row.type as string] || row.type }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" min-width="150" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="((row.isRead === '1' || row.isRead === true) ? 'info' : 'danger') as 'info' | 'danger'" size="small">
              {{ (row.isRead === '1' || row.isRead === true) ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button
              v-if="!(row.isRead === '1' || row.isRead === true)"
              type="primary"
              link
              size="small"
              @click="handleMarkRead(row)"
            >
              标记已读
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
