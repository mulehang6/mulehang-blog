package com.mulehang.blog.dto;

import lombok.Data;

/**
 * 访客登录请求
 * 用于快速生成临时访问令牌，方便测试
 */
@Data
public class GuestLoginRequest {

    /**
     * 访客昵称（可选，默认生成随机昵称）
     */
    private String nickname;

    /**
     * Token 有效期（秒），默认 1 小时
     */
    private Long expiresIn = 3600L;
}
