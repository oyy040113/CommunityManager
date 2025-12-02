/**
 * 格式化日期时间
 * @param {string|Date} date 日期
 * @param {string} format 格式类型: 'date' | 'datetime' | 'time' | 'relative'
 * @returns {string}
 */
export function formatDateTime(date, format = 'datetime') {
  if (!date) return '-'
  
  const d = new Date(date)
  
  if (format === 'relative') {
    return formatRelativeTime(d)
  }
  
  const options = {}
  
  if (format === 'date' || format === 'datetime') {
    options.year = 'numeric'
    options.month = 'numeric'
    options.day = 'numeric'
  }
  
  if (format === 'time' || format === 'datetime') {
    options.hour = '2-digit'
    options.minute = '2-digit'
  }
  
  return d.toLocaleString('zh-CN', options)
}

/**
 * 格式化相对时间
 * @param {Date} date 
 * @returns {string}
 */
export function formatRelativeTime(date) {
  const now = new Date()
  const diff = now - date
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  const month = 30 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`
  } else {
    return formatDateTime(date, 'date')
  }
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @returns {string}
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + units[i]
}

/**
 * 格式化数字（添加千分位分隔符）
 * @param {number} num 
 * @returns {string}
 */
export function formatNumber(num) {
  return num?.toLocaleString('zh-CN') || '0'
}

/**
 * 截断文本
 * @param {string} text 
 * @param {number} maxLength 
 * @returns {string}
 */
export function truncateText(text, maxLength = 100) {
  if (!text || text.length <= maxLength) return text
  return text.slice(0, maxLength) + '...'
}
