package com.eems.service;

import com.eems.common.api.PageResult;
import com.eems.dto.PublicContentPageQuery;
import com.eems.vo.PublicCategoryVO;
import com.eems.vo.PublicContentListVO;
import com.eems.vo.PublicContentVO;

import java.util.List;

public interface PublicContentService {
    List<PublicCategoryVO> categoryTree();
    PageResult<PublicContentListVO> pageByCategory(String code, PublicContentPageQuery query);
    PublicContentVO get(Long id);
    List<PublicContentListVO> topByCategory(String code, Long exhibitionId, int limit);
}
