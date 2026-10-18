package com.eems.site;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteResolver {
    private final JdbcTemplate jdbc;
    private final Environment environment;

    public SiteResolver(JdbcTemplate jdbc, Environment environment) {
        this.jdbc = jdbc;
        this.environment = environment;
    }

    public Long resolve(String hostHeader, boolean adminRequest) {
        String hostname = HostParser.parse(hostHeader);
        List<SiteRecord> matches = jdbc.query(
                "SELECT s.id, s.status FROM site_domain d JOIN site s ON s.id = d.site_id WHERE d.domain = ?",
                (rs, rowNum) -> new SiteRecord(rs.getLong("id"), rs.getString("status")), hostname);
        SiteRecord selected;
        if (matches.isEmpty()) {
            if (!localFallbackEnabled()) {
                throw new SiteResolutionException("SITE_NOT_FOUND", "未找到对应站点", 404);
            }
            selected = defaultSite();
        } else {
            selected = matches.get(0);
        }
        if (!"ENABLED".equals(selected.status())) {
            throw new SiteResolutionException("SITE_DISABLED", "站点已停用", 403);
        }
        if (adminRequest && !selected.id().equals(defaultSite().id())) {
            throw new SiteResolutionException("SITE_ADMIN_FORBIDDEN", "当前管理端仅支持默认站点", 403);
        }
        return selected.id();
    }

    private SiteRecord defaultSite() {
        List<SiteRecord> rows = jdbc.query("SELECT id, status FROM site WHERE code = 'default'",
                (rs, rowNum) -> new SiteRecord(rs.getLong("id"), rs.getString("status")));
        if (rows.size() != 1) {
            throw new SiteResolutionException("DEFAULT_SITE_MISSING", "默认站点未配置", 503);
        }
        return rows.get(0);
    }

    private boolean localFallbackEnabled() {
        return environment.acceptsProfiles(Profiles.of("local", "test"))
                && !environment.acceptsProfiles(Profiles.of("prod"));
    }

    private record SiteRecord(Long id, String status) {
    }
}
