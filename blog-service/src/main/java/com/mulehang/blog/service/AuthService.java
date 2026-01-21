package com.mulehang.blog.service;

import com.mulehang.blog.dto.GuestLoginRequest;
import com.mulehang.blog.dto.LoginRequest;
import com.mulehang.blog.dto.RegisterRequest;
import com.mulehang.blog.vo.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（包含 Token 和用户信息）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 登录响应（注册成功后自动登录）
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 退出登录（可选：用于清理缓存等）
     *
     * @param userId 用户ID
     */
    void logout(Long userId);

    /**
     * 访客登录（生成临时访问令牌，用于测试）
     *
     * @param request 访客登录请求
     * @return 登录响应（包含临时 Token）
     */
    LoginResponse guestLogin(GuestLoginRequest request);
}

