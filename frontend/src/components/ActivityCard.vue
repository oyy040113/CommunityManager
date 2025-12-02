<template>
  <el-card class="activity-card" shadow="hover" @click="$emit('click')">
    <div class="activity-cover" :style="{ backgroundImage: `url(${activity.coverImage || defaultCover})` }">
      <el-tag :type="statusType" class="status-tag">{{ statusText }}</el-tag>
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
          <span>{{ activity.registrationCount || 0 }}/{{ activity.maxParticipants || '不限' }}</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { Calendar, Location, User } from '@element-plus/icons-vue'

const props = defineProps({
  activity: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

const defaultCover = '/default-cover.jpg'

const statusType = computed(() => {
  const now = new Date()
  const start = new Date(props.activity.startTime)
  const end = new Date(props.activity.endTime)
  const regEnd = new Date(props.activity.registrationDeadline)
  
  if (now < regEnd) return 'success'
  if (now >= start && now <= end) return 'warning'
  return 'info'
})

const statusText = computed(() => {
  const now = new Date()
  const start = new Date(props.activity.startTime)
  const end = new Date(props.activity.endTime)
  const regEnd = new Date(props.activity.registrationDeadline)
  
  if (now < regEnd) return '报名中'
  if (now < start) return '即将开始'
  if (now >= start && now <= end) return '进行中'
  return '已结束'
})

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
</script>

<style scoped>
.activity-card {
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;
  height: 100%;
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
</style>
