<template>
  <div class="shell platform-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">T</div>
        <div><div class="brand-title">TakeOut Platform</div><div class="brand-subtitle">平台管理控制台</div></div>
      </div>
      <nav class="nav">
        <button v-for="item in nav" :key="item.key" :class="['nav-item', view === item.key ? 'active' : '']" @click="view = item.key">{{ item.label }}</button>
      </nav>
      <div class="sidebar-footer">
        <div class="footer-label">当前账号</div>
        <div class="footer-value">{{ session.displayName }}</div>
        <button class="logout-admin" @click="$emit('logout')">退出后台</button>
      </div>
    </aside>
    <main class="content">
      <header class="topbar">
        <div><h1>{{ title }}</h1><p>{{ description }}</p></div>
        <div class="topbar-actions"><button class="ghost mobile-logout" @click="$emit('logout')">退出</button><button class="refresh-btn" :disabled="loading" @click="load">{{ loading ? '刷新中...' : '刷新数据' }}</button></div>
      </header>
      <div v-if="error" class="inline-error">{{ error }}</div>
      <template v-if="view === 'overview'">
        <section class="overview-grid">
          <article v-for="(item, index) in metrics" :key="item.label" :class="['stat-card', index === 3 ? 'accent' : '']">
            <div class="stat-value">{{ item.value }}</div><div class="stat-label">{{ item.label }}</div>
          </article>
        </section>
        <section class="platform-overview-grid">
          <article class="panel">
            <div class="panel-head compact"><h2>运营管理</h2><span class="panel-note">全平台数据</span></div>
            <div class="quick-actions platform-quick-actions">
              <button v-for="item in nav.slice(1, 5)" :key="item.key" @click="view = item.key"><strong>{{ item.label }}</strong><span>{{ navDescriptions[item.key] }}</span></button>
            </div>
          </article>
          <article class="panel platform-summary">
            <div class="panel-head compact"><h2>平台摘要</h2></div>
            <div class="summary-row"><span>平均商家订单</span><strong>{{ averageOrdersPerShop }}</strong></div>
            <div class="summary-row"><span>订单平均实付</span><strong>¥{{ averageOrderAmount }}</strong></div>
            <div class="summary-row"><span>当前管理账号</span><strong>{{ session.displayName }}</strong></div>
          </article>
        </section>
      </template>
      <section v-else class="panel">
        <div class="panel-head">
          <div><h2>{{ title }}</h2><div class="panel-subtitle">共 {{ page.total }} 条记录</div></div>
          <button v-if="view === 'shops'" class="action-btn" @click="openShopForm">新增商家</button>
        </div>
        <div v-if="shopFormVisible" class="form shop-form platform-shop-form">
          <label><span>商家名称</span><input v-model.trim="shopForm.name" /></label><label><span>联系电话</span><input v-model.trim="shopForm.phone" /></label>
          <label class="full"><span>地址</span><input v-model.trim="shopForm.address" /></label><label><span>起送价</span><input v-model.number="shopForm.minOrderAmount" type="number" min="0" /></label><label><span>配送费</span><input v-model.number="shopForm.deliveryFee" type="number" min="0" /></label>
          <label><span>管理员账号</span><input v-model.trim="shopForm.adminUsername" /></label><label><span>初始密码</span><input v-model="shopForm.adminPassword" type="password" /></label>
          <div class="form-actions full"><button class="ghost" @click="shopFormVisible=false">取消</button><button class="primary" @click="saveShopForm">创建商家</button></div>
        </div>
        <div v-if="!['configs','audits'].includes(view)" class="order-tools"><input v-model.trim="keyword" :placeholder="view === 'orders' ? '输入订单号' : '输入名称或手机号'" @keyup.enter="search" /><select v-if="view === 'orders'" v-model="shopId"><option value="">全部商家</option><option v-for="shop in shops" :key="shop.id" :value="shop.id">{{ shop.name }}</option></select><button class="action-btn" @click="search">查询</button></div>
        <div v-if="view==='audits'" class="order-tools"><select v-model="auditAction"><option value="">全部操作</option><option value="UPDATE_STATUS">状态修改</option><option value="RESET_PASSWORD">密码重置</option><option value="CANCEL">订单取消</option><option value="CREATE">创建</option><option value="UPDATE">编辑</option></select><button class="action-btn" @click="search">查询</button></div>
        <div v-if="selectedOrder" class="order-detail-summary">
          <div class="detail-summary-head"><div><span class="detail-label">订单号</span><strong>{{ selectedOrder.orderNo }}</strong></div><span :class="['status-badge', orderStatusClass(selectedOrder.orderStatus)]">{{ orderStatusText(selectedOrder.orderStatus) }}</span></div>
          <div class="detail-summary-grid"><span><small>收货人</small>{{ selectedOrder.receiverName }} · {{ selectedOrder.receiverPhone }}</span><span><small>配送地址</small>{{ selectedOrder.receiverAddress }}</span></div>
          <div class="order-items"><span v-for="item in selectedOrder.items" :key="item.dishId" class="item-pill">{{ item.dishName }} x{{ item.quantity }}</span></div>
          <div v-if="selectedOrder.refund" class="refund-note">退款 {{ selectedOrder.refund.refundNo }} · ¥{{ selectedOrder.refund.amount }} · {{ selectedOrder.refund.reason }}</div>
          <div class="detail-summary-actions"><input v-if="[10,20].includes(selectedOrder.orderStatus)" v-model.trim="cancelReason" :placeholder="selectedOrder.orderStatus===20?'填写退款原因':'填写取消原因'" /><button v-if="[10,20].includes(selectedOrder.orderStatus)" class="ghost danger-button" @click="cancelOrder">{{ selectedOrder.orderStatus===20?'退款并取消':'取消订单' }}</button><button class="ghost" @click="selectedOrder=null">关闭详情</button></div>
        </div>
        <div v-if="loading" class="empty">加载中...</div>
        <div v-else-if="records.length === 0" class="empty"><strong>暂无数据</strong><span>调整筛选条件后重试</span></div>
        <div v-else class="platform-table-wrap"><table class="platform-table">
          <thead><tr><th v-for="column in columns" :key="column.key">{{ column.label }}</th><th v-if="view !== 'audits'">操作</th></tr></thead>
          <tbody><tr v-for="item in records" :key="item.id || item.key"><td v-for="column in columns" :key="column.key"><input v-if="view==='configs'&&column.key==='value'" v-model="item.value" /><input v-else-if="view==='admins'&&column.key==='newPassword'" v-model="item.newPassword" type="password" placeholder="至少 6 位" /><span v-else-if="isStatusColumn(column.key)" :class="['status-badge', tableStatusClass(item, column.key)]">{{ display(item, column.key) }}</span><template v-else>{{ display(item, column.key) }}</template></td><td v-if="view !== 'audits'" class="table-actions"><button v-if="view==='users'||view==='shops'||view==='admins'" :class="['link-btn', item.status === 1 ? 'danger' : '']" @click="toggle(item)">{{ item.status === 1 ? '停用' : '启用' }}</button><button v-if="view==='admins'" class="link-btn" @click="resetPassword(item)">重置密码</button><button v-else-if="view==='orders'" class="link-btn" @click="showOrder(item.id)">查看详情</button><button v-else-if="view==='configs'" class="link-btn" @click="saveConfig(item)">保存</button></td></tr></tbody>
        </table></div>
        <div class="pager"><span>第 {{ page.page }} / {{ totalPages }} 页</span><div><button class="ghost" :disabled="page.page <= 1" @click="changePage(-1)">上一页</button><button class="ghost" :disabled="page.page >= totalPages" @click="changePage(1)">下一页</button></div></div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  cancelPlatformOrder,
  createPlatformShop,
  getPlatformOrder,
  getPlatformOverview,
  listPlatformAudits,
  listPlatformConfigs,
  listPlatformMerchantAdmins,
  listPlatformOrders,
  listPlatformShops,
  listPlatformUsers,
  resetPlatformMerchantAdminPassword,
  updatePlatformConfig,
  updatePlatformMerchantAdminStatus,
  updatePlatformShopStatus,
  updatePlatformUserStatus
} from './api/admin'

