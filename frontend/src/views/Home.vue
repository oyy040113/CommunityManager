<template>
  <div class="home-page">
    <!-- Banner区域 -->
    <section class="banner">
      <div class="banner-content">
        <h1>学生社团信息整理系统</h1>
        <p>发现精彩社团，参与丰富活动，记录成长轨迹</p>
        <div class="banner-actions">
          <el-button type="primary" size="large" @click="$router.push('/clubs')">
            浏览社团
          </el-button>
          <el-button size="large" @click="$router.push('/activities')">
            查看活动
          </el-button>
        </div>
      </div>
    </section>
    
    <!-- 统计数据 -->
    <section class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <el-icon size="40" color="#409eff"><OfficeBuilding /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ stats.clubCount }}</span>
              <span class="stat-label">活跃社团</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <el-icon size="40" color="#67c23a"><User /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ stats.userCount }}</span>
              <span class="stat-label">注册用户</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <el-icon size="40" color="#e6a23c"><Calendar /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ stats.activityCount }}</span>
              <span class="stat-label">精彩活动</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <el-icon size="40" color="#f56c6c"><TrendCharts /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ stats.participationCount }}</span>
              <span class="stat-label">活动参与</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>
    
    <!-- 热门社团 -->
    <section class="section">
      <div class="section-header">
        <h2>热门社团</h2>
        <el-button text type="primary" @click="$router.push('/clubs')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="6" v-for="club in topClubs" :key="club.id">
          <el-card class="club-card" shadow="hover" @click="$router.push(`/clubs/${club.id}`)">
            <div class="club-logo">
              <el-avatar :size="64" :src="club.logo">
                {{ club.name?.charAt(0) }}
              </el-avatar>
            </div>
            <h3 class="club-name">{{ club.name }}</h3>
            <el-tag size="small" :type="getClubTypeTag(club.type)">
              {{ getClubTypeName(club.type) }}
            </el-tag>
            <p class="club-desc">{{ club.purpose || '暂无简介' }}</p>
            <div class="club-stats">
              <span><el-icon><User /></el-icon> {{ club.memberCount }} 成员</span>
              <span><el-icon><Star /></el-icon> {{ club.activityScore?.toFixed(1) }} 活跃度</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>
    
    <!-- 最新活动 -->
    <section class="section">
      <div class="section-header">
        <h2>最新活动</h2>
        <el-button text type="primary" @click="$router.push('/activities')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="8" v-for="activity in upcomingActivities" :key="activity.id">
          <el-card class="activity-card" shadow="hover" @click="$router.push(`/activities/${activity.id}`)">
            <template #header>
              <div class="activity-header">
                <el-tag :type="getStatusTag(activity.status)" size="small">
                  {{ getStatusName(activity.status) }}
                </el-tag>
                <span class="activity-club">{{ activity.clubName }}</span>
              </div>
            </template>
            <h3 class="activity-title">{{ activity.title }}</h3>
            <div class="activity-info">
              <p><el-icon><Clock /></el-icon> {{ formatDate(activity.startTime) }}</p>
              <p><el-icon><Location /></el-icon> {{ activity.location }}</p>
              <p><el-icon><User /></el-icon> {{ activity.currentParticipants }}/{{ activity.maxParticipants || '不限' }} 人</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTopClubs } from '@/api/club'
import { getUpcomingActivities } from '@/api/activity'
import { 
  OfficeBuilding, User, Calendar, TrendCharts, 
  ArrowRight, Star, Clock, Location 
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const stats = ref({
  clubCount: 0,
  userCount: 0,
  activityCount: 0,
  participationCount: 0
})

const topClubs = ref([])
const upcomingActivities = ref([])

const clubTypeMap = {
  ACADEMIC: { name: '学术科技', tag: '' },
  CULTURAL: { name: '文化艺术', tag: 'success' },
  SPORTS: { name: '体育运动', tag: 'warning' },
  VOLUNTEER: { name: '志愿服务', tag: 'danger' },
  INNOVATION: { name: '创新创业', tag: 'info' },
  INTEREST: { name: '兴趣爱好', tag: '' },
  OTHER: { name: '其他', tag: 'info' }
}

const statusMap = {
  DRAFT: { name: '草稿', tag: 'info' },
  PUBLISHED: { name: '报名中', tag: 'success' },
  REGISTRATION_CLOSED: { name: '报名截止', tag: 'warning' },
  ONGOING: { name: '进行中', tag: '' },
  COMPLETED: { name: '已结束', tag: 'info' },
  CANCELLED: { name: '已取消', tag: 'danger' }
}

const getClubTypeName = (type) => clubTypeMap[type]?.name || type
const getClubTypeTag = (type) => clubTypeMap[type]?.tag || ''
const getStatusName = (status) => statusMap[status]?.name || status
const getStatusTag = (status) => statusMap[status]?.tag || ''
const formatDate = (date) => dayjs(date).format('MM-DD HH:mm')

onMounted(async () => {
  try {
    // 获取热门社团
    const clubRes = await getTopClubs(4)
    topClubs.value = clubRes.data || []
    stats.value.clubCount = topClubs.value.length
    
    // 获取最新活动
    const activityRes = await getUpcomingActivities({ size: 3 })
    upcomingActivities.value = activityRes.data?.content || []
    stats.value.activityCount = upcomingActivities.value.length
    
    // 模拟其他统计数据
    stats.value.userCount = 128
    stats.value.participationCount = 560
  } catch (e) {
    console.error('加载数据失败', e)
  }
})
</script>

<style scoped>
.home-page {
  padding-bottom: 40px;
}

.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 60px 40px;
  margin-bottom: 30px;
  text-align: center;
  color: #fff;
}

.banner h1 {
  font-size: 36px;
  margin-bottom: 16px;
}

.banner p {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 24px;
}

.banner-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.stats-section {
  margin-bottom: 40px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.club-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.club-card:hover {
  transform: translateY(-4px);
}

.club-logo {
  margin-bottom: 12px;
}

.club-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.club-desc {
  font-size: 13px;
  color: #909399;
  margin: 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.club-stats {
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 12px;
  color: #606266;
}

.club-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.activity-card {
  cursor: pointer;
  transition: all 0.3s;
}

.activity-card:hover {
  transform: translateY(-4px);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-club {
  font-size: 13px;
  color: #909399;
}

.activity-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-info {
  font-size: 13px;
  color: #606266;
}

.activity-info p {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}
</style>
