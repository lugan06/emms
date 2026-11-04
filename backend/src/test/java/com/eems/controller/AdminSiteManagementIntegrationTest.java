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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSiteManagementIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtTokenUtil tokens;
    @Autowired private ObjectMapper json;
    @MockitoSpyBean private OperationLogServiceImpl logs;
    private String superToken;
    private String editorToken;

    @BeforeEach
    void setup() {
        for (long id = 73001; id <= 73002; id++) {
            jdbc.update("INSERT INTO admin_user (id, username, password_hash, nickname, role_code) VALUES (?, ?, 'unused', 'Batch3', ?)",
                    id, "batch3-user-" + id, id == 73001 ? "SUPER_ADMIN" : "EDITOR");
        }
        jdbc.update("INSERT INTO admin_user_site (admin_user_id, site_id) VALUES (73002, 1)");
        superToken = tokens.generateToken(73001L, "batch3-user-73001", "SUPER_ADMIN", 1L, 0L);
        editorToken = tokens.generateToken(73002L, "batch3-user-73002", "EDITOR", 1L, 0L);
    }

    @AfterEach
    void cleanup() {
        assertNull(SiteContext.currentSiteIdOrNull());
        jdbc.update("DELETE FROM operation_log WHERE username LIKE 'batch3-user-%' OR module = 'SITE_MANAGEMENT'");
        jdbc.update("DELETE FROM admin_user_site WHERE admin_user_id IN (73001, 73002)");
        jdbc.update("DELETE FROM admin_user WHERE id IN (73001, 73002)");
        var ids = jdbc.queryForList("SELECT id FROM site WHERE code LIKE 'batch3-%'", Long.class);
        for (Long id : ids) {
            jdbc.update("DELETE FROM site_config WHERE site_id = ?", id);
            jdbc.update("DELETE FROM site_domain WHERE site_id = ?", id);
            jdbc.update("DELETE FROM site WHERE id = ?", id);
        }
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request) {
        return request.header("Host", "localhost").header("Authorization", "Bearer " + superToken);
    }

    private String body(String code, String name, String primary, String alias) throws Exception {
        return json.writeValueAsString(Map.of("code", code, "name", name, "domains",
                List.of(Map.of("domain", primary, "primary", true), Map.of("domain", alias, "primary", false))));
    }

    private JsonNode create(String code) throws Exception {
        var response = mvc.perform(auth(post("/api/admin/sites")).contentType(APPLICATION_JSON)
                .content(body(code, "Batch3 New", code + ".example", "alias." + code + ".example")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("DISABLED"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("data");
    }

    @Test
    void createsAllRowsInTargetSiteAndAuditsWhileKeepingHostConfigUnchanged() throws Exception {
        int hostConfigs = jdbc.queryForObject("SELECT COUNT(*) FROM site_config WHERE site_id = 1", Integer.class);
        JsonNode site = create("batch3-create");
        long id = site.get("id").asLong();
        assertNotEquals(1L, id);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM site_config WHERE site_id = ? AND config_code = 'default' AND site_title = 'Batch3 New'", Integer.class, id));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM site_domain WHERE site_id = ?", Integer.class, id));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM site_domain WHERE site_id = ? AND is_primary = 1", Integer.class, id));
        assertEquals(hostConfigs, jdbc.queryForObject("SELECT COUNT(*) FROM site_config WHERE site_id = 1", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM operation_log WHERE module = 'SITE_MANAGEMENT' AND request_params = ?", Integer.class, "{\"siteId\":" + id + "}"));
        mvc.perform(auth(get("/api/admin/sites")).param("keyword", "batch3-create").param("status", "DISABLED"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].domains.length()").value(2));
    }

    @Test
    void auditFailureRollsBackSiteDomainsConfigAndAuditInsert() throws Exception {
        doAnswer(call -> { call.callRealMethod(); throw new IllegalStateException("audit unavailable"); })
                .when(logs).success(eq("SITE_MANAGEMENT"), anyString(), anyString(), anyString(), anyString(), isNull(), anyString());
        mvc.perform(auth(post("/api/admin/sites")).contentType(APPLICATION_JSON)
                .content(body("batch3-rollback", "Rollback", "rollback.example", "alias.rollback.example")))
                .andExpect(status().isInternalServerError());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM site WHERE code = 'batch3-rollback'", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM site_domain WHERE domain LIKE '%rollback.example'", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM site_config WHERE site_title = 'Rollback'", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM operation_log WHERE module = 'SITE_MANAGEMENT'", Integer.class));
    }

    @Test
    void editorAndAnonymousCannotUseAnyManagementEndpoint() throws Exception {
        var payload = body("batch3-denied", "Denied", "deny.example", "alias.deny.example");
        for (var request : List.of(get("/api/admin/sites"), post("/api/admin/sites").contentType(APPLICATION_JSON).content(payload),
                put("/api/admin/sites/1").contentType(APPLICATION_JSON).content(payload),
                put("/api/admin/sites/1/status").contentType(APPLICATION_JSON).content("{\"status\":\"DISABLED\"}"))) {
            mvc.perform(request.header("Host", "localhost").header("Authorization", "Bearer " + editorToken))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(get("/api/admin/sites").header("Host", "localhost")).andExpect(status().isUnauthorized());
        mvc.perform(auth(get("/api/admin/sites")).header("X-Site-Id", "1")).andExpect(status().isBadRequest());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM site WHERE code = 'batch3-denied'", Integer.class));
    }

    @Test
    void updateReplacesDomainsSwapsPrimaryAndPreservesCodeAndConfig() throws Exception {
        long id = create("batch3-edit").get("id").asLong();
        mvc.perform(auth(put("/api/admin/sites/" + id)).contentType(APPLICATION_JSON)
                .content(body("changed-code", "Changed", "new.edit.example", "alias.new.edit.example")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SITE_CODE_IMMUTABLE"));
        mvc.perform(auth(put("/api/admin/sites/" + id)).contentType(APPLICATION_JSON)
                .content(body("batch3-edit", "Updated", "alias.batch3-edit.example", "batch3-edit.example")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.name").value("Updated"))
                .andExpect(jsonPath("$.data.code").value("batch3-edit"));
        assertEquals("alias.batch3-edit.example", jdbc.queryForObject("SELECT domain FROM site_domain WHERE site_id = ? AND is_primary = 1", String.class, id));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM site_config WHERE site_id = ?", Integer.class, id));
        mvc.perform(auth(get("/api/admin/sites")).param("keyword", "ALIAS.BATCH3-EDIT"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1));
        mvc.perform(auth(get("/api/admin/sites")).param("keyword", "batch3-%"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void duplicateCodesOrDomainsAreRejectedWithoutPartialChanges() throws Exception {
        long first = create("batch3-first").get("id").asLong();
        long second = create("batch3-second").get("id").asLong();
        mvc.perform(auth(post("/api/admin/sites")).contentType(APPLICATION_JSON)
                .content(body("batch3-first", "Duplicate", "different.example", "alias.different.example")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SITE_CODE_EXISTS"));
        mvc.perform(auth(put("/api/admin/sites/" + second)).contentType(APPLICATION_JSON)
                .content(body("batch3-second", "Rejected", "BATCH3-FIRST.EXAMPLE", "different.example")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SITE_DOMAIN_EXISTS"));
        assertEquals("Batch3 New", jdbc.queryForObject("SELECT name FROM site WHERE id = ?", String.class, second));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM site_domain WHERE site_id = ?", Integer.class, second));
        assertEquals(first, jdbc.queryForObject("SELECT site_id FROM site_domain WHERE domain = 'batch3-first.example'", Long.class));
    }

    @Test
    void enableDisableImmediatelyAffectsHostAndPublicAccess() throws Exception {
        long id = create("batch3-status").get("id").asLong();
        for (String state : List.of("ENABLED", "DISABLED")) {
            mvc.perform(auth(put("/api/admin/sites/" + id + "/status")).contentType(APPLICATION_JSON)
                    .content("{\"status\":\"" + state + "\"}")).andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value(state));
            mvc.perform(get("/api/public/site").header("Host", "batch3-status.example"))
                    .andExpect(status().is("ENABLED".equals(state) ? 200 : 403));
        }
        mvc.perform(auth(get("/api/admin/sites")).param("status", "ENABLED").param("keyword", "batch3-status"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"name\":\"A\",\"code\":\"batch3-invalid\",\"domains\":[]}",
        "{\"name\":\" \",\"code\":\"batch3-invalid\",\"domains\":[{\"domain\":\"a.example\",\"primary\":true}]}",
        "{\"name\":\"A\",\"code\":\"UPPER\",\"domains\":[{\"domain\":\"a.example\",\"primary\":true}]}",
        "{\"name\":\"A\",\"code\":\"batch3-invalid\",\"domains\":[{\"domain\":\"a.example\",\"primary\":false}]}",
        "{\"name\":\"A\",\"code\":\"batch3-invalid\",\"domains\":[{\"domain\":\"https://a.example\",\"primary\":true}]}",
        "{\"name\":\"A\",\"code\":\"batch3-invalid\",\"domains\":[{\"domain\":\"a.example:80\",\"primary\":true}]}",
        "{\"name\":\"A\",\"code\":\"batch3-invalid\",\"domains\":[{\"domain\":\"a.example/path\",\"primary\":true}]}",
        "{\"name\":\"A\",\"code\":\"batch3-invalid\",\"domains\":[{\"domain\":\"a.example\"}]}",
        "{\"name\":\"A\",\"code\":\"batch3-invalid\",\"domains\":[null]}"
    })
    void rejectsInvalidPayloadsWithoutWrites(String payload) throws Exception {
        mvc.perform(auth(post("/api/admin/sites")).contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM site WHERE code = 'batch3-invalid'", Integer.class));
    }

    @Test
    void validatesPaginationStatusAndMissingIdsAndDocumentsAllOperations() throws Exception {
        for (String[] pair : List.of(new String[]{"page", "0"}, new String[]{"pageSize", "101"}, new String[]{"status", "BOGUS"})) {
            mvc.perform(auth(get("/api/admin/sites")).param(pair[0], pair[1])).andExpect(status().isBadRequest());
        }
        mvc.perform(auth(put("/api/admin/sites/9999999/status")).contentType(APPLICATION_JSON).content("{\"status\":\"ENABLED\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(auth(put("/api/admin/sites/1/status")).contentType(APPLICATION_JSON).content("{\"status\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/sites'].get.operationId").exists())
                .andExpect(jsonPath("$.paths['/api/admin/sites'].post.requestBody").exists())
                .andExpect(jsonPath("$.paths['/api/admin/sites/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/admin/sites/{id}/status'].put").exists())
                .andExpect(jsonPath("$.components.schemas.SiteSaveRequest.properties.domains.description").exists());
    }
}

