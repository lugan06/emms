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

    public ResolvedSite resolveHost(String hostHeader) {
        String hostname = HostParser.parse(hostHeader);
        List<ResolvedSite> matches = jdbc.query(
                "SELECT s.id, s.status FROM site_domain d JOIN site s ON s.id = d.site_id WHERE d.domain = ?",
                (rs, rowNum) -> new ResolvedSite(rs.getLong("id"), rs.getString("status")), hostname);
        ResolvedSite selected;
        if (matches.isEmpty()) {
            if (!localFallbackEnabled()) {
                throw new SiteResolutionException("SITE_NOT_FOUND", "未找到对应站点", 404);
            }
            selected = defaultSite();
        } else {
            selected = matches.get(0);
        }
        return selected;
    }

    public ResolvedSite requireSite(Long siteId) {
        List<ResolvedSite> matches = jdbc.query("SELECT id, status FROM site WHERE id = ?",
                (rs, rowNum) -> new ResolvedSite(rs.getLong("id"), rs.getString("status")), siteId);
        if (matches.isEmpty()) {
            throw new SiteResolutionException("SITE_NOT_FOUND", "未找到对应站点", 404);
        }
        return matches.get(0);
    }

    public static void requireEnabled(ResolvedSite selected) {
        if (!"ENABLED".equals(selected.status())) {
            throw new SiteResolutionException("SITE_DISABLED", "站点已停用", 403);
        }
    }

    private ResolvedSite defaultSite() {
        List<ResolvedSite> rows = jdbc.query("SELECT id, status FROM site WHERE code = 'default'",
                (rs, rowNum) -> new ResolvedSite(rs.getLong("id"), rs.getString("status")));
        if (rows.size() != 1) {
            throw new SiteResolutionException("DEFAULT_SITE_MISSING", "默认站点未配置", 503);
        }
        return rows.get(0);
    }

    private boolean localFallbackEnabled() {
        return environment.acceptsProfiles(Profiles.of("local", "test"))
                && !environment.acceptsProfiles(Profiles.of("prod"));
    }

    public record ResolvedSite(Long id, String status) {
    }
}
