<template>
  <div class="my-space-page">
    <el-card shadow="hover">
      <el-tabs v-model="activeTab" type="card">
        <!-- 我的借阅 -->
        <el-tab-pane label="当前借阅" name="borrowing">
          <el-table :data="borrowList" border stripe v-loading="loading">
            <el-table-column prop="bookName" label="图书名称"></el-table-column>
            <el-table-column prop="borrowTime" label="借阅时间"></el-table-column>
            <el-table-column prop="dueTime" label="应还时间">
              <template #default="scope">
                <span :class="scope.row.overdueDays > 0 ? 'text-red' : ''">
                  {{ scope.row.dueTime }}
                  <span v-if="scope.row.overdueDays > 0">(逾期{{ scope.row.overdueDays }}天)</span>
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="branchName" label="借阅分馆"></el-table-column>
            <el-table-column label="操作">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleReturn(scope.row.id)">归还</el-button>
                <el-button
                    type="warning"
                    size="small"
                    @click="handleReservation(scope.row.bookId)"
                    v-if="scope.row.overdueDays <= 0"
                >
                  续借
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 借阅历史 -->
        <el-tab-pane label="借阅历史" name="borrowHistory">
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
        </el-tab-pane>

        <!-- 我的预定 -->
        <el-tab-pane label="我的预定" name="reservation">
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
                    @click="cancelReservation(scope.row.id)"
                    v-if="scope.row.status === 'PENDING'"
                >
                  取消预定
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 罚款记录 -->
        <el-tab-pane label="罚款记录" name="fines">
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
                    @click="payFine(scope.row.fineId)"
                    v-if="scope.row.payStatus === 'unpaid'"
                >
                  立即支付
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/utils/request'

const activeTab = ref('borrowing')
const loading = ref(false)
const borrowList = ref([])
const borrowHistoryList = ref([])
const reservationList = ref([])
const fineList = ref([])

onMounted(() => {
  getMyBorrowList()
  getMyBorrowHistory()
  getMyReservation()
  getMyFines()
})

// 获取当前借阅列表
const getMyBorrowList = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/borrow/my-borrow')
    borrowList.value = res.data
  } catch (error) {
    ElMessage.error('获取借阅列表失败')
  } finally {
    loading.value = false
  }
}

// 获取借阅历史
const getMyBorrowHistory = async () => {
  try {
    const res = await axios.get('/api/borrow/my-history')
    borrowHistoryList.value = res.data
  } catch (error) {
    ElMessage.error('获取借阅历史失败')
  }
}

// 获取我的预定
const getMyReservation = async () => {
  try {
    const res = await axios.get('/api/reservation/my-reservation')
    reservationList.value = res.data
  } catch (error) {
    ElMessage.error('获取预定列表失败')
  }
}

// 获取我的罚款
const getMyFines = async () => {
  try {
    const res = await axios.get('/api/fines/my-fines')
    fineList.value = res.data
  } catch (error) {
    ElMessage.error('获取罚款记录失败')
  }
}

// 归还图书
const handleReturn = async (borrowId) => {
  try {
    await axios.post(`/api/borrow/return/${borrowId}`)
    ElMessage.success('归还成功')
    getMyBorrowList()
  } catch (error) {
    ElMessage.error('归还失败：' + error.response.data.msg)
  }
}

// 续借图书
const handleReservation = async (bookId) => {
  try {
    await axios.post(`/api/borrow/renew/${bookId}`)
    ElMessage.success('续借申请提交成功')
    getMyBorrowList()
  } catch (error) {
    ElMessage.error('续借失败：' + error.response.data.msg)
  }
}

// 取消预定
const cancelReservation = async (reservationId) => {
  try {
    await axios.delete(`/api/reservation/cancel/${reservationId}`)
    ElMessage.success('取消预定成功')
    getMyReservation()
  } catch (error) {
    ElMessage.error('取消预定失败')
  }
}

// 支付罚款
const payFine = async (fineId) => {
  try {
    await axios.post(`/api/fines/pay/${fineId}`)
    ElMessage.success('罚款支付成功')
    getMyFines()
  } catch (error) {
    ElMessage.error('支付失败：' + error.response.data.msg)
  }
}
</script>

<style scoped>
.my-space-page {
  padding: 20px;
}
.text-red {
  color: #f56c6c;
}
</style>