<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧系统说明 -->
      <div class="login-left">
        <h1>图书借阅管理系统</h1>
        <p>Library Borrowing Management System</p>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-right">
        <h2>用户登录</h2>

        <form @submit.prevent="handleLogin">
          <div class="form-item">
            <input
                type="text"
                v-model="form.username"
                placeholder="用户名"
            />
          </div>

          <div class="form-item">
            <input
                type="password"
                v-model="form.password"
                placeholder="密码"
            />
          </div>

          <button class="login-btn" type="submit">
            登录
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.replace('/home') // ⭐
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  }
}

</script>




<style scoped>

/* 登录页 */
.login-page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #2b5876, #4e4376);
  display: flex;
  justify-content: center;
  align-items: center;

  box-sizing: border-box;
}


/* 登录容器 */
.login-container {
  width: 1395px;
  max-width: 90%;
  height: 420px;
  background: #fff;
  display: flex;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
}

/* 左侧介绍 */
.login-left {
  flex: 1;
  background: #34495e;
  color: #fff;
  padding: 60px 40px;
}

.login-left h1 {
  font-size: 28px;
  margin-bottom: 20px;
}

.login-left p {
  font-size: 14px;
  opacity: 0.8;
}

/* 右侧登录 */
.login-right {
  flex: 1;
  padding: 60px 40px;
}

.login-right h2 {
  margin-bottom: 30px;
  font-size: 22px;
  color: #333;
}

.form-item {
  margin-bottom: 20px;
}

.form-item input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

.form-item input:focus {
  outline: none;
  border-color: #4e4376;
}

.login-btn {
  width: 100%;
  height: 42px;
  background: #4e4376;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.login-btn:hover {
  background: #3b2f5c;
}

/* 小屏幕兼容（但不是竖屏风） */
@media screen and (max-width: 1000px) {
  .login-container {
    width: 90%;
    height: auto;
  }

  .login-left {
    display: none;
  }
}
</style>
