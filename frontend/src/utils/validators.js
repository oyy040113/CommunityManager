// 表单验证规则

/**
 * 必填验证
 * @param {string} message 
 * @param {string} trigger 
 * @returns {object}
 */
export const required = (message = '此字段不能为空', trigger = 'blur') => ({
  required: true,
  message,
  trigger
})

/**
 * 邮箱验证
 * @param {string} message 
 * @returns {object}
 */
export const email = (message = '请输入正确的邮箱格式') => ({
  type: 'email',
  message,
  trigger: 'blur'
})

/**
 * 手机号验证
 * @param {string} message 
 * @returns {object}
 */
export const phone = (message = '请输入正确的手机号') => ({
  pattern: /^1[3-9]\d{9}$/,
  message,
  trigger: 'blur'
})

/**
 * 长度范围验证
 * @param {number} min 
 * @param {number} max 
 * @param {string} message 
 * @returns {object}
 */
export const length = (min, max, message = `长度必须在${min}到${max}个字符之间`) => ({
  min,
  max,
  message,
  trigger: 'blur'
})

/**
 * 最小长度验证
 * @param {number} min 
 * @param {string} message 
 * @returns {object}
 */
export const minLength = (min, message = `长度不能少于${min}个字符`) => ({
  min,
  message,
  trigger: 'blur'
})

/**
 * 最大长度验证
 * @param {number} max 
 * @param {string} message 
 * @returns {object}
 */
export const maxLength = (max, message = `长度不能超过${max}个字符`) => ({
  max,
  message,
  trigger: 'blur'
})

/**
 * 数字范围验证
 * @param {number} min 
 * @param {number} max 
 * @param {string} message 
 * @returns {object}
 */
export const range = (min, max, message = `数值必须在${min}到${max}之间`) => ({
  type: 'number',
  min,
  max,
  message,
  trigger: 'blur'
})

/**
 * URL验证
 * @param {string} message 
 * @returns {object}
 */
export const url = (message = '请输入正确的URL格式') => ({
  type: 'url',
  message,
  trigger: 'blur'
})

/**
 * 自定义正则验证
 * @param {RegExp} pattern 
 * @param {string} message 
 * @returns {object}
 */
export const pattern = (pattern, message = '格式不正确') => ({
  pattern,
  message,
  trigger: 'blur'
})

/**
 * 自定义验证器
 * @param {Function} validator 
 * @param {string} trigger 
 * @returns {object}
 */
export const custom = (validator, trigger = 'blur') => ({
  validator,
  trigger
})

// 预定义规则集合
export const rules = {
  username: [
    required('请输入用户名'),
    length(3, 20, '用户名长度必须在3到20个字符之间'),
    pattern(/^[a-zA-Z0-9_]+$/, '用户名只能包含字母、数字和下划线')
  ],
  password: [
    required('请输入密码'),
    minLength(6, '密码长度不能少于6个字符')
  ],
  email: [
    required('请输入邮箱'),
    email()
  ],
  phone: [
    phone()
  ],
  name: [
    required('请输入姓名'),
    length(2, 20, '姓名长度必须在2到20个字符之间')
  ]
}
