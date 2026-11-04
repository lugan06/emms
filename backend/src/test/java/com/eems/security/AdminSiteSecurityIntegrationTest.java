package com.eems.security;

import com.eems.site.DomainSiteFilter;
import com.eems.site.SiteContext;
import com.eems.site.SiteContextFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSiteSecurityIntegrationTest {
    private static final long SITE_B = 62001L;
    private static final long SUPER = 63001L;
    private static final long EDITOR = 63002L;
    private static final String PASSWORD = "batch2-old-password";
    private static final String HASH = new BCryptPasswordEncoder().encode(PASSWORD);
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper json;
    @Autowired private JwtTokenUtil tokens;
    @Autowired private JwtProperties properties;
    @Autowired private FilterChainProxy securityChain;
    @Autowired @Qualifier("domainSiteFilter") private FilterRegistrationBean<DomainSiteFilter> domainRegistration;
    @Autowired @Qualifier("disableSiteContextServletRegistration")
    private FilterRegistrationBean<SiteContextFilter> siteRegistration;
    @Autowired @Qualifier("disableJwtServletRegistration")
    private FilterRegistrationBean<JwtAuthenticationFilter> jwtRegistration;

    @BeforeEach
    void setup() {
        jdbc.update("INSERT INTO site (id, name, code, status) VALUES (?, 'Batch2 B', 'batch2-b', 'ENABLED')", SITE_B);
        jdbc.update("INSERT INTO site_domain (id, site_id, domain, is_primary) VALUES (62001, ?, 'batch2-b.example', 1)", SITE_B);
        jdbc.update("INSERT INTO site_config (id, site_id, config_code, site_title) VALUES (64001, 1, 'default', 'Batch2 A')");
        jdbc.update("INSERT INTO site_config (id, site_id, config_code, site_title) VALUES (64002, ?, 'default', 'Batch2 B')", SITE_B);
        createUser(SUPER, "batch2-super", "SUPER_ADMIN", "ENABLED", 0);
        createUser(EDITOR, "batch2-editor", "EDITOR", "ENABLED", 0);
        createUser(63003L, "batch2-disabled", "EDITOR", "DISABLED", 0);
        createUser(63004L, "batch2-unknown", "UNKNOWN", "ENABLED", 0);
        createUser(63005L, "batch2-deleted", "SUPER_ADMIN", "ENABLED", 1);
        jdbc.update("INSERT INTO admin_user_site (admin_user_id, site_id) VALUES (?, 1), (?, ?)", EDITOR, EDITOR, SITE_B);
    }

    @AfterEach
    void cleanup() {
        assertNull(SiteContext.currentSiteIdOrNull(), "The outer filter must always clear the site context");
        jdbc.update("DELETE FROM operation_log WHERE module = 'AUTH' OR username LIKE 'batch2-%'");
        jdbc.update("DELETE FROM admin_user_site WHERE admin_user_id BETWEEN 63001 AND 63005");
        jdbc.update("DELETE FROM admin_user WHERE id BETWEEN 63001 AND 63005");
        jdbc.update("DELETE FROM site_config WHERE id IN (64001, 64002)");
        jdbc.update("DELETE FROM site_domain WHERE id = 62001");
        jdbc.update("DELETE FROM site WHERE id = ?", SITE_B);
    }

    @Test
    void filtersHaveOneRegistrationAndAuthenticationPrecedesOverride() {
        assertTrue(domainRegistration.isEnabled());
        assertTrue(domainRegistration.getOrder() < -100);
        assertFalse(siteRegistration.isEnabled());
        assertFalse(jwtRegistration.isEnabled());
        var filters = securityChain.getFilters("/api/admin/site");
        int jwt = -1, site = -1, authorization = -1;
        for (int i = 0; i < filters.size(); i++) {
            if (filters.get(i) instanceof JwtAuthenticationFilter) jwt = i;
            if (filters.get(i) instanceof SiteContextFilter) site = i;
            if (filters.get(i) instanceof AuthorizationFilter) authorization = i;
        }
        assertTrue(jwt >= 0 && jwt < site && site < authorization);
        assertEquals(1, filters.stream().filter(f -> f instanceof JwtAuthenticationFilter).count());
        assertEquals(1, filters.stream().filter(f -> f instanceof SiteContextFilter).count());
    }

    @Test
    void editorAndSuperCanLoginNonDefaultSiteAndReceiveScopedClaims() throws Exception {
        String editor = login("batch2-editor", PASSWORD, "batch2-b.example");
        var claims = tokens.parseClaims(editor);
        assertEquals(SITE_B, claims.get("loginSiteId", Number.class).longValue());
        assertEquals(0L, claims.get("authVersion", Number.class).longValue());
        mvc.perform(get("/api/admin/site").header("Host", "batch2-b.example").header("Authorization", "Bearer " + editor))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.siteTitle").value("Batch2 B"));
        login("batch2-super", PASSWORD, "batch2-b.example");
        assertNotNull(jdbc.queryForObject("SELECT last_login_at FROM admin_user WHERE id = ?", Date.class, EDITOR));
    }

    @Test
    void unboundEditorCannotLoginAndFailureAuditSurvivesRollback() throws Exception {
        jdbc.update("DELETE FROM admin_user_site WHERE admin_user_id = ? AND site_id = ?", EDITOR, SITE_B);
        mvc.perform(post("/api/admin/login").header("Host", "batch2-b.example")
                        .contentType(APPLICATION_JSON).content(credentials("batch2-editor", PASSWORD)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SITE_ACCESS_DENIED"))
                .andExpect(header().doesNotExist("Authorization"));
        assertNull(jdbc.queryForObject("SELECT last_login_at FROM admin_user WHERE id = ?", Date.class, EDITOR));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM operation_log WHERE username = 'batch2-editor' "
                + "AND result = 'FAILURE' AND error_message = 'SITE_ACCESS_DENIED'", Integer.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"batch2-disabled", "batch2-deleted", "no-such-user"})
    void unavailableAccountsCannotLogin(String username) throws Exception {
        mvc.perform(post("/api/admin/login").contentType(APPLICATION_JSON).content(credentials(username, PASSWORD)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void unknownRoleCannotLogin() throws Exception {
        mvc.perform(post("/api/admin/login").contentType(APPLICATION_JSON).content(credentials("batch2-unknown", PASSWORD)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SITE_ACCESS_DENIED"));
    }

    @Test
    void wrongPasswordFailureIsPersistedWithoutSecrets() throws Exception {
        mvc.perform(post("/api/admin/login").contentType(APPLICATION_JSON)
                        .content(credentials("batch2-editor", "wrong-password-private")))
                .andExpect(status().isBadRequest());
        String stored = jdbc.queryForObject("SELECT request_params FROM operation_log "
                + "WHERE username = 'batch2-editor' AND result = 'FAILURE'", String.class);
        assertEquals("{}", stored);
    }

    @Test
    void editorTokenCannotBeReplayedAtAnotherGrantedDomain() throws Exception {
        String token = login("batch2-editor", PASSWORD, "localhost");
        mvc.perform(get("/api/admin/me").header("Host", "batch2-b.example").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("TOKEN_SITE_MISMATCH"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "62001", "", "garbage", "-1"})
    void editorCannotSendOverrideEvenForCurrentSiteOrInvalidValue(String value) throws Exception {
        mvc.perform(get("/api/admin/site").header("Authorization", bearer(EDITOR, "batch2-editor", "EDITOR", 1L, 0L))
                        .header("X-Site-Id", value))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SITE_OVERRIDE_FORBIDDEN"));
    }

    @Test
    void editorCannotSendOverrideOnGlobalEndpoint() throws Exception {
        mvc.perform(get("/api/admin/company").header("Authorization", bearer(EDITOR, "batch2-editor", "EDITOR", 1L, 0L))
                        .header("X-Site-Id", "1"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SITE_OVERRIDE_FORBIDDEN"));
    }

    @Test
    void revokingGrantTakesEffectOnNextRequest() throws Exception {
        String token = bearer(EDITOR, "batch2-editor", "EDITOR", 1L, 0L);
        mvc.perform(get("/api/admin/me").header("Authorization", token)).andExpect(status().isOk());
        jdbc.update("DELETE FROM admin_user_site WHERE admin_user_id = ? AND site_id = 1", EDITOR);
        mvc.perform(get("/api/admin/me").header("Authorization", token)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SITE_ACCESS_DENIED"));
    }

    @Test
    void superOverrideControlsRealTenantQueriesAndDoesNotLeakToNextRequest() throws Exception {
        String token = bearer(SUPER, "batch2-super", "SUPER_ADMIN", 1L, 0L);
        mvc.perform(get("/api/admin/site").header("Authorization", token).header("X-Site-Id", SITE_B))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.siteTitle").value("Batch2 B"));
        assertNull(SiteContext.currentSiteIdOrNull());
        mvc.perform(get("/api/admin/site").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.siteTitle").value("Batch2 A"));
    }

    @Test
    void superOverrideWritesTargetSiteAndAuditIgnoresForgedBodySiteId() throws Exception {
        mvc.perform(put("/api/admin/site").header("Authorization", bearer(SUPER, "batch2-super", "SUPER_ADMIN", 1L, 0L))
                        .header("X-Site-Id", SITE_B).contentType(APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("siteTitle", "Updated B", "siteId", 1))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.siteTitle").value("Updated B"));
        assertEquals("Updated B", jdbc.queryForObject("SELECT site_title FROM site_config WHERE id = 64002", String.class));
        assertEquals("Batch2 A", jdbc.queryForObject("SELECT site_title FROM site_config WHERE id = 64001", String.class));
        assertEquals(SITE_B, jdbc.queryForObject("SELECT site_id FROM operation_log "
                + "WHERE username = 'batch2-super' AND module = 'SITE_CONFIG'", Long.class));
    }

    @Test
    void superCanMaintainDisabledSiteWhileLoginAndPublicAccessAreDenied() throws Exception {
        jdbc.update("UPDATE site SET status = 'DISABLED' WHERE id = ?", SITE_B);
        String token = bearer(SUPER, "batch2-super", "SUPER_ADMIN", 1L, 0L);
        mvc.perform(get("/api/admin/site").header("Authorization", token).header("X-Site-Id", SITE_B))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.siteTitle").value("Batch2 B"));
        mvc.perform(get("/api/admin/site").header("Host", "batch2-b.example").header("Authorization", token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/public/site").header("Host", "batch2-b.example"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SITE_DISABLED"));
        mvc.perform(post("/api/admin/login").header("Host", "batch2-b.example")
                        .contentType(APPLICATION_JSON).content(credentials("batch2-super", PASSWORD)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SITE_DISABLED"));
        mvc.perform(get("/api/admin/me").header("Host", "batch2-b.example")
                        .header("Authorization", bearer(EDITOR, "batch2-editor", "EDITOR", SITE_B, 0L)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SITE_DISABLED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "0", "-1", "abc", "1.5", "1,2", " 1", "1 ", "9223372036854775808", "999999999999999999999999999"})
    void superOverrideRejectsMalformedHeader(String value) throws Exception {
        mvc.perform(get("/api/admin/site").header("Authorization", bearer(SUPER, "batch2-super", "SUPER_ADMIN", 1L, 0L))
                        .header("X-Site-Id", value))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_SITE_ID"));
    }

    @Test
    void repeatedHeaderAndNonexistentTargetAreRejected() throws Exception {
        String token = bearer(SUPER, "batch2-super", "SUPER_ADMIN", 1L, 0L);
        mvc.perform(get("/api/admin/site").header("Authorization", token).header("X-Site-Id", "1", "62001"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/site").header("Authorization", token).header("X-Site-Id", "99999"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SITE_NOT_FOUND"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/admin/company", "/api/admin/logs", "/api/admin/password", "/api/admin/sites", "/api/admin/users", "/api/admin/unknown"})
    void superOverrideIsNotAllowedOnGlobalOrUndeclaredResources(String path) throws Exception {
        mvc.perform(get(path).header("Authorization", bearer(SUPER, "batch2-super", "SUPER_ADMIN", 1L, 0L))
                        .header("X-Site-Id", "1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SITE_OVERRIDE_NOT_ALLOWED"));
    }

    @Test
    void loginAndPublicRequestsRejectOverrideWithoutChangingContext() throws Exception {
        mvc.perform(post("/api/admin/login").header("X-Site-Id", SITE_B).contentType(APPLICATION_JSON)
                        .content(credentials("batch2-super", PASSWORD)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/public/site").header("X-Site-Id", SITE_B)).andExpect(status().isBadRequest());
    }

    @Test
    void authenticationMustSucceedBeforeAnyOverrideIsConsidered() throws Exception {
        mvc.perform(get("/api/admin/site").header("X-Site-Id", SITE_B)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/site").header("Authorization", "Bearer invalid").header("X-Site-Id", SITE_B))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordChangeIncrementsVersionAndInvalidatesAllOldTokens() throws Exception {
        String first = login("batch2-editor", PASSWORD, "localhost");
        String secondSite = login("batch2-editor", PASSWORD, "batch2-b.example");
        mvc.perform(put("/api/admin/password").header("Authorization", "Bearer " + first).contentType(APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("oldPassword", PASSWORD, "newPassword", "batch2-new-password"))))
                .andExpect(status().isOk());
        assertEquals(1L, jdbc.queryForObject("SELECT auth_version FROM admin_user WHERE id = ?", Long.class, EDITOR));
        mvc.perform(get("/api/admin/me").header("Authorization", "Bearer " + first)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/me").header("Host", "batch2-b.example").header("Authorization", "Bearer " + secondSite))
                .andExpect(status().isUnauthorized());
        String fresh = login("batch2-editor", "batch2-new-password", "localhost");
        assertEquals(1L, tokens.parseClaims(fresh).get("authVersion", Number.class).longValue());
        mvc.perform(get("/api/admin/me").header("Authorization", "Bearer " + fresh)).andExpect(status().isOk());
    }

    @Test
    void wrongOldPasswordDoesNotChangeVersionOrInvalidateToken() throws Exception {
        String token = bearer(EDITOR, "batch2-editor", "EDITOR", 1L, 0L);
        mvc.perform(put("/api/admin/password").header("Authorization", token).contentType(APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("oldPassword", "wrong", "newPassword", "new-password"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PASSWORD"));
        assertEquals(0L, jdbc.queryForObject("SELECT auth_version FROM admin_user WHERE id = ?", Long.class, EDITOR));
        mvc.perform(get("/api/admin/me").header("Authorization", token)).andExpect(status().isOk());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM operation_log WHERE username = 'batch2-editor' "
                + "AND error_message = 'INVALID_PASSWORD'", Integer.class));
    }

    @Test
    void databaseDisableDeleteAndVersionChangeImmediatelyInvalidateTokens() throws Exception {
        String token = bearer(EDITOR, "batch2-editor", "EDITOR", 1L, 0L);
        jdbc.update("UPDATE admin_user SET status = 'DISABLED' WHERE id = ?", EDITOR);
        mvc.perform(get("/api/admin/me").header("Authorization", token)).andExpect(status().isUnauthorized());
        jdbc.update("UPDATE admin_user SET status = 'ENABLED', auth_version = auth_version + 1 WHERE id = ?", EDITOR);
        mvc.perform(get("/api/admin/me").header("Authorization", token)).andExpect(status().isUnauthorized());
        String fresh = bearer(EDITOR, "batch2-editor", "EDITOR", 1L, 1L);
        jdbc.update("UPDATE admin_user SET deleted = 1 WHERE id = ?", EDITOR);
        mvc.perform(get("/api/admin/me").header("Authorization", fresh)).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenRoleCannotOverrideCurrentDatabaseRole() throws Exception {
        String token = bearer(SUPER, "batch2-super", "SUPER_ADMIN", 1L, 0L);
        jdbc.update("UPDATE admin_user SET role_code = 'EDITOR' WHERE id = ?", SUPER);
        mvc.perform(get("/api/admin/site").header("Authorization", token).header("X-Site-Id", SITE_B))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SITE_OVERRIDE_FORBIDDEN"));
        jdbc.update("UPDATE admin_user SET role_code = 'UNKNOWN' WHERE id = ?", SUPER);
        mvc.perform(get("/api/admin/me").header("Authorization", token)).andExpect(status().isUnauthorized());
    }

    @Test
    void oldExpiredTamperedAndMismatchedIdentityTokensAreRejected() throws Exception {
        String legacy = Jwts.builder().subject("batch2-super").claim("userId", SUPER).claim("roleCode", "SUPER_ADMIN")
                .expiration(Date.from(Instant.now().plusSeconds(60))).signWith(Keys.hmacShaKeyFor(
                        properties.getSecret().getBytes(StandardCharsets.UTF_8))).compact();
        JwtProperties expiredProperties = new JwtProperties();
        expiredProperties.setSecret(properties.getSecret());
        expiredProperties.setAccessTokenTtl(-60);
        String expired = new JwtTokenUtil(expiredProperties).generateToken(SUPER, "batch2-super", "SUPER_ADMIN", 1L, 0L);
        String valid = bearer(SUPER, "batch2-super", "SUPER_ADMIN", 1L, 0L).substring(7);
        for (String token : new String[] {legacy, expired, valid + "tampered", "",
                tokens.generateToken(SUPER, "another-user", "SUPER_ADMIN", 1L, 0L),
                tokens.generateToken(99999L, "missing", "SUPER_ADMIN", 1L, 0L)}) {
            mvc.perform(get("/api/admin/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void publicAccessIgnoresExpiredAdminTokenAndUntrustedForwardedHost() throws Exception {
        mvc.perform(get("/api/public/site").header("Authorization", "Bearer expired-or-invalid")
                        .header("X-Forwarded-Host", "batch2-b.example"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.siteTitle").value("Batch2 A"));
    }

    @Test
    void corsPreflightAllowsSiteHeaderWithoutAuthenticationOrHostResolution() throws Exception {
        mvc.perform(options("/api/admin/site").header("Host", "unbound-preflight.example")
                        .header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,x-site-id"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Headers",
                        org.hamcrest.Matchers.containsString("x-site-id")));
    }

    @Test
    void openApiDocumentsOverridesOnlyWhereAllowed() throws Exception {
        MvcResult result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        var api = json.readTree(result.getResponse().getContentAsString());
        var parameters = api.at("/paths/~1api~1admin~1site/get/parameters");
        assertTrue(parameters.toString().contains("X-Site-Id"));
        assertFalse(api.at("/paths/~1api~1admin~1password/put/parameters").toString().contains("X-Site-Id"));
        assertFalse(api.at("/paths/~1api~1admin~1login/post/parameters").toString().contains("X-Site-Id"));
        assertTrue(api.at("/paths/~1api~1admin~1login/post/responses/403").isObject());
        assertTrue(api.at("/components/securitySchemes/bearerAuth").isObject());
    }

    @Test
    void rejectionAuditStoresContextAndNoRawCredentialsOrToken() throws Exception {
        String token = bearer(EDITOR, "batch2-editor", "EDITOR", 1L, 0L);
        mvc.perform(get("/api/admin/site").header("Authorization", token).header("X-Site-Id", "private-header-value"))
                .andExpect(status().isForbidden());
        String params = jdbc.queryForObject("SELECT request_params FROM operation_log "
                + "WHERE username = 'batch2-editor' AND error_message = 'SITE_OVERRIDE_FORBIDDEN'", String.class);
        assertTrue(params.contains("siteId"));
        assertFalse(params.contains(token));
        assertFalse(params.contains("private-header-value"));
        assertFalse(params.contains(PASSWORD));
    }

    private void createUser(long id, String username, String role, String status, int deleted) {
        jdbc.update("INSERT INTO admin_user (id, username, password_hash, nickname, role_code, status, deleted) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)", id, username, HASH, username, role, status, deleted);
    }

    private String credentials(String username, String password) throws Exception {
        return json.writeValueAsString(Map.of("username", username, "password", password));
    }

    private String login(String username, String password, String host) throws Exception {
        MvcResult result = mvc.perform(post("/api/admin/login").header("Host", host).contentType(APPLICATION_JSON)
                        .content(credentials(username, password)))
                .andExpect(status().isOk()).andReturn();
        String token = json.readTree(result.getResponse().getContentAsString()).at("/data/token").asText();
        assertEquals("Bearer " + token, result.getResponse().getHeader("Authorization"));
        return token;
    }

    private String bearer(long userId, String username, String role, long siteId, long version) {
        return "Bearer " + tokens.generateToken(userId, username, role, siteId, version);
    }
}
