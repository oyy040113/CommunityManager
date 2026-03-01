<template>
  <div class="activity-list-page">
    <div class="page-header">
      <h1>活动中心</h1>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="活动名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable>
            <el-option label="报名中" value="upcoming" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已结束" value="finished" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadActivities">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 活动列表 -->
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="8" v-for="activity in activities" :key="activity.id">
        <el-card class="activity-card" shadow="hover" @click="$router.push(`/activities/${activity.id}`)">
          <div class="activity-cover" :style="{ backgroundImage: `url(${activity.coverImage || '/default-cover.jpg'})` }">
            <el-tag :type="getStatusType(activity)" class="status-tag">
              {{ getStatusText(activity) }}
            </el-tag>
          </div>
          <div class="activity-content">
            <h3 class="activity-title">{{ activity.title }}</h3>
            <div class="activity-meta">
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDateTime(activity.startTime) }}
              </span>
              <span class="meta-item">
                <el-icon><Location /></el-icon>
                {{ activity.location }}
              </span>
            </div>
            <p class="activity-desc">{{ activity.description }}</p>
            <el-divider />
            <div class="activity-footer">
              <div class="club-info">
                <el-avatar :size="24" :src="activity.clubLogo">{{ activity.clubName?.charAt(0) }}</el-avatar>
                <span>{{ activity.clubName }}</span>
              </div>
              <div class="participant-info">
                <el-icon><User /></el-icon>
                <span v-if="['ONGOING', 'COMPLETED'].includes(activity.status)">
                  签到{{ activity.checkedInCount || 0 }}/报名{{ activity.registrationCount || 0 }}/{{ activity.maxParticipants || '不限' }}
                </span>
                <span v-else>{{ activity.registrationCount || 0 }}/{{ activity.maxParticipants || '不限' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 空状态 -->
    <el-empty v-if="activities.length === 0 && !loading" description="暂无活动" />
    
    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[6, 12, 18, 24]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadActivities"
        @current-change="loadActivities"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { searchActivities } from '@/api/activity'
import { Calendar, Location, User } from '@element-plus/icons-vue'

const loading = ref(false)
const activities = ref([])
const total = ref(0)

const filters = reactive({
  keyword: '',
  status: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  size: 6
})

const getStatusType = (activity) => {
  const now = new Date()
  const start = new Date(activity.startTime)
  const end = new Date(activity.endTime)
  const regEnd = new Date(activity.registrationDeadline)
  
  if (now < regEnd) return 'success'
  if (now >= start && now <= end) return 'warning'
  return 'info'
}

const getStatusText = (activity) => {
  const now = new Date()
  const start = new Date(activity.startTime)
  const end = new Date(activity.endTime)
  const regEnd = new Date(activity.registrationDeadline)
  
  if (now < regEnd) return '报名中'
  if (now < start) return '即将开始'
  if (now >= start && now <= end) return '进行中'
  return '已结束'
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { 
    month: 'numeric', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadActivities = async () => {
  loading.value = true
  try {
    const params = {
      keyword: filters.keyword || undefined,
      page: pagination.page - 1,
      size: pagination.size
    }
    if (filters.dateRange?.length === 2) {
      params.startDate = filters.dateRange[0].toISOString()
      params.endDate = filters.dateRange[1].toISOString()
    }
    const res = await searchActivities(params)
    activities.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } catch (e) {
    console.error('加载活动列表失败', e)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.keyword = ''
  filters.status = ''
  filters.dateRange = null
  pagination.page = 1
  loadActivities()
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.activity-list-page {
  padding-bottom: 40px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
}

.filter-card {
  margin-bottom: 20px;
}

.activity-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
  overflow: hidden;
}

.activity-card:hover {
  transform: translateY(-4px);
}

.activity-card :deep(.el-card__body) {
  padding: 0;
}

.activity-cover {
  height: 160px;
  background-size: cover;
  background-position: center;
  background-color: #f0f2f5;
  position: relative;
}

.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
}

.activity-content {
  padding: 16px;
}

.activity-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.activity-desc {
  font-size: 14px;
  color: #909399;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.club-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}

.participant-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
