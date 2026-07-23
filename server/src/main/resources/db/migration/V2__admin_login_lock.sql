ALTER TABLE `admin_account`
  ADD COLUMN `failed_login_attempts` INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数';

ALTER TABLE `admin_account`
  ADD COLUMN `locked_until` DATETIME DEFAULT NULL COMMENT '登录锁定截止时间';
