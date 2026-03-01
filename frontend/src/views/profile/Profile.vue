<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="uploadAvatar"
            >
              <el-avatar :size="100" :src="userStore.user?.avatar">
                {{ (userStore.user?.realName || userStore.user?.name)?.charAt(0) }}
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon><Camera /></el-icon>
              </div>
            </el-upload>
            <h2>{{ userStore.user?.realName || userStore.user?.name }}</h2>
            <p class="user-email">{{ userStore.user?.email }}</p>
            <el-tag :type="getRoleTagType(userStore.user?.role)">
              {{ getRoleLabel(userStore.user?.role) }}
            </el-tag>
          </div>
          
          <el-divider />
          
          <div class="stats-section">
            <div class="stat-item">
              <span class="stat-value">{{ stats.clubCount }}</span>
              <span class="stat-label">加入社团</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.activityCount }}</span>
              <span class="stat-label">参与活动</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.feedbackCount }}</span>
              <span class="stat-label">发表评价</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧设置表单 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>个人资料</span>
          </template>
          
          <el-form 
            ref="profileFormRef"
            :model="profileForm" 
            :rules="profileRules"
            label-width="100px"
            style="max-width: 500px"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="profileForm.name" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="学号" prop="studentId">
              <el-input v-model="profileForm.studentId" placeholder="请输入学号" />
            </el-form-item>
            <el-form-item label="院系" prop="department">
              <el-input v-model="profileForm.department" placeholder="请输入院系" />
            </el-form-item>
            <el-form-item label="专业" prop="major">
              <el-input v-model="profileForm.major" placeholder="请输入专业" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input 
                v-model="profileForm.bio" 
                type="textarea" 
                :rows="3" 
                placeholder="介绍一下自己..."
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <el-card class="password-card">
          <template #header>
            <span>修改密码</span>
          </template>
          
          <el-form 
            ref="passwordFormRef"
            :model="passwordForm" 
            :rules="passwordRules"
            label-width="100px"
            style="max-width: 500px"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :loading="changingPassword" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserProfile, updateProfile, changePassword as changePasswordApi, uploadAvatar as uploadAvatarApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const saving = ref(false)
const changingPassword = ref(false)

const roleMap = {
  ADMIN: { label: '超级管理员', type: 'danger' },
  CLUB_LEADER: { label: '社团负责人', type: 'warning' },
  TEACHER: { label: '指导老师', type: 'success' },
  USER: { label: '普通学生', type: 'info' }
}
const getRoleLabel = (role) => roleMap[role]?.label || role || '普通学生'
const getRoleTagType = (role) => roleMap[role]?.type || 'info'

const stats = reactive({
  clubCount: 0,
  activityCount: 0,
  feedbackCount: 0
})

const profileForm = reactive({
  username: '',
  name: '',
  email: '',
  phone: '',
  studentId: '',
  department: '',
  major: '',
  bio: ''
})

const profileRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadProfile = async () => {
  try {
    const res = await getUserProfile()
    const data = res.data
    Object.assign(profileForm, {
      username: data.username,
      name: data.realName || data.name,
      email: data.email,
      phone: data.phone || '',
      studentId: data.studentId || '',
      department: data.department || '',
      major: data.major || '',
      bio: data.bio || ''
    })
    stats.clubCount = data.clubCount || 0
    stats.activityCount = data.activityCount || 0
    stats.feedbackCount = data.feedbackCount || 0
  } catch (e) {
    console.error('加载个人资料失败', e)
  }
}

const saveProfile = async () => {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    await updateProfile({
      realName: profileForm.name,
      email: profileForm.email,
      phone: profileForm.phone,
      studentId: profileForm.studentId,
      department: profileForm.department,
      major: profileForm.major,
      bio: profileForm.bio
    })
    ElMessage.success('资料保存成功')
    userStore.fetchUser()
  } catch (e) {
    console.error('保存资料失败', e)
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  changingPassword.value = true
  try {
    await changePasswordApi({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch (e) {
    console.error('修改密码失败', e)
  } finally {
    changingPassword.value = false
  }
}

// 头像上传
const beforeAvatarUpload = (file) => {
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

const uploadAvatar = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  
  try {
    const res = await uploadAvatarApi(formData)
    ElMessage.success('头像上传成功')
    userStore.fetchUser()
  } catch (e) {
    console.error('上传头像失败', e)
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  padding-bottom: 40px;
}

.profile-card {
  text-align: center;
}

.avatar-section {
  padding: 20px;
}

.avatar-uploader {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: #fff;
  font-size: 24px;
}

.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.avatar-section h2 {
  margin: 16px 0 8px;
  font-size: 20px;
  font-weight: 600;
}

.user-email {
  color: #909399;
  font-size: 14px;
  margin-bottom: 12px;
}

.stats-section {
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.password-card {
  margin-top: 20px;
}
</style>
