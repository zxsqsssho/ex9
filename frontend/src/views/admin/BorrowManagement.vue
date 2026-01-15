<template>
  <div class="borrow-management">
    <!-- 借阅管理 -->
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
      <!-- 借阅记录列表：固定高度 -->
      <el-table
          :data="borrowList.content || []"
          border
          stripe
          v-loading="loading"
          class="fixed-height-table"
      >
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
            <span :class="scope.row.overdueDays > 0 ? 'text-red' : ''">
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
            <el-button :link="true" color="blue" size="small" @click="viewFine(scope.row.id)" v-if="scope.row.overdueDays > 0">
              查看罚款
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <el-pagination
          v-if="borrowList.total > 0"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="queryForm.pageNum"
          :page-sizes="[10, 20, 50]"
          :page-size="queryForm.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="borrowList.total"
          style="margin-top: 20px; text-align: right"
      ></el-pagination>
    </el-card>
    <!-- 预定管理 -->
    <el-card style="margin-top: 20px;">
      <div class="card-header">
        <h2>预定管理</h2>
      </div>
      <!-- 预定筛选条件：修复图书状态查询 -->
      <el-form :model="reserveQueryForm" inline class="query-form" @submit.prevent="getReservationList">
        <el-form-item label="图书名称">
          <el-input v-model="reserveQueryForm.bookName" placeholder="请输入图书名称"></el-input>
        </el-form-item>
        <el-form-item label="所属分馆">
          <el-select v-model="reserveQueryForm.branchId" placeholder="请选择分馆">
            <el-option v-for="branch in branchList" :key="branch.branchId" :label="branch.branchName" :value="branch.branchId"></el-option>
          </el-select>
        </el-form-item>
        <!-- 图书状态筛选（修复参数传递） -->
        <el-form-item label="图书状态">
          <el-select v-model="reserveQueryForm.bookStatus" placeholder="请选择图书状态">
            <el-option label="全部" value=""></el-option>
            <el-option label="空闲" value="AVAILABLE"></el-option>
            <el-option label="排队中" value="QUEUED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getReservationList">查询</el-button>
          <el-button @click="resetReserveQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 预定列表：同图书ID去重 + 固定高度 -->
      <el-table
          :data="uniqueReservationList"
          border
          stripe
          v-loading="reserveLoading"
          :default-sort="{ prop: 'bookId', order: 'asc' }"
          class="fixed-height-table"
      >
        <el-table-column prop="bookId" label="图书ID" width="80"></el-table-column>
        <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>
        <el-table-column prop="author" label="作者" width="120"></el-table-column>
        <el-table-column prop="branchName" label="所属分馆" width="120">
          <template #default="scope">
            {{ getBranchName(scope.row.branchId) }}
          </template>
        </el-table-column>
        <el-table-column label="图书状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.availableNum > 0 ? 'success' : 'info'">
              {{ scope.row.availableNum > 0 ? '空闲' : '排队中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewReservationQueue(scope.row.bookId)">
              查看队列
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 预定分页 -->
      <el-pagination
          v-if="reservationList.total > 0"
          @size-change="handleReserveSizeChange"
          @current-change="handleReserveCurrentChange"
          :current-page="reserveQueryForm.pageNum"
          :page-sizes="[10, 20, 50]"
          :page-size="reserveQueryForm.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="uniqueReservationList.length"
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
      <!-- 罚款列表：固定高度 -->
      <el-table
          :data="fineList.content || []"
          border
          stripe
          v-loading="fineLoading"
          class="fixed-height-table"
      >
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
          v-if="fineList.total > 0"
          @size-change="handleFineSizeChange"
          @current-change="handleFineCurrentChange"
          :current-page="fineQueryForm.pageNum"
          :page-sizes="[10, 20, 50]"
          :page-size="fineQueryForm.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="fineList.total"
          style="margin-top: 20px; text-align: right"
      ></el-pagination>
    </el-card>
    <!-- 预定队列弹窗 -->
    <el-dialog v-model="queueDialogVisible" title="图书预定队列（按预定时间排序）" width="700px">
      <el-table
          :data="reservationQueueList.content || []"
          border stripe
          :default-sort="{ prop: 'reserveTime', order: 'asc' }"
          class="fixed-height-table"
      >
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
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="handleQueueComplete(scope.row.id)"
                v-if="scope.row.status === 'PENDING'"
            >
              标记完成（已完成）
            </el-button>
            <el-button
                type="danger"
                size="small"
                @click="handleQueueCancel(scope.row.id)"
                v-if="scope.row.status === 'PENDING'"
            >
              取消预定
            </el-button>
            <el-button
                type="success"
                size="small"
                @click="handleQueueComplete(scope.row.id)"
                v-if="scope.row.status === 'READY'"
            >
              标记完成（已完成）
            </el-button>
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
import { ref, reactive, onMounted, computed } from 'vue'
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
  getBookReservationQueue,
  updateReservationStatus
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
const borrowList = ref({ content: [], total: 0 })
const queryForm = reactive({
  userName: '',
  bookName: '',
  branchId: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})
// 预定管理数据（修复图书状态查询参数）
const reservationList = ref({ content: [], total: 0 })
const reserveQueryForm = reactive({
  bookName: '',
  branchId: '',
  bookStatus: '', // 图书状态筛选字段（AVAILABLE/QUEUED）
  pageNum: 1,
  pageSize: 10
})
// 罚款管理数据
const fineList = ref({ content: [], total: 0 })
const fineQueryForm = reactive({
  userName: '',
  bookName: '',
  payStatus: '',
  pageNum: 1,
  pageSize: 10
})
// 预定队列弹窗
const queueDialogVisible = ref(false)
const reservationQueueList = ref({ content: [] })

// 核心修改1：同图书ID去重，只保留一条记录（按bookId唯一）
const uniqueReservationList = computed(() => {
  const content = reservationList.value.content || []
  const bookMap = new Map()
  content.forEach(item => {
    if (!bookMap.has(item.bookId)) {
      bookMap.set(item.bookId, item)
    }
  })
  return Array.from(bookMap.values())
})

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

// ========== 借阅管理 ==========
const getBorrowList = async () => {
  loading.value = true
  try {
    const params = { ...queryForm, branchId: queryForm.branchId || undefined }
    const res = await getAllBorrowRecords(params)
    borrowList.value = res.data
  } catch (err) {
    ElMessage.error('获取借阅记录失败：' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}
const resetQuery = () => {
  queryForm.userName = ''
  queryForm.bookName = ''
  queryForm.branchId = ''
  queryForm.status = ''
  queryForm.pageNum = 1
  getBorrowList()
}
const handleSizeChange = (val) => {
  queryForm.pageSize = val
  getBorrowList()
}
const handleCurrentChange = (val) => {
  queryForm.pageNum = val
  getBorrowList()
}
const handleReturn = async (borrowId) => {
  try {
    await ElMessageBox.confirm('确认该图书已归还吗？', '归还确认', { type: 'warning' })
    await apiReturnBook(borrowId)
    ElMessage.success('归还成功')
    getBorrowList()
    getFineList()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('归还失败')
    }
  }
}
const viewFine = (borrowId) => {
  const fine = fineList.value.content.find(item => item.recordId === borrowId)
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
        { dangerouslyUseHTMLString: true }
    )
  } else {
    ElMessage.info('未查询到相关罚款记录')
  }
}
const getStatusTagType = (status) => {
  switch (status) {
    case 'BORROWED': return 'primary'
    case 'RETURNED': return 'success'
    case 'OVERDUE': return 'danger'
    default: return 'default'
  }
}
const getStatusText = (status) => {
  switch (status) {
    case 'BORROWED': return '借阅中'
    case 'RETURNED': return '已归还'
    case 'OVERDUE': return '已逾期'
    default: return status
  }
}

// ========== 预定管理 ==========
const getReservationList = async () => {
  reserveLoading.value = true;
  try {
    const params = {
      // 分页参数转换（前端pageNum从1开始，后端Pageable从0开始）
      page: reserveQueryForm.pageNum - 1,
      size: reserveQueryForm.pageSize,
      // 筛选参数：仅传递非空值，避免后端处理无效参数
      bookName: reserveQueryForm.bookName || undefined,
      branchId: reserveQueryForm.branchId || undefined,
      bookStatus: reserveQueryForm.bookStatus || undefined, // 关键：传递图书状态参数
      sort: 'bookId,asc'
    };
    // 调用后端接口（确保接口路径和参数名与后端一致）
    const res = await getAllReservations(params);
    reservationList.value = res.data;
  } catch (err) {
    ElMessage.error('获取预定记录失败：' + (err.response?.data?.message || err.message));
  } finally {
    reserveLoading.value = false;
  }
};
const resetReserveQuery = () => {
  reserveQueryForm.bookName = ''
  reserveQueryForm.branchId = ''
  reserveQueryForm.bookStatus = '' // 重置图书状态
  reserveQueryForm.pageNum = 1
  getReservationList()
}
const handleReserveSizeChange = (val) => {
  reserveQueryForm.pageSize = val
  getReservationList()
}
const handleReserveCurrentChange = (val) => {
  reserveQueryForm.pageNum = val
  getReservationList()
}
const viewReservationQueue = async (bookId) => {
  try {
    const res = await getBookReservationQueue(bookId)
    // 按预定时间升序排序（前端二次确认，确保顺序）
    res.data.content = res.data.content.sort((a, b) => new Date(a.reserveTime) - new Date(b.reserveTime))
    reservationQueueList.value = res.data
    queueDialogVisible.value = true
  } catch (err) {
    ElMessage.error('获取预定队列失败：' + (err.response?.data?.message || err.message))
  }
}
// 标记完成：触发状态流转
const handleQueueComplete = async (reservationId) => {
  try {
    await ElMessageBox.confirm('确认执行该操作吗？', '操作确认', { type: 'warning' })
    await updateReservationStatus(reservationId)
    ElMessage.success('操作成功')
    // 刷新队列，后端会自动更新下一个预定状态
    const bookId = reservationQueueList.value.content[0]?.bookId
    viewReservationQueue(bookId)
    getReservationList()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('操作失败：' + (err.response?.data?.message || err.message))
    }
  }
}
// 取消预定：触发状态流转
const handleQueueCancel = async (reservationId) => {
  try {
    await ElMessageBox.confirm('确认取消该预定吗？', '取消确认', { type: 'warning' })
    await apiCancelReservation(reservationId)
    ElMessage.success('取消成功')
    // 刷新队列，后端会自动更新下一个预定状态
    const bookId = reservationQueueList.value.content[0]?.bookId
    viewReservationQueue(bookId)
    getReservationList()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('取消失败：' + (err.response?.data?.message || err.message))
    }
  }
}
// 状态显示
const getReserveStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '等待中'
    case 'READY': return '可借阅'
    case 'CANCELLED': return '已取消'
    case 'COMPLETED': return '已完成'
    default: return status
  }
}
const getReserveStatusTagType = (status) => {
  switch (status) {
    case 'PENDING': return 'info'
    case 'READY': return 'primary'
    case 'COMPLETED': return 'success'
    case 'CANCELLED': return 'danger'
    default: return 'default'
  }
}

