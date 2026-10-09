package com.eems.service;

import com.eems.common.api.PageResult;
import com.eems.dto.GuestbookCreateRequest;
import com.eems.dto.GuestbookPageQuery;
import com.eems.dto.GuestbookReplyRequest;
import com.eems.vo.GuestbookVO;

public interface GuestbookService {
    void create(GuestbookCreateRequest request, String clientIp);

    PageResult<GuestbookVO> page(GuestbookPageQuery query);

    GuestbookVO markRead(Long id);

    GuestbookVO reply(Long id, GuestbookReplyRequest request, String username);

    GuestbookVO close(Long id);
}
