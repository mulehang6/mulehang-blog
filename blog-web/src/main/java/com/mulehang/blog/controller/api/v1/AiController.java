package com.mulehang.blog.controller.api.v1;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.mulehang.blog.ai.AiService;
import com.mulehang.blog.ai.AiServiceFactory;
import com.mulehang.blog.ai.model.AiMessage;
import com.mulehang.blog.ai.model.AiRequest;
import com.mulehang.blog.ai.model.AiResponse;
import com.mulehang.blog.dto.AiAssistantDTO;
import com.mulehang.blog.dto.AiChatDTO;
import com.mulehang.blog.dto.AiWritingDTO;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.AiWritingAssistant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI 服务", description = "AI 对话与内容生产")
@RequiredArgsConstructor
public class AiController {

    private final AiServiceFactory aiServiceFactory;
    private final AiWritingAssistant aiWritingAssistant;

    /**
     * 同步对话
     */
    @PostMapping("/chat")
    @Operation(summary = "AI 对话（同步）")
    @SentinelResource(
            value = "ai-chat",
            blockHandler = "chatBlockHandler",
            fallback = "chatFallback"
    )
    public Result<String> chat(@Valid @RequestBody AiChatDTO dto) {
        AiService aiService = getAiService(dto.getProvider());
        
        List<AiMessage> messages = dto.getMessages().stream()
                .map(m -> new AiMessage(m.getRole(), m.getContent()))
                .collect(Collectors.toList());

        AiRequest request = AiRequest.builder()
                .messages(messages)
                .temperature(dto.getTemperature())
                .maxTokens(dto.getMaxTokens())
                .build();

        AiResponse response = aiService.chat(request);
        return Result.ok(response.getContent());
    }

    /**
     * 同步对话限流处理
     */
    public Result<String> chatBlockHandler(@Valid @RequestBody AiChatDTO dto, BlockException e) {
        log.warn("AI 对话接口被限流: {}", e.getRule());
        return Result.fail("请求过于频繁，请稍后再试");
    }

    /**
     * 同步对话降级处理
     */
    public Result<String> chatFallback(@Valid @RequestBody AiChatDTO dto, Throwable e) {
        log.error("AI 对话接口异常降级", e);
        return Result.fail("AI 服务暂时不可用，请稍后再试");
    }

    /**
     * 流式对话(SSE)
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 对话(流式)")
    @PreAuthorize("isAuthenticated()")
    @SentinelResource(
            value = "ai-chat-stream",
            blockHandler = "chatStreamBlockHandler",
            fallback = "chatStreamFallback"
    )
    public Flux<ServerSentEvent<String>> chatStream(@Valid @RequestBody AiChatDTO dto) {
        AiService aiService = getAiService(dto.getProvider());

        List<AiMessage> messages = dto.getMessages().stream()
                .map(m -> new AiMessage(m.getRole(), m.getContent()))
                .collect(Collectors.toList());

        AiRequest request = AiRequest.builder()
                .messages(messages)
                .temperature(dto.getTemperature())
                .maxTokens(dto.getMaxTokens())
                .stream(true)
                .build();

        return aiService.chatStream(request)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build())
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("[DONE]")
                        .build()))
                .onErrorResume(e -> {
                    log.error("Stream error", e);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data("[ERROR] " + e.getMessage())
                            .build());
                });
    }

    /**
     * 流式对话限流处理
     */
    public Flux<ServerSentEvent<String>> chatStreamBlockHandler(@Valid @RequestBody AiChatDTO dto, BlockException e) {
        log.warn("AI 流式对话接口被限流: {}", e.getRule());
        return Flux.just(ServerSentEvent.<String>builder()
                .event("blocked")
                .data("[BLOCKED] 请求过于频繁，请稍后再试")
                .build());
    }

    /**
     * 流式对话降级处理
     */
    public Flux<ServerSentEvent<String>> chatStreamFallback(@Valid @RequestBody AiChatDTO dto, Throwable e) {
        log.error("AI 流式对话接口异常降级", e);
        return Flux.just(ServerSentEvent.<String>builder()
                .event("error")
                .data("[ERROR] AI 服务暂时不可用，请稍后再试")
                .build());
    }

