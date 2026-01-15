<template>
  <div class="my-reservations-page">
    <el-card shadow="hover">
      <div class="card-header">
        <h2>我的预定记录</h2>
        <el-button type="primary" @click="refreshReservations">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
      <!-- 预定筛选（保持不变，状态参数已绑定） -->
      <el-form :model="queryForm" inline class="query-form" @submit.prevent="getMyReservations">
        <el-form-item label="预定状态">
          <el-select v-model="queryForm.status" placeholder="全部状态">
            <el-option label="全部" value=""></el-option>
            <el-option label="等待中" value="PENDING"></el-option>
            <el-option label="可借阅" value="READY"></el-option>
            <el-option label="已取消" value="CANCELLED"></el-option>
            <el-option label="已完成" value="COMPLETED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getMyReservations">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 预定列表（保持不变） -->
      <div class="table-container">
        <el-table :data="reservationList.content || []" border stripe v-loading="loading">
          <el-table-column prop="id" label="预定ID" width="80"></el-table-column>
          <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>
          <el-table-column prop="author" label="作者" width="120"></el-table-column>
          <el-table-column prop="reserveTime" label="预定时间" min-width="180"></el-table-column>
          <el-table-column prop="expiryTime" label="预定有效期" min-width="180">
            <template #default="scope">
              <span :class="isExpired(scope.row.expiryTime) ? 'text-red' : ''">
                {{ scope.row.expiryTime }}
                <span v-if="isExpired(scope.row.expiryTime)">(已过期)</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="branchName" label="预定分馆" width="120"></el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 'PENDING'">等待中</el-tag>
              <el-tag type="success" v-if="scope.row.status === 'READY'">可借阅</el-tag>
              <el-tag type="danger" v-if="scope.row.status === 'CANCELLED'">已取消</el-tag>
              <el-tag type="info" v-if="scope.row.status === 'COMPLETED'">已完成</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="180">
            <template #default="scope">
              <div class="action-buttons">
                <el-button
                    type="danger"
                    size="small"
                    @click="handleCancelReservation(scope.row.id)"
                    v-if="scope.row.status === 'PENDING' || scope.row.status === 'READY'"
                    :loading="cancelLoading === scope.row.id"
                >
                  取消预定
                </el-button>
                <el-button
                    type="primary"
                    size="small"
                    @click="handleGoBorrow(scope.row)"
                    v-if="scope.row.status === 'READY'"
                >
                  去借阅
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页（保持不变） -->
        <el-pagination
            v-if="reservationList.total > 0"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="queryForm.pageNum"
            :page-sizes="[10, 20, 50]"
            :page-size="queryForm.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="reservationList.total"
            style="margin-top: 16px; text-align: right"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Refresh} from '@element-plus/icons-vue'
import {useRouter} from 'vue-router'
import {
  getMyReservations as apiGetMyReservations,
  cancelReservation as apiCancelReservation
} from '@/api/reservation'

const router = useRouter()
const loading = ref(false)
const cancelLoading = ref('')
const reservationList = ref({content: [], total: 0})

// 查询条件（保持不变）
const queryForm = ref({
  status: '', // 空字符串表示全部，非空传递枚举值
  pageNum: 1,
  pageSize: 10
})

onMounted(() => {
  getMyReservations()
})

// 核心修复：传递 status 参数到API
const getMyReservations = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryForm.value.pageNum,
      pageSize: queryForm.value.pageSize,
      status: queryForm.value.status || undefined // 空值转为undefined，不传递无效参数
    }
    const res = await apiGetMyReservations(params)
    reservationList.value = res.data
  } catch (error) {
    ElMessage.error('获取预定记录失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 其他方法保持不变...
const refreshReservations = async () => {
  queryForm.value.pageNum = 1
  await getMyReservations()
  ElMessage.success('预定记录已刷新')
}

const resetQuery = () => {
  queryForm.value = {
    status: '',
    pageNum: 1,
    pageSize: 10
  }
  getMyReservations()
}

const isExpired = (expiryTime) => {
  if (!expiryTime) return false
  return new Date(expiryTime) < new Date()
}

const handleCancelReservation = async (reservationId) => {
  try {
    cancelLoading.value = reservationId
    await ElMessageBox.confirm(
        '确认取消这个预定吗？取消后不可恢复',
        '取消预定确认',
        {
          confirmButtonText: '确认取消',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    await apiCancelReservation(reservationId)
    ElMessage.success('取消预定成功')
    await getMyReservations()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || '取消预定失败'
      ElMessage.error(errorMsg)
    }
  } finally {
    cancelLoading.value = ''
  }
}

const handleGoBorrow = (reservation) => {
  router.push({
    name: 'BookDetail',
    params: {id: reservation.bookId}
  })
}

const handleSizeChange = (val) => {
  queryForm.value.pageSize = val
  getMyReservations()
}

const handleCurrentChange = (val) => {
  queryForm.value.pageNum = val
  getMyReservations()
}
</script>

<style scoped>
.my-reservations-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.query-form {
  margin-bottom: 16px;
}
.table-container {
  margin-top: 8px;
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
  .my-reservations-page {
    padding: 10px;
  }
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>