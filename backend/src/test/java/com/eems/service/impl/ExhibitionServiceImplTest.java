package com.eems.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.eems.common.exception.BusinessException;
import com.eems.dto.ExhibitionPageQuery;
import com.eems.dto.ExhibitionSaveRequest;
import com.eems.dto.PublicExhibitionPageQuery;
import com.eems.entity.Exhibition;
import com.eems.entity.SiteConfig;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.ExhibitionMapper;
import com.eems.mapper.SiteConfigMapper;
import com.eems.service.OperationLogService;
import com.eems.vo.ExhibitionVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExhibitionServiceImplTest {
    private final ExhibitionMapper mapper = mock(ExhibitionMapper.class);
    private final SiteConfigMapper siteConfigMapper = mock(SiteConfigMapper.class);
    private final OperationLogService logService = mock(OperationLogService.class);
    private final AdminUserMapper adminUserMapper = mock(AdminUserMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExhibitionServiceImpl service = new ExhibitionServiceImpl(
            mapper, siteConfigMapper, logService, adminUserMapper, objectMapper);

    @Test
    void createShouldRejectEndBeforeStart() {
        ExhibitionSaveRequest request = request();
        request.setEndAt(request.getStartAt());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create(request, "admin"));

        assertEquals("EXHIBITION_TIME_INVALID", exception.getCode());
        verify(mapper, never()).insert(any(Exhibition.class));
    }

    @Test
    void updateShouldEncodePlainTextExtraDataForJsonColumn() {
        Exhibition value = exhibition(1L, "PUBLISHED");
        value.setExhibitionCode("EEMS-2026");
        ExhibitionSaveRequest request = request();
        request.setExtraData("ex adipisicing Excepteur");
        when(mapper.selectById(1L)).thenReturn(value);
        when(mapper.selectOne(any())).thenReturn(value);
        when(mapper.updateById(any(Exhibition.class))).thenReturn(1);

        service.update(1L, request, "admin");

        assertEquals("\"ex adipisicing Excepteur\"", value.getExtraData());
        verify(mapper).updateById(value);
    }

    @Test
    void updateShouldKeepValidJsonExtraDataUnchanged() {
        Exhibition value = exhibition(1L, "PUBLISHED");
        value.setExhibitionCode("EEMS-2026");
        ExhibitionSaveRequest request = request();
        request.setExtraData("{\"theme\":\"green\"}");
        when(mapper.selectById(1L)).thenReturn(value);
        when(mapper.selectOne(any())).thenReturn(value);
        when(mapper.updateById(any(Exhibition.class))).thenReturn(1);

        service.update(1L, request, "admin");

        assertEquals("{\"theme\":\"green\"}", value.getExtraData());
    }

    @Test
    void publishShouldOnlyAllowDraftAndWriteLog() {
        Exhibition exhibition = exhibition(1L, "DRAFT");
        when(mapper.selectById(1L)).thenReturn(exhibition);
        when(mapper.updateById(any(Exhibition.class))).thenReturn(1);

        service.publish(1L, "admin");

        assertEquals("PUBLISHED", exhibition.getStatus());
        verify(logService).success("admin", "发布展会", "/api/admin/exhibitions/1/publish", "{\"id\":1}");
    }

    @Test
    void republishShouldChangeOfflineToPublishedAndWriteLog() {
        Exhibition exhibition = exhibition(8L, "OFFLINE");
        when(mapper.selectById(8L)).thenReturn(exhibition);
        when(mapper.updateById(any(Exhibition.class))).thenReturn(1);

        ExhibitionVO result = service.republish(8L, "admin");

        assertEquals("PUBLISHED", result.status());
        assertEquals("PUBLISHED", exhibition.getStatus());
        verify(logService).success("admin", "重新上架展会", "/api/admin/exhibitions/8/republish", "{\"id\":8}");
    }

    @Test
    void republishShouldRejectDraft() {
        when(mapper.selectById(9L)).thenReturn(exhibition(9L, "DRAFT"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.republish(9L, "admin"));

        assertEquals("EXHIBITION_NOT_OFFLINE", exception.getCode());
        verify(mapper, never()).updateById(any(Exhibition.class));
    }

    @Test
    void offlineShouldRejectDraft() {
        when(mapper.selectById(2L)).thenReturn(exhibition(2L, "DRAFT"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.offline(2L, "admin"));

        assertEquals("EXHIBITION_NOT_PUBLISHED", exception.getCode());
        verify(mapper, never()).updateById(any(Exhibition.class));
    }

    @Test
    void setCurrentShouldRequirePublishedAndUpdateSingletonConfig() {
        when(mapper.selectById(3L)).thenReturn(exhibition(3L, "PUBLISHED"));
        SiteConfig config = new SiteConfig();
        config.setId(8L);
        config.setConfigCode("default");
        when(siteConfigMapper.selectOne(any())).thenReturn(config);
        when(siteConfigMapper.updateById(any(SiteConfig.class))).thenReturn(1);

        service.setCurrent(3L, "admin");

        assertEquals(3L, config.getCurrentExhibitionId());
        verify(siteConfigMapper).updateById(config);
        verify(logService).success("admin", "设置当前展会", "/api/admin/exhibitions/3/set-current", "{\"id\":3}");
    }

    @Test
    void deleteShouldUseLogicalDeleteAndWriteLog() {
        Exhibition exhibition = exhibition(4L, "OFFLINE");
        exhibition.setExhibitionCode("EEMS-2026");
        when(mapper.selectById(4L)).thenReturn(exhibition);
        when(mapper.deleteById(4L)).thenReturn(1);

        service.delete(4L, "admin");

        verify(mapper).deleteById(4L);
        verify(logService).success("admin", "删除展会", "/api/admin/exhibitions/4",
                "{\"id\":4,\"code\":\"EEMS-2026\"}");
    }

    @Test
    void restoreShouldClearLogicalDeleteAndWriteLog() {
        Exhibition exhibition = exhibition(6L, "OFFLINE");
        exhibition.setExhibitionCode("EEMS-2026");
        when(mapper.restoreById(6L, null)).thenReturn(1);
        when(mapper.selectById(6L)).thenReturn(exhibition);

        ExhibitionVO result = service.restore(6L, "admin");

        assertEquals(6L, result.id());
        verify(mapper).restoreById(6L, null);
        verify(logService).success("admin", "恢复展会", "/api/admin/exhibitions/6/restore", "{\"id\":6}");
    }

    @Test
    void restoreShouldRejectActiveExhibition() {
        when(mapper.restoreById(7L, null)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.restore(7L, "admin"));

        assertEquals("EXHIBITION_NOT_DELETED", exception.getCode());
        verify(mapper, never()).selectById(7L);
    }

    @Test
    void pageShouldFilterAndMapRecords() {
        Exhibition value = exhibition(5L, "PUBLISHED");
        value.setTitle("涂料博览会");
        Page<Exhibition> page = new Page<>(1, 20);
        page.setRecords(List.of(value));
        page.setTotal(1);
        when(mapper.selectPage(any(), any())).thenReturn(page);

        ExhibitionPageQuery query = new ExhibitionPageQuery();
        query.setKeyword("涂料");
        query.setStatus("PUBLISHED");
        query.setYear(2026);

        assertEquals("涂料博览会", service.page(query).getRecords().get(0).title());
        verify(mapper).selectPage(any(), any());
    }

    @Test
    void publicPageShouldMapCurrentExhibition() {
        Exhibition value = exhibition(10L, "PUBLISHED");
        value.setTitle("公开展会");
        Page<Exhibition> page = new Page<>(1, 20);
        page.setRecords(List.of(value));
        page.setTotal(1);
        SiteConfig config = new SiteConfig();
        config.setConfigCode("default");
        config.setCurrentExhibitionId(10L);
        when(mapper.selectPage(any(), any())).thenReturn(page);
        when(siteConfigMapper.selectOne(any())).thenReturn(config);

        PublicExhibitionPageQuery query = new PublicExhibitionPageQuery();

        assertEquals(true, service.publicPage(query).getRecords().get(0).isCurrent());
    }

    @Test
    void publicDetailShouldRejectUnpublishedExhibition() {
        when(mapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publicGet(11L));

        assertEquals("PUBLIC_EXHIBITION_NOT_FOUND", exception.getCode());
    }

    private ExhibitionSaveRequest request() {
        ExhibitionSaveRequest request = new ExhibitionSaveRequest();
        request.setExhibitionCode("EEMS-2026");
        request.setTitle("涂料博览会");
        request.setYear(2026);
        request.setStartAt(LocalDateTime.of(2026, 6, 10, 9, 0));
        request.setEndAt(LocalDateTime.of(2026, 6, 12, 17, 0));
        return request;
    }

    private Exhibition exhibition(Long id, String status) {
        Exhibition value = new Exhibition();
        value.setId(id);
        value.setStatus(status);
        value.setStartAt(LocalDateTime.of(2026, 6, 10, 9, 0));
        value.setEndAt(LocalDateTime.of(2026, 6, 12, 17, 0));
        return value;
    }
}
