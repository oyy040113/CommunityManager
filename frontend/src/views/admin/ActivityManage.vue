<template>
  <div class="activity-manage-page">
    <div class="page-header">
      <h1>活动管理</h1>
      <el-button type="primary" @click="openActivityDialog()">
        <el-icon><Plus /></el-icon> 创建活动
      </el-button>
    </div>
    
    <!-- 活动列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <el-select v-model="selectedClubId" placeholder="选择社团" clearable @change="loadActivities">
            <el-option v-for="club in myClubs" :key="club.id" :label="club.name" :value="club.id" />
          </el-select>
          <el-input v-model="searchKeyword" placeholder="搜索活动" style="width: 200px" clearable />
        </div>
      </template>
      
      <el-table :data="filteredActivities" v-loading="loading">
        <el-table-column label="活动信息" min-width="250">
          <template #default="{ row }">
            <div class="activity-cell">
              <img :src="row.coverImage || '/default-cover.svg'" class="cover-thumb" />
              <div class="activity-info">
                <h4>{{ row.title }}</h4>
                <span class="club-name">{{ row.clubName }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="200">
          <template #default="{ row }">
            <div class="time-cell">
              <div>{{ formatDateTime(row.startTime) }}</div>
              <div class="end-time">至 {{ formatDateTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column label="报名/签到" width="140">
          <template #default="{ row }">
            <div v-if="['ONGOING', 'COMPLETED'].includes(row.status)">
              <div>签到 {{ row.checkedInCount || 0 }} / 报名 {{ row.registrationCount || 0 }}</div>
              <div class="end-time">上限 {{ row.maxParticipants || '不限' }}</div>
            </div>
            <span v-else>{{ row.registrationCount || 0 }} / {{ row.maxParticipants || '不限' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">{{ getStatusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openActivityDialog(row)">编辑</el-button>
            <el-button type="info" text size="small" @click="viewParticipants(row)">参与者</el-button>
            <el-button type="danger" text size="small" @click="deleteActivity(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 活动编辑对话框 -->
    <el-dialog 
      v-model="showActivityDialog" 
      :title="editingActivity ? '编辑活动' : '创建活动'"
      width="700px"
    >
      <el-form 
        ref="activityFormRef"
        :model="activityForm" 
        :rules="activityRules"
        label-width="100px"
      >
        <el-form-item label="封面图片">
          <el-upload
            class="cover-uploader"
            :show-file-list="false"
            :before-upload="beforeCoverUpload"
            :http-request="uploadCover"
          >
            <el-image 
              v-if="activityForm.coverImage" 
              :src="activityForm.coverImage"
              fit="cover"
              class="upload-cover"
            >
              <template #error>
                <div class="cover-placeholder">
                  <el-icon><Plus /></el-icon>
                  <div>上传封面</div>
                </div>
              </template>
            </el-image>
            <div v-else class="cover-placeholder">
              <el-icon><Plus /></el-icon>
              <div>上传封面</div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="activityForm.clubId" placeholder="请选择社团" style="width: 100%">
            <el-option v-for="club in myClubs" :key="club.id" :label="club.name" :value="club.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="activityForm.title" placeholder="请输入活动标题" />
        </el-form-item>
        <el-form-item label="活动简介" prop="description">
          <el-input v-model="activityForm.description" type="textarea" :rows="4" placeholder="请输入活动简介" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="activityForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="activityForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="报名截止" prop="registrationDeadline">
          <el-date-picker
            v-model="activityForm.registrationDeadline"
            type="datetime"
            placeholder="选择报名截止时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动地点" prop="location">
              <el-input v-model="activityForm.location" placeholder="请输入活动地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="人数限制">
              <el-input-number v-model="activityForm.maxParticipants" :min="0" placeholder="0表示不限" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showActivityDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingActivity" @click="saveActivity">
          {{ editingActivity ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 参与者对话框 -->
    <el-dialog v-model="showParticipantsDialog" title="参与者列表" width="900px">
      <el-table :data="participants" v-loading="participantsLoading">
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar">{{ (row.realName || row.username)?.charAt(0) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="100" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column label="报名时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.registeredAt) }}</template>
        </el-table-column>
        <el-table-column label="签到" width="160">
          <template #default="{ row }">
            <el-tag v-if="row.checkedIn" type="success">已签到</el-tag>
            <el-button 
              v-else-if="isActivityStarted(viewingActivity)" 
              type="primary" 
              size="small" 
              @click="handleManualCheckIn(row)"
            >
              签到
            </el-button>
            <el-tag v-else type="info">未签到</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="participants.length === 0 && !participantsLoading" description="暂无参与者" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyManagedClubs, searchClubsAdmin } from '@/api/club'
import { 
  getClubActivities, 
  createActivity, 
  updateActivity, 
  deleteActivity as deleteActivityApi,
  getActivityParticipants,
  manualCheckIn
} from '@/api/activity'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const myClubs = ref([])
const activities = ref([])
const selectedClubId = ref('')
const searchKeyword = ref('')

const filteredActivities = computed(() => {
  let result = activities.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(a => a.title?.toLowerCase().includes(keyword))
  }
  return result
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
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatLocalDateTime = (date) => {
  if (!date) return null
  const d = date instanceof Date ? date : new Date(date)
  const pad = (n) => String(n).padStart(2, '0')

  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const loadMyClubs = async () => {
  try {
    if (userStore.isAdmin) {
      const res = await searchClubsAdmin({
        status: 'APPROVED',
        page: 0,
        size: 1000
      })
      myClubs.value = res.data?.content || []
    } else {
      const res = await getMyManagedClubs()
      myClubs.value = res.data || []
    }

    if (myClubs.value.length > 0 && !selectedClubId.value) {
      selectedClubId.value = myClubs.value[0].id
      loadActivities()
    }
  } catch (e) {
    console.error('加载社团失败', e)
  }
}

const loadActivities = async () => {
  if (!selectedClubId.value) {
    activities.value = []
    return
  }
  
  loading.value = true
  try {
    const res = await getClubActivities(selectedClubId.value)
    activities.value = res.data?.content || res.data || []
  } catch (e) {
    console.error('加载活动失败', e)
  } finally {
    loading.value = false
  }
}

// 活动编辑
const showActivityDialog = ref(false)
const activityFormRef = ref(null)
const editingActivity = ref(null)
const savingActivity = ref(false)

const activityForm = reactive({
  clubId: '',
  title: '',
  description: '',
  coverImage: '',
  startTime: null,
  endTime: null,
  registrationDeadline: null,
  location: '',
  maxParticipants: 0
})

const activityRules = {
  clubId: [{ required: true, message: '请选择社团', trigger: 'change' }],
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  registrationDeadline: [{ required: true, message: '请选择报名截止时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }]
}

const openActivityDialog = (activity = null) => {
  editingActivity.value = activity
  if (activity) {
    Object.assign(activityForm, {
      clubId: activity.clubId,
      title: activity.title,
      description: activity.description || '',
      coverImage: activity.coverImage || '',
      startTime: new Date(activity.startTime),
      endTime: new Date(activity.endTime),
      registrationDeadline: new Date(activity.registrationDeadline),
      location: activity.location,
      maxParticipants: activity.maxParticipants || 0
    })
  } else {
    Object.assign(activityForm, {
      clubId: selectedClubId.value,
      title: '',
      description: '',
      coverImage: '',
      startTime: null,
      endTime: null,
      registrationDeadline: null,
      location: '',
      maxParticipants: 0
    })
  }
  showActivityDialog.value = true
}

// 封面上传
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

const uploadCover = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  
  try {
    const res = await request.post('/files/upload/activity-cover', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    if (res.data) {
      activityForm.coverImage = res.data
      ElMessage.success('封面上传成功')
    }
  } catch (e) {
    console.error('上传封面失败', e)
    ElMessage.error(e.message || '上传封面失败')
  }
}

const saveActivity = async () => {
  const valid = await activityFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  savingActivity.value = true
  try {
    const data = {
      ...activityForm,
      startTime: formatLocalDateTime(activityForm.startTime),
      endTime: formatLocalDateTime(activityForm.endTime),
      registrationDeadline: formatLocalDateTime(activityForm.registrationDeadline),
      maxParticipants: activityForm.maxParticipants || null
    }
    
    if (editingActivity.value) {
      await updateActivity(editingActivity.value.id, data)
    } else {
      await createActivity(data)
    }
    
    ElMessage.success(editingActivity.value ? '保存成功' : '创建成功')
    showActivityDialog.value = false
    loadActivities()
  } catch (e) {
    console.error('保存活动失败', e)
  } finally {
    savingActivity.value = false
  }
}

const deleteActivity = async (activity) => {
  try {
    await ElMessageBox.confirm(`确定要删除活动"${activity.title}"吗？`, '提示', { type: 'warning' })
    await deleteActivityApi(activity.id)
    ElMessage.success('已删除')
    loadActivities()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除活动失败', e)
    }
  }
}

// 参与者
const showParticipantsDialog = ref(false)
const participants = ref([])
const participantsLoading = ref(false)
const viewingActivity = ref(null)

const isActivityStarted = (activity) => {
  if (!activity) return false
  return new Date() >= new Date(activity.startTime)
}

const viewParticipants = async (activity) => {
  viewingActivity.value = activity
  showParticipantsDialog.value = true
  participantsLoading.value = true
  try {
    const res = await getActivityParticipants(activity.id)
    participants.value = res.data?.content || res.data || []
  } catch (e) {
    console.error('加载参与者失败', e)
  } finally {
    participantsLoading.value = false
  }
}

const handleManualCheckIn = async (registration) => {
  try {
    await manualCheckIn(viewingActivity.value.id, registration.userId)
    ElMessage.success(`已为 ${registration.realName || registration.username} 签到`)
    // 刷新参与者列表
    const res = await getActivityParticipants(viewingActivity.value.id)
    participants.value = res.data?.content || res.data || []
  } catch (e) {
    console.error('签到失败', e)
  }
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
.activity-manage-page {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-cell {
  display: flex;
  align-items: center;
  gap: 12px;
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

.time-cell {
  font-size: 13px;
}

.end-time {
  color: #909399;
  font-size: 12px;
}
</style>
