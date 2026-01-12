<!--<template>-->
<!--  <div class="my-space-page">-->
<!--    <el-card shadow="hover">-->
<!--      <el-tabs v-model="activeTab" type="card">-->
<!--        &lt;!&ndash; 我的借阅 &ndash;&gt;-->
<!--        <el-tab-pane label="当前借阅" name="borrowing">-->
<!--          <div class="table-container">-->
<!--            <el-table :data="borrowList" border stripe v-loading="loading">-->
<!--              <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>-->
<!--              <el-table-column prop="borrowTime" label="借阅时间" min-width="180"></el-table-column>-->
<!--              <el-table-column prop="dueTime" label="应还时间" min-width="180">-->
<!--                <template #default="scope">-->
<!--                  <span :class="scope.row.overdueDays > 0 ? 'text-red' : ''">-->
<!--                    {{ scope.row.dueTime }}-->
<!--                    <span v-if="scope.row.overdueDays > 0">(逾期{{ scope.row.overdueDays }}天)</span>-->
<!--                  </span>-->
<!--                </template>-->
<!--              </el-table-column>-->
<!--              <el-table-column prop="branchName" label="借阅分馆" min-width="120"></el-table-column>-->
<!--              <el-table-column label="操作" fixed="right" min-width="180">-->
<!--                <template #default="scope">-->
<!--                  <div class="action-buttons">-->
<!--                    <el-button-->
<!--                        type="primary"-->
<!--                        size="small"-->
<!--                        @click="handleReturn(scope.row.id)"-->
<!--                        :loading="returnLoading === scope.row.id"-->
<!--                    >-->
<!--                      归还-->
<!--                    </el-button>-->
<!--                    <el-button-->
<!--                        type="warning"-->
<!--                        size="small"-->
<!--                        @click="handleRenew(scope.row.bookId)"-->
<!--                        v-if="scope.row.overdueDays <= 0"-->
<!--                    >-->
<!--                      续借-->
<!--                    </el-button>-->
<!--                  </div>-->
<!--                </template>-->
<!--              </el-table-column>-->
<!--            </el-table>-->
<!--          </div>-->
<!--        </el-tab-pane>-->

<!--        &lt;!&ndash; 借阅历史 &ndash;&gt;-->
<!--        <el-tab-pane label="借阅历史" name="borrowHistory">-->
<!--          <div class="table-container">-->
<!--            <el-table :data="borrowHistoryList" border stripe v-loading="loading">-->
<!--              <el-table-column prop="bookName" label="图书名称"></el-table-column>-->
<!--              <el-table-column prop="borrowTime" label="借阅时间"></el-table-column>-->
<!--              <el-table-column prop="returnTime" label="归还时间"></el-table-column>-->
<!--              <el-table-column prop="status" label="借阅状态">-->
<!--                <template #default="scope">-->
<!--                  {{ scope.row.status === 'RETURNED' ? '已归还' : '已逾期归还' }}-->
<!--                </template>-->
<!--              </el-table-column>-->
<!--            </el-table>-->
<!--          </div>-->
<!--        </el-tab-pane>-->

<!--        &lt;!&ndash; 我的预定 &ndash;&gt;-->
<!--        <el-tab-pane label="我的预定" name="reservation">-->
<!--          <div class="table-container">-->
<!--            <el-table :data="reservationList" border stripe v-loading="loading">-->
<!--              <el-table-column prop="bookName" label="图书名称"></el-table-column>-->
<!--              <el-table-column prop="reserveTime" label="预定时间"></el-table-column>-->
<!--              <el-table-column prop="expiryTime" label="预定有效期"></el-table-column>-->
<!--              <el-table-column prop="status" label="状态">-->
<!--                <template #default="scope">-->
<!--                  <el-tag v-if="scope.row.status === 'PENDING'">待处理</el-tag>-->
<!--                  <el-tag type="success" v-if="scope.row.status === 'READY'">可借阅</el-tag>-->
<!--                  <el-tag type="danger" v-if="scope.row.status === 'CANCELLED'">已取消</el-tag>-->
<!--                  <el-tag type="info" v-if="scope.row.status === 'COMPLETED'">已完成</el-tag>-->
<!--                </template>-->
<!--              </el-table-column>-->
<!--              <el-table-column label="操作">-->
<!--                <template #default="scope">-->
<!--                  <el-button-->
<!--                      type="danger"-->
<!--                      size="small"-->
<!--                      @click="handleCancelReservation(scope.row.id)"-->
<!--                      v-if="scope.row.status === 'PENDING'"-->
<!--                  >-->
<!--                    取消预定-->
<!--                  </el-button>-->
<!--                </template>-->
<!--              </el-table-column>-->
<!--            </el-table>-->
<!--          </div>-->
<!--        </el-tab-pane>-->

