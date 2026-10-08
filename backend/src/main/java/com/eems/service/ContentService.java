package com.eems.service;

import com.eems.common.api.PageResult;
import com.eems.dto.ContentPageQuery;
import com.eems.dto.ContentSaveRequest;
import com.eems.vo.ContentVO;

public interface ContentService {
    PageResult<ContentVO> page(ContentPageQuery query);
    ContentVO get(Long id);
    ContentVO create(ContentSaveRequest request, String username);
    ContentVO update(Long id, ContentSaveRequest request, String username);
    void delete(Long id, String username);
    ContentVO publish(Long id, String username);
    ContentVO offline(Long id, String username);
}