    /**
     * 生成文章摘要
     */
    @PostMapping("/assistant/summary")
    @Operation(summary = "生成文章摘要")
    @SentinelResource(
            value = "ai-assistant",
            blockHandler = "assistantBlockHandler",
            fallback = "assistantFallback"
    )
    public Result<String> generateSummary(@Valid @RequestBody AiAssistantDTO dto) {
        AiService aiService = aiServiceFactory.getDefaultService();
        int maxLength = dto.getMaxLength() != null ? dto.getMaxLength() : 200;
        String summary = aiService.generateSummary(dto.getContent(), maxLength);
        return Result.ok(summary);
    }

    /**
     * 推荐文章标题
     */
    @PostMapping("/assistant/titles")
    @Operation(summary = "推荐文章标题")
    @SentinelResource(
            value = "ai-assistant",
            blockHandler = "assistantBlockHandler",
            fallback = "assistantFallback"
    )
    public Result<List<String>> suggestTitles(@Valid @RequestBody AiAssistantDTO dto) {
        AiService aiService = aiServiceFactory.getDefaultService();
        int count = dto.getCount() != null ? dto.getCount() : 3;
        List<String> titles = aiService.suggestTitles(dto.getContent(), count);
        return Result.ok(titles);
    }

    /**
     * 推荐文章标签
     */
    @PostMapping("/assistant/tags")
    @Operation(summary = "推荐文章标签")
    @SentinelResource(
            value = "ai-assistant",
            blockHandler = "assistantBlockHandler",
            fallback = "assistantFallback"
    )
    public Result<List<String>> suggestTags(@Valid @RequestBody AiAssistantDTO dto) {
        AiService aiService = aiServiceFactory.getDefaultService();
        int count = dto.getCount() != null ? dto.getCount() : 5;
        List<String> tags = aiService.suggestTags(dto.getContent(), count);
        return Result.ok(tags);
    }

    /**
     * AI 助手功能限流处理（通用）
     */
    public Result<?> assistantBlockHandler(@Valid @RequestBody AiAssistantDTO dto, BlockException e) {
        log.warn("AI 助手接口被限流: {}", e.getRule());
        return Result.fail("请求过于频繁，请稍后再试");
    }

    /**
     * AI 助手功能降级处理（通用）
     */
    public Result<?> assistantFallback(@Valid @RequestBody AiAssistantDTO dto, Throwable e) {
        log.error("AI 助手接口异常降级", e);
        return Result.fail("AI 服务暂时不可用，请稍后再试");
    }

    /**
     * 生成文章大纲
     */
    @PostMapping("/writing/outline")
    @Operation(
            summary = "生成文章大纲",
            description = "根据主题生成详细的文章大纲，返回列表形式的要点"
    )
    @SentinelResource(
            value = "ai-writing",
            blockHandler = "writingBlockHandler",
            fallback = "writingFallback"
    )
    public Result<List<String>> generateOutline(@Valid @RequestBody AiWritingDTO dto) {
        if (dto.getTopic() == null || dto.getTopic().trim().isEmpty()) {
            return Result.fail("主题不能为空");
        }
        List<String> outline = aiWritingAssistant.generateOutline(dto.getTopic());
        return Result.ok(outline);
    }

