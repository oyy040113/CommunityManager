<template>
  <div class="club-detail-page" v-loading="loading">
    <template v-if="club">
      <!-- 社团信息卡片 -->
      <el-card class="info-card">
        <div class="club-header">
          <el-avatar :size="100" :src="club.logo" class="club-avatar">
            {{ club.name?.charAt(0) }}
          </el-avatar>
          <div class="club-info">
            <h1 class="club-name">{{ club.name }}</h1>
            <div class="club-meta">
              <el-tag :type="getClubTypeTag(club.type)">{{ getClubTypeName(club.type) }}</el-tag>
              <span class="meta-item"><el-icon><User /></el-icon> 负责人：{{ club.creatorName }}</span>
              <span class="meta-item"><el-icon><Calendar /></el-icon> 创建于：{{ formatDate(club.createdAt) }}</span>
            </div>
            <p class="club-purpose">{{ club.purpose }}</p>
          </div>
          <div class="club-actions">
            <template v-if="!isMember && userStore.isLoggedIn">
              <el-button type="primary" @click="handleJoin">申请加入</el-button>
            </template>
            <template v-else-if="isMember">
              <el-button type="danger" plain @click="handleLeave">退出社团</el-button>
            </template>
            <template v-if="isLeader">
              <el-button type="warning" @click="$router.push(`/admin/club/${club.id}`)">管理社团</el-button>
            </template>
          </div>
        </div>
      </el-card>
      
      <!-- 统计信息 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-icon" style="background: #409eff"><el-icon><User /></el-icon></div>
            <div class="stat-content">
              <span class="stat-value">{{ club.memberCount }}</span>
              <span class="stat-label">成员数量</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-icon" style="background: #67c23a"><el-icon><Calendar /></el-icon></div>
            <div class="stat-content">
              <span class="stat-value">{{ club.activityCount }}</span>
              <span class="stat-label">举办活动</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-icon" style="background: #e6a23c"><el-icon><TrendCharts /></el-icon></div>
            <div class="stat-content">
              <span class="stat-value">{{ club.activityScore?.toFixed(1) }}</span>
              <span class="stat-label">活跃指数</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 详细内容标签页 -->
      <el-card>
        <el-tabs v-model="activeTab">
          <!-- 社团简介 -->
          <el-tab-pane label="社团简介" name="intro">
            <div class="intro-content">
              <p v-if="club.description">{{ club.description }}</p>
              <el-empty v-else description="暂无简介" />
            </div>
          </el-tab-pane>
          
          <!-- 社团活动 -->
          <el-tab-pane label="社团活动" name="activities">
            <div v-if="activities.length > 0">
              <el-timeline>
                <el-timeline-item
                  v-for="activity in activities"
                  :key="activity.id"
                  :timestamp="formatDate(activity.startTime)"
                  placement="top"
                  :type="getActivityStatus(activity).type"
                >
                  <el-card shadow="hover" class="activity-card" @click="$router.push(`/activities/${activity.id}`)">
                    <div class="activity-header">
                      <h4>{{ activity.title }}</h4>
                      <el-tag size="small" :type="getActivityStatus(activity).type">
                        {{ getActivityStatus(activity).text }}
                      </el-tag>
                    </div>
                    <p class="activity-desc">{{ activity.description }}</p>
                    <div class="activity-meta">
                      <span><el-icon><Location /></el-icon> {{ activity.location }}</span>
                      <span><el-icon><User /></el-icon> {{ activity.registrationCount || 0 }}/{{ activity.maxParticipants || '不限' }} 人</span>
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
            <el-empty v-else description="暂无活动" />
          </el-tab-pane>
          
          <!-- 社团成员 -->
          <el-tab-pane label="成员列表" name="members">
            <el-table :data="members" v-loading="membersLoading">
              <el-table-column label="头像" width="80">
                <template #default="{ row }">
                  <el-avatar :size="40" :src="row.userAvatar">{{ row.userName?.charAt(0) }}</el-avatar>
                </template>
              </el-table-column>
              <el-table-column prop="userName" label="姓名" />
              <el-table-column prop="role" label="角色">
                <template #default="{ row }">
                  <el-tag :type="row.role === 'LEADER' ? 'danger' : row.role === 'CORE' ? 'warning' : 'info'">
                    {{ getRoleName(row.role) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="position" label="职位" />
              <el-table-column prop="joinedAt" label="加入时间">
                <template #default="{ row }">{{ formatDate(row.joinedAt) }}</template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <!-- 公告栏 -->
          <el-tab-pane label="公告栏" name="announcements">
            <div v-if="announcements.length > 0">
              <el-card v-for="ann in announcements" :key="ann.id" class="announcement-card" shadow="never">
                <div class="ann-header">
                  <h4>{{ ann.title }}</h4>
                  <el-tag v-if="ann.pinned" type="danger" size="small">置顶</el-tag>
                </div>
                <p class="ann-content">{{ ann.content }}</p>
                <div class="ann-footer">
                  <span>{{ ann.creatorName }}</span>
                  <span>{{ formatDate(ann.createdAt) }}</span>
                </div>
              </el-card>
            </div>
            <el-empty v-else description="暂无公告" />
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>
    
    <!-- 加入社团对话框 -->
    <el-dialog v-model="showJoinDialog" title="申请加入社团" width="500px">
      <el-form :model="joinForm" label-width="80px">
        <el-form-item label="申请理由">
          <el-input
            v-model="joinForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入申请加入的理由（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showJoinDialog = false">取消</el-button>
        <el-button type="primary" :loading="joining" @click="submitJoin">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getClub, getClubMembers, joinClub, leaveClub, getClubActivities, getClubAnnouncements } from '@/api/club'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Calendar, Location, TrendCharts } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const club = ref(null)
