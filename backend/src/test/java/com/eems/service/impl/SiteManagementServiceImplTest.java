package com.eems.service.impl;

import com.eems.common.exception.BusinessException;
import com.eems.dto.*;
import com.eems.entity.Site;
import com.eems.mapper.SiteManagementMapper;
import com.eems.service.OperationLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DuplicateKeyException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SiteManagementServiceImplTest {
    private final SiteManagementMapper mapper = mock(SiteManagementMapper.class);
    private final OperationLogService logs = mock(OperationLogService.class);
    private final SiteManagementServiceImpl service = new SiteManagementServiceImpl(mapper, logs);

    private SiteSaveRequest request(List<SiteDomainRequest> domains) {
        return new SiteSaveRequest(" New site ", "new-site", domains);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://example.com", "example.com:80", "example.com/path",
            "a..example", "-a.example", "a_.example", "*.example", "user@example.com", "example.com?x=1",
            "example.com#x", "example.com.", "[::1]", "中文.example", "a example.com", ""})
    void rejectsInvalidDomainsBeforeAnyWrite(String domain) {
        assertThrows(BusinessException.class, () -> service.create(request(List.of(new SiteDomainRequest(domain, true))), "super"));
        verifyNoInteractions(mapper, logs);
    }

    @Test
    void requiresExactlyOnePrimaryAndNoDuplicateNormalizedDomains() {
        for (var domains : List.of(List.<SiteDomainRequest>of(),
                List.of(new SiteDomainRequest("a.example", false)),
                List.of(new SiteDomainRequest("a.example", true), new SiteDomainRequest("b.example", true)),
                List.of(new SiteDomainRequest("A.EXAMPLE", true), new SiteDomainRequest("a.example", false)))) {
            assertThrows(BusinessException.class, () -> service.create(request(domains), "super"));
        }
        verifyNoInteractions(mapper, logs);
    }

    @Test
    void createsDisabledSiteWithExplicitTargetConfigurationAndAuditInOrder() {
        doAnswer(call -> { Site s = call.getArgument(0); s.setId(42L); return 1; }).when(mapper).insert(any(Site.class));
        var value = service.create(request(List.of(new SiteDomainRequest(" A.EXAMPLE ", true),
                new SiteDomainRequest("alias.example", false))), "super");
        assertEquals("DISABLED", value.status());
        assertEquals("New site", value.name());
        assertEquals("a.example", value.domains().get(0).domain());
        var order = inOrder(mapper, logs);
        order.verify(mapper).insert(any(Site.class));
        order.verify(mapper).insertDomain(42L, "a.example", true);
        order.verify(mapper).insertDomain(42L, "alias.example", false);
        order.verify(mapper).initializeConfig(42L, "New site", "a.example");
        order.verify(logs).success("SITE_MANAGEMENT", "super", "新建站点", "POST", "/api/admin/sites", null, "{\"siteId\":42}");
    }

    @Test
    void blocksExistingCodeAndExistingDomains() {
        var request = request(List.of(new SiteDomainRequest("a.example", true)));
        when(mapper.selectCount(any())).thenReturn(1L);
        assertEquals("SITE_CODE_EXISTS", assertThrows(BusinessException.class, () -> service.create(request, "super")).getCode());
        when(mapper.selectCount(any())).thenReturn(0L);
        when(mapper.countOtherDomain("a.example", 0L)).thenReturn(1);
        assertEquals("SITE_DOMAIN_EXISTS", assertThrows(BusinessException.class, () -> service.create(request, "super")).getCode());
        verify(mapper, never()).insert(any(Site.class));
    }

    @Test
    void mapsUniqueConstraintRaceToBusinessError() {
        when(mapper.insert(any(Site.class))).thenThrow(new DuplicateKeyException("racing insert"));
        assertEquals("SITE_CONFLICT", assertThrows(BusinessException.class,
                () -> service.create(request(List.of(new SiteDomainRequest("a.example", true))), "super")).getCode());
        verifyNoInteractions(logs);
    }

    @Test
    void rejectsCodeChangeBeforeReplacingDomains() {
        Site existing = new Site(); existing.setId(42L); existing.setCode("immutable");
        when(mapper.lockSite(42L)).thenReturn(existing);
        assertEquals("SITE_CODE_IMMUTABLE", assertThrows(BusinessException.class,
                () -> service.update(42L, request(List.of(new SiteDomainRequest("a.example", true))), "super")).getCode());
        verify(mapper, never()).deleteDomains(anyLong());
        verifyNoInteractions(logs);
    }
}

