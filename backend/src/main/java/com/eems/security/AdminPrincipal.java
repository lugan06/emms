package com.eems.security;

import java.security.Principal;

/** Identity rebuilt from the database for each authenticated request. */
public record AdminPrincipal(Long userId, String username, String roleCode, Long loginSiteId)
        implements Principal {
    @Override
    public String getName() {
        return username;
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(roleCode);
    }
}