const activeTab = ref('intro')

const activities = ref([])
const members = ref([])
const membersLoading = ref(false)
const announcements = ref([])

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

const getRoleName = (role) => {
  const map = { LEADER: '负责人', CORE: '核心成员', MEMBER: '普通成员' }
  return map[role] || role
}

const getActivityStatus = (activity) => {
  const now = new Date()
  const start = new Date(activity.startTime)
  const end = new Date(activity.endTime)
  if (now < start) return { text: '即将开始', type: 'warning' }
  if (now >= start && now <= end) return { text: '进行中', type: 'success' }
  return { text: '已结束', type: 'info' }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const isMember = computed(() => {
  if (!userStore.user || !members.value.length) return false
  return members.value.some(m => m.userId === userStore.user.id)
})

const isLeader = computed(() => {
  if (!userStore.user || !club.value) return false
  return club.value.creatorId === userStore.user.id
})

const loadClubDetail = async () => {
  loading.value = true
  try {
    const clubId = route.params.id
    const [clubRes, activitiesRes, membersRes, annRes] = await Promise.all([
      getClub(clubId),
      getClubActivities(clubId),
      getClubMembers(clubId),
      getClubAnnouncements(clubId)
    ])
    club.value = clubRes.data
    activities.value = activitiesRes.data?.content || activitiesRes.data || []
    members.value = membersRes.data?.content || membersRes.data || []
    announcements.value = annRes.data?.content || annRes.data || []
  } catch (e) {
    console.error('加载社团详情失败', e)
    ElMessage.error('加载社团信息失败')
  } finally {
    loading.value = false
  }
}

// 加入社团
const showJoinDialog = ref(false)
const joining = ref(false)
const joinForm = reactive({ reason: '' })

const handleJoin = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  showJoinDialog.value = true
}

const submitJoin = async () => {
  joining.value = true
  try {
    await joinClub(club.value.id)
    ElMessage.success('加入成功')
    showJoinDialog.value = false
    loadClubDetail()
  } catch (e) {
    console.error('加入社团失败', e)
  } finally {
    joining.value = false
  }
}

// 退出社团
const handleLeave = async () => {
  try {
    await ElMessageBox.confirm('确定要退出该社团吗？', '提示', {
      type: 'warning'
    })
    await leaveClub(club.value.id)
    ElMessage.success('已退出社团')
    loadClubDetail()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('退出社团失败', e)
    }
  }
}

onMounted(() => {
  loadClubDetail()
})
</script>

<style scoped>
.club-detail-page {
  padding-bottom: 40px;
}

.info-card {
  margin-bottom: 20px;
}

.club-header {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.club-avatar {
  flex-shrink: 0;
}

.club-info {
  flex: 1;
}

.club-name {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 12px;
}

.club-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
  color: #606266;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.club-purpose {
  font-size: 15px;
  color: #606266;
}

.club-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.intro-content {
  padding: 20px;
  line-height: 1.8;
  color: #606266;
}

.activity-card {
  cursor: pointer;
  transition: all 0.3s;
}

.activity-card:hover {
  transform: translateX(4px);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.activity-header h4 {
  font-size: 16px;
  font-weight: 600;
}

.activity-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.activity-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #606266;
}

.activity-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.announcement-card {
  margin-bottom: 16px;
  border-left: 3px solid #409eff;
}

.ann-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.ann-header h4 {
  font-size: 16px;
  font-weight: 600;
}

.ann-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.ann-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}
</style>
