package com.mulehang.blog.dto;

import lombok.Data;

/**
 * GitHub OAuth 回调 DTO
 * 
 * @author mulehang
 * @date 2026-01-21
 */
@Data
public class GitHubOAuthCallbackDTO {
    
    /**
     * GitHub 返回的授权码
     */
    private String code;
    
    /**
     * 状态码（用于防止 CSRF 攻击）
     */
    private String state;
}
