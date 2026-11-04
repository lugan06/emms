package com.eems.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;
import com.eems.site.SiteRequestPolicy;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eemsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EEMS 展会管理系统 API")
                        .version("v2.0.0")
                        .description("EEMS 管理端与公共浏览端接口文档。管理端接口使用 JWT Bearer Token 鉴权。"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("登录接口返回的 JWT。请求时填写 token，Swagger UI 会自动添加 Authorization: Bearer <token>。")));
    }

    @Bean
    public OpenApiCustomizer siteOverrideDocumentation() {
        return api -> api.getPaths().forEach((path, item) -> {
            if (SiteRequestPolicy.allowsOverride(path)) {
                item.readOperations().forEach(operation -> operation.addParametersItem(new Parameter()
                        .name(SiteRequestPolicy.HEADER).in("header").required(false)
                        .description("仅 SUPER_ADMIN 可用，单个正整数站点 ID；目标须存在，允许维护停用站点。普通管理员携带返回 403。")
                        .schema(new StringSchema().pattern("^[1-9][0-9]{0,18}$"))));
            }
        });
    }
}
