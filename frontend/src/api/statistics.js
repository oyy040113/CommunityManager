import request from '@/utils/request'

// 获取总体统计数据
export function getOverviewStats() {
  return request.get('/statistics/overview')
}

// 获取首页公共统计数据
export function getPublicOverviewStats() {
  return request.get('/statistics/public-overview')
}

// 获取社团统计数据
export function getClubStats(clubId) {
  return request.get(`/statistics/clubs/${clubId}`)
}

// 获取成员增长趋势
export function getMemberGrowthTrend(clubId, params) {
  return request.get(`/statistics/clubs/${clubId}/member-growth`, { params })
}

// 获取活动参与统计
export function getActivityParticipationStats(clubId, params) {
  return request.get(`/statistics/clubs/${clubId}/activity-participation`, { params })
}

// 获取评分分布
export function getRatingDistribution(clubId) {
  return request.get(`/statistics/clubs/${clubId}/rating-distribution`)
}

// 获取社团排行榜
export function getClubRankings(params) {
  return request.get('/statistics/club-rankings', { params })
}

// 获取热门活动
export function getPopularActivities(params) {
  return request.get('/statistics/popular-activities', { params })
}

// 获取用户活动统计
export function getUserActivityStats() {
  return request.get('/statistics/user/activities')
}

// 获取用户社团统计
export function getUserClubStats() {
  return request.get('/statistics/user/clubs')
}
