<template>
  <div class="book-management">
    <el-card>
      <div class="card-header">
        <h2>图书管理</h2>
        <el-button type="primary" @click="openAddDialog">新增图书</el-button>
      </div>
      <!-- 筛选条件（核心修改：拆分两行布局） -->
      <el-form :model="queryForm" class="query-form" @submit.prevent="getBookList">
        <!-- 第一行：图书名称、作者、ISBN、所属分馆 -->
        <div class="query-row">
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
        </div>
        <!-- 第二行：图书类型、图书状态、查询按钮、重置按钮（核心调整行） -->
        <div class="query-row" style="margin-top: 10px;">
          <el-form-item label="图书类型">
            <el-select v-model="queryForm.bookType" placeholder="请选择类型">
              <el-option label="技术书籍" value="技术书籍" />
              <el-option label="教材" value="教材" />
              <el-option label="古典文学" value="古典文学" />
              <el-option label="教辅" value="教辅" />
            </el-select>
          </el-form-item>
          <el-form-item label="图书状态">
            <el-select v-model="queryForm.status" placeholder="请选择状态">
              <el-option label="全部" value="" />
              <el-option label="正常可借" value="AVAILABLE" />
              <el-option label="缺货" value="OUT_OF_STOCK" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="getBookList">查询</el-button>
          </el-form-item>
          <el-form-item>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </div>
      </el-form>
      <!-- 图书列表（保持不变） -->
      <el-table :data="bookList.content || []" border stripe v-loading="loading">
        <el-table-column prop="bookId" label="图书ID" width="80"></el-table-column>
        <el-table-column prop="bookName" label="图书名称" min-width="200"></el-table-column>
        <el-table-column prop="author" label="作者" width="120"></el-table-column>
        <el-table-column prop="isbn" label="ISBN" width="180"></el-table-column>
        <el-table-column prop="category" label="分类" width="100"></el-table-column>
        <el-table-column prop="branchName" label="所属分馆" width="120">
          <template #default="scope">
            {{ getBranchName(scope.row.branchId) }}
          </template>
        </el-table-column>
        <el-table-column prop="bookType" label="类型" width="80">
          <template #default="scope">
            {{ scope.row.bookType }}
          </template>
        </el-table-column>
        <el-table-column prop="totalNum" label="总数量" width="80"></el-table-column>
        <el-table-column prop="availableNum" label="可借数量" width="80"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'AVAILABLE' ? 'success' : 'danger'">
              {{ scope.row.status === 'AVAILABLE' ? '正常可借' : '缺货' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="link" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button type="link" color="red" @click="handleDeleteBook(scope.row.bookId)" :disabled="scope.row.availableNum > 0">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页（保持不变） -->
      <el-pagination
          v-if="bookList.total > 0"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="queryForm.pageNum"
          :page-sizes="[10, 20, 50]"
          :page-size="queryForm.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="bookList.total"
          style="margin-top: 20px; text-align: right"
      ></el-pagination>
    </el-card>
    <!-- 新增/编辑图书弹窗（保持不变） -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑图书' : '新增图书'" width="600px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="图书名称" prop="bookName">
          <el-input v-model="form.bookName" placeholder="请输入图书名称"></el-input>
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者"></el-input>
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn" :disabled="isEdit">
          <el-input v-model="form.isbn" placeholder="请输入ISBN"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="请输入图书分类"></el-input>
        </el-form-item>
        <el-form-item label="所属分馆" prop="branchId">
          <el-select v-model="form.branchId" placeholder="请选择分馆">
            <el-option v-for="branch in branchList" :key="branch.branchId" :label="branch.branchName" :value="branch.branchId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="图书类型" prop="bookType">
          <el-select v-model="form.bookType" placeholder="请选择图书类型">
            <el-option label="技术书籍" value="技术书籍" />
            <el-option label="教材" value="教材" />
            <el-option label="古典文学" value="古典文学" />
            <el-option label="教辅" value="教辅" />
          </el-select>
        </el-form-item>
        <el-form-item label="总数量" prop="totalNum">
          <el-input v-model.number="form.totalNum" placeholder="请输入总数量"></el-input>
        </el-form-item>
        <el-form-item label="可借数量" prop="availableNum">
          <el-input v-model.number="form.availableNum" placeholder="请输入可借数量"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBranchList } from '@/api/branch'
import { addBook, updateBook, deleteBook, searchBooks } from '@/api/book'
const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const bookList = ref({ content: [], total: 0 })
const branchList = ref([])
const total = ref(0)
// 查询条件（保持不变）
const queryForm = reactive({
  bookName: '',
  author: '',
  isbn: '',
  branchId: '',
  bookType: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})
// 表单数据（保持不变）
const form = reactive({
  bookId: '',
  bookName: '',
  author: '',
  isbn: '',
  category: '',
  branchId: '',
  bookType: '技术书籍',
  totalNum: 0,
  availableNum: 0,
  status: 'AVAILABLE'
})
// 表单校验规则（保持不变）
const formRules = reactive({
  bookName: [{required: true, message: '请输入图书名称', trigger: 'blur'}],
  author: [{required: true, message: '请输入作者', trigger: 'blur'}],
  isbn: [{required: true, message: '请输入ISBN', trigger: 'blur'}],
  category: [{required: true, message: '请输入分类', trigger: 'blur'}],
  branchId: [{required: true, message: '请选择分馆', trigger: 'change'}],
  bookType: [{required: true, message: '请选择图书类型', trigger: 'change'}],
  totalNum: [
    {required: true, message: '请输入总数量', trigger: 'blur'},
    {type: 'number', min: 1, message: '总数量必须大于0', trigger: 'blur'}
  ],
  availableNum: [
    {required: true, message: '请输入可借数量', trigger: 'blur'},
    {type: 'number', min: 0, message: '可借数量不能为负数', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (form.totalNum > 0 && value > form.totalNum) {
          callback(new Error(`可借数量不能大于总数量（当前总数量：${form.totalNum}）`));
        } else {
          callback();
        }
      },
      trigger: ['blur', 'change']
    }
  ]
})
// 初始化（保持不变）
onMounted(() => {
  loadBranchList()
  getBookList()
})
// 加载分馆列表（保持不变）
const loadBranchList = async () => {
  try {
    const res = await getBranchList()
    branchList.value = res.data
  } catch (err) {
    ElMessage.error('获取分馆列表失败')
  }
}
// 获取图书列表（保持不变）
const getBookList = async () => {
  loading.value = true
  try {
    const queryParams = {
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      branchId: queryForm.branchId || undefined,
      bookName: queryForm.bookName || undefined,
      author: queryForm.author || undefined,
      isbn: queryForm.isbn || undefined,
      bookType: queryForm.bookType || undefined,
      status: queryForm.status || undefined
    }
    const res = await searchBooks(queryParams)
    bookList.value = res.data
    total.value = res.data.totalElements
  } catch (err) {
    ElMessage.error('获取图书列表失败：' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}
// 重置查询（保持不变）
const resetQuery = () => {
  queryForm.bookName = ''
  queryForm.author = ''
  queryForm.isbn = ''
  queryForm.branchId = ''
  queryForm.bookType = ''
  queryForm.status = ''
  queryForm.pageNum = 1
  getBookList()
}
// 分页处理（保持不变）
const handleSizeChange = (val) => {
  queryForm.pageSize = val
  getBookList()
}
const handleCurrentChange = (val) => {
  queryForm.pageNum = val
  getBookList()
}
// 打开新增弹窗（保持不变）
const openAddDialog = () => {
  isEdit.value = false
  form.bookId = ''
  form.bookName = ''
  form.author = ''
  form.isbn = ''
  form.category = ''
  form.branchId = ''
  form.bookType = '技术书籍'
  form.totalNum = 0
  form.availableNum = 0
  form.status = 'AVAILABLE'
  dialogVisible.value = true
}
// 打开编辑弹窗（保持不变）
const openEditDialog = (row) => {
  isEdit.value = true
  form.bookId = row.bookId
  form.bookName = row.bookName
  form.author = row.author
  form.isbn = row.isbn
  form.category = row.category
  form.branchId = row.branchId
  form.bookType = row.bookType
  form.totalNum = row.totalNum
  form.availableNum = row.availableNum
  form.status = row.status
  dialogVisible.value = true
}
// 提交表单（保持不变）
const submitForm = async () => {
  try {
    await formRef.value.validate()
    form.status = form.availableNum > 0 ? 'AVAILABLE' : 'OUT_OF_STOCK'
    if (isEdit.value) {
      await updateBook(form.bookId, form)
      ElMessage.success('编辑图书成功')
    } else {
      await addBook(form)
      ElMessage.success('新增图书成功')
    }
    dialogVisible.value = false
    getBookList()
  } catch (err) {
    ElMessage.error('操作失败：' + (err.response?.data?.message || err.message))
  }
}
// 删除图书（保持不变）
const handleDeleteBook = async (bookId) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除该图书吗？删除后不可恢复！',
        '删除确认',
        {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    await deleteBook(bookId)
    ElMessage.success('删除图书成功')
    getBookList()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败：' + (err.response?.data?.message || err.message))
    }
  }
}
// 获取分馆名称（保持不变）
const getBranchName = (branchId) => {
  const branch = branchList.value.find(item => item.branchId === branchId)
  return branch ? branch.branchName : ''
}
</script>
<style scoped>
.book-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

/* 核心修改：新增查询行样式，实现横向排列并支持换行 */
.query-form {
  margin-bottom: 20px;
}

.query-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.query-row .el-form-item {
  margin-bottom: 0;
}

/* 保持原有样式不变 */
.el-table {
  width: 100%;
}
</style>