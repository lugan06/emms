package com.eems.security;

import com.eems.common.api.Result;
import com.eems.service.OperationLogService;
import com.eems.site.SiteContext;
import com.eems.site.SiteRequestPolicy;
import com.eems.util.TraceIdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SecurityFailureHandler {
    private final ObjectMapper objectMapper;
    private final OperationLogService logs;

    public SecurityFailureHandler(ObjectMapper objectMapper, OperationLogService logs) {
        this.objectMapper = objectMapper;
        this.logs = logs;
    }

    public void reject(HttpServletRequest request, HttpServletResponse response, int status,
                       String code, String message, String username) throws IOException {
        // Whitelist only context metadata: never store JWTs, passwords or raw headers/body.
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("siteId", SiteContext.currentSiteIdOrNull());
        params.put("siteOverridePresent", request.getHeader(SiteRequestPolicy.HEADER) != null);
        logs.failure("AUTH", username, "请求拒绝", request.getMethod(), SiteRequestPolicy.path(request),
                request.getRemoteAddr(), objectMapper.writeValueAsString(params), code);
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.failure(code, message, TraceIdUtil.currentOrCreate()));
    }
}
