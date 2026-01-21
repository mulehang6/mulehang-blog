package com.mulehang.blog.ai;

import com.mulehang.blog.ai.config.AiProperties;
import com.mulehang.blog.ai.provider.AnthropicProvider;
import com.mulehang.blog.ai.provider.OpenAiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 服务工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceFactory {
    
    private final AiProperties aiProperties;
    private final Map<String, AiService> providerCache = new ConcurrentHashMap<>();

    /**
     * 获取默认 AI 服务
     */
    public AiService getDefaultService() {
        String provider = aiProperties.getDefaultProvider();
        return getService(provider);
    }

    /**
     * 获取指定的 AI 服务
     */
    public AiService getService(String providerName) {
        return providerCache.computeIfAbsent(providerName, this::createProvider);
    }

    private AiService createProvider(String providerName) {
        if (aiProperties.getProviders() == null || !aiProperties.getProviders().containsKey(providerName)) {
            log.error("Provider '{}' not configured", providerName);
            throw new IllegalArgumentException("Provider not configured: " + providerName);
        }

        AiProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        
        switch (providerName.toLowerCase()) {
            case "openai":
                log.info("Creating OpenAI provider with baseUrl: {}", config.getBaseUrl());
                return new OpenAiProvider(config);
            case "anthropic":
                log.info("Creating Anthropic provider with baseUrl: {}", config.getBaseUrl());
                return new AnthropicProvider(config);
            default:
                log.warn("Unknown provider '{}', treating as OpenAI-compatible", providerName);
                return new OpenAiProvider(config);
        }
    }
}
