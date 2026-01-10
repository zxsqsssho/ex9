<template>
  <div class="my-space-page">
    <el-card shadow="hover">
      <el-tabs v-model="activeTab" type="card">
        <!-- 我的借阅 -->
        <el-tab-pane label="当前借阅" name="borrowing">
          <div class="table-container">
            <el-table :data="borrowList" border stripe v-loading="loading">
              <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>
              <el-table-column prop="borrowTime" label="借阅时间" min-width="180"></el-table-column>
              <el-table-column prop="dueTime" label="应还时间" min-width="180">
                <template #default="scope">
                  <span :class="scope.row.overdueDays > 0 ? 'text-red' : ''">
                    {{ scope.row.dueTime }}
                    <span v-if="scope.row.overdueDays > 0">(逾期{{ scope.row.overdueDays }}天)</span>
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="branchName" label="借阅分馆" min-width="120"></el-table-column>
              <el-table-column label="操作" fixed="right" min-width="180">
                <template #default="scope">
                  <div class="action-buttons">
                    <el-button
                        type="primary"
                        size="small"
                        @click="handleReturn(scope.row.id)"
                        :loading="returnLoading === scope.row.id"
                    >
                      归还
                    </el-button>
                    <el-button
                        type="warning"
                        size="small"
                        @click="handleRenew(scope.row.bookId)"
                        v-if="scope.row.overdueDays <= 0"
                    >
                      续借
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 借阅历史 -->
        <el-tab-pane label="借阅历史" name="borrowHistory">
          <div class="table-container">
            <el-table :data="borrowHistoryList" border stripe v-loading="loading">
              <el-table-column prop="bookName" label="图书名称"></el-table-column>
              <el-table-column prop="borrowTime" label="借阅时间"></el-table-column>
              <el-table-column prop="returnTime" label="归还时间"></el-table-column>
              <el-table-column prop="status" label="借阅状态">
                <template #default="scope">
                  {{ scope.row.status === 'RETURNED' ? '已归还' : '已逾期归还' }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 我的预定 -->
        <el-tab-pane label="我的预定" name="reservation">
          <div class="table-container">
            <el-table :data="reservationList" border stripe v-loading="loading">
              <el-table-column prop="bookName" label="图书名称"></el-table-column>
              <el-table-column prop="reserveTime" label="预定时间"></el-table-column>
              <el-table-column prop="expiryTime" label="预定有效期"></el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag v-if="scope.row.status === 'PENDING'">待处理</el-tag>
                  <el-tag type="success" v-if="scope.row.status === 'READY'">可借阅</el-tag>
                  <el-tag type="danger" v-if="scope.row.status === 'CANCELLED'">已取消</el-tag>
                  <el-tag type="info" v-if="scope.row.status === 'COMPLETED'">已完成</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button
                      type="danger"
                      size="small"
                      @click="handleCancelReservation(scope.row.id)"
                      v-if="scope.row.status === 'PENDING'"
                  >
                    取消预定
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 罚款记录 -->
        <el-tab-pane label="罚款记录" name="fines">
          <div class="table-container">
            <el-table :data="fineList" border stripe v-loading="loading">
              <el-table-column prop="bookName" label="图书名称"></el-table-column>
              <el-table-column prop="fineAmount" label="罚款金额(元)"></el-table-column>
              <el-table-column prop="overdueDays" label="逾期天数"></el-table-column>
              <el-table-column prop="payStatus" label="支付状态">
                <template #default="scope">
                  <el-tag type="danger" v-if="scope.row.payStatus === 'unpaid'">未支付</el-tag>
                  <el-tag type="success" v-if="scope.row.payStatus === 'paid'">已支付</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button
                      type="primary"
                      size="small"
                      @click="handlePayFine(scope.row.fineId)"
                      v-if="scope.row.payStatus === 'unpaid'"
                  >
                    立即支付
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router' // 引入路由
// 导入API函数 - 注意重命名避免冲突
import {
  getMyBorrowList as apiGetMyBorrowList,
  getMyBorrowHistory as apiGetMyBorrowHistory,
  returnBook as apiReturnBook,
  renewBook as apiRenewBook
} from '@/api/borrow'

import {
  getMyReservations as apiGetMyReservations,
  cancelReservation as apiCancelReservation
} from '@/api/reservation'

import {
  getMyFines as apiGetMyFines,
  payFine as apiPayFine
} from '@/api/fines'

const activeTab = ref('borrowing')
const loading = ref(false)
const returnLoading = ref('')

// 数据列表
const borrowList = ref([])
const borrowHistoryList = ref([])
const reservationList = ref([])
const fineList = ref([])

onMounted(() => {
  loadAllData()
})

// 加载所有数据
const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      getMyBorrowList(),
      getMyBorrowHistory(),
      getMyReservation(),
      getMyFines()
    ])
  } catch (error) {
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

// 获取当前借阅列表
const getMyBorrowList = async () => {
  try {
    const res = await apiGetMyBorrowList()
    borrowList.value = res.data
  } catch (error) {
    ElMessage.error('获取借阅列表失败')
  }
}

// 获取借阅历史
const getMyBorrowHistory = async () => {
  try {
    const res = await apiGetMyBorrowHistory()
    borrowHistoryList.value = res.data
  } catch (error) {
    ElMessage.error('获取借阅历史失败')
  }
}

// 获取我的预定
const getMyReservation = async () => {
  try {
    const res = await apiGetMyReservations()
    reservationList.value = res.data
  } catch (error) {
    ElMessage.error('获取预定列表失败')
  }
}

// 获取我的罚款
const getMyFines = async () => {
  try {
    const res = await apiGetMyFines()
    fineList.value = res.data
  } catch (error) {
    ElMessage.error('获取罚款记录失败')
  }
}

// 归还图书
const handleReturn = async (borrowId) => {
  try {
    returnLoading.value = borrowId
    await ElMessageBox.confirm(
        '确认归还这本书吗？',
        '归还确认',
        {
          confirmButtonText: '确认归还',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await apiReturnBook(borrowId)
    ElMessage.success('归还成功')
    await getMyBorrowList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '归还失败'
      ElMessage.error(errorMsg)
    }
  } finally {
    returnLoading.value = ''
  }
}

// 续借图书
const handleRenew = async (bookId) => {
  try {
    await ElMessageBox.confirm(
        '确认续借这本书吗？',
        '续借确认',
        {
          confirmButtonText: '确认续借',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await apiRenewBook(bookId)
    ElMessage.success('续借申请提交成功')
    await getMyBorrowList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '续借失败'
      ElMessage.error(errorMsg)
    }
  }
}

// 取消预定
const handleCancelReservation = async (reservationId) => {
  try {
    await ElMessageBox.confirm(
        '确认取消这个预定吗？',
        '取消预定确认',
        {
          confirmButtonText: '确认取消',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await apiCancelReservation(reservationId)
    ElMessage.success('取消预定成功')
    await getMyReservation()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消预定失败')
    }
  }
}

// 支付罚款
const handlePayFine = async (fineId) => {
  try {
    await ElMessageBox.confirm(
        '确认支付这笔罚款吗？',
        '支付确认',
        {
          confirmButtonText: '确认支付',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await apiPayFine(fineId)
    ElMessage.success('罚款支付成功')
    await getMyFines()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '支付失败'
      ElMessage.error(errorMsg)
    }
  }
}
</script>

<style scoped>
.my-space-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.table-container {
  margin-top: 16px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.text-red {
  color: #f56c6c;
  font-weight: 500;
}

@media (max-width: 768px) {
  .my-space-page {
    padding: 10px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>