package com.worldcup.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class AdminAuditInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "anonymous";
        log.info("[ADMIN AUDIT] {} accessed {} {}", username, request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                @Nullable Exception ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "anonymous";
        if (ex != null) {
            log.warn("[ADMIN AUDIT] {} {} {} -> {} ({})",
                username, request.getMethod(), request.getRequestURI(),
                response.getStatus(), ex.getClass().getSimpleName());
        } else {
            log.info("[ADMIN AUDIT] {} {} {} -> {}",
                username, request.getMethod(), request.getRequestURI(), response.getStatus());
        }
    }
}
