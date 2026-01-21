package com.mulehang.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 写作助手请求 DTO
 */
@Data
@Schema(description = "AI 写作助手请求")
public class AiWritingDTO {

    @Schema(description = "文章主题（用于生成大纲）", example = "Spring Boot 3.x 新特性详解")
    @Size(max = 200, message = "主题长度不能超过200字符")
    private String topic;

    @Schema(description = "文章内容（用于续写/润色/翻译）", example = "Spring Boot 3.0带来了许多重大更新...")
    @Size(max = 50000, message = "内容长度不能超过50000字符")
    private String content;

    @Schema(description = "目标语言（用于翻译）", example = "英文")
    @Size(max = 50, message = "目标语言长度不能超过50字符")
    private String targetLanguage;

    @Schema(description = "目标字数（用于扩写/缩写）", example = "1000")
    @Min(value = 100, message = "目标字数不能小于100")
    @Max(value = 10000, message = "目标字数不能超过10000")
    private Integer targetLength;

    @Schema(description = "AI服务提供商（可选，为空则使用默认）", example = "zhipu")
    private String provider;
}