defineProps({ session: { type: Object, required: true } })
defineEmits(['logout'])

const nav = [
  { key: 'overview', label: '平台概览' },
  { key: 'users', label: '用户管理' },
  { key: 'shops', label: '商家管理' },
  { key: 'admins', label: '商家账号' },
  { key: 'orders', label: '全平台订单' },
  { key: 'configs', label: '系统配置' },
  { key: 'audits', label: '审计日志' }
]
const navDescriptions = {
  users: '用户状态与账号',
  shops: '商家资料与营业状态',
  admins: '商家后台账号',
  orders: '订单查询与处置'
}
const descriptions = {
  overview: '全平台关键经营指标',
  users: '查询用户并管理账号状态',
  shops: '维护商家资料与营业状态',
  admins: '管理商家后台账号',
  orders: '查询并处理全平台订单',
  configs: '维护平台运行参数',
  audits: '查看后台操作记录'
}
const tableColumns = {
  users: [{ key: 'id', label: 'ID' }, { key: 'nickname', label: '用户' }, { key: 'phone', label: '手机号' }, { key: 'status', label: '状态' }],
  shops: [{ key: 'id', label: 'ID' }, { key: 'name', label: '商家' }, { key: 'phone', label: '电话' }, { key: 'businessStatus', label: '营业' }, { key: 'status', label: '状态' }],
  admins: [{ key: 'id', label: 'ID' }, { key: 'username', label: '账号' }, { key: 'shopName', label: '商家' }, { key: 'status', label: '状态' }, { key: 'newPassword', label: '新密码' }],
  configs: [{ key: 'description', label: '配置项' }, { key: 'value', label: '值' }, { key: 'updatedAt', label: '更新时间' }],
  audits: [{ key: 'createdAt', label: '时间' }, { key: 'adminId', label: '管理员' }, { key: 'action', label: '操作' }, { key: 'targetType', label: '对象' }, { key: 'targetId', label: '对象ID' }, { key: 'detail', label: '详情' }],
  orders: [{ key: 'orderNo', label: '订单号' }, { key: 'userId', label: '用户' }, { key: 'shopName', label: '商家' }, { key: 'payAmount', label: '金额' }, { key: 'orderStatus', label: '状态' }, { key: 'createdAt', label: '时间' }]
}
const resourceLoaders = {
  users: listPlatformUsers,
  shops: listPlatformShops,
  admins: listPlatformMerchantAdmins,
  orders: listPlatformOrders,
  audits: listPlatformAudits
}

