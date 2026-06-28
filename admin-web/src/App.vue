<template>
  <div v-if="!adminSession" class="login-page">
    <section class="login-card">
      <div class="brand login-brand">
        <div class="brand-mark">T</div>
        <div>
          <div class="brand-title">TakeOut Admin</div>
          <div class="brand-subtitle">商家管理后台</div>
        </div>
      </div>
      <h1>管理员登录</h1>
      <p>登录后可管理订单、分类、菜品和经营数据。</p>
      <div class="login-form">
        <label>
          <span>账号</span>
          <input v-model="loginForm.username" autocomplete="username" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="loginForm.password" type="password" autocomplete="current-password" @keyup.enter="submitLogin" />
        </label>
        <div v-if="loginError" class="login-error">{{ loginError }}</div>
        <button class="primary login-submit" :disabled="loginLoading" @click="submitLogin">
          {{ loginLoading ? '登录中...' : '登录后台' }}
        </button>
      </div>
      <div class="default-account">默认账号：admin / 123456</div>
    </section>
  </div>

  <div v-else class="shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">T</div>
        <div>
          <div class="brand-title">TakeOut Admin</div>
          <div class="brand-subtitle">商家管理后台</div>
        </div>
      </div>

      <nav class="nav">
        <button
          v-for="item in navItems"
          :key="item.key"
          :class="['nav-item', activeView === item.key ? 'active' : '']"
          @click="activeView = item.key"
        >
          {{ item.label }}
        </button>
      </nav>

      <div class="sidebar-footer">
        <div class="footer-label">当前账号</div>
        <div class="footer-value">{{ adminSession.displayName }} · {{ adminSession.shopName }}</div>
        <div class="footer-label">后端地址</div>
        <div class="footer-value">{{ baseUrl }}</div>
        <button class="logout-admin" @click="logoutAdmin">退出后台</button>
      </div>
    </aside>

    <main class="content">
      <header class="topbar">
        <div>
          <h1>{{ currentTitle }}</h1>
          <p>{{ currentDescription }}</p>
        </div>
        <div class="topbar-actions">
          <button class="ghost" @click="resetSeed">重置基础数据</button>
          <button class="refresh-btn" @click="refresh">刷新数据</button>
        </div>
      </header>

      <section class="overview-grid">
        <article class="stat-card accent">
          <div class="stat-value">{{ overview.todayOrderCount }}</div>
          <div class="stat-label">今日订单</div>
        </article>
        <article class="stat-card">
          <div class="stat-value">¥{{ formatMoney(overview.todaySalesAmount) }}</div>
          <div class="stat-label">今日销售额</div>
        </article>
        <article class="stat-card">
          <div class="stat-value">{{ overview.pendingOrderCount }}</div>
          <div class="stat-label">待接单</div>
        </article>
        <article class="stat-card">
          <div class="stat-value">{{ overview.dishCount }}</div>
          <div class="stat-label">在售菜品</div>
        </article>
      </section>

      <section v-if="activeView === 'orders'" class="panel">
        <div class="panel-head">
          <div>
            <h2>订单列表</h2>
            <div class="panel-subtitle">{{ orderListSummary }}</div>
          </div>
          <div class="chips">
            <button
              v-for="item in filters"
              :key="item.status"
              :class="['chip', activeStatus === item.status ? 'active' : '']"
              @click="activeStatus = item.status"
            >
              {{ item.label }}
            </button>
          </div>
        </div>
        <div class="order-tools">
          <input
            v-model.trim="orderSearch"
            placeholder="输入订单号搜索"
            @keyup.enter="searchOrders"
          />
          <button class="action-btn" @click="searchOrders">搜索</button>
          <button class="ghost" @click="resetOrderSearch">重置</button>
        </div>
        <div v-if="orderError" class="inline-error">{{ orderError }}</div>
        <div v-if="loading" class="empty">加载中...</div>
        <div v-else-if="orders.length === 0" class="empty">暂无订单</div>
        <div v-else class="orders">
          <article v-for="order in orders" :key="order.id" class="order-card">
            <div class="order-head">
              <div>
                <div class="order-no">{{ order.displayNo }}</div>
                <div class="order-meta">{{ order.shopName }} · {{ order.createdAt || '基础订单' }}</div>
              </div>
              <div class="order-side">
                <span :class="['status-badge', statusClass(order.orderStatus)]">{{ order.statusText }}</span>
                <div class="order-amount">¥{{ formatMoney(order.payAmount) }}</div>
              </div>
            </div>
            <div class="order-info">
              <span>{{ order.receiverName }}</span>
              <span>{{ order.receiverPhone }}</span>
            </div>
            <div class="order-address">{{ order.receiverAddress }}</div>
            <div class="order-items">
              <span v-for="item in order.items" :key="`${order.id}-${item.dishId}`" class="item-pill">
                {{ item.dishName }} x{{ item.quantity }}
              </span>
            </div>
            <div class="order-actions">
              <button class="link-btn" :disabled="isOrderBusy(order.id)" @click="openOrderDetail(order.id)">查看详情</button>
              <button v-if="order.orderStatus === 20" class="link-btn" :disabled="isOrderBusy(order.id)" @click="acceptMerchantOrder(order.id)">接单</button>
              <button v-if="order.orderStatus === 20" class="link-btn danger" :disabled="isOrderBusy(order.id)" @click="rejectMerchantOrder(order.id)">拒单</button>
              <button v-if="nextOrderStatus(order)" class="link-btn" :disabled="isOrderBusy(order.id)" @click="advanceOrder(order)">
                {{ isOrderBusy(order.id) ? '处理中...' : nextOrderStatus(order).label }}
              </button>
            </div>
          </article>
        </div>
        <div class="pager">
          <span>共 {{ orderPage.total }} 条，第 {{ orderPage.page }} / {{ totalOrderPages }} 页</span>
          <div>
            <button class="ghost" :disabled="orderPage.page <= 1" @click="changeOrderPage(orderPage.page - 1)">上一页</button>
            <button class="ghost" :disabled="orderPage.page >= totalOrderPages" @click="changeOrderPage(orderPage.page + 1)">下一页</button>
          </div>
        </div>
      </section>

      <section v-else-if="activeView === 'categories'" class="panel">
        <div class="panel-head">
          <h2>分类管理</h2>
          <button class="action-btn" @click="openCategoryForm()">新增分类</button>
        </div>
        <div class="table">
          <div class="row category-head">
            <span>名称</span>
            <span>排序</span>
            <span>状态</span>
            <span>操作</span>
          </div>
          <div v-for="item in categories" :key="item.id" class="row category-row">
            <span>{{ item.name }}</span>
            <span>{{ item.sort }}</span>
            <span>{{ item.status === 1 ? '启用' : '禁用' }}</span>
            <span class="actions">
              <button class="link-btn" @click="openCategoryForm(item)">编辑</button>
              <button class="link-btn danger" @click="removeCategory(item.id)">删除</button>
            </span>
          </div>
        </div>
      </section>

      <section v-else-if="activeView === 'dishes'" class="panel">
        <div class="panel-head">
          <div>
            <h2>菜品管理</h2>
            <div class="panel-subtitle">共 {{ dishes.length }} 个菜品，{{ activeDishCount }} 个上架，{{ lowStockDishCount }} 个低库存</div>
          </div>
          <button class="action-btn" @click="openDishForm()">新增菜品</button>
        </div>
        <div class="table">
          <div class="row dish-head">
            <span>名称</span>
            <span>分类</span>
            <span>价格</span>
            <span>库存</span>
            <span>状态</span>
            <span>操作</span>
          </div>
          <div v-for="item in dishes" :key="item.id" class="row dish-row">
            <span class="dish-cell">
              <img v-if="item.imageUrl" :src="item.imageUrl" alt="" />
              <span v-else class="dish-thumb-empty">无图</span>
              <span>
                <strong>{{ item.name }}</strong>
                <small>{{ item.description || '暂无简介' }}</small>
              </span>
            </span>
            <span>{{ item.categoryName }}</span>
            <span>¥{{ formatMoney(item.price) }}</span>
            <span>
              <span :class="['status-badge', stockClass(item.stock)]">{{ stockText(item.stock) }}</span>
            </span>
            <span>
              <span :class="['status-badge', item.status === 1 ? 'success' : 'muted']">{{ item.status === 1 ? '上架' : '下架' }}</span>
            </span>
            <span class="actions">
              <button class="link-btn" @click="openDishForm(item)">编辑</button>
              <button class="link-btn" @click="toggleDishStatus(item)">
                {{ item.status === 1 ? '下架' : '上架' }}
              </button>
              <button class="link-btn danger" @click="removeDish(item.id)">删除</button>
            </span>
          </div>
        </div>
      </section>

      <section v-else class="dashboard-board">
        <section class="panel hero-panel">
          <div>
            <div class="hero-kicker">Operations</div>
            <div class="hero-title">订单、菜品和经营数据已接入真实后端</div>
            <p class="hero-copy">
              当前后台支持 JWT 登录、基础数据重置、订单处理、分类菜品维护和本地图片上传。
            </p>
          </div>
          <div class="hero-actions">
            <button class="primary" @click="activeView = 'orders'">处理订单</button>
            <button class="ghost" @click="activeView = 'dishes'">维护菜品</button>
          </div>
        </section>

        <section class="dashboard-grid">
          <article class="panel focus-panel">
            <div class="panel-head compact">
              <h2>今日处理重点</h2>
              <button class="link-btn" @click="activeView = 'orders'">查看订单</button>
            </div>
            <div class="focus-list">
              <div class="focus-item">
                <span>待接单订单</span>
                <strong>{{ overview.pendingOrderCount }}</strong>
              </div>
              <div class="focus-item">
                <span>今日销售额</span>
                <strong>¥{{ formatMoney(overview.todaySalesAmount) }}</strong>
              </div>
              <div class="focus-item">
                <span>在售菜品</span>
                <strong>{{ overview.dishCount }}</strong>
              </div>
            </div>
          </article>

          <article class="panel flow-panel">
            <div class="panel-head compact">
              <h2>业务链路</h2>
              <button class="link-btn" @click="resetSeed">重置数据</button>
            </div>
            <div class="flow-steps">
              <span>登录后台</span>
              <span>重置数据</span>
              <span>小程序下单</span>
              <span>模拟支付</span>
              <span>后台接单</span>
              <span>完成订单</span>
            </div>
          </article>
        </section>

        <section class="panel capability-panel">
          <div class="panel-head compact">
            <h2>功能覆盖</h2>
            <span class="panel-note">当前版本边界说明</span>
          </div>
          <div class="capability-grid">
            <div class="capability-item">
              <strong>后台认证</strong>
              <span>BCrypt 密码校验，JWT 保护管理接口。</span>
            </div>
            <div class="capability-item">
              <strong>订单闭环</strong>
              <span>下单、支付、接单、制作、配送、完成。</span>
            </div>
            <div class="capability-item">
              <strong>商品维护</strong>
              <span>分类、菜品、库存、上下架和图片上传。</span>
            </div>
            <div class="capability-item">
              <strong>数据恢复</strong>
              <span>一键恢复基础数据，小程序保留本地 fallback。</span>
            </div>
          </div>
        </section>
      </section>
    </main>
  </div>

  <div v-if="formVisible" class="modal-mask" @click.self="closeForm">
    <div class="modal">
      <h3>{{ formMode === 'category' ? '分类' : '菜品' }}{{ currentEditId ? '编辑' : '新增' }}</h3>
      <div class="form">
        <template v-if="formMode === 'category'">
          <label>
            <span>名称</span>
            <input v-model="form.name" />
          </label>
          <label>
            <span>排序</span>
            <input v-model.number="form.sort" type="number" />
          </label>
          <label>
            <span>状态</span>
            <select v-model.number="form.status">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </label>
        </template>

        <template v-else>
          <label>
            <span>分类</span>
            <select v-model.number="form.categoryId">
              <option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option>
            </select>
          </label>
          <label>
            <span>名称</span>
            <input v-model="form.name" />
          </label>
          <label>
            <span>价格</span>
            <input v-model.number="form.price" type="number" step="0.01" />
          </label>
          <label>
            <span>库存</span>
            <input v-model.number="form.stock" type="number" />
          </label>
          <label>
            <span>状态</span>
            <select v-model.number="form.status">
              <option :value="1">上架</option>
              <option :value="0">下架</option>
            </select>
          </label>
          <label>
            <span>图片地址</span>
            <input v-model="form.imageUrl" />
          </label>
          <label>
            <span>上传图片</span>
            <input type="file" accept="image/*" :disabled="uploading" @change="handleImageUpload" />
          </label>
          <div v-if="form.imageUrl" class="image-preview full">
            <img :src="form.imageUrl" alt="菜品图片预览" />
            <span>{{ form.imageUrl }}</span>
          </div>
          <div v-if="uploading" class="form-hint full">图片上传中...</div>
          <label class="full">
            <span>简介</span>
            <textarea v-model="form.description"></textarea>
          </label>
        </template>
      </div>
      <div v-if="formError" class="inline-error form-error">{{ formError }}</div>
      <div class="modal-actions">
        <button class="ghost" :disabled="savingForm" @click="closeForm">取消</button>
        <button class="primary" :disabled="savingForm || uploading" @click="saveForm">
          {{ savingForm ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>

  <div v-if="orderDetailVisible" class="modal-mask" @click.self="closeOrderDetail">
    <div class="modal order-modal">
      <div class="modal-title-row">
        <h3>订单详情</h3>
        <button class="ghost" @click="closeOrderDetail">关闭</button>
      </div>
      <div v-if="!currentOrder" class="empty">加载中...</div>
      <div v-else class="order-detail">
        <div class="detail-grid">
          <div>
            <span>订单号</span>
            <strong>{{ currentOrder.orderNo }}</strong>
          </div>
          <div>
            <span>状态</span>
            <strong>{{ statusText(currentOrder.orderStatus) }}</strong>
          </div>
          <div>
            <span>收货人</span>
            <strong>{{ currentOrder.receiverName }} · {{ currentOrder.receiverPhone }}</strong>
          </div>
          <div>
            <span>实付金额</span>
            <strong>¥{{ formatMoney(currentOrder.payAmount) }}</strong>
          </div>
        </div>
        <div class="detail-address">{{ currentOrder.receiverAddress }}</div>
        <div v-if="currentOrder.remark" class="detail-remark">备注：{{ currentOrder.remark }}</div>
        <div class="detail-items">
          <div v-for="item in currentOrder.items" :key="`${currentOrder.id}-${item.dishId}`" class="detail-item">
            <div>
              <strong>{{ item.dishName }}</strong>
              <span>{{ item.sizeOption || '默认规格' }} · {{ item.spiceOption || '默认辣度' }}</span>
            </div>
            <div>x{{ item.quantity }}</div>
            <div>¥{{ formatMoney(item.subtotalAmount) }}</div>
          </div>
        </div>
        <div class="modal-actions">
          <button v-if="currentOrder.orderStatus === 20" class="primary" :disabled="isOrderBusy(currentOrder.id)" @click="acceptMerchantOrder(currentOrder.id)">接单</button>
          <button v-if="currentOrder.orderStatus === 20" class="ghost" :disabled="isOrderBusy(currentOrder.id)" @click="rejectMerchantOrder(currentOrder.id)">拒单</button>
          <button v-if="nextOrderStatus(currentOrder)" class="primary" :disabled="isOrderBusy(currentOrder.id)" @click="advanceOrder(currentOrder)">
            {{ isOrderBusy(currentOrder.id) ? '处理中...' : nextOrderStatus(currentOrder).label }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import {
  acceptOrder,
  createCategory,
  createDish,
  deleteCategory,
  deleteDish,
  getOrderDetail,
  getOverview,
  listCategories,
  listDishes,
  listOrders,
  loginAdmin,
  resetSeedData,
  rejectOrder,
  updateCategory,
  updateDish,
  updateDishStatus,
  updateOrderStatus,
  uploadImage
} from './api/admin'

const baseUrl = '/api'
const navItems = [
  { key: 'dashboard', label: '经营概览' },
  { key: 'orders', label: '订单管理' },
  { key: 'categories', label: '分类管理' },
  { key: 'dishes', label: '菜品管理' }
]

const activeView = ref('dashboard')
const activeStatus = ref(0)
const loading = ref(false)
const overview = ref({
  todayOrderCount: 0,
  todaySalesAmount: 0,
  pendingOrderCount: 0,
  dishCount: 0
})
const orders = ref([])
const orderSearch = ref('')
const orderPage = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})
const categories = ref([])
const dishes = ref([])
const formVisible = ref(false)
const formMode = ref('category')
const currentEditId = ref(null)
const form = reactive({})
const loginForm = reactive({
  username: 'admin',
  password: '123456'
})
const loginLoading = ref(false)
const loginError = ref('')
const adminSession = ref(readAdminSession())
const orderDetailVisible = ref(false)
const currentOrder = ref(null)
const orderError = ref('')
const orderBusyId = ref(null)
const formError = ref('')
const savingForm = ref(false)
const uploading = ref(false)

const filters = [
  { label: '全部', status: 0 },
  { label: '待接单', status: 20 },
  { label: '已接单', status: 30 },
  { label: '制作中', status: 40 },
  { label: '配送中', status: 50 },
  { label: '已完成', status: 60 }
]

const currentTitle = computed(() => {
  const item = navItems.find(nav => nav.key === activeView.value)
  return item ? item.label : 'TakeOut Admin'
})

const currentDescription = computed(() => {
  if (activeView.value === 'orders') return '查看和处理当前店铺订单'
  if (activeView.value === 'categories') return '维护菜品分类和排序'
  if (activeView.value === 'dishes') return '维护商品、价格、库存和上架状态'
  return '快速查看今日经营核心指标'
})

const totalOrderPages = computed(() => Math.max(Math.ceil(orderPage.total / orderPage.pageSize), 1))
const activeDishCount = computed(() => dishes.value.filter(item => item.status === 1).length)
const lowStockDishCount = computed(() => dishes.value.filter(item => Number(item.stock) <= 10).length)

const orderListSummary = computed(() => {
  const filter = filters.find(item => item.status === activeStatus.value)
  const filterText = filter?.label || '全部'
  const searchText = orderSearch.value ? `，订单号包含 ${orderSearch.value}` : ''
  return `当前筛选：${filterText}${searchText}，共 ${orderPage.total} 条`
})

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}

