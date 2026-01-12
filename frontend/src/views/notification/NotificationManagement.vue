<!-- src/views/notification/NotificationManagement.vue -->
<template>
  <div class="notification-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>通知管理</span>
          <div>
            <el-button v-if="isAdmin" type="primary" @click="showSendDialog = true">
              发送系统通知
            </el-button>
            <el-button type="warning" @click="markAllAsRead" :loading="markAllLoading">
              全部标记为已读
            </el-button>
          </div>
        </div>
      </template>

      <!-- 通知列表 -->
      <div v-loading="loading">
        <div v-if="notifications.length === 0" class="empty">
          <el-empty description="暂无通知" />
        </div>

        <div v-else class="notification-list">
          <div v-for="item in notifications" :key="item.id"
               class="notification-item"
               :class="{ 'unread': !item.read, 'important': item.important }"
               @click="markAsRead(item)">
            <div class="notification-header">
              <div class="title-section">
                <el-tag v-if="!item.read" type="danger" size="small">未读</el-tag>
                <el-tag v-if="item.important" type="warning" size="small">重要</el-tag>
                <span class="type-tag">{{ getTypeText(item.type) }}</span>
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
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 发送通知对话框（管理员） -->
    <el-dialog
        v-model="showSendDialog"
        title="发送系统通知"
        width="500px"
    >
      <el-form :model="notificationForm" label-width="80px">
        <!-- 在发送通知对话框中 -->
        <el-form-item label="接收用户">
          <el-select
              v-model="notificationForm.userId"
              clearable
              placeholder="选择用户（不选则发送给所有人）"
              filterable
          >
            <el-option
                v-for="user in userList"
                :key="user.id"
                :label="user.realName + ' (' + user.username + ')'"
                :value="user.id"
            />
          </el-select>
          <div v-if="userList.length === 0" class="empty-users">
            暂无用户数据或加载失败
          </div>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="notificationForm.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
              v-model="notificationForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入通知内容"
          />
        </el-form-item>
        <el-form-item label="重要通知">
          <el-switch v-model="notificationForm.important" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showSendDialog = false">取消</el-button>
          <el-button type="primary" @click="sendNotification" :loading="sending">
            发送
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isSystemAdmin)

// 数据
const notifications = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 发送通知相关
const showSendDialog = ref(false)
const sending = ref(false)
const notificationForm = ref({
  userId: null,
  title: '',
  content: '',
  important: false
})
const userList = ref([])
const markAllLoading = ref(false)

// 通知类型映射
const typeMap = {
  'RESERVATION_AVAILABLE': '预定可借',
  'RESERVATION_REMINDER': '预定提醒',
  'OVERDUE_REMINDER': '逾期提醒',
  'SYSTEM_NOTIFICATION': '系统通知'
}

// 获取通知列表
const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await request.get('/notifications/user', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    if (res.code === 200) {
      notifications.value = res.data.content
      total.value = res.data.totalElements
    }
  } catch (error) {
    console.error('获取通知失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取未读数量
const fetchUnreadCount = async () => {
  try {
    const res = await request.get('/notifications/unread-count')
    if (res.code === 200) {
      // 可以在这里更新全局的未读数量
      console.log('未读通知数量:', res.data)
    }
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

// 标记为已读
const markAsRead = async (item) => {
  if (item.read) return

  try {
    const res = await request.put(`/notifications/mark-read/${item.id}`)
    if (res.code === 200) {
      item.read = true
      ElMessage.success('标记为已读')
      fetchUnreadCount()
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 全部标记为已读
const markAllAsRead = async () => {
  markAllLoading.value = true
  try {
    const res = await request.put('/notifications/mark-all-read')
    if (res.code === 200) {
      ElMessage.success('全部标记为已读')
      notifications.value.forEach(item => item.read = true)
      fetchUnreadCount()
    }
  } catch (error) {
    console.error('全部标记已读失败:', error)
  } finally {
    markAllLoading.value = false
  }
}

// 发送通知（管理员）
const sendNotification = async () => {
  if (!notificationForm.value.title.trim()) {
    ElMessage.warning('请输入通知标题')
    return
  }
  if (!notificationForm.value.content.trim()) {
    ElMessage.warning('请输入通知内容')
    return
  }

  sending.value = true
  try {
    const endpoint = notificationForm.value.important
        ? '/notifications/admin/important'
        : '/notifications/admin/system'

    const res = await request.post(endpoint, notificationForm.value)
    if (res.code === 200) {
      ElMessage.success('通知发送成功')
      showSendDialog.value = false
      notificationForm.value = {
        userId: null,
        title: '',
        content: '',
        important: false
      }
      fetchNotifications()
    }
  } catch (error) {
    console.error('发送通知失败:', error)
  } finally {
    sending.value = false
  }
}

// 获取用户列表（带分页参数）
const fetchUserList = async () => {
  if (!isAdmin.value) return

  try {
    const res = await request.get('/users', {
      params: {
        page: 0,  // 默认第一页
        size: 100, // 获取较大数量，或者你也可以实现分页
        sort: 'id' // 默认按id排序
      }
    })
    if (res.code === 200) {
      // 如果后端返回的是分页对象，可能是 res.data.content
      // 如果是直接返回用户列表，可能是 res.data
      userList.value = res.data.content || res.data
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchNotifications()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchNotifications()
}

// 辅助函数
const getTypeText = (type) => {
  return typeMap[type] || type
}

const formatTime = (time) => {
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 初始化
onMounted(() => {
  fetchNotifications()
  fetchUnreadCount()
  if (isAdmin.value) {
    fetchUserList()
  }
})
</script>

<style scoped>
.notification-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-list {
  max-height: 600px;
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
</style>