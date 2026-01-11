<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <h1>用户注册</h1>
        <p>创建您的图书馆账户</p>
      </div>

      <el-form
          :model="registerForm"
          :rules="registerRules"
          ref="registerFormRef"
          class="register-form"
          @submit.prevent="handleRegister"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item prop="username">
              <el-input
                  v-model="registerForm.username"
                  placeholder="用户名"
                  prefix-icon="User"
                  size="large"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="password">
              <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="密码"
                  prefix-icon="Lock"
                  size="large"
                  show-password
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item prop="realName">
          <el-input
              v-model="registerForm.realName"
              placeholder="真实姓名"
              prefix-icon="Avatar"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
              v-model="registerForm.email"
              placeholder="邮箱"
              prefix-icon="Message"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
              v-model="registerForm.phone"
              placeholder="手机号（可选）"
              prefix-icon="Phone"
              size="large"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item prop="userType">
              <el-select
                  v-model="registerForm.userType"
                  placeholder="用户类型"
                  size="large"
                  style="width: 100%"
              >
                <el-option label="学生" value="STUDENT" />
                <el-option label="教师" value="TEACHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="branchId">
              <el-select
                  v-model="registerForm.branchId"
                  placeholder="选择分馆"
                  size="large"
                  style="width: 100%"
              >
                <el-option label="校本部" :value="1" />
                <el-option label="黄塘校区" :value="2" />
                <el-option label="程江校区" :value="3" />
                <el-option label="江南校区" :value="4" />
                <el-option label="丰顺校区" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>

        <div class="register-footer">
          <span>已有账号？</span>
          <el-button type="text" @click="toLogin">立即登录</el-button>
        </div>
      </el-form>
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

const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  userType: 'STUDENT',
  branchId: 1
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度不能少于3位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
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
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    await userStore.register(registerForm)

    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const toLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
/* 修改点1: 让紫色背景填充整个页面 */
.register-page {
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

/* 修改点2: 调整注册容器样式，确保居中 */
.register-container {
  width: 100%;
  max-width: 800px; /* 最大宽度限制 */
  background: white;
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  padding: 50px;
  margin: 20px; /* 添加外边距确保在小屏幕上不会贴边 */
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.register-header h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 10px;
}

.register-header p {
  color: #666;
  font-size: 16px;
}

.register-form {
  width: 100%;
}

.register-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  margin-top: 20px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.register-footer .el-button {
  padding: 0;
  margin-left: 5px;
}

/* 响应式设计 - 修改点3: 确保在不同设备上都能正常显示 */
@media (max-width: 768px) {
  .register-container {
    width: 95%;
    padding: 30px 20px;
    margin: 10px; /* 小屏幕上的外边距小一些 */
  }

  .el-row {
    flex-direction: column;
  }

  .el-col {
    width: 100%;
    margin-bottom: 20px;
  }
}

/* 平板设备适配 */
@media (min-width: 769px) and (max-width: 1024px) {
  .register-container {
    width: 90%;
    max-width: 90%;
    padding: 40px 30px;
  }
}

/* 极小屏幕适配 */
@media (max-width: 480px) {
  .register-container {
    border-radius: 10px;
    padding: 20px 15px;
  }

  .register-header h1 {
    font-size: 24px;
  }

  .register-header p {
    font-size: 14px;
  }

  .el-col {
    margin-bottom: 15px;
  }
}
</style>