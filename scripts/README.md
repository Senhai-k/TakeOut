# scripts

本地开发、构建、部署辅助脚本目录。

- `mysql-backup.ps1`：使用 `mysqldump` 创建一致性备份并生成 SHA-256 校验文件。
- `mysql-restore.ps1`：验证备份哈希后恢复数据库，必须显式指定 `-ConfirmRestore`。

脚本默认从 `MYSQL_PWD` 读取密码；未设置时使用安全密码提示。不要把密码写入脚本或提交到仓库。