const view = ref('overview')
const overview = ref({})
const records = ref([])
const shops = ref([])
const keyword = ref('')
const shopId = ref('')
const auditAction = ref('')
const loading = ref(false)
const error = ref('')
const shopFormVisible = ref(false)
const selectedOrder = ref(null)
const cancelReason = ref('')
const page = reactive({ page: 1, pageSize: 10, total: 0 })
const shopForm = reactive({ name: '', phone: '', address: '', minOrderAmount: 0, deliveryFee: 0, adminUsername: '', adminPassword: '', businessStatus: 1, status: 1 })

const title = computed(() => nav.find(item => item.key === view.value)?.label || '平台管理')
const description = computed(() => descriptions[view.value])
const totalPages = computed(() => Math.max(Math.ceil(page.total / page.pageSize), 1))
const columns = computed(() => tableColumns[view.value] || tableColumns.orders)
const metrics = computed(() => [
  { label: '用户数', value: overview.value.userCount || 0 },
  { label: '商家数', value: overview.value.shopCount || 0 },
  { label: '订单数', value: overview.value.orderCount || 0 },
  { label: '支付金额', value: `¥${Number(overview.value.paidSalesAmount || 0).toFixed(2)}` }
])
const averageOrdersPerShop = computed(() => overview.value.shopCount
  ? (Number(overview.value.orderCount || 0) / overview.value.shopCount).toFixed(1)
  : '0.0')
const averageOrderAmount = computed(() => overview.value.orderCount
  ? (Number(overview.value.paidSalesAmount || 0) / overview.value.orderCount).toFixed(2)
  : '0.00')

