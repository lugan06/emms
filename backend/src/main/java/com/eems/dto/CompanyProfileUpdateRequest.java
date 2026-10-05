package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "公司信息更新请求")
public record CompanyProfileUpdateRequest(
        @NotBlank @Size(max = 200) @Schema(description = "公司名称", requiredMode = Schema.RequiredMode.REQUIRED) String companyName,
        @Size(max = 500) String address,
        @Size(max = 20) String postalCode,
        @Size(max = 100) String contactName,
        @Size(max = 30) String mobile,
        @Size(max = 30) String telephone,
        @Size(max = 30) String fax,
        @Email @Size(max = 150) String email,
        @Size(max = 50) String qq,
        @Size(max = 500) String wechatImageUrl,
        @Size(max = 100) String businessLicenseNo,
        String otherInfo) {
}