function statusText(status) {
  const item = filters.find(filter => filter.status === status)
  if (item && item.status !== 0) return item.label
  if (status === 10) return '待支付'
  if (status === 70) return '已取消'
  if (status === 80) return '退款中'
  if (status === 90) return '已退款'
  return `状态 ${status}`
}

function statusClass(status) {
  if (status === 20) return 'warning'
  if (status === 30 || status === 40) return 'info'
  if (status === 50) return 'primary'
  if (status === 60) return 'success'
  if (status === 70) return 'muted'
  return 'neutral'
}

function stockText(stock) {
  const value = Number(stock || 0)
  if (value <= 0) return '售罄'
  if (value <= 10) return `低库存 ${value}`
  return `库存 ${value}`
}

function stockClass(stock) {
  const value = Number(stock || 0)
  if (value <= 0) return 'muted'
  if (value <= 10) return 'warning'
  return 'success'
}

function isOrderBusy(id) {
  return orderBusyId.value === id
}

function normalizeOrder(order) {
  return {
    ...order,
    displayNo: order.orderNo || `订单 ${order.id}`,
    statusText: statusText(order.orderStatus)
  }
}

function nextOrderStatus(order) {
  if (!order) return null
  if (order.orderStatus === 30) return { status: 40, label: '开始制作' }
  if (order.orderStatus === 40) return { status: 50, label: '开始配送' }
  if (order.orderStatus === 50) return { status: 60, label: '完成订单' }
  return null
}

