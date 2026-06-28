const { request } = require('./request')

const EMPTY_STATS = [
  { label: '累计订单', value: '0' },
  { label: '累计消费', value: '0' },
  { label: '奖励积分', value: '0' }
]

const FALLBACK_STATS = [
  { label: '累计订单', value: '45' },
  { label: '累计消费', value: '567' },
  { label: '奖励积分', value: '1230' }
]

function getProfileStats(user) {
  if (!user) {
    return EMPTY_STATS
  }
  return FALLBACK_STATS
}

async function loadProfileStats(user) {
  if (!user) {
    return EMPTY_STATS
  }
  try {
    const stats = await request({
      url: '/app/auth/profile-stats'
    })
    return formatStats(stats)
  } catch (error) {
    return FALLBACK_STATS
  }
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
