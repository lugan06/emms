package com.eems.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenUtilTest {

    @Test
    void generatedTokenShouldContainIdentityAndRoleClaims() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("01234567890123456789012345678901");
        properties.setAccessTokenTtl(120);
        JwtTokenUtil tokenUtil = new JwtTokenUtil(properties);

        String token = tokenUtil.generateToken(1L, "admin", "SUPER_ADMIN");
        Claims claims = tokenUtil.parseClaims(token);

        assertTrue(tokenUtil.isValid(token));
        assertEquals("admin", claims.getSubject());
        assertEquals(1L, claims.get("userId", Number.class).longValue());
        assertEquals("SUPER_ADMIN", claims.get("roleCode", String.class));
        assertFalse(tokenUtil.isValid(token + "tampered"));
    }
}
