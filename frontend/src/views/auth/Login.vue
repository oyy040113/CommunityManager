<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h1>学生社团信息整理系统</h1>
        <p>登录您的账号</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            native-type="submit"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
      
      <el-divider>测试账号</el-divider>
      <div class="test-accounts">
        <el-row :gutter="10">
          <el-col :span="8">
            <el-tag @click="fillTestAccount('admin', 'admin123')" class="test-tag account-tag">
              <div class="account-name">系统管理员</div>
              <div class="account-info">admin / admin123</div>
            </el-tag>
          </el-col>
          <el-col :span="8">
            <el-tag @click="fillTestAccount('leader1', 'leader123')" type="success" class="test-tag account-tag">
              <div class="account-name">社长李明</div>
              <div class="account-info">leader1 / leader123</div>
            </el-tag>
          </el-col>
          <el-col :span="8">
            <el-tag @click="fillTestAccount('leader2', 'leader123')" type="success" class="test-tag account-tag">
              <div class="account-name">社长王华</div>
              <div class="account-info">leader2 / leader123</div>
            </el-tag>
          </el-col>
        </el-row>
        <el-row :gutter="10" style="margin-top: 8px">
          <el-col :span="8">
            <el-tag @click="fillTestAccount('leader3', 'leader123')" type="success" class="test-tag account-tag">
              <div class="account-name">社长张伟</div>
              <div class="account-info">leader3 / leader123</div>
            </el-tag>
          </el-col>
          <el-col :span="8">
            <el-tag @click="fillTestAccount('teacher1', 'teacher123')" type="warning" class="test-tag account-tag">
              <div class="account-name">陈老师</div>
              <div class="account-info">teacher1 / teacher123</div>
            </el-tag>
          </el-col>
          <el-col :span="8">
            <el-tag @click="fillTestAccount('teacher2', 'teacher123')" type="warning" class="test-tag account-tag">
              <div class="account-name">刘老师</div>
              <div class="account-info">teacher2 / teacher123</div>
            </el-tag>
          </el-col>
        </el-row>
        <el-row :gutter="10" style="margin-top: 8px">
          <el-col :span="8">
            <el-tag @click="fillTestAccount('teacher3', 'teacher123')" type="warning" class="test-tag account-tag">
              <div class="account-name">赵老师</div>
              <div class="account-info">teacher3 / teacher123</div>
            </el-tag>
          </el-col>
          <el-col :span="8">
            <el-tag @click="fillTestAccount('student1', 'student123')" type="info" class="test-tag account-tag">
              <div class="account-name">学生小红</div>
              <div class="account-info">student1 / student123</div>
            </el-tag>
          </el-col>
          <el-col :span="8">
            <el-tag @click="fillTestAccount('student2', 'student123')" type="info" class="test-tag account-tag">
              <div class="account-name">学生小刚</div>
              <div class="account-info">student2 / student123</div>
            </el-tag>
          </el-col>
        </el-row>
        <el-row :gutter="10" style="margin-top: 8px">
          <el-col :span="8">
            <el-tag @click="fillTestAccount('student3', 'student123')" type="info" class="test-tag account-tag">
              <div class="account-name">学生小美</div>
              <div class="account-info">student3 / student123</div>
            </el-tag>
          </el-col>
          <el-col :span="8" />
          <el-col :span="8" />
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    console.error('登录失败', e)
  } finally {
    loading.value = false
  }
}

const fillTestAccount = (username, password) => {
  form.username = username
  form.password = password
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.login-header p {
  color: #909399;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #606266;
}

.login-footer a {
  color: #409eff;
  margin-left: 4px;
}

.test-accounts {
  display: block;
}

.test-tag {
  cursor: pointer;
  justify-content: center;
  width: 100%;
  display: flex !important;
  flex-direction: column;
  padding: 8px 4px;
  min-height: 50px;
  line-height: 1.2;
}

.account-tag {
  background: linear-gradient(135deg, #f5f7fa 0%, #f5f7fa 100%);
}

.account-tag:hover {
  opacity: 0.8;
  transform: translateY(-2px);
}

.account-name {
  font-weight: 600;
  font-size: 12px;
}

.account-info {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}
</style>
