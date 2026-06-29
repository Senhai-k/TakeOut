# TakeOut

[![CI](https://github.com/Senhai-k/TakeOut/actions/workflows/ci.yml/badge.svg)](https://github.com/Senhai-k/TakeOut/actions/workflows/ci.yml)

一个外卖点餐系统，包含用户端微信小程序、商家管理后台和 Spring Boot 后端服务。项目覆盖点餐、购物车、地址、下单、模拟支付、订单流转、商家处理、分类菜品管理和经营概览等核心流程。

## 项目亮点

- 用户端：商家浏览、菜品搜索、规格选择、购物车、地址管理、确认订单、订单跟踪、评价和个人中心。
- 商家后台：经营概览、订单处理、用户评价查看、分类管理、菜品管理和图片上传。
- 后端服务：REST API、统一响应、参数校验、异常处理、JPA 持久化、H2 文件库和 JWT 管理端认证。
- 订单数据：后端优先，保留本地 fallback，并统一合并支付方式、评价等本地补充字段。
- 工程验证：后端接口测试、管理后台生产构建、小程序 JS 语法检查和 GitHub Actions CI。

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
- [版本记录](CHANGELOG.md)：版本变更摘要。

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

## 说明

- 支付使用模拟支付接口，不接入真实微信支付。
- 当前用户端使用默认登录；后台已具备 BCrypt 密码校验、JWT 签发和管理接口保护，但还不是完整多角色权限系统。
- 默认使用 H2 文件库，便于 clone 后直接运行；生产化可迁移到 MySQL。
- 小程序端保留后端优先、本地 fallback 的策略，避免本地服务未启动时完全不可用。
- 图片上传当前限制为 jpg、jpeg、png、webp、gif，默认最大 2MB。
