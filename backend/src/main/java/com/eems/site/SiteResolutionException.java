package com.eems.site;

public class SiteResolutionException extends RuntimeException {
    private final String code;
    private final int status;

    public SiteResolutionException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
