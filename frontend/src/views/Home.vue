<template>
  <div class="home-page">
    <div class="home-container">
      <!-- 左侧导航栏 -->
      <div class="home-left">
        <!-- 系统LOGO -->
        <div class="logo">
          <div class="title">图书借阅管理系统</div>
          <div class="subtitle">Library Borrowing Management System</div>
        </div>

        <!-- 导航菜单 -->
        <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical-demo"
            background-color="#4d6688"
            text-color="#fff"
            active-text-color="#ffd04b"
            unique-opened
            router
        >

        <!-- 公共模块 -->
          <el-sub-menu index="public">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>图书查询</span>
            </template>
            <el-menu-item index="/home/book-query">查询图书</el-menu-item>
          </el-sub-menu>

          <!-- 我的空间 -->
          <el-sub-menu index="my-space">
            <template #title>
              <el-icon><User /></el-icon>
              <span>我的空间</span>
            </template>
            <el-menu-item index="/home/my-space">借阅记录</el-menu-item>
            <el-menu-item index="/home/my-reservations">预定记录</el-menu-item>
            <el-menu-item index="/home/my-fines">罚款记录</el-menu-item>
          </el-sub-menu>

          <!-- 管理员模块 -->
          <el-sub-menu index="admin" v-if="hasAdminRole">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>管理员模块</span>
            </template>
            <el-menu-item index="/home/user-management" v-if="hasSystemOrBranchRole">
              用户管理
            </el-menu-item>
            <el-menu-item index="/home/book-management" v-if="hasSystemOrBranchRole">
              图书管理
            </el-menu-item>
            <el-menu-item index="/home/borrow-management" v-if="hasSystemOrBranchRole">
              借阅管理
            </el-menu-item>
            <el-menu-item index="/home/notification-management" v-if="hasSystemRole">
              通知管理
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>

      <!-- 右侧内容区 -->
      <div class="home-right">
        <header class="header">
          <span>欢迎，{{ userStore.currentUser?.realName || userStore.userName }}</span>
          <el-button type="text" @click="handleLogout">退出登录</el-button>
        </header>

        <div class="content">
          <!-- 仪表盘 -->
          <div class="dashboard">
            <el-row :gutter="20">
              <el-col :span="6" v-for="item in dashboardItems" :key="item.label">
                <el-card shadow="hover" class="dashboard-card">
                  <div class="card-label">{{ item.label }}</div>
                  <div class="card-value">{{ item.value }}</div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Document, User, Setting } from '@element-plus/icons-vue'

import axios from 'axios'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const activeMenu = computed(() => route.path)

// 角色权限
const hasAdminRole = computed(() => ['ROLE_SYSTEM_ADMIN', 'ROLE_BRANCH_ADMIN'].includes(userStore.role))
const hasSystemRole = computed(() => userStore.role === 'ROLE_SYSTEM_ADMIN')
const hasSystemOrBranchRole = computed(() => ['ROLE_SYSTEM_ADMIN', 'ROLE_BRANCH_ADMIN'].includes(userStore.role))


// 菜单跳转
const handleMenuSelect = (index) => router.push(index)

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 仪表盘数据
const dashboardItems = reactive([
  { label: '当前借阅数量', value: 0 },
  { label: '逾期数量', value: 0 },
  { label: '预定提醒', value: 0 },
  { label: '可借图书总数', value: 0 },
])

// 获取仪表盘数据
const fetchDashboard = async () => {
  try {
    const res = await axios.get('/api/dashboard')
    dashboardItems[0].value = res.data.currentBorrow || 0
    dashboardItems[1].value = res.data.overdue || 0
    dashboardItems[2].value = res.data.reservationReminder || 0
    dashboardItems[3].value = res.data.availableBooks || 0
  } catch (err) {
    console.error('获取仪表盘数据失败', err)
  }
}

onMounted(() => {
  fetchDashboard()
})
</script>

<style scoped>
.home-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #2b5876, #4e4376);
  box-sizing: border-box;
}

.home-container {
  width: 1400px;
  max-width: 90%;
  height: 600px;
  display: flex;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
}

/* 左侧导航栏 */
.home-left {
  width: 280px;
  background: #4d6688;
  color: #fff;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
}

.logo {
  text-align: center;
  margin-bottom: 40px;
}

.logo .title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 5px;
  line-height: 1.2;
}

.logo .subtitle {
  font-size: 12px;
  color: #c0c0c0;
  line-height: 1.2;
}

/* 菜单文字与图标 */
.el-menu-vertical-demo .el-menu-item,
.el-menu-vertical-demo .el-sub-menu__title {
  font-size: 14px;
  padding-left: 15px;
  display: flex;
  align-items: center;
  white-space: normal;
}

.el-menu-vertical-demo .el-icon {
  font-size: 18px;
  margin-right: 10px;
}

.el-menu-vertical-demo .el-sub-menu .el-menu-item {
  padding-left: 35px; /* 缩进 */
  display: block;       /* 垂直排列 */
}

/* 右侧内容 */
.home-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.header {
  height: 60px;
  background: #497ab1;
  color: white;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
  font-weight: 500;
}

.header el-button {
  color: white;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 仪表盘 */
.dashboard {
  margin-bottom: 20px;
}

.dashboard-card {
  text-align: center;
  padding: 20px 0;
  border-radius: 8px;
  background: #fff;
  transition: all 0.3s;
}

.dashboard-card:hover {
  transform: translateY(-5px);
}

.card-label {
  font-size: 14px;
  color: #888;
  margin-bottom: 10px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}
</style>
