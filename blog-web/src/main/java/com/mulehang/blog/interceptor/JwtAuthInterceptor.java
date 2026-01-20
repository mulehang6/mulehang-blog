package com.mulehang.blog.interceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.util.JwtUtil;
import com.mulehang.blog.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * JWT 认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 从请求头获取 Token
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("请求未携带有效的 Authorization 头");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(7);

        try {
            // 2. 验证并解析 Token
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            
            // 3. 提取用户信息
            Long userId = jwt.getClaim("userId").asLong();
            String username = jwt.getClaim("username").asString();
            List<String> roles = jwt.getClaim("roles").asList(String.class);

            // 4. 构建用户信息并存入上下文
            UserInfoVO userInfo = UserInfoVO.builder()
                    .id(userId)
                    .username(username)
                    .roles(roles)
                    .build();
            
            UserContext.setCurrentUser(userInfo);
            
            return true;
        } catch (JWTVerificationException e) {
            log.error("Token 验证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除用户上下文
        UserContext.clear();
    }
}

