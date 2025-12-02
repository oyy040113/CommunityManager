<template>
  <div class="notifications-page">
    <div class="page-header">
      <h1>通知中心</h1>
      <div class="header-actions">
        <el-button @click="markAllAsRead" :disabled="unreadCount === 0">
          全部标为已读
        </el-button>
      </div>
    </div>
    
    <el-tabs v-model="activeTab" @tab-change="loadNotifications">
      <el-tab-pane label="全部" name="all">
        <template #label>
          <span>全部</span>
          <el-badge v-if="totalCount > 0" :value="totalCount" class="tab-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="未读" name="unread">
        <template #label>
          <span>未读</span>
          <el-badge v-if="unreadCount > 0" :value="unreadCount" class="tab-badge" type="danger" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="已读" name="read" />
    </el-tabs>
    
    <div class="notification-list" v-loading="loading">
      <el-card 
        v-for="notification in notifications" 
        :key="notification.id" 
        class="notification-card"
        :class="{ unread: !notification.read }"
        shadow="hover"
        @click="handleNotificationClick(notification)"
      >
        <div class="notification-content">
          <div class="notification-icon" :style="{ background: getTypeColor(notification.type) }">
            <el-icon><component :is="getTypeIcon(notification.type)" /></el-icon>
          </div>
          <div class="notification-body">
            <h4 class="notification-title">{{ notification.title }}</h4>
            <p class="notification-text">{{ notification.content }}</p>
            <div class="notification-meta">
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
              <el-tag v-if="!notification.read" type="danger" size="small">未读</el-tag>
            </div>
          </div>
          <div class="notification-actions" @click.stop>
            <el-button 
              v-if="!notification.read"
              type="primary" 
              text 
              size="small"
              @click="markAsRead(notification)"
            >
              标为已读
            </el-button>
            <el-button 
              type="danger" 
              text 
              size="small"
              @click="deleteNotification(notification)"
            >
              删除
            </el-button>
          </div>
        </div>
      </el-card>
      
      <el-empty v-if="notifications.length === 0 && !loading" description="暂无通知" />
    </div>
    
    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadNotifications"
        @current-change="loadNotifications"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  getNotifications, 
  markNotificationAsRead, 
  markAllNotificationsAsRead,
  deleteNotification as deleteNotificationApi
} from '@/api/notification'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, User, Calendar, Notification, ChatDotRound, Warning } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('all')
const notifications = ref([])
const total = ref(0)
const unreadCount = ref(0)
const totalCount = ref(0)

const pagination = reactive({
  page: 1,
  size: 10
})

const getTypeIcon = (type) => {
  const icons = {
    ACTIVITY: Calendar,
    CLUB: User,
    SYSTEM: Notification,
    FEEDBACK: ChatDotRound,
    ALERT: Warning
  }
  return icons[type] || Bell
}

const getTypeColor = (type) => {
  const colors = {
    ACTIVITY: '#67c23a',
    CLUB: '#409eff',
    SYSTEM: '#909399',
    FEEDBACK: '#e6a23c',
    ALERT: '#f56c6c'
  }
  return colors[type] || '#409eff'
}

const formatTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  // 1分钟内
  if (diff < 60000) return '刚刚'
  // 1小时内
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  // 今天内
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 更早
  return date.toLocaleString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadNotifications = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    if (activeTab.value === 'unread') {
      params.read = false
    } else if (activeTab.value === 'read') {
      params.read = true
    }
    
    const res = await getNotifications(params)
    notifications.value = res.data?.content || res.data || []
    total.value = res.data?.totalElements || notifications.value.length
    
    // 更新未读数量
    unreadCount.value = notifications.value.filter(n => !n.read).length
    totalCount.value = total.value
  } catch (e) {
    console.error('加载通知失败', e)
  } finally {
    loading.value = false
  }
}

const handleNotificationClick = async (notification) => {
  // 先标记为已读
  if (!notification.read) {
    await markAsRead(notification)
  }
  
  // 根据类型跳转
  if (notification.targetType === 'ACTIVITY' && notification.targetId) {
    router.push(`/activities/${notification.targetId}`)
  } else if (notification.targetType === 'CLUB' && notification.targetId) {
    router.push(`/clubs/${notification.targetId}`)
  }
}

const markAsRead = async (notification) => {
  try {
    await markNotificationAsRead(notification.id)
    notification.read = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    userStore.fetchNotifications()
  } catch (e) {
    console.error('标记已读失败', e)
  }
}

const markAllAsRead = async () => {
  try {
    await markAllNotificationsAsRead()
    notifications.value.forEach(n => n.read = true)
    unreadCount.value = 0
    userStore.fetchNotifications()
    ElMessage.success('已全部标为已读')
  } catch (e) {
    console.error('标记全部已读失败', e)
  }
}

const deleteNotification = async (notification) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', { type: 'warning' })
    await deleteNotificationApi(notification.id)
    const index = notifications.value.findIndex(n => n.id === notification.id)
    if (index > -1) {
      notifications.value.splice(index, 1)
      total.value--
      if (!notification.read) {
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
    }
    ElMessage.success('已删除')
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除通知失败', e)
    }
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadNotifications()
})
</script>

<style scoped>
.notifications-page {
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
}

.tab-badge {
  margin-left: 4px;
}

.notification-list {
  margin-top: 20px;
}

.notification-card {
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.notification-card:hover {
  transform: translateX(4px);
}

.notification-card.unread {
  border-left: 3px solid #409eff;
}

.notification-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}

.notification-body {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
}

.notification-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.5;
}

.notification-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
