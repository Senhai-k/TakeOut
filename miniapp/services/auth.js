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

async function loginDefaultUser() {
  try {
    const user = await request({
      url: '/app/auth/login',
      method: 'POST'
    })
    saveUser(user)
    return user
  } catch (error) {
    saveUser(fallbackUser)
    return fallbackUser
  }
}

module.exports = {
  getStoredUser,
  saveUser,
  clearUser,
  loginDefaultUser,
  fallbackUser
}
