package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.common.api.PageResult;
import com.eems.common.exception.BusinessException;
import com.eems.dto.GuestbookCreateRequest;
import com.eems.dto.GuestbookPageQuery;
import com.eems.dto.GuestbookReplyRequest;
import com.eems.entity.AdminUser;
import com.eems.entity.Guestbook;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.GuestbookMapper;
import com.eems.service.GuestbookService;
import com.eems.service.OperationLogService;
import com.eems.vo.GuestbookVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GuestbookServiceImpl implements GuestbookService {
    private static final String UNREAD = "UNREAD";
    private static final String READ = "READ";
    private static final String REPLIED = "REPLIED";
    private static final String CLOSED = "CLOSED";
    private static final int MAX_SUBMISSIONS_PER_IP = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(10);

    private final GuestbookMapper guestbookMapper;
    private final AdminUserMapper adminUserMapper;
    private final OperationLogService operationLogService;
    private final ConcurrentHashMap<String, RateWindow> rateWindows = new ConcurrentHashMap<>();

    public GuestbookServiceImpl(GuestbookMapper guestbookMapper, AdminUserMapper adminUserMapper) {
        this(guestbookMapper, adminUserMapper, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public GuestbookServiceImpl(GuestbookMapper guestbookMapper, AdminUserMapper adminUserMapper,
                                OperationLogService operationLogService) {
        this.guestbookMapper = guestbookMapper;
        this.adminUserMapper = adminUserMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    @Transactional
    public void create(GuestbookCreateRequest request, String clientIp) {
        String ip = StringUtils.hasText(clientIp) ? clientIp.trim() : "unknown";
        checkRateLimit(ip);

        Guestbook guestbook = new Guestbook();
        guestbook.setName(trim(request.getName()));
        guestbook.setPhone(trimToNull(request.getPhone()));
        guestbook.setEmail(trimToNull(request.getEmail()));
        guestbook.setCompanyName(trimToNull(request.getCompanyName()));
        guestbook.setMessage(trim(request.getMessage()));
        guestbook.setStatus(UNREAD);
        guestbook.setIpAddress(ip);
        guestbookMapper.insert(guestbook);
    }

    @Override
    public PageResult<GuestbookVO> page(GuestbookPageQuery query) {
        Page<Guestbook> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Guestbook> wrapper = new LambdaQueryWrapper<Guestbook>()
                .orderByDesc(Guestbook::getCreatedAt)
                .orderByDesc(Guestbook::getId);
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Guestbook::getStatus, query.getStatus().trim());
        }
        Page<Guestbook> result = guestbookMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords().stream().map(GuestbookVO::from).toList(),
                result.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional
    public GuestbookVO markRead(Long id) {
        return markRead(id, null);
    }

    @Override
    @Transactional
    public GuestbookVO markRead(Long id, String username) {
        Guestbook guestbook = find(id);
        if (!UNREAD.equals(guestbook.getStatus())) {
            throw new BusinessException("GUESTBOOK_INVALID_STATUS", "只有未读留言可以标记为已读");
        }
        guestbook.setStatus(READ);
        guestbookMapper.updateById(guestbook);
        AuditSupport.success(operationLogService, "GUESTBOOK", username, "标记留言已读", "POST",
                "/api/admin/guestbooks/" + id + "/read", "{\"id\":" + id + "}");
        return GuestbookVO.from(guestbook);
    }

    @Override
    @Transactional
    public GuestbookVO reply(Long id, GuestbookReplyRequest request, String username) {
        Guestbook guestbook = find(id);
        if (CLOSED.equals(guestbook.getStatus())) {
            throw new BusinessException("GUESTBOOK_CLOSED", "已关闭留言不能回复");
        }
        AdminUser adminUser = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username)
                .eq(AdminUser::getStatus, "ENABLED"));
        if (adminUser == null) {
            throw new BusinessException("ADMIN_NOT_FOUND", "管理员不存在或已禁用");
        }
        guestbook.setReplyContent(trim(request.getReplyContent()));
        guestbook.setRepliedBy(adminUser.getId());
        guestbook.setRepliedAt(LocalDateTime.now());
        guestbook.setStatus(REPLIED);
        guestbookMapper.updateById(guestbook);
        AuditSupport.success(operationLogService, "GUESTBOOK", username, "回复留言", "POST",
                "/api/admin/guestbooks/" + id + "/reply", "{\"id\":" + id + "}");
        return GuestbookVO.from(guestbook);
    }

    @Override
    @Transactional
    public GuestbookVO close(Long id) {
        return close(id, null);
    }

    @Override
    @Transactional
    public GuestbookVO close(Long id, String username) {
        Guestbook guestbook = find(id);
        if (CLOSED.equals(guestbook.getStatus())) {
            throw new BusinessException("GUESTBOOK_ALREADY_CLOSED", "留言已经关闭");
        }
        guestbook.setStatus(CLOSED);
        guestbookMapper.updateById(guestbook);
        AuditSupport.success(operationLogService, "GUESTBOOK", username, "关闭留言", "POST",
                "/api/admin/guestbooks/" + id + "/close", "{\"id\":" + id + "}");
        return GuestbookVO.from(guestbook);
    }

    private Guestbook find(Long id) {
        Guestbook guestbook = guestbookMapper.selectById(id);
        if (guestbook == null) {
            throw new BusinessException("GUESTBOOK_NOT_FOUND", "留言不存在");
        }
        return guestbook;
    }

    private void checkRateLimit(String ip) {
        long now = Instant.now().toEpochMilli();
        rateWindows.compute(ip, (key, current) -> {
            if (current == null || now - current.startedAt() >= RATE_LIMIT_WINDOW.toMillis()) {
                return new RateWindow(now, 1);
            }
            if (current.count() >= MAX_SUBMISSIONS_PER_IP) {
                throw new BusinessException("TOO_MANY_REQUESTS", "留言提交过于频繁，请稍后再试");
            }
            return new RateWindow(current.startedAt(), current.count() + 1);
        });
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        String trimmed = trim(value);
        return StringUtils.hasText(trimmed) ? trimmed : null;
    }

    private record RateWindow(long startedAt, int count) {
    }
}
