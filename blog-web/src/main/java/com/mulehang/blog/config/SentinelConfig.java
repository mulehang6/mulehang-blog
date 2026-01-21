package com.mulehang.blog.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 流控和熔断配置
 * 为 AI 接口提供流量控制和降级保护
 */
@Slf4j
@Configuration
public class SentinelConfig {

    /**
     * 初始化 Sentinel 规则
     */
    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
        log.info("Sentinel 流控和熔断规则初始化完成");
    }

    /**
     * 初始化流控规则
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // AI 同步对话接口限流规则
        FlowRule aiChatRule = new FlowRule();
        aiChatRule.setResource("ai-chat");
        aiChatRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        aiChatRule.setCount(10);  // 每秒最多10次请求
        aiChatRule.setLimitApp("default");
        rules.add(aiChatRule);

        // AI 流式对话接口限流规则
        FlowRule aiStreamRule = new FlowRule();
        aiStreamRule.setResource("ai-chat-stream");
        aiStreamRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        aiStreamRule.setCount(5);  // 流式接口更严格，每秒最多5次
        aiStreamRule.setLimitApp("default");
        rules.add(aiStreamRule);

        // AI 助手功能限流规则（摘要、标题、标签）
        FlowRule aiAssistantRule = new FlowRule();
        aiAssistantRule.setResource("ai-assistant");
        aiAssistantRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        aiAssistantRule.setCount(20);  // 助手功能相对宽松
        aiAssistantRule.setLimitApp("default");
        rules.add(aiAssistantRule);

        // AI 写作助手限流规则（大纲、续写、润色、翻译）
        FlowRule aiWritingRule = new FlowRule();
        aiWritingRule.setResource("ai-writing");
        aiWritingRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        aiWritingRule.setCount(10);  // 写作助手每秒10次
        aiWritingRule.setLimitApp("default");
        rules.add(aiWritingRule);

        // AI 写作助手流式接口限流规则
        FlowRule aiWritingStreamRule = new FlowRule();
        aiWritingStreamRule.setResource("ai-writing-stream");
        aiWritingStreamRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        aiWritingStreamRule.setCount(5);  // 流式接口更严格
        aiWritingStreamRule.setLimitApp("default");
        rules.add(aiWritingStreamRule);

        FlowRuleManager.loadRules(rules);
        log.info("已加载 {} 条流控规则", rules.size());
    }

    /**
     * 初始化熔断降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // AI 对话接口熔断规则（错误率策略）
        DegradeRule aiChatDegradeRule = new DegradeRule();
        aiChatDegradeRule.setResource("ai-chat");
        aiChatDegradeRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        aiChatDegradeRule.setCount(0.5);  // 错误率超过50%时触发熔断
        aiChatDegradeRule.setTimeWindow(30);  // 熔断持续30秒
        aiChatDegradeRule.setMinRequestAmount(10);  // 最少10次请求才开始统计
        aiChatDegradeRule.setStatIntervalMs(1000);  // 统计时间窗口1秒
        rules.add(aiChatDegradeRule);

        // AI 流式对话接口熔断规则
        DegradeRule aiStreamDegradeRule = new DegradeRule();
        aiStreamDegradeRule.setResource("ai-chat-stream");
        aiStreamDegradeRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        aiStreamDegradeRule.setCount(0.5);
        aiStreamDegradeRule.setTimeWindow(30);
        aiStreamDegradeRule.setMinRequestAmount(5);
        aiStreamDegradeRule.setStatIntervalMs(1000);
        rules.add(aiStreamDegradeRule);

        // AI 助手功能熔断规则
        DegradeRule aiAssistantDegradeRule = new DegradeRule();
        aiAssistantDegradeRule.setResource("ai-assistant");
        aiAssistantDegradeRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        aiAssistantDegradeRule.setCount(0.6);  // 助手功能容错率更高
        aiAssistantDegradeRule.setTimeWindow(20);
        aiAssistantDegradeRule.setMinRequestAmount(10);
        aiAssistantDegradeRule.setStatIntervalMs(1000);
        rules.add(aiAssistantDegradeRule);

        // AI 写作助手熔断规则
        DegradeRule aiWritingDegradeRule = new DegradeRule();
        aiWritingDegradeRule.setResource("ai-writing");
        aiWritingDegradeRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        aiWritingDegradeRule.setCount(0.5);
        aiWritingDegradeRule.setTimeWindow(30);
        aiWritingDegradeRule.setMinRequestAmount(10);
        aiWritingDegradeRule.setStatIntervalMs(1000);
        rules.add(aiWritingDegradeRule);

        // AI 写作助手流式接口熔断规则
        DegradeRule aiWritingStreamDegradeRule = new DegradeRule();
        aiWritingStreamDegradeRule.setResource("ai-writing-stream");
        aiWritingStreamDegradeRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        aiWritingStreamDegradeRule.setCount(0.5);
        aiWritingStreamDegradeRule.setTimeWindow(30);
        aiWritingStreamDegradeRule.setMinRequestAmount(5);
        aiWritingStreamDegradeRule.setStatIntervalMs(1000);
        rules.add(aiWritingStreamDegradeRule);

        DegradeRuleManager.loadRules(rules);
        log.info("已加载 {} 条熔断降级规则", rules.size());
    }
}
