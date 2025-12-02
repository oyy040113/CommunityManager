<template>
  <div class="main-layout">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon size="24"><School /></el-icon>
          <span>学生社团信息整理系统</span>
        </div>
        
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          :ellipsis="false"
          class="nav-menu"
          router
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/clubs">社团列表</el-menu-item>
          <el-menu-item index="/activities">活动中心</el-menu-item>
          <template v-if="userStore.isLoggedIn && userStore.user?.role === 'LEADER'">
            <el-sub-menu index="admin">
              <template #title>管理中心</template>
              <el-menu-item index="/admin/activities">活动管理</el-menu-item>
              <el-menu-item index="/admin/statistics">数据统计</el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
        
        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <!-- 通知 -->
            <el-badge :value="userStore.unreadCount" :hidden="userStore.unreadCount === 0" class="notification-badge">
              <el-button :icon="Bell" circle @click="$router.push('/notifications')" />
            </el-badge>
            
            <!-- 用户下拉菜单 -->
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-dropdown">
                <el-avatar :size="32" :src="userStore.user?.avatar">
                  {{ userStore.user?.name?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ userStore.user?.name }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="my-clubs">
                    <el-icon><OfficeBuilding /></el-icon>我的社团
                  </el-dropdown-item>
                  <el-dropdown-item command="my-activities">
                    <el-icon><Calendar /></el-icon>我的活动
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    
    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
    
    <!-- 底部 -->
    <el-footer class="footer">
      <p>© 2024 学生社团信息整理系统 - 毕业设计项目</p>
    </el-footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { 
  Bell, ArrowDown, User, OfficeBuilding, 
  Calendar, SwitchButton, School 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/clubs')) return '/clubs'
  if (path.startsWith('/activities')) return '/activities'
  if (path.startsWith('/admin')) return 'admin'
  return path
})

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'my-clubs':
      router.push('/my-clubs')
      break
    case 'my-activities':
      router.push('/my-activities')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        userStore.logout()
        router.push('/')
      } catch {}
      break
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
  cursor: pointer;
  margin-right: 40px;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-badge {
  margin-right: 8px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main-content {
  flex: 1;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  padding: 20px;
}

.footer {
  background: #f5f7fa;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style>
