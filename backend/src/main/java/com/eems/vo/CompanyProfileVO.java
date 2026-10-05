package com.eems.vo;

import com.eems.entity.CompanyProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "公司信息")
public record CompanyProfileVO(Long id, String profileCode, String companyName, String address,
                               String postalCode, String contactName, String mobile, String telephone,
                               String fax, String email, String qq, String wechatImageUrl,
                               String businessLicenseNo, String otherInfo) {
    public static CompanyProfileVO from(CompanyProfile value) {
        return new CompanyProfileVO(value.getId(), value.getProfileCode(), value.getCompanyName(),
                value.getAddress(), value.getPostalCode(), value.getContactName(), value.getMobile(),
                value.getTelephone(), value.getFax(), value.getEmail(), value.getQq(),
                value.getWechatImageUrl(), value.getBusinessLicenseNo(), value.getOtherInfo());
    }
}
