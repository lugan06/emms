-- EEMS V2 batch 1: READ-ONLY preflight, MySQL >= 8.0.16.
-- Select the intended database in the client. Do not connect the application to it.
-- Run before event_system_v2_admin_site_schema.sql. Follow ../migration-v2-batch1.md.

SELECT DATABASE() AS selected_database, VERSION() AS mysql_version,
       @@autocommit AS client_autocommit;

-- Inspect the existing baseline: unsigned BIGINT IDs, InnoDB, matching foreign
-- key column types, uk_site_config_site(site_id), and uk_site_domain_domain(domain).
SHOW CREATE TABLE `admin_user`;
SHOW CREATE TABLE `site`;
SHOW CREATE TABLE `site_domain`;
SHOW CREATE TABLE `site_config`;

-- Expected: 1. The default site's numeric ID is deliberately not hard-coded.
SELECT COUNT(*) AS enabled_default_site_count
FROM `site` WHERE `code` = 'default' AND `status` = 'ENABLED';

-- Every following data result must be 0. Stop and investigate otherwise.
SELECT COUNT(*) AS invalid_site_status_count
FROM `site` WHERE `status` NOT IN ('ENABLED', 'DISABLED') OR `status` IS NULL;

SELECT COUNT(*) AS invalid_primary_flag_count
FROM `site_domain` WHERE `is_primary` NOT IN (0, 1) OR `is_primary` IS NULL;

SELECT COUNT(*) AS sites_with_multiple_primary_domains
FROM (
    SELECT `site_id` FROM `site_domain`
    WHERE `is_primary` = 1 GROUP BY `site_id` HAVING COUNT(*) > 1
) AS `invalid_primary_sites`;

SELECT COUNT(*) AS sites_without_one_active_config
FROM (
    SELECT s.`id` FROM `site` s
    LEFT JOIN `site_config` c ON c.`site_id` = s.`id` AND c.`deleted` = 0
    GROUP BY s.`id` HAVING COUNT(c.`id`) <> 1
) AS `invalid_config_sites`;

SELECT COUNT(*) AS unknown_active_admin_role_count
FROM `admin_user`
WHERE `deleted` = 0 AND `role_code` NOT IN ('SUPER_ADMIN', 'EDITOR');

-- All three results must be 0 on a fresh, unmodified batch-1 baseline.
-- A nonzero result can mean a partial/previous migration: do not blindly rerun DDL.
SELECT COUNT(*) AS existing_binding_table_count
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'admin_user_site';

SELECT COUNT(*) AS existing_batch1_column_count
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND ((table_name = 'admin_user' AND column_name = 'auth_version')
    OR (table_name = 'site' AND column_name IN ('created_at', 'updated_at'))
    OR (table_name = 'site_domain' AND column_name = 'primary_site_id'));

SELECT COUNT(*) AS existing_batch1_constraint_count
FROM information_schema.table_constraints
WHERE constraint_schema = DATABASE()
  AND constraint_name IN ('chk_admin_user_auth_version', 'chk_site_status',
                          'chk_site_domain_is_primary', 'uk_site_domain_primary_site');

-- Informational only: zero primary domains is allowed in this infrastructure
-- batch (legacy local-only sites may have aliases only). Future management
-- services will enforce exactly one primary domain on creation/domain edits.
SELECT s.`id`, s.`code`
FROM `site` s LEFT JOIN `site_domain` d ON d.`site_id` = s.`id` AND d.`is_primary` = 1
WHERE d.`id` IS NULL;

SELECT `role_code`, `status`, COUNT(*) AS editor_backfill_count
FROM `admin_user` WHERE `deleted` = 0 AND `role_code` = 'EDITOR'
GROUP BY `role_code`, `status`;
