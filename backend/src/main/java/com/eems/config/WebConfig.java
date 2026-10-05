package com.eems.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    public WebConfig(ObjectProvider<FileStorageProperties> propertiesProvider) {
        this.fileStorageProperties = propertiesProvider.getIfAvailable(FileStorageProperties::new);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = fileStorageProperties.getPublicUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "/uploads";
        }
        String pattern = prefix.replaceAll("/+$", "") + "/**";
        String location = java.nio.file.Paths.get(fileStorageProperties.getLocalPath())
                .toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:[*]", "http://127.0.0.1:[*]"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
