package com.eems.controller;

import com.eems.common.api.Result;
import com.eems.dto.GuestbookCreateRequest;
import com.eems.service.GuestbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/guestbook")
@Tag(name = "公共留言", description = "公共浏览端在线留言提交")
public class PublicGuestbookController {
    private final GuestbookService service;

    public PublicGuestbookController(GuestbookService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "提交在线留言", description = "提交后进入未读状态，公共端不会返回管理字段")
    public Result<Void> create(@Valid @RequestBody GuestbookCreateRequest request,
                                HttpServletRequest httpRequest) {
        service.create(request, httpRequest.getRemoteAddr());
        return Result.success();
    }
}
