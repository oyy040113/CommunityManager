<template>
  <div class="club-manage-page">
    <div class="page-header">
      <h1>社团管理 - {{ club?.name }}</h1>
      <el-button type="primary" @click="$router.push(`/clubs/${clubId}`)">
        <el-icon><View /></el-icon> 查看社团主页
      </el-button>
    </div>
    
    <el-tabs v-model="activeTab">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="info">
        <el-card>
          <el-form 
            ref="infoFormRef"
            :model="infoForm" 
            :rules="infoRules"
            label-width="100px"
            style="max-width: 600px"
          >
            <el-form-item label="社团Logo">
              <el-upload
                class="logo-uploader"
                :show-file-list="false"
                :before-upload="beforeLogoUpload"
                :http-request="uploadLogo"
              >
                <el-avatar 
                  v-if="infoForm.logo" 
                  :size="100" 
                  :src="infoForm.logo"
                  class="upload-avatar"
                />
                <div v-else class="upload-placeholder">
                  <el-icon><Plus /></el-icon>
                  <div>上传Logo</div>
                </div>
              </el-upload>
            </el-form-item>
            <el-form-item label="社团名称" prop="name">
              <el-input v-model="infoForm.name" />
            </el-form-item>
            <el-form-item label="社团类型" prop="type">
              <el-select v-model="infoForm.type" style="width: 100%">
                <el-option v-for="(item, key) in clubTypeMap" :key="key" :label="item.name" :value="key" />
              </el-select>
            </el-form-item>
            <el-form-item label="社团宗旨">
              <el-input v-model="infoForm.purpose" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="社团简介">
              <el-input v-model="infoForm.description" type="textarea" :rows="4" />
            </el-form-item>
            <el-form-item label="联系邮箱">
              <el-input v-model="infoForm.contactEmail" />
            </el-form-item>
            <el-form-item label="活动地点">
              <el-input v-model="infoForm.location" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveInfo">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 成员管理 -->
      <el-tab-pane label="成员管理" name="members">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>成员列表 ({{ members.length }}人)</span>
              <el-input 
                v-model="memberSearch"
                placeholder="搜索成员"
                style="width: 200px"
                clearable
              />
            </div>
          </template>
          
          <el-table :data="filteredMembers" v-loading="membersLoading">
            <el-table-column label="头像" width="80">
              <template #default="{ row }">
                <el-avatar :size="40" :src="row.userAvatar">{{ row.userName?.charAt(0) }}</el-avatar>
              </template>
            </el-table-column>
            <el-table-column prop="userName" label="姓名" />
            <el-table-column prop="userEmail" label="邮箱" />
            <el-table-column label="角色" width="120">
              <template #default="{ row }">
                <el-select 
                  v-model="row.role" 
                  size="small"
                  :disabled="row.role === 'LEADER'"
                  @change="updateMemberRole(row)"
                >
                  <el-option label="负责人" value="LEADER" disabled />
                  <el-option label="指导老师" value="TEACHER" />
                  <el-option label="普通成员" value="MEMBER" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="position" label="职位" width="150">
              <template #default="{ row }">
                <el-input 
                  v-model="row.position"
                  size="small"
                  placeholder="职位"
                  @blur="updateMemberPosition(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="加入时间" width="120">
              <template #default="{ row }">{{ formatDate(row.joinedAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button 
                  v-if="row.role !== 'LEADER'"
                  type="danger" 
                  text 
                  size="small"
                  @click="removeMember(row)"
                >
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <!-- 加入申请 -->
      <el-tab-pane label="加入申请" name="applications">
        <el-card>
          <el-table :data="pendingApplications" v-loading="applicationsLoading">
            <el-table-column label="头像" width="80">
              <template #default="{ row }">
                <el-avatar :size="40" :src="row.userAvatar">{{ row.userName?.charAt(0) }}</el-avatar>
              </template>
            </el-table-column>
            <el-table-column prop="userName" label="姓名" />
            <el-table-column prop="userEmail" label="邮箱" />
            <el-table-column label="申请时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="handleApplication(row, 'APPROVED')">通过</el-button>
                <el-button type="danger" size="small" @click="handleApplication(row, 'REJECTED')">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="pendingApplications.length === 0 && !applicationsLoading" description="暂无待审批的申请" />
        </el-card>
      </el-tab-pane>
      
      <!-- 公告管理 -->
      <el-tab-pane label="公告管理" name="announcements">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>公告列表</span>
              <el-button type="primary" size="small" @click="showAnnouncementDialog = true">
                发布公告
              </el-button>
            </div>
          </template>
          
          <el-table :data="announcements" v-loading="announcementsLoading">
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column label="置顶" width="80">
              <template #default="{ row }">
                <el-switch v-model="row.pinned" @change="togglePinned(row)" />
              </template>
            </el-table-column>
            <el-table-column label="发布时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" text size="small" @click="editAnnouncement(row)">编辑</el-button>
                <el-button type="danger" text size="small" @click="deleteAnnouncement(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 公告编辑对话框 -->
    <el-dialog 
      v-model="showAnnouncementDialog" 
      :title="editingAnnouncement ? '编辑公告' : '发布公告'"
      width="600px"
    >
      <el-form :model="announcementForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="announcementForm.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="announcementForm.pinned" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAnnouncementDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingAnnouncement" @click="saveAnnouncement">
          {{ editingAnnouncement ? '保存' : '发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  getClub, 
  updateClub, 
  getClubMembers, 
  updateMemberRole as updateMemberRoleApi,
  removeMember as removeMemberApi,
  getClubApplications,
  reviewApplication,
  getClubAnnouncements,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement as deleteAnnouncementApi
} from '@/api/club'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Plus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const clubId = computed(() => route.params.id)
const activeTab = ref('info')

const club = ref(null)
const saving = ref(false)

const clubTypeMap = {
  ACADEMIC: { name: '学术科技' },
  CULTURAL: { name: '文化艺术' },
  SPORTS: { name: '体育运动' },
  VOLUNTEER: { name: '志愿服务' },
  INNOVATION: { name: '创新创业' },
  INTEREST: { name: '兴趣爱好' },
  OTHER: { name: '其他' }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 基本信息
const infoFormRef = ref(null)
const infoForm = reactive({
  logo: '',
  name: '',
  type: '',
  purpose: '',
  description: '',
  contactEmail: '',
  location: ''
})

const infoRules = {
  name: [{ required: true, message: '请输入社团名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择社团类型', trigger: 'change' }]
}

const loadClub = async () => {
  try {
    const res = await getClub(clubId.value)
    club.value = res.data
    
    // 检查权限：只有社团负责人或管理员才能管理
    if (club.value.leaderId !== userStore.user.id && userStore.user.role !== 'ADMIN') {
      ElMessage.error('您没有权限管理此社团')
      router.push('/my-clubs')
      return
    }
    
    Object.assign(infoForm, {
      logo: res.data.logo || '',
      name: res.data.name,
      type: res.data.type,
      purpose: res.data.purpose || '',
      description: res.data.description || '',
      contactEmail: res.data.contactEmail || '',
      location: res.data.location || ''
    })
  } catch (e) {
    console.error('加载社团信息失败', e)
    ElMessage.error('加载社团信息失败')
  }
}

const saveInfo = async () => {
  const valid = await infoFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    await updateClub(clubId.value, infoForm)
    ElMessage.success('保存成功')
    loadClub()
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    saving.value = false
  }
}

// Logo上传
const beforeLogoUpload = (file) => {
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

const uploadLogo = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  
  try {
    // 使用文件上传API
    const res = await fetch('/api/files/upload?type=club-logos', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      },
      body: formData
    })
    const data = await res.json()
    if (data.success) {
      infoForm.logo = data.data
      ElMessage.success('Logo上传成功')
      // 自动保存
      saveInfo()
    } else {
      ElMessage.error('上传失败：' + (data.message || '未知错误'))
    }
  } catch (e) {
    console.error('上传Logo失败', e)
    ElMessage.error('上传Logo失败')
  }
}

