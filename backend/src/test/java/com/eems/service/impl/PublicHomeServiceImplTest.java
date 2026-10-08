package com.eems.service.impl;

import com.eems.service.CompanyProfileService;
import com.eems.service.ExhibitionService;
import com.eems.service.PublicContentService;
import com.eems.service.SiteConfigService;
import com.eems.vo.PublicCompanyVO;
import com.eems.vo.PublicContentListVO;
import com.eems.vo.PublicExhibitionVO;
import com.eems.vo.PublicHomeVO;
import com.eems.vo.PublicSiteVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
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
        PublicContentListVO content = mock(PublicContentListVO.class);
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
        verify(publicContentService).topByCategory("industry-news", null, 6);
    }
}