async function refresh() {
  await Promise.all([loadOverview(), loadByView()])
}

async function resetSeed() {
  if (!window.confirm('确定重置基础数据吗？当前订单、购物车和基础数据会恢复到初始状态。')) return
  await resetSeedData()
  orderPage.page = 1
  orderSearch.value = ''
  await refresh()
}

function readAdminSession() {
  try {
    const raw = window.localStorage.getItem('takeout_admin_session')
    return raw ? JSON.parse(raw) : null
  } catch (error) {
    return null
  }
}

function saveAdminSession(session) {
  adminSession.value = session
  window.localStorage.setItem('takeout_admin_session', JSON.stringify(session))
}

async function submitLogin() {
  if (loginLoading.value) return
  loginLoading.value = true
  loginError.value = ''
  try {
    const session = await loginAdmin({
      username: loginForm.username,
      password: loginForm.password
    })
    saveAdminSession(session)
    await refresh()
  } catch (error) {
    loginError.value = error.message || '登录失败'
  } finally {
    loginLoading.value = false
  }
}

function logoutAdmin() {
  window.localStorage.removeItem('takeout_admin_session')
  adminSession.value = null
}

function handleUnauthorized() {
  adminSession.value = null
  loginError.value = '登录已过期，请重新登录'
}

async function loadOverview() {
  try {
    overview.value = await getOverview()
  } catch (error) {
    overview.value = {
      todayOrderCount: 0,
      todaySalesAmount: 0,
      pendingOrderCount: 0,
      dishCount: 0
    }
  }
}

