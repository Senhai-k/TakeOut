const storage = require('../utils/storage')

const PAYMENT_KEY = 'TAKEOUT_PAYMENT_METHOD'

const PAYMENT_METHODS = [
  { key: 'wechat', name: '微信支付', desc: '默认模拟支付方式' },
  { key: 'balance', name: '余额支付', desc: '用于本地流程验证' }
]

function getPaymentMethods() {
  return PAYMENT_METHODS
}

function getPaymentMethod() {
  const saved = storage.get(PAYMENT_KEY, null)
  return PAYMENT_METHODS.find(item => item.key === saved) || PAYMENT_METHODS[0]
}

function setPaymentMethod(key) {
  const method = PAYMENT_METHODS.find(item => item.key === key) || PAYMENT_METHODS[0]
  storage.set(PAYMENT_KEY, method.key)
  return method
}

module.exports = {
  getPaymentMethods,
  getPaymentMethod,
  setPaymentMethod
}
