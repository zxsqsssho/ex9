<!-- src/views/user/MyNotifications.vue -->

<template>
  <div class="my-notifications">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的通知</span>
          <div>
            <el-button type="primary" @click="refreshNotifications">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="success" @click="markAllAsRead" :disabled="unreadCount === 0" :loading="markAllLoading">
              <el-icon><Check /></el-icon>
              全部标记为已读
            </el-button>
          </div>
        </div>
      </template>

      <!-- 通知统计 -->
      <div class="stats-container">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <el-icon class="icon total"><MessageBox /></el-icon>
                <div class="stat-info">
                  <span class="stat-count">{{ pagination.total }}</span>
                  <span class="stat-label">总通知数</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <el-icon class="icon unread"><Bell /></el-icon>
                <div class="stat-info">
                  <span class="stat-count">{{ unreadCount }}</span>
                  <span class="stat-label">未读通知</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 通知列表 -->
      <div v-loading="loading">
        <div v-if="notifications.length === 0" class="empty">
          <el-empty description="暂无通知" />
        </div>

        <div v-else class="notification-list">
          <div
              v-for="item in notifications"
              :key="item.id"
              class="notification-item"
              :class="{ 'unread': !item.read, 'important': item.important }"
              @click="markAsRead(item)"
          >
            <div class="notification-header">
              <div class="title-section">
                <el-tag v-if="!item.read" type="danger" size="small">未读</el-tag>
                <el-tag v-if="item.important" type="warning" size="small">重要</el-tag>
                <span class="type-tag">{{ getTypeLabel(item.type) }}</span>
                <span class="title">{{ item.title }}</span>
              </div>
              <span class="time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <div class="notification-content">
              {{ item.content }}
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
              v-model:current-page="pagination.current"
              v-model:page-size="pagination.size"
              :total="pagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  Check,
  MessageBox,
  Bell
} from '@element-plus/icons-vue'
import { getMyNotifications, markAsRead as markAsReadApi, markAllAsRead as markAllAsReadApi, getUnreadCount } from '@/api/notification'

// 通知数据
const notifications = ref([])

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 加载状态
const loading = ref(false)
const markAllLoading = ref(false)

// 未读数量
const unreadCount = ref(0)

// 获取通知类型标签文本
const getTypeLabel = (type) => {
  const labelMap = {
    RESERVATION_AVAILABLE: '预定可借',
    RESERVATION_REMINDER: '预定提醒',
    OVERDUE_REMINDER: '逾期提醒',
    SYSTEM_NOTIFICATION: '系统通知'
  }
  return labelMap[type] || '通知'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 今天
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `今天 ${hours}:${minutes}`
  }

  // 昨天
  if (diff < 2 * 24 * 60 * 60 * 1000) {
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `昨天 ${hours}:${minutes}`
  }

  // 一周内
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000))
    return `${days}天前`
  }

  // 更早
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 加载通知
const loadNotifications = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.size
    }

    const response = await getMyNotifications(params)
    if (response.code === 200) {
      notifications.value = response.data.content || []
      pagination.total = response.data.totalElements || 0

      // 计算未读数量
      unreadCount.value = notifications.value.filter(item => !item.read).length
    } else {
      ElMessage.error(response.message || '加载通知失败')
    }
  } catch (error) {
    console.error('加载通知失败:', error)
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const response = await getUnreadCount()
    if (response.code === 200) {
      unreadCount.value = response.data || 0
    }
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

// 刷新通知
const refreshNotifications = async () => {
  pagination.current = 1
  await loadNotifications()
  ElMessage.success('通知已刷新')
}

// 标记为已读
const markAsRead = async (item) => {
  if (item.read) return

  try {
    const response = await markAsReadApi(item.id)
    if (response.code === 200) {
      item.read = true
      unreadCount.value--
      ElMessage.success('标记为已读')
    } else {
      ElMessage.error(response.message || '标记为已读失败')
    }
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记为已读失败')
  }
}

// 全部标记为已读
const markAllAsRead = async () => {
  try {
    await ElMessageBox.confirm('确认将所有通知标记为已读？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    markAllLoading.value = true
    const response = await markAllAsReadApi()
    if (response.code === 200) {
      notifications.value.forEach(item => {
        item.read = true
      })
      unreadCount.value = 0
      ElMessage.success('全部标记为已读成功')
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记全部为已读失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    markAllLoading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadNotifications()
}

// 处理当前页变化
const handleCurrentChange = (page) => {
  pagination.current = page
  loadNotifications()
}

// 组件挂载时
onMounted(() => {
  loadNotifications()
  loadUnreadCount()
})
</script>

<style scoped>
.my-notifications {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-container {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  margin-bottom: 10px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 15px;
}

.stat-item .icon {
  font-size: 32px;
}

.stat-item .icon.total {
  color: #409eff;
}

.stat-item .icon.unread {
  color: #e6a23c;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-count {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.notification-list {
  max-height: 500px;
  overflow-y: auto;
}

.notification-item {
  padding: 15px;
  margin-bottom: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #f0f9ff;
  border-left: 4px solid #409eff;
}

.notification-item.important {
  border-color: #e6a23c;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-tag {
  padding: 2px 6px;
  background-color: #f2f6fc;
  border-radius: 3px;
  font-size: 12px;
  color: #909399;
}

.title {
  font-weight: bold;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
}

.notification-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.empty {
  text-align: center;
  padding: 50px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-notifications {
    padding: 12px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .notification-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .title-section {
    flex-wrap: wrap;
  }
}
</style>