async function loadOrders() {
  loading.value = true
  orderError.value = ''
  try {
    const page = await listOrders({
      status: activeStatus.value,
      orderNo: orderSearch.value,
      page: orderPage.page,
      pageSize: orderPage.pageSize
    })
    orders.value = (page.records || []).map(normalizeOrder)
    orderPage.total = page.total || 0
    orderPage.page = page.page || orderPage.page
    orderPage.pageSize = page.pageSize || orderPage.pageSize
  } catch (error) {
    orders.value = []
    orderPage.total = 0
    orderError.value = error.message || '订单加载失败'
  } finally {
    loading.value = false
  }
}

function searchOrders() {
  orderPage.page = 1
  loadOrders()
}

function resetOrderSearch() {
  orderSearch.value = ''
  orderPage.page = 1
  loadOrders()
}

function changeOrderPage(page) {
  orderPage.page = Math.min(Math.max(page, 1), totalOrderPages.value)
  loadOrders()
}

async function loadCategories() {
  try {
    const page = await listCategories()
    categories.value = page.records || []
  } catch (error) {
    categories.value = []
  }
}

async function loadDishes() {
  try {
    const page = await listDishes()
    dishes.value = page.records || []
  } catch (error) {
    dishes.value = []
  }
}

async function loadByView() {
  if (activeView.value === 'orders') return loadOrders()
  if (activeView.value === 'categories') return loadCategories()
  if (activeView.value === 'dishes') return loadDishes()
  return Promise.resolve()
}

