<template>
  <div class="my-fines-page">
    <el-card shadow="hover">
      <div class="card-header">
        <h2>我的罚款记录</h2>
        <el-button type="primary" @click="refreshFines">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
      <!-- 罚款筛选 -->
      <el-form :model="queryForm" inline class="query-form" @submit.prevent="getMyFines">
        <el-form-item label="支付状态">
          <el-select v-model="queryForm.payStatus" placeholder="全部状态">
            <el-option label="全部" value=""></el-option>
            <el-option label="未支付" value="unpaid"></el-option>
            <el-option label="已支付" value="paid"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getMyFines">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 罚款列表 -->
      <div class="table-container">
        <el-table :data="fineList.content || []" border stripe v-loading="loading">
          <el-table-column prop="fineId" label="罚款ID" width="80"></el-table-column>
          <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>
          <el-table-column prop="overdueDays" label="逾期天数" width="100"></el-table-column>
          <el-table-column prop="fineAmount" label="罚款金额(元)" width="120">
            <template #default="scope">
              {{ scope.row.fineAmount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="payStatus" label="支付状态" width="100">
            <template #default="scope">
              <el-tag type="danger" v-if="scope.row.payStatus === 'unpaid'">未支付</el-tag>
              <el-tag type="success" v-if="scope.row.payStatus === 'paid'">已支付</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="payTime" label="支付时间" min-width="180"></el-table-column>
          <el-table-column label="操作" fixed="right" width="120">
            <template #default="scope">
              <el-button
                  type="primary"
                  size="small"
                  @click="handlePayFine(scope.row.fineId)"
                  v-if="scope.row.payStatus === 'unpaid'"
                  :loading="payLoading === scope.row.fineId"
              >
                立即支付
              </el-button>
              <el-button
                  type="text"
                  size="small"
                  @click="viewFineDetail(scope.row.fineId)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页 -->
        <el-pagination
            v-if="fineList.total > 0"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="queryForm.pageNum"
            :page-sizes="[10, 20, 50]"
            :page-size="queryForm.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="fineList.total"
            style="margin-top: 16px; text-align: right"
        ></el-pagination>
      </div>
    </el-card>
    <!-- 罚款详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="罚款详情" width="500px">
      <div v-if="fineDetail" class="detail-content">
        <p><strong>罚款ID：</strong>{{ fineDetail.fineId }}</p>
        <p><strong>图书名称：</strong>{{ fineDetail.bookName }}</p>
        <p><strong>ISBN：</strong>{{ fineDetail.isbn }}</p>
        <p><strong>借阅记录ID：</strong>{{ fineDetail.recordId }}</p>
        <p><strong>逾期天数：</strong>{{ fineDetail.overdueDays }}天</p>
        <p><strong>罚款金额：</strong>{{ fineDetail.fineAmount.toFixed(2) }}元</p>
        <p><strong>支付状态：</strong>
          <el-tag type="danger" v-if="fineDetail.payStatus === 'unpaid'">未支付</el-tag>
          <el-tag type="success" v-if="fineDetail.payStatus === 'paid'">已支付</el-tag>
        </p>
        <p v-if="fineDetail.payTime"><strong>支付时间：</strong>{{ fineDetail.payTime }}</p>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getMyFines as apiGetMyFines, payFine as apiPayFine, getFineDetail as apiGetFineDetail } from '@/api/fines'

const loading = ref(false)
const payLoading = ref('')
const detailDialogVisible = ref(false)
const fineList = ref({ content: [], total: 0 })
const fineDetail = ref(null)

// 查询条件（规范分页参数）
const queryForm = ref({
  payStatus: '',
  pageNum: 1, // 前端页码从1开始
  pageSize: 10
})

onMounted(() => {
  getMyFines()
})

// 核心修复：确保分页参数正确传递给API
const getMyFines = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryForm.value.pageNum,
      pageSize: queryForm.value.pageSize,
      payStatus: queryForm.value.payStatus || undefined
    }
    const res = await apiGetMyFines(params)
    fineList.value = res.data // 后端返回Page对象（content + totalElements）
  } catch (error) {
    ElMessage.error('获取罚款记录失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 其他方法保持不变
const refreshFines = async () => {
  queryForm.value.pageNum = 1
  await getMyFines()
  ElMessage.success('罚款记录已刷新')
}

const resetQuery = () => {
  queryForm.value = {
    payStatus: '',
    pageNum: 1,
    pageSize: 10
  }
  getMyFines()
}

const handleSizeChange = (val) => {
  queryForm.value.pageSize = val
  getMyFines()
}

const handleCurrentChange = (val) => {
  queryForm.value.pageNum = val
  getMyFines()
}

const handlePayFine = async (fineId) => {
  try {
    payLoading.value = fineId
    await ElMessageBox.confirm(
        '确认支付这笔罚款吗？支付后不可撤销',
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
      const errorMsg = error.response?.data?.message || '支付失败'
      ElMessage.error(errorMsg)
    }
  } finally {
    payLoading.value = ''
  }
}

const viewFineDetail = async (fineId) => {
  try {
    const res = await apiGetFineDetail(fineId)
    fineDetail.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取罚款详情失败：' + (error.response?.data?.message || error.message))
  }
}
</script>

<style scoped>
.my-fines-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.query-form {
  margin: 16px 0;
}
.table-container {
  margin-top: 8px;
}
.detail-content {
  line-height: 2;
  color: #303133;
}
@media (max-width: 768px) {
  .my-fines-page {
    padding: 10px;
  }
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>