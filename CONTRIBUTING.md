# Contributing

TakeOut is split into three surfaces:

- `miniapp/`: WeChat mini program user client.
- `admin-web/`: Vue 3 merchant and platform console.
- `server/`: Spring Boot backend service.

## Local Validation

Run the checks that match the files you changed.

Backend:

```powershell
cd server
mvn test
```

Admin web:

```powershell
cd admin-web
npm install
npm run build
```

Miniapp JavaScript syntax:

```powershell
Get-ChildItem -LiteralPath miniapp -Recurse -File -Filter *.js | ForEach-Object { node --check $_.FullName }
```

Deployment configuration:

```powershell
docker compose -f deploy/docker/docker-compose.yml config --quiet
```

## Pull Requests

- Keep backend, admin-web, and miniapp changes scoped when possible.
- Include screenshots for visible UI changes.
- Do not commit generated build output, local databases, logs, `.env`, or WeChat private project config.
- Use `.env.example` as the public variable reference and keep real secrets out of the repository.