    /**
     * 续写文章（同步）
     */
    @PostMapping("/writing/continue")
    @Operation(
            summary = "续写文章（同步）",
            description = "根据已有内容续写文章，返回完整结果（非流式）"
    )
    @SentinelResource(
            value = "ai-writing",
            blockHandler = "writingBlockHandler",
            fallback = "writingFallback"
    )
    public Result<String> continueWriting(@Valid @RequestBody AiWritingDTO dto) {
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return Result.fail("内容不能为空");
        }
        // 同步版本：收集完整结果
        String result = aiWritingAssistant.continueWriting(dto.getContent())
                .collectList()
                .map(chunks -> String.join("", chunks))
                .block();
        return Result.ok(result);
    }

    /**
     * 续写文章（流式SSE）
     */
    @PostMapping(value = "/writing/continue/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "续写文章（流式）",
            description = "根据已有内容续写文章，使用SSE流式输出结果，需要登录认证"
    )
    @PreAuthorize("isAuthenticated()")
    @SentinelResource(
            value = "ai-writing-stream",
            blockHandler = "writingStreamBlockHandler",
            fallback = "writingStreamFallback"
    )
    public Flux<ServerSentEvent<String>> continueWritingStream(@Valid @RequestBody AiWritingDTO dto) {
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data("[ERROR] 内容不能为空")
                    .build());
        }
        
        return aiWritingAssistant.continueWriting(dto.getContent())
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build())
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("[DONE]")
                        .build()))
                .onErrorResume(e -> {
                    log.error("续写流式输出失败", e);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data("[ERROR] " + e.getMessage())
                            .build());
                });
    }

    /**
     * 润色文章
     */
    @PostMapping("/writing/polish")
    @Operation(
            summary = "润色文章",
            description = "对文章内容进行润色优化，提升文字质量但保持原意"
    )
    @SentinelResource(
            value = "ai-writing",
            blockHandler = "writingBlockHandler",
            fallback = "writingFallback"
    )
    public Result<String> polish(@Valid @RequestBody AiWritingDTO dto) {
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return Result.fail("内容不能为空");
        }
        String polished = aiWritingAssistant.polish(dto.getContent());
        return Result.ok(polished);
    }

    /**
     * 翻译文章
     */
    @PostMapping("/writing/translate")
    @Operation(
            summary = "翻译文章",
            description = "将文章内容翻译成指定语言"
    )
    @SentinelResource(
            value = "ai-writing",
            blockHandler = "writingBlockHandler",
            fallback = "writingFallback"
    )
    public Result<String> translate(@Valid @RequestBody AiWritingDTO dto) {
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return Result.fail("内容不能为空");
        }
        if (dto.getTargetLanguage() == null || dto.getTargetLanguage().trim().isEmpty()) {
            return Result.fail("目标语言不能为空");
        }
        String translated = aiWritingAssistant.translate(dto.getContent(), dto.getTargetLanguage());
        return Result.ok(translated);
    }

    /**
     * 写作助手功能限流处理（通用）
     */
    public Result<?> writingBlockHandler(@Valid @RequestBody AiWritingDTO dto, BlockException e) {
        log.warn("AI 写作助手接口被限流: {}", e.getRule());
        return Result.fail("请求过于频繁，请稍后再试");
    }

    /**
     * 写作助手功能降级处理（通用）
     */
    public Result<?> writingFallback(@Valid @RequestBody AiWritingDTO dto, Throwable e) {
        log.error("AI 写作助手接口异常降级", e);
        return Result.fail("AI 服务暂时不可用，请稍后再试");
    }

    /**
     * 写作助手流式接口限流处理
     */
    public Flux<ServerSentEvent<String>> writingStreamBlockHandler(@Valid @RequestBody AiWritingDTO dto, BlockException e) {
        log.warn("AI 写作助手流式接口被限流: {}", e.getRule());
        return Flux.just(ServerSentEvent.<String>builder()
                .event("blocked")
                .data("[BLOCKED] 请求过于频繁，请稍后再试")
                .build());
    }

    /**
     * 写作助手流式接口降级处理
     */
    public Flux<ServerSentEvent<String>> writingStreamFallback(@Valid @RequestBody AiWritingDTO dto, Throwable e) {
        log.error("AI 写作助手流式接口异常降级", e);
        return Flux.just(ServerSentEvent.<String>builder()
                .event("error")
                .data("[ERROR] AI 服务暂时不可用，请稍后再试")
                .build());
    }

    private AiService getAiService(String provider) {
        if (provider != null && !provider.isEmpty()) {
            return aiServiceFactory.getService(provider);
        }
        return aiServiceFactory.getDefaultService();
    }
}
