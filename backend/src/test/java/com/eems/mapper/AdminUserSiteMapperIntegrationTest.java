package com.eems.mapper;

import com.eems.entity.AdminUser;
import com.eems.entity.AdminUserSite;
import com.eems.site.SiteContextTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AdminUserSiteMapperIntegrationTest {
    private static final long SITE_A = 91001L;
    private static final long SITE_B = 91002L;

    @Autowired private AdminUserMapper adminMapper;
    @Autowired private AdminUserSiteMapper bindingMapper;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void createSitesWithoutContext() {
        SiteContextTestSupport.clear();
        jdbc.update("INSERT INTO site (id, name, code) VALUES (?, 'Grant site A', 'grant-a')", SITE_A);
        jdbc.update("INSERT INTO site (id, name, code) VALUES (?, 'Grant site B', 'grant-b')", SITE_B);
    }

    @AfterEach
    void clearSiteContext() {
        SiteContextTestSupport.clear();
    }

    @Test
    void javaAndDatabaseDefaultAuthVersionsAreZero() {
        assertEquals(0L, new AdminUser().getAuthVersion());
        AdminUser user = createAdmin("mapper-default", "EDITOR", "ENABLED", 0);
        assertEquals(0L, adminMapper.selectById(user.getId()).getAuthVersion());

        jdbc.update("INSERT INTO admin_user (username, password_hash, nickname, role_code) "
                + "VALUES ('database-default', 'test-placeholder', 'Test', 'EDITOR')");
        assertEquals(0L, jdbc.queryForObject(
                "SELECT auth_version FROM admin_user WHERE username = 'database-default'", Long.class));
    }

    @Test
    void authVersionCanBeUpdatedWithoutChangingCredentials() {
        AdminUser user = createAdmin("version-update", "EDITOR", "ENABLED", 0);
        user.setAuthVersion(7L);
        assertEquals(1, adminMapper.updateById(user));
        AdminUser loaded = adminMapper.selectById(user.getId());
        assertEquals(7L, loaded.getAuthVersion());
        assertEquals(user.getPasswordHash(), loaded.getPasswordHash());
        assertEquals(user.getRoleCode(), loaded.getRoleCode());
    }

    @Test
    void bindingsCanBeInsertedReadAndRevokedWithoutSiteContext() {
        Long userId = createAdmin("no-context", "EDITOR", "ENABLED", 0).getId();
        assertEquals(1, bindingMapper.insertBinding(userId, SITE_A));
        assertTrue(bindingMapper.existsBinding(userId, SITE_A));
        AdminUserSite loaded = bindingMapper.selectBinding(userId, SITE_A);
        assertEquals(userId, loaded.getAdminUserId());
        assertEquals(SITE_A, loaded.getSiteId());
        assertNotNull(loaded.getCreatedAt());
        assertEquals(1, bindingMapper.selectByAdminUserId(userId).size());
        assertEquals(1, bindingMapper.deleteBinding(userId, SITE_A));
        assertFalse(bindingMapper.existsBinding(userId, SITE_A));
        assertNull(bindingMapper.selectBinding(userId, SITE_A));
        assertEquals(0, bindingMapper.deleteBinding(userId, SITE_A));
    }

    @Test
    void explicitTargetAndGlobalQueriesIgnoreCurrentSite() {
        Long userId = createAdmin("cross-context", "EDITOR", "ENABLED", 0).getId();
        SiteContextTestSupport.selectDefaultSite();
        bindingMapper.insertBinding(userId, SITE_A);
        bindingMapper.insertBinding(userId, SITE_B);

        assertEquals(List.of(SITE_A, SITE_B), bindingMapper.selectByAdminUserId(userId).stream()
                .map(AdminUserSite::getSiteId).toList());
        assertTrue(bindingMapper.existsBinding(userId, SITE_A));
        assertEquals(SITE_A, bindingMapper.selectBinding(userId, SITE_A).getSiteId());
        assertFalse(bindingMapper.existsBinding(userId, 1L));

        SiteContextTestSupport.selectSite(SITE_B);
        assertEquals(1, bindingMapper.deleteBinding(userId, SITE_A));
        assertFalse(bindingMapper.existsBinding(userId, SITE_A));
        assertTrue(bindingMapper.existsBinding(userId, SITE_B));
    }

    @Test
    void compositeKeyAllowsManyToManyAndRejectsDuplicatePair() {
        Long first = createAdmin("first-admin", "EDITOR", "ENABLED", 0).getId();
        Long second = createAdmin("second-admin", "EDITOR", "ENABLED", 0).getId();
        bindingMapper.insertBinding(first, SITE_A);
        bindingMapper.insertBinding(first, SITE_B);
        bindingMapper.insertBinding(second, SITE_A);
        assertThrows(DuplicateKeyException.class, () -> bindingMapper.insertBinding(first, SITE_A));
        assertEquals(2, bindingMapper.selectByAdminUserId(first).size());
        assertEquals(1, bindingMapper.selectByAdminUserId(second).size());
    }

    @Test
    void preciseRevocationPreservesOtherSitesAndAdministrators() {
        Long first = createAdmin("revoke-first", "EDITOR", "ENABLED", 0).getId();
        Long second = createAdmin("revoke-second", "EDITOR", "ENABLED", 0).getId();
        bindingMapper.insertBinding(first, SITE_A);
        bindingMapper.insertBinding(first, SITE_B);
        bindingMapper.insertBinding(second, SITE_A);

        assertEquals(1, bindingMapper.deleteBinding(first, SITE_A));
        assertTrue(bindingMapper.existsBinding(first, SITE_B));
        assertTrue(bindingMapper.existsBinding(second, SITE_A));
        assertEquals(0, bindingMapper.deleteBinding(second, SITE_B));
    }

    @Test
    void bulkRevocationIsLimitedToExplicitAdministratorAcrossContexts() {
        Long first = createAdmin("bulk-first", "EDITOR", "ENABLED", 0).getId();
        Long second = createAdmin("bulk-second", "EDITOR", "ENABLED", 0).getId();
        bindingMapper.insertBinding(first, SITE_A);
        bindingMapper.insertBinding(first, SITE_B);
        bindingMapper.insertBinding(second, SITE_A);

        SiteContextTestSupport.selectDefaultSite();
        assertEquals(2, bindingMapper.deleteByAdminUserId(first));
        assertTrue(bindingMapper.selectByAdminUserId(first).isEmpty());
        assertTrue(bindingMapper.existsBinding(second, SITE_A));
    }

    @Test
    void bothForeignKeysRejectMissingParentsAndRestrictPhysicalParentDeletion() {
        Long userId = createAdmin("foreign-keys", "EDITOR", "ENABLED", 0).getId();
        assertThrows(DataIntegrityViolationException.class,
                () -> bindingMapper.insertBinding(Long.MAX_VALUE, SITE_A));
        assertThrows(DataIntegrityViolationException.class,
                () -> bindingMapper.insertBinding(userId, Long.MAX_VALUE));
        bindingMapper.insertBinding(userId, SITE_A);
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("DELETE FROM admin_user WHERE id = ?", userId));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("DELETE FROM site WHERE id = ?", SITE_A));
    }

    @Test
    void shippedBackfillIncludesDisabledEditorsAndExcludesSuperAndDeletedAccounts() {
        Long enabled = createAdmin("backfill-enabled", "EDITOR", "ENABLED", 0).getId();
        Long disabled = createAdmin("backfill-disabled", "EDITOR", "DISABLED", 0).getId();
        Long superAdmin = createAdmin("backfill-super", "SUPER_ADMIN", "ENABLED", 0).getId();
        Long deleted = createAdmin("backfill-deleted", "EDITOR", "ENABLED", 1).getId();
        bindingMapper.insertBinding(enabled, SITE_A);

        executeShippedBackfill();

        assertTrue(bindingMapper.existsBinding(enabled, 1L));
        assertTrue(bindingMapper.existsBinding(disabled, 1L));
        assertTrue(bindingMapper.existsBinding(enabled, SITE_A));
        assertTrue(bindingMapper.selectByAdminUserId(superAdmin).isEmpty());
        assertTrue(bindingMapper.selectByAdminUserId(deleted).isEmpty());
        assertFalse(bindingMapper.existsBinding(disabled, SITE_A));
        assertEquals("DISABLED", adminMapper.selectById(disabled).getStatus());
    }

    @Test
    void repeatingShippedBackfillPreservesExistingGrantsAndTheirTimestamps() {
        Long userId = createAdmin("backfill-repeat", "EDITOR", "ENABLED", 0).getId();
        LocalDateTime originalTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        jdbc.update("INSERT INTO admin_user_site (admin_user_id, site_id, created_at) VALUES (?, 1, ?)",
                userId, originalTime);
        executeShippedBackfill();
        executeShippedBackfill();
        assertEquals(1, bindingMapper.selectByAdminUserId(userId).size());
        assertEquals(originalTime, bindingMapper.selectBinding(userId, 1L).getCreatedAt());
    }

    @Test
    void shippedBackfillFindsDefaultSiteByCodeInsteadOfNumericId() {
        Long userId = createAdmin("backfill-by-code", "EDITOR", "ENABLED", 0).getId();
        jdbc.update("UPDATE site SET code = 'original-default' WHERE code = 'default'");
        jdbc.update("UPDATE site SET code = 'default' WHERE id = ?", SITE_A);
        executeShippedBackfill();
        assertTrue(bindingMapper.existsBinding(userId, SITE_A));
        assertFalse(bindingMapper.existsBinding(userId, 1L));
    }

    @Test
    void siteDefaultsAreDisabledWithMaintenanceTimestampsAndValidStatusOnly() {
        assertEquals("DISABLED", jdbc.queryForObject("SELECT status FROM site WHERE id = ?", String.class, SITE_A));
        assertNotNull(jdbc.queryForObject("SELECT created_at FROM site WHERE id = ?", LocalDateTime.class, SITE_A));
        assertNotNull(jdbc.queryForObject("SELECT updated_at FROM site WHERE id = ?", LocalDateTime.class, SITE_A));
        assertEquals("ENABLED", jdbc.queryForObject("SELECT status FROM site WHERE id = 1", String.class));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE site SET status = 'UNKNOWN' WHERE id = ?", SITE_A));
    }

    @Test
    void primaryDomainGeneratedKeyAllowsAliasesAndOnePrimaryPerSite() {
        insertDomain(92001L, SITE_A, "primary-a.example", 1);
        insertDomain(92002L, SITE_A, "alias-a.example", 0);
        insertDomain(92003L, SITE_A, "alias-a2.example", 0);
        insertDomain(92004L, SITE_B, "primary-b.example", 1);
        assertEquals(SITE_A, jdbc.queryForObject(
                "SELECT primary_site_id FROM site_domain WHERE id = 92001", Long.class));
        assertNull(jdbc.queryForObject("SELECT primary_site_id FROM site_domain WHERE id = 92002", Long.class));
        assertThrows(DuplicateKeyException.class,
                () -> insertDomain(92005L, SITE_A, "second-primary.example", 1));
        assertThrows(DuplicateKeyException.class,
                () -> jdbc.update("UPDATE site_domain SET is_primary = 1 WHERE id = 92002"));
        jdbc.update("UPDATE site_domain SET is_primary = 0 WHERE id = 92001");
        assertEquals(1, jdbc.update("UPDATE site_domain SET is_primary = 1 WHERE id = 92002"));
        assertEquals(SITE_A, jdbc.queryForObject(
                "SELECT primary_site_id FROM site_domain WHERE id = 92002", Long.class));
    }

    @Test
    void domainUniquenessAndPrimaryFlagConstraintsAreEnforced() {
        insertDomain(93001L, SITE_A, "unique.example", 0);
        assertThrows(DuplicateKeyException.class,
                () -> insertDomain(93002L, SITE_B, "unique.example", 0));
        assertThrows(DataIntegrityViolationException.class,
                () -> insertDomain(93003L, SITE_A, "invalid-flag.example", 2));
    }

    @Test
    void authenticationVersionCannotBeNegativeOrNull() {
        Long userId = createAdmin("invalid-version", "EDITOR", "ENABLED", 0).getId();
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE admin_user SET auth_version = -1 WHERE id = ?", userId));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE admin_user SET auth_version = NULL WHERE id = ?", userId));
    }

    private AdminUser createAdmin(String username, String role, String status, int deleted) {
        AdminUser user = new AdminUser();
        user.setUsername(username);
        user.setPasswordHash("test-placeholder-not-used-for-login");
        user.setNickname("Test administrator");
        user.setRoleCode(role);
        user.setStatus(status);
        user.setDeleted(deleted);
        assertEquals(1, adminMapper.insert(user));
        assertNotNull(user.getId());
        return user;
    }

    private void insertDomain(long id, long siteId, String domain, int primary) {
        jdbc.update("INSERT INTO site_domain (id, site_id, domain, is_primary) VALUES (?, ?, ?, ?)",
                id, siteId, domain, primary);
    }

    private void executeShippedBackfill() {
        new ResourceDatabasePopulator(new ClassPathResource("migration/event_system_v2_admin_site_backfill.sql"))
                .execute(jdbc.getDataSource());
    }
}
