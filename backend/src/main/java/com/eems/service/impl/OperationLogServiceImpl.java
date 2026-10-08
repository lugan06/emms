package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eems.entity.AdminUser;
import com.eems.entity.OperationLog;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.OperationLogMapper;
import com.eems.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl implements OperationLogService {
    private final OperationLogMapper mapper;
    private final AdminUserMapper adminUserMapper;

    public OperationLogServiceImpl(OperationLogMapper mapper, AdminUserMapper adminUserMapper) {
        this.mapper = mapper;
        this.adminUserMapper = adminUserMapper;
    }

    @Override
    public void success(String username, String operation, String requestUrl, String params) {
        success("EXHIBITION", username, operation, requestUrl, params);
    }

    @Override
    public void success(String module, String username, String operation, String requestUrl, String params) {
        AdminUser user = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username));
        OperationLog log = new OperationLog();
        log.setAdminUserId(user == null ? null : user.getId());
        log.setUsername(username);
        log.setModule(module);
        log.setOperation(operation);
        log.setRequestMethod("POST");
        log.setRequestUrl(requestUrl);
        log.setRequestParams(params);
        log.setResult("SUCCESS");
        mapper.insert(log);
    }
}