<!--        &lt;!&ndash; 罚款记录 &ndash;&gt;-->
<!--        <el-tab-pane label="罚款记录" name="fines">-->
<!--          <div class="table-container">-->
<!--            <el-table :data="fineList" border stripe v-loading="loading">-->
<!--              <el-table-column prop="bookName" label="图书名称"></el-table-column>-->
<!--              <el-table-column prop="fineAmount" label="罚款金额(元)"></el-table-column>-->
<!--              <el-table-column prop="overdueDays" label="逾期天数"></el-table-column>-->
<!--              <el-table-column prop="payStatus" label="支付状态">-->
<!--                <template #default="scope">-->
<!--                  <el-tag type="danger" v-if="scope.row.payStatus === 'unpaid'">未支付</el-tag>-->
<!--                  <el-tag type="success" v-if="scope.row.payStatus === 'paid'">已支付</el-tag>-->
<!--                </template>-->
<!--              </el-table-column>-->
<!--              <el-table-column label="操作">-->
<!--                <template #default="scope">-->
<!--                  <el-button-->
<!--                      type="primary"-->
<!--                      size="small"-->
<!--                      @click="handlePayFine(scope.row.fineId)"-->
<!--                      v-if="scope.row.payStatus === 'unpaid'"-->
<!--                  >-->
<!--                    立即支付-->
<!--                  </el-button>-->
<!--                </template>-->
<!--              </el-table-column>-->
<!--            </el-table>-->
<!--          </div>-->
<!--        </el-tab-pane>-->

<!--        &lt;!&ndash; 新增：我的通知标签页 &ndash;&gt;-->
<!--        <el-tab-pane label="我的通知" name="notifications">-->
<!--          <div class="notifications-container">-->
<!--            &lt;!&ndash; 通知统计和操作栏 &ndash;&gt;-->
<!--            <div class="notifications-header">-->
<!--              <div class="stats">-->
<!--                <span class="unread-count">未读通知: {{ notificationUnreadCount }}</span>-->
<!--                <el-button-->
<!--                    type="primary"-->
<!--                    size="small"-->
<!--                    @click="markAllAsRead"-->
<!--                    :loading="markAllLoading"-->
<!--                    :disabled="notificationUnreadCount === 0"-->
<!--                >-->
<!--                  全部标记为已读-->
<!--                </el-button>-->
<!--              </div>-->
<!--            </div>-->

<!--            &lt;!&ndash; 通知列表 &ndash;&gt;-->
<!--            <div v-loading="notificationsLoading">-->
<!--              <div v-if="notificationsList.length === 0" class="empty-notifications">-->
<!--                <el-empty description="暂无通知" />-->
<!--              </div>-->

<!--              <div v-else class="notifications-list">-->
<!--                <div-->
<!--                    v-for="item in notificationsList"-->
<!--                    :key="item.id"-->
<!--                    class="notification-item"-->
<!--                    :class="{ 'unread': !item.read, 'important': item.important }"-->
<!--                    @click="markAsRead(item)"-->
<!--                >-->
<!--                  <div class="notification-content">-->
<!--                    <div class="notification-header">-->
<!--                      <div class="title-section">-->
<!--                        <el-tag v-if="!item.read" type="danger" size="small">未读</el-tag>-->
<!--                        <el-tag v-if="item.important" type="warning" size="small">重要</el-tag>-->
<!--                        <span class="type-tag">{{ getNotificationTypeText(item.type) }}</span>-->
<!--                        <span class="title">{{ item.title }}</span>-->
<!--                      </div>-->
<!--                      <span class="time">{{ formatNotificationTime(item.createdAt) }}</span>-->
<!--                    </div>-->
<!--                    <div class="notification-body">-->
<!--                      {{ item.content }}-->
<!--                    </div>-->
<!--                  </div>-->
<!--                </div>-->
<!--              </div>-->

