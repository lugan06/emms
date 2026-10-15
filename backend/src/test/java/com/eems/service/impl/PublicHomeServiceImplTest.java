package com.eems.service.impl;

import com.eems.service.CompanyProfileService;
import com.eems.common.exception.BusinessException;
import com.eems.service.ExhibitionService;
import com.eems.service.PublicContentService;
import com.eems.service.SiteConfigService;
import com.eems.vo.PublicCompanyVO;
import com.eems.vo.PublicContentVO;
import com.eems.vo.PublicExhibitionVO;
import com.eems.vo.PublicHomeVO;
import com.eems.vo.PublicSiteVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicHomeServiceImplTest {
    private final SiteConfigService siteConfigService = mock(SiteConfigService.class);
    private final CompanyProfileService companyProfileService = mock(CompanyProfileService.class);
    private final ExhibitionService exhibitionService = mock(ExhibitionService.class);
    private final PublicContentService publicContentService = mock(PublicContentService.class);
    private final PublicHomeServiceImpl service = new PublicHomeServiceImpl(
            siteConfigService, companyProfileService, exhibitionService, publicContentService);

    @Test
    void homeShouldUseCurrentExhibitionForExhibitionSections() {
        PublicSiteVO site = new PublicSiteVO("EEMS", null, null, null, null, null, null, null);
        PublicCompanyVO company = new PublicCompanyVO("EEMS", null, null, null, null, null, null);
        PublicExhibitionVO exhibition = mock(PublicExhibitionVO.class);
        PublicContentVO content = mock(PublicContentVO.class);
        when(siteConfigService.getPublicDefault()).thenReturn(site);
        when(siteConfigService.getCurrentExhibitionId()).thenReturn(8L);
        when(companyProfileService.getPublicDefault()).thenReturn(company);
        when(exhibitionService.publicGet(8L)).thenReturn(exhibition);
        when(publicContentService.topByCategory(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), eq(6))).thenReturn(List.of(content));

        PublicHomeVO result = service.home();

        assertEquals(site, result.site());
        assertEquals(exhibition, result.currentExhibition());
        verify(publicContentService).topByCategory("exhibition-intro", 8L, 6);
        verify(publicContentService).topByCategory("events", 8L, 6);
        verify(publicContentService).topByCategory("exhibition-scope", 8L, 6);
        verify(publicContentService).topByCategory("exhibition-news", 8L, 6);
        verify(publicContentService).topByCategory("industry-news", null, 6);
        verify(publicContentService).topByCategory("exhibitor-news", null, 6);
        assertEquals(List.of(content), result.samePeriodActivities());
    }

    @Test
    void missingCurrentExhibitionShouldNotMixContentFromOtherExhibitions() {
        when(siteConfigService.getCurrentExhibitionId()).thenReturn(null);
        PublicHomeVO result = service.home();

        assertEquals(List.of(), result.exhibitionIntro());
        assertEquals(List.of(), result.samePeriodActivities());
        assertEquals(List.of(), result.exhibitionScope());
        assertEquals(List.of(), result.exhibitionNews());
        verify(exhibitionService, never()).publicGet(org.mockito.ArgumentMatchers.any());
        verify(publicContentService, never()).topByCategory(eq("exhibition-intro"), isNull(), eq(6));
        verify(publicContentService).topByCategory("industry-news", null, 6);
        verify(publicContentService).topByCategory("exhibitor-news", null, 6);
    }

    @Test
    void unavailableCurrentExhibitionShouldNotExposeItsSections() {
        when(siteConfigService.getCurrentExhibitionId()).thenReturn(8L);
        when(exhibitionService.publicGet(8L)).thenThrow(new BusinessException("PUBLIC_EXHIBITION_NOT_FOUND", "展会不存在"));

        PublicHomeVO result = service.home();

        assertEquals(null, result.currentExhibition());
        assertEquals(List.of(), result.exhibitionIntro());
        verify(publicContentService, never()).topByCategory(anyString(), eq(8L), eq(6));
    }
}
