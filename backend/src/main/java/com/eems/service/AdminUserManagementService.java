package com.eems.service;

import com.eems.common.api.PageResult;
import com.eems.dto.*;
import com.eems.vo.ManagedAdminUserVO;
import java.util.List;

public interface AdminUserManagementService {
    PageResult<ManagedAdminUserVO> page(AdminUserPageQuery query);
    ManagedAdminUserVO create(AdminUserCreateRequest request, String operator);
    ManagedAdminUserVO update(Long id, AdminUserUpdateRequest request, String operator);
    ManagedAdminUserVO assignSites(Long id, List<Long> siteIds, String operator);
    ManagedAdminUserVO changeStatus(Long id, String status, String operator);
    void resetPassword(Long id, String password, String operator);
}