function query() {
  const params = new URLSearchParams({ page: page.page, pageSize: page.pageSize })
  if (keyword.value) params.set(view.value === 'orders' ? 'orderNo' : 'keyword', keyword.value)
  if (view.value === 'orders' && shopId.value) params.set('shopId', shopId.value)
  if (view.value === 'audits' && auditAction.value) params.set('action', auditAction.value)
  return params.toString()
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    if (view.value === 'overview') {
      overview.value = await getPlatformOverview()
      return
    }
    if (view.value === 'configs') {
      records.value = await listPlatformConfigs()
      Object.assign(page, { page: 1, pageSize: records.value.length || 1, total: records.value.length })
      return
    }
    if (view.value === 'orders' && !shops.value.length) {
      shops.value = (await listPlatformShops('pageSize=100')).records || []
    }
    const result = await resourceLoaders[view.value](query())
    records.value = (result.records || []).map(item => ({ ...item, newPassword: '' }))
    Object.assign(page, { page: result.page || 1, pageSize: result.pageSize || 10, total: result.total || 0 })
  } catch (loadError) {
    error.value = loadError.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function search() {
  page.page = 1
  load()
}

function changePage(delta) {
  page.page += delta
  load()
}

async function toggle(item) {
  const status = item.status === 1 ? 0 : 1
  if (view.value === 'users') await updatePlatformUserStatus(item.id, status)
  else if (view.value === 'admins') await updatePlatformMerchantAdminStatus(item.id, status)
  else await updatePlatformShopStatus(item.id, status)
  await load()
}

function openShopForm() {
  Object.assign(shopForm, { name: '', phone: '', address: '', minOrderAmount: 0, deliveryFee: 0, adminUsername: '', adminPassword: '', businessStatus: 1, status: 1 })
  shopFormVisible.value = true
}

async function saveShopForm() {
  error.value = ''
  try {
    await createPlatformShop({ ...shopForm })
    shopFormVisible.value = false
    await load()
  } catch (saveError) {
    error.value = saveError.message || '创建失败'
  }
}

async function showOrder(id) {
  selectedOrder.value = await getPlatformOrder(id)
}

async function saveConfig(item) {
  await updatePlatformConfig(item.key, item.value)
  await load()
}

async function resetPassword(item) {
  if (!item.newPassword || item.newPassword.length < 6) {
    error.value = '新密码至少 6 位'
    return
  }
  await resetPlatformMerchantAdminPassword(item.id, item.newPassword)
  await load()
}

async function cancelOrder() {
  if (!cancelReason.value) {
    error.value = '请填写取消原因'
    return
  }
  selectedOrder.value = await cancelPlatformOrder(selectedOrder.value.id, cancelReason.value)
  cancelReason.value = ''
  await load()
}

function orderStatusText(status) {
  return ({ 10: '待支付', 20: '待接单', 30: '已接单', 40: '制作中', 50: '配送中', 60: '已完成', 70: '已取消' })[status] || '未知'
}

function orderStatusClass(status) {
  if (status === 20) return 'warning'
  if ([30, 40, 50].includes(status)) return 'info'
  if (status === 60) return 'success'
  return 'neutral'
}

function isStatusColumn(key) {
  return ['status', 'businessStatus', 'orderStatus'].includes(key)
}

function tableStatusClass(item, key) {
  if (key === 'orderStatus') return orderStatusClass(item[key])
  return item[key] === 1 ? 'success' : 'neutral'
}

function formatDate(value) {
  if (!value) return '-'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

function display(item, key) {
  if (key === 'status') return item[key] === 1 ? '启用' : '停用'
  if (key === 'businessStatus') return item[key] === 1 ? '营业中' : '休息中'
  if (key === 'orderStatus') return orderStatusText(item[key])
  if (key === 'payAmount') return `¥${Number(item[key] || 0).toFixed(2)}`
  if (['createdAt', 'updatedAt'].includes(key)) return formatDate(item[key])
  return item[key] ?? '-'
}

watch(view, () => {
  page.page = 1
  keyword.value = ''
  load()
})
onMounted(load)
</script>
