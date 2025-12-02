// 常量定义

// 社团类型
export const CLUB_TYPES = {
  ACADEMIC: { label: '学术科技', value: 'ACADEMIC', tag: '' },
  CULTURAL: { label: '文化艺术', value: 'CULTURAL', tag: 'success' },
  SPORTS: { label: '体育运动', value: 'SPORTS', tag: 'warning' },
  VOLUNTEER: { label: '志愿服务', value: 'VOLUNTEER', tag: 'danger' },
  INNOVATION: { label: '创新创业', value: 'INNOVATION', tag: 'info' },
  INTEREST: { label: '兴趣爱好', value: 'INTEREST', tag: '' },
  OTHER: { label: '其他', value: 'OTHER', tag: 'info' }
}

// 社团类型选项列表
export const CLUB_TYPE_OPTIONS = Object.values(CLUB_TYPES)

// 成员角色
export const MEMBER_ROLES = {
  LEADER: { label: '负责人', value: 'LEADER', tag: 'danger' },
  CORE: { label: '核心成员', value: 'CORE', tag: 'warning' },
  MEMBER: { label: '普通成员', value: 'MEMBER', tag: 'info' }
}

// 成员角色选项列表
export const MEMBER_ROLE_OPTIONS = Object.values(MEMBER_ROLES)

// 成员状态
export const MEMBER_STATUS = {
  PENDING: { label: '待审核', value: 'PENDING', tag: 'warning' },
  APPROVED: { label: '已通过', value: 'APPROVED', tag: 'success' },
  REJECTED: { label: '已拒绝', value: 'REJECTED', tag: 'danger' }
}

// 活动状态
export const ACTIVITY_STATUS = {
  DRAFT: { label: '草稿', value: 'DRAFT', tag: 'info' },
  PUBLISHED: { label: '已发布', value: 'PUBLISHED', tag: 'success' },
  ONGOING: { label: '进行中', value: 'ONGOING', tag: 'warning' },
  COMPLETED: { label: '已结束', value: 'COMPLETED', tag: '' },
  CANCELLED: { label: '已取消', value: 'CANCELLED', tag: 'danger' }
}

// 通知类型
export const NOTIFICATION_TYPES = {
  ACTIVITY: { label: '活动通知', value: 'ACTIVITY', color: '#67c23a' },
  CLUB: { label: '社团通知', value: 'CLUB', color: '#409eff' },
  SYSTEM: { label: '系统通知', value: 'SYSTEM', color: '#909399' },
  FEEDBACK: { label: '反馈通知', value: 'FEEDBACK', color: '#e6a23c' },
  ALERT: { label: '提醒', value: 'ALERT', color: '#f56c6c' }
}

// 用户角色
export const USER_ROLES = {
  USER: { label: '普通用户', value: 'USER' },
  LEADER: { label: '社团负责人', value: 'LEADER' },
  ADMIN: { label: '管理员', value: 'ADMIN' }
}

// 分页默认配置
export const DEFAULT_PAGINATION = {
  page: 1,
  size: 10,
  sizes: [10, 20, 50, 100]
}

// 文件上传配置
export const UPLOAD_CONFIG = {
  maxSize: 5 * 1024 * 1024, // 5MB
  imageTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
  acceptImage: '.jpg,.jpeg,.png,.gif,.webp'
}
