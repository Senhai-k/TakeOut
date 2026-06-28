const { request } = require('./request')
const orderService = require('./order')

const EMPTY_STATS = [
  { label: '累计订单', value: '0' },
  { label: '累计消费', value: '0' },
  { label: '奖励积分', value: '0' }
]

function getProfileStats() {
  return getLocalProfileStats()
}

async function loadProfileStats(user) {
  if (!user) {
    return EMPTY_STATS
  }
  const localStats = getLocalProfileStats()
  try {
    const stats = await request({
      url: '/app/auth/profile-stats'
    })
    const remoteStats = formatStats(stats)
    return hasAnyStat(remoteStats) || !hasAnyStat(localStats) ? remoteStats : localStats
  } catch (error) {
    return localStats
  }
}

function getLocalProfileStats() {
  const orders = orderService.listLocalOrders()
  if (!orders.length) {
    return EMPTY_STATS
  }
  const orderCount = orders.length
  const totalSpent = orders
    .filter(item => Number(item.payStatus) === 1 && Number(item.orderStatus) !== 70)
    .reduce((sum, item) => sum + Number(item.payAmount || 0), 0)
  return formatStats({
    orderCount,
    totalSpent,
    rewardPoints: Math.floor(totalSpent) * 2
  })
}

function hasAnyStat(stats) {
  return stats.some(item => Number(item.value) > 0)
}

function formatStats(stats) {
  return [
    { label: '累计订单', value: String(stats.orderCount || 0) },
    { label: '累计消费', value: String(Math.floor(Number(stats.totalSpent || 0))) },
    { label: '奖励积分', value: String(stats.rewardPoints || 0) }
  ]
}

module.exports = {
  getProfileStats,
  loadProfileStats
}
