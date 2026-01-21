package com.mulehang.blog.service;

import com.mulehang.blog.ai.AiService;
import com.mulehang.blog.ai.AiServiceFactory;
import com.mulehang.blog.ai.model.AiMessage;
import com.mulehang.blog.ai.model.AiRequest;
import com.mulehang.blog.ai.model.AiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI写作助手服务
 * 提供文章大纲生成、续写、润色、翻译等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiWritingAssistant {

    private final AiServiceFactory aiServiceFactory;

    /**
     * 生成文章大纲
     *
     * @param topic 文章主题
     * @return 大纲列表
     */
    public List<String> generateOutline(String topic) {
        log.info("生成文章大纲, 主题: {}", topic);
        
        AiService aiService = aiServiceFactory.getDefaultService();
        String prompt = "请为主题\"" + topic + "\"生成一个详细的文章大纲，用换行分隔每个要点：";
        
        AiRequest request = AiRequest.builder()
                .messages(List.of(new AiMessage("user", prompt)))
                .temperature(0.7)
                .maxTokens(1000)
                .build();
        
        AiResponse response = aiService.chat(request);
        
        // 按换行符分割，过滤空行
        List<String> outline = Arrays.stream(response.getContent().split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
        
        log.info("大纲生成完成, 共 {} 个要点", outline.size());
        return outline;
    }

    /**
     * 续写文章（流式输出）
     *
     * @param existingContent 已有内容
     * @return 续写内容流
     */
    public Flux<String> continueWriting(String existingContent) {
        log.info("开始续写文章, 已有内容长度: {}", existingContent.length());
        
        AiService aiService = aiServiceFactory.getDefaultService();
        String prompt = "请继续完成以下文章：\n\n" + existingContent;
        
        AiRequest request = AiRequest.builder()
                .messages(List.of(new AiMessage("user", prompt)))
                .temperature(0.8)
                .maxTokens(2000)
                .stream(true)
                .build();
        
        return aiService.chatStream(request)
                .doOnComplete(() -> log.info("续写完成"))
                .doOnError(e -> log.error("续写失败", e));
    }

    /**
     * 润色文章
     *
     * @param content 原始内容
     * @return 润色后的内容
     */
    public String polish(String content) {
        log.info("开始润色文章, 原文长度: {}", content.length());
        
        AiService aiService = aiServiceFactory.getDefaultService();
        String prompt = "请对以下文章进行润色，提升文字质量，但保持原意：\n\n" + content;
        
        AiRequest request = AiRequest.builder()
                .messages(List.of(new AiMessage("user", prompt)))
                .temperature(0.5)
                .maxTokens(content.length() * 2)
                .build();
        
        AiResponse response = aiService.chat(request);
        String polished = response.getContent();
        
        log.info("润色完成, 结果长度: {}", polished.length());
        return polished;
    }

    /**
     * 翻译文本
     *
     * @param content        待翻译内容
     * @param targetLanguage 目标语言
     * @return 翻译后的内容
     */
    public String translate(String content, String targetLanguage) {
        log.info("开始翻译, 目标语言: {}, 原文长度: {}", targetLanguage, content.length());
        
        AiService aiService = aiServiceFactory.getDefaultService();
        String prompt = String.format(
            "请将以下内容翻译成%s：\n\n%s",
            targetLanguage, content
        );
        
        AiRequest request = AiRequest.builder()
                .messages(List.of(new AiMessage("user", prompt)))
                .temperature(0.3)
                .maxTokens(content.length() * 3)
                .build();
        
        AiResponse response = aiService.chat(request);
        String translated = response.getContent();
        
        log.info("翻译完成, 结果长度: {}", translated.length());
        return translated;
    }
}
