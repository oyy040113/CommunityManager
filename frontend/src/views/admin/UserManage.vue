<template>
  <div class="user-manage-page">
    <div class="page-header">
      <h1>用户管理</h1>
      <el-button type="primary" @click="openUserDialog()">
        <el-icon><Plus /></el-icon> 创建用户
      </el-button>
    </div>
    
    <!-- 搜索过滤 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input 
            v-model="searchKeyword" 
            placeholder="用户名/姓名/邮箱" 
            clearable 
            style="width: 200px"
            @keyup.enter="loadUsers"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchRole" placeholder="全部" clearable style="width: 150px">
            <el-option label="超级管理员" value="ADMIN" />
            <el-option label="社团负责人" value="CLUB_LEADER" />
            <el-option label="指导老师" value="TEACHER" />
            <el-option label="普通学生" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchEnabled" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUsers">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 用户列表 -->
    <el-card>
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar">
              {{ row.realName?.charAt(0) || row.username?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="studentId" label="学号/工号" width="120" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openUserDialog(row)">
              编辑
            </el-button>
            <el-button type="warning" text size="small" @click="changeRole(row)">
              改角色
            </el-button>
            <el-button 
              :type="row.enabled ? 'danger' : 'success'" 
              text 
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
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
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>
    
    <!-- 用户编辑对话框 -->
    <el-dialog 
      v-model="showUserDialog" 
      :title="editingUser ? '编辑用户' : '创建用户'"
      width="600px"
    >
      <el-form 
        ref="userFormRef"
        :model="userForm" 
        :rules="userRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="userForm.username" 
            placeholder="请输入用户名"
            :disabled="!!editingUser"
          />
        </el-form-item>
        <el-form-item v-if="!editingUser" label="密码" prop="password">
          <el-input 
            v-model="userForm.password" 
            type="password" 
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="学号/工号" prop="studentId">
          <el-input v-model="userForm.studentId" placeholder="请输入学号或工号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="超级管理员" value="ADMIN" />
            <el-option label="社团负责人" value="CLUB_LEADER" />
            <el-option label="指导老师" value="TEACHER" />
            <el-option label="普通学生" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="院系">
          <el-input v-model="userForm.department" placeholder="请输入院系" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="userForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="userForm.grade" placeholder="请输入年级，如2026" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUserDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingUser" @click="saveUser">
          {{ editingUser ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 修改角色对话框 -->
    <el-dialog v-model="showRoleDialog" title="修改用户角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前用户">
          <div>{{ roleChangeUser?.realName || roleChangeUser?.username }}</div>
        </el-form-item>
        <el-form-item label="当前角色">
          <el-tag :type="getRoleTagType(roleChangeUser?.role)">
            {{ getRoleLabel(roleChangeUser?.role) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新角色">
          <el-select v-model="newRole" placeholder="请选择新角色" style="width: 100%">
            <el-option label="超级管理员" value="ADMIN" />
            <el-option label="社团负责人" value="CLUB_LEADER" />
            <el-option label="指导老师" value="TEACHER" />
            <el-option label="普通学生" value="USER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRoleDialog = false">取消</el-button>
        <el-button type="primary" :loading="changingRole" @click="confirmChangeRole">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  searchUsersAdmin, 
  createUser, 
  updateUserAdmin, 
  updateUserRole, 
  setUserStatus 
} from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

// 搜索相关
const searchKeyword = ref('')
const searchRole = ref(null)
const searchEnabled = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 用户列表
const users = ref([])
const loading = ref(false)

// 角色映射
const roleMap = {
  ADMIN: { label: '超级管理员', type: 'danger' },
  CLUB_LEADER: { label: '社团负责人', type: 'warning' },
  TEACHER: { label: '指导老师', type: 'success' },
  USER: { label: '普通学生', type: 'info' }
}

const getRoleLabel = (role) => roleMap[role]?.label || role
const getRoleTagType = (role) => roleMap[role]?.type || 'info'

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      role: searchRole.value || undefined,
      enabled: searchEnabled.value !== null ? searchEnabled.value : undefined
    }
    
    const res = await searchUsersAdmin(params)
    users.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (e) {
    console.error('加载用户列表失败', e)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  searchRole.value = null
  searchEnabled.value = null
  currentPage.value = 1
  loadUsers()
}

// 用户编辑
const showUserDialog = ref(false)
const userFormRef = ref(null)
const editingUser = ref(null)
const savingUser = ref(false)

const userForm = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  studentId: '',
  role: 'USER',
  department: '',
  major: '',
  grade: ''
})

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  studentId: [
    { required: true, message: '请输入学号或工号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const openUserDialog = (user = null) => {
  editingUser.value = user
  if (user) {
    Object.assign(userForm, {
      username: user.username,
      password: '',
      realName: user.realName || '',
      email: user.email || '',
      phone: user.phone || '',
      studentId: user.studentId || '',
      role: user.role,
      department: user.department || '',
      major: user.major || '',
      grade: user.grade || ''
    })
  } else {
    Object.assign(userForm, {
      username: '',
      password: '',
      realName: '',
      email: '',
      phone: '',
      studentId: '',
      role: 'USER',
      department: '',
      major: '',
      grade: ''
    })
  }
  showUserDialog.value = true
}

const saveUser = async () => {
  const valid = await userFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  savingUser.value = true
  try {
    if (editingUser.value) {
      // 编辑用户
      await updateUserAdmin(editingUser.value.id, userForm)
      ElMessage.success('用户更新成功')
    } else {
      // 创建用户
      await createUser(userForm, userForm.password)
      ElMessage.success('用户创建成功')
    }
    showUserDialog.value = false
    loadUsers()
  } catch (e) {
    console.error('保存用户失败', e)
  } finally {
    savingUser.value = false
  }
}

// 修改角色
const showRoleDialog = ref(false)
const roleChangeUser = ref(null)
const newRole = ref('')
const changingRole = ref(false)

const changeRole = (user) => {
  roleChangeUser.value = user
  newRole.value = user.role
  showRoleDialog.value = true
}

const confirmChangeRole = async () => {
  if (!newRole.value) {
    ElMessage.warning('请选择新角色')
    return
  }
  
  if (newRole.value === roleChangeUser.value.role) {
    ElMessage.warning('新角色与当前角色相同')
    return
  }
  
  changingRole.value = true
  try {
    await updateUserRole(roleChangeUser.value.id, newRole.value)
    ElMessage.success('角色修改成功')
    showRoleDialog.value = false
    loadUsers()
  } catch (e) {
    console.error('修改角色失败', e)
  } finally {
    changingRole.value = false
  }
}

// 启用/禁用用户
const toggleStatus = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要${user.enabled ? '禁用' : '启用'}用户 ${user.realName || user.username} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await setUserStatus(user.id, !user.enabled)
    ElMessage.success(`用户已${user.enabled ? '禁用' : '启用'}`)
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('修改用户状态失败', e)
    }
  }
}

onMounted(() => {
  if (!userStore.isAdmin) {
    ElMessage.error('只有管理员可以访问此页面')
    router.push('/')
    return
  }
  loadUsers()
})
</script>

<style scoped>
.user-manage-page {
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
