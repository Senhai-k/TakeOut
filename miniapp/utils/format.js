function money(value) {
  const number = Number(value || 0)
  return number.toFixed(2)
}

function orderStatusText(status) {
  const map = {
    10: '待支付',
    20: '已支付待接单',
    30: '商家已接单',
    40: '制作中',
    50: '配送中',
    60: '已完成',
    70: '已取消',
    80: '退款中',
    90: '已退款'
  }
  return map[status] || '未知状态'
}

function nowText() {
  const date = new Date()
  const pad = value => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

module.exports = {
  money,
  orderStatusText,
  nowText
}
