import { defineStore } from 'pinia'
import { login, register, getCurrentUser } from '@/api/user'
import { getUnreadCount } from '@/api/notification'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: null,
    user: null,
    unreadCount: 0
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'ADMIN',
    isClubLeader: (state) => state.user?.role === 'CLUB_LEADER',
    isTeacher: (state) => state.user?.role === 'TEACHER',
    isClubLeaderOrAdmin: (state) => ['ADMIN', 'CLUB_LEADER'].includes(state.user?.role),
    isAdminOrTeacher: (state) => ['ADMIN', 'TEACHER'].includes(state.user?.role),
    userRole: (state) => state.user?.role || 'USER',
    hasPermission: (state) => (roles) => {
      if (!state.token || !state.user) return false
      if (Array.isArray(roles)) {
        return roles.includes(state.user.role)
      }
      return state.user.role === roles
    }
  },
  
  actions: {
    // 初始化
    initFromStorage() {
      const token = localStorage.getItem('token')
      const user = localStorage.getItem('user')
      
      if (token && user) {
        this.token = token
        this.user = JSON.parse(user)
        this.fetchUnreadCount()
        this.refreshCurrentUser()
      }
    },
    
    // 登录
    async login(username, password) {
      const res = await login({ username, password })
      await this.setAuth(res.data)
      return res
    },
    
    // 注册
    async register(data) {
      const res = await register(data)
      await this.setAuth(res.data)
      return res
    },
    
    // 设置认证信息
    async setAuth(authData) {
      this.token = authData.token
      this.user = authData.user
      
      localStorage.setItem('token', authData.token)
      localStorage.setItem('refreshToken', authData.refreshToken)
      localStorage.setItem('user', JSON.stringify(authData.user))
      
      await this.refreshCurrentUser()
      this.fetchUnreadCount()
    },
    
    // 登出
    logout() {
      this.token = null
      this.user = null
      this.unreadCount = 0
      
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    },
    
    // 更新用户信息
    updateUser(userData) {
      this.user = { ...this.user, ...userData }
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    
    // 获取未读通知数
    async fetchUnreadCount() {
      if (!this.token) return
      
      try {
        const res = await getUnreadCount()
        this.unreadCount = res.data
      } catch (e) {
        console.error('获取未读通知数失败', e)
      }
    },

    async refreshCurrentUser() {
      if (!this.token) return

      try {
        const res = await getCurrentUser()
        this.user = res.data
        localStorage.setItem('user', JSON.stringify(this.user))
      } catch (e) {
        console.error('获取用户信息失败', e)
      }
    },

    // alias for components that call fetchUser
    async fetchUser() {
      return this.refreshCurrentUser()
    }
  }
})
