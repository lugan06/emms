package com.eems.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.common.exception.BusinessException;
import com.eems.dto.GuestbookCreateRequest;
import com.eems.dto.GuestbookPageQuery;
import com.eems.dto.GuestbookReplyRequest;
import com.eems.entity.AdminUser;
import com.eems.entity.Guestbook;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.GuestbookMapper;
import com.eems.vo.GuestbookVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class GuestbookServiceImplTest {
    private final GuestbookMapper mapper = mock(GuestbookMapper.class);
    private final AdminUserMapper adminUserMapper = mock(AdminUserMapper.class);
    private final GuestbookServiceImpl service = new GuestbookServiceImpl(mapper, adminUserMapper);

    @Test
    void createShouldStoreUnreadMessageAndClientIp() {
        when(mapper.insert(any(Guestbook.class))).thenAnswer(invocation -> {
            Guestbook value = invocation.getArgument(0);
            value.setId(1L);
            return 1;
        });

        service.create(createRequest(), " 192.0.2.10 ");

        var captor = org.mockito.ArgumentCaptor.forClass(Guestbook.class);
        verify(mapper).insert(captor.capture());
        Guestbook value = captor.getValue();
        assertEquals("张先生", value.getName());
        assertEquals("13800138000", value.getPhone());
        assertEquals("UNREAD", value.getStatus());
        assertEquals("192.0.2.10", value.getIpAddress());
    }

    @Test
    void createShouldRejectMoreThanFiveMessagesFromSameIpInTenMinutes() {
        when(mapper.insert(any(Guestbook.class))).thenReturn(1);

        for (int i = 0; i < 5; i++) {
            service.create(createRequest(), "192.0.2.20");
        }

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create(createRequest(), "192.0.2.20"));

        assertEquals("TOO_MANY_REQUESTS", exception.getCode());
        verify(mapper, times(5)).insert(any(Guestbook.class));
    }

    @Test
    void pageShouldFilterByStatusAndMaskPrivateFields() {
        Guestbook value = guestbook(1L, "UNREAD");
        value.setPhone("13800138000");
        value.setEmail("contact@example.com");
        value.setIpAddress("192.0.2.10");
        Page<Guestbook> page = new Page<>(1, 20);
        page.setRecords(List.of(value));
        page.setTotal(1);
        when(mapper.selectPage(any(), any())).thenReturn(page);

        GuestbookPageQuery query = new GuestbookPageQuery();
        query.setStatus("UNREAD");

        GuestbookVO result = service.page(query).getRecords().get(0);

        assertEquals("138****8000", result.phone());
        assertEquals("c***@example.com", result.email());
        assertEquals("192.0.2.*", result.ipAddress());
        verify(mapper).selectPage(any(), any());
    }

    @Test
    void markReadShouldOnlyAllowUnreadMessage() {
        Guestbook value = guestbook(2L, "UNREAD");
        when(mapper.selectById(2L)).thenReturn(value);
        when(mapper.updateById(value)).thenReturn(1);

        GuestbookVO result = service.markRead(2L);

        assertEquals("READ", result.status());
        assertEquals("READ", value.getStatus());
        verify(mapper).updateById(value);
    }

    @Test
    void markReadShouldRejectAlreadyProcessedMessage() {
        when(mapper.selectById(3L)).thenReturn(guestbook(3L, "REPLIED"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.markRead(3L));

        assertEquals("GUESTBOOK_INVALID_STATUS", exception.getCode());
        verify(mapper, never()).updateById(any(Guestbook.class));
    }

    @Test
    void replyShouldStoreAdminAndReplyTime() {
        Guestbook value = guestbook(4L, "READ");
        AdminUser admin = new AdminUser();
        admin.setId(8L);
        admin.setUsername("admin");
        admin.setStatus("ENABLED");
        when(mapper.selectById(4L)).thenReturn(value);
        when(adminUserMapper.selectOne(any())).thenReturn(admin);
        when(mapper.updateById(value)).thenReturn(1);

        GuestbookReplyRequest request = new GuestbookReplyRequest();
        request.setReplyContent("  您好，展位申请已开放。  ");

        GuestbookVO result = service.reply(4L, request, "admin");

        assertEquals("REPLIED", result.status());
        assertEquals("您好，展位申请已开放。", value.getReplyContent());
        assertEquals(8L, value.getRepliedBy());
        assertEquals("REPLIED", result.status());
        verify(mapper).updateById(value);
    }

    @Test
    void replyShouldRejectClosedMessage() {
        when(mapper.selectById(5L)).thenReturn(guestbook(5L, "CLOSED"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reply(5L, replyRequest(), "admin"));

        assertEquals("GUESTBOOK_CLOSED", exception.getCode());
        verify(adminUserMapper, never()).selectOne(any());
        verify(mapper, never()).updateById(any(Guestbook.class));
    }

    @Test
    void closeShouldUpdateStatus() {
        Guestbook value = guestbook(6L, "REPLIED");
        when(mapper.selectById(6L)).thenReturn(value);
        when(mapper.updateById(value)).thenReturn(1);

        GuestbookVO result = service.close(6L);

        assertEquals("CLOSED", result.status());
        verify(mapper).updateById(value);
    }

    private GuestbookCreateRequest createRequest() {
        GuestbookCreateRequest request = new GuestbookCreateRequest();
        request.setName("张先生");
        request.setPhone("13800138000");
        request.setEmail("contact@example.com");
        request.setCompanyName("示例科技");
        request.setMessage("请问如何申请展位？");
        return request;
    }

    private GuestbookReplyRequest replyRequest() {
        GuestbookReplyRequest request = new GuestbookReplyRequest();
        request.setReplyContent("回复内容");
        return request;
    }

    private Guestbook guestbook(Long id, String status) {
        Guestbook value = new Guestbook();
        value.setId(id);
        value.setName("张先生");
        value.setMessage("留言内容");
        value.setStatus(status);
        return value;
    }
}
