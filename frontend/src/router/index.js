// src/router/index.js

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 懒加载路由组件
const Login = () => import('@/views/Login.vue')
const Register = () => import('@/views/Register.vue')
const Home = () => import('@/views/Home.vue')
const BookQuery = () => import('@/views/book/BookQuery.vue')
const BookDetail = () => import('@/views/book/BookDetail.vue')
const UserManagement = () => import('@/views/user/UserManagement.vue')

// 拆分的独立页面
const BorrowRecords = () => import('@/views/user/BorrowRecords.vue')
const Reservations = () => import('@/views/user/MyReservations.vue')
const Fines = () => import('@/views/user/MyFines.vue')
const UserNotifications = () => import('@/views/user/MyNotifications.vue')

const NotificationManagement = () => import('@/views/notification/NotificationManagement.vue')
const BookManagement = () => import('@/views/admin/BookManagement.vue')
const BorrowManagement = () => import('@/views/admin/BorrowManagement.vue')

const routes = [
    {
        path: '/',
        redirect: '/home/my-notifications'
    },
    {
        path: '/login',
        component: Login,
        name: 'Login',
        meta: { title: '登录' }
    },
    {
        path: '/register',
        component: Register,
        name: 'Register',
        meta: { title: '注册' }
    },
    {
        path: '/home',
        component: Home,
        name: 'Home',
        meta: { requiresAuth: true },
        children: [
            // 公共模块
            {
                path: 'book-query',
                component: BookQuery,
                name: 'BookQuery',
                meta: { title: '图书查询' }
            },
            {
                path: 'book-detail/:id',
                component: BookDetail,
                name: 'BookDetail',
                meta: { title: '图书详情' }
            },

            // 管理员模块
            {
                path: 'user-management',
                component: UserManagement,
                name: 'UserManagement',
                meta: {
                    title: '用户管理',
                    requireRole: ['ROLE_SYSTEM_ADMIN', 'ROLE_BRANCH_ADMIN']
                }
            },
            {
                path: 'notification-management',
                component: NotificationManagement,
                name: 'NotificationManagement',
                meta: {
                    title: '通知管理',
                    requireRole: ['ROLE_SYSTEM_ADMIN']
                }
            },
            {
                path: 'book-management',
                component: BookManagement,
                name: 'BookManagement',
                meta: {
                    title: '图书管理',
                    requireRole: ['ROLE_SYSTEM_ADMIN', 'ROLE_BRANCH_ADMIN']
                }
            },
            {
                path: 'borrow-management',
                component: BorrowManagement,
                name: 'BorrowManagement',
                meta: {
                    title: '借阅管理',
                    requireRole: ['ROLE_SYSTEM_ADMIN', 'ROLE_BRANCH_ADMIN']
                }
            },

            // 用户个人中心 - 拆分为独立页面
            {
                path: 'borrow-records',
                component: BorrowRecords,
                name: 'BorrowRecords',
                meta: { title: '借阅记录' }
            },
            {
                path: 'my-reservations',
                component: Reservations,
                name: 'MyReservations',
                meta: { title: '预定记录' }
            },
            {
                path: 'my-fines',
                component: Fines,
                name: 'MyFines',
                meta: { title: '罚款记录' }
            },
            {
                path: 'my-notifications',
                component: UserNotifications,
                name: 'MyNotifications',
                meta: { title: '我的通知' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局路由守卫
router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()

    // 设置页面标题
    if (to.meta.title) {
        document.title = `${to.meta.title} - 图书馆管理系统`
    }

    // 公开页面直接放行
    if (to.path === '/login' || to.path === '/register') {
        next()
        return
    }

    // 检查是否需要认证
    if (to.meta.requiresAuth) {
        // 检查是否有Basic Auth凭据
        const authString = localStorage.getItem('basic_auth')
        if (!authString) {
            ElMessage.warning('请先登录')
            next('/login')
            return
        }

        // 如果用户信息不存在，尝试获取
        if (!userStore.userInfo) {
            try {
                await userStore.fetchCurrentUser()
            } catch (error) {
                // 如果获取失败，清除认证信息并跳转到登录页
                userStore.logout()
                ElMessage.warning('登录已过期，请重新登录')
                next('/login')
                return
            }
        }

        // 检查角色权限
        const requireRole = to.meta.requireRole
        if (requireRole && requireRole.length > 0) {
            if (!requireRole.includes(userStore.role)) {
                ElMessage.error('无权限访问该页面')
                next('/home')
                return
            }
        }
    }
    next()
})

export default router