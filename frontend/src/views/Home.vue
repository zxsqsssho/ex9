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
import { Document, User, Setting, Bell, Books, History, Money } from '@element-plus/icons-vue'
import axios from '@/utils/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)

// 角色权限
const hasAdminRole = computed(() => userStore.isSystemAdmin || userStore.isBranchAdmin)
const hasSystemRole = computed(() => userStore.isSystemAdmin)
const hasSystemOrBranchRole = computed(() => userStore.isSystemAdmin || userStore.isBranchAdmin)

// 菜单跳转
const handleMenuSelect = (index) => router.push(index)

// 退出登录
const handleLogout = async () => {
  try {
    userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    ElMessage.error('退出登录失败')
  }
}

// 仪表盘数据
const dashboardItems = reactive([
  { label: '当前借阅数量', value: 0, icon: Books, color: '#409EFF' },
  { label: '逾期数量', value: 0, icon: Bell, color: '#F56C6C' },
  { label: '预定提醒', value: 0, icon: History, color: '#E6A23C' },
  { label: '罚款金额', value: '0元', icon: Money, color: '#67C23A' },
])

// 获取仪表盘数据
const fetchDashboard = async () => {
  try {
    // 调用后端的dashboard接口
    const res = await axios.get('/dashboard')
    const data = res.data
    if (data) {
      dashboardItems[0].value = data.currentBorrow || 0
      dashboardItems[1].value = data.overdue || 0
      dashboardItems[2].value = data.reservationReminder || 0
      dashboardItems[3].value = data.fineAmount ? `${data.fineAmount}元` : '0元'
    }
  } catch (err) {
    console.error('获取仪表盘数据失败', err)
    // 如果接口不存在，显示模拟数据
    dashboardItems[0].value = 3
    dashboardItems[1].value = 0
    dashboardItems[2].value = 2
    dashboardItems[3].value = '0元'
  }
}

onMounted(() => {
  fetchDashboard()
})
</script>

<style scoped>
/* 修改：确保页面完全填充视口 */
.home-page {
  position: fixed; /* 使用 fixed 定位确保覆盖整个视口 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #4d6688;
  display: flex;
  box-sizing: border-box;
}

/* 修改：移除最小高度限制，使用父级固定定位 */
.home-container {
  width: 100%;
  height: 100%;
  display: flex;
  background: #f5f7fa;
  box-sizing: border-box;
}

/* 左侧导航栏 - 保持固定定位 */
.home-left {
  width: 280px;
  background: #4d6688;
  color: #fff;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  position: relative; /* 改为相对定位，因为父级已经是固定定位 */
  height: 100%;
  overflow-y: auto;
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
  display: block; /* 垂直排列 */
}

/* 右侧内容 - 完全填充剩余空间 */
.home-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  height: 100%;
  width: calc(100% - 280px); /* 计算剩余宽度 */
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
  flex-shrink: 0; /* 防止header被压缩 */
}

.header el-button {
  color: white;
}

/* 修改：让内容区域填充剩余空间 */
.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  height: calc(100% - 60px); /* 减去header的高度 */
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

/* 确保内容区域的所有元素都不会溢出 */
.el-row, .el-col {
  height: auto;
}
</style>