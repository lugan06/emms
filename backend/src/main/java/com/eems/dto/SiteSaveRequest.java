package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

@Schema(description = "新建或更新站点；更新时编码须与原值一致")
public record SiteSaveRequest(
        @Schema(description = "站点名称") @NotBlank @Size(max = 200) String name,
        @Schema(description = "唯一且不可修改的站点编码，小写字母、数字、下划线及连字符", example = "expo-2026")
        @NotBlank @Pattern(regexp = "[a-z0-9][a-z0-9_-]{0,49}") String code,
        @Schema(description = "完整域名列表，至少一条，且恰好一个主域名")
        @NotEmpty @Size(max = 50) List<@NotNull @Valid SiteDomainRequest> domains) {
}