function openCategoryForm(item = null) {
  formMode.value = 'category'
  currentEditId.value = item ? item.id : null
  formError.value = ''
  Object.assign(form, {
    name: item?.name || '',
    sort: item?.sort || 1,
    status: item?.status ?? 1
  })
  formVisible.value = true
}

function openDishForm(item = null) {
  if (!categories.value.length) {
    window.alert('请先创建分类，再新增菜品')
    return
  }
  formMode.value = 'dish'
  currentEditId.value = item ? item.id : null
  formError.value = ''
  Object.assign(form, {
    categoryId: item?.categoryId || categories.value[0]?.id || 0,
    name: item?.name || '',
    imageUrl: item?.imageUrl || '',
    description: item?.description || '',
    price: item?.price || 0,
    stock: item?.stock || 0,
    status: item?.status ?? 1
  })
  formVisible.value = true
}

function closeForm() {
  formVisible.value = false
  formError.value = ''
}

async function saveForm() {
  if (savingForm.value) return
  formError.value = validateForm()
  if (formError.value) return
  savingForm.value = true
  try {
    if (formMode.value === 'category') {
      const payload = {
        name: form.name.trim(),
        sort: Number(form.sort),
        status: Number(form.status)
      }
      if (currentEditId.value) {
        await updateCategory(currentEditId.value, payload)
      } else {
        await createCategory(payload)
      }
      await loadCategories()
    } else {
      const payload = {
        categoryId: Number(form.categoryId),
        name: form.name.trim(),
        imageUrl: form.imageUrl,
        description: form.description,
        price: Number(form.price),
        stock: Number(form.stock),
        status: Number(form.status)
      }
      if (currentEditId.value) {
        await updateDish(currentEditId.value, payload)
      } else {
        await createDish(payload)
      }
      await loadDishes()
    }
    closeForm()
    await loadOverview()
  } catch (error) {
    formError.value = error.message || '保存失败'
  } finally {
    savingForm.value = false
  }
}

