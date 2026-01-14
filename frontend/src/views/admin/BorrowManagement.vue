<!--frontend/src/views/admin/BorrowManagement.vue-->
<template>
  <div class="borrow-management">
    <el-card>
      <div class="card-header">
        <h2>借阅管理</h2>
      </div>
      <!-- 筛选条件 -->
      <el-form :model="queryForm" inline class="query-form" @submit.prevent="getBorrowList">
        <el-form-item label="用户名称">
          <el-input v-model="queryForm.userName" placeholder="请输入用户名称"></el-input>
        </el-form-item>
        <el-form-item label="图书名称">
          <el-input v-model="queryForm.bookName" placeholder="请输入图书名称"></el-input>
        </el-form-item>
        <el-form-item label="所属分馆">
          <el-select v-model="queryForm.branchId" placeholder="请选择分馆">
            <el-option v-for="branch in branchList" :key="branch.branchId" :label="branch.branchName" :value="branch.branchId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="借阅状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态">
            <el-option label="借阅中" value="BORROWED"></el-option>
            <el-option label="已归还" value="RETURNED"></el-option>
            <el-option label="已逾期" value="OVERDUE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getBorrowList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 借阅记录列表 -->
      <el-table :data="borrowList" border stripe v-loading="loading">
        <el-table-column prop="id" label="记录ID" width="80"></el-table-column>
        <el-table-column prop="userRealName" label="用户名称" width="120"></el-table-column>
        <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>
        <el-table-column prop="branchName" label="所属分馆" width="120">
          <template #default="scope">
            {{ getBranchName(scope.row.branchId) }}
          </template>
        </el-table-column>
        <el-table-column prop="borrowTime" label="借阅时间" width="180"></el-table-column>
        <el-table-column prop="dueTime" label="应还时间" width="180">
          <template #default="scope">
            <span :class="scope.row.status === 'OVERDUE' ? 'text-red' : ''">
              {{ scope.row.dueTime }}
              <span v-if="scope.row.overdueDays > 0">(逾期{{ scope.row.overdueDays }}天)</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="returnTime" label="归还时间" width="180"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleReturn(scope.row.id)" v-if="scope.row.status === 'BORROWED' || scope.row.status === 'OVERDUE'">
              确认归还
            </el-button>
            <el-button type="text" color="blue" size="small" @click="viewFine(scope.row.id)" v-if="scope.row.overdueDays > 0">
              查看罚款
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <el-pagination
          v-if="total > 0"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="queryForm.pageNum"
          :page-sizes="[10, 20, 50]"
          :page-size="queryForm.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          style="margin-top: 20px; text-align: right"
      ></el-pagination>
    </el-card>

    <!-- 预定管理 -->
    <el-card style="margin-top: 20px;">
      <div class="card-header">
        <h2>预定管理</h2>
      </div>
      <!-- 预定筛选条件 -->
      <el-form :model="reserveQueryForm" inline class="query-form" @submit.prevent="getReservationList">
        <el-form-item label="用户名称">
          <el-input v-model="reserveQueryForm.userName" placeholder="请输入用户名称"></el-input>
        </el-form-item>
        <el-form-item label="图书名称">
          <el-input v-model="reserveQueryForm.bookName" placeholder="请输入图书名称"></el-input>
        </el-form-item>
        <el-form-item label="所属分馆">
          <el-select v-model="reserveQueryForm.branchId" placeholder="请选择分馆">
            <el-option v-for="branch in branchList" :key="branch.branchId" :label="branch.branchName" :value="branch.branchId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="预定状态">
          <el-select v-model="reserveQueryForm.status" placeholder="请选择状态">
            <el-option label="等待中" value="PENDING"></el-option>
            <el-option label="可借阅" value="READY"></el-option>
            <el-option label="已取消" value="CANCELLED"></el-option>
            <el-option label="已完成" value="COMPLETED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getReservationList">查询</el-button>
          <el-button @click="resetReserveQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 预定列表 -->
      <el-table :data="reservationList" border stripe v-loading="reserveLoading">
        <el-table-column prop="id" label="预定ID" width="80"></el-table-column>
        <el-table-column prop="userRealName" label="用户名称" width="120"></el-table-column>
        <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>
        <el-table-column prop="branchName" label="所属分馆" width="120">
          <template #default="scope">
            {{ getBranchName(scope.row.branchId) }}
          </template>
        </el-table-column>
        <el-table-column prop="reserveTime" label="预定时间" width="180"></el-table-column>
        <el-table-column prop="expiryTime" label="预定有效期" width="180"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getReserveStatusTagType(scope.row.status)">
              {{ getReserveStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button type="primary" size="small" @click="completeReservation(scope.row.id)" v-if="scope.row.status === 'READY'">
              标记完成
            </el-button>
            <el-button type="danger" size="small" @click="cancelReservation(scope.row.id)" v-if="scope.row.status === 'PENDING' || scope.row.status === 'READY'">
              取消预定
            </el-button>
            <el-button type="text" color="blue" size="small" @click="viewReservationQueue(scope.row.bookId)">
              查看队列
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 预定分页 -->
      <el-pagination
          v-if="reserveTotal > 0"
          @size-change="handleReserveSizeChange"
          @current-change="handleReserveCurrentChange"
          :current-page="reserveQueryForm.pageNum"
          :page-sizes="[10, 20, 50]"
          :page-size="reserveQueryForm.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="reserveTotal"
          style="margin-top: 20px; text-align: right"
      ></el-pagination>
    </el-card>

    <!-- 罚款管理 -->
    <el-card style="margin-top: 20px;">
      <div class="card-header">
        <h2>罚款管理</h2>
      </div>
      <!-- 罚款筛选条件 -->
      <el-form :model="fineQueryForm" inline class="query-form" @submit.prevent="getFineList">
        <el-form-item label="用户名称">
          <el-input v-model="fineQueryForm.userName" placeholder="请输入用户名称"></el-input>
        </el-form-item>
        <el-form-item label="图书名称">
          <el-input v-model="fineQueryForm.bookName" placeholder="请输入图书名称"></el-input>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="fineQueryForm.payStatus" placeholder="请选择支付状态">
            <el-option label="未支付" value="unpaid"></el-option>
            <el-option label="已支付" value="paid"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getFineList">查询</el-button>
          <el-button @click="resetFineQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 罚款列表 -->
      <el-table :data="fineList" border stripe v-loading="fineLoading">
        <el-table-column prop="fineId" label="罚款ID" width="80"></el-table-column>
        <el-table-column prop="userRealName" label="用户名称" width="120"></el-table-column>
        <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>
        <el-table-column prop="overdueDays" label="逾期天数" width="100"></el-table-column>
        <el-table-column prop="fineAmount" label="罚款金额(元)" width="120">
          <template #default="scope">
            {{ scope.row.fineAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="payStatus" label="支付状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.payStatus === 'paid' ? 'success' : 'danger'">
              {{ scope.row.payStatus === 'paid' ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="180"></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="success" size="small" @click="updateFineStatus(scope.row.fineId, 'paid')" v-if="scope.row.payStatus === 'unpaid'">
              标记已支付
            </el-button>
            <el-button type="warning" size="small" @click="updateFineStatus(scope.row.fineId, 'unpaid')" v-if="scope.row.payStatus === 'paid'">
              标记未支付
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 罚款分页 -->
      <el-pagination
          v-if="fineTotal > 0"
          @size-change="handleFineSizeChange"
          @current-change="handleFineCurrentChange"
          :current-page="fineQueryForm.pageNum"
          :page-sizes="[10, 20, 50]"
          :page-size="fineQueryForm.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="fineTotal"
          style="margin-top: 20px; text-align: right"
      ></el-pagination>
    </el-card>

    <!-- 预定队列弹窗 -->
    <el-dialog v-model="queueDialogVisible" title="图书预定队列" width="600px">
      <el-table :data="reservationQueueList" border stripe>
        <el-table-column prop="id" label="预定ID" width="80"></el-table-column>
        <el-table-column prop="userRealName" label="用户名称" width="120"></el-table-column>
        <el-table-column prop="reserveTime" label="预定时间" width="180"></el-table-column>
        <el-table-column prop="expiryTime" label="预定有效期" width="180"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getReserveStatusTagType(scope.row.status)">
              {{ getReserveStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="queueDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllBranches } from '@/api/branch'
import {
  getAllBorrowRecords,
  returnBook as apiReturnBook,
  updateBorrowStatus
} from '@/api/borrow'
import {
  getAllReservations,
  cancelReservation as apiCancelReservation,
  completeReservation as apiCompleteReservation,
  getBookReservationQueue
} from '@/api/reservation'
import {
  getAllFines,
  updateFineStatus as apiUpdateFineStatus
} from '@/api/fines'

// 公共数据
const branchList = ref([])
const loading = ref(false)
const reserveLoading = ref(false)
const fineLoading = ref(false)

// 借阅记录数据
const borrowList = ref([])
const total = ref(0)
const queryForm = reactive({
  userName: '',
  bookName: '',
  branchId: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

// 预定管理数据
const reservationList = ref([])
const reserveTotal = ref(0)
const reserveQueryForm = reactive({
  userName: '',
  bookName: '',
  branchId: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

// 罚款管理数据
const fineList = ref([])
const fineTotal = ref(0)
const fineQueryForm = reactive({
  userName: '',
  bookName: '',
  payStatus: '',
  pageNum: 1,
  pageSize: 10
})

// 预定队列弹窗
const queueDialogVisible = ref(false)
const reservationQueueList = ref([])

// 初始化
onMounted(() => {
  loadBranchList()
  getBorrowList()
  getReservationList()
  getFineList()
})

// 加载分馆列表
const loadBranchList = async () => {
  try {
    const res = await getAllBranches()
    branchList.value = res.data
  } catch (err) {
    ElMessage.error('获取分馆列表失败')
  }
}

// ========== 借阅记录管理 ==========
// 获取借阅记录列表
const getBorrowList = async () => {
  loading.value = true
  try {
    const res = await getAllBorrowRecords({
      ...queryForm,
      branchId: queryForm.branchId || undefined
    })
    borrowList.value = res.data.content
    total.value = res.data.totalElements
  } catch (err) {
    ElMessage.error('获取借阅记录失败')
  } finally {
    loading.value = false
  }
}

// 重置借阅筛选条件
const resetQuery = () => {
  queryForm.userName = ''
  queryForm.bookName = ''
  queryForm.branchId = ''
  queryForm.status = ''
  queryForm.pageNum = 1
  getBorrowList()
}

// 借阅分页大小改变
const handleSizeChange = (val) => {
  queryForm.pageSize = val
  getBorrowList()
}

// 借阅当前页改变
const handleCurrentChange = (val) => {
  queryForm.pageNum = val
  getBorrowList()
}

// 确认归还图书
const handleReturn = async (borrowId) => {
  try {
    await ElMessageBox.confirm(
        '确认该图书已归还吗？',
        '归还确认',
        {
          confirmButtonText: '确认归还',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    await apiReturnBook(borrowId)
    ElMessage.success('归还成功')
    getBorrowList()
    getFineList() // 刷新罚款列表
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('归还失败')
    }
  }
}

// 查看罚款详情
const viewFine = (borrowId) => {
  // 筛选当前借阅记录对应的罚款
  const fine = fineList.value.find(item => item.recordId === borrowId)
  if (fine) {
    ElMessageBox.alert(
        `<div>
        <p>罚款ID：${fine.fineId}</p>
        <p>图书名称：${fine.bookName}</p>
        <p>逾期天数：${fine.overdueDays}天</p>
        <p>罚款金额：${fine.fineAmount.toFixed(2)}元</p>
        <p>支付状态：${fine.payStatus === 'paid' ? '已支付' : '未支付'}</p>
        ${fine.payTime ? `<p>支付时间：${fine.payTime}</p>` : ''}
      </div>`,
        '罚款详情',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '关闭'
        }
    )
  } else {
    ElMessage.info('未查询到相关罚款记录')
  }
}

// 获取借阅状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'BORROWED':
      return 'primary'
    case 'RETURNED':
      return 'success'
    case 'OVERDUE':
      return 'danger'
    default:
      return 'default'
  }
}

// 获取借阅状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'BORROWED':
      return '借阅中'
    case 'RETURNED':
      return '已归还'
    case 'OVERDUE':
      return '已逾期'
    default:
      return status
  }
}

// ========== 预定管理 ==========
// 获取预定列表
const getReservationList = async () => {
  reserveLoading.value = true
  try {
    const res = await getAllReservations({
      ...reserveQueryForm,
      branchId: reserveQueryForm.branchId || undefined
    })
    reservationList.value = res.data.content
    reserveTotal.value = res.data.totalElements
  } catch (err) {
    ElMessage.error('获取预定记录失败')
  } finally {
    reserveLoading.value = false
  }
}

// 重置预定筛选条件
const resetReserveQuery = () => {
  reserveQueryForm.userName = ''
  reserveQueryForm.bookName = ''
  reserveQueryForm.branchId = ''
  reserveQueryForm.status = ''
  reserveQueryForm.pageNum = 1
  getReservationList()
}

// 预定分页大小改变
const handleReserveSizeChange = (val) => {
  reserveQueryForm.pageSize = val
  getReservationList()
}

// 预定当前页改变
const handleReserveCurrentChange = (val) => {
  reserveQueryForm.pageNum = val
  getReservationList()
}

// 取消预定
const cancelReservation = async (reservationId) => {
  try {
    await ElMessageBox.confirm(
        '确认取消该预定吗？',
        '取消预定确认',
        {
          confirmButtonText: '确认取消',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    await apiCancelReservation(reservationId)
    ElMessage.success('取消预定成功')
    getReservationList()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('取消预定失败')
    }
  }
}

// 标记预定完成
const completeReservation = async (reservationId) => {
  try {
    await ElMessageBox.confirm(
        '确认该预定已完成吗？',
        '完成确认',
        {
          confirmButtonText: '确认完成',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    await apiCompleteReservation(reservationId)
    ElMessage.success('标记完成成功')
    getReservationList()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('标记完成失败')
    }
  }
}

// 查看预定队列
const viewReservationQueue = async (bookId) => {
  try {
    const res = await getBookReservationQueue(bookId)
    reservationQueueList.value = res.data
    queueDialogVisible.value = true
  } catch (err) {
    ElMessage.error('获取预定队列失败')
  }
}

// 获取预定状态标签类型
const getReserveStatusTagType = (status) => {
  switch (status) {
    case 'PENDING':
      return 'info'
    case 'READY':
      return 'success'
    case 'CANCELLED':
      return 'danger'
    case 'COMPLETED':
      return 'primary'
    default:
      return 'default'
  }
}

// 获取预定状态文本
const getReserveStatusText = (status) => {
  switch (status) {
    case 'PENDING':
      return '等待中'
    case 'READY':
      return '可借阅'
    case 'CANCELLED':
      return '已取消'
    case 'COMPLETED':
      return '已完成'
    default:
      return status
  }
}

// ========== 罚款管理 ==========
// 获取罚款列表
const getFineList = async () => {
  fineLoading.value = true
  try {
    const res = await getAllFines({
      ...fineQueryForm,
      payStatus: fineQueryForm.payStatus || undefined
    })
    fineList.value = res.data.content
    fineTotal.value = res.data.totalElements
  } catch (err) {
    ElMessage.error('获取罚款记录失败')
  } finally {
    fineLoading.value = false
  }
}

// 重置罚款筛选条件
const resetFineQuery = () => {
  fineQueryForm.userName = ''
  fineQueryForm.bookName = ''
  fineQueryForm.payStatus = ''
  fineQueryForm.pageNum = 1
  getFineList()
}

// 罚款分页大小改变
const handleFineSizeChange = (val) => {
  fineQueryForm.pageSize = val
  getFineList()
}

// 罚款当前页改变
const handleFineCurrentChange = (val) => {
  fineQueryForm.pageNum = val
  getFineList()
}

// 更新罚款状态
const updateFineStatus = async (fineId, status) => {
  try {
    const statusText = status === 'paid' ? '已支付' : '未支付'
    await ElMessageBox.confirm(
        `确认将该罚款标记为${statusText}吗？`,
        '状态更新确认',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    await apiUpdateFineStatus(fineId, status)
    ElMessage.success(`罚款已标记为${statusText}`)
    getFineList()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('状态更新失败')
    }
  }
}

// 获取分馆名称
const getBranchName = (branchId) => {
  const branch = branchList.value.find(item => item.branchId === branchId)
  return branch ? branch.branchName : ''
}
</script>

<style scoped>
.borrow-management {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.query-form {
  margin-bottom: 20px;
}
.text-red {
  color: #f56c6c;
  font-weight: 500;
}
.el-dialog .el-table {
  width: 100%;
}
</style>