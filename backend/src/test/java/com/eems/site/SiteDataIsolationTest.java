package com.eems.site;

import com.eems.common.exception.BusinessException;
import com.eems.dto.ExhibitionPageQuery;
import com.eems.dto.ExhibitionSaveRequest;
import com.eems.entity.Exhibition;
import com.eems.mapper.CmsCategoryMapper;
import com.eems.mapper.CmsContentMapper;
import com.eems.mapper.ExhibitionMapper;
import com.eems.service.ExhibitionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Sql({"/public-home-fixture.sql", "/site-data-isolation-fixture.sql"})
class SiteDataIsolationTest {
    @Autowired private ExhibitionService exhibitionService;
    @Autowired private ExhibitionMapper exhibitionMapper;
    @Autowired private CmsCategoryMapper categoryMapper;
    @Autowired private CmsContentMapper contentMapper;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void selectSiteA() {
        SiteContext.setSiteId(1L);
    }

    @AfterEach
    void clearSite() {
        SiteContext.clear();
    }

    @Test
    void servicePageAndGetNeverExposeOtherSite() {
        var page = exhibitionService.page(new ExhibitionPageQuery());
        assertEquals(1, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals(101L, page.getRecords().get(0).id());
        assertEquals(101L, exhibitionService.get(101L).id());
        assertEquals("EXHIBITION_NOT_FOUND", assertThrows(BusinessException.class,
                () -> exhibitionService.get(202L)).getCode());

        SiteContext.setSiteId(2L);
        assertEquals(202L, exhibitionService.get(202L).id());
        assertEquals(1, exhibitionService.page(new ExhibitionPageQuery()).getTotal());
        assertEquals("EXHIBITION_NOT_FOUND", assertThrows(BusinessException.class,
                () -> exhibitionService.get(101L)).getCode());
    }

    @Test
    void serviceAndMapperCannotUpdateOrDeleteOtherSiteById() {
        ExhibitionSaveRequest request = new ExhibitionSaveRequest();
        request.setExhibitionCode("EXPO-B");
        request.setTitle("Changed from site A");
        request.setYear(2028);
        request.setStartAt(LocalDateTime.of(2028, 1, 1, 9, 0));
        request.setEndAt(LocalDateTime.of(2028, 1, 2, 17, 0));
        assertEquals("EXHIBITION_NOT_FOUND", assertThrows(BusinessException.class,
                () -> exhibitionService.update(202L, request, "admin")).getCode());
        assertEquals("EXHIBITION_NOT_FOUND", assertThrows(BusinessException.class,
                () -> exhibitionService.delete(202L, "admin")).getCode());

        Exhibition changed = new Exhibition();
        changed.setId(202L);
        changed.setTitle("Direct mapper attempt");
        assertEquals(0, exhibitionMapper.updateById(changed));
        assertEquals(0, exhibitionMapper.deleteById(202L));
        assertEquals("Site B exhibition", jdbc.queryForObject("SELECT title FROM exhibition WHERE id = 202", String.class));
        assertEquals(0, jdbc.queryForObject("SELECT deleted FROM exhibition WHERE id = 202", Integer.class));

        Exhibition attemptedTransfer = new Exhibition();
        attemptedTransfer.setId(101L);
        attemptedTransfer.setSiteId(2L);
        attemptedTransfer.setTitle("Updated within site A");
        assertEquals(1, exhibitionMapper.updateById(attemptedTransfer));
        assertEquals(1L, jdbc.queryForObject("SELECT site_id FROM exhibition WHERE id = 101", Long.class));

        assertEquals(1, exhibitionMapper.deleteById(101L));
        assertEquals(1, jdbc.queryForObject("SELECT deleted FROM exhibition WHERE id = 101", Integer.class));
    }

    @Test
    void insertAlwaysTakesSiteIdFromContextAndMissingContextFailsClosed() {
        Exhibition created = new Exhibition();
        created.setId(301L);
        created.setSiteId(2L);
        created.setExhibitionCode("NEW-A");
        created.setTitle("New A exhibition");
        assertEquals(1, exhibitionMapper.insert(created));
        assertEquals(1L, created.getSiteId());
        assertEquals(1L, jdbc.queryForObject("SELECT site_id FROM exhibition WHERE id = 301", Long.class));

        SiteContext.clear();
        RuntimeException error = assertThrows(RuntimeException.class, () -> exhibitionMapper.selectById(101L));
        Throwable rootCause = error;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        assertEquals(IllegalStateException.class, rootCause.getClass());
        assertEquals("Site context is missing", rootCause.getMessage());
    }

    @Test
    void handwrittenSelectCountAndRestoreAreScoped() {
        assertEquals(2L, categoryMapper.selectIncludingDeletedByCode("exhibition-intro", 1L).getId());
        assertNull(categoryMapper.selectDeletedById(1002L));
        assertEquals(0, categoryMapper.restoreById(1002L));
        assertEquals(1001L, contentMapper.selectIncludingDeletedBySlug("shared-slug", 1L).getId());
        assertEquals(0, contentMapper.countActiveByCategoryId(1002L));
        assertEquals(0, exhibitionMapper.restoreById(203L, null));
        assertEquals(1, jdbc.queryForObject("SELECT deleted FROM exhibition WHERE id = 203", Integer.class));

        SiteContext.setSiteId(2L);
        assertEquals(1002L, categoryMapper.selectIncludingDeletedByCode("exhibition-intro", 2L).getId());
        assertNotNull(categoryMapper.selectDeletedById(1002L));
        assertEquals(1002L, contentMapper.selectIncludingDeletedBySlug("shared-slug", 2L).getId());
        assertEquals(1, contentMapper.countActiveByCategoryId(1002L));
        assertEquals(1, exhibitionMapper.restoreById(203L, null));
    }
}
