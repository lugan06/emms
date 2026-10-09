package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.common.api.PageResult;
import com.eems.common.exception.BusinessException;
import com.eems.dto.FileAssetPageQuery;
import com.eems.entity.AdminUser;
import com.eems.entity.FileAsset;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.FileAssetMapper;
import com.eems.service.FileAssetService;
import com.eems.service.FileStorageService;
import com.eems.service.OperationLogService;
import com.eems.vo.FileAssetVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileAssetServiceImpl implements FileAssetService {
    private final FileAssetMapper fileAssetMapper;
    private final AdminUserMapper adminUserMapper;
    private final FileStorageService fileStorageService;
    private final OperationLogService operationLogService;

    public FileAssetServiceImpl(FileAssetMapper fileAssetMapper, AdminUserMapper adminUserMapper,
                                FileStorageService fileStorageService) {
        this(fileAssetMapper, adminUserMapper, fileStorageService, null);
    }

    @Autowired
    public FileAssetServiceImpl(FileAssetMapper fileAssetMapper, AdminUserMapper adminUserMapper,
                                FileStorageService fileStorageService, OperationLogService operationLogService) {
        this.fileAssetMapper = fileAssetMapper;
        this.adminUserMapper = adminUserMapper;
        this.fileStorageService = fileStorageService;
        this.operationLogService = operationLogService;
    }

    @Override
    @Transactional
    public FileAssetVO upload(MultipartFile file, String username) {
        AdminUser uploader = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username)
                .eq(AdminUser::getStatus, "ENABLED"));
        if (uploader == null) {
            throw new BusinessException("ADMIN_NOT_FOUND", "管理员不存在或已禁用");
        }

        FileStorageService.StoredFile storedFile = fileStorageService.store(file);
        try {
            FileAsset asset = new FileAsset();
            asset.setOriginalName(storedFile.originalName());
            asset.setStorageName(storedFile.storageName());
            asset.setFileUrl(storedFile.fileUrl());
            asset.setFileType(storedFile.contentType());
            asset.setFileSize(storedFile.size());
            asset.setFileHash(storedFile.sha256());
            asset.setUploadedBy(uploader.getId());
            if (fileAssetMapper.insert(asset) != 1) {
                throw new BusinessException("FILE_METADATA_SAVE_FAILED", "文件元数据保存失败");
            }
            AuditSupport.success(operationLogService, "FILE_ASSET", username, "上传文件", "POST",
                    "/api/admin/files", "{\"id\":" + asset.getId() + "}");
            return FileAssetVO.from(asset);
        } catch (RuntimeException exception) {
            fileStorageService.remove(storedFile);
            throw exception;
        }
    }

    @Override
    public PageResult<FileAssetVO> page(FileAssetPageQuery query) {
        Page<FileAsset> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<FileAsset> wrapper = new LambdaQueryWrapper<FileAsset>()
                .orderByDesc(FileAsset::getCreatedAt);
        if (StringUtils.hasText(query.getFileType())) {
            wrapper.eq(FileAsset::getFileType, query.getFileType().trim());
        }
        Page<FileAsset> result = fileAssetMapper.selectPage(page, wrapper);
        List<FileAssetVO> records = result.getRecords().stream().map(FileAssetVO::from).toList();
        return new PageResult<>(records, result.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        delete(id, null);
    }

    @Override
    @Transactional
    public void delete(Long id, String username) {
        FileAsset asset = fileAssetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("FILE_NOT_FOUND", "文件资源不存在");
        }
        if (fileAssetMapper.deleteById(id) != 1) {
            throw new BusinessException("FILE_DELETE_FAILED", "文件删除失败");
        }
        AuditSupport.success(operationLogService, "FILE_ASSET", username, "删除文件", "DELETE",
                "/api/admin/files/" + id, "{\"id\":" + id + "}");
    }
}
