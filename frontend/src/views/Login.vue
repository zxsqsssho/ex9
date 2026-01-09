<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">图书借阅系统</h2>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="loginForm.userType" placeholder="请选择用户类型">
            <el-option label="学生" value="STUDENT"></el-option>
            <el-option label="教师" value="TEACHER"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const loginFormRef = ref(null)
const router = useRouter()
const userStore = useUserStore()

// 登录表单
const loginForm = ref({
  username: '',
  password: '',
  userType: ''
})

// 校验规则
const loginRules = ref({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
})

// 登录方法
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    const res = await userStore.login(loginForm.value)
    if (res) {
      ElMessage.success('登录成功')
      router.push('/home/book-query')
    }
  } catch (error) {
    ElMessage.error('表单校验失败，请检查输入')
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
}
.login-card {
  width: 400px;
  padding: 20px;
}
.login-title {
  text-align: center;
  margin-bottom: 20px;
  color: #1989fa;
}
.login-btn {
  width: 100%;
}
</style>