package com.mulehang.blog.controller;

import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.dto.GitHubOAuthCallbackDTO;
import com.mulehang.blog.dto.GuestLoginRequest;
import com.mulehang.blog.dto.LoginRequest;
import com.mulehang.blog.dto.RegisterRequest;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.AuthService;
import com.mulehang.blog.service.GitHubOAuthService;
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
    private final GitHubOAuthService gitHubOAuthService;

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

    /**
     * 访客登录
     *
     * @param request 访客登录请求
     * @return 登录响应（包含临时 Token）
     */
    @Operation(summary = "访客登录", description = "生成临时访问令牌，无需注册即可测试接口")
    @PostMapping("/guest")
    public Result<LoginResponse> guestLogin(@RequestBody(required = false) GuestLoginRequest request) {
        // 如果没有传递请求体，使用默认配置
        if (request == null) {
            request = new GuestLoginRequest();
        }
        LoginResponse response = authService.guestLogin(request);
        return Result.ok(response);
    }

    /**
     * 获取 GitHub OAuth 授权 URL
     *
     * @return GitHub 授权 URL
     */
    @Operation(summary = "GitHub 授权", description = "获取 GitHub OAuth 授权 URL，用于跳转到 GitHub 登录页面")
    @GetMapping("/oauth/github/authorize")
    public Result<String> githubAuthorize(@RequestParam(required = false) String state) {
        // 如果没有传递 state，使用默认值
        if (state == null || state.isBlank()) {
            state = "default";
        }
        String authorizeUrl = gitHubOAuthService.getAuthorizeUrl(state);
        return Result.ok(authorizeUrl);
    }

    /**
     * GitHub OAuth 回调接口
     *
     * @param callback 回调参数（包含 code 和 state）
     * @return 登录响应（包含 JWT Token 和用户信息）
     */
    @Operation(summary = "GitHub OAuth 回调", description = "GitHub 授权回调接口，用于接收 GitHub 返回的授权码")
    @GetMapping("/oauth/github/callback")
    public Result<LoginResponse> githubCallback(@Valid GitHubOAuthCallbackDTO callback) {
        LoginResponse response = gitHubOAuthService.login(callback.getCode());
        return Result.ok(response);
    }
}