<!--              &lt;!&ndash; 分页 &ndash;&gt;-->
<!--              <div class="pagination">-->
<!--                <el-pagination-->
<!--                    v-model:current-page="notificationPage"-->
<!--                    v-model:page-size="notificationPageSize"-->
<!--                    :total="notificationTotal"-->
<!--                    :page-sizes="[10, 20, 50]"-->
<!--                    layout="total, sizes, prev, pager, next, jumper"-->
<!--                    @size-change="handleNotificationSizeChange"-->
<!--                    @current-change="handleNotificationPageChange"-->
<!--                />-->
<!--              </div>-->
<!--            </div>-->
<!--          </div>-->
<!--        </el-tab-pane>-->

<!--      </el-tabs>-->
<!--    </el-card>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--  import { ref, onMounted, watch, computed } from 'vue'-->
<!--  import { ElMessage, ElMessageBox } from 'element-plus'-->
<!--  import { useRouter } from 'vue-router'-->
<!--  import { Bell } from '@element-plus/icons-vue'-->
<!--  import router from '@/router'-->

<!--  // 导入API函数-->
<!--  import {-->
<!--  getMyBorrowList as apiGetMyBorrowList,-->
<!--  getMyBorrowHistory as apiGetMyBorrowHistory,-->
<!--  returnBook as apiReturnBook,-->
<!--  renewBook as apiRenewBook-->
<!--} from '@/api/borrow'-->

<!--  import {-->
<!--  getMyReservations as apiGetMyReservations,-->
<!--  cancelReservation as apiCancelReservation-->
<!--} from '@/api/reservation'-->

<!--  import {-->
<!--  getMyFines as apiGetMyFines,-->
<!--  payFine as apiPayFine-->
<!--} from '@/api/fines'-->

<!--  // 通知相关API（需要你创建）-->
<!--  import {-->
<!--  getMyNotifications as apiGetMyNotifications,-->
<!--  getUnreadCount as apiGetUnreadCount,-->
<!--  markAsRead as apiMarkAsRead,-->
<!--  markAllAsRead as apiMarkAllAsRead-->
<!--} from '@/api/notification'-->

<!--  const activeTab = ref('borrowing')-->
<!--  const loading = ref(false)-->
<!--  const returnLoading = ref('')-->

<!--  // 数据列表-->
<!--  const borrowList = ref([])-->
<!--  const borrowHistoryList = ref([])-->
<!--  const reservationList = ref([])-->
<!--  const fineList = ref([])-->

<!--  // 新增：通知相关数据-->
<!--  const notificationsList = ref([])-->
<!--  const notificationsLoading = ref(false)-->
<!--  const notificationUnreadCount = ref(0)-->
<!--  const notificationPage = ref(1)-->
<!--  const notificationPageSize = ref(10)-->
<!--  const notificationTotal = ref(0)-->
<!--  const markAllLoading = ref(false)-->

<!--  // 顶部通知铃铛的未读数量-->
<!--  const unreadCount = ref(0)-->

<!--  onMounted(() => {-->
<!--  loadAllData()-->
<!--})-->

<!--  // 加载所有数据-->
<!--  const loadAllData = async () => {-->
<!--  loading.value = true-->
<!--  try {-->
<!--  await Promise.all([-->
<!--  getMyBorrowList(),-->
<!--  getMyBorrowHistory(),-->
<!--  getMyReservation(),-->
<!--  getMyFines()-->
<!--  ])-->
<!--} catch (error) {-->
<!--  ElMessage.error('数据加载失败')-->
<!--} finally {-->
<!--  loading.value = false-->
<!--}-->
<!--}-->

