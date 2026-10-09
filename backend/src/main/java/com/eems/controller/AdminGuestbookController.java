package com.eems.controller;

import com.eems.common.api.PageResult;
import com.eems.common.api.Result;
import com.eems.dto.GuestbookPageQuery;
import com.eems.dto.GuestbookReplyRequest;
import com.eems.service.GuestbookService;
import com.eems.vo.GuestbookVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/guestbooks")
@Tag(name = "留言管理", description = "在线留言查询、回复和关闭")
@SecurityRequirement(name = "bearerAuth")
public class AdminGuestbookController {
    private final GuestbookService service;

    public AdminGuestbookController(GuestbookService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "分页查询在线留言")
    public Result<PageResult<GuestbookVO>> page(@Valid @ModelAttribute GuestbookPageQuery query) {
        return Result.success(service.page(query));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "标记留言为已读")
    public Result<GuestbookVO> markRead(
            @Parameter(description = "留言 ID", example = "1") @PathVariable Long id) {
        return Result.success(service.markRead(id));
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "回复在线留言")
    public Result<GuestbookVO> reply(
            @Parameter(description = "留言 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody GuestbookReplyRequest request,
            Authentication authentication) {
        return Result.success(service.reply(id, request, authentication.getName()));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "关闭在线留言")
    public Result<GuestbookVO> close(
            @Parameter(description = "留言 ID", example = "1") @PathVariable Long id) {
        return Result.success(service.close(id));
    }
}
