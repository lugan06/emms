package com.eems.common.api;

public class Result<T> {

    private String code;
    private String message;
    private T data;
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