// 成员管理
const members = ref([])
const membersLoading = ref(false)
const memberSearch = ref('')

const filteredMembers = computed(() => {
  if (!memberSearch.value) return members.value
  const keyword = memberSearch.value.toLowerCase()
  return members.value.filter(m => 
    m.userName?.toLowerCase().includes(keyword) ||
    m.userEmail?.toLowerCase().includes(keyword)
  )
})

const loadMembers = async () => {
  membersLoading.value = true
  try {
    const res = await getClubMembers(clubId.value)
    members.value = res.data?.content || res.data || []
  } catch (e) {
    console.error('加载成员失败', e)
  } finally {
    membersLoading.value = false
  }
}

const updateMemberRole = async (member) => {
  try {
    await updateMemberRoleApi(member.id, { role: member.role, position: member.position })
    ElMessage.success('角色更新成功')
  } catch (e) {
    console.error('更新角色失败', e)
    loadMembers()
  }
}

const updateMemberPosition = async (member) => {
  try {
    await updateMemberRoleApi(member.id, { role: member.role, position: member.position })
  } catch (e) {
    console.error('更新职位失败', e)
  }
}

const removeMember = async (member) => {
  try {
    await ElMessageBox.confirm(`确定要移除成员 ${member.userName} 吗？`, '提示', { type: 'warning' })
    await removeMemberApi(clubId.value, member.userId)
    ElMessage.success('已移除')
    loadMembers()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('移除成员失败', e)
    }
  }
}

