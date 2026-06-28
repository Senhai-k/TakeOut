# takeout-server

TakeOut 后端服务，提供用户端小程序和商家管理后台所需的 REST API。

## 技术栈

- Java 17
- Spring Boot 3.3.5
- Spring Web / Validation / Data JPA / Security
- H2 文件数据库，MySQL 兼容模式
- JUnit 5 + Spring MockMvc

## 模块说明

```text
src/main/java/com/takeout/
├── common/       # 统一响应、错误码、异常处理
├── config/       # Web、安全、初始化配置
├── controller/   # 用户端、商家端和健康检查接口
├── dto/          # 请求和响应对象
├── domain/       # JPA 实体和枚举
├── repository/   # JPA Repository
├── security/     # JWT 和认证过滤器
└── service/      # 业务逻辑
```

## 本地运行

```powershell
mvn spring-boot:run
```

健康检查：

```powershell
Invoke-RestMethod http://127.0.0.1:8080/api/health
```

默认使用 H2 文件库：

```text
jdbc:h2:file:./data/takeout;MODE=MySQL
```

## 测试

```powershell
mvn test
```

最近一次本地结果：

```text
Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

接口详情见根目录 [docs/api.md](../docs/api.md)。
