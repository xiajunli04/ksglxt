import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Landing',
      component: () => import('@/views/LandingView.vue'),
      meta: { title: '广软课室申请系统' },
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录', guest: true },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { title: '注册', guest: true },
    },
    {
      path: '/admin',
      component: () => import('@/layout/MainLayout.vue'),
      redirect: '/admin/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '首页', auth: true },
        },
        {
          path: 'classroom',
          name: 'ClassroomList',
          component: () => import('@/views/ClassroomListView.vue'),
          meta: { title: '课室管理', auth: true },
        },
        {
          path: 'classroom/add',
          name: 'ClassroomAdd',
          component: () => import('@/views/ClassroomDetailView.vue'),
          meta: { title: '新增课室', auth: true },
        },
        {
          path: 'classroom/:id',
          name: 'ClassroomDetail',
          component: () => import('@/views/ClassroomDetailView.vue'),
          meta: { title: '课室详情', auth: true },
        },
        {
          path: 'booking/my',
          name: 'MyBooking',
          component: () => import('@/views/MyBookingView.vue'),
          meta: { title: '我的申请', auth: true, roles: ['TEACHER', 'STUDENT'] },
        },
        {
          path: 'booking/create',
          name: 'BookingCreate',
          component: () => import('@/views/BookingCreateView.vue'),
          meta: { title: '新建申请', auth: true, roles: ['TEACHER', 'STUDENT', 'ADMIN'] },
        },
        {
          path: 'booking/:id',
          name: 'BookingDetail',
          component: () => import('@/views/BookingDetailView.vue'),
          meta: { title: '申请详情', auth: true },
        },
        {
          path: 'approval/pending',
          name: 'ApprovalPending',
          component: () => import('@/views/ApprovalPendingView.vue'),
          meta: { title: '待审批', auth: true, roles: ['ADMIN'] },
        },
        {
          path: 'approval/all',
          name: 'ApprovalAll',
          component: () => import('@/views/ApprovalAllView.vue'),
          meta: { title: '全部申请', auth: true, roles: ['ADMIN'] },
        },
        {
          path: 'announcement',
          name: 'AnnouncementList',
          component: () => import('@/views/AnnouncementListView.vue'),
          meta: { title: '公告通知', auth: true },
        },
        {
          path: 'announcement/add',
          name: 'AnnouncementAdd',
          component: () => import('@/views/AnnouncementDetailView.vue'),
          meta: { title: '新增公告', auth: true },
        },
        {
          path: 'announcement/:id',
          name: 'AnnouncementDetail',
          component: () => import('@/views/AnnouncementDetailView.vue'),
          meta: { title: '公告详情', auth: true },
        },
        {
          path: 'notification',
          name: 'Notification',
          component: () => import('@/views/NotificationView.vue'),
          meta: { title: '我的消息', auth: true },
        },
        {
          path: 'user/list',
          name: 'UserList',
          component: () => import('@/views/UserListView.vue'),
          meta: { title: '用户管理', auth: true, roles: ['ADMIN'] },
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/ProfileView.vue'),
          meta: { title: '个人中心', auth: true },
        },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')

  // Set page title
  if (to.meta.title) {
    document.title = `${to.meta.title} - 广软课室申请系统`
  }

  // Landing page (/) is always accessible
  if (to.path === '/') {
    next()
    return
  }

  // Guest-only routes (login, register)
  if (to.meta.guest) {
    if (token) {
      next('/admin/dashboard')
      return
    }
    next()
    return
  }

  // Protected routes (require auth)
  if (to.meta.auth) {
    if (!token) {
      next('/login')
      return
    }

    // Role check
    if (to.meta.roles) {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo)
          const roles = (user as Record<string, unknown>).roles as string[]
          const primaryRole = roles && roles.length > 0 ? roles[0] : ''
          const allowedRoles = to.meta.roles as string[]
          // Admin can access all
          if (primaryRole === 'ADMIN') {
            next()
            return
          }
          if (!allowedRoles.some(r => roles?.includes(r.toUpperCase()) || roles?.includes(r))) {
            next('/admin/dashboard')
            return
          }
        } catch {
          // ignore parse errors
        }
      }
    }

    next()
    return
  }

  next()
})

export default router
