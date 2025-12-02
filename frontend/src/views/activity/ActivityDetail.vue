<template>
  <div class="activity-detail-page" v-loading="loading">
    <template v-if="activity">
      <!-- 活动封面与基本信息 -->
      <el-card class="header-card">
        <div class="activity-header">
          <div class="cover-section">
            <img :src="activity.coverImage || '/default-cover.jpg'" alt="活动封面" class="cover-image" />
          </div>
          <div class="info-section">
            <div class="status-row">
              <el-tag :type="getStatusType(activity)" size="large">{{ getStatusText(activity) }}</el-tag>
            </div>
            <h1 class="activity-title">{{ activity.title }}</h1>
            
            <div class="info-list">
              <div class="info-item">
                <el-icon><Calendar /></el-icon>
                <div class="info-content">
                  <span class="info-label">活动时间</span>
                  <span class="info-value">{{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}</span>
                </div>
              </div>
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <div class="info-content">
                  <span class="info-label">活动地点</span>
                  <span class="info-value">{{ activity.location }}</span>
                </div>
              </div>
              <div class="info-item">
                <el-icon><User /></el-icon>
                <div class="info-content">
                  <span class="info-label">参与人数</span>
                  <span class="info-value">{{ activity.registrationCount || 0 }} / {{ activity.maxParticipants || '不限' }} 人</span>
                </div>
              </div>
              <div class="info-item">
                <el-icon><Timer /></el-icon>
                <div class="info-content">
                  <span class="info-label">报名截止</span>
                  <span class="info-value">{{ formatDateTime(activity.registrationDeadline) }}</span>
                </div>
              </div>
            </div>
            
            <div class="action-row">
              <template v-if="!isRegistered && canRegister">
                <el-button type="primary" size="large" @click="handleRegister" :loading="registering">
                  立即报名
                </el-button>
              </template>
              <template v-else-if="isRegistered">
                <el-button type="success" size="large" disabled>已报名</el-button>
                <el-button type="danger" plain size="large" @click="handleCancel">取消报名</el-button>
              </template>
              <template v-else>
                <el-button type="info" size="large" disabled>报名已结束</el-button>
              </template>
              
              <el-button v-if="canFeedback" type="warning" size="large" @click="showFeedbackDialog = true">
                评价活动
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 主办社团信息 -->
      <el-card class="club-card">
        <template #header>
          <span>主办社团</span>
        </template>
        <div class="club-info" @click="$router.push(`/clubs/${activity.clubId}`)">
          <el-avatar :size="48" :src="activity.clubLogo">{{ activity.clubName?.charAt(0) }}</el-avatar>
          <div class="club-detail">
            <h3>{{ activity.clubName }}</h3>
            <p>{{ activity.clubPurpose || '暂无宗旨' }}</p>
          </div>
          <el-button text type="primary">查看详情</el-button>
        </div>
      </el-card>
      
      <!-- 活动详情标签页 -->
      <el-card>
        <el-tabs v-model="activeTab">
          <!-- 活动介绍 -->
          <el-tab-pane label="活动介绍" name="intro">
            <div class="intro-content">
              <p v-if="activity.description">{{ activity.description }}</p>
              <el-empty v-else description="暂无详细介绍" />
            </div>
          </el-tab-pane>
          
          <!-- 参与人员 -->
          <el-tab-pane label="参与人员" name="participants">
            <el-table :data="participants" v-loading="participantsLoading">
              <el-table-column label="头像" width="80">
                <template #default="{ row }">
                  <el-avatar :size="40" :src="row.userAvatar">{{ row.userName?.charAt(0) }}</el-avatar>
                </template>
              </el-table-column>
              <el-table-column prop="userName" label="姓名" />
              <el-table-column label="报名时间">
                <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="状态">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'REGISTERED' ? 'success' : 'info'">
                    {{ row.status === 'REGISTERED' ? '已报名' : row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="participants.length === 0 && !participantsLoading" description="暂无参与者" />
          </el-tab-pane>
          
          <!-- 活动评价 -->
          <el-tab-pane label="活动评价" name="feedback">
            <div v-if="feedbacks.length > 0" class="feedback-list">
              <div class="feedback-summary">
                <div class="summary-score">
                  <span class="score-value">{{ averageRating.toFixed(1) }}</span>
                  <el-rate :model-value="averageRating" disabled />
                  <span class="score-count">{{ feedbacks.length }} 条评价</span>
                </div>
              </div>
              <el-divider />
              <div v-for="fb in feedbacks" :key="fb.id" class="feedback-item">
                <div class="feedback-header">
                  <el-avatar :size="36" :src="fb.userAvatar">{{ fb.userName?.charAt(0) }}</el-avatar>
                  <div class="feedback-user">
                    <span class="user-name">{{ fb.userName }}</span>
                    <el-rate :model-value="fb.rating" disabled size="small" />
                  </div>
                  <span class="feedback-time">{{ formatDateTime(fb.createdAt) }}</span>
                </div>
                <p class="feedback-content">{{ fb.content }}</p>
              </div>
            </div>
            <el-empty v-else description="暂无评价" />
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>
    
    <!-- 评价对话框 -->
    <el-dialog v-model="showFeedbackDialog" title="评价活动" width="500px">
      <el-form :model="feedbackForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="feedbackForm.rating" show-text :texts="['很差', '较差', '一般', '良好', '非常好']" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            :rows="4"
            placeholder="分享你对这次活动的感受..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFeedbackDialog = false">取消</el-button>
        <el-button type="primary" :loading="submittingFeedback" @click="submitFeedback">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  getActivity, 
  getActivityParticipants, 
  getActivityFeedbacks,
  registerActivity,
  cancelRegistration,
  submitActivityFeedback
} from '@/api/activity'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Location, User, Timer } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activity = ref(null)
const activeTab = ref('intro')

const participants = ref([])
const participantsLoading = ref(false)
const feedbacks = ref([])

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
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const isRegistered = computed(() => {
  if (!userStore.user || !participants.value.length) return false
  return participants.value.some(p => p.userId === userStore.user.id)
})

const canRegister = computed(() => {
  if (!activity.value) return false
  const now = new Date()
  const regEnd = new Date(activity.value.registrationDeadline)
  const maxP = activity.value.maxParticipants
  return now < regEnd && (!maxP || participants.value.length < maxP)
})

const canFeedback = computed(() => {
  if (!activity.value || !userStore.user) return false
  const now = new Date()
  const end = new Date(activity.value.endTime)
  return now > end && isRegistered.value
})

const averageRating = computed(() => {
  if (feedbacks.value.length === 0) return 0
  const sum = feedbacks.value.reduce((acc, fb) => acc + fb.rating, 0)
  return sum / feedbacks.value.length
})

const loadActivityDetail = async () => {
  loading.value = true
  try {
    const activityId = route.params.id
    const [actRes, partRes, fbRes] = await Promise.all([
      getActivity(activityId),
      getActivityParticipants(activityId),
      getActivityFeedbacks(activityId)
    ])
    activity.value = actRes.data
    participants.value = partRes.data?.content || partRes.data || []
    feedbacks.value = fbRes.data?.content || fbRes.data || []
  } catch (e) {
    console.error('加载活动详情失败', e)
    ElMessage.error('加载活动信息失败')
  } finally {
    loading.value = false
  }
}

// 报名
const registering = ref(false)

const handleRegister = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  registering.value = true
  try {
    await registerActivity(activity.value.id)
    ElMessage.success('报名成功')
    loadActivityDetail()
  } catch (e) {
    console.error('报名失败', e)
  } finally {
    registering.value = false
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消报名吗？', '提示', { type: 'warning' })
    await cancelRegistration(activity.value.id)
    ElMessage.success('已取消报名')
    loadActivityDetail()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消报名失败', e)
    }
  }
}

