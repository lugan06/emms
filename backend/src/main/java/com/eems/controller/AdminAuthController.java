package com.eems.controller;

import com.eems.common.api.Result;
import com.eems.dto.AdminLoginRequest;
import com.eems.dto.AdminPasswordChangeRequest;
import com.eems.service.AdminAuthService;
import com.eems.vo.AdminLoginResponse;
import com.eems.vo.AdminUserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员认证", description = "管理员登录与身份认证接口")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "管理员登录",
            description = "使用管理员账号和密码登录，成功后返回 JWT。token 可用于后续 /api/admin/** 接口。",
            operationId = "adminLogin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "账号或密码错误，或请求参数不合法",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/Result"))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Result<AdminLoginResponse>> login(@Valid @RequestBody AdminLoginRequest request,
                                                            @Parameter(hidden = true, in = ParameterIn.HEADER)
                                                            HttpServletRequest servletRequest) {
        AdminLoginResponse loginResponse = adminAuthService.login(request, resolveClientIp(servletRequest));
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, loginResponse.getTokenType() + " " + loginResponse.getToken())
                .body(Result.success(loginResponse));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前管理员", description = "获取当前 JWT 对应的管理员基本信息")
    @SecurityRequirement(name = "bearerAuth")
    public Result<AdminUserVO> me(Authentication authentication) {
        return Result.success(adminAuthService.currentUser(authentication.getName()));
    }

    @PutMapping("/password")
    @Operation(summary = "修改管理员密码", description = "修改成功后前端应清除旧 token 并重新登录")
    @SecurityRequirement(name = "bearerAuth")
    public Result<Void> changePassword(@Valid @RequestBody AdminPasswordChangeRequest request,
                                       Authentication authentication) {
        adminAuthService.changePassword(authentication.getName(), request);
        return Result.success();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
