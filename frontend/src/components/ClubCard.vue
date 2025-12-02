<template>
  <el-card class="club-card" shadow="hover" @click="$emit('click')">
    <div class="club-logo">
      <el-avatar :size="64" :src="club.logo">
        {{ club.name?.charAt(0) }}
      </el-avatar>
    </div>
    <h3 class="club-name">{{ club.name }}</h3>
    <el-tag size="small" :type="getClubTypeTag(club.type)">
      {{ getClubTypeName(club.type) }}
    </el-tag>
    <p class="club-purpose">{{ club.purpose || '暂无宗旨' }}</p>
    <el-divider />
    <div class="club-stats">
      <div class="stat-item">
        <span class="stat-value">{{ club.memberCount || 0 }}</span>
        <span class="stat-label">成员</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ club.activityCount || 0 }}</span>
        <span class="stat-label">活动</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ (club.activityScore || 0).toFixed(1) }}</span>
        <span class="stat-label">活跃度</span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  club: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

const clubTypeMap = {
  ACADEMIC: { name: '学术科技', tag: '' },
  CULTURAL: { name: '文化艺术', tag: 'success' },
  SPORTS: { name: '体育运动', tag: 'warning' },
  VOLUNTEER: { name: '志愿服务', tag: 'danger' },
  INNOVATION: { name: '创新创业', tag: 'info' },
  INTEREST: { name: '兴趣爱好', tag: '' },
  OTHER: { name: '其他', tag: 'info' }
}

const getClubTypeName = (type) => clubTypeMap[type]?.name || type
const getClubTypeTag = (type) => clubTypeMap[type]?.tag || ''
</script>

<style scoped>
.club-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
}

.club-card:hover {
  transform: translateY(-4px);
}

.club-logo {
  margin-bottom: 12px;
}

.club-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.club-purpose {
  font-size: 13px;
  color: #909399;
  margin: 12px 0;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.club-stats {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}
</style>
