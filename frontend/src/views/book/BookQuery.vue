<template>
  <div class="book-query-page">
    <!-- 筛选条件 -->
    <el-card shadow="hover" class="query-card">
      <el-form :model="queryForm" inline @submit.prevent="handleQuery">
        <el-form-item label="图书名称">
          <el-input v-model="queryForm.bookName" placeholder="请输入图书名称"></el-input>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="queryForm.author" placeholder="请输入作者"></el-input>
        </el-form-item>
        <el-form-item label="ISBN">
          <el-input v-model="queryForm.isbn" placeholder="请输入ISBN"></el-input>
        </el-form-item>
        <el-form-item label="所属分馆">
          <el-select v-model="queryForm.branchId" placeholder="请选择分馆">
            <el-option v-for="branch in branchList" :key="branch.branchId" :label="branch.branchName" :value="branch.branchId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="图书类型">
          <el-select v-model="queryForm.bookType">
            <el-option label="技术书籍" value="技术书籍" />
            <el-option label="教材" value="教材" />
            <el-option label="古典文学" value="古典文学" />
            <el-option label="教辅" value="教辅" />
          </el-select>
        </el-form-item>
        <el-form-item label="可借状态">
          <el-select v-model="queryForm.status">
            <el-option label="可借" value="AVAILABLE" />
            <el-option label="不可借" value="OUT_OF_STOCK" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 图书列表 -->
    <el-card shadow="hover" class="list-card">
      <el-table :data="bookList" border stripe v-loading="loading">
        <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>
        <el-table-column prop="author" label="作者" width="120"></el-table-column>
        <el-table-column prop="isbn" label="ISBN" width="180"></el-table-column>
        <el-table-column prop="category" label="分类" width="100"></el-table-column>
        <el-table-column prop="branchName" label="所属分馆" width="100"></el-table-column>
        <el-table-column prop="bookType" label="类型" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="availableNum" label="可借数量" width="100"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="text" @click="toDetail(scope.row.bookId)">查看详情</el-button>
            <!-- 条件渲染：可借数量>0显示借阅，否则显示预定 -->
            <el-button
                type="primary"
                size="small"
                @click="handleBorrow(scope.row)"
                :disabled="scope.row.availableNum <= 0"
                v-if="scope.row.availableNum > 0"
            >
              借阅
            </el-button>
            <el-button
                type="success"
                size="small"
                @click="handleReserve(scope.row)"
                :disabled="scope.row.availableNum > 0"
                v-else
            >
              预定
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
      >
      </el-pagination>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '@/utils/request'
// 导入借阅和预定API方法
import { borrowBook } from '@/api/borrow'
import { reserveBook } from '@/api/reservation'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const bookList = ref([])
const branchList = ref([])
const total = ref(0)

// 查询条件
const queryForm = ref({
  bookName: '',
  author: '',
  isbn: '',
  category: '',
  branchId: null,
  bookType: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

// 判断是否管理员
const isAdmin = ['ROLE_SYSTEM_ADMIN', 'ROLE_BRANCH_ADMIN'].includes(userStore.role)

// 初始化
onMounted(() => {
  getBranchList()
  handleQuery()
})

// 获取分馆列表（所有角色通用）
const getBranchList = async () => {
  try {
    const res = await axios.get('/branches')

    // ⚠️ ApiResponse 结构
    branchList.value = res.data

  } catch (err) {
    ElMessage.error('获取分馆列表失败：' + (err.response?.data?.msg || err.message))
    branchList.value = []
  }
}


// 多条件查询图书
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await axios.post('/books/query', queryForm.value)
    // ⚠️ axios 已经在拦截器里拆过 data
    const pageData = res.data
    bookList.value = pageData.content
    total.value = pageData.totalElements
    // 后端页码从 0 开始
    queryForm.value.pageNum = pageData.number + 1
  } catch (error) {
    ElMessage.error('查询失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  queryForm.value = {
    bookName: '',
    author: '',
    isbn: '',
    category: '',
    branchId: isAdmin ? null : 0,
    bookType: '',
    status: '',
    pageNum: 1,
    pageSize: 10
  }
  handleQuery()
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryForm.value.pageSize = val
  handleQuery()
}

// 当前页改变
const handleCurrentChange = (val) => {
  queryForm.value.pageNum = val
  handleQuery()
}

// 跳转到图书详情
const toDetail = (bookId) => {
  router.push({ name: 'BookDetail', params: { id: bookId } })
}

// 借阅图书
const handleBorrow = async (book) => {
  try {
    await borrowBook({
      bookId: book.bookId,
      branchId: book.branchId
    })
    ElMessage.success('借阅申请提交成功')
    handleQuery() // 刷新列表
  } catch (error) {
    ElMessage.error('借阅失败：' + (error.response?.data?.msg || error.message))
  }
}

// 新增：预定图书
const handleReserve = async (book) => {
  try {
    await reserveBook(book.bookId, book.branchId)
    ElMessage.success('预定成功，预定有效期7天')
    handleQuery() // 刷新列表，更新按钮状态
  } catch (error) {
    ElMessage.error('预定失败：' + (error.response?.data?.message || error.message))
  }
}
</script>

<style scoped>
.book-query-page {
  padding: 20px;
}
.query-card {
  margin-bottom: 20px;
}
.list-card {
  min-height: 500px;
}
</style>