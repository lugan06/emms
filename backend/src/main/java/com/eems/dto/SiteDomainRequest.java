package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "绑定域名，不包含协议、端口或路径")
public record SiteDomainRequest(
        @Schema(description = "ASCII 域名，国际化域名请使用 punycode", example = "expo.example.com")
        @NotBlank @Size(max = 253) String domain,
        @Schema(description = "是否主域名，每个站点必须且只能有一个")
        @NotNull Boolean primary) {
}

