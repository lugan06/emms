package com.eems.site;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SiteResolverTest {
    private JdbcTemplate jdbc;
    private MockEnvironment environment;
    private SiteResolver resolver;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:site_resolver;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP TABLE IF EXISTS site_domain");
        jdbc.execute("DROP TABLE IF EXISTS site");
        jdbc.execute("CREATE TABLE site (id BIGINT PRIMARY KEY, code VARCHAR(50), status VARCHAR(20))");
        jdbc.execute("CREATE TABLE site_domain (id BIGINT PRIMARY KEY, site_id BIGINT, domain VARCHAR(253))");
        jdbc.update("INSERT INTO site VALUES (1, 'default', 'ENABLED'), (2, 'second', 'ENABLED')");
        jdbc.update("INSERT INTO site_domain VALUES (1, 1, 'a.example'), (2, 2, 'b.example')");
        environment = new MockEnvironment();
        environment.setActiveProfiles("test");
        resolver = new SiteResolver(jdbc, environment);
    }

    private Long resolveEnabled(String host) {
        var site = resolver.resolveHost(host);
        SiteResolver.requireEnabled(site);
        return site.id();
    }

    @Test
    void resolvesNormalizedHostAndKeepsSitesDistinct() {
        assertEquals(1L, resolveEnabled("A.Example.:5173"));
        assertEquals(2L, resolveEnabled("b.example"));
    }

    @Test
    void unknownHostFallsBackOnlyInLocalOrTest() {
        assertEquals(1L, resolveEnabled("tunnel.example"));
        environment.setActiveProfiles("prod");
        assertEquals("SITE_NOT_FOUND", assertThrows(SiteResolutionException.class,
                () -> resolveEnabled("tunnel.example")).getCode());
    }

    @Test
    void disabledSiteDoesNotFallBack() {
        jdbc.update("UPDATE site SET status = 'DISABLED' WHERE id = 2");
        assertEquals("SITE_DISABLED", assertThrows(SiteResolutionException.class,
                () -> resolveEnabled("b.example")).getCode());
    }

    @Test
    void missingOrDisabledDefaultFailsClosed() {
        jdbc.update("UPDATE site SET status = 'DISABLED' WHERE id = 1");
        assertEquals("SITE_DISABLED", assertThrows(SiteResolutionException.class,
                () -> resolveEnabled("unknown.example")).getCode());
        jdbc.update("DELETE FROM site WHERE id = 1");
        assertEquals("DEFAULT_SITE_MISSING", assertThrows(SiteResolutionException.class,
                () -> resolveEnabled("unknown.example")).getCode());
    }

    @Test
    void adminCanResolveNonDefaultAndDisabledSitesBeforeIdentityChecks() {
        assertEquals(2L, resolver.resolveHost("b.example").id());
        jdbc.update("UPDATE site SET status = 'DISABLED' WHERE id = 2");
        assertEquals("DISABLED", resolver.resolveHost("b.example").status());
        assertEquals(2L, resolver.requireSite(2L).id());
        assertEquals("SITE_NOT_FOUND", assertThrows(SiteResolutionException.class,
                () -> resolver.requireSite(99L)).getCode());
    }

    @Test
    void rejectsMalformedHostInsteadOfFallingBack() {
        for (String host : new String[] {"https://a.example", "a.example:bad", "a.example, b.example", "a..example", ""}) {
            assertEquals("INVALID_HOST", assertThrows(SiteResolutionException.class,
                    () -> resolveEnabled(host)).getCode());
        }
    }

    @Test
    void contextRequiresValueAndCanBeCleared() {
        assertThrows(IllegalStateException.class, SiteContext::requireSiteId);
        SiteContext.setSiteId(2L);
        assertEquals(2L, SiteContext.requireSiteId());
        SiteContext.clear();
        assertThrows(IllegalStateException.class, SiteContext::requireSiteId);
    }
}
