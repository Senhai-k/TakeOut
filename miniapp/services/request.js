const app = getApp()

function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.baseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(options.header || {})
      },
      success(res) {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(res)
          return
        }
        if (res.data && res.data.code !== 0) {
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
