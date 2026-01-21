package com.mulehang.blog.controller.api.v1;

import com.mulehang.blog.ai.AiService;
import com.mulehang.blog.ai.AiServiceFactory;
import com.mulehang.blog.ai.model.AiMessage;
import com.mulehang.blog.ai.model.AiRequest;
import com.mulehang.blog.ai.model.AiResponse;
import com.mulehang.blog.dto.AiAssistantDTO;
import com.mulehang.blog.dto.AiChatDTO;
import com.mulehang.blog.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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

    /**
     * 同步对话
     */
    @PostMapping("/chat")
    @Operation(summary = "AI 对话（同步）")
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
     * 流式对话(SSE)
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 对话(流式)")
    @PreAuthorize("isAuthenticated()")
    public Flux<String> chatStream(@Valid @RequestBody AiChatDTO dto) {
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
                .map(chunk -> "data: " + chunk + "\n\n")
                .onErrorResume(e -> {
                    log.error("Stream error", e);
                    return Flux.just("data: [ERROR] " + e.getMessage() + "\n\n");
                });
    }

    /**
     * 生成文章摘要
     */
    @PostMapping("/assistant/summary")
    @Operation(summary = "生成文章摘要")
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
    public Result<List<String>> suggestTags(@Valid @RequestBody AiAssistantDTO dto) {
        AiService aiService = aiServiceFactory.getDefaultService();
        int count = dto.getCount() != null ? dto.getCount() : 5;
        List<String> tags = aiService.suggestTags(dto.getContent(), count);
        return Result.ok(tags);
    }

    private AiService getAiService(String provider) {
        if (provider != null && !provider.isEmpty()) {
            return aiServiceFactory.getService(provider);
        }
        return aiServiceFactory.getDefaultService();
    }
}
