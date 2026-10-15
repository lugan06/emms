package com.eems.service.impl;

import com.eems.common.exception.BusinessException;
import com.eems.service.CompanyProfileService;
import com.eems.service.ExhibitionService;
import com.eems.service.PublicContentService;
import com.eems.service.PublicHomeService;
import com.eems.service.SiteConfigService;
import com.eems.vo.PublicContentVO;
import com.eems.vo.PublicExhibitionVO;
import com.eems.vo.PublicHomeVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicHomeServiceImpl implements PublicHomeService {
    private static final int SECTION_LIMIT = 6;
    private final SiteConfigService siteConfigService;
    private final CompanyProfileService companyProfileService;
    private final ExhibitionService exhibitionService;
    private final PublicContentService publicContentService;

    public PublicHomeServiceImpl(SiteConfigService siteConfigService,
                                 CompanyProfileService companyProfileService,
                                 ExhibitionService exhibitionService,
                                 PublicContentService publicContentService) {
        this.siteConfigService = siteConfigService;
        this.companyProfileService = companyProfileService;
        this.exhibitionService = exhibitionService;
        this.publicContentService = publicContentService;
    }

    @Override
    public PublicHomeVO home() {
        Long currentExhibitionId = siteConfigService.getCurrentExhibitionId();
        PublicExhibitionVO currentExhibition = publicExhibition(currentExhibitionId);
        Long visibleExhibitionId = currentExhibition == null ? null : currentExhibitionId;
        return new PublicHomeVO(
                siteConfigService.getPublicDefault(),
                companyProfileService.getPublicDefault(),
                currentExhibition,
                current(visibleExhibitionId, "exhibition-intro"),
                current(visibleExhibitionId, "events"),
                current(visibleExhibitionId, "exhibition-scope"),
                current(visibleExhibitionId, "exhibition-news"),
                publicContentService.topByCategory("industry-news", null, SECTION_LIMIT),
                publicContentService.topByCategory("exhibitor-news", null, SECTION_LIMIT));
    }

    private List<PublicContentVO> current(Long exhibitionId, String categoryCode) {
        if (exhibitionId == null) {
            return List.of();
        }
        return publicContentService.topByCategory(categoryCode, exhibitionId, SECTION_LIMIT);
    }

    private PublicExhibitionVO publicExhibition(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return exhibitionService.publicGet(id);
        } catch (BusinessException exception) {
            return null;
        }
    }
}
