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

    @Test
    void resolvesNormalizedHostAndKeepsSitesDistinct() {
        assertEquals(1L, resolver.resolve("A.Example.:5173", false));
        assertEquals(2L, resolver.resolve("b.example", false));
    }

    @Test
    void unknownHostFallsBackOnlyInLocalOrTest() {
        assertEquals(1L, resolver.resolve("tunnel.example", false));
        environment.setActiveProfiles("prod");
        assertEquals("SITE_NOT_FOUND", assertThrows(SiteResolutionException.class,
                () -> resolver.resolve("tunnel.example", false)).getCode());
    }

    @Test
    void disabledSiteDoesNotFallBack() {
        jdbc.update("UPDATE site SET status = 'DISABLED' WHERE id = 2");
        assertEquals("SITE_DISABLED", assertThrows(SiteResolutionException.class,
                () -> resolver.resolve("b.example", false)).getCode());
    }

    @Test
    void missingOrDisabledDefaultFailsClosed() {
        jdbc.update("UPDATE site SET status = 'DISABLED' WHERE id = 1");
        assertEquals("SITE_DISABLED", assertThrows(SiteResolutionException.class,
                () -> resolver.resolve("unknown.example", false)).getCode());
        jdbc.update("DELETE FROM site WHERE id = 1");
        assertEquals("DEFAULT_SITE_MISSING", assertThrows(SiteResolutionException.class,
                () -> resolver.resolve("unknown.example", false)).getCode());
    }

    @Test
    void adminCannotSelectOtherSiteUsingHost() {
        assertEquals("SITE_ADMIN_FORBIDDEN", assertThrows(SiteResolutionException.class,
                () -> resolver.resolve("b.example", true)).getCode());
        assertEquals(1L, resolver.resolve("a.example", true));
    }

    @Test
    void rejectsMalformedHostInsteadOfFallingBack() {
        for (String host : new String[] {"https://a.example", "a.example:bad", "a.example, b.example", "a..example", ""}) {
            assertEquals("INVALID_HOST", assertThrows(SiteResolutionException.class,
                    () -> resolver.resolve(host, false)).getCode());
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
