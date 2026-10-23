package com.eems.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebConfigTest {

    @Test
    void localStorageShouldBeRegisteredAsFileResource() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setLocalPath("D:/ems-uploads/");

        @SuppressWarnings("unchecked")
        ObjectProvider<FileStorageProperties> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable(org.mockito.ArgumentMatchers.any())).thenReturn(properties);

        WebConfig webConfig = new WebConfig(provider);
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);

        webConfig.addResourceHandlers(registry);

        Path root = Paths.get(properties.getLocalPath()).toAbsolutePath().normalize();
        String expectedLocation = root.toUri().toString();
        if (!expectedLocation.endsWith("/")) {
            expectedLocation += "/";
        }
        verify(registry).addResourceHandler(eq("/uploads/**"));
        verify(registration).addResourceLocations(eq(expectedLocation));
    }
}
