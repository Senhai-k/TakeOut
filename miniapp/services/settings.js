const storage = require('../utils/storage')

const SETTINGS_KEY = 'TAKEOUT_USER_SETTINGS'

const DEFAULT_SETTINGS = {
  orderNotice: true,
  autoLocation: true,
  showDevInfo: false
}

function getSettings() {
  return {
    ...DEFAULT_SETTINGS,
    ...(storage.get(SETTINGS_KEY, {}) || {})
  }
}

function updateSetting(key, value) {
  const settings = {
    ...getSettings(),
    [key]: Boolean(value)
  }
  storage.set(SETTINGS_KEY, settings)
  return settings
}

module.exports = {
  getSettings,
  updateSetting
}
