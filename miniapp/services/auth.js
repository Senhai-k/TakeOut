const { request } = require('./request')
const storage = require('../utils/storage')

const USER_KEY = 'takeout_user'

const fallbackUser = {
  userId: 1,
  nickname: '张三',
  phone: '13800000000',
  avatarText: '张',
  memberLevel: '黄金会员',
  token: 'local-user-1-token'
}

function getStoredUser() {
  return storage.get(USER_KEY, null)
}

function saveUser(user) {
  storage.set(USER_KEY, user)
}

function clearUser() {
  storage.remove(USER_KEY)
}

async function logoutUser() {
  const user = getStoredUser()
  if (user && user.refreshToken) {
    try {
      await request({ url: '/app/auth/logout', method: 'POST', data: { refreshToken: user.refreshToken } })
    } catch (error) {
      // Local logout must still succeed when the backend is unavailable.
    }
  }
  clearUser()
}

async function loginDefaultUser(code = '') {
  try {
    const user = await request({
      url: '/app/auth/login',
      method: 'POST',
      data: { code }
    })
    saveUser(user)
    return user
  } catch (error) {
    saveUser(fallbackUser)
    return fallbackUser
  }
}

function getWechatLoginCode() {
  return new Promise((resolve, reject) => {
    wx.login({
      success(result) {
        if (result.code) resolve(result.code)
        else reject(new Error('微信登录凭证获取失败'))
      },
      fail: reject
    })
  })
}

async function loginCurrentUser() {
  try {
    const app = getApp()
    const code = app.globalData.wechatLoginEnabled ? await getWechatLoginCode() : ''
    return loginDefaultUser(code)
  } catch (error) {
    saveUser(fallbackUser)
    return fallbackUser
  }
}

async function bindPhone(phoneCode) {
  const result = await request({ url: '/app/auth/phone', method: 'POST', data: { phoneCode } })
  const user = { ...getStoredUser(), phone: result.phone }
  saveUser(user)
  return user
}

module.exports = {
  getStoredUser,
  saveUser,
  clearUser,
  logoutUser,
  loginDefaultUser,
  loginCurrentUser,
  bindPhone,
  fallbackUser
}
