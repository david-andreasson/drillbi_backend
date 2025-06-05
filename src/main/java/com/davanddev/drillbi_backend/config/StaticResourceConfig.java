package com.davanddev.drillbi_backend.config;

import org.springframework.lang.NonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    private static final Logger log = LoggerFactory.getLogger(StaticResourceConfig.class);
    @Value("${drillbi.images.path}")
    private String imagesPath;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String prefix = imagesPath.startsWith("/") ? "file:" : "file:///";
        log.info("[StaticResourceConfig] Serving images from: {}{}", prefix, imagesPath);
        registry.addResourceHandler("/images/**")
                .addResourceLocations(prefix + imagesPath);
    }
}
