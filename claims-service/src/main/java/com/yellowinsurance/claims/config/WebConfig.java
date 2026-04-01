package com.yellowinsurance.claims.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // VULNERABILITY: Serving files from /tmp/uploads - could expose uploaded malicious files
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/tmp/uploads/");
    }
}
