<template>
  <div class="club-list-page">
    <div class="page-header">
      <h1>社团列表</h1>
      <el-button v-if="userStore.isLoggedIn" type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon> 创建社团
      </el-button>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="社团名称/简介" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.type" placeholder="全部类型" clearable>
            <el-option v-for="(item, key) in clubTypeMap" :key="key" :label="item.name" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadClubs">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 社团列表 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="club in clubs" :key="club.id">
        <el-card class="club-card" shadow="hover" @click="$router.push(`/clubs/${club.id}`)">
          <div class="club-logo">
            <el-avatar :size="80" :src="club.logo">
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
              <span class="stat-value">{{ club.memberCount }}</span>
              <span class="stat-label">成员</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ club.activityCount }}</span>
              <span class="stat-label">活动</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ club.feedbackCount || 0 }}人 · {{ (club.averageRating || 0).toFixed(1) }}分</span>
              <span class="stat-label">活动评价</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 空状态 -->
    <el-empty v-if="clubs.length === 0 && !loading" description="暂无社团数据" />
    
    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadClubs"
        @current-change="loadClubs"
      />
    </div>
    
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
        <el-form-item label="联系邮箱">
          <el-input v-model="createForm.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
        <el-form-item label="活动地点">
          <el-input v-model="createForm.location" placeholder="请输入常用活动地点" />
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
import { searchClubs, createClub } from '@/api/club'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const clubs = ref([])
const total = ref(0)

const filters = reactive({
  keyword: '',
  type: ''
})

const pagination = reactive({
  page: 1,
  size: 8
})

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

const loadClubs = async () => {
  loading.value = true
  try {
    const res = await searchClubs({
      keyword: filters.keyword || undefined,
      type: filters.type || undefined,
      page: pagination.page - 1,
      size: pagination.size
    })
    clubs.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } catch (e) {
    console.error('加载社团列表失败', e)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.keyword = ''
  filters.type = ''
  pagination.page = 1
  loadClubs()
}

// 创建社团
const showCreateDialog = ref(false)
const createFormRef = ref(null)
const creating = ref(false)

const createForm = reactive({
  name: '',
  type: '',
  purpose: '',
  description: '',
  contactEmail: '',
  location: ''
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
    ElMessage.success('社团申请已提交，请等待管理员审批')
    showCreateDialog.value = false
    // 不跳转，因为社团还在待审批状态
    loadClubs()
  } catch (e) {
    console.error('创建社团失败', e)
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  loadClubs()
})
</script>

<style scoped>
.club-list-page {
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

.filter-card {
  margin-bottom: 20px;
}

.club-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
