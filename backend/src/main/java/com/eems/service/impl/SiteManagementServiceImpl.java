package com.eems.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.common.api.PageResult;
import com.eems.common.exception.BusinessException;
import com.eems.dto.*;
import com.eems.entity.Site;
import com.eems.entity.SiteDomain;
import com.eems.mapper.SiteManagementMapper;
import com.eems.service.OperationLogService;
import com.eems.service.SiteManagementService;
import com.eems.site.SiteResolutionException;
import com.eems.vo.ManagedSiteVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SiteManagementServiceImpl implements SiteManagementService {
    private final SiteManagementMapper mapper;
    private final OperationLogService logs;

    public SiteManagementServiceImpl(SiteManagementMapper mapper, OperationLogService logs) {
        this.mapper = mapper;
        this.logs = logs;
    }

    @Override
    public List<com.eems.vo.SiteOptionVO> options() {
        return mapper.selectList(Wrappers.<Site>lambdaQuery().orderByAsc(Site::getCode).orderByAsc(Site::getId))
                .stream().map(com.eems.vo.SiteOptionVO::from).toList();
    }

    @Override
    public PageResult<ManagedSiteVO> page(SitePageQuery query) {
        var wrapper = Wrappers.<Site>lambdaQuery();
        if (StringUtils.hasText(query.getStatus())) wrapper.eq(Site::getStatus, query.getStatus());
        if (StringUtils.hasText(query.getKeyword())) {
            // Treat SQL LIKE wildcards as literal search text using an explicit escape character.
            String keyword = "%" + query.getKeyword().trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            wrapper.apply("(LOWER(name) LIKE {0} ESCAPE '!' OR LOWER(code) LIKE {0} ESCAPE '!' "
                    + "OR id IN (SELECT site_id FROM site_domain WHERE domain LIKE {0} ESCAPE '!'))", keyword);
        }
        wrapper.orderByDesc(Site::getCreatedAt).orderByDesc(Site::getId);
        var page = mapper.selectPage(new Page<Site>(query.getPage(), query.getPageSize()), wrapper);
        Map<Long, List<SiteDomainRequest>> domains = new HashMap<>();
        if (!page.getRecords().isEmpty()) {
            domains = mapper.selectDomains(page.getRecords().stream().map(Site::getId).toList()).stream()
                    .collect(Collectors.groupingBy(SiteDomain::getSiteId, Collectors.mapping(
                            d -> new SiteDomainRequest(d.getDomain(), d.getPrimary()), Collectors.toList())));
        }
        Map<Long, List<SiteDomainRequest>> grouped = domains;
        return new PageResult<>(page.getRecords().stream()
                .map(s -> toVO(s, grouped.getOrDefault(s.getId(), List.of()))).toList(),
                page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public ManagedSiteVO create(SiteSaveRequest request, String username) {
        List<SiteDomainRequest> domains = validateDomains(request.domains());
        if (mapper.selectCount(Wrappers.<Site>lambdaQuery().eq(Site::getCode, request.code())) > 0) {
            throw new BusinessException("SITE_CODE_EXISTS", "站点编码已存在");
        }
        checkDomainOwners(domains, 0L);
        Site site = new Site();
        site.setName(request.name().trim());
        site.setCode(request.code());
        site.setStatus("DISABLED");
        site.setCreatedAt(LocalDateTime.now());
        site.setUpdatedAt(site.getCreatedAt());
        try {
            mapper.insert(site);
            writeDomains(site.getId(), domains);
            String primary = domains.stream().filter(SiteDomainRequest::primary).findFirst().orElseThrow().domain();
            mapper.initializeConfig(site.getId(), site.getName(), primary);
        } catch (DuplicateKeyException exception) {
            throw duplicate();
        }
        audit(site.getId(), username, "新建站点", "POST", "/api/admin/sites");
        return toVO(site, domains);
    }

    @Override
    @Transactional
    public ManagedSiteVO update(Long id, SiteSaveRequest request, String username) {
        List<SiteDomainRequest> domains = validateDomains(request.domains());
        Site site = requireLocked(id);
        if (!site.getCode().equals(request.code())) {
            throw new BusinessException("SITE_CODE_IMMUTABLE", "站点编码创建后不可修改");
        }
        checkDomainOwners(domains, id);
        site.setName(request.name().trim());
        site.setUpdatedAt(LocalDateTime.now());
        try {
            mapper.updateById(site);
            // Parent lock serializes edits; delete first also permits changing the primary domain.
            mapper.deleteDomains(id);
            writeDomains(id, domains);
        } catch (DuplicateKeyException exception) {
            throw duplicate();
        }
        audit(id, username, "修改站点", "PUT", "/api/admin/sites/" + id);
        return toVO(site, domains);
    }

    @Override
    @Transactional
    public ManagedSiteVO changeStatus(Long id, String status, String username) {
        if (!Set.of("ENABLED", "DISABLED").contains(status)) {
            throw new BusinessException("INVALID_SITE_STATUS", "无效的站点状态");
        }
        Site site = requireLocked(id);
        List<SiteDomainRequest> domains = mapper.selectDomains(List.of(id)).stream()
                .map(d -> new SiteDomainRequest(d.getDomain(), d.getPrimary())).toList();
        if ("ENABLED".equals(status)) validateDomains(domains);
        site.setStatus(status);
        site.setUpdatedAt(LocalDateTime.now());
        mapper.updateById(site);
        audit(id, username, "ENABLED".equals(status) ? "启用站点" : "停用站点",
                "PUT", "/api/admin/sites/" + id + "/status");
        return toVO(site, domains);
    }

    private Site requireLocked(Long id) {
        Site site = mapper.lockSite(id);
        if (site == null) throw new SiteResolutionException("SITE_NOT_FOUND", "站点不存在", 404);
        return site;
    }

    private List<SiteDomainRequest> validateDomains(List<SiteDomainRequest> input) {
        if (input == null || input.isEmpty() || input.size() > 50) {
            throw new BusinessException("INVALID_SITE_DOMAINS", "请填写 1 至 50 个绑定域名");
        }
        Set<String> unique = new HashSet<>();
        List<SiteDomainRequest> result = new ArrayList<>();
        int primaries = 0;
        for (SiteDomainRequest item : input) {
            if (item == null || item.domain() == null || item.primary() == null) throw invalidDomain();
            String domain = item.domain().trim().toLowerCase(Locale.ROOT);
            if (domain.isEmpty() || domain.length() > 253) throw invalidDomain();
            for (String label : domain.split("\\.", -1)) {
                if (!label.matches("[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?")) throw invalidDomain();
            }
            if (!unique.add(domain)) throw new BusinessException("DUPLICATE_SITE_DOMAIN", "绑定域名不能重复");
            if (item.primary()) primaries++;
            result.add(new SiteDomainRequest(domain, item.primary()));
        }
        if (primaries != 1) throw new BusinessException("PRIMARY_DOMAIN_REQUIRED", "必须且只能设置一个主域名");
        return result;
    }

    private void checkDomainOwners(List<SiteDomainRequest> domains, Long siteId) {
        for (SiteDomainRequest domain : domains) {
            if (mapper.countOtherDomain(domain.domain(), siteId) > 0) {
                throw new BusinessException("SITE_DOMAIN_EXISTS", "域名已绑定其他站点");
            }
        }
    }

    private void writeDomains(Long siteId, List<SiteDomainRequest> domains) {
        for (SiteDomainRequest domain : domains) mapper.insertDomain(siteId, domain.domain(), domain.primary());
    }

    private void audit(Long id, String username, String operation, String method, String url) {
        // Participates in the same transaction: an audit failure rolls back the entire change.
        logs.success("SITE_MANAGEMENT", username, operation, method, url, null, "{\"siteId\":" + id + "}");
    }

    private BusinessException invalidDomain() {
        return new BusinessException("INVALID_SITE_DOMAIN", "域名格式无效，请填写不含协议、端口或路径的 ASCII 域名");
    }

    private BusinessException duplicate() {
        return new BusinessException("SITE_CONFLICT", "站点编码或域名已被使用，请刷新后重试");
    }

    private ManagedSiteVO toVO(Site site, List<SiteDomainRequest> domains) {
        return new ManagedSiteVO(site.getId(), site.getName(), site.getCode(), site.getStatus(),
                site.getCreatedAt(), site.getUpdatedAt(), domains);
    }
}

