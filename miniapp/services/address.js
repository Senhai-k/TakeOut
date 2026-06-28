const storage = require('../utils/storage')
const { request } = require('./request')

const ADDRESS_KEY = 'TAKEOUT_ADDRESSES'

function defaultAddresses() {
  return [
    {
      id: 1,
      receiverName: '张三',
      receiverPhone: '13800000000',
      province: '广东省',
      city: '深圳市',
      district: '南山区',
      detail: '科技园',
      houseNumber: 'A 座 1001',
      isDefault: true
    }
  ]
}

async function listAddresses() {
  try {
    const addresses = await request({ url: '/app/addresses' })
    storage.set(ADDRESS_KEY, addresses)
    return addresses
  } catch (error) {
    return listLocalAddresses()
  }
}

function listLocalAddresses() {
  const addresses = storage.get(ADDRESS_KEY, null)
  if (addresses) {
    return addresses
  }
  const initial = defaultAddresses()
  storage.set(ADDRESS_KEY, initial)
  return initial
}

async function getAddress(id) {
  return (await listAddresses()).find(item => item.id === Number(id))
}

async function getDefaultAddress() {
  const addresses = await listAddresses()
  return addresses.find(item => item.isDefault) || addresses[0] || null
}

async function saveAddress(address) {
  try {
    const saved = await saveBackendAddress(address)
    saveLocalAddress(saved)
    return saved
  } catch (error) {
    return saveLocalAddress(address)
  }
}

function saveLocalAddress(address) {
  const addresses = listLocalAddresses()
  const id = address.id || Date.now()
  const next = { ...address, id }
  let result = addresses.filter(item => Number(item.id) !== Number(id))
  if (next.isDefault) {
    result = result.map(item => ({ ...item, isDefault: false }))
  }
  result.unshift(next)
  storage.set(ADDRESS_KEY, result)
  return next
}

async function removeAddress(id) {
  try {
    await request({
      url: `/app/addresses/${id}`,
      method: 'DELETE'
    })
  } catch (error) {
    // Local fallback keeps the miniapp usable when the backend is offline.
  }
  const addresses = listLocalAddresses().filter(item => Number(item.id) !== Number(id))
  storage.set(ADDRESS_KEY, addresses)
}

async function setDefaultAddress(id) {
  try {
    const address = await request({
      url: `/app/addresses/${id}/default`,
      method: 'POST'
    })
    saveLocalAddress(address)
  } catch (error) {
    // Fall through to local default switching.
  }
  const addressId = Number(id)
  const addresses = listLocalAddresses().map(item => ({
    ...item,
    isDefault: item.id === addressId
  }))
  storage.set(ADDRESS_KEY, addresses)
}

async function ensureBackendAddress(address) {
  if (!address) return null
  return saveBackendAddress(address)
}

async function saveBackendAddress(address) {
  const payload = {
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    province: address.province,
    city: address.city,
    district: address.district,
    detail: address.detail,
    houseNumber: address.houseNumber,
    isDefault: !!address.isDefault
  }
  if (!address.id) {
    return request({
      url: '/app/addresses',
      method: 'POST',
      data: payload
    })
  }
  try {
    return await request({
      url: `/app/addresses/${address.id}`,
      method: 'PUT',
      data: payload
    })
  } catch (error) {
    return request({
      url: '/app/addresses',
      method: 'POST',
      data: payload
    })
  }
}

module.exports = {
  listAddresses,
  getAddress,
  getDefaultAddress,
  saveAddress,
  removeAddress,
  setDefaultAddress,
  ensureBackendAddress
}
