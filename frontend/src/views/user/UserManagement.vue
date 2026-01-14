<!--frontend/src/views/user/UserManagement.vue-->
<template>
  <div class="user-management">
    <el-card>
      <div class="card-header">
        <h2>用户管理</h2>
        <el-button type="primary" @click="handleAddUser">新增用户</el-button>
      </div>

      <!-- 查询条件区域 -->
      <div class="query-area">
        <el-form :model="queryForm" :inline="true" class="query-form">
          <el-form-item label="查询条件">
            <el-input
                v-model="queryForm.username"
                placeholder="请输入用户名/姓名/邮箱"
                clearable
                title="用户搜索输入框"
            ></el-input>
          </el-form-item>
          <!-- 移除了用户类型选择框 -->
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table
          :data="userList"
          border
          v-if="Array.isArray(userList)"
          v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="realName" label="姓名"></el-table-column>
        <el-table-column prop="userType" label="用户类型">
          <template #default="scope">
            <el-tag :type="getUserTypeTagType(scope.row.userType)">
              {{ getUserTypeDisplayName(scope.row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : scope.row.status === 'DELETED' ? 'danger' : 'info'">
              {{ getStatusDisplayName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <!-- 正常用户显示编辑和删除 -->
            <el-button
                v-if="scope.row.status !== 'DELETED'"
                type="text"
                @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
                v-if="scope.row.status !== 'DELETED'"
                type="text"
                style="color: red"
                @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
            <!-- 已删除的用户显示添加/恢复按钮 -->
            <el-button
                v-if="scope.row.status === 'DELETED'"
                type="text"
                style="color: #67C23A"
                @click="handleRecover(scope.row)"
            >
              添加
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态处理 -->
      <div v-if="!loading && (!userList || userList.length === 0)" class="empty-state">
        <el-empty description="暂无用户数据" />
      </div>

      <!-- 分页组件 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
            :current-page="queryForm.page"
            :page-size="queryForm.size"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id"
                    :title="form.id ? '用户名不可编辑' : '请输入用户名'"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="form.userType" placeholder="请选择用户类型" title="选择用户类型">
            <el-option label="学生" value="STUDENT"></el-option>
            <el-option label="教师" value="TEACHER"></el-option>
            <el-option label="分馆管理员" value="BRANCH_ADMIN"></el-option>
            <el-option label="系统管理员" value="SYSTEM_ADMIN"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="分馆" prop="branchId">
          <el-select v-model="form.branchId" placeholder="请选择分馆">
            <el-option label="校本部" :value="1"></el-option>
            <el-option label="黄塘校区" :value="2"></el-option>
            <el-option label="程江校区" :value="3"></el-option>
            <el-option label="江南校区" :value="4"></el-option>
            <el-option label="丰顺校区" :value="5"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入至少6位密码"></el-input>
        </el-form-item>
        <el-form-item v-if="form.id" label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="启用" value="ACTIVE"></el-option>
            <el-option label="禁用" value="INACTIVE"></el-option>
            <!-- 编辑时不能直接选择已删除状态，需要通过删除按钮操作 -->
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/request'

// 状态变量
const userList = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const total = ref(0)

// 查询条件
const queryForm = reactive({
  username: '',
  page: 1,
  size: 10
})

// 表单数据
const form = ref({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  userType: 'STUDENT',
  branchId: 1,
  password: '',
  status: 'ACTIVE'
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  branchId: [
    { required: true, message: '请选择分馆', trigger: 'change' }
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (!form.value.id && (!value || value.length < 6)) {
          callback(new Error('密码至少6个字符'))
        } else {
          callback()
        }
      }
    }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 用户类型显示名称映射
const userTypeMapping = {
  'STUDENT': '学生',
  'TEACHER': '教师',
  'BRANCH_ADMIN': '分馆管理员',
  'SYSTEM_ADMIN': '系统管理员'
}

// 状态显示名称映射
const statusMapping = {
  'ACTIVE': '启用',
  'INACTIVE': '禁用',
  'DELETED': '已删除'
}

// 获取用户类型显示名称
const getUserTypeDisplayName = (userType) => {
  return userTypeMapping[userType] || userType
}

// 获取用户类型对应的标签类型
const getUserTypeTagType = (userType) => {
  const typeMap = {
    'SYSTEM_ADMIN': 'danger',
    'BRANCH_ADMIN': 'warning',
    'TEACHER': 'primary',
    'STUDENT': 'success'
  }
  return typeMap[userType] || 'info'
}

// 获取状态显示名称
const getStatusDisplayName = (status) => {
  return statusMapping[status] || status
}

// 初始化
onMounted(() => {
  getUserList()
})

// 获取用户列表
// 获取用户列表
const getUserList = async () => {
  loading.value = true
  try {
    // 构建基础分页参数
    const baseParams = {
      page: queryForm.page - 1, // 后端从0开始
      size: queryForm.size
    }

    let res

    // 修改点：判断是否有用户名，如果有则调用搜索接口，否则调用分页列表接口
    if (queryForm.username && queryForm.username.trim()) {
      // 调用搜索接口
      const searchParams = {
        ...baseParams,
        keyword: queryForm.username.trim()  // 注意：搜索接口使用keyword参数
      }

      console.log('搜索用户参数:', searchParams)

      res = await axios.get('/users/search', {
        params: searchParams,
        paramsSerializer: function(params) {
          return Object.keys(params)
              .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
              .join('&')
        }
      })

      console.log('搜索用户响应:', res)
    } else {
      // 调用分页列表接口
      console.log('分页列表参数:', baseParams)

      res = await axios.get('/users', {
        params: baseParams,
        paramsSerializer: function(params) {
          return Object.keys(params)
              .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
              .join('&')
        }
      })

      console.log('分页列表响应:', res)
    }

    // 处理响应数据 - 这部分保持不变
    if (res.data && Array.isArray(res.data)) {
      userList.value = res.data
      total.value = res.data.length
    } else if (res.data && res.data.content && Array.isArray(res.data.content)) {
      userList.value = res.data.content
      total.value = res.data.totalElements || 0
    } else if (res.data && res.data.data && Array.isArray(res.data.data)) {
      userList.value = res.data.data
      total.value = res.data.total || res.data.data.length
    } else {
      userList.value = []
      total.value = 0
      console.warn('用户列表数据格式不正确:', res.data)
    }

  } catch (err) {
    console.error('获取用户列表失败:', err)
    ElMessage.error('获取用户列表失败: ' + (err.message || '未知错误'))
    userList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 查询处理
const handleQuery = () => {
  queryForm.page = 1
  getUserList()
}

// 重置查询
const resetQuery = () => {
  queryForm.username = ''  // 只重置用户名
  queryForm.page = 1
  getUserList()
}

// 分页处理
const handleSizeChange = (size) => {
  queryForm.size = size
  queryForm.page = 1
  getUserList()
}

const handleCurrentChange = (page) => {
  queryForm.page = page
  getUserList()
}

// 编辑用户
const handleEdit = (row) => {
  console.log('编辑用户数据:', row)

  form.value = {
    id: row.id,
    username: row.username,
    realName: row.realName,
    email: row.email,
    phone: row.phone || '',
    userType: row.userType,
    branchId: row.branchId || 1,
    password: '',
    status: row.status || 'ACTIVE'
  }

  console.log('表单数据:', form.value)
  dialogVisible.value = true
}

// 新增用户
const handleAddUser = () => {
  form.value = {
    id: null,
    username: '',
    realName: '',
    email: '',
    phone: '',
    userType: 'STUDENT',
    branchId: 1,
    password: '',
    status: 'ACTIVE'
  }

  // 重置表单验证
  if (formRef.value) {
    formRef.value.resetFields()
  }

  dialogVisible.value = true
}

// 保存用户
const handleSave = async () => {
  try {
    if (formRef.value) {
      await formRef.value.validate()
    }

    saving.value = true
    console.log('保存用户数据:', JSON.stringify(form.value, null, 2))

    const requestData = {
      realName: form.value.realName,
      email: form.value.email,
      phone: form.value.phone,
      userType: form.value.userType,
      branchId: Number(form.value.branchId),
      status: form.value.status
    }

    if (!form.value.id) {
      requestData.username = form.value.username
      requestData.password = form.value.password
    }

    console.log('发送到后端的请求数据:', JSON.stringify(requestData, null, 2))

    if (form.value.id) {
      const res = await axios.put(`/users/${form.value.id}`, requestData)
      console.log('编辑用户响应:', res)
      ElMessage.success('用户更新成功')
    } else {
      const res = await axios.post('/users', requestData)
      console.log('新增用户响应:', res)
      ElMessage.success('用户创建成功')
    }

    dialogVisible.value = false
    getUserList()

  } catch (err) {
    console.error('保存用户失败:', err)
    let errorMsg = '操作失败'

    // 更详细的错误处理
    if (err.response && err.response.data) {
      const errorData = err.response.data
      console.log('错误响应:', errorData)

      if (errorData.message) {
        errorMsg = errorData.message
      } else if (errorData.error) {
        errorMsg = errorData.error
      }

      // 如果有字段验证错误
      if (errorData.errors) {
        const fieldErrors = Object.values(errorData.errors).join(', ')
        errorMsg = `字段错误: ${fieldErrors}`
      }
    } else if (err.message) {
      errorMsg = err.message
    }

    ElMessage.error(errorMsg)
  } finally {
    saving.value = false
  }
}

// 删除用户（软删除）
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除用户 "${row.username}" 吗？`,
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: 'el-button--danger',
          buttonSize: 'default'
        }
    )

    // 发送软删除请求
    const res = await axios.patch(`/users/${row.id}/status/DELETED`)
    console.log('删除用户响应:', res)

    ElMessage.success('用户已删除')

    // 删除后重新加载当前页数据，保持分页稳定
    const currentPageSize = userList.value.length

    // 如果当前页只有一条数据且不是第一页，则返回上一页
    if (currentPageSize === 1 && queryForm.page > 1) {
      queryForm.page -= 1
    }

    // 重新加载数据
    getUserList()

  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除用户失败:', err)
      const errorMsg = err.response?.data?.message || '删除失败'
      ElMessage.error(errorMsg)
    }
  }
}

// 新增：恢复已删除的用户
const handleRecover = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定要恢复用户 "${row.username}" 吗？`,
        '恢复确认',
        {
          confirmButtonText: '确定恢复',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    // 发送恢复请求，将用户状态改为ACTIVE
    const res = await axios.patch(`/users/${row.id}/status/ACTIVE`)
    console.log('恢复用户响应:', res)

    ElMessage.success('用户已恢复')

    // 重新加载数据
    getUserList()

  } catch (err) {
    if (err !== 'cancel') {
      console.error('恢复用户失败:', err)
      const errorMsg = err.response?.data?.message || '恢复失败'
      ElMessage.error(errorMsg)
    }
  }
}
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.query-area {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-start;
}

.query-form .el-form-item {
  margin-bottom: 0;
}

.empty-state {
  padding: 50px 0;
  text-align: center;
  color: #999;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>