package com.eems.service;

import com.eems.dto.AdminLoginRequest;
import com.eems.vo.AdminLoginResponse;

public interface AdminAuthService {

    AdminLoginResponse login(AdminLoginRequest request, String clientIp);
}
