<template>
  <div class="statistics-page">
    <h1>数据统计</h1>
    
    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%)">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ overview.totalMembers }}</span>
            <span class="stat-label">总成员数</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%)">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ overview.totalClubs }}</span>
            <span class="stat-label">管理社团</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%)">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ overview.totalActivities }}</span>
            <span class="stat-label">举办活动</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #fab6b6 100%)">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ overview.averageRating?.toFixed(1) || '-' }}</span>
            <span class="stat-label">平均评分</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 选择社团 -->
    <el-card class="filter-card">
      <el-select v-model="selectedClubId" placeholder="选择社团查看详情" clearable @change="loadClubStats" style="width: 300px">
        <el-option v-for="club in myClubs" :key="club.id" :label="club.name" :value="club.id" />
      </el-select>
    </el-card>
    
    <el-row :gutter="20" v-if="selectedClubId">
      <!-- 成员增长趋势 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>成员增长趋势</span>
          </template>
          <div class="chart-container" ref="memberChartRef"></div>
        </el-card>
      </el-col>
      
      <!-- 活动参与统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>活动参与统计</span>
          </template>
          <div class="chart-container" ref="activityChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" v-if="selectedClubId" style="margin-top: 20px">
      <!-- 活动评分分布 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>活动评分分布</span>
          </template>
          <div class="chart-container" ref="ratingChartRef"></div>
        </el-card>
      </el-col>
      
      <!-- 最近活动列表 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近活动</span>
          </template>
          <el-table :data="recentActivities" size="small">
            <el-table-column prop="title" label="活动名称" />
            <el-table-column label="参与人数" width="100">
              <template #default="{ row }">{{ row.registrationCount }}</template>
            </el-table-column>
            <el-table-column label="评分" width="100">
              <template #default="{ row }">
                <span v-if="row.averageRating">{{ row.averageRating.toFixed(1) }}</span>
                <span v-else class="no-rating">暂无</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row)" size="small">{{ getStatusText(row) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 社团排行榜 -->
    <el-card v-if="myClubs.length > 1" style="margin-top: 20px">
      <template #header>
        <span>社团活跃度排行</span>
      </template>
      <el-table :data="clubRankings">
        <el-table-column label="排名" width="80">
          <template #default="{ $index }">
            <span :class="['rank', `rank-${$index + 1}`]">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="社团" min-width="200">
          <template #default="{ row }">
            <div class="club-cell">
              <el-avatar :size="36" :src="row.logo">{{ row.name?.charAt(0) }}</el-avatar>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="成员数" width="100" />
        <el-table-column prop="activityCount" label="活动数" width="100" />
        <el-table-column label="活动评价" width="180">
          <template #default="{ row }">
            <span>{{ row.feedbackCount || 0 }}人 · {{ (row.averageRating || 0).toFixed(1) }}分</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyCreatedClubs, getClubActivities } from '@/api/club'
import { getOverviewStats } from '@/api/statistics'
import { ElMessage } from 'element-plus'
import { User, OfficeBuilding, Calendar, Star } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const myClubs = ref([])
const selectedClubId = ref('')

const overview = reactive({
  totalMembers: 0,
  totalClubs: 0,
  totalActivities: 0,
  averageRating: 0
})

const recentActivities = ref([])
const clubRankings = ref([])

// 图表引用
const memberChartRef = ref(null)
const activityChartRef = ref(null)
const ratingChartRef = ref(null)

const getStatusType = (activity) => {
  const now = new Date()
  const start = new Date(activity.startTime)
  const end = new Date(activity.endTime)
  if (now < start) return 'warning'
  if (now >= start && now <= end) return 'success'
  return 'info'
}

const getStatusText = (activity) => {
  const now = new Date()
  const start = new Date(activity.startTime)
  const end = new Date(activity.endTime)
  if (now < start) return '未开始'
  if (now >= start && now <= end) return '进行中'
  return '已结束'
}

const getScoreColor = (score) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

const loadMyClubs = async () => {
  try {
    // 尝试使用统计概览接口
    try {
      const overviewRes = await getOverviewStats()
      if (overviewRes.data) {
        overview.totalMembers = overviewRes.data.totalMembers || 0
        overview.totalClubs = overviewRes.data.totalClubs || 0
        overview.totalActivities = overviewRes.data.totalActivities || 0
        overview.averageRating = overviewRes.data.averageRating || 0
        myClubs.value = overviewRes.data.clubs || []
      }
    } catch {
      // 如果统计接口失败，使用我管理的社团接口
      const res = await getMyCreatedClubs()
      myClubs.value = res.data || []
      
      // 计算概览数据
      let totalMembers = 0
      let totalActivities = 0
      let totalRatings = []
      
      for (const club of myClubs.value) {
        totalMembers += club.memberCount || 0
        totalActivities += club.activityCount || 0
        if ((club.averageRating || 0) > 0) {
          totalRatings.push(club.averageRating)
        }
      }
      
      overview.totalMembers = totalMembers
      overview.totalClubs = myClubs.value.length
      overview.totalActivities = totalActivities
      overview.averageRating = totalRatings.length > 0 
        ? totalRatings.reduce((a, b) => a + b, 0) / totalRatings.length 
        : 0
    }
    
    // 计算排行榜
    clubRankings.value = [...myClubs.value].sort((a, b) => 
      (b.averageRating || 0) - (a.averageRating || 0) || (b.feedbackCount || 0) - (a.feedbackCount || 0)
    )
    
    if (myClubs.value.length > 0) {
      selectedClubId.value = myClubs.value[0].id
      loadClubStats()
    }
  } catch (e) {
    console.error('加载社团失败', e)
  }
}

