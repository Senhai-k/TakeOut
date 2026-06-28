# Docker 部署

当前仓库里的后端是 Spring Boot + H2 内存库，所以最简单的 Docker 部署方式是先把后端服务容器化。

## 构建并启动

在仓库根目录执行：

```bash
docker compose -f deploy/docker/docker-compose.yml up --build -d
```

启动后访问：

```text
http://localhost:8080/api/health
```

## 停止

```bash
docker compose -f deploy/docker/docker-compose.yml down
```

## 说明

- 这个镜像会在构建阶段执行 `mvn package`，再把生成的 JAR 放进运行时镜像。
- 目前配置使用 H2 文件数据库，并通过 Docker volume 持久化数据，未启用 `AUTO_SERVER`。
- 如果后面要做正式部署，建议把数据库切到 MySQL，再加 Nginx 和 HTTPS。
