package com.eems.config;

import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.SecurityFailureHandler;
import com.eems.site.SiteContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SiteContextFilter siteContextFilter;
    private final SecurityFailureHandler failures;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, SiteContextFilter siteContextFilter,
                          SecurityFailureHandler failures) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.siteContextFilter = siteContextFilter;
        this.failures = failures;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> { })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/admin/login", "/api/public/**", "/uploads/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, exception) -> {
                            failures.reject(request, response, 401, "UNAUTHORIZED", "请先登录", null);
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            failures.reject(request, response, 403, "FORBIDDEN", "没有权限", request.getUserPrincipal() == null
                                    ? null : request.getUserPrincipal().getName());
                        }))
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(siteContextFilter, JwtAuthenticationFilter.class);
        return http.build();
    }
}
