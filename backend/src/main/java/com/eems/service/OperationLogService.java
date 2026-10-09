package com.eems.service;

import com.eems.common.api.PageResult;
import com.eems.dto.OperationLogPageQuery;
import com.eems.vo.OperationLogVO;

public interface OperationLogService {
    void success(String username, String operation, String requestUrl, String params);

    void success(String module, String username, String operation, String requestUrl, String params);

    void success(String module, String username, String operation, String requestMethod,
                 String requestUrl, String requestIp, String params);

    void failure(String module, String username, String operation, String requestMethod,
                 String requestUrl, String requestIp, String params, String errorMessage);

    PageResult<OperationLogVO> page(OperationLogPageQuery query);
}
