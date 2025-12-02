import request from '@/utils/request'

// 获取通知列表
export function getNotifications(params) {
  return request.get('/notifications', { params })
}

// 获取未读通知
export function getUnreadNotifications() {
  return request.get('/notifications/unread')
}

// 获取未读数量
export function getUnreadCount() {
  return request.get('/notifications/unread/count')
}

// 标记为已读
export function markNotificationAsRead(id) {
  return request.put(`/notifications/${id}/read`)
}

// 全部标记已读
export function markAllNotificationsAsRead() {
  return request.put('/notifications/read-all')
}

// 删除通知
export function deleteNotification(id) {
  return request.delete(`/notifications/${id}`)
}
