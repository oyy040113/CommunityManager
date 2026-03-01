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

// 转让社团
export function transferClub(id, data) {
  return request.post(`/clubs/${id}/transfer`, data)
}

// 设置社团管理员
export function setClubAdmin(id, data) {
  return request.post(`/clubs/${id}/set-admin`, data)
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

// 获取我创建/管理的社团
export function getMyCreatedClubs() {
  return request.get('/clubs/my-created')
}

// 获取我管理的社团
export function getMyManagedClubs() {
  return request.get('/clubs/my-managed')
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
export function reviewApplication(memberId, approved, rejectReason = '') {
  return request.put(`/clubs/members/${memberId}/review`, { 
    approved, 
    rejectReason 
  })
}

// 更新成员角色/职位
export function updateMemberRole(memberId, data) {
  return request.put(`/clubs/members/${memberId}/role`, data)
}

// 移除成员
export function removeMember(clubId, userId) {
  return request.delete(`/clubs/${clubId}/members/${userId}`)
}

// 邀请指导老师
export function inviteTeacher(clubId, data) {
  return request.post(`/clubs/${clubId}/invite-teacher`, data)
}

// 接受指导老师邀请
export function acceptTeacherInvitation(memberId) {
  return request.put(`/clubs/teacher-invitations/${memberId}/accept`)
}

// 拒绝指导老师邀请
export function rejectTeacherInvitation(memberId) {
  return request.put(`/clubs/teacher-invitations/${memberId}/reject`)
}

// 获取指导老师邀请列表
export function getTeacherInvitations() {
  return request.get('/clubs/teacher-invitations')
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

// ==================== 管理员接口 ====================

// 获取待审批社团
export function getPendingClubs(params) {
  return request.get('/clubs/admin/pending', { params })
}

// 管理员搜索社团
export function searchClubsAdmin(params) {
  return request.get('/clubs/admin/search', { params })
}

// 管理员创建社团
export function createClubByAdmin(data, leaderId) {
  return request.post('/clubs/admin/create', data, {
    params: { leaderId }
  })
}

// 审批社团
export function approveClub(id, data) {
  return request.put(`/clubs/admin/${id}/approve`, data)
}

// 管理员删除社团
export function deleteClubByAdmin(id) {
  return request.delete(`/clubs/admin/${id}`)
}
