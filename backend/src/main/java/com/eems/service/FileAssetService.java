package com.eems.service;

import com.eems.common.api.PageResult;
import com.eems.dto.FileAssetPageQuery;
import com.eems.vo.FileAssetVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileAssetService {
    FileAssetVO upload(MultipartFile file, String username);

    PageResult<FileAssetVO> page(FileAssetPageQuery query);

    void delete(Long id);
}
