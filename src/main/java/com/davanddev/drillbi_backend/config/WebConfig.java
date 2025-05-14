package com.davanddev.drillbi_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var registration = registry.addMapping("/**")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);

        if ("dev".equals(activeProfile)) {
            // Dev-mode allow these
            registration.allowedOriginPatterns("http://localhost:*");

        } else {
            // Production – Allow local and production
            registration.allowedOriginPatterns("https://drillbi.se", "https://drillbi.com");

        }
    }
}
