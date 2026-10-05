package com.eems.service.impl;

import com.eems.dto.SiteConfigUpdateRequest;
import com.eems.entity.SiteConfig;
import com.eems.mapper.ExhibitionMapper;
import com.eems.mapper.SiteConfigMapper;
import com.eems.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

class SiteConfigServiceImplTest {
    @Test
    void updateDefaultShouldUpdateSingletonConfig() {
        SiteConfigMapper mapper = mock(SiteConfigMapper.class);
        ExhibitionMapper exhibitionMapper = mock(ExhibitionMapper.class);
        SiteConfig config = new SiteConfig();
        config.setId(1L);
        config.setConfigCode("default");
        config.setSiteTitle("Old title");
        when(mapper.selectOne(any())).thenReturn(config);
        when(exhibitionMapper.selectCount(any())).thenReturn(1L);

        SiteConfigServiceImpl service = new SiteConfigServiceImpl(mapper, exhibitionMapper);
        when(mapper.updateById(any(SiteConfig.class))).thenReturn(1);
        service.updateDefault(new SiteConfigUpdateRequest("New title", null, null, null, null,
                null, null, null, null, null, 2L));

        ArgumentCaptor<SiteConfig> captor = ArgumentCaptor.forClass(SiteConfig.class);
        verify(mapper).updateById(captor.capture());
        assertEquals("New title", captor.getValue().getSiteTitle());
        assertEquals(2L, captor.getValue().getCurrentExhibitionId());
    }

    @Test
    void updateDefaultShouldRejectMissingCurrentExhibition() {
        SiteConfigMapper mapper = mock(SiteConfigMapper.class);
        ExhibitionMapper exhibitionMapper = mock(ExhibitionMapper.class);
        when(exhibitionMapper.selectCount(any())).thenReturn(0L);

        SiteConfigServiceImpl service = new SiteConfigServiceImpl(mapper, exhibitionMapper);

        BusinessException exception = org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> service.updateDefault(new SiteConfigUpdateRequest("Title", null, null, null, null,
                        null, null, null, null, null, 44L)));

        assertEquals("EXHIBITION_NOT_FOUND", exception.getCode());
        verify(mapper, never()).updateById(any(SiteConfig.class));
    }
}
