# Repository Guidelines

## 项目结构与模块划分

本仓库是一个外卖小程序系统，按端和职责拆分：

- `miniapp/`：微信小程序用户端。页面在 `miniapp/pages/`，接口封装在 `miniapp/services/`，通用工具在 `miniapp/utils/`。
- `server/`：Spring Boot 后端。源码在 `server/src/main/java/com/takeout/`，测试在 `server/src/test/java/`，SQL 脚本在 `server/sql/`。
- `docs/`：需求、接口、数据库、部署等文档。
- `admin-web/`：预留的商家端/平台管理后台目录。
- `deploy/`、`scripts/`：部署说明和辅助脚本文档。

## 构建、测试与本地开发命令

后端命令在 `server/` 目录执行：

```bash
mvn test
mvn package
```

- `mvn test`：运行 Spring Boot 测试，包括 MockMvc 接口测试。
- `mvn package`：构建后端 JAR 包。

小程序使用微信开发者工具导入 `miniapp/` 目录。连接本地后端时，默认接口地址为 `http://127.0.0.1:8080/api`，需要在开发者工具中开启“不校验合法域名、web-view、TLS 版本以及 HTTPS 证书”。

## 编码规范与命名约定

后端采用常见 Spring Boot 分层：

- 控制器：`controller/app`、`controller/merchant`
- 服务：`service/*Service.java`
- DTO：`dto/app`、`dto/merchant`
- 实体：`domain/*`
- 仓储：`repository/*Repository.java`

Java 使用 4 空格缩进。DTO 优先使用 `record`。小程序页面保持微信原生命名规范：每个页面对应 `.js`、`.wxml`、`.wxss`、`.json` 四类文件。

## 测试规范

后端测试使用 JUnit 5 和 Spring MockMvc。新增接口、订单状态流转、金额计算等关键逻辑必须补测试。测试方法名建议描述行为，例如：

```java
void mockPayUpdatesOrderStatus()
```

提交前运行：

```bash
mvn test
```

前端 JS 可用 Node 做语法检查：

```bash
node --check miniapp/pages/order-detail/order-detail.js
```

## 提交与 Pull Request 规范

当前工作区无法可靠读取 Git 历史，因此不强制既有提交格式。建议使用简洁的祈使句提交信息：

```text
Add merchant order status APIs
Refine miniapp order confirmation layout
```

Pull Request 应包含：变更摘要、影响模块、测试结果；涉及界面调整时附截图或录屏。

## 安全与配置说明

不要提交真实密钥、支付证书、生产数据库账号或个人敏感信息。当前后端本地开发使用 H2/JPA，配置在 `server/src/main/resources/application.yml`。后续接入 MySQL 或生产环境时，应使用独立 profile 管理配置。