function validateForm() {
  if (!form.name || !form.name.trim()) return formMode.value === 'category' ? '请输入分类名称' : '请输入菜品名称'
  if (formMode.value === 'category') {
    if (!Number.isFinite(Number(form.sort))) return '请输入有效排序值'
    return ''
  }
  if (!Number(form.categoryId)) return '请选择分类'
  if (!Number.isFinite(Number(form.price)) || Number(form.price) < 0) return '请输入有效价格'
  if (!Number.isInteger(Number(form.stock)) || Number(form.stock) < 0) return '库存必须是大于等于 0 的整数'
  return ''
}

async function handleImageUpload(event) {
  const file = event.target.files && event.target.files[0]
  if (!file) return
  uploading.value = true
  formError.value = ''
  try {
    const result = await uploadImage(file)
    form.imageUrl = result.url
  } catch (error) {
    formError.value = error.message || '上传失败'
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

async function openOrderDetail(id) {
  orderDetailVisible.value = true
  currentOrder.value = null
  try {
    currentOrder.value = normalizeOrder(await getOrderDetail(id))
  } catch (error) {
    window.alert(error.message || '订单加载失败')
    closeOrderDetail()
  }
}

function closeOrderDetail() {
  orderDetailVisible.value = false
  currentOrder.value = null
}

async function refreshOrderAfterAction(order) {
  const normalized = normalizeOrder(order)
  currentOrder.value = normalized
  orders.value = orders.value.map(item => (item.id === normalized.id ? normalized : item))
  await loadOverview()
}

async function acceptMerchantOrder(id) {
  await runOrderAction(id, () => acceptOrder(id))
}

async function rejectMerchantOrder(id) {
  const reason = window.prompt('请输入拒单原因', '商家暂时无法接单')
  if (reason === null) return
  await runOrderAction(id, () => rejectOrder(id, reason))
}

async function advanceOrder(order) {
  const next = nextOrderStatus(order)
  if (!next) return
  await runOrderAction(order.id, () => updateOrderStatus(order.id, next.status))
}

async function runOrderAction(id, action) {
  if (orderBusyId.value) return
  orderBusyId.value = id
  orderError.value = ''
  try {
    const updated = await action()
    await refreshOrderAfterAction(updated)
  } catch (error) {
    orderError.value = error.message || '订单操作失败'
    window.alert(orderError.value)
  } finally {
    orderBusyId.value = null
  }
}

async function removeCategory(id) {
  if (!window.confirm('确定删除这个分类吗？')) return
  await deleteCategory(id)
  await loadCategories()
  await loadOverview()
}

async function removeDish(id) {
  if (!window.confirm('确定删除这个菜品吗？')) return
  await deleteDish(id)
  await loadDishes()
  await loadOverview()
}

async function toggleDishStatus(item) {
  await updateDishStatus(item.id, item.status === 1 ? 0 : 1)
  await loadDishes()
}

watch(activeStatus, () => {
  if (activeView.value === 'orders') {
    orderPage.page = 1
    loadOrders()
  }
})

watch(activeView, () => {
  loadByView()
})

onMounted(async () => {
  window.addEventListener('takeout-admin-unauthorized', handleUnauthorized)
  if (adminSession.value) {
    await refresh()
  }
})

onUnmounted(() => {
  window.removeEventListener('takeout-admin-unauthorized', handleUnauthorized)
})
</script>
