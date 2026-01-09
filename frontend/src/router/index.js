import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 懒加载路由组件
const Login = () => import('@/views/Login.vue')
const Home = () => import('@/views/Home.vue')

// 人员A：图书查询模块
const BookQuery = () => import('@/views/book/BookQuery.vue')
const BookDetail = () => import('@/views/book/BookDetail.vue')

// 人员B：用户/权限/通知模块
const UserManagement = () => import('@/views/user/UserManagement.vue')
const NotificationManagement = () => import('@/views/notification/NotificationManagement.vue')

// 人员C：图书/借阅管理模块
const BookManagement = () => import('@/views/admin/BookManagement.vue')
const BorrowManagement = () => import('@/views/admin/BorrowManagement.vue')

const MySpace = () => import('@/views/user/MySpace.vue')

const MyReservations = () => import('@/views/user/MyReservations.vue')
const MyFines = () => import('@/views/user/MyFines.vue')


const routes = [
    {
        path: '/',
        redirect: '/home' // ⭐ 不再根据 token 判断
    },
    { path: '/login', component: Login, name: 'Login' },
    {
        path: '/home',
        component: Home,
        name: 'Home',
        meta: { requiresAuth: true },
        children: [
            // 公共模块（人员A）
            { path: 'book-query', component: BookQuery, name: 'BookQuery', meta: { title: '图书查询' } },
            { path: 'book-detail/:id', component: BookDetail, name: 'BookDetail', meta: { title: '图书详情' } },

            // 管理员模块（人员B）
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

            // 管理员模块（人员C）
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

            // 普通用户模块
            { path: 'my-space', component: MySpace, name: 'MySpace', meta: { title: '我的空间' } },
            { path: 'my-reservations', component: MyReservations, name: 'MyReservations', meta: { title: '我的预定记录' } },
            { path: 'my-fines', component: MyFines, name: 'MyFines', meta: { title: '我的罚款记录' } },




        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

/**
 * ⭐ 全局路由守卫（核心）
 * - 不再使用 token
 * - 使用 current-user 接口确认是否登录
 */
router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()

    // 访问登录页，直接放行
    if (to.path === '/login') {
        next()
        return
    }

    // 需要登录的页面
    if (to.meta.requiresAuth) {
        // 本地没有用户信息 → 向后端确认会话
        if (!userStore.userInfo) {
            try {
                await userStore.fetchCurrentUser()
            } catch (e) {
                ElMessage.warning('请先登录')
                next('/login')
                return
            }
        }

        // ⭐ 角色校验
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
