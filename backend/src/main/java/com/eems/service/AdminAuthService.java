package com.eems.service;

import com.eems.dto.AdminLoginRequest;
import com.eems.dto.AdminPasswordChangeRequest;
import com.eems.vo.AdminLoginResponse;
import com.eems.vo.AdminUserVO;

public interface AdminAuthService {

    AdminLoginResponse login(AdminLoginRequest request, String clientIp);

    AdminUserVO currentUser(String username);

    void changePassword(String username, AdminPasswordChangeRequest request);
}
