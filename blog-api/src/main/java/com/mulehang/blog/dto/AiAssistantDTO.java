package com.mulehang.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 助手请求 DTO
 */
@Data
public class AiAssistantDTO {
    /**
     * 文章内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;
    
    /**
     * 最大长度（用于摘要）
     */
    private Integer maxLength;
    
    /**
     * 生成数量（用于标题/标签）
     */
    private Integer count;
}
