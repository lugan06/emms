package com.eems.controller;

import com.eems.security.JwtTokenUtil;
import com.eems.service.impl.OperationLogServiceImpl;
import com.eems.site.SiteContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserManagementIntegrationTest {
    private static final long A = 84001L, B = 84002L, SUPER = 85001L;
    private static final String PASSWORD = "batch4-initial-password";
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtTokenUtil tokens;
    @Autowired private ObjectMapper json;
    @Autowired private PasswordEncoder passwords;
    @MockitoSpyBean private OperationLogServiceImpl logs;
    private String superToken;

    @BeforeEach
    void setup() {
        jdbc.update("INSERT INTO site (id, name, code, status) VALUES (?, 'Batch4 A', 'batch4-a', 'ENABLED'), (?, 'Batch4 B', 'batch4-b', 'ENABLED')", A, B);
        jdbc.update("INSERT INTO site_domain (site_id, domain, is_primary) VALUES (?, 'batch4-a.example', 1), (?, 'batch4-b.example', 1)", A, B);
        jdbc.update("INSERT INTO admin_user (id, username, password_hash, nickname, role_code) VALUES (?, 'batch4-super', 'unchanged-super-hash', 'Super', 'SUPER_ADMIN')", SUPER);
        jdbc.update("INSERT INTO admin_user (id, username, password_hash, nickname, role_code, deleted) VALUES (85002, 'batch4-deleted', 'unused', 'Deleted', 'EDITOR', 1), (85003, 'batch4-unknown', 'unused', 'Unknown', 'UNKNOWN', 0)");
        superToken = tokens.generateToken(SUPER, "batch4-super", "SUPER_ADMIN", 1L, 0L);
    }

    @AfterEach
    void cleanup() {
        assertNull(SiteContext.currentSiteIdOrNull());
        jdbc.update("DELETE FROM operation_log WHERE module IN ('ADMIN_USER', 'AUTH') OR username LIKE 'batch4-%'");
        jdbc.update("DELETE FROM admin_user_site WHERE admin_user_id IN (SELECT id FROM admin_user WHERE username LIKE 'batch4-%')");
        jdbc.update("DELETE FROM admin_user WHERE username LIKE 'batch4-%'");
        jdbc.update("DELETE FROM site_domain WHERE site_id IN (?, ?)", A, B);
        jdbc.update("DELETE FROM site WHERE id IN (?, ?)", A, B);
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request) {
        return request.header("Host", "localhost").header("Authorization", "Bearer " + superToken);
    }

    private String body(Object value) throws Exception { return json.writeValueAsString(value); }

    private JsonNode create(String username, List<Long> ids) throws Exception {
        String result = mvc.perform(auth(post("/api/admin/users")).contentType(APPLICATION_JSON).content(body(Map.of(
                "username", username, "password", PASSWORD, "nickname", " Batch4 Editor ", "siteIds", ids,
                "roleCode", "SUPER_ADMIN", "status", "DISABLED", "authVersion", 999))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("ENABLED"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.authVersion").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(result).get("data");
    }

    private String login(String username, String password, String host) throws Exception {
        var response = mvc.perform(post("/api/admin/login").header("Host", host).contentType(APPLICATION_JSON)
                .content(body(Map.of("username", username, "password", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).at("/data/token").asText();
    }

    private void me(String token, String host, int expected) throws Exception {
        mvc.perform(get("/api/admin/me").header("Host", host).header("Authorization", "Bearer " + token))
                .andExpect(status().is(expected));
    }

    private long version(long id) {
        return jdbc.queryForObject("SELECT auth_version FROM admin_user WHERE id = ?", Long.class, id);
    }

    @Test
    void createForcesEditorAndGrantARejectsLoginAtB() throws Exception {
        JsonNode user = create("batch4-editor", List.of(A));
        long id = user.get("id").asLong();
        assertEquals("EDITOR", jdbc.queryForObject("SELECT role_code FROM admin_user WHERE id = ?", String.class, id));
        String hash = jdbc.queryForObject("SELECT password_hash FROM admin_user WHERE id = ?", String.class, id);
        assertNotEquals(PASSWORD, hash); assertTrue(passwords.matches(PASSWORD, hash));
        assertEquals("Batch4 Editor", user.get("nickname").asText());
        assertEquals(A, user.at("/siteIds/0").asLong());
        String token = login("batch4-editor", PASSWORD, "batch4-a.example");
        me(token, "batch4-a.example", 200);
        mvc.perform(post("/api/admin/login").header("Host", "batch4-b.example").contentType(APPLICATION_JSON)
                .content(body(Map.of("username", "batch4-editor", "password", PASSWORD))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SITE_ACCESS_DENIED"));
        me(token, "batch4-b.example", 403);
        assertEquals(0L, version(id));
    }

    @Test
    void replacingOrClearingGrantsIsAtomicAndInvalidatesOldTokens() throws Exception {
        long id = create("batch4-grants", List.of(A)).get("id").asLong();
        String old = login("batch4-grants", PASSWORD, "batch4-a.example");
        mvc.perform(auth(put("/api/admin/users/" + id + "/sites")).contentType(APPLICATION_JSON)
                .content(body(Map.of("siteIds", List.of(B)))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.siteIds[0]").value(B));
        assertEquals(1L, version(id));
        me(old, "batch4-a.example", 401);
        mvc.perform(post("/api/admin/login").header("Host", "batch4-a.example").contentType(APPLICATION_JSON)
                .content(body(Map.of("username", "batch4-grants", "password", PASSWORD))))
                .andExpect(status().isForbidden());
        String fresh = login("batch4-grants", PASSWORD, "batch4-b.example");
        mvc.perform(auth(put("/api/admin/users/" + id + "/sites")).contentType(APPLICATION_JSON)
                .content("{\"siteIds\":[]}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sites").isEmpty());
        me(fresh, "batch4-b.example", 401);
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM admin_user_site WHERE admin_user_id = ?", Integer.class, id));
    }

    @Test
    void disableAndReenableCannotReviveAnyPriorToken() throws Exception {
        long id = create("batch4-status", List.of(A, B)).get("id").asLong();
        String tokenA = login("batch4-status", PASSWORD, "batch4-a.example");
        String tokenB = login("batch4-status", PASSWORD, "batch4-b.example");
        mvc.perform(auth(put("/api/admin/users/" + id + "/status")).contentType(APPLICATION_JSON)
                .content("{\"status\":\"DISABLED\"}")).andExpect(status().isOk());
        assertEquals(1L, version(id));
        me(tokenA, "batch4-a.example", 401); me(tokenB, "batch4-b.example", 401);
        mvc.perform(post("/api/admin/login").header("Host", "batch4-a.example").contentType(APPLICATION_JSON)
                .content(body(Map.of("username", "batch4-status", "password", PASSWORD))))
                .andExpect(status().isBadRequest());
        mvc.perform(auth(put("/api/admin/users/" + id + "/status")).contentType(APPLICATION_JSON)
                .content("{\"status\":\"ENABLED\"}")).andExpect(status().isOk());
        assertEquals(2L, version(id));
        me(tokenA, "batch4-a.example", 401);
        me(login("batch4-status", PASSWORD, "batch4-a.example"), "batch4-a.example", 200);
    }

    @Test
    void resetPasswordInvalidatesTokensAcrossAllSitesAndNeverAuditsSecrets() throws Exception {
        long id = create("batch4-password", List.of(A, B)).get("id").asLong();
        String a = login("batch4-password", PASSWORD, "batch4-a.example");
        String b = login("batch4-password", PASSWORD, "batch4-b.example");
        String reset = "batch4-reset-secret";
        mvc.perform(auth(put("/api/admin/users/" + id + "/password")).contentType(APPLICATION_JSON)
                .content(body(Map.of("password", reset)))).andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
        assertEquals(1L, version(id));
        me(a, "batch4-a.example", 401); me(b, "batch4-b.example", 401);
        mvc.perform(post("/api/admin/login").header("Host", "batch4-a.example").contentType(APPLICATION_JSON)
                .content(body(Map.of("username", "batch4-password", "password", PASSWORD))))
                .andExpect(status().isBadRequest());
        me(login("batch4-password", reset, "batch4-a.example"), "batch4-a.example", 200);
        List<String> audit = jdbc.queryForList("SELECT request_params FROM operation_log WHERE module = 'ADMIN_USER'", String.class);
        assertFalse(audit.isEmpty());
        for (String entry : audit) { assertFalse(entry.contains(PASSWORD)); assertFalse(entry.contains(reset)); assertFalse(entry.contains("$2")); }
    }

    @ParameterizedTest
    @ValueSource(longs = {85001, 85002, 85003, 999999})
    void noManagementMutationCanTargetSuperDeletedOrUnknownAccounts(long id) throws Exception {
        for (var request : List.of(
                put("/api/admin/users/" + id).content("{\"nickname\":\"Hacked\"}"),
                put("/api/admin/users/" + id + "/sites").content(body(Map.of("siteIds", List.of(A)))),
                put("/api/admin/users/" + id + "/status").content("{\"status\":\"DISABLED\"}"),
                put("/api/admin/users/" + id + "/password").content("{\"password\":\"attempt-password\"}"))) {
            mvc.perform(auth(request).contentType(APPLICATION_JSON)).andExpect(status().isNotFound());
        }
        assertEquals("Super", jdbc.queryForObject("SELECT nickname FROM admin_user WHERE id = ?", String.class, SUPER));
        assertEquals("unchanged-super-hash", jdbc.queryForObject("SELECT password_hash FROM admin_user WHERE id = ?", String.class, SUPER));
        assertEquals(0L, version(SUPER));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM admin_user_site WHERE admin_user_id = ?", Integer.class, SUPER));
    }

    @Test
    void ordinaryAdminsCannotCallAnyManagementEndpointOrOptions() throws Exception {
        long id = create("batch4-denied", List.of(A)).get("id").asLong();
        String token = login("batch4-denied", PASSWORD, "batch4-a.example");
        for (var request : List.of(get("/api/admin/users"), get("/api/admin/sites/options"),
                post("/api/admin/users").content(body(Map.of("username", "batch4-nope", "password", PASSWORD, "nickname", "Nope", "siteIds", List.of(A)))),
                put("/api/admin/users/" + id).content("{\"nickname\":\"Nope\"}"),
                put("/api/admin/users/" + id + "/sites").content(body(Map.of("siteIds", List.of(B)))),
                put("/api/admin/users/" + id + "/status").content("{\"status\":\"DISABLED\"}"),
                put("/api/admin/users/" + id + "/password").content("{\"password\":\"attempt-password\"}"))) {
            mvc.perform(request.header("Host", "batch4-a.example").header("Authorization", "Bearer " + token)
                    .contentType(APPLICATION_JSON)).andExpect(status().isForbidden());
        }
        mvc.perform(get("/api/admin/users").header("Host", "localhost")).andExpect(status().isUnauthorized());
        mvc.perform(auth(get("/api/admin/users")).header("X-Site-Id", A)).andExpect(status().isBadRequest());
        mvc.perform(auth(get("/api/admin/sites/options")).header("X-Site-Id", A)).andExpect(status().isBadRequest());
    }

    @Test
    void nicknameUpdateCannotEscalateRoleOrChangeOtherFieldsAndListSupportsFilters() throws Exception {
        long id = create("batch4-search-one", List.of(A)).get("id").asLong();
        create("batch4-search-two", List.of(B));
        mvc.perform(auth(put("/api/admin/users/" + id)).contentType(APPLICATION_JSON)
                .content("{\"nickname\":\" New nickname \",\"roleCode\":\"SUPER_ADMIN\",\"username\":\"hacked\",\"authVersion\":100}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.nickname").value("New nickname"))
                .andExpect(jsonPath("$.data.username").value("batch4-search-one"));
        assertEquals(0L, version(id));
        assertEquals("EDITOR", jdbc.queryForObject("SELECT role_code FROM admin_user WHERE id = ?", String.class, id));
        mvc.perform(auth(get("/api/admin/users")).param("keyword", "NEW NICK").param("status", "ENABLED").param("siteId", Long.toString(A)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].sites[0].name").value("Batch4 A"))
                .andExpect(jsonPath("$.data.records[0].passwordHash").doesNotExist());
        mvc.perform(auth(get("/api/admin/users")).param("keyword", "batch4-search").param("pageSize", "1").param("page", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(2)).andExpect(jsonPath("$.data.records.length()").value(1));
        mvc.perform(auth(get("/api/admin/users")).param("keyword", "batch4-%")).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
        mvc.perform(auth(get("/api/admin/users")).param("keyword", "batch4-super")).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void validatesSitesAndDuplicateUsernameWithoutPartialChanges() throws Exception {
        long id = create("batch4-validate", List.of(A)).get("id").asLong();
        for (List<Long> ids : List.of(List.of(999999L), List.of(A, A), List.of(-1L))) {
            mvc.perform(auth(put("/api/admin/users/" + id + "/sites")).contentType(APPLICATION_JSON)
                    .content(body(Map.of("siteIds", ids)))).andExpect(status().isBadRequest());
            assertEquals(List.of(A), jdbc.queryForList("SELECT site_id FROM admin_user_site WHERE admin_user_id = ?", Long.class, id));
            assertEquals(0L, version(id));
        }
        mvc.perform(auth(post("/api/admin/users")).contentType(APPLICATION_JSON).content(body(Map.of(
                "username", "batch4-invalid-site", "password", PASSWORD, "nickname", "Test", "siteIds", List.of(999999L)))))
                .andExpect(status().isBadRequest());
        mvc.perform(auth(post("/api/admin/users")).contentType(APPLICATION_JSON).content(body(Map.of(
                "username", "batch4-validate", "password", PASSWORD, "nickname", "Test", "siteIds", List.of(B)))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("ADMIN_USERNAME_EXISTS"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM admin_user WHERE username = 'batch4-invalid-site'", Integer.class));
    }

    private void failAudit() {
        doAnswer(call -> { call.callRealMethod(); throw new IllegalStateException("audit failed"); })
                .when(logs).success(eq("ADMIN_USER"), anyString(), anyString(), anyString(), anyString(), isNull(), anyString());
    }

    @Test
    void creationAndItsBindingsRollBackWhenAuditFails() throws Exception {
        failAudit();
        mvc.perform(auth(post("/api/admin/users")).contentType(APPLICATION_JSON).content(body(Map.of(
                "username", "batch4-rollback", "password", PASSWORD, "nickname", "Rollback", "siteIds", List.of(A, B)))))
                .andExpect(status().isInternalServerError());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM admin_user WHERE username = 'batch4-rollback'", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM operation_log WHERE module = 'ADMIN_USER'", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM admin_user_site WHERE site_id IN (?, ?)", Integer.class, A, B));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/sites", "/status", "/password"})
    void allMutationsRollBackAccountGrantVersionAndAuditOnFailure(String suffix) throws Exception {
        long id = create("batch4-rollback-change", List.of(A)).get("id").asLong();
        String hash = jdbc.queryForObject("SELECT password_hash FROM admin_user WHERE id = ?", String.class, id);
        failAudit();
        Object payload = switch (suffix) {
            case "/sites" -> Map.of("siteIds", List.of(B));
            case "/status" -> Map.of("status", "DISABLED");
            case "/password" -> Map.of("password", "replacement-password");
            default -> Map.of("nickname", "Changed");
        };
        mvc.perform(auth(put("/api/admin/users/" + id + suffix)).contentType(APPLICATION_JSON).content(body(payload)))
                .andExpect(status().isInternalServerError());
        assertEquals(0L, version(id));
        assertEquals("ENABLED", jdbc.queryForObject("SELECT status FROM admin_user WHERE id = ?", String.class, id));
        assertEquals("Batch4 Editor", jdbc.queryForObject("SELECT nickname FROM admin_user WHERE id = ?", String.class, id));
        assertEquals(hash, jdbc.queryForObject("SELECT password_hash FROM admin_user WHERE id = ?", String.class, id));
        assertEquals(List.of(A), jdbc.queryForList("SELECT site_id FROM admin_user_site WHERE admin_user_id = ?", Long.class, id));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM operation_log WHERE module = 'ADMIN_USER'", Integer.class));
    }

    @Test
    void optionsIncludeDisabledSitesAndOpenApiDescribesAllOperations() throws Exception {
        jdbc.update("UPDATE site SET status = 'DISABLED' WHERE id = ?", B);
        var result = mvc.perform(auth(get("/api/admin/sites/options"))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode options = json.readTree(result).get("data");
        boolean found = false;
        for (JsonNode option : options) if (option.get("id").asLong() == B) {
            found = true; assertEquals("DISABLED", option.get("status").asText());
        }
        assertTrue(found);
        create("batch4-disabled-site", List.of(B)); // Granting is permitted; disabled domains still cannot log in.
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/users'].get").exists())
                .andExpect(jsonPath("$.paths['/api/admin/users'].post").exists())
                .andExpect(jsonPath("$.paths['/api/admin/users/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/admin/users/{id}/sites'].put").exists())
                .andExpect(jsonPath("$.paths['/api/admin/users/{id}/status'].put").exists())
                .andExpect(jsonPath("$.paths['/api/admin/users/{id}/password'].put").exists())
                .andExpect(jsonPath("$.paths['/api/admin/sites/options'].get").exists())
                .andExpect(jsonPath("$.components.schemas.AdminUserCreateRequest.properties.password.writeOnly").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"username\":\"bad space\",\"password\":\"valid-password\",\"nickname\":\"Test\",\"siteIds\":[]}",
            "{\"username\":\"batch4-invalid\",\"password\":\"short\",\"nickname\":\"Test\",\"siteIds\":[]}",
            "{\"username\":\"batch4-invalid\",\"password\":\"valid-password\",\"nickname\":\" \",\"siteIds\":[]}",
            "{\"username\":\"batch4-invalid\",\"password\":\"valid-password\",\"nickname\":\"Test\",\"siteIds\":[null]}",
            "{\"username\":\"batch4-invalid\",\"password\":\"valid-password\",\"nickname\":\"Test\"}"
    })
    void invalidRequestsCannotCreateAccounts(String payload) throws Exception {
        mvc.perform(auth(post("/api/admin/users")).contentType(APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM admin_user WHERE username = 'batch4-invalid'", Integer.class));
    }
}