<!--// 获取当前借阅列表-->
<!--const getMyBorrowList = async () => {-->
<!--  try {-->
<!--    const res = await apiGetMyBorrowList()-->
<!--    borrowList.value = res.data-->
<!--  } catch (error) {-->
<!--    ElMessage.error('获取借阅列表失败')-->
<!--  }-->
<!--}-->

<!--// 获取借阅历史-->
<!--const getMyBorrowHistory = async () => {-->
<!--  try {-->
<!--    const res = await apiGetMyBorrowHistory()-->
<!--    borrowHistoryList.value = res.data-->
<!--  } catch (error) {-->
<!--    ElMessage.error('获取借阅历史失败')-->
<!--  }-->
<!--}-->

<!--// 获取我的预定-->
<!--const getMyReservation = async () => {-->
<!--  try {-->
<!--    const res = await apiGetMyReservations()-->
<!--    reservationList.value = res.data-->
<!--  } catch (error) {-->
<!--    ElMessage.error('获取预定列表失败')-->
<!--  }-->
<!--}-->

<!--// 获取我的罚款-->
<!--const getMyFines = async () => {-->
<!--  try {-->
<!--    const res = await apiGetMyFines()-->
<!--    fineList.value = res.data-->
<!--  } catch (error) {-->
<!--    ElMessage.error('获取罚款记录失败')-->
<!--  }-->
<!--}-->

<!--// 归还图书-->
<!--const handleReturn = async (borrowId) => {-->
<!--  try {-->
<!--    returnLoading.value = borrowId-->
<!--    await ElMessageBox.confirm(-->
<!--        '确认归还这本书吗？',-->
<!--        '归还确认',-->
<!--        {-->
<!--          confirmButtonText: '确认归还',-->
<!--          cancelButtonText: '取消',-->
<!--          type: 'warning'-->
<!--        }-->
<!--    )-->

<!--    await apiReturnBook(borrowId)-->
<!--    ElMessage.success('归还成功')-->
<!--    await getMyBorrowList()-->
<!--  } catch (error) {-->
<!--    if (error !== 'cancel') {-->
<!--      const errorMsg = error.response?.data?.msg || '归还失败'-->
<!--      ElMessage.error(errorMsg)-->
<!--    }-->
<!--  } finally {-->
<!--    returnLoading.value = ''-->
<!--  }-->
<!--}-->

<!--// 续借图书-->
<!--const handleRenew = async (bookId) => {-->
<!--  try {-->
<!--    await ElMessageBox.confirm(-->
<!--        '确认续借这本书吗？',-->
<!--        '续借确认',-->
<!--        {-->
<!--          confirmButtonText: '确认续借',-->
<!--          cancelButtonText: '取消',-->
<!--          type: 'warning'-->
<!--        }-->
<!--    )-->

<!--    await apiRenewBook(bookId)-->
<!--    ElMessage.success('续借申请提交成功')-->
<!--    await getMyBorrowList()-->
<!--  } catch (error) {-->
<!--    if (error !== 'cancel') {-->
<!--      const errorMsg = error.response?.data?.msg || '续借失败'-->
<!--      ElMessage.error(errorMsg)-->
<!--    }-->
<!--  }-->
<!--}-->

<!--// 取消预定-->
<!--const handleCancelReservation = async (reservationId) => {-->
<!--  try {-->
<!--    await ElMessageBox.confirm(-->
<!--        '确认取消这个预定吗？',-->
<!--        '取消预定确认',-->
<!--        {-->
<!--          confirmButtonText: '确认取消',-->
<!--          cancelButtonText: '取消',-->
<!--          type: 'warning'-->
<!--        }-->
<!--    )-->

<!--    await apiCancelReservation(reservationId)-->
<!--    ElMessage.success('取消预定成功')-->
<!--    await getMyReservation()-->
<!--  } catch (error) {-->
<!--    if (error !== 'cancel') {-->
<!--      ElMessage.error('取消预定失败')-->
<!--    }-->
<!--  }-->
<!--}-->

