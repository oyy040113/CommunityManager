<template>
  <div class="my-activities-page">
    <h1>我的活动</h1>
    
    <el-tabs v-model="activeTab">
      <!-- 已报名的活动 -->
      <el-tab-pane label="已报名" name="registered">
        <el-table :data="registeredActivities" v-loading="loading">
          <el-table-column label="活动" min-width="300">
            <template #default="{ row }">
              <div class="activity-cell" @click="$router.push(`/activities/${row.activity?.id}`)">
                <img :src="row.activity?.coverImage || '/default-cover.jpg'" class="cover-thumb" />
                <div class="activity-info">
                  <h4>{{ row.activity?.title }}</h4>
                  <span class="club-name">{{ row.activity?.clubName }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="活动时间" width="200">
            <template #default="{ row }">
              {{ formatDateTime(row.activity?.startTime) }}
            </template>
          </el-table-column>
          <el-table-column label="地点" width="150">
            <template #default="{ row }">{{ row.activity?.location }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.activity)">{{ getStatusText(row.activity) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="报名时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.registeredAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button 
                v-if="canCancel(row.activity)" 
                type="danger" 
                size="small" 
                @click="handleCancel(row)"
              >
                取消报名
              </el-button>
              <el-button 
                v-if="canFeedback(row.activity) && !row.hasFeedback" 
                type="warning" 
                size="small"
                @click="openFeedbackDialog(row)"
              >
                评价
              </el-button>
              <el-tag v-if="row.hasFeedback" type="success" size="small">已评价</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="registeredActivities.length === 0 && !loading" description="您还没有报名任何活动">
          <el-button type="primary" @click="$router.push('/activities')">浏览活动</el-button>
        </el-empty>
      </el-tab-pane>
      
      <!-- 历史活动 -->
      <el-tab-pane label="历史记录" name="history">
        <el-timeline v-if="historyActivities.length > 0">
          <el-timeline-item
            v-for="item in historyActivities"
            :key="item.id"
            :timestamp="formatDate(item.activity?.endTime)"
            placement="top"
          >
            <el-card shadow="hover" class="history-card" @click="$router.push(`/activities/${item.activity?.id}`)">
              <div class="history-header">
                <h4>{{ item.activity?.title }}</h4>
                <el-tag v-if="item.hasFeedback" type="success" size="small">已评价</el-tag>
              </div>
              <p class="history-meta">
                <span>{{ item.activity?.clubName }}</span>
                <span>{{ item.activity?.location }}</span>
              </p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无历史活动记录" />
      </el-tab-pane>
      
      <!-- 我的评价 -->
      <el-tab-pane label="我的评价" name="feedbacks">
        <div v-if="myFeedbacks.length > 0" class="feedback-list">
          <el-card v-for="fb in myFeedbacks" :key="fb.id" class="feedback-card" shadow="never">
            <div class="feedback-header">
              <h4 @click="$router.push(`/activities/${fb.activityId}`)">{{ fb.activityTitle }}</h4>
              <el-rate :model-value="fb.rating" disabled />
            </div>
            <p class="feedback-content">{{ fb.content }}</p>
            <div class="feedback-footer">
              <span>{{ formatDateTime(fb.createdAt) }}</span>
            </div>
          </el-card>
        </div>
        <el-empty v-else description="您还没有发表过评价" />
      </el-tab-pane>
    </el-tabs>
    
    <!-- 评价对话框 -->
    <el-dialog v-model="showFeedbackDialog" title="评价活动" width="500px">
      <div class="feedback-activity-info" v-if="currentActivity">
        <h4>{{ currentActivity.title }}</h4>
      </div>
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
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  getMyRegistrations, 
  cancelRegistration, 
  submitActivityFeedback,
  getMyFeedbacks
} from '@/api/activity'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('registered')

const registeredActivities = ref([])
const myFeedbacks = ref([])

const historyActivities = computed(() => {
  const now = new Date()
  return registeredActivities.value.filter(item => {
    const end = new Date(item.activity?.endTime)
    return now > end
  })
})

const getStatusType = (activity) => {
  if (!activity) return 'info'
  const now = new Date()
  const start = new Date(activity.startTime)
  const end = new Date(activity.endTime)
  const regEnd = new Date(activity.registrationDeadline)
  
  if (now < regEnd) return 'success'
  if (now >= start && now <= end) return 'warning'
  return 'info'
}

const getStatusText = (activity) => {
  if (!activity) return '-'
  const now = new Date()
  const start = new Date(activity.startTime)
  const end = new Date(activity.endTime)
  const regEnd = new Date(activity.registrationDeadline)
  
  if (now < regEnd) return '报名中'
  if (now < start) return '即将开始'
  if (now >= start && now <= end) return '进行中'
  return '已结束'
}

const canCancel = (activity) => {
  if (!activity) return false
  const now = new Date()
  const start = new Date(activity.startTime)
  return now < start
}

const canFeedback = (activity) => {
  if (!activity) return false
  const now = new Date()
  const end = new Date(activity.endTime)
  return now > end
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

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const loadData = async () => {
  loading.value = true
  try {
    const [regRes, fbRes] = await Promise.all([
      getMyRegistrations(),
      getMyFeedbacks()
    ])
    registeredActivities.value = regRes.data?.content || regRes.data || []
    myFeedbacks.value = fbRes.data?.content || fbRes.data || []
  } catch (e) {
    console.error('加载数据失败', e)
  } finally {
    loading.value = false
  }
}

// 取消报名
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消报名吗？', '提示', { type: 'warning' })
    await cancelRegistration(row.activity.id)
    ElMessage.success('已取消报名')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消报名失败', e)
    }
  }
}

// 评价
const showFeedbackDialog = ref(false)
const submittingFeedback = ref(false)
const currentActivity = ref(null)
const feedbackForm = reactive({
  rating: 5,
  content: ''
})

const openFeedbackDialog = (row) => {
  currentActivity.value = row.activity
  feedbackForm.rating = 5
  feedbackForm.content = ''
  showFeedbackDialog.value = true
}

const submitFeedback = async () => {
  submittingFeedback.value = true
  try {
    await submitActivityFeedback(currentActivity.value.id, feedbackForm)
    ElMessage.success('评价提交成功')
    showFeedbackDialog.value = false
    loadData()
  } catch (e) {
    console.error('提交评价失败', e)
  } finally {
    submittingFeedback.value = false
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadData()
})
</script>

<style scoped>
.my-activities-page h1 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
}

.activity-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.cover-thumb {
  width: 80px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.activity-info h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.club-name {
  font-size: 12px;
  color: #909399;
}

.history-card {
  cursor: pointer;
  transition: all 0.3s;
}

.history-card:hover {
  transform: translateX(4px);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-header h4 {
  font-size: 16px;
  font-weight: 600;
}

.history-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.feedback-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.feedback-card {
  border-left: 3px solid #409eff;
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.feedback-header h4 {
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  color: #409eff;
}

.feedback-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.feedback-footer {
  font-size: 12px;
  color: #909399;
}

.feedback-activity-info {
  background: #f4f4f5;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.feedback-activity-info h4 {
  font-size: 15px;
  font-weight: 600;
}
</style>
