package com.eems.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "统一接口响应体")
public class Result<T> {

    @Schema(description = "响应编码", example = "OK")
    private String code;

    @Schema(description = "响应消息", example = "success")
    private String message;

    @Schema(description = "业务数据")
    private T data;

    @Schema(description = "链路追踪 ID", nullable = true)
    private String traceId;

    public Result() {
    }

    public Result(String code, String message, T data, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>("OK", "success", data, null);
    }

    public static Result<Void> success() {
        return new Result<Void>("OK", "success", null, null);
    }

    public static <T> Result<T> failure(String code, String message, String traceId) {
        return new Result<T>(code, message, null, traceId);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
