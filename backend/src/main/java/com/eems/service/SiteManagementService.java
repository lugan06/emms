package com.eems.service;

import com.eems.common.api.PageResult;
import com.eems.dto.SitePageQuery;
import com.eems.dto.SiteSaveRequest;
import com.eems.vo.ManagedSiteVO;

public interface SiteManagementService {
    java.util.List<com.eems.vo.SiteOptionVO> options();
    PageResult<ManagedSiteVO> page(SitePageQuery query);
    ManagedSiteVO create(SiteSaveRequest request, String username);
    ManagedSiteVO update(Long id, SiteSaveRequest request, String username);
    ManagedSiteVO changeStatus(Long id, String status, String username);
}

