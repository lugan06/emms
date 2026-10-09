package com.eems.service.impl;

import com.eems.service.OperationLogService;

final class AuditSupport {
    private AuditSupport() {
    }

    static void success(OperationLogService service, String module, String username, String operation,
                        String method, String url, String params) {
        if (service != null) {
            service.success(module, username, operation, method, url, null, params);
        }
    }
}
