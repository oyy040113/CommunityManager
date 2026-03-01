<template>
  <div class="activity-approval-page">
    <div class="page-header">
      <h1>活动审批</h1>
      <el-tag :type="userStore.isAdmin ? 'danger' : 'success'" size="large">
        {{ userStore.isAdmin ? '超级管理员' : '指导老师' }}
      </el-tag>
    </div>
    
    <!-- 搜索过滤 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input 
            v-model="searchKeyword" 
            placeholder="活动名称/社团名称" 
            clearable 
            style="width: 200px"
            @keyup.enter="loadActivities"
          />
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="searchApprovalStatus" placeholder="全部" clearable style="width: 150px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchStatus" placeholder="全部" clearable style="width: 150px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="报名截止" value="REGISTRATION_CLOSED" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadActivities">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 活动列表 -->
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审批" name="pending">
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="tab-badge" />
        </el-tab-pane>
        <el-tab-pane label="已审批" name="reviewed" />
        <el-tab-pane label="全部" name="all" />
      </el-tabs>
      
      <el-table :data="activities" v-loading="loading" stripe>
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <el-image 
              v-if="row.coverImage"
              :src="row.coverImage" 
              fit="cover"
              class="cover-image"
            >
              <template #error>
                <div class="cover-fallback">
                  {{ row.title?.charAt(0) }}
                </div>
              </template>
            </el-image>
            <div v-else class="cover-fallback">
              {{ row.title?.charAt(0) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活动信息" min-width="250">
          <template #default="{ row }">
            <div class="activity-info">
              <h4>{{ row.title }}</h4>
              <div class="info-meta">
                <el-tag size="small" type="success">{{ row.clubName }}</el-tag>
                <span class="organizer">组织者: {{ row.organizerName }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="180">
          <template #default="{ row }">
            <div class="time-info">
              <div>{{ formatDateTime(row.startTime) }}</div>
              <div class="end-time">至 {{ formatDateTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="150" show-overflow-tooltip />
        <el-table-column label="报名" width="100">
          <template #default="{ row }">
            <div class="registration-info">
              {{ row.currentParticipants || 0 }} / {{ row.maxParticipants || '不限' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活动状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getApprovalTagType(row.approvalStatus)" size="small">
              {{ getApprovalLabel(row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="viewDetail(row)">
              查看详情
            </el-button>
            <template v-if="row.approvalStatus === 'PENDING'">
              <el-button type="success" text size="small" @click="approveActivity(row, true)">
                通过
              </el-button>
              <el-button type="danger" text size="small" @click="approveActivity(row, false)">
                拒绝
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadActivities"
          @current-change="loadActivities"
        />
      </div>
    </el-card>
    
    <!-- 活动详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="活动详情" width="800px">
      <el-descriptions v-if="selectedActivity" :column="2" border>
        <el-descriptions-item label="活动标题" :span="2">
          {{ selectedActivity.title }}
        </el-descriptions-item>
        <el-descriptions-item label="所属社团">
          {{ selectedActivity.clubName }}
        </el-descriptions-item>
        <el-descriptions-item label="组织者">
          {{ selectedActivity.organizerName }}
        </el-descriptions-item>
        <el-descriptions-item label="活动地点" :span="2">
          {{ selectedActivity.location }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">
          {{ formatDateTime(selectedActivity.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ formatDateTime(selectedActivity.endTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="报名截止">
          {{ formatDateTime(selectedActivity.registrationDeadline) }}
        </el-descriptions-item>
        <el-descriptions-item label="人数限制">
          {{ selectedActivity.maxParticipants || '不限' }}
        </el-descriptions-item>
        <el-descriptions-item label="活动状态">
          <el-tag :type="getStatusTagType(selectedActivity.status)">
            {{ getStatusLabel(selectedActivity.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getApprovalTagType(selectedActivity.approvalStatus)">
            {{ getApprovalLabel(selectedActivity.approvalStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="selectedActivity.approvedBy" label="审批人">
          {{ selectedActivity.approvedBy }}
        </el-descriptions-item>
        <el-descriptions-item v-if="selectedActivity.approvedAt" label="审批时间">
          {{ formatDateTime(selectedActivity.approvedAt) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="selectedActivity.rejectReason" label="拒绝原因" :span="2">
          <el-text type="danger">{{ selectedActivity.rejectReason }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="活动简介" :span="2">
          {{ selectedActivity.description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <template v-if="selectedActivity?.approvalStatus === 'PENDING'">
          <el-button type="success" @click="approveActivity(selectedActivity, true)">
            通过审批
          </el-button>
          <el-button type="danger" @click="approveActivity(selectedActivity, false)">
            拒绝审批
          </el-button>
        </template>
      </template>
    </el-dialog>
    
    <!-- 审批对话框 -->
    <el-dialog v-model="showApprovalDialog" :title="approvalAction ? '通过活动' : '拒绝活动'" width="500px">
      <el-form label-width="100px">
        <el-form-item label="活动名称">
          <div>{{ approvalActivity?.title }}</div>
        </el-form-item>
        <el-form-item label="所属社团">
          <div>{{ approvalActivity?.clubName }}</div>
        </el-form-item>
        <el-form-item v-if="!approvalAction" label="拒绝原因" required>
          <el-input 
            v-model="rejectReason" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApprovalDialog = false">取消</el-button>
        <el-button 
          :type="approvalAction ? 'success' : 'danger'" 
          :loading="approving" 
          @click="confirmApproval"
        >
          确认{{ approvalAction ? '通过' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  getPendingActivities, 
  searchActivitiesAdmin, 
  approveActivity as approveActivityApi 
} from '@/api/activity'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

// 搜索相关
const searchKeyword = ref('')
const searchApprovalStatus = ref(null)
const searchStatus = ref(null)
const activeTab = ref('pending')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 活动列表
const activities = ref([])
const loading = ref(false)
const pendingCount = ref(0)

// 状态映射
const statusMap = {
  DRAFT: { label: '草稿', type: 'info' },
  PUBLISHED: { label: '已发布', type: 'success' },
  REGISTRATION_CLOSED: { label: '报名截止', type: 'warning' },
  ONGOING: { label: '进行中', type: '' },
  COMPLETED: { label: '已完成', type: 'info' },
  CANCELLED: { label: '已取消', type: 'danger' }
}

const approvalStatusMap = {
  PENDING: { label: '待审批', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' }
}

const getStatusLabel = (status) => statusMap[status]?.label || status
const getStatusTagType = (status) => statusMap[status]?.type || ''
const getApprovalLabel = (status) => approvalStatusMap[status]?.label || status
const getApprovalTagType = (status) => approvalStatusMap[status]?.type || ''

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

// 加载活动列表
const loadActivities = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      approvalStatus: searchApprovalStatus.value || undefined,
      status: searchStatus.value || undefined
    }
    
    // 根据标签页加载不同数据
    if (activeTab.value === 'pending') {
      params.approvalStatus = 'PENDING'
    } else if (activeTab.value === 'reviewed') {
      params.approvalStatus = undefined
      // 排除待审批的
    }
    
    const res = await searchActivitiesAdmin(params)
    activities.value = res.data.content || []
    total.value = res.data.totalElements || 0
    
    // 更新待审批数量
    if (activeTab.value !== 'pending') {
      loadPendingCount()
    } else {
      pendingCount.value = total.value
    }
  } catch (e) {
    console.error('加载活动列表失败', e)
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

// 加载待审批数量
const loadPendingCount = async () => {
  try {
    const res = await getPendingActivities({ page: 0, size: 1 })
    pendingCount.value = res.data.totalElements || 0
  } catch (e) {
    console.error('加载待审批数量失败', e)
  }
}

// 标签页切换
const handleTabChange = () => {
  currentPage.value = 1
  loadActivities()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  searchApprovalStatus.value = null
  searchStatus.value = null
  currentPage.value = 1
  loadActivities()
}

// 查看详情
const showDetailDialog = ref(false)
const selectedActivity = ref(null)

const viewDetail = (activity) => {
  selectedActivity.value = activity
  showDetailDialog.value = true
}

// 审批活动
const showApprovalDialog = ref(false)
const approvalActivity = ref(null)
const approvalAction = ref(true) // true=通过, false=拒绝
const rejectReason = ref('')
const approving = ref(false)

const approveActivity = (activity, approved) => {
  approvalActivity.value = activity
  approvalAction.value = approved
  rejectReason.value = ''
  showApprovalDialog.value = true
}

const confirmApproval = async () => {
  if (!approvalAction.value && !rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  approving.value = true
  try {
    await approveActivityApi(approvalActivity.value.id, {
      approved: approvalAction.value,
      rejectReason: approvalAction.value ? undefined : rejectReason.value
    })
    
    ElMessage.success(approvalAction.value ? '活动已通过审批' : '活动已拒绝')
    showApprovalDialog.value = false
    showDetailDialog.value = false
    loadActivities()
  } catch (e) {
    console.error('审批活动失败', e)
  } finally {
    approving.value = false
  }
}

onMounted(() => {
  if (!userStore.isAdminOrTeacher) {
    ElMessage.error('只有管理员或指导老师可以访问此页面')
    router.push('/')
    return
  }
  loadActivities()
})
</script>

<style scoped>
.activity-approval-page {
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
  color: #303133;
  margin: 0;
}

.search-card {
  margin-bottom: 20px;
}

.tab-badge {
  position: relative;
  top: -10px;
}

.cover-image {
  width: 80px;
  height: 60px;
  border-radius: 4px;
}

.cover-fallback {
  width: 80px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 24px;
  font-weight: 600;
  border-radius: 4px;
}

.activity-info h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.info-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #909399;
}

.time-info {
  font-size: 13px;
  line-height: 1.6;
}

.end-time {
  color: #909399;
}

.registration-info {
  text-align: center;
  font-weight: 600;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
