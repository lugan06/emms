package com.eems.service;

public interface OperationLogService {
    void success(String username, String operation, String requestUrl, String params);

    void success(String module, String username, String operation, String requestUrl, String params);
}
