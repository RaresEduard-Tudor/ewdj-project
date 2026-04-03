package com.worldcup.config;

import com.worldcup.interceptor.AdminAuditInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminAuditInterceptor adminAuditInterceptor;

    public WebConfig(AdminAuditInterceptor adminAuditInterceptor) {
        this.adminAuditInterceptor = adminAuditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuditInterceptor).addPathPatterns("/admin/**");
    }
}
