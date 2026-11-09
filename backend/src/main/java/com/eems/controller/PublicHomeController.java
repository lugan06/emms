package com.eems.controller;

import com.eems.common.api.Result;
import com.eems.service.PublicHomeService;
import com.eems.vo.PublicHomeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/home")
@Tag(name = "公共首页", description = "公共首页及展会概览子栏目聚合数据")
public class PublicHomeController {
    private final PublicHomeService service;

    public PublicHomeController(PublicHomeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "获取公共首页聚合数据", description = "返回站点、公司、当前展会、兼容的固定模块及 overviewSections；概览按启用子栏目排序，返回当前展会及未关联展会的全部公开内容，正文经过安全过滤")
    public Result<PublicHomeVO> home() {
        return Result.success(service.home());
    }
}
