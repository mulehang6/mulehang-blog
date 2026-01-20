package com.mulehang.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置。
 * <p>
 * 当前阶段（Milestone 1）主要目的是让：
 * <ul>
 *   <li>Swagger/Knife4j 文档可直接访问</li>
 *   <li>Milestone 1 的 /api/v1/** 接口可直接调通</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    /**
     * 配置安全过滤器链。
     *
     * <p>当前配置禁用 CSRF，允许 Swagger 文档和 API 接口直接访问，其他请求需要认证。</p>
     *
     * @param http HTTP 安全配置
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/error",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/doc.html",
                                "/webjars/**",
                                "/api/v1/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
