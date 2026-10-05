package com.eems.controller;

import com.eems.common.api.Result;
import com.eems.service.CompanyProfileService;
import com.eems.service.SiteConfigService;
import com.eems.vo.PublicCompanyVO;
import com.eems.vo.PublicSiteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@Tag(name = "公共站点", description = "公共浏览端站点和公司信息")
public class PublicSiteController {

    private final SiteConfigService siteConfigService;
    private final CompanyProfileService companyProfileService;

    public PublicSiteController(SiteConfigService siteConfigService, CompanyProfileService companyProfileService) {
        this.siteConfigService = siteConfigService;
        this.companyProfileService = companyProfileService;
    }

    @GetMapping("/site")
    @Operation(summary = "获取公共站点信息", description = "只返回公共浏览端需要的站点字段，不包含统计代码和内部配置")
    public Result<PublicSiteVO> site() {
        return Result.success(siteConfigService.getPublicDefault());
    }

    @GetMapping("/company")
    @Operation(summary = "获取公共公司信息", description = "只返回公共浏览端需要的公司联系字段")
    public Result<PublicCompanyVO> company() {
        return Result.success(companyProfileService.getPublicDefault());
    }
}
