package com.eems.controller;

import com.eems.common.api.PageResult;
import com.eems.common.api.Result;
import com.eems.dto.FileAssetPageQuery;
import com.eems.service.FileAssetService;
import com.eems.vo.FileAssetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/files")
@Tag(name = "文件资源", description = "本地文件上传、查询和逻辑删除")
@SecurityRequirement(name = "bearerAuth")
public class FileAssetController {
    private final FileAssetService fileAssetService;

    public FileAssetController(FileAssetService fileAssetService) {
        this.fileAssetService = fileAssetService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", description = "上传图片、常用文档或 MP4 视频，返回文件资源 ID 和访问地址")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "上传成功"),
            @ApiResponse(responseCode = "400", description = "文件为空、超过大小限制、类型或内容不合法，或文件存储目录不可写",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ResultVoid"))),
            @ApiResponse(responseCode = "401", description = "未登录",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ResultVoid"))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ResultVoid")))
    })
    public Result<FileAssetVO> upload(
            @Parameter(description = "待上传文件", required = true)
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        return Result.success(fileAssetService.upload(file, authentication.getName()));
    }

    @GetMapping
    @Operation(summary = "分页查询文件资源")
    public Result<PageResult<FileAssetVO>> page(@Valid @ModelAttribute FileAssetPageQuery query) {
        return Result.success(fileAssetService.page(query));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "逻辑删除文件资源", description = "只删除数据库可见性，不直接删除物理文件")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        fileAssetService.delete(id, authentication == null ? null : authentication.getName());
        return Result.success();
    }
}
