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

          <!-- 我的空间 - 拆分为独立页面 -->
          <el-sub-menu index="my-space">
            <template #title>
              <el-icon><User /></el-icon>
              <span>我的空间</span>
            </template>
            <el-menu-item index="/home/borrow-records">借阅记录</el-menu-item>
            <el-menu-item index="/home/my-reservations">预定记录</el-menu-item>
            <el-menu-item index="/home/my-fines">罚款记录</el-menu-item>
            <el-menu-item index="/home/my-notifications">我的通知</el-menu-item>
          </el-sub-menu>

          <!-- 管理员模块 -->
          <el-sub-menu index="admin" v-if="hasAdminRole">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>管理员模块</span>
            </template>
            <el-menu-item index="/home/user-management"
                          v-if="hasSystemOrBranchRole">
              用户管理
            </el-menu-item>
            <el-menu-item index="/home/book-management"
                          v-if="hasSystemOrBranchRole">
              图书管理
            </el-menu-item>
            <el-menu-item index="/home/borrow-management"
                          v-if="hasSystemOrBranchRole">
              借阅管理
            </el-menu-item>
            <el-menu-item index="/home/notification-management"
                          v-if="hasSystemRole">
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
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {Document, User, Setting} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)

// 角色权限
const hasAdminRole = computed(() => userStore.isSystemAdmin || userStore.isBranchAdmin)
const hasSystemRole = computed(() => userStore.isSystemAdmin)
const hasSystemOrBranchRole = computed(() => userStore.isSystemAdmin || userStore.isBranchAdmin)

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
</script>

<style scoped>
.home-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #4d6688;
  display: flex;
  box-sizing: border-box;
}

.home-container {
  width: 100%;
  height: 100%;
  display: flex;
  background: #f5f7fa;
  box-sizing: border-box;
}

.home-left {
  width: 280px;
  background: #4d6688;
  color: #fff;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  position: relative;
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

.el-menu-vertical-demo ,
.el-menu-vertical-demo  {
  font-size: 14px;
  padding-left: 15px;
  display: flex;
  align-items: center;
  white-space: normal;
}

.el-menu-vertical-demo  {
  font-size: 18px;
  margin-right: 10px;
}

.el-menu-vertical-demo .el-sub-menu {
  padding-left: 35px;
  display: block;
}

.home-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  height: 100%;
  width: calc(100% - 280px);
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
  flex-shrink: 0;
}

.header el-button {
  color: white;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  height: calc(100% - 60px);
}

@media (max-width: 768px) {
  .home-left {
    width: 220px;
    padding: 20px 10px;
  }

  .home-right {
    width: calc(100% - 220px);
  }
}
</style>