-- EEMS V2 batch 1: ONE-TIME incremental schema, MySQL >= 8.0.16 / InnoDB.
-- Requires event_system_v2_multisite_foundation.sql already applied.
-- Run the read-only event_system_v2_admin_site_precheck.sql and verify its results first.
-- Select the database explicitly in the client; this file intentionally has no USE.
-- Back up the database, stop application writes, and do NOT use mysql --force.
-- MySQL DDL implicitly commits. This file is NOT transactional or rerunnable.

ALTER TABLE `admin_user`
    ADD COLUMN `auth_version` BIGINT NOT NULL DEFAULT 0
        COMMENT '认证版本，后续密码或账号状态变更时递增',
    ADD CONSTRAINT `chk_admin_user_auth_version` CHECK (`auth_version` >= 0);

ALTER TABLE `site`
    MODIFY COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'DISABLED'
        COMMENT '站点状态：ENABLED、DISABLED',
    ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间，历史站点记录迁移时间',
    ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    ADD KEY `idx_site_status_created` (`status`, `created_at`),
    ADD CONSTRAINT `chk_site_status` CHECK (`status` IN ('ENABLED', 'DISABLED'));

ALTER TABLE `site_domain`
    ADD COLUMN `primary_site_id` BIGINT UNSIGNED
        GENERATED ALWAYS AS (
            CASE WHEN `is_primary` = 1 THEN `site_id` ELSE NULL END
        ) STORED COMMENT '主域名所属站点，别名为空，用于主域名唯一约束',
    ADD UNIQUE KEY `uk_site_domain_primary_site` (`primary_site_id`),
    ADD CONSTRAINT `chk_site_domain_is_primary` CHECK (`is_primary` IN (0, 1));

ALTER TABLE `site_config`
    MODIFY COLUMN `config_code` VARCHAR(50) NOT NULL DEFAULT 'default'
        COMMENT '站点内配置标识，各站点均使用default';

CREATE TABLE `admin_user_site` (
    `admin_user_id` BIGINT UNSIGNED NOT NULL COMMENT '管理员ID',
    `site_id` BIGINT UNSIGNED NOT NULL COMMENT '授权站点ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    PRIMARY KEY (`admin_user_id`, `site_id`),
    KEY `idx_admin_user_site_site` (`site_id`, `admin_user_id`),
    CONSTRAINT `fk_admin_user_site_admin`
        FOREIGN KEY (`admin_user_id`) REFERENCES `admin_user` (`id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `fk_admin_user_site_site`
        FOREIGN KEY (`site_id`) REFERENCES `site` (`id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='普通管理员站点授权，复合主键，撤销授权时物理删除';

-- Read-only verification: compare definitions with the statements above.
SHOW CREATE TABLE `admin_user`;
SHOW CREATE TABLE `site`;
SHOW CREATE TABLE `site_domain`;
SHOW CREATE TABLE `site_config`;
SHOW CREATE TABLE `admin_user_site`;

-- Expected: 0. Existing statuses and site data must match the pre-migration backup.
SELECT COUNT(*) AS invalid_initial_auth_version_count
FROM `admin_user` WHERE `auth_version` IS NULL OR `auth_version` <> 0;

-- Next: run event_system_v2_admin_site_backfill.sql in the SAME selected database.
