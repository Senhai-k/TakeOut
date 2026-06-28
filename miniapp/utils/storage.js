function get(key, fallback) {
  try {
    const value = wx.getStorageSync(key)
    return value === '' || value === undefined ? fallback : value
  } catch (error) {
    return fallback
  }
}

function set(key, value) {
  wx.setStorageSync(key, value)
}

function remove(key) {
  wx.removeStorageSync(key)
}

module.exports = {
  get,
  set,
  remove
}
