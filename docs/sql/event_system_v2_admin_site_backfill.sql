-- EEMS V2 batch 1: standalone historical EDITOR grant backfill.
-- Run ONLY after the schema migration, while application writes are stopped.
-- Select the database in the client; no numeric site ID is hard-coded.
-- Verify this first result is exactly 1 BEFORE executing the INSERT below.
SELECT COUNT(*) AS default_site_count FROM `site` WHERE `code` = 'default';

-- One atomic DML statement. Repeating during the migration adds no duplicates
-- and preserves timestamps on existing bindings. Includes disabled EDITORs;
-- excludes SUPER_ADMIN and soft-deleted accounts. No INSERT IGNORE: FK errors fail.
INSERT INTO `admin_user_site` (`admin_user_id`, `site_id`)
SELECT u.`id`, s.`id`
FROM `admin_user` u
JOIN `site` s ON s.`code` = 'default'
WHERE u.`role_code` = 'EDITOR'
  AND u.`deleted` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `admin_user_site` aus
      WHERE aus.`admin_user_id` = u.`id` AND aus.`site_id` = s.`id`
  );

-- Expected: 0. Compare total grants with preflight counts and existing bindings.
SELECT COUNT(*) AS editors_missing_default_grant
FROM `admin_user` u
JOIN `site` s ON s.`code` = 'default'
WHERE u.`role_code` = 'EDITOR' AND u.`deleted` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `admin_user_site` aus
      WHERE aus.`admin_user_id` = u.`id` AND aus.`site_id` = s.`id`
  );

-- Do NOT rerun after live authorization management starts: this would restore
-- deliberately revoked default-site grants for historical EDITOR accounts.
