package com.eems.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.common.exception.BusinessException;
import com.eems.dto.OperationLogPageQuery;
import com.eems.entity.OperationLog;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.OperationLogMapper;
import com.eems.vo.OperationLogVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperationLogServiceImplTest {
    private final OperationLogMapper mapper = mock(OperationLogMapper.class);
    private final AdminUserMapper adminUserMapper = mock(AdminUserMapper.class);
    private final OperationLogServiceImpl service = new OperationLogServiceImpl(mapper, adminUserMapper);

    @Test
    void pageShouldMapRecordsAndApplyFilters() {
        OperationLog value = new OperationLog();
        value.setId(1L);
        value.setAdminUserId(8L);
        value.setUsername("admin");
        value.setModule("CMS_CONTENT");
        value.setOperation("发布内容");
        value.setRequestMethod("POST");
        value.setRequestUrl("/api/admin/contents/1/publish");
        value.setRequestIp("192.0.2.10");
        value.setRequestParams("{\"id\":1}");
        value.setResult("SUCCESS");
        value.setCreatedAt(LocalDateTime.of(2026, 10, 9, 10, 0));
        Page<OperationLog> page = new Page<>(1, 20);
        page.setRecords(List.of(value));
        page.setTotal(1);
        when(mapper.selectPage(any(), any())).thenReturn(page);

        OperationLogPageQuery query = new OperationLogPageQuery();
        query.setUsername("admin");
        query.setModule("cms_content");
        query.setOperation("发布");
        query.setResult("success");
        query.setStartAt(LocalDateTime.of(2026, 10, 1, 0, 0));
        query.setEndAt(LocalDateTime.of(2026, 10, 31, 23, 59, 59));

        OperationLogVO result = service.page(query).getRecords().get(0);

        assertEquals("admin", result.username());
        assertEquals("CMS_CONTENT", result.module());
        assertEquals("192.0.2.*", result.requestIp());
        assertEquals(1L, service.page(query).getTotal());
        verify(mapper, org.mockito.Mockito.times(2)).selectPage(any(), any());
    }

    @Test
    void pageShouldRedactSensitiveParameters() {
        OperationLog value = new OperationLog();
        value.setRequestIp("10.0.0.12");
        value.setRequestParams("{\"password\":\"secret\",\"phone\":\"13800138000\",\"email\":\"a@example.com\",\"message\":\"private\"}");
        Page<OperationLog> page = new Page<>(1, 20);
        page.setRecords(List.of(value));
        page.setTotal(1);
        when(mapper.selectPage(any(), any())).thenReturn(page);

        OperationLogVO result = service.page(new OperationLogPageQuery()).getRecords().get(0);

        String params = result.requestParams();
        assertEquals("10.0.0.*", result.requestIp());
        org.junit.jupiter.api.Assertions.assertFalse(params.contains("secret"));
        org.junit.jupiter.api.Assertions.assertFalse(params.contains("13800138000"));
        org.junit.jupiter.api.Assertions.assertFalse(params.contains("a@example.com"));
        org.junit.jupiter.api.Assertions.assertFalse(params.contains("private"));
    }

    @Test
    void pageShouldRejectInvalidResult() {
        OperationLogPageQuery query = new OperationLogPageQuery();
        query.setResult("UNKNOWN");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.page(query));

        assertEquals("OPERATION_LOG_RESULT_INVALID", exception.getCode());
    }
}
