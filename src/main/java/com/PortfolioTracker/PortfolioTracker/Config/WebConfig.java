package com.PortfolioTracker.PortfolioTracker.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Value("${frontend.url}")
    private String frontendUrl;


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply to all endpoints
                        .allowedOrigins("https://unrivaled-muffin-db0ee0.netlify.app") // Frontend origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // HTTP methods allowed
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true); // Allow cookies (if needed)
            }
        };
    }
}
