# TakeOut

[![CI](https://github.com/Senhai-k/TakeOut/actions/workflows/ci.yml/badge.svg)](https://github.com/Senhai-k/TakeOut/actions/workflows/ci.yml)

一个外卖点餐系统，包含用户端微信小程序、商家管理后台和 Spring Boot 后端服务。项目覆盖点餐、购物车、地址、下单、模拟支付、订单流转、商家接单、分类菜品管理和经营概览等核心流程。

## 项目亮点

- 用户端原生微信小程序：浏览商家、搜索菜品、规格选择、购物车、地址管理、确认订单、订单跟踪。
- Vue 3 管理后台：经营概览、订单处理、分类管理、菜品管理、图片上传。
- Spring Boot 后端：REST API、统一响应、参数校验、异常处理、JPA 持久化、H2 文件库。
- 登录与认证：用户端默认登录，后台使用 BCrypt + JWT 保护管理接口。
- 个人中心：登录后从后端读取订单数、累计消费和奖励积分统计，离线时保留本地 fallback。
- 基础数据：后台可一键恢复店铺、菜品、地址和 5 笔不同状态订单。
- 订单闭环：创建订单、模拟支付、商家接单/拒单、配送状态更新、用户确认完成。
- 订单评价：已完成订单可提交本地评价，并在订单详情中展示。
- 离线兜底：后端不可用时，小程序保留本地 mock/fallback 能力。
- 可验证：后端 MockMvc 接口测试覆盖订单、购物车、商家目录和统计接口，后台可执行生产构建。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 用户端 | 原生微信小程序、WXML、WXSS、JavaScript |
| 管理后台 | Vue 3、Vite、JavaScript |
| 后端 | Java 17、Spring Boot 3.3、Spring Web、Spring Validation、Spring Data JPA |
| 数据库 | H2 文件库，兼容 MySQL 模式；提供 MySQL 初始化脚本作为后续迁移参考 |
| 部署 | Docker、Docker Compose、Nginx 配置示例 |
| 测试 | JUnit 5、Spring MockMvc |

## 目录结构

```text
.
├── miniapp/      # 用户端微信小程序
├── admin-web/   # Vue 3 商家/平台管理后台
├── server/      # Spring Boot 后端服务
├── docs/        # 需求、接口、数据库、架构、订单流和部署文档
├── deploy/      # Docker、Nginx 等部署配置
└── scripts/     # 本地开发和辅助脚本
```

## 文档导航

- [系统架构](docs/architecture.md)：模块边界、请求链路和技术取舍。
- [订单流程](docs/order-flow.md)：订单状态、用户操作、商家操作和后端约束。
- [运行检查清单](docs/checklist.md)：本地运行和发布前检查项。
- [接口文档](docs/api.md)：用户端和商家端 API。
- [数据库设计](docs/database.md)：核心表结构和数据模型。
- [版本记录](CHANGELOG.md)：版本变更和验证结果。

## 本地运行

### 1. 启动后端

```powershell
cd D:\Projects\Codex\TakeOut\server
mvn spring-boot:run
```

健康检查：

```powershell
Invoke-RestMethod http://127.0.0.1:8080/api/health
```

默认数据库为 H2 文件库，数据文件位于 `server/data/`。H2 控制台默认路径为：

```text
http://127.0.0.1:8080/h2-console
```

后端也提供 profile 配置：

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

`mysql` profile 需要提前创建数据库，并通过环境变量配置连接信息。

### 2. 启动管理后台

```powershell
cd D:\Projects\Codex\TakeOut\admin-web
npm install
npm run dev
```

默认访问：

```text
http://localhost:5173
```

### 3. 导入微信小程序

使用微信开发者工具导入：

```text
D:\Projects\Codex\TakeOut\miniapp
```

本地联调时，在微信开发者工具中开启：

```text
详情 -> 本地设置 -> 不校验合法域名、web-view、TLS版本以及HTTPS证书
```

小程序默认请求：

```text
http://127.0.0.1:8080/api
```

真机调试时，需要把 `miniapp/app.js` 中的本地地址改为电脑局域网 IP。

## Docker 运行

```powershell
cd D:\Projects\Codex\TakeOut
docker compose -f deploy/docker/docker-compose.yml up --build -d
```

停止服务：

```powershell
docker compose -f deploy/docker/docker-compose.yml down
```

## 验证命令

后端测试：

```powershell
cd D:\Projects\Codex\TakeOut\server
mvn test
```

后台构建：

```powershell
cd D:\Projects\Codex\TakeOut\admin-web
npm run build
```

小程序 JS 语法检查：

```powershell
cd D:\Projects\Codex\TakeOut
Get-ChildItem -LiteralPath miniapp -Recurse -File -Filter *.js | ForEach-Object { node --check $_.FullName }
```

最近一次本地检查结果：

- `mvn test`：通过，26 个测试成功。
- `npm run build`：通过。
- 小程序 JS 语法检查：通过。
- GitHub Actions CI：通过。

## 说明

当前版本保留了以下取舍：

- 支付使用模拟支付接口，不接入真实微信支付。
- 当前用户端使用默认登录；后台已具备 BCrypt 密码校验、JWT 签发和管理接口保护，但还不是完整多角色权限系统。
- 默认使用 H2 文件库，便于 clone 后直接运行；生产化可迁移到 MySQL。
- 小程序端保留后端优先、本地 fallback 的策略，避免本地服务未启动时完全不可用。
- 图片上传当前限制为 jpg、jpeg、png、webp、gif，默认最大 2MB。

## 后续可扩展

- 接入真实注册/登录和更完整的多角色权限。
- 图片上传与对象存储。
- MySQL 生产库和 Flyway/Liquibase 数据库迁移。
- 微信支付或第三方支付沙箱。
- 管理后台路由、权限和更完整的数据筛选。
