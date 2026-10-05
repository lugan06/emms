package com.eems.vo;

import com.eems.entity.CompanyProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "公共端公司信息")
public record PublicCompanyVO(
        @Schema(description = "公司名称", example = "上海艾姆斯展览服务有限公司") String companyName,
        @Schema(description = "公司地址", nullable = true) String address,
        @Schema(description = "邮政编码", nullable = true) String postalCode,
        @Schema(description = "公开联系人", nullable = true) String contactName,
        @Schema(description = "公开联系电话", nullable = true) String telephone,
        @Schema(description = "公开邮箱", nullable = true) String email,
        @Schema(description = "微信二维码地址", nullable = true) String wechatImageUrl) {

    public static PublicCompanyVO from(CompanyProfile value) {
        return new PublicCompanyVO(value.getCompanyName(), value.getAddress(), value.getPostalCode(),
                value.getContactName(), value.getTelephone(), value.getEmail(), value.getWechatImageUrl());
    }
}
