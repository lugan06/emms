package com.eems.site;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class SiteFilterConfig {
    @Bean
    public FilterRegistrationBean<SiteContextFilter> siteContextFilter(SiteResolver resolver, ObjectMapper objectMapper) {
        FilterRegistrationBean<SiteContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SiteContextFilter(resolver, objectMapper));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