const loadClubStats = async () => {
  if (!selectedClubId.value) return
  
  try {
    const [activitiesRes] = await Promise.all([
      getClubActivities(selectedClubId.value)
    ])
    
    const activities = activitiesRes.data?.content || activitiesRes.data || []
    recentActivities.value = activities.slice(0, 5)
    
    // 渲染图表
    await nextTick()
    renderCharts(activities)
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

const renderCharts = (activities) => {
  // 这里使用简单的模拟图表
  // 实际项目中应该使用 ECharts 或其他图表库
  
  if (memberChartRef.value) {
    memberChartRef.value.innerHTML = `
      <div class="simple-chart">
        <div class="chart-bars">
          ${generateBars(6, '成员')}
        </div>
        <div class="chart-labels">
          ${['1月', '2月', '3月', '4月', '5月', '6月'].map(m => `<span>${m}</span>`).join('')}
        </div>
      </div>
    `
  }
  
  if (activityChartRef.value) {
    activityChartRef.value.innerHTML = `
      <div class="simple-chart">
        <div class="chart-bars">
          ${generateBars(6, '活动')}
        </div>
        <div class="chart-labels">
          ${['1月', '2月', '3月', '4月', '5月', '6月'].map(m => `<span>${m}</span>`).join('')}
        </div>
      </div>
    `
  }
  
  if (ratingChartRef.value) {
    const ratingDist = [0, 0, 0, 0, 0]
    activities.forEach(a => {
      if (a.averageRating) {
        const index = Math.min(Math.floor(a.averageRating) - 1, 4)
        if (index >= 0) ratingDist[index]++
      }
    })
    
    ratingChartRef.value.innerHTML = `
      <div class="rating-distribution">
        ${[5, 4, 3, 2, 1].map((star, i) => `
          <div class="rating-row">
            <span class="star-label">${star}星</span>
            <div class="rating-bar-bg">
              <div class="rating-bar" style="width: ${Math.min((ratingDist[4 - i] / Math.max(...ratingDist, 1)) * 100, 100)}%"></div>
            </div>
            <span class="rating-count">${ratingDist[4 - i]}</span>
          </div>
        `).join('')}
      </div>
    `
  }
}

const generateBars = (count, type) => {
  const bars = []
  for (let i = 0; i < count; i++) {
    const height = 30 + Math.random() * 60
    bars.push(`<div class="bar" style="height: ${height}%"></div>`)
  }
  return bars.join('')
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadMyClubs()
})
</script>

<style scoped>
.statistics-page {
  padding-bottom: 40px;
}

.statistics-page h1 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
}

.overview-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.filter-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.simple-chart {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-bars {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  padding: 20px;
}

.chart-bars .bar {
  width: 40px;
  background: linear-gradient(180deg, #409eff 0%, #79bbff 100%);
  border-radius: 4px 4px 0 0;
  transition: height 0.3s;
}

.chart-labels {
  display: flex;
  justify-content: space-around;
  padding: 8px 20px;
  color: #909399;
  font-size: 12px;
}

.rating-distribution {
  width: 100%;
  padding: 20px;
}

.rating-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.star-label {
  width: 40px;
  font-size: 13px;
  color: #606266;
}

.rating-bar-bg {
  flex: 1;
  height: 12px;
  background: #f0f2f5;
  border-radius: 6px;
  margin: 0 12px;
  overflow: hidden;
}

.rating-bar {
  height: 100%;
  background: linear-gradient(90deg, #f56c6c 0%, #e6a23c 50%, #67c23a 100%);
  border-radius: 6px;
  transition: width 0.3s;
}

.rating-count {
  width: 30px;
  text-align: right;
  font-size: 13px;
  color: #909399;
}

.no-rating {
  color: #c0c4cc;
}

.club-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
}

.rank-1 {
  background: #ffd700;
  color: #fff;
}

.rank-2 {
  background: #c0c0c0;
  color: #fff;
}

.rank-3 {
  background: #cd7f32;
  color: #fff;
}
</style>
