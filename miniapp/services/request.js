const app = getApp()
const storage = require('../utils/storage')

function authHeader() {
  const user = storage.get('takeout_user', null)
  return user && user.token && !String(user.token).startsWith('local-')
    ? { Authorization: `Bearer ${user.token}` }
    : {}
}

let refreshPromise = null

function refreshSession() {
  if (refreshPromise) return refreshPromise
  const user = storage.get('takeout_user', null)
  if (!user || !user.refreshToken) return Promise.reject(new Error('登录已过期'))
  refreshPromise = new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.baseUrl}/app/auth/refresh`, method: 'POST', data: { refreshToken: user.refreshToken },
      header: { 'Content-Type': 'application/json' },
      success(res) {
        if (res.data && res.data.code === 0 && res.data.data) {
          storage.set('takeout_user', { ...user, ...res.data.data })
          resolve(res.data.data)
        } else reject(res.data || new Error('登录已过期'))
      }, fail: reject
    })
  }).finally(() => { refreshPromise = null })
  return refreshPromise
}

function request(options, canRetry = true) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.baseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...authHeader(),
        ...(options.header || {})
      },
      success(res) {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(res)
          return
        }
        if (res.data && res.data.code !== 0) {
          if (res.data.code === 40100 && canRetry && !options.url.startsWith('/app/auth/')) {
            refreshSession().then(() => request(options, false)).then(resolve).catch(reject)
            return
          }
          reject(res.data)
          return
        }
        resolve(res.data ? res.data.data : null)
      },
      fail(error) {
        reject(error)
      }
    })
  })
}

module.exports = {
  request
}
