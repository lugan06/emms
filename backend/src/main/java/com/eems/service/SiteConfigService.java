package com.eems.service;

import com.eems.dto.SiteConfigUpdateRequest;
import com.eems.vo.SiteConfigVO;

public interface SiteConfigService {
    SiteConfigVO getDefault();
    SiteConfigVO updateDefault(SiteConfigUpdateRequest request);
}
