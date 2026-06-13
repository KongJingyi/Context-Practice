package com.team13.context.config;

import com.team13.context.common.UserContext;
import com.team13.context.mapper.UserRoleMapper;
import com.team13.context.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 解析 JWT 并写入 Spring Security 上下文，供 {@code @PreAuthorize} 使用。
 */
@Component
@ConditionalOnProperty(name = "app.jwt.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRoleMapper userRoleMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Long userId = jwtUtils.parseUserId(token);
                if (userId != null) {
                    List<String> roles = userRoleMapper.selectRoleCodesByUserId(userId);
                    var authorities =
                            roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
                    var authentication =
                            new UsernamePasswordAuthenticationToken(userId, token, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
