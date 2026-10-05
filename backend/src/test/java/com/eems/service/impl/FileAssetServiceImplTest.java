package com.eems.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.dto.FileAssetPageQuery;
import com.eems.entity.AdminUser;
import com.eems.entity.FileAsset;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.FileAssetMapper;
import com.eems.service.FileStorageService;
import com.eems.vo.FileAssetVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileAssetServiceImplTest {
    @Test
    void uploadShouldPersistMetadataAndUploader() {
        FileAssetMapper mapper = mock(FileAssetMapper.class);
        AdminUserMapper adminMapper = mock(AdminUserMapper.class);
        FileStorageService storage = mock(FileStorageService.class);
        AdminUser admin = new AdminUser();
        admin.setId(7L);
        admin.setUsername("admin");
        admin.setStatus("ENABLED");
        when(adminMapper.selectOne(any())).thenReturn(admin);
        when(storage.store(any())).thenReturn(new FileStorageService.StoredFile(
                "logo.png", "random.png", "/uploads/random.png", "image/png", 10, "hash"));
        when(mapper.insert(any(FileAsset.class))).thenAnswer(invocation -> {
            FileAsset asset = invocation.getArgument(0);
            asset.setId(3L);
            return 1;
        });

        FileAssetVO result = new FileAssetServiceImpl(mapper, adminMapper, storage).upload(
                new MockMultipartFile("file", "logo.png", "image/png", new byte[]{1}), "admin");

        ArgumentCaptor<FileAsset> captor = ArgumentCaptor.forClass(FileAsset.class);
        verify(mapper).insert(captor.capture());
        assertEquals(7L, captor.getValue().getUploadedBy());
        assertEquals("/uploads/random.png", result.fileUrl());
    }

    @Test
    void pageShouldReturnMappedRecords() {
        FileAssetMapper mapper = mock(FileAssetMapper.class);
        AdminUserMapper adminMapper = mock(AdminUserMapper.class);
        FileStorageService storage = mock(FileStorageService.class);
        FileAsset asset = new FileAsset();
        asset.setId(2L);
        asset.setOriginalName("manual.pdf");
        asset.setFileType("application/pdf");
        Page<FileAsset> result = new Page<>(1, 20);
        result.setRecords(java.util.List.of(asset));
        result.setTotal(1);
        when(mapper.selectPage(any(), any())).thenReturn(result);

        FileAssetPageQuery query = new FileAssetPageQuery();
        PageResultHolder holder = new PageResultHolder(new FileAssetServiceImpl(mapper, adminMapper, storage).page(query));

        assertEquals(1, holder.result().getTotal());
        assertEquals("manual.pdf", holder.result().getRecords().get(0).originalName());
    }

    @Test
    void deleteShouldUseLogicalDelete() {
        FileAssetMapper mapper = mock(FileAssetMapper.class);
        FileAsset asset = new FileAsset();
        asset.setId(9L);
        when(mapper.selectById(9L)).thenReturn(asset);
        when(mapper.deleteById(9L)).thenReturn(1);

        new FileAssetServiceImpl(mapper, mock(AdminUserMapper.class), mock(FileStorageService.class)).delete(9L);

        verify(mapper).deleteById(9L);
    }

    private record PageResultHolder(com.eems.common.api.PageResult<FileAssetVO> result) {
    }
}
