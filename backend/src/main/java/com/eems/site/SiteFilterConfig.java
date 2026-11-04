package com.eems.site;

import com.eems.mapper.AdminUserSiteMapper;
import com.eems.mapper.AdminUserMapper;
import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.JwtTokenUtil;
import com.eems.security.SecurityFailureHandler;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class SiteFilterConfig {
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenUtil tokens, AdminUserMapper users,
                                                          SecurityFailureHandler failures) {
        return new JwtAuthenticationFilter(tokens, users, failures);
    }

    @Bean
    public FilterRegistrationBean<DomainSiteFilter> domainSiteFilter(SiteResolver resolver, SecurityFailureHandler failures) {
        FilterRegistrationBean<DomainSiteFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new DomainSiteFilter(resolver, failures));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }

    @Bean
    public SiteContextFilter siteContextFilter(SiteResolver resolver, AdminUserSiteMapper bindings,
                                               SecurityFailureHandler failures) {
        return new SiteContextFilter(resolver, bindings, failures);
    }

    @Bean
    public FilterRegistrationBean<SiteContextFilter> disableSiteContextServletRegistration(SiteContextFilter filter) {
        FilterRegistrationBean<SiteContextFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> disableJwtServletRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
