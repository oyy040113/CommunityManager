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
    isClubLeader: (state) => state.user?.role === 'CLUB_LEADER'
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
      }
    },
    
    // 登录
    async login(username, password) {
      const res = await login({ username, password })
      this.setAuth(res.data)
      return res
    },
    
    // 注册
    async register(data) {
      const res = await register(data)
      this.setAuth(res.data)
      return res
    },
    
    // 设置认证信息
    setAuth(authData) {
      this.token = authData.token
      this.user = authData.user
      
      localStorage.setItem('token', authData.token)
      localStorage.setItem('refreshToken', authData.refreshToken)
      localStorage.setItem('user', JSON.stringify(authData.user))
      
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
    }
  }
})
