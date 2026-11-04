package com.eems.security;

import com.eems.mapper.AdminUserMapper;
import com.eems.service.OperationLogService;
import com.eems.site.SiteContextFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

/** Controller slices test endpoint/method permissions. Real JWT/tenant chain has separate full-stack tests. */
@TestConfiguration
public class ControllerSecurityTestConfig {
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenUtil tokens, SecurityFailureHandler failures) {
        return new JwtAuthenticationFilter(tokens, mock(AdminUserMapper.class), failures);
    }

    @Bean
    public SecurityFailureHandler securityFailureHandler(ObjectMapper mapper) {
        return new SecurityFailureHandler(mapper, mock(OperationLogService.class));
    }

    @Bean
    public SiteContextFilter siteContextFilter() {
        return new SiteContextFilter(null, null, null) {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                return true;
            }
        };
    }
}
