import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 布局组件
import MainLayout from '@/layouts/MainLayout.vue'

// 公共页面
import Home from '@/views/Home.vue'
import Login from '@/views/auth/Login.vue'
import Register from '@/views/auth/Register.vue'

// 社团相关
import ClubList from '@/views/club/ClubList.vue'
import ClubDetail from '@/views/club/ClubDetail.vue'
import MyClubs from '@/views/club/MyClubs.vue'

// 活动相关
import ActivityList from '@/views/activity/ActivityList.vue'
import ActivityDetail from '@/views/activity/ActivityDetail.vue'
import MyActivities from '@/views/activity/MyActivities.vue'

// 个人中心
import Profile from '@/views/profile/Profile.vue'
import Notifications from '@/views/profile/Notifications.vue'

// 管理页面
import UserManage from '@/views/admin/UserManage.vue'
import ClubManage from '@/views/admin/ClubManage.vue'
import ActivityManage from '@/views/admin/ActivityManage.vue'
import ActivityApproval from '@/views/admin/ActivityApproval.vue'
import ClubApproval from '@/views/admin/ClubApproval.vue'
import Statistics from '@/views/admin/Statistics.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { guest: true }
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: Home
      },
      // 社团
      {
        path: 'clubs',
        name: 'ClubList',
        component: ClubList
      },
      {
        path: 'clubs/:id',
        name: 'ClubDetail',
        component: ClubDetail
      },
      {
        path: 'my-clubs',
        name: 'MyClubs',
        component: MyClubs,
        meta: { requiresAuth: true }
      },
      // 活动
      {
        path: 'activities',
        name: 'ActivityList',
        component: ActivityList
      },
      {
        path: 'activities/:id',
        name: 'ActivityDetail',
        component: ActivityDetail
      },
      {
        path: 'my-activities',
        name: 'MyActivities',
        component: MyActivities,
        meta: { requiresAuth: true }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: { requiresAuth: true }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: Notifications,
        meta: { requiresAuth: true }
      },
      // 管理页面
      {
        path: 'admin/club/:id',
        name: 'ClubManage',
        component: ClubManage,
        meta: { requiresAuth: true, requiresClubLeaderOrAdmin: true }
      },
      {
        path: 'admin/activities',
        name: 'ActivityManage',
        component: ActivityManage,
        meta: { requiresAuth: true, requiresClubLeaderOrAdmin: true }
      },
      {
        path: 'admin/activity-approval',
        name: 'ActivityApproval',
        component: ActivityApproval,
        meta: { requiresAuth: true, requiresAdminOrTeacher: true }
      },
      {
        path: 'admin/club-approval',
        name: 'ClubApproval',
        component: ClubApproval,
        meta: { requiresAuth: true, requiresAdminOrTeacher: true }
      },
      {
        path: 'admin/users',
        name: 'UserManage',
        component: UserManage,
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/statistics',
        name: 'Statistics',
        component: Statistics,
        meta: { requiresAuth: true, requiresClubLeaderOrAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.guest && userStore.isLoggedIn) {
    next({ name: 'Home' })
  } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    ElMessage.error('只有管理员可以访问该页面')
    next({ name: 'Home' })
  } else if (to.meta.requiresClubLeaderOrAdmin && !userStore.isClubLeaderOrAdmin) {
    ElMessage.error('只有社团负责人或管理员可以访问该页面')
    next({ name: 'Home' })
  } else if (to.meta.requiresAdminOrTeacher && !userStore.isAdminOrTeacher) {
    ElMessage.error('只有管理员或指导老师可以访问该页面')
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
