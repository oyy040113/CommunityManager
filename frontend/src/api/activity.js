import request from '@/utils/request'

// 搜索活动
export function searchActivities(params) {
  return request.get('/activities', { params })
}

// 获取活动详情
export function getActivity(id) {
  return request.get(`/activities/${id}`)
}

// 获取即将开始的活动
export function getUpcomingActivities(params) {
  return request.get('/activities/upcoming', { params })
}

// 获取社团活动
export function getClubActivities(clubId, params) {
  return request.get(`/clubs/${clubId}/activities`, { params })
}

// 创建活动
export function createActivity(data) {
  return request.post('/activities', data)
}

// 更新活动
export function updateActivity(id, data) {
  return request.put(`/activities/${id}`, data)
}

// 删除活动
export function deleteActivity(id) {
  return request.delete(`/activities/${id}`)
}

// 发布活动
export function publishActivity(id) {
  return request.put(`/activities/${id}/publish`)
}

// 报名活动
export function registerActivity(id, formData = null) {
  return request.post(`/activities/${id}/register`, formData)
}

// 取消报名
export function cancelRegistration(id) {
  return request.delete(`/activities/${id}/cancel`)
}

// 签到
export function checkIn(id, qrCode) {
  return request.post(`/activities/${id}/check-in`, { qrCode })
}

// 社团负责人手动签到
export function manualCheckIn(activityId, userId) {
  return request.post(`/activities/${activityId}/manual-check-in/${userId}`)
}

// 获取活动参与者
export function getActivityParticipants(id, params) {
  return request.get(`/activities/${id}/participants`, { params })
}

// 获取我的活动报名
export function getMyRegistrations(params) {
  return request.get('/activities/my-registrations', { params })
}

// 获取我的活动历史
export function getMyActivityHistory(params) {
  return request.get('/activities/my-history', { params })
}

// 提交活动评价
export function submitActivityFeedback(activityId, data) {
  return request.post(`/activities/${activityId}/feedback`, data)
}

// 获取活动评价
export function getActivityFeedbacks(id, params) {
  return request.get(`/activities/${id}/feedbacks`, { params })
}

// 获取我的评价
export function getMyFeedbacks(params) {
  return request.get('/activities/my-feedbacks', { params })
}

// 提交活动总结
export function submitSummary(id, data) {
  return request.post(`/activities/${id}/summary`, data)
}

// 获取活动统计
export function getActivityStats(id) {
  return request.get(`/activities/${id}/stats`)
}

// ==================== Admin/Teacher ====================

// 获取待审批活动
export function getPendingActivities(params) {
  return request.get('/activities/admin/pending', { params })
}

// 管理员/老师搜索活动
export function searchActivitiesAdmin(params) {
  return request.get('/activities/admin/search', { params })
}

// 审批活动
export function approveActivity(id, data) {
  return request.put(`/activities/admin/${id}/approve`, data)
}
