<template>
  <div class="user-management">
    <el-card>
      <div class="card-header">
        <h2>用户管理</h2>
        <el-button type="primary" @click="handleAddUser">新增用户</el-button>
      </div>

      <el-table :data="userList" border>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'ROLE_SYSTEM_ADMIN' ? 'danger' : 'warning'">
              {{ scope.row.role === 'ROLE_SYSTEM_ADMIN' ? '系统管理员' : '分馆管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-switch
                v-model="scope.row.status"
                active-value="ACTIVE"
                inactive-value="INACTIVE"
                @change="handleStatusChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" text color="red" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog v-model="dialogVisible" title="用户信息">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role">
            <el-option label="系统管理员" value="ROLE_SYSTEM_ADMIN"></el-option>
            <el-option label="分馆管理员" value="ROLE_BRANCH_ADMIN"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, updateUserStatus, deleteUser, saveUser } from '@/api/user' // 假设的API

const userList = ref([])
const dialogVisible = ref(false)
const form = ref({
  id: null,
  username: '',
  name: '',
  role: 'ROLE_BRANCH_ADMIN'
})

// 获取用户列表
onMounted(async () => {
  try {
    const res = await getUserList()
    userList.value = res.data
  } catch (err) {
    ElMessage.error('获取用户列表失败')
  }
})

// 状态变更
const handleStatusChange = async (row) => {
  try {
    await updateUserStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (err) {
    ElMessage.error('状态更新失败')
    row.status = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' // 回滚
  }
}

// 编辑用户
const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

// 新增用户
const handleAddUser = () => {
  form.value = { id: null, username: '', name: '', role: 'ROLE_BRANCH_ADMIN' }
  dialogVisible.value = true
}

// 保存用户
const handleSave = async () => {
  try {
    if (form.value.id) {
      await saveUser(form.value) // 编辑
    } else {
      await saveUser(form.value) // 新增
    }
    dialogVisible.value = false
    ElMessage.success('操作成功')
    // 重新加载列表
    const res = await getUserList()
    userList.value = res.data
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    userList.value = userList.value.filter(item => item.id !== row.id)
  } catch (err) {
    ElMessage.error('删除失败')
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
</style>