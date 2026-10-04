package com.eems.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eemsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EEMS 展会管理系统 API")
                        .version("v1.0.0")
                        .description("EEMS 管理端与公共浏览端接口文档。管理端接口使用 JWT Bearer Token 鉴权。"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("登录接口返回的 JWT。请求时填写 token，Swagger UI 会自动添加 Authorization: Bearer <token>。")));
    }
}
