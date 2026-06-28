const addressService = require('../../services/address')

Page({
  data: {
    form: {
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detail: '',
      houseNumber: '',
      isDefault: false
    }
  },

  async onLoad(options) {
    if (!options.id) return
    const address = await addressService.getAddress(options.id)
    if (address) {
      this.setData({
        form: address
      })
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field
    this.setData({
      [`form.${field}`]: event.detail.value
    })
  },

  onDefaultChange(event) {
    this.setData({
      'form.isDefault': event.detail.value
    })
  },

  async saveAddress() {
    const form = this.data.form
    if (!form.receiverName || !form.receiverPhone || !form.province || !form.city || !form.district || !form.detail) {
      wx.showToast({
        title: '请补全地址信息',
        icon: 'none'
      })
      return
    }
    if (!/^1\d{10}$/.test(form.receiverPhone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      })
      return
    }
    await addressService.saveAddress(form)
    wx.showToast({ title: '已保存', icon: 'success' })
    wx.navigateBack()
  }
})