<!--// 支付罚款-->
<!--const handlePayFine = async (fineId) => {-->
<!--  try {-->
<!--    await ElMessageBox.confirm(-->
<!--        '确认支付这笔罚款吗？',-->
<!--        '支付确认',-->
<!--        {-->
<!--          confirmButtonText: '确认支付',-->
<!--          cancelButtonText: '取消',-->
<!--          type: 'warning'-->
<!--        }-->
<!--    )-->

<!--    await apiPayFine(fineId)-->
<!--    ElMessage.success('罚款支付成功')-->
<!--    await getMyFines()-->
<!--  } catch (error) {-->
<!--    if (error !== 'cancel') {-->
<!--      const errorMsg = error.response?.data?.msg || '支付失败'-->
<!--      ElMessage.error(errorMsg)-->
<!--    }-->
<!--  }-->
<!--}-->

<!--  const getMyNotifications = async () => {-->
<!--    notificationsLoading.value = true-->
<!--    try {-->
<!--      const res = await apiGetMyNotifications({-->
<!--        page: notificationPage.value - 1,-->
<!--        size: notificationPageSize.value-->
<!--      })-->
<!--      if (res.code === 200) {-->
<!--        notificationsList.value = res.data.content || []-->
<!--        notificationTotal.value = res.data.totalElements || 0-->
<!--      }-->
<!--    } catch (error) {-->
<!--      console.error('获取通知列表失败:', error)-->
<!--      ElMessage.error('获取通知列表失败')-->
<!--    } finally {-->
<!--      notificationsLoading.value = false-->
<!--    }-->
<!--  }-->

<!--  const getUnreadCount = async () => {-->
<!--    try {-->
<!--      const res = await apiGetUnreadCount()-->
<!--      if (res.code === 200) {-->
<!--        unreadCount.value = res.data-->
<!--        notificationUnreadCount.value = res.data-->
<!--      }-->
<!--    } catch (error) {-->
<!--      console.error('获取未读数量失败:', error)-->
<!--    }-->
<!--  }-->

<!--  const markAsRead = async (item) => {-->
<!--    if (item.read) return-->

<!--    try {-->
<!--      const res = await apiMarkAsRead(item.id)-->
<!--      if (res.code === 200) {-->
<!--        item.read = true-->
<!--        unreadCount.value = Math.max(0, unreadCount.value - 1)-->
<!--        notificationUnreadCount.value = Math.max(0, notificationUnreadCount.value - 1)-->
<!--        ElMessage.success('标记为已读')-->
<!--      }-->
<!--    } catch (error) {-->
<!--      console.error('标记已读失败:', error)-->
<!--      ElMessage.error('标记已读失败')-->
<!--    }-->
<!--  }-->

<!--  const markAllAsRead = async () => {-->
<!--    markAllLoading.value = true-->
<!--    try {-->
<!--      const res = await apiMarkAllAsRead()-->
<!--      if (res.code === 200) {-->
<!--        notificationsList.value.forEach(item => item.read = true)-->
<!--        unreadCount.value = 0-->
<!--        notificationUnreadCount.value = 0-->
<!--        ElMessage.success('全部标记为已读')-->
<!--      }-->
<!--    } catch (error) {-->
<!--      console.error('全部标记已读失败:', error)-->
<!--      ElMessage.error('全部标记已读失败')-->
<!--    } finally {-->
<!--      markAllLoading.value = false-->
<!--    }-->
<!--  }-->

<!--  // 分页处理-->
<!--  const handleNotificationSizeChange = (val) => {-->
<!--    notificationPageSize.value = val-->
<!--    notificationPage.value = 1-->
<!--    getMyNotifications()-->
<!--  }-->

<!--  const handleNotificationPageChange = (val) => {-->
<!--    notificationPage.value = val-->
<!--    getMyNotifications()-->
<!--  }-->