// 加入申请
const pendingApplications = ref([])
const applicationsLoading = ref(false)

const loadApplications = async () => {
  applicationsLoading.value = true
  try {
    const res = await getClubApplications(clubId.value)
    pendingApplications.value = res.data || []
  } catch (e) {
    console.error('加载申请失败', e)
  } finally {
    applicationsLoading.value = false
  }
}

const handleApplication = async (row, status) => {
  try {
    const approved = status === 'APPROVED'
    await reviewApplication(row.id, approved, '')
    ElMessage.success(approved ? '已通过' : '已拒绝')
    loadApplications()
    loadMembers()
  } catch (e) {
    console.error('审批失败', e)
  }
}

// 公告管理
const announcements = ref([])
const announcementsLoading = ref(false)
const showAnnouncementDialog = ref(false)
const editingAnnouncement = ref(null)
const savingAnnouncement = ref(false)

const announcementForm = reactive({
  title: '',
  content: '',
  pinned: false
})

const loadAnnouncements = async () => {
  announcementsLoading.value = true
  try {
    const res = await getClubAnnouncements(clubId.value)
    announcements.value = res.data?.content || res.data || []
  } catch (e) {
    console.error('加载公告失败', e)
  } finally {
    announcementsLoading.value = false
  }
}

const editAnnouncement = (ann) => {
  editingAnnouncement.value = ann
  Object.assign(announcementForm, {
    title: ann.title,
    content: ann.content,
    pinned: ann.pinned
  })
  showAnnouncementDialog.value = true
}

const saveAnnouncement = async () => {
  if (!announcementForm.title || !announcementForm.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  
  savingAnnouncement.value = true
  try {
    if (editingAnnouncement.value) {
      await updateAnnouncement(editingAnnouncement.value.id, announcementForm)
    } else {
      await createAnnouncement({
        ...announcementForm,
        clubId: clubId.value
      })
    }
    ElMessage.success(editingAnnouncement.value ? '保存成功' : '发布成功')
    showAnnouncementDialog.value = false
    editingAnnouncement.value = null
    Object.assign(announcementForm, { title: '', content: '', pinned: false })
    loadAnnouncements()
  } catch (e) {
    console.error('保存公告失败', e)
  } finally {
    savingAnnouncement.value = false
  }
}

const togglePinned = async (ann) => {
  try {
    await updateAnnouncement(ann.id, { pinned: ann.pinned })
    ElMessage.success(ann.pinned ? '已置顶' : '已取消置顶')
  } catch (e) {
    console.error('更新失败', e)
    ann.pinned = !ann.pinned
  }
}

const deleteAnnouncement = async (ann) => {
  try {
    await ElMessageBox.confirm('确定要删除这条公告吗？', '提示', { type: 'warning' })
    await deleteAnnouncementApi(ann.id)
    ElMessage.success('已删除')
    loadAnnouncements()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除公告失败', e)
    }
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadClub()
  loadMembers()
  loadApplications()
  loadAnnouncements()
})
</script>

<style scoped>
.club-manage-page {
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

.logo-uploader {
  cursor: pointer;
}

.upload-avatar {
  display: block;
  border: 2px solid #dcdfe6;
  border-radius: 4px;
}

.upload-placeholder {
  width: 100px;
  height: 100px;
  border: 2px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-placeholder .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
}
</style>
