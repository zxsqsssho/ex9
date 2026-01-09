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

const routes = [
    {
        path: '/',
        redirect: () => {
            const userStore = useUserStore()
            return userStore.token ? '/home' : '/login'
        }
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
            { path: 'user-management', component: UserManagement, name: 'UserManagement', meta: { title: '用户管理', requireRole: ['ROLE_SYSTEM_ADMIN', 'ROLE_BRANCH_ADMIN'] } },
            { path: 'notification-management', component: NotificationManagement, name: 'NotificationManagement', meta: { title: '通知管理', requireRole: ['ROLE_SYSTEM_ADMIN'] } },
            // 管理员模块（人员C）
            { path: 'book-management', component: BookManagement, name: 'BookManagement', meta: { title: '图书管理', requireRole: ['ROLE_SYSTEM_ADMIN', 'ROLE_BRANCH_ADMIN'] } },
            { path: 'borrow-management', component: BorrowManagement, name: 'BorrowManagement', meta: { title: '借阅管理', requireRole: ['ROLE_SYSTEM_ADMIN', 'ROLE_BRANCH_ADMIN'] } },
            // 普通用户模块
            { path: 'my-space', component: MySpace, name: 'MySpace', meta: { title: '我的空间' } }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

// 路由守卫：权限校验
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    if (to.meta.requiresAuth && !userStore.token) {
        ElMessage.warning('请先登录！')
        next('/login')
        return
    }
    next() // 必须调用next()，否则路由不会跳转
})

export default router