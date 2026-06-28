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

async function request(path, options = {}) {
  const isLoginRequest = path === '/admin/auth/login'
  const response = await fetch(`${baseUrl}${path}`, {
    method: options.method || 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...(isLoginRequest ? {} : authHeader()),
      ...(options.headers || {})
    },
    body: options.body ? JSON.stringify(options.body) : undefined
  })
  const json = await response.json()
  if (json.code !== 0) {
    if (json.code === 40100) {
      window.localStorage.removeItem(SESSION_KEY)
      window.dispatchEvent(new Event('takeout-admin-unauthorized'))
    }
    throw new Error(json.message || '请求失败')
  }
  return json.data
}

async function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await fetch(`${baseUrl}/merchant/uploads/images`, {
    method: 'POST',
    headers: {
      ...authHeader()
    },
    body: formData
  })
  const json = await response.json()
  if (json.code !== 0) {
    if (json.code === 40100) {
      window.localStorage.removeItem(SESSION_KEY)
      window.dispatchEvent(new Event('takeout-admin-unauthorized'))
    }
    throw new Error(json.message || '上传失败')
  }
  return json.data
}

const getOverview = () => request('/merchant/statistics/overview')
const loginAdmin = payload => request('/admin/auth/login', { method: 'POST', body: payload })
const resetSeedData = () => request('/merchant/seed/reset', { method: 'POST' })
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
  loginAdmin,
  resetSeedData,
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
