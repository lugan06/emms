package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "在线留言提交参数")
public class GuestbookCreateRequest {
    @NotBlank(message = "name 不能为空")
    @Size(max = 100, message = "name 长度不能超过 100")
    @Schema(description = "留言人姓名", example = "张先生", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 30, message = "phone 长度不能超过 30")
    @Pattern(regexp = "^[0-9+()\\-\\s]{6,30}$", message = "phone 格式不正确")
    @Schema(description = "联系电话，可为空", example = "13800138000", nullable = true)
    private String phone;

    @Email(message = "email 格式不正确")
    @Size(max = 150, message = "email 长度不能超过 150")
    @Schema(description = "联系邮箱，可为空", example = "contact@example.com", nullable = true)
    private String email;

    @Size(max = 200, message = "companyName 长度不能超过 200")
    @Schema(description = "公司名称，可为空", example = "示例科技有限公司", nullable = true)
    private String companyName;

    @NotBlank(message = "message 不能为空")
    @Size(max = 2000, message = "message 长度不能超过 2000")
    @Schema(description = "留言内容", example = "请问如何申请展位？", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
