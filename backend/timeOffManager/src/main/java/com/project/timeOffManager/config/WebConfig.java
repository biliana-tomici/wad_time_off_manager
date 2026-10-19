package com.project.timeOffManager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig {

    @Value("${app.cors.allowed-origins:}")
    private String allowedOriginsProp;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = Arrays.stream(allowedOriginsProp.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new);

                if (origins.length > 0) {
                    registry.addMapping("/**")
                            .allowedOrigins(origins)
                            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                            .allowedHeaders("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With")
                            .exposedHeaders("Content-Disposition")
                            .allowCredentials(true);
                }
            }
        };
    }
}
