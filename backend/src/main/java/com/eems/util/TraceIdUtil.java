package com.eems.util;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceIdUtil {

    public static final String TRACE_ID = "traceId";

    private TraceIdUtil() {
    }

    public static String currentOrCreate() {
        String current = MDC.get(TRACE_ID);
        if (current != null && !current.isBlank()) {
            return current;
        }
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID, traceId);
        return traceId;
    }
}