// ========== 罚款管理 ==========
const getFineList = async () => {
  fineLoading.value = true
  try {
    const params = {
      ...fineQueryForm,
      payStatus: fineQueryForm.payStatus || undefined
    }
    const res = await getAllFines(params)
    fineList.value = res.data
  } catch (err) {
    ElMessage.error('获取罚款记录失败')
  } finally {
    fineLoading.value = false
  }
}
const resetFineQuery = () => {
  fineQueryForm.userName = ''
  fineQueryForm.bookName = ''
  fineQueryForm.payStatus = ''
  fineQueryForm.pageNum = 1
  getFineList()
}
const handleFineSizeChange = (val) => {
  fineQueryForm.pageSize = val
  getFineList()
}
const handleFineCurrentChange = (val) => {
  fineQueryForm.pageNum = val
  getFineList()
}
const updateFineStatus = async (fineId, status) => {
  try {
    const statusText = status === 'paid' ? '已支付' : '未支付'
    await ElMessageBox.confirm(`确认标记为${statusText}吗？`, '状态确认', { type: 'warning' })
    await apiUpdateFineStatus(fineId, status)
    ElMessage.success(`标记为${statusText}成功`)
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
.borrow-management { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.query-form { margin-bottom: 20px; }
.text-red { color: #f56c6c; font-weight: 500; }
.el-dialog .el-table { width: 100%; }
.el-button--link { color: #409eff; }
.el-button--link:hover { color: #66b1ff; }

/* 固定表格高度 */
.fixed-height-table {
  height: 400px; /* 固定高度 */
  overflow-y: auto; /* 垂直滚动 */
  overflow-x: hidden;
}

/* 优化滚动条样式 */
.fixed-height-table::-webkit-scrollbar {
  width: 6px;
}
.fixed-height-table::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 3px;
}
.fixed-height-table::-webkit-scrollbar-track {
  background-color: #f5f5f5;
}
</style>