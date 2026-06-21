package com.yourname.ticketflow.security;

import com.yourname.ticketflow.common.utils.JwtUtils;
import com.yourname.ticketflow.modules.user.mapper.SysUserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器 — 从 Token 解析用户并加载权限
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final SysUserMapper userMapper;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, SysUserMapper userMapper) {
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            try {
                Long userId = jwtUtils.getUserId(token);
                String username = jwtUtils.getUsername(token);

                // 从数据库加载用户权限和角色
                List<String> permissions = userMapper.selectPermissionCodesByUserId(userId);
                List<String> roles = userMapper.selectRoleCodesByUserId(userId);

                // 角色作为 ROLE_ 前缀的权限
                List<String> authorities = new java.util.ArrayList<>(permissions);
                if (roles != null) {
                    roles.forEach(role -> authorities.add("ROLE_" + role));
                }
                // 超级管理员拥有所有权限
                if (roles != null && roles.contains("SUPER_ADMIN")) {
                    authorities.add("ROLE_SUPER_ADMIN");
                }

                log.info("用户 {} (id={}) 权限: {}", username, userId, authorities);

                JwtUserDetails userDetails = JwtUserDetails.builder()
                        .userId(userId)
                        .username(username)
                        .permissions(authorities)
                        .enabled(true)
                        .build();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.warn("JWT 认证失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 Bearer Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
