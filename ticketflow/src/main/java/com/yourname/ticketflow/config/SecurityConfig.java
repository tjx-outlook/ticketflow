package com.yourname.ticketflow.config;

import com.yourname.ticketflow.security.JwtAccessDeniedHandler;
import com.yourname.ticketflow.security.JwtAuthenticationEntryPoint;
import com.yourname.ticketflow.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 安全配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    /**
     * 白名单 - 无需认证即可访问
     */
    private static final String[] WHITE_LIST = {
            "/api/user/login",
            "/api/user/register",
            "/api/upload/**",
            "/uploads/**",
            "/api/event/list",
            "/api/event/*",
            "/api/payment/debug",
            "/api/payment/create",
            "/api/payment/status/**",
            "/api/payment/alipay/notify",
            "/api/payment/wechat/notify",
            "/api/system/dict/**",
            "/docs",
            "/api-docs.html",
            "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 跨域
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 禁用 CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 无状态 Session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 请求权限配置
                .authorizeHttpRequests(auth -> auth
                        // 白名单 — 完全公开
                        .requestMatchers(WHITE_LIST).permitAll()
                        // 公开的 GET 请求（活动列表/详情、字典数据）
                        .requestMatchers(HttpMethod.GET, "/api/event/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/seat/list/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/system/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/merchant/**").permitAll()
                        // OPTIONS 预检请求放行
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 其余需要认证
                        .anyRequest().authenticated())
                // 异常处理
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173", "http://localhost:8080"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
