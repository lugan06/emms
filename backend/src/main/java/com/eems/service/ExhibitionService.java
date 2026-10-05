package com.eems.service;

import com.eems.common.api.PageResult;
import com.eems.dto.ExhibitionPageQuery;
import com.eems.dto.ExhibitionSaveRequest;
import com.eems.vo.ExhibitionVO;

public interface ExhibitionService {
    PageResult<ExhibitionVO> page(ExhibitionPageQuery query);
    ExhibitionVO get(Long id);
    ExhibitionVO create(ExhibitionSaveRequest request, String username);
    ExhibitionVO update(Long id, ExhibitionSaveRequest request, String username);
    void delete(Long id, String username);
    ExhibitionVO restore(Long id, String username);
    ExhibitionVO publish(Long id, String username);
    ExhibitionVO republish(Long id, String username);
    ExhibitionVO offline(Long id, String username);
    ExhibitionVO setCurrent(Long id, String username);
}
