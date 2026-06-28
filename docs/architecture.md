# 系统架构

本文档说明 TakeOut 的模块边界、请求链路和当前技术取舍。

## 模块划分

```text
TakeOut
├── miniapp/      用户端微信小程序
├── admin-web/   Vue 3 商家管理后台
├── server/      Spring Boot 后端 API
├── docs/        项目文档
├── deploy/      Docker、Nginx 配置
└── scripts/     本地辅助脚本
```

### miniapp

用户端使用原生微信小程序实现，覆盖商家浏览、菜品选择、购物车、地址、下单、模拟支付和订单跟踪。小程序请求后端 `/api/app/**`，并保留本地 fallback 数据，降低对后端启动状态的依赖。

### admin-web

管理后台使用 Vue 3 和 Vite 实现，覆盖登录、经营概览、订单处理、分类管理、菜品管理、图片上传和基础数据重置。后台登录后将 JWT 保存在浏览器本地，请求 `/api/merchant/**` 时自动携带 `Authorization: Bearer <token>`。

### server

后端使用 Spring Boot 3.3 和 Java 17 实现，提供用户端 API、后台认证 API、商家管理 API、文件上传和基础数据初始化。默认使用 H2 文件库，便于本地运行。

## 请求链路

### 用户端链路

```text
微信小程序页面
  -> miniapp/services/*.js
  -> /api/app/**
  -> Controller
  -> Service
  -> Repository
  -> H2 数据库
```

典型流程：

- 小程序首页加载店铺和菜品。
- 用户加入购物车并提交订单。
- 后端创建待支付订单。
- 用户调用模拟支付接口，订单进入待接单状态。
- 用户在订单详情中查看状态或确认完成。

### 管理后台链路

```text
Vue 管理后台
  -> /api/admin/auth/login
  -> 返回 JWT
  -> /api/merchant/** 携带 Bearer token
  -> JWT 过滤器校验
  -> Controller
  -> Service
  -> Repository
  -> H2 数据库
```

典型流程：

- 管理员使用 `admin / 123456` 登录。
- 后台保存 token 和管理员信息。
- 订单、分类、菜品、上传、基础数据重置接口均通过 JWT 访问。
- 后端返回登录过期时，前端清理本地会话并回到登录页。

## 后端分层

```text
controller  接收 HTTP 请求，完成参数入口和响应封装
dto         定义请求、响应和分页结构
service     承载业务规则、状态流转和数据组装
repository  基于 Spring Data JPA 访问数据库
domain      领域实体和枚举
security    JWT 签发、解析和认证过滤
config      Web、静态资源和安全配置
```

当前后端重点把业务规则放在 service 层，例如订单状态流转、起送价校验、后台登录校验、基础数据重置等，Controller 保持薄入口。

## 数据存储

默认使用 H2 文件库：

```text
server/data/
```

`docs/database.md` 和初始化脚本保留了面向 MySQL 的设计说明，后续生产化可以迁移到 MySQL，并补充 Flyway 或 Liquibase 管理数据库版本。

当前配置包含：

- 默认配置：H2 文件库，适合本地快速运行。
- `test` profile：H2 内存库，供自动化测试使用。
- `dev` profile：独立 H2 文件库，便于本地开发隔离数据。
- `mysql` profile：MySQL 连接配置模板，使用环境变量传入连接信息。

## 认证设计

### 用户端

用户端当前使用默认登录，后端返回默认用户和 token，小程序展示登录状态并支持退出。该设计不等同于真实微信登录。

### 管理后台

后台提供默认管理员账号，密码使用 BCrypt 校验。登录成功后后端签发 HMAC-SHA256 JWT，管理接口通过过滤器保护 `/api/merchant/**`。当前已经具备后台接口保护能力，但还不是完整的多角色权限系统。

## 文件上传

商家后台菜品图片上传到后端本地目录：

```text
server/uploads/
```

后端通过 `/uploads/**` 暴露静态访问路径。当前仅允许 jpg、jpeg、png、webp、gif，默认最大 2MB。正式上线建议迁移到对象存储，并增加鉴权、生命周期和内容安全策略。

## 基础数据

后端启动时会自动补齐基础数据。后台还提供受 JWT 保护的重置接口：

```text
POST /api/merchant/seed/reset
```

重置后包含 3 个店铺、12 个分类、12 个菜品、1 个默认地址和 5 笔不同状态订单。

## 当前边界

- 支付是模拟支付，不接入真实微信支付。
- 用户端登录是默认登录，不是真实微信手机号或 openid 体系。
- 后台已具备 JWT 保护，但不是完整 RBAC。
- 默认 H2 适合本地运行，不建议直接作为生产存储。
- 图片上传使用本地磁盘，生产环境建议改为对象存储。
