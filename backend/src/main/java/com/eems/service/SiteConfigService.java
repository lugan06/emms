package com.eems.service;

import com.eems.dto.SiteConfigUpdateRequest;
import com.eems.vo.PublicSiteVO;
import com.eems.vo.SiteConfigVO;

public interface SiteConfigService {
    SiteConfigVO getDefault();
    PublicSiteVO getPublicDefault();
    SiteConfigVO updateDefault(SiteConfigUpdateRequest request);
}
