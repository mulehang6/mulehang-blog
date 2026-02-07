package com.mulehang.blog.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sentinel 配置测试
 * 验证流控和熔断规则是否正确加载
 */
@DisplayName("Sentinel 配置测试")
class SentinelConfigTest {

    private SentinelConfig sentinelConfig;

    @BeforeEach
    void setUp() {
        sentinelConfig = new SentinelConfig();
        // 清空之前的规则
        FlowRuleManager.loadRules(List.of());
        DegradeRuleManager.loadRules(List.of());
    }

    @Test
    @DisplayName("应该成功初始化流控规则")
    void shouldInitFlowRules() {
        // When
        sentinelConfig.initRules();
        List<FlowRule> rules = FlowRuleManager.getRules();

        // Then
        assertNotNull(rules, "流控规则不应为 null");
        assertEquals(9, rules.size(), "应该加载 9 条流控规则");

        FlowRule aiChatRule = rules.stream()
                .filter(r -> "ai-chat".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(aiChatRule, "ai-chat 流控规则应该存在");
        assertEquals(RuleConstant.FLOW_GRADE_QPS, aiChatRule.getGrade(), "应该使用 QPS 限流");
        assertEquals(10.0, aiChatRule.getCount(), "QPS 限制应该为 10");

        FlowRule aiStreamRule = rules.stream()
                .filter(r -> "ai-chat-stream".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(aiStreamRule, "ai-chat-stream 流控规则应该存在");
        assertEquals(5.0, aiStreamRule.getCount(), "流式接口 QPS 限制应该为 5");

        FlowRule aiAssistantRule = rules.stream()
                .filter(r -> "ai-assistant".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(aiAssistantRule, "ai-assistant 流控规则应该存在");
        assertEquals(20.0, aiAssistantRule.getCount(), "助手功能 QPS 限制应该为 20");

        FlowRule aiWritingRule = rules.stream()
                .filter(r -> "ai-writing".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(aiWritingRule, "ai-writing 流控规则应该存在");
        assertEquals(10.0, aiWritingRule.getCount(), "写作助手 QPS 限制应该为 10");

        FlowRule aiWritingStreamRule = rules.stream()
                .filter(r -> "ai-writing-stream".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(aiWritingStreamRule, "ai-writing-stream 流控规则应该存在");
        assertEquals(5.0, aiWritingStreamRule.getCount(), "写作助手流式 QPS 限制应该为 5");

        FlowRule authLoginRule = rules.stream()
                .filter(r -> "auth-login".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(authLoginRule, "auth-login 流控规则应该存在");
        assertEquals(5.0, authLoginRule.getCount(), "登录 QPS 限制应该为 5");

        FlowRule authRegisterRule = rules.stream()
                .filter(r -> "auth-register".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(authRegisterRule, "auth-register 流控规则应该存在");
        assertEquals(3.0, authRegisterRule.getCount(), "注册 QPS 限制应该为 3");

        FlowRule commentCreateRule = rules.stream()
                .filter(r -> "comment-create".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(commentCreateRule, "comment-create 流控规则应该存在");
        assertEquals(10.0, commentCreateRule.getCount(), "评论 QPS 限制应该为 10");

        FlowRule fileUploadRule = rules.stream()
                .filter(r -> "file-upload".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(fileUploadRule, "file-upload 流控规则应该存在");
        assertEquals(5.0, fileUploadRule.getCount(), "上传 QPS 限制应该为 5");
    }

    @Test
    @DisplayName("应该成功初始化熔断规则")
    void shouldInitDegradeRules() {
        // When
        sentinelConfig.initRules();
        List<DegradeRule> rules = DegradeRuleManager.getRules();

        // Then
        assertNotNull(rules, "熔断规则不应为 null");
        assertEquals(5, rules.size(), "应该加载 5 条熔断规则");

        DegradeRule aiChatRule = rules.stream()
                .filter(r -> "ai-chat".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(aiChatRule, "ai-chat 熔断规则应该存在");
        assertEquals(CircuitBreakerStrategy.ERROR_RATIO.getType(), aiChatRule.getGrade(), 
                "应该使用错误率熔断策略");
        assertEquals(0.5, aiChatRule.getCount(), "错误率阈值应该为 50%");
        assertEquals(30, aiChatRule.getTimeWindow(), "熔断时间窗口应该为 30 秒");
        assertEquals(10, aiChatRule.getMinRequestAmount(), "最小请求数应该为 10");

        DegradeRule aiStreamRule = rules.stream()
                .filter(r -> "ai-chat-stream".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(aiStreamRule, "ai-chat-stream 熔断规则应该存在");
        assertEquals(5, aiStreamRule.getMinRequestAmount(), "流式接口最小请求数应该为 5");

        DegradeRule aiAssistantRule = rules.stream()
                .filter(r -> "ai-assistant".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(aiAssistantRule, "ai-assistant 熔断规则应该存在");
        assertEquals(0.6, aiAssistantRule.getCount(), "助手功能错误率阈值应该为 60%");
        assertEquals(20, aiAssistantRule.getTimeWindow(), "助手功能熔断时间窗口应该为 20 秒");

        DegradeRule aiWritingRule = rules.stream()
                .filter(r -> "ai-writing".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(aiWritingRule, "ai-writing 熔断规则应该存在");
        assertEquals(10, aiWritingRule.getMinRequestAmount(), "写作助手最小请求数应该为 10");

        DegradeRule aiWritingStreamRule = rules.stream()
                .filter(r -> "ai-writing-stream".equals(r.getResource()))
                .findFirst()
                .orElse(null);
        assertNotNull(aiWritingStreamRule, "ai-writing-stream 熔断规则应该存在");
        assertEquals(5, aiWritingStreamRule.getMinRequestAmount(), "写作助手流式最小请求数应该为 5");
    }

    @Test
    @DisplayName("规则应该按资源名称正确分组")
    void shouldGroupRulesByResource() {
        // When
        sentinelConfig.initRules();

        // Then
        List<FlowRule> flowRules = FlowRuleManager.getRules();
        List<DegradeRule> degradeRules = DegradeRuleManager.getRules();

        // 验证 AI 资源有对应的流控和熔断规则
        String[] aiResources = {"ai-chat", "ai-chat-stream", "ai-assistant", "ai-writing", "ai-writing-stream"};
        for (String resource : aiResources) {
            long flowCount = flowRules.stream()
                    .filter(r -> resource.equals(r.getResource()))
                    .count();
            long degradeCount = degradeRules.stream()
                    .filter(r -> resource.equals(r.getResource()))
                    .count();

            assertEquals(1, flowCount, resource + " 应该有 1 条流控规则");
            assertEquals(1, degradeCount, resource + " 应该有 1 条熔断规则");
        }

        // 验证新增业务资源仅有流控规则
        String[] extraFlowResources = {"auth-login", "auth-register", "comment-create", "file-upload"};
        for (String resource : extraFlowResources) {
            long flowCount = flowRules.stream()
                    .filter(r -> resource.equals(r.getResource()))
                    .count();
            long degradeCount = degradeRules.stream()
                    .filter(r -> resource.equals(r.getResource()))
                    .count();

            assertEquals(1, flowCount, resource + " 应该有 1 条流控规则");
            assertEquals(0, degradeCount, resource + " 不应配置熔断规则");
        }
    }

    @Test
    @DisplayName("流控规则的 QPS 阈值应该符合预期")
    void shouldHaveCorrectQpsThreshold() {
        // When
        sentinelConfig.initRules();
        List<FlowRule> rules = FlowRuleManager.getRules();

        // Then
        // ai-chat-stream 应该是最严格的（5 QPS）
        // ai-chat 中等严格（10 QPS）
        // ai-assistant 最宽松（20 QPS）
        FlowRule streamRule = findFlowRule(rules, "ai-chat-stream");
        FlowRule chatRule = findFlowRule(rules, "ai-chat");
        FlowRule assistantRule = findFlowRule(rules, "ai-assistant");

        assertTrue(streamRule.getCount() < chatRule.getCount(), 
                "流式接口 QPS 应该小于同步接口");
        assertTrue(chatRule.getCount() < assistantRule.getCount(), 
                "同步接口 QPS 应该小于助手功能");
    }

    private FlowRule findFlowRule(List<FlowRule> rules, String resource) {
        return rules.stream()
                .filter(r -> resource.equals(r.getResource()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到资源 " + resource + " 的流控规则"));
    }
}
