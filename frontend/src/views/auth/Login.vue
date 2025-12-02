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
        <el-tag @click="fillTestAccount('admin', 'admin123')" class="test-tag">
          管理员: admin / admin123
        </el-tag>
        <el-tag @click="fillTestAccount('leader', 'leader123')" type="success" class="test-tag">
          社团负责人: leader / leader123
        </el-tag>
        <el-tag @click="fillTestAccount('test', 'test123')" type="info" class="test-tag">
          普通用户: test / test123
        </el-tag>
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
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.test-tag {
  cursor: pointer;
  justify-content: center;
}

.test-tag:hover {
  opacity: 0.8;
}
</style>
