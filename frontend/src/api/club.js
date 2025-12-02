import request from '@/utils/request'

// 搜索社团
export function searchClubs(params) {
  return request.get('/clubs', { params })
}

// 获取社团详情
export function getClub(id) {
  return request.get(`/clubs/${id}`)
}

// 获取热门社团
export function getTopClubs(limit = 10) {
  return request.get('/clubs/top', { params: { limit } })
}

// 创建社团
export function createClub(data) {
  return request.post('/clubs', data)
}

// 更新社团
export function updateClub(id, data) {
  return request.put(`/clubs/${id}`, data)
}

// 申请加入社团
export function joinClub(clubId, applicationReason) {
  return request.post(`/clubs/${clubId}/join`, { applicationReason })
}

// 退出社团
export function leaveClub(clubId) {
  return request.delete(`/clubs/${clubId}/leave`)
}

// 获取我的社团
export function getMyClubs() {
  return request.get('/clubs/my-clubs')
}

// 获取我创建的社团
export function getMyCreatedClubs() {
  return request.get('/clubs/my-created')
}

// 获取我的申请
export function getMyApplications() {
  return request.get('/clubs/my-applications')
}

// 获取社团成员
export function getClubMembers(clubId, params) {
  return request.get(`/clubs/${clubId}/members`, { params })
}

// 获取社团活动
export function getClubActivities(clubId, params) {
  return request.get(`/clubs/${clubId}/activities`, { params })
}

// 获取社团公告
export function getClubAnnouncements(clubId, params) {
  return request.get(`/clubs/${clubId}/announcements`, { params })
}

// 获取待审核申请
export function getClubApplications(clubId, params) {
  return request.get(`/clubs/${clubId}/applications`, { params })
}

// 获取待审批（负责人）
export function getPendingApprovals() {
  return request.get('/clubs/pending-approvals')
}

// 审核申请
export function approveJoinRequest(memberId, status) {
  return request.put(`/clubs/members/${memberId}/approve`, { status })
}

// 更新成员信息
export function updateMember(clubId, memberId, data) {
  return request.put(`/clubs/${clubId}/members/${memberId}`, data)
}

// 移除成员
export function removeMember(clubId, memberId) {
  return request.delete(`/clubs/${clubId}/members/${memberId}`)
}

// 创建公告
export function createAnnouncement(data) {
  return request.post('/announcements', data)
}

// 更新公告
export function updateAnnouncement(id, data) {
  return request.put(`/announcements/${id}`, data)
}

// 删除公告
export function deleteAnnouncement(id) {
  return request.delete(`/announcements/${id}`)
}
