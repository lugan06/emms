package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.common.api.PageResult;
import com.eems.common.exception.BusinessException;
import com.eems.dto.OperationLogPageQuery;
import com.eems.entity.AdminUser;
import com.eems.entity.OperationLog;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.OperationLogMapper;
import com.eems.service.OperationLogService;
import com.eems.vo.OperationLogVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

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
        success(module, username, operation, "POST", requestUrl, null, params);
    }

    @Override
    public void success(String module, String username, String operation, String requestMethod,
                        String requestUrl, String requestIp, String params) {
        AdminUser user = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username));
        OperationLog log = new OperationLog();
        log.setAdminUserId(user == null ? null : user.getId());
        log.setUsername(username);
        log.setModule(module);
        log.setOperation(operation);
        log.setRequestMethod(requestMethod);
        log.setRequestUrl(requestUrl);
        log.setRequestIp(requestIp);
        log.setRequestParams(sanitize(params));
        log.setResult("SUCCESS");
        mapper.insert(log);
    }

    @Override
    public void failure(String module, String username, String operation, String requestMethod,
                        String requestUrl, String requestIp, String params, String errorMessage) {
        AdminUser user = username == null ? null : adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username));
        OperationLog log = new OperationLog();
        log.setAdminUserId(user == null ? null : user.getId());
        log.setUsername(username);
        log.setModule(module);
        log.setOperation(operation);
        log.setRequestMethod(requestMethod);
        log.setRequestUrl(requestUrl);
        log.setRequestIp(requestIp);
        log.setRequestParams(sanitize(params));
        log.setResult("FAILURE");
        log.setErrorMessage(sanitize(errorMessage));
        mapper.insert(log);
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String masked = value
                .replaceAll("(?i)(\\\"?(?:password|oldPassword|newPassword|phone|mobile|email|message|replyContent|ipAddress)\\\"?\\s*:\\s*\\\")([^\\\"]*)(\\\")", "$1***$3")
                .replaceAll("(?i)(password|oldPassword|newPassword|phone|mobile|email|message|replyContent|ipAddress)\\s*=\\s*[^,;\\s]+", "$1=***");
        return masked.replaceAll("(?<![0-9])1[3-9][0-9]{9}(?![0-9])", "***PHONE***")
                .replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "***@***");
    }

    @Override
    public PageResult<OperationLogVO> page(OperationLogPageQuery query) {
        Page<OperationLog> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreatedAt)
                .orderByDesc(OperationLog::getId);
        if (StringUtils.hasText(query.getUsername())) {
            wrapper.eq(OperationLog::getUsername, query.getUsername().trim());
        }
        if (StringUtils.hasText(query.getModule())) {
            wrapper.eq(OperationLog::getModule, query.getModule().trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(query.getOperation())) {
            wrapper.like(OperationLog::getOperation, query.getOperation().trim());
        }
        if (StringUtils.hasText(query.getResult())) {
            String result = query.getResult().trim().toUpperCase(Locale.ROOT);
            if (!"SUCCESS".equals(result) && !"FAILURE".equals(result)) {
                throw new BusinessException("OPERATION_LOG_RESULT_INVALID", "操作结果必须是 SUCCESS 或 FAILURE");
            }
            wrapper.eq(OperationLog::getResult, result);
        }
        if (query.getStartAt() != null) {
            wrapper.ge(OperationLog::getCreatedAt, query.getStartAt());
        }
        if (query.getEndAt() != null) {
            wrapper.le(OperationLog::getCreatedAt, query.getEndAt());
        }
        Page<OperationLog> result = mapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords().stream().map(OperationLogVO::from).toList(),
                result.getTotal(), query.getPage(), query.getPageSize());
    }
}
