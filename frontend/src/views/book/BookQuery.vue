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
          <el-select v-model="queryForm.bookType" placeholder="请选择类型">
            <el-option label="图书" value="book"></el-option>
            <el-option label="杂志" value="magazine"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="可借状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态">
            <el-option label="正常可借" value="normal"></el-option>
            <el-option label="缺货" value="out_of_stock"></el-option>
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
        <el-table-column prop="bookType" label="类型" width="80">
          <template #default="scope">
            {{ scope.row.bookType === 'book' ? '图书' : '杂志' }}
          </template>
        </el-table-column>
        <el-table-column prop="availableNum" label="可借数量" width="100"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="text" @click="toDetail(scope.row.bookId)">查看详情</el-button>
            <el-button
                type="primary"
                size="small"
                @click="handleBorrow(scope.row)"
                :disabled="scope.row.availableNum <= 0"
            >
              借阅
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
import { useRouter } from 'vue-router'
import axios from '@/utils/request'

const router = useRouter()
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
  branchId: '',
  bookType: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

// 初始化
onMounted(() => {
  getBranchList()
  handleQuery()
})

// 获取分馆列表
const getBranchList = async () => {
  const res = await axios.get('/api/branches')
  branchList.value = res.data
}

// 多条件查询图书
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await axios.post('/api/books/query', queryForm.value)
    bookList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('查询失败：' + error.response.data.msg)
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
    branchId: '',
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
    await axios.post('/api/borrow/create', {
      bookId: book.bookId,
      branchId: book.branchId
    })
    ElMessage.success('借阅申请提交成功')
    handleQuery() // 刷新列表
  } catch (error) {
    ElMessage.error('借阅失败：' + error.response.data.msg)
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