<!--  // 辅助函数-->
<!--  const getNotificationTypeText = (type) => {-->
<!--    const typeMap = {-->
<!--      'RESERVATION_AVAILABLE': '预定可借',-->
<!--      'RESERVATION_REMINDER': '预定提醒',-->
<!--      'OVERDUE_REMINDER': '逾期提醒',-->
<!--      'SYSTEM_NOTIFICATION': '系统通知'-->
<!--    }-->
<!--    return typeMap[type] || type-->
<!--  }-->

<!--  const formatNotificationTime = (time) => {-->
<!--    const date = new Date(time)-->
<!--    return date.toLocaleString('zh-CN', {-->
<!--      year: 'numeric',-->
<!--      month: '2-digit',-->
<!--      day: '2-digit',-->
<!--      hour: '2-digit',-->
<!--      minute: '2-digit'-->
<!--    })-->
<!--  }-->

<!--  // 跳转到通知管理页面（如果需要独立页面）-->
<!--  const goToNotificationPage = () => {-->
<!--    router.push('/home/notification-management')-->
<!--  }-->

<!--  // 监听标签页切换，当切换到通知标签页时加载通知数据-->
<!--  watch(activeTab, (newTab) => {-->
<!--    if (newTab === 'notifications') {-->
<!--      getMyNotifications()-->
<!--      getUnreadCount()-->
<!--    }-->
<!--  })-->

<!--  // 定时刷新未读数量（每分钟）-->
<!--  onMounted(() => {-->
<!--    getUnreadCount()-->
<!--    setInterval(() => {-->
<!--      getUnreadCount()-->
<!--    }, 60000) // 60秒-->
<!--  })-->
<!--</script>-->

