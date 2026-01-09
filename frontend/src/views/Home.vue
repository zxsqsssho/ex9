<template>
  <div class="home-container">
    <!-- 侧边栏导航 -->
    <aside class="sidebar">
      <div class="logo">图书馆管理系统</div>
      <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-demo"
          @select="handleMenuSelect"
      >
        <!-- 公共模块 -->
        <el-sub-menu index="public">
          <template #title>
            <el-icon><document /></el-icon>
            <span>公共模块</span>
          </template>
          <el-menu-item index="/home/book-query">图书查询</el-menu-item>
        </el-sub-menu>

        <!-- 普通用户模块 -->
        <el-menu-item index="/home/my-space">
          <el-icon><user /></el-icon>
          <span>我的空间</span>
        </el-menu-item>

        <!-- 管理员模块（根据角色显示） -->
        <el-sub-menu index="admin" v-if="hasAdminRole">
          <template #title>
            <el-icon><setting /></el-icon>
            <span>管理员模块</span>
          </template>
          <el-menu-item index="/home/user-management" v-if="hasSystemOrBranchRole">用户管理</el-menu-item>
          <el-menu-item index="/home/notification-management" v-if="hasSystemRole">通知管理</el-menu-item>
          <el-menu-item index="/home/book-management" v-if="hasSystemOrBranchRole">图书管理</el-menu-item>
          <el-menu-item index="/home/borrow-management" v-if="hasSystemOrBranchRole">借阅管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="user-info">
          <span>欢迎，{{ userStore.userName }}</span>
          <el-button type="text" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <!-- 路由视图 -->
      <router-view class="content" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Document, User, Setting } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

// 活跃菜单
const activeMenu = computed(() => route.path)

// 角色权限判断
const hasAdminRole = computed(() => {
  return ['ROLE_SYSTEM_ADMIN', 'ROLE_BRANCH_ADMIN'].includes(userStore.userRole)
})
const hasSystemRole = computed(() => userStore.userRole === 'ROLE_SYSTEM_ADMIN')
const hasSystemOrBranchRole = computed(() => {
  return ['ROLE_SYSTEM_ADMIN', 'ROLE_BRANCH_ADMIN'].includes(userStore.userRole)
})

// 菜单选择事件
const handleMenuSelect = (index) => {
  router.push(index)
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 200px;
  background: #2c3e50;
  color: white;
  height: 100%;
}

.logo {
  text-align: center;
  padding: 20px;
  font-size: 18px;
  border-bottom: 1px solid #34495e;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background: #ecf0f1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #ddd;
}

.user-info {
  display: flex;
  gap: 20px;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>