<!--frontend/src/views/Login.vue-->
<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧系统说明 -->
      <div class="login-left">
        <h1>图书借阅管理系统</h1>
        <p>Library Borrowing Management System</p>
        <div class="features">
          <div class="feature-item">
            <i class="el-icon-reading"></i>
            <span>多分馆图书查询</span>
          </div>
          <div class="feature-item">
            <i class="el-icon-user"></i>
            <span>角色权限管理</span>
          </div>
          <div class="feature-item">
            <i class="el-icon-bell"></i>
            <span>智能提醒通知</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-right">
        <div class="login-header">
          <h2>用户登录</h2>
          <p>请使用您的账号密码登录系统</p>
        </div>

        <el-form
            :model="loginForm"
            :rules="loginRules"
            ref="loginFormRef"
            class="login-form"
            @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                size="large"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
                type="primary"
                size="large"
                class="login-btn"
                :loading="loading"
                @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>

          <div class="login-footer">
            <span>还没有账号？</span>
            <el-button type="text" @click="toRegister">立即注册</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    // 调用user store的登录方法
    await userStore.login(loginForm)

    ElMessage.success('登录成功')
    router.push('/home/my-notifications')
  } catch (error) {
    console.error('登录失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('用户名或密码错误')
    } else {
      ElMessage.error(error.message || '登录失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const toRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
/* 修改点1: 让紫色背景填充整个页面 */
.login-page {
  width: 100%;
  height: 100vh; /* 使用视口高度确保填充整个屏幕 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0; /* 移除padding，确保背景完全填充 */
  margin: 0;
  position: fixed; /* 使用fixed定位确保覆盖整个窗口 */
  top: 0;
  left: 0;
  overflow: auto; /* 允许滚动如果内容超出 */
}

/* 修改点2: 调整容器样式，确保居中 */
.login-container {
  width: 100%;
  max-width: 900px; /* 最大宽度限制 */
  background: white;
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  display: flex;
  min-height: 500px;
  margin: 20px; /* 在小屏幕上添加一些外边距 */
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #2b5876, #4e4376);
  color: white;
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-left h1 {
  font-size: 32px;
  margin-bottom: 15px;
  font-weight: 600;
}

.login-left p {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.features {
  margin-top: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
  font-size: 16px;
}

.feature-item i {
  font-size: 24px;
  margin-right: 15px;
  color: #ffd04b;
}

.login-right {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  margin-top: 10px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.login-footer  {
  padding: 0;
  margin-left: 5px;
}

/* 响应式设计 - 修改点3: 确保在不同设备上都能居中 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    width: 95%;
    max-width: 95%;
    margin: 10px;
  }

  .login-left {
    display: block; /* 在小屏幕上显示左侧区域 */
    padding: 30px 20px;
  }

  .login-right {
    padding: 40px 20px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .login-container {
    width: 90%;
    max-width: 90%;
  }
}

/* 确保在极小的屏幕上也能正常显示 */
@media (max-width: 480px) {
  .login-container {
    border-radius: 10px;
    min-height: auto;
  }

  .login-left,
  .login-right {
    padding: 20px 15px;
  }

  .login-left h1 {
    font-size: 24px;
  }

  .login-header h2 {
    font-size: 22px;
  }
}
</style>