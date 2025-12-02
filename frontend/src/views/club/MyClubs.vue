<template>
  <div class="my-clubs-page">
    <h1>我的社团</h1>
    
    <el-tabs v-model="activeTab">
      <!-- 已加入的社团 -->
      <el-tab-pane label="已加入" name="joined">
        <el-row :gutter="20" v-loading="loading">
          <el-col :span="6" v-for="item in joinedClubs" :key="item.id">
            <el-card class="club-card" shadow="hover">
              <div class="club-logo">
                <el-avatar :size="64" :src="item.club?.logo">
                  {{ item.club?.name?.charAt(0) }}
                </el-avatar>
                <el-badge v-if="item.role === 'LEADER'" value="负责人" class="role-badge" />
              </div>
              <h3 class="club-name">{{ item.club?.name }}</h3>
              <el-tag size="small">{{ getRoleName(item.role) }}</el-tag>
              <p class="join-time">加入时间：{{ formatDate(item.joinedAt) }}</p>
              <el-divider />
              <div class="card-actions">
                <el-button text type="primary" @click="$router.push(`/clubs/${item.club?.id}`)">
                  查看详情
                </el-button>
                <el-button 
                  v-if="item.role === 'LEADER'" 
                  text 
                  type="warning"
                  @click="$router.push(`/admin/club/${item.club?.id}`)"
                >
                  管理社团
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="joinedClubs.length === 0 && !loading" description="您还没有加入任何社团">
          <el-button type="primary" @click="$router.push('/clubs')">浏览社团</el-button>
        </el-empty>
      </el-tab-pane>
      
      <!-- 我创建的社团 -->
      <el-tab-pane label="我创建的" name="created">
        <el-row :gutter="20" v-loading="loading">
          <el-col :span="6" v-for="club in createdClubs" :key="club.id">
            <el-card class="club-card" shadow="hover">
              <div class="club-logo">
                <el-avatar :size="64" :src="club.logo">
                  {{ club.name?.charAt(0) }}
                </el-avatar>
              </div>
              <h3 class="club-name">{{ club.name }}</h3>
              <el-tag :type="getClubTypeTag(club.type)" size="small">
                {{ getClubTypeName(club.type) }}
              </el-tag>
              <div class="club-stats">
                <span><el-icon><User /></el-icon> {{ club.memberCount }} 成员</span>
                <span><el-icon><Calendar /></el-icon> {{ club.activityCount }} 活动</span>
              </div>
              <el-divider />
              <div class="card-actions">
                <el-button text type="primary" @click="$router.push(`/clubs/${club.id}`)">
                  查看详情
                </el-button>
                <el-button text type="warning" @click="$router.push(`/admin/club/${club.id}`)">
                  管理社团
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="createdClubs.length === 0 && !loading" description="您还没有创建社团">
          <el-button type="primary" @click="showCreateDialog = true">创建社团</el-button>
        </el-empty>
      </el-tab-pane>
      
      <!-- 待审批 -->
      <el-tab-pane label="待审批" name="pending">
        <el-table :data="pendingApprovals" v-loading="loading">
          <el-table-column label="社团" min-width="200">
            <template #default="{ row }">
              <div class="club-cell">
                <el-avatar :size="40" :src="row.club?.logo">{{ row.club?.name?.charAt(0) }}</el-avatar>
                <span>{{ row.club?.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="申请人" prop="userName" width="120" />
          <el-table-column label="申请时间" width="180">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handleApprove(row, 'APPROVED')">通过</el-button>
              <el-button type="danger" size="small" @click="handleApprove(row, 'REJECTED')">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="pendingApprovals.length === 0 && !loading" description="暂无待审批的申请" />
      </el-tab-pane>
    </el-tabs>
    
    <!-- 创建社团对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建社团" width="600px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="社团名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入社团名称" />
        </el-form-item>
        <el-form-item label="社团类型" prop="type">
          <el-select v-model="createForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option v-for="(item, key) in clubTypeMap" :key="key" :label="item.name" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="社团宗旨" prop="purpose">
          <el-input v-model="createForm.purpose" type="textarea" :rows="2" placeholder="请输入社团宗旨" />
        </el-form-item>
        <el-form-item label="社团简介" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="4" placeholder="请输入社团简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyClubs, getMyCreatedClubs, createClub, getPendingApprovals, approveJoinRequest } from '@/api/club'
import { ElMessage } from 'element-plus'
import { User, Calendar } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('joined')

const joinedClubs = ref([])
const createdClubs = ref([])
const pendingApprovals = ref([])

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

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const loadData = async () => {
  loading.value = true
  try {
    const [joinedRes, createdRes, pendingRes] = await Promise.all([
      getMyClubs(),
      getMyCreatedClubs(),
      getPendingApprovals()
    ])
    joinedClubs.value = joinedRes.data || []
    createdClubs.value = createdRes.data || []
    pendingApprovals.value = pendingRes.data || []
  } catch (e) {
    console.error('加载数据失败', e)
  } finally {
    loading.value = false
  }
}

// 审批
const handleApprove = async (row, status) => {
  try {
    await approveJoinRequest(row.id, status)
    ElMessage.success(status === 'APPROVED' ? '已通过' : '已拒绝')
    loadData()
  } catch (e) {
    console.error('审批失败', e)
  }
}

// 创建社团
const showCreateDialog = ref(false)
const createFormRef = ref(null)
const creating = ref(false)

const createForm = reactive({
  name: '',
  type: '',
  purpose: '',
  description: ''
})

const createRules = {
  name: [{ required: true, message: '请输入社团名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择社团类型', trigger: 'change' }]
}

const handleCreate = async () => {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  creating.value = true
  try {
    const res = await createClub(createForm)
    ElMessage.success('社团创建成功')
    showCreateDialog.value = false
    router.push(`/clubs/${res.data.id}`)
  } catch (e) {
    console.error('创建社团失败', e)
  } finally {
    creating.value = false
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
.my-clubs-page h1 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
}

.club-card {
  text-align: center;
  margin-bottom: 20px;
}

.club-logo {
  position: relative;
  display: inline-block;
  margin-bottom: 12px;
}

.role-badge {
  position: absolute;
  top: -8px;
  right: -24px;
}

.club-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.join-time {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.club-stats {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 12px;
  font-size: 13px;
  color: #606266;
}

.club-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.club-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
