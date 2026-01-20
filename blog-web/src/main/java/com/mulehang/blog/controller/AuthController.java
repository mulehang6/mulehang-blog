package com.mulehang.blog.controller;

import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.dto.LoginRequest;
import com.mulehang.blog.dto.RegisterRequest;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.AuthService;
import com.mulehang.blog.vo.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户登录、注册、退出等接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.ok(response);
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 登录响应
     */
    @Operation(summary = "用户注册", description = "注册新用户，注册成功后自动登录")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return Result.ok(response);
    }

    /**
     * 退出登录

     * @return 成功消息
     */
    @Operation(summary = "退出登录", description = "退出当前登录状态")
    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = UserContext.getCurrentUserId();
        authService.logout(userId);
        return Result.ok();
    }
}

