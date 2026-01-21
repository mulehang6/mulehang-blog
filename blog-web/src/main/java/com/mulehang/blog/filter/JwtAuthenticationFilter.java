package com.mulehang.blog.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.util.JwtUtil;
import com.mulehang.blog.vo.UserInfoVO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器
 * 从请求头中提取 JWT Token,验证并设置 Spring Security 上下文
 * 支持异步dispatch(如SSE流式传输),确保异步响应时也能正确认证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null) {
                DecodedJWT jwt = jwtUtil.verifyToken(token);
                Long userId = jwt.getClaim("userId").asLong();
                String username = jwt.getClaim("username").asString();
                List<String> roles = jwt.getClaim("roles").asList(String.class);

                // 设置 Spring Security 上下文
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                // 设置 UserContext
                UserInfoVO userInfo = UserInfoVO.builder()
                        .id(userId)
                        .username(username)
                        .roles(roles)
                        .build();
                UserContext.setCurrentUser(userInfo);
            }
        } catch (Exception e) {
            log.debug("JWT 验证失败: {}", e.getMessage());
        } finally {
            try {
                chain.doFilter(request, response);
            } finally {
                UserContext.clear();
            }
        }
    }

    /**
     * 重写此方法以支持异步dispatch
     * 默认OncePerRequestFilter会跳过异步dispatch,导致SSE流式响应完成后的异步dispatch无法通过JWT认证
     * 返回false表示在异步dispatch时也需要执行此过滤器
     *
     * @return false - 在异步dispatch时也执行过滤器
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    /**
     * 从请求头提取 Token
     *
     * @param request HTTP 请求
     * @return Token 字符串，如果不存在则返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
