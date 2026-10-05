package com.eems.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO) private Long id;
    private Long adminUserId;
    private String username;
    private String module;
    private String operation;
    private String requestMethod;
    private String requestUrl;
    private String requestIp;
    private String requestParams;
    private String result;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic private Integer deleted;
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public Long getAdminUserId() { return adminUserId; } public void setAdminUserId(Long v) { adminUserId = v; }
    public String getUsername() { return username; } public void setUsername(String v) { username = v; }
    public String getModule() { return module; } public void setModule(String v) { module = v; }
    public String getOperation() { return operation; } public void setOperation(String v) { operation = v; }
    public String getRequestMethod() { return requestMethod; } public void setRequestMethod(String v) { requestMethod = v; }
    public String getRequestUrl() { return requestUrl; } public void setRequestUrl(String v) { requestUrl = v; }
    public String getRequestIp() { return requestIp; } public void setRequestIp(String v) { requestIp = v; }
    public String getRequestParams() { return requestParams; } public void setRequestParams(String v) { requestParams = v; }
    public String getResult() { return result; } public void setResult(String v) { result = v; }
    public String getErrorMessage() { return errorMessage; } public void setErrorMessage(String v) { errorMessage = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { updatedAt = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { deleted = v; }
}
