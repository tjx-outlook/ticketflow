package com.yourname.ticketflow.config;

import com.yourname.ticketflow.tenant.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置 - 静态资源映射 + 租户拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    /**
     * 静态资源映射：/uploads/** → 本地 uploads/ 目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir.getAbsolutePath() + "/");
        // Knife4j/Swagger 静态资源
        registry.addResourceHandler("/doc.html", "/swagger-ui/**", "/webjars/**", "/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/",
                                     "classpath:/META-INF/resources/webjars/");
    }

    /**
     * 租户拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/event/list",
                        "/api/system/**",
                        "/api/upload/**"
                );
    }
}
