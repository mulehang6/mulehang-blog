package com.mulehang.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * AI 聊天请求 DTO
 */
@Data
public class AiChatDTO {
    /**
     * 消息列表
     */
    @NotEmpty(message = "消息列表不能为空")
    private List<MessageDTO> messages;
    
    /**
     * 温度 (0-2)
     */
    private Double temperature;
    
    /**
     * 最大 Token
     */
    private Integer maxTokens;
    
    /**
     * 指定 Provider (可选，不指定则使用默认)
     */
    private String provider;

    @Data
    public static class MessageDTO {
        /**
         * 角色: system, user, assistant
         */
        private String role;
        
        /**
         * 内容
         */
        private String content;
    }
}
