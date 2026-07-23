const baseUrl = '/api'
const SESSION_KEY = 'takeout_admin_session'

function authHeader() {
  try {
    const raw = window.localStorage.getItem(SESSION_KEY)
    const session = raw ? JSON.parse(raw) : null
    return session?.token ? { Authorization: `Bearer ${session.token}` } : {}
  } catch (error) {
    return {}
  }
}

function handleUnauthorized() {
  window.localStorage.removeItem(SESSION_KEY)
  window.dispatchEvent(new Event('takeout-admin-unauthorized'))
}

let refreshPromise = null

function refreshAdminSession() {
  if (refreshPromise) return refreshPromise
  const session = JSON.parse(window.localStorage.getItem(SESSION_KEY) || 'null')
  if (!session?.refreshToken) return Promise.reject(new Error('登录已过期'))
  refreshPromise = fetch(`${baseUrl}/admin/auth/refresh`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ refreshToken: session.refreshToken }) })
    .then(response => parseApiResponse(response, '登录已过期'))
    .then(next => { const merged = { ...session, ...next }; window.localStorage.setItem(SESSION_KEY, JSON.stringify(merged)); return merged })
    .finally(() => { refreshPromise = null })
  return refreshPromise
}

async function parseApiResponse(response, fallbackMessage) {
  let json
  try {
    json = await response.json()
  } catch (error) {
    throw new Error(fallbackMessage)
  }
  if (!response.ok) {
    throw new Error(json.message || fallbackMessage)
  }
  if (json.code !== 0) {
    const error = new Error(json.message || fallbackMessage)
    error.code = json.code
    throw error
  }
  return json.data
}

async function request(path, options = {}, canRetry = true) {
  const isAuthRequest = path.startsWith('/admin/auth/')
  const response = await fetch(`${baseUrl}${path}`, {
    method: options.method || 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...(isAuthRequest ? {} : authHeader()),
      ...(options.headers || {})
    },
    body: options.body ? JSON.stringify(options.body) : undefined
  })
  try { return await parseApiResponse(response, '请求失败') } catch (error) {
    if (error.code === 40100 && canRetry && !isAuthRequest) {
      try { await refreshAdminSession(); return request(path, options, false) } catch (refreshError) { handleUnauthorized(); throw refreshError }
    }
    throw error
  }
}

async function uploadImage(file, canRetry = true) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await fetch(`${baseUrl}/merchant/uploads/images`, {
    method: 'POST',
    headers: {
      ...authHeader()
    },
    body: formData
  })
  try { return await parseApiResponse(response, '上传失败') } catch (error) {
    if (error.code === 40100 && canRetry) { try { await refreshAdminSession(); return uploadImage(file, false) } catch (refreshError) { handleUnauthorized(); throw refreshError } }
    throw error
  }
}

const getOverview = () => request('/merchant/statistics/overview')
const getShop = () => request('/merchant/shop')
const updateShop = payload => request('/merchant/shop', { method: 'PUT', body: payload })
const getPlatformOverview = () => request('/platform/overview')
const listPlatformUsers = (query = '') => request(`/platform/users?${query}`)
const updatePlatformUserStatus = (id, status) => request(`/platform/users/${id}/status`, { method: 'POST', body: { status } })
const listPlatformShops = (query = '') => request(`/platform/shops?${query}`)
const updatePlatformShopStatus = (id, status) => request(`/platform/shops/${id}/status`, { method: 'POST', body: { status } })
const listPlatformOrders = (query = '') => request(`/platform/orders?${query}`)
const createPlatformShop = payload => request('/platform/shops', { method: 'POST', body: payload })
const updatePlatformShop = (id, payload) => request(`/platform/shops/${id}`, { method: 'PUT', body: payload })
const getPlatformOrder = id => request(`/platform/orders/${id}`)
const listPlatformConfigs = () => request('/platform/configs')
const updatePlatformConfig = (key, value) => request(`/platform/configs/${key}`, { method: 'PUT', body: { value } })
const listPlatformAudits = (query = '') => request(`/platform/audit-logs?${query}`)
const listPlatformMerchantAdmins = (query = '') => request(`/platform/merchant-admins?${query}`)
const updatePlatformMerchantAdminStatus = (id, status) => request(`/platform/merchant-admins/${id}/status`, { method: 'POST', body: { status } })
const resetPlatformMerchantAdminPassword = (id, password) => request(`/platform/merchant-admins/${id}/password`, { method: 'POST', body: { password } })
const cancelPlatformOrder = (id, reason) => request(`/platform/orders/${id}/cancel`, { method: 'POST', body: { reason } })
const loginAdmin = payload => request('/admin/auth/login', { method: 'POST', body: payload })
async function logoutAdminSession() {
  const session = JSON.parse(window.localStorage.getItem(SESSION_KEY) || 'null')
  if (!session?.refreshToken) return
  await request('/admin/auth/logout', { method: 'POST', body: { refreshToken: session.refreshToken } })
}
function listOrders(params = {}) {
  const query = new URLSearchParams()
  if (params.status) query.set('status', params.status)
  if (params.orderNo) query.set('orderNo', params.orderNo)
  query.set('page', params.page || 1)
  query.set('pageSize', params.pageSize || 10)
  return request(`/merchant/orders?${query.toString()}`)
}
const getOrderDetail = id => request(`/merchant/orders/${id}`)
const acceptOrder = id => request(`/merchant/orders/${id}/accept`, { method: 'POST' })
const rejectOrder = (id, reason) => request(`/merchant/orders/${id}/reject`, { method: 'POST', body: { reason } })
const updateOrderStatus = (id, status) => request(`/merchant/orders/${id}/status`, { method: 'POST', body: { status } })
const listCategories = () => request('/merchant/categories')
const createCategory = payload => request('/merchant/categories', { method: 'POST', body: payload })
const updateCategory = (id, payload) => request(`/merchant/categories/${id}`, { method: 'PUT', body: payload })
const deleteCategory = id => request(`/merchant/categories/${id}`, { method: 'DELETE' })
const listDishes = () => request('/merchant/dishes')
const createDish = payload => request('/merchant/dishes', { method: 'POST', body: payload })
const updateDish = (id, payload) => request(`/merchant/dishes/${id}`, { method: 'PUT', body: payload })
const deleteDish = id => request(`/merchant/dishes/${id}`, { method: 'DELETE' })
const updateDishStatus = (id, status) => request(`/merchant/dishes/${id}/status`, { method: 'POST', body: { status } })

export {
  getOverview,
  getShop,
  updateShop,
  getPlatformOverview,
  listPlatformUsers,
  updatePlatformUserStatus,
  listPlatformShops,
  updatePlatformShopStatus,
  listPlatformOrders,
  createPlatformShop,
  updatePlatformShop,
  getPlatformOrder,
  listPlatformConfigs,
  updatePlatformConfig,
  listPlatformAudits,
  listPlatformMerchantAdmins,
  updatePlatformMerchantAdminStatus,
  resetPlatformMerchantAdminPassword,
  cancelPlatformOrder,
  loginAdmin,
  logoutAdminSession,
  listOrders,
  getOrderDetail,
  acceptOrder,
  rejectOrder,
  updateOrderStatus,
  listCategories,
  createCategory,
  updateCategory,
  deleteCategory,
  listDishes,
  createDish,
  updateDish,
  deleteDish,
  updateDishStatus,
  uploadImage
}
