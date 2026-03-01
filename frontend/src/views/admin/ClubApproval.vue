<template>
  <div class="club-approval-page">
    <div class="page-header">
      <h1>社团审批</h1>
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
            placeholder="社团名称"
            clearable
            style="width: 200px"
            @keyup.enter="loadClubs"
          />
        </el-form-item>
        <el-form-item label="社团类型">
          <el-select v-model="searchType" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="(item, key) in clubTypeMap" :key="key" :label="item.name" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="全部" clearable style="width: 150px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已解散" value="DISSOLVED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadClubs">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 社团列表 -->
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审批" name="pending">
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="tab-badge" />
        </el-tab-pane>
        <el-tab-pane label="全部" name="all" />
      </el-tabs>

      <el-table :data="clubs" v-loading="loading" stripe>
        <el-table-column label="社团名称" min-width="200">
          <template #default="{ row }">
            <div class="club-info">
              <el-avatar :size="40" :src="row.logo">
                {{ row.name?.charAt(0) }}
              </el-avatar>
              <div class="club-detail">
                <h4>{{ row.name }}</h4>
                <el-tag size="small" :type="getClubTypeTag(row.type)">
                  {{ getClubTypeName(row.type) }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="宗旨" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.purpose || '暂无' }}
          </template>
        </el-table-column>
        <el-table-column label="创建者" width="120">
          <template #default="{ row }">
            {{ row.creatorName }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批人" width="120">
          <template #default="{ row }">
            {{ row.approvedByName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="viewDetail(row)">
              查看详情
            </el-button>
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" text size="small" @click="handleApprove(row, true)">
                通过
              </el-button>
              <el-button type="danger" text size="small" @click="handleApprove(row, false)">
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
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadClubs"
          @current-change="loadClubs"
        />
      </div>
    </el-card>

    <!-- 社团详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="社团详情" width="700px">
      <el-descriptions v-if="selectedClub" :column="2" border>
        <el-descriptions-item label="社团名称" :span="2">
          {{ selectedClub.name }}
        </el-descriptions-item>
        <el-descriptions-item label="社团类型">
          <el-tag :type="getClubTypeTag(selectedClub.type)" size="small">
            {{ getClubTypeName(selectedClub.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(selectedClub.status)" size="small">
            {{ getStatusLabel(selectedClub.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建者">
          {{ selectedClub.creatorName }}
        </el-descriptions-item>
        <el-descriptions-item label="负责人">
          {{ selectedClub.leaderName }}
        </el-descriptions-item>
        <el-descriptions-item label="联系邮箱">
          {{ selectedClub.contactEmail || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="活动地点">
          {{ selectedClub.location || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="社团宗旨" :span="2">
          {{ selectedClub.purpose || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="社团简介" :span="2">
          {{ selectedClub.description || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(selectedClub.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="审批时间">
          {{ formatDateTime(selectedClub.approvedAt) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="selectedClub.approvedByName" label="审批人">
          {{ selectedClub.approvedByName }}
        </el-descriptions-item>
        <el-descriptions-item v-if="selectedClub.rejectReason" label="拒绝原因" :span="2">
          <el-text type="danger">{{ selectedClub.rejectReason }}</el-text>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <template v-if="selectedClub?.status === 'PENDING'">
          <el-button type="success" @click="handleApprove(selectedClub, true)">
            通过审批
          </el-button>
          <el-button type="danger" @click="handleApprove(selectedClub, false)">
            拒绝审批
          </el-button>
        </template>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="showApprovalDialog" :title="approvalAction ? '通过社团申请' : '拒绝社团申请'" width="500px">
      <el-form label-width="100px">
        <el-form-item label="社团名称">
          <div>{{ approvalClub?.name }}</div>
        </el-form-item>
        <el-form-item label="创建者">
          <div>{{ approvalClub?.creatorName }}</div>
        </el-form-item>
        <el-form-item label="社团类型">
          <el-tag :type="getClubTypeTag(approvalClub?.type)" size="small">
            {{ getClubTypeName(approvalClub?.type) }}
          </el-tag>
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
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getPendingClubs, searchClubsAdmin, approveClub } from '@/api/club'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const userStore = useUserStore()

// 搜索
const searchKeyword = ref('')
const searchType = ref(null)
const searchStatus = ref(null)
const activeTab = ref('pending')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 列表
const clubs = ref([])
const loading = ref(false)
const pendingCount = ref(0)

// 类型映射
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
  PENDING: { label: '待审批', type: 'warning' },
  ACTIVE: { label: '活跃', type: 'success' },
  INACTIVE: { label: '不活跃', type: 'info' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  SUSPENDED: { label: '已暂停', type: 'warning' },
  DISSOLVED: { label: '已解散', type: 'info' }
}

const getClubTypeName = (type) => clubTypeMap[type]?.name || type || '-'
const getClubTypeTag = (type) => clubTypeMap[type]?.tag || ''
const getStatusLabel = (status) => statusMap[status]?.label || status
const getStatusTagType = (status) => statusMap[status]?.type || ''

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

// 加载社团列表
const loadClubs = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'pending') {
      const res = await getPendingClubs({
        page: currentPage.value - 1,
        size: pageSize.value
      })
      clubs.value = res.data?.content || []
      total.value = res.data?.totalElements || 0
      pendingCount.value = total.value
    } else {
      const res = await searchClubsAdmin({
        keyword: searchKeyword.value || undefined,
        type: searchType.value || undefined,
        status: searchStatus.value || undefined,
        page: currentPage.value - 1,
        size: pageSize.value
      })
      clubs.value = res.data?.content || []
      total.value = res.data?.totalElements || 0
    }
  } catch (e) {
    console.error('加载社团列表失败', e)
  } finally {
    loading.value = false
  }
}

// 加载待审批数量
const loadPendingCount = async () => {
  try {
    const res = await getPendingClubs({ page: 0, size: 1 })
    pendingCount.value = res.data?.totalElements || 0
  } catch (e) {
    // ignore
  }
}

const handleTabChange = () => {
  currentPage.value = 1
  loadClubs()
}

const resetSearch = () => {
  searchKeyword.value = ''
  searchType.value = null
  searchStatus.value = null
  currentPage.value = 1
  loadClubs()
}

// 查看详情
const showDetailDialog = ref(false)
const selectedClub = ref(null)

const viewDetail = (club) => {
  selectedClub.value = club
  showDetailDialog.value = true
}

// 审批
const showApprovalDialog = ref(false)
const approvalClub = ref(null)
const approvalAction = ref(true)
const rejectReason = ref('')
const approving = ref(false)

const handleApprove = (club, approved) => {
  approvalClub.value = club
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
    await approveClub(approvalClub.value.id, {
      approved: approvalAction.value,
      rejectReason: approvalAction.value ? undefined : rejectReason.value
    })
    ElMessage.success(approvalAction.value ? '社团审批已通过' : '社团审批已拒绝')
    showApprovalDialog.value = false
    showDetailDialog.value = false
    loadClubs()
    loadPendingCount()
  } catch (e) {
    console.error('审批失败', e)
  } finally {
    approving.value = false
  }
}

onMounted(() => {
  loadClubs()
  loadPendingCount()
})
</script>

<style scoped>
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

.search-card {
  margin-bottom: 20px;
}

.club-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.club-detail h4 {
  margin: 0 0 4px;
  font-size: 14px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.tab-badge {
  margin-left: 8px;
}
</style>