<!--&lt;!&ndash;<style scoped>&ndash;&gt;-->
<!--&lt;!&ndash;.my-space-page {&ndash;&gt;-->
<!--&lt;!&ndash;  padding: 20px;&ndash;&gt;-->
<!--&lt;!&ndash;  background-color: #f5f7fa;&ndash;&gt;-->
<!--&lt;!&ndash;  min-height: calc(100vh - 64px);&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;.table-container {&ndash;&gt;-->
<!--&lt;!&ndash;  margin-top: 16px;&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;.action-buttons {&ndash;&gt;-->
<!--&lt;!&ndash;  display: flex;&ndash;&gt;-->
<!--&lt;!&ndash;  gap: 8px;&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;.text-red {&ndash;&gt;-->
<!--&lt;!&ndash;  color: #f56c6c;&ndash;&gt;-->
<!--&lt;!&ndash;  font-weight: 500;&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->

<!--&lt;!&ndash;@media (max-width: 768px) {&ndash;&gt;-->
<!--&lt;!&ndash;  .my-space-page {&ndash;&gt;-->
<!--&lt;!&ndash;    padding: 10px;&ndash;&gt;-->
<!--&lt;!&ndash;  }&ndash;&gt;-->

<!--&lt;!&ndash;  .action-buttons {&ndash;&gt;-->
<!--&lt;!&ndash;    flex-direction: column;&ndash;&gt;-->
<!--&lt;!&ndash;    gap: 4px;&ndash;&gt;-->
<!--&lt;!&ndash;  }&ndash;&gt;-->
<!--&lt;!&ndash;}&ndash;&gt;-->
<!--&lt;!&ndash;</style>&ndash;&gt;-->

<!--<style scoped>-->
<!--.my-space-page {-->
<!--  padding: 20px;-->
<!--  background-color: #f5f7fa;-->
<!--  min-height: calc(100vh - 64px);-->
<!--}-->

<!--/* 通知头部样式 */-->
<!--.notification-header {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  padding: 0 0 15px 0;-->
<!--  border-bottom: 1px solid #e4e7ed;-->
<!--  margin-bottom: 15px;-->
<!--}-->

<!--.notification-header .title {-->
<!--  font-size: 18px;-->
<!--  font-weight: bold;-->
<!--  color: #303133;-->
<!--}-->

<!--.notification-bell {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  cursor: pointer;-->
<!--  padding: 8px 12px;-->
<!--  border-radius: 4px;-->
<!--  transition: all 0.3s;-->
<!--}-->

<!--.notification-bell:hover {-->
<!--  background-color: #f5f5f5;-->
<!--}-->

<!--.bell-text {-->
<!--  margin-left: 6px;-->
<!--  font-size: 14px;-->
<!--}-->

<!--/* 通知标签页样式 */-->
<!--.notifications-container {-->
<!--  min-height: 400px;-->
<!--}-->

<!--.notifications-header {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  margin-bottom: 20px;-->
<!--  padding: 10px 0;-->
<!--}-->

<!--.stats {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 15px;-->
<!--}-->

<!--.unread-count {-->
<!--  font-size: 14px;-->
<!--  color: #f56c6c;-->
<!--  font-weight: 500;-->
<!--}-->

<!--/* 通知列表样式 */-->
<!--.notifications-list {-->
<!--  max-height: 500px;-->
<!--  overflow-y: auto;-->
<!--}-->

<!--.notification-item {-->
<!--  padding: 15px;-->
<!--  margin-bottom: 10px;-->
<!--  border: 1px solid #e4e7ed;-->
<!--  border-radius: 6px;-->
<!--  cursor: pointer;-->
<!--  transition: all 0.3s;-->
<!--  background-color: white;-->
<!--}-->

<!--.notification-item:hover {-->
<!--  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);-->
<!--  transform: translateY(-2px);-->
<!--}-->

<!--.notification-item.unread {-->
<!--  background-color: #f0f9ff;-->
<!--  border-left: 4px solid #409eff;-->
<!--}-->

<!--.notification-item.important {-->
<!--  border-color: #e6a23c;-->
<!--}-->

<!--.notification-header {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  margin-bottom: 8px;-->
<!--}-->

<!--.title-section {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 8px;-->
<!--  flex-wrap: wrap;-->
<!--}-->

<!--.type-tag {-->
<!--  padding: 2px 6px;-->
<!--  background-color: #f2f6fc;-->
<!--  border-radius: 3px;-->
<!--  font-size: 12px;-->
<!--  color: #909399;-->
<!--}-->

<!--.title {-->
<!--  font-weight: bold;-->
<!--  color: #303133;-->
<!--}-->

<!--.time {-->
<!--  font-size: 12px;-->
<!--  color: #909399;-->
<!--  white-space: nowrap;-->
<!--}-->

<!--.notification-body {-->
<!--  color: #606266;-->
<!--  font-size: 14px;-->
<!--  line-height: 1.6;-->
<!--  margin-top: 8px;-->
<!--}-->

<!--.empty-notifications {-->
<!--  text-align: center;-->
<!--  padding: 50px 0;-->
<!--}-->

<!--.pagination {-->
<!--  margin-top: 20px;-->
<!--  display: flex;-->
<!--  justify-content: center;-->
<!--}-->

<!--/* 原有样式保持不变 */-->
<!--.table-container {-->
<!--  margin-top: 16px;-->
<!--}-->

<!--.action-buttons {-->
<!--  display: flex;-->
<!--  gap: 8px;-->
<!--}-->

<!--.text-red {-->
<!--  color: #f56c6c;-->
<!--  font-weight: 500;-->
<!--}-->

<!--@media (max-width: 768px) {-->
<!--  .my-space-page {-->
<!--    padding: 10px;-->
<!--  }-->

<!--  .action-buttons {-->
<!--    flex-direction: column;-->
<!--    gap: 4px;-->
<!--  }-->

<!--  .notification-header {-->
<!--    flex-direction: column;-->
<!--    align-items: flex-start;-->
<!--    gap: 10px;-->
<!--  }-->

<!--  .title-section {-->
<!--    flex-direction: column;-->
<!--    align-items: flex-start;-->
<!--    gap: 5px;-->
<!--  }-->

<!--  .notification-header {-->
<!--    flex-direction: column;-->
<!--    align-items: flex-start;-->
<!--    gap: 8px;-->
<!--  }-->
<!--}-->
<!--</style>-->