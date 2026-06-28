# 部署说明

建议部署组件：

- Nginx
- 后端服务
- MySQL
- Redis
- HTTPS 证书

## Docker 部署

仓库当前提供了后端服务的 Docker 配置：

```bash
docker compose -f deploy/docker/docker-compose.yml up --build -d
```

访问健康检查：

```text
http://localhost:8080/api/health
```

注意：

- 当前后端默认使用 H2 文件数据库，并通过 Docker volume 持久化数据，未启用 `AUTO_SERVER`。
- 如果要做生产部署，建议把数据库改成 MySQL，再单独加 Nginx 做反向代理。
