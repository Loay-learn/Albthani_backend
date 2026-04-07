package com.albthani.currency_exchange.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // كشف مجلد uploads كـ static resource
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
