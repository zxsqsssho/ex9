<template>
  <div class="borrow-records-page">
    <el-card shadow="hover">
      <el-tabs v-model="activeTab" type="card">
        <!-- 当前借阅 -->
        <el-tab-pane label="当前借阅" name="borrowing">
          <div class="table-container">
            <el-table :data="borrowList.content || []" border stripe v-loading="loading">
              <el-table-column prop="id" label="记录ID" width="80"></el-table-column>
              <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>
              <el-table-column prop="author" label="作者" width="120"></el-table-column>
              <el-table-column prop="borrowTime" label="借阅时间" min-width="180"></el-table-column>
              <el-table-column prop="dueTime" label="应还时间" min-width="180">
                <template #default="scope">
                  <span :class="scope.row.overdueDays > 0 ? 'text-red' : ''">
                    {{ scope.row.dueTime }}
                    <span v-if="scope.row.overdueDays > 0">(逾期{{ scope.row.overdueDays }}天)</span>
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="branchName" label="借阅分馆" width="120"></el-table-column>
              <el-table-column label="操作" fixed="right" width="180">
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
                style="margin-top: 16px; text-align: right"
            ></el-pagination>
          </div>
        </el-tab-pane>
        <!-- 借阅历史 -->
        <el-tab-pane label="借阅历史" name="history">
          <div class="table-container">
            <el-table :data="borrowHistoryList.content || []" border stripe v-loading="loading">
              <el-table-column prop="id" label="记录ID" width="80"></el-table-column>
              <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>
              <el-table-column prop="author" label="作者" width="120"></el-table-column>
              <el-table-column prop="borrowTime" label="借阅时间" min-width="180"></el-table-column>
              <el-table-column prop="returnTime" label="归还时间" min-width="180"></el-table-column>
              <el-table-column prop="dueTime" label="应还时间" min-width="180"></el-table-column>
              <el-table-column prop="branchName" label="借阅分馆" width="120"></el-table-column>
              <el-table-column prop="status" label="借阅状态" width="120">
                <template #default="scope">
                  <el-tag type="success" v-if="scope.row.status === 'RETURNED'">已归还</el-tag>
                  <el-tag type="danger" v-if="scope.row.status === 'OVERDUE'">已逾期归还</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="overdueDays" label="逾期天数" width="100">
                <template #default="scope">
                  {{ scope.row.overdueDays || 0 }}天
                </template>
              </el-table-column>
            </el-table>
            <!-- 分页 -->
            <el-pagination
                v-if="borrowHistoryList.total > 0"
                @size-change="handleHistorySizeChange"
                @current-change="handleHistoryCurrentChange"
                :current-page="historyQueryForm.pageNum"
                :page-sizes="[10, 20, 50]"
                :page-size="historyQueryForm.pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="borrowHistoryList.total"
                style="margin-top: 16px; text-align: right"
            ></el-pagination>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 导入借阅相关API
import {
  getMyBorrowList as apiGetMyBorrowList,
  getMyBorrowHistory as apiGetMyBorrowHistory,
  returnBook as apiReturnBook,
  renewBook as apiRenewBook
} from '@/api/borrow'

const activeTab = ref('borrowing')
const loading = ref(false)
const returnLoading = ref('')

// 当前借阅查询条件
const queryForm = ref({
  pageNum: 1,
  pageSize: 10
})

// 借阅历史查询条件
const historyQueryForm = ref({
  pageNum: 1,
  pageSize: 10
})

// 数据列表（接收后端Page对象）
const borrowList = ref({ content: [], total: 0 }) // 当前借阅
const borrowHistoryList = ref({ content: [], total: 0 }) // 借阅历史

onMounted(() => {
  loadAllData()
})

// 加载所有数据
const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      getMyBorrowList(),
      getMyBorrowHistory()
    ])
  } catch (error) {
    ElMessage.error('借阅记录加载失败')
  } finally {
    loading.value = false
  }
}

// 获取当前借阅列表
const getMyBorrowList = async () => {
  try {
    const params = {
      page: queryForm.value.pageNum - 1,
      size: queryForm.value.pageSize
    }
    const res = await apiGetMyBorrowList(params)
    borrowList.value = res.data
  } catch (error) {
    ElMessage.error('获取当前借阅列表失败：' + (error.response?.data?.message || error.message))
  }
}

// 获取借阅历史
const getMyBorrowHistory = async () => {
  try {
    const params = {
      page: historyQueryForm.value.pageNum - 1,
      size: historyQueryForm.value.pageSize
    }
    const res = await apiGetMyBorrowHistory(params)
    borrowHistoryList.value = res.data
  } catch (error) {
    ElMessage.error('获取借阅历史失败：' + (error.response?.data?.message || error.message))
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
    await getMyBorrowList() // 归还后刷新当前借阅列表
    await getMyBorrowHistory() // 同步刷新历史列表
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || '归还失败'
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
        '确认续借这本书吗？续借后将延长借阅期限',
        '续借确认',
        {
          confirmButtonText: '确认续借',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    await apiRenewBook(bookId)
    ElMessage.success('续借成功')
    await getMyBorrowList() // 续借后刷新当前借阅列表
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || '续借失败'
      ElMessage.error(errorMsg)
    }
  }
}

// 当前借阅分页处理
const handleSizeChange = (val) => {
  queryForm.value.pageSize = val
  getMyBorrowList()
}

const handleCurrentChange = (val) => {
  queryForm.value.pageNum = val
  getMyBorrowList()
}

// 借阅历史分页处理
const handleHistorySizeChange = (val) => {
  historyQueryForm.value.pageSize = val
  getMyBorrowHistory()
}

const handleHistoryCurrentChange = (val) => {
  historyQueryForm.value.pageNum = val
  getMyBorrowHistory()
}
</script>
<style scoped>
.borrow-records-page {
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
  .borrow-records-page {
    padding: 10px;
  }
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>