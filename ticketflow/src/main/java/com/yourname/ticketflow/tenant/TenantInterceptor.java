package com.yourname.ticketflow.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 租户拦截器 - 从请求头 X-Tenant-Id 解析租户ID
 */
@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String tenantIdStr = request.getHeader("X-Tenant-Id");
        Long tenantId = 1L; // 默认租户
        if (tenantIdStr != null && !tenantIdStr.isEmpty()) {
            try {
                tenantId = Long.valueOf(tenantIdStr);
            } catch (NumberFormatException e) {
                log.warn("无效的租户ID: {}", tenantIdStr);
            }
        }
        TenantContext.setTenantId(tenantId);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {
        // no-op
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 清除 ThreadLocal，防止内存泄漏
        TenantContext.clear();
    }
}