// 评价
const showFeedbackDialog = ref(false)
const submittingFeedback = ref(false)
const feedbackForm = reactive({
  rating: 5,
  content: ''
})

const submitFeedback = async () => {
  submittingFeedback.value = true
  try {
    await submitActivityFeedback(activity.value.id, feedbackForm)
    ElMessage.success('评价提交成功')
    showFeedbackDialog.value = false
    loadActivityDetail()
  } catch (e) {
    console.error('提交评价失败', e)
  } finally {
    submittingFeedback.value = false
  }
}

onMounted(() => {
  loadActivityDetail()
})
</script>

<style scoped>
.activity-detail-page {
  padding-bottom: 40px;
}

.header-card {
  margin-bottom: 20px;
}

.activity-header {
  display: flex;
  gap: 32px;
}

.cover-section {
  flex-shrink: 0;
  width: 400px;
}

.cover-image {
  width: 100%;
  height: 280px;
  object-fit: cover;
  border-radius: 8px;
}

.info-section {
  flex: 1;
}

.status-row {
  margin-bottom: 12px;
}

.activity-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 20px;
}

.info-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.info-item > .el-icon {
  font-size: 24px;
  color: #409eff;
  margin-top: 4px;
}

.info-content {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 13px;
  color: #909399;
}

.info-value {
  font-size: 15px;
  color: #303133;
}

.action-row {
  display: flex;
  gap: 12px;
}

.club-card {
  margin-bottom: 20px;
}

.club-info {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
}

.club-detail {
  flex: 1;
}

.club-detail h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.club-detail p {
  font-size: 13px;
  color: #909399;
}

.intro-content {
  padding: 20px;
  line-height: 1.8;
  color: #606266;
}

.feedback-summary {
  padding: 20px;
  text-align: center;
}

.summary-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.score-value {
  font-size: 48px;
  font-weight: 600;
  color: #409eff;
}

.score-count {
  font-size: 14px;
  color: #909399;
}

.feedback-list {
  padding: 20px;
}

.feedback-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.feedback-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.feedback-user {
  flex: 1;
}

.user-name {
  display: block;
  font-weight: 500;
  margin-bottom: 4px;
}

.feedback-time {
  font-size: 12px;
  color: #909399;
}

.feedback-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}
</style>
