/**
 * 密码强度验证工具
 */

/**
 * 计算密码强度
 * @param {string} password - 密码
 * @returns {Object} 包含强度等级、百分比和文本描述的对象
 */
export function calculatePasswordStrength(password) {
  if (!password) {
    return { level: 'weak', percentage: 0, text: '弱', score: 0 }
  }

  let score = 0
  let checks = 0

  // 长度检查
  if (password.length >= 6) score += 10
  if (password.length >= 8) score += 15
  if (password.length >= 12) score += 20
  if (password.length >= 16) score += 5

  // 字符类型检查
  if (/[a-z]/.test(password)) { 
    score += 10
    checks++
  }
  if (/[A-Z]/.test(password)) { 
    score += 10
    checks++
  }
  if (/\d/.test(password)) { 
    score += 10
    checks++
  }
  if (/[^a-zA-Z0-9]/.test(password)) { 
    score += 15
    checks++
  }

  // 多样性奖励
  if (checks >= 3) score += 10
  if (checks >= 4) score += 15

  // 常见模式检查（减分）
  if (/(.)\1{2,}/.test(password)) score -= 10 // 连续重复字符
  if (/123|abc|qwe|asd|zxc/i.test(password)) score -= 15 // 常见序列
  if (/password|123456|qwerty|admin/i.test(password)) score -= 20 // 常见密码

  // 确保分数在合理范围内
  score = Math.max(0, Math.min(100, score))

  let level, text, color
  if (score < 30) {
    level = 'weak'
    text = '弱'
    color = '#f56c6c'
  } else if (score < 60) {
    level = 'medium'
    text = '中等'
    color = '#e6a23c'
  } else if (score < 80) {
    level = 'strong'
    text = '强'
    color = '#67c23a'
  } else {
    level = 'very-strong'
    text = '很强'
    color = '#409eff'
  }

  return {
    level,
    percentage: score,
    text,
    color,
    score
  }
}

/**
 * 验证密码是否符合基本要求
 * @param {string} password - 密码
 * @returns {Object} 验证结果
 */
export function validatePassword(password) {
  const errors = []
  
  if (!password) {
    errors.push('密码不能为空')
    return { valid: false, errors }
  }

  if (password.length < 6) {
    errors.push('密码长度至少6位')
  }

  if (password.length > 50) {
    errors.push('密码长度不能超过50位')
  }

  if (!/[a-zA-Z]/.test(password)) {
    errors.push('密码必须包含字母')
  }

  if (!/\d/.test(password)) {
    errors.push('密码必须包含数字')
  }

  // 检查是否包含空格
  if (/\s/.test(password)) {
    errors.push('密码不能包含空格')
  }

  return {
    valid: errors.length === 0,
    errors
  }
}

/**
 * 生成密码强度建议
 * @param {string} password - 密码
 * @returns {Array} 建议列表
 */
export function getPasswordSuggestions(password) {
  const suggestions = []
  
  if (!password) return ['请输入密码']

  if (password.length < 8) {
    suggestions.push('建议密码长度至少8位')
  }

  if (!/[a-z]/.test(password)) {
    suggestions.push('添加小写字母')
  }

  if (!/[A-Z]/.test(password)) {
    suggestions.push('添加大写字母')
  }

  if (!/\d/.test(password)) {
    suggestions.push('添加数字')
  }

  if (!/[^a-zA-Z0-9]/.test(password)) {
    suggestions.push('添加特殊字符（如 !@#$%^&*）')
  }

  if (/(.)\1{2,}/.test(password)) {
    suggestions.push('避免连续重复字符')
  }

  if (/123|abc|qwe|asd|zxc/i.test(password)) {
    suggestions.push('避免使用常见字符序列')
  }

  if (suggestions.length === 0) {
    suggestions.push('密码强度良好')
  }

  return suggestions
}

/**
 * 检查密码是否为常见弱密码
 * @param {string} password - 密码
 * @returns {boolean} 是否为弱密码
 */
export function isWeakPassword(password) {
  const weakPasswords = [
    '123456', '123456789', 'qwerty', 'password', '12345678',
    '111111', '123123', 'admin', 'root', 'user', 'test',
    'qwertyuiop', 'asdfghjkl', 'zxcvbnm', '1qaz2wsx',
    'qwe123', 'abc123', '123qwe', 'a123456', '123abc'
  ]

  return weakPasswords.includes(password.toLowerCase())
}