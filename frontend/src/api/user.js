import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 用户注册
export function register(data) {
  return request.post('/auth/register', data)
}

// 获取当前用户信息
export function getCurrentUser() {
  return request.get('/auth/me')
}

// 刷新Token
export function refreshToken(refreshToken) {
  return request.post('/auth/refresh', null, {
    params: { refreshToken }
  })
}

// 获取用户资料
export function getUserProfile() {
  return request.get('/users/profile')
}

// 更新个人信息
export function updateProfile(data) {
  return request.put('/users/profile', data)
}

// 修改密码
export function changePassword(data) {
  return request.put('/users/password', data)
}

// 上传头像
export function uploadAvatar(formData) {
  return request.post('/users/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取用户信息
export function getUserById(id) {
  return request.get(`/users/${id}`)
}

// ==================== 管理员接口 ====================

// 获取所有用户
export function getAllUsers(params) {
  return request.get('/users/admin/list', { params })
}

// 搜索用户
export function searchUsersAdmin(params) {
  return request.get('/users/admin/search', { params })
}

// 创建用户
export function createUser(data, password) {
  return request.post('/users/admin/create', data, {
    params: { password }
  })
}

// 更新用户信息（管理员）
export function updateUserAdmin(id, data) {
  return request.put(`/users/admin/${id}`, data)
}

// 更新用户角色
export function updateUserRole(id, role) {
  return request.put(`/users/admin/${id}/role`, null, {
    params: { role }
  })
}

// 更新用户状态（启用/禁用）
export function setUserStatus(id, enabled) {
  return request.put(`/users/admin/${id}/status`, null, {
    params: { enabled }
  })
}
