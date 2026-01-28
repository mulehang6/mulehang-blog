<template>
  <div class="space-y-10">
    <header class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div class="space-y-3">
        <h1 class="font-serif text-4xl font-medium text-ink">AI 工作台</h1>
        <p class="text-ink-light">
          对话、摘要、标题、标签与写作助手集中在一处完成。
        </p>
      </div>
      <Button
        variant="outline"
        size="sm"
        class="self-start rounded-full border-ink/20 text-ink"
        :disabled="!hasResults"
        @click="openResultPanel()"
      >
        查看结果
      </Button>
    </header>

    <!-- 连接设置 -->
    <Card class="border-ink/10 bg-paper-card shadow-soft">
      <CardHeader>
        <CardTitle class="font-serif text-2xl text-ink">连接设置</CardTitle>
      </CardHeader>
      <CardContent class="space-y-6">
        <div class="space-y-4">
          <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">Base URL</label>
              <Input
                v-model="aiBaseUrl"
                placeholder="https://api.openai.com"
                autocomplete="off"
              />
            </div>
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">模型 ID</label>
              <Input
                v-model="aiModel"
                placeholder="gpt-4o-mini"
                autocomplete="off"
              />
            </div>
            <div class="space-y-2 md:col-span-2">
              <label class="text-xs font-medium text-ink-light">API Key</label>
              <Input
                v-model="aiApiKey"
                type="password"
                placeholder="sk-..."
                autocomplete="off"
              />
              <label class="mt-2 flex items-center gap-2 text-xs text-ink-light">
                <input
                  v-model="rememberKey"
                  type="checkbox"
                  class="h-4 w-4 rounded border-ink/20 accent-clay"
                />
                记住密钥（仅保存在本地）
              </label>
            </div>
          </div>
          <p class="text-xs text-ink-light">
            填写 Base URL 时需同时提供 API Key；Model 会覆盖后端默认模型。
          </p>
          <div class="flex flex-wrap gap-2">
            <Button
              variant="outline"
              size="sm"
              class="rounded-full border-ink/20 text-ink"
              @click="clearOverrides"
            >
              恢复默认
            </Button>
          </div>
        </div>

        <div class="border-t border-ink/10 pt-4 space-y-4">
          <h3 class="text-sm font-semibold text-ink">对话设置</h3>
          <div class="grid grid-cols-1 gap-3 md:grid-cols-4">
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">模型提供商</label>
              <select
                v-model="chatProvider"
                class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              >
                <option value="">默认</option>
                <option value="openai">OpenAI</option>
                <option value="anthropic">Anthropic</option>
              </select>
            </div>
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">温度</label>
              <Input v-model.number="chatTemperature" type="number" step="0.1" min="0" max="2" />
            </div>
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">最大 Token</label>
              <Input v-model.number="chatMaxTokens" type="number" min="64" max="4096" />
            </div>
            <label
              class="mt-7 flex items-center gap-2 text-sm text-ink-light"
            >
              <input
                v-model="useChatStream"
                type="checkbox"
                class="h-4 w-4 rounded border-ink/20 accent-clay"
              />
              流式输出（需登录）
            </label>
          </div>
        </div>

        <div class="border-t border-ink/10 pt-4 space-y-4">
          <h3 class="text-sm font-semibold text-ink">内容助手设置</h3>
          <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">模型提供商</label>
              <select
                v-model="assistantProvider"
                class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              >
                <option value="">默认</option>
                <option value="openai">OpenAI</option>
                <option value="anthropic">Anthropic</option>
              </select>
            </div>
          </div>
        </div>

        <div class="border-t border-ink/10 pt-4 space-y-4">
          <h3 class="text-sm font-semibold text-ink">写作助手设置</h3>
          <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">模型提供商</label>
              <select
                v-model="writingProvider"
                class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              >
                <option value="">默认</option>
                <option value="openai">OpenAI</option>
                <option value="anthropic">Anthropic</option>
              </select>
            </div>
            <label class="mt-7 flex items-center gap-2 text-sm text-ink-light">
              <input
                v-model="useWritingStream"
                type="checkbox"
                class="h-4 w-4 rounded border-ink/20 accent-clay"
              />
              续写流式输出（需登录）
            </label>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- AI 对话 -->
    <Card class="border-ink/10 bg-paper-card shadow-soft">
      <CardHeader>
        <div class="flex flex-wrap items-center justify-between gap-3">
          <CardTitle class="font-serif text-2xl text-ink">AI 对话</CardTitle>
          <div class="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              class="rounded-full border-ink/20 text-ink"
              @click="resetChat"
            >
              清空对话
            </Button>
            <Button
              v-if="isChatting && useChatStream"
              variant="ghost"
              size="sm"
              class="rounded-full text-clay"
              @click="stopChatStream"
            >
              停止输出
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent class="space-y-4">
        <div
          ref="chatContainer"
          class="max-h-[420px] space-y-3 overflow-y-auto rounded-2xl border border-ink/10 bg-paper-dark/30 p-4"
        >
          <div v-if="chatMessages.length === 0" class="text-sm text-ink-light">
            写下第一句话，让 AI 接住你的想法。
          </div>
          <div
            v-for="(message, index) in chatMessages"
            :key="`${message.role}-${index}`"
            class="flex"
            :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
          >
            <div
              class="max-w-[80%] rounded-2xl px-4 py-3 text-sm leading-relaxed"
              :class="
                message.role === 'user'
                  ? 'bg-ink text-white'
                  : 'bg-paper-card text-ink border border-ink/10'
              "
            >
              <p v-if="message.role === 'user'" class="whitespace-pre-wrap">
                {{ message.content }}
              </p>
              <MarkdownRenderer
                v-else
                :content="message.content"
                class="prose-sm text-sm leading-relaxed"
              />
            </div>
          </div>
        </div>
        <div class="flex flex-col gap-3 md:flex-row">
          <Textarea
            v-model="chatInput"
            placeholder="输入你的问题..."
            class="min-h-20 flex-1"
            @keydown.enter.exact.prevent="sendChat"
          />
          <Button
            class="h-11 rounded-xl bg-ink px-6 text-white hover:bg-clay"
            :disabled="isChatting"
            @click="sendChat"
          >
            {{ isChatting ? "处理中..." : "发送" }}
          </Button>
        </div>
        <p v-if="useChatStream && !userStore.isLoggedIn" class="text-xs text-clay">
          流式对话需要登录，未登录时将自动使用同步模式。
        </p>
      </CardContent>
    </Card>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <!-- 内容助手 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
        <CardTitle class="font-serif text-2xl text-ink">内容助手</CardTitle>
      </CardHeader>
      <CardContent class="space-y-5">
          <Textarea
            v-model="assistantContent"
            placeholder="粘贴正文内容，生成摘要/标题/标签"
            class="min-h-32"
          />
          <div class="grid grid-cols-2 gap-3">
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">摘要长度</label>
              <Input v-model.number="assistantMaxLength" type="number" min="50" max="500" />
            </div>
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">标题/标签数量</label>
              <Input v-model.number="assistantCount" type="number" min="1" max="10" />
            </div>
          </div>
          <div class="flex flex-wrap gap-2">
            <Button
              class="rounded-full bg-ink text-white hover:bg-clay"
              :disabled="assistantLoading.summary"
              @click="handleSummary"
            >
              {{ assistantLoading.summary ? "生成中..." : "生成摘要" }}
            </Button>
            <Button
              variant="outline"
              class="rounded-full border-ink/20 text-ink"
              :disabled="assistantLoading.titles"
              @click="handleTitles"
            >
              {{ assistantLoading.titles ? "生成中..." : "推荐标题" }}
            </Button>
            <Button
              variant="outline"
              class="rounded-full border-ink/20 text-ink"
              :disabled="assistantLoading.tags"
              @click="handleTags"
            >
              {{ assistantLoading.tags ? "生成中..." : "推荐标签" }}
            </Button>
          </div>
        </CardContent>
      </Card>

      <!-- 写作助手 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">写作助手</CardTitle>
        </CardHeader>
        <CardContent class="space-y-5">
          <div class="space-y-2">
            <label class="text-xs font-medium text-ink-light">文章主题</label>
            <Input v-model="outlineTopic" placeholder="例如：Java 21 新特性总结" />
          </div>
          <Button
            variant="outline"
            class="rounded-full border-ink/20 text-ink"
            :disabled="writingLoading.outline"
            @click="handleOutline"
          >
            {{ writingLoading.outline ? "生成中..." : "生成大纲" }}
          </Button>
          <Textarea
            v-model="writingContent"
            placeholder="输入文章内容，用于续写/润色/翻译"
            class="min-h-32"
          />
          <div class="flex flex-wrap gap-2">
            <Button
              class="rounded-full bg-ink text-white hover:bg-clay"
              :disabled="writingLoading.continue"
              @click="handleContinue"
            >
              {{ writingLoading.continue ? "处理中..." : "续写文章" }}
            </Button>
            <Button
              variant="outline"
              class="rounded-full border-ink/20 text-ink"
              :disabled="writingLoading.polish"
              @click="handlePolish"
            >
              {{ writingLoading.polish ? "处理中..." : "润色" }}
            </Button>
            <Button
              variant="outline"
              class="rounded-full border-ink/20 text-ink"
              :disabled="writingLoading.translate"
              @click="handleTranslate"
            >
              {{ writingLoading.translate ? "处理中..." : "翻译" }}
            </Button>
            <Button
              v-if="writingLoading.continue && useWritingStream"
              variant="ghost"
              class="rounded-full text-clay"
              @click="stopWritingStream"
            >
              停止续写
            </Button>
          </div>
          <div class="space-y-2">
            <label class="text-xs font-medium text-ink-light">翻译目标语言</label>
            <Input v-model="targetLanguage" placeholder="例如：英文 / 日文" />
          </div>
          <p v-if="useWritingStream && !userStore.isLoggedIn" class="text-xs text-clay">
            续写流式输出需要登录，未登录时将自动使用同步模式。
          </p>
        </CardContent>
      </Card>
    </div>

    <Sheet :open="isResultPanelOpen" @update:open="setResultPanelOpen">
      <SheetContent
        side="right"
        class="w-full border-ink/10 bg-paper-card p-0 sm:max-w-md"
      >
        <SheetHeader class="border-b border-ink/10 pr-12">
          <div class="flex items-start justify-between gap-3">
            <div class="space-y-1">
              <SheetTitle class="font-serif text-2xl text-ink">生成结果</SheetTitle>
              <SheetDescription class="text-xs text-ink-light">
                生成内容集中展示，可折叠收起。
              </SheetDescription>
            </div>
            <Button
              variant="outline"
              size="sm"
              class="rounded-full border-ink/20 text-ink"
              :disabled="!hasResults"
              @click="clearResults"
            >
              清空结果
            </Button>
          </div>
        </SheetHeader>
        <div class="space-y-3 p-4">
          <div v-if="!hasResults" class="text-sm text-ink-light">
            暂无生成结果。
          </div>

          <Collapsible
            v-if="assistantSummary"
            :open="resultSections.summary"
            class="space-y-2"
            @update:open="(value) => (resultSections.summary = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>摘要</span>
              <ChevronDown
                class="size-4 transition-transform"
                :class="resultSections.summary ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent
              class="rounded-xl border border-ink/10 bg-paper-card p-3 text-sm text-ink-light whitespace-pre-wrap"
            >
              {{ assistantSummary }}
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="assistantTitles.length"
            :open="resultSections.titles"
            class="space-y-2"
            @update:open="(value) => (resultSections.titles = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>标题建议</span>
              <ChevronDown
                class="size-4 transition-transform"
                :class="resultSections.titles ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent class="rounded-xl border border-ink/10 bg-paper-card p-3">
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="title in assistantTitles"
                  :key="title"
                  class="rounded-full border border-ink/10 bg-paper-dark/60 px-3 py-1 text-xs text-ink"
                >
                  {{ title }}
                </span>
              </div>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="assistantTags.length"
            :open="resultSections.tags"
            class="space-y-2"
            @update:open="(value) => (resultSections.tags = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>标签建议</span>
              <ChevronDown
                class="size-4 transition-transform"
                :class="resultSections.tags ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent class="rounded-xl border border-ink/10 bg-paper-card p-3">
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="tag in assistantTags"
                  :key="tag"
                  class="rounded-full border border-ink/10 bg-paper-card px-3 py-1 text-xs text-ink-light"
                >
                  # {{ tag }}
                </span>
              </div>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="outline.length"
            :open="resultSections.outline"
            class="space-y-2"
            @update:open="(value) => (resultSections.outline = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>大纲</span>
              <ChevronDown
                class="size-4 transition-transform"
                :class="resultSections.outline ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent class="rounded-xl border border-ink/10 bg-paper-card p-3">
              <ul class="list-disc pl-5 text-sm text-ink-light">
                <li v-for="item in outline" :key="item">{{ item }}</li>
              </ul>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="writingLoading.continue || continueOutput"
            :open="resultSections.continuation"
            class="space-y-2"
            @update:open="(value) => (resultSections.continuation = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>续写结果</span>
              <ChevronDown
                class="size-4 transition-transform"
                :class="resultSections.continuation ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent class="rounded-xl border border-ink/10 bg-paper-card p-3">
              <p v-if="continueOutput" class="text-sm text-ink-light whitespace-pre-wrap">
                {{ continueOutput }}
              </p>
              <p v-else class="text-sm text-ink-light">续写生成中...</p>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="polishOutput"
            :open="resultSections.polish"
            class="space-y-2"
            @update:open="(value) => (resultSections.polish = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>润色结果</span>
              <ChevronDown
                class="size-4 transition-transform"
                :class="resultSections.polish ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent class="rounded-xl border border-ink/10 bg-paper-card p-3">
              <p class="text-sm text-ink-light whitespace-pre-wrap">{{ polishOutput }}</p>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="translateOutput"
            :open="resultSections.translate"
            class="space-y-2"
            @update:open="(value) => (resultSections.translate = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>翻译结果</span>
              <ChevronDown
                class="size-4 transition-transform"
                :class="resultSections.translate ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent class="rounded-xl border border-ink/10 bg-paper-card p-3">
              <p class="text-sm text-ink-light whitespace-pre-wrap">{{ translateOutput }}</p>
            </CollapsibleContent>
          </Collapsible>
        </div>
      </SheetContent>
    </Sheet>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, reactive, ref, watch } from "vue";
import { toast } from "vue-sonner";
import { aiApi } from "@/api/ai";
import type { AiMessage, AiChatRequest } from "@/types/api";
import { useUserStore } from "@/stores/user";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import { Input } from "@/components/ui/input";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Textarea } from "@/components/ui/textarea";
import MarkdownRenderer from "@/components/Markdown/MarkdownRenderer.vue";
import { ChevronDown } from "lucide-vue-next";

type ResultSectionKey =
  | "summary"
  | "titles"
  | "tags"
  | "outline"
  | "continuation"
  | "polish"
  | "translate";

const userStore = useUserStore();

const storageKeys = {
  baseUrl: "ai.baseUrl",
  model: "ai.model",
  apiKey: "ai.apiKey",
  rememberKey: "ai.rememberKey",
};

const aiBaseUrl = ref(localStorage.getItem(storageKeys.baseUrl) || "");
const aiModel = ref(localStorage.getItem(storageKeys.model) || "");
const storedRememberKey = localStorage.getItem(storageKeys.rememberKey) === "true";
const rememberKey = ref(storedRememberKey);
const aiApiKey = ref(
  storedRememberKey
    ? localStorage.getItem(storageKeys.apiKey) || ""
    : sessionStorage.getItem(storageKeys.apiKey) || "",
);

const chatMessages = ref<AiMessage[]>([]);
const chatInput = ref("");
const chatProvider = ref("");
const chatTemperature = ref(0.7);
const chatMaxTokens = ref(800);
const useChatStream = ref(true);
const isChatting = ref(false);
const chatController = ref<AbortController | null>(null);
const chatContainer = ref<HTMLElement | null>(null);

const assistantContent = ref("");
const assistantSummary = ref("");
const assistantTitles = ref<string[]>([]);
const assistantTags = ref<string[]>([]);
const assistantMaxLength = ref(200);
const assistantCount = ref(3);
const assistantProvider = ref("");
const assistantLoading = ref({
  summary: false,
  titles: false,
  tags: false,
});

const outlineTopic = ref("");
const outline = ref<string[]>([]);
const writingContent = ref("");
const continueOutput = ref("");
const polishOutput = ref("");
const translateOutput = ref("");
const targetLanguage = ref("英文");
const writingProvider = ref("");
const useWritingStream = ref(true);
const writingController = ref<AbortController | null>(null);
const writingLoading = ref({
  outline: false,
  continue: false,
  polish: false,
  translate: false,
});

const isResultPanelOpen = ref(false);
const resultSections = reactive<Record<ResultSectionKey, boolean>>({
  summary: true,
  titles: true,
  tags: true,
  outline: true,
  continuation: true,
  polish: true,
  translate: true,
});
const hasResults = computed(
  () =>
    Boolean(assistantSummary.value)
    || assistantTitles.value.length > 0
    || assistantTags.value.length > 0
    || outline.value.length > 0
    || Boolean(continueOutput.value)
    || Boolean(polishOutput.value)
    || Boolean(translateOutput.value),
);

watch(
  () => chatMessages.value.length,
  () => {
    scrollChatToBottom();
  },
);

/**
 * 滚动聊天窗口到底部。
 */
function scrollChatToBottom() {
  nextTick(() => {
    if (!chatContainer.value) return;
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  });
}

watch(aiBaseUrl, (value) => {
  if (value) {
    localStorage.setItem(storageKeys.baseUrl, value);
  } else {
    localStorage.removeItem(storageKeys.baseUrl);
  }
});

watch(aiModel, (value) => {
  if (value) {
    localStorage.setItem(storageKeys.model, value);
  } else {
    localStorage.removeItem(storageKeys.model);
  }
});

watch(rememberKey, (value) => {
  localStorage.setItem(storageKeys.rememberKey, String(value));
  if (value) {
    sessionStorage.removeItem(storageKeys.apiKey);
    if (aiApiKey.value) {
      localStorage.setItem(storageKeys.apiKey, aiApiKey.value);
    } else {
      localStorage.removeItem(storageKeys.apiKey);
    }
    return;
  }
  localStorage.removeItem(storageKeys.apiKey);
  if (aiApiKey.value) {
    sessionStorage.setItem(storageKeys.apiKey, aiApiKey.value);
  } else {
    sessionStorage.removeItem(storageKeys.apiKey);
  }
});

watch(aiApiKey, (value) => {
  const storage = rememberKey.value ? localStorage : sessionStorage;
  if (value) {
    storage.setItem(storageKeys.apiKey, value);
  } else {
    storage.removeItem(storageKeys.apiKey);
  }
});

/**
 * 控制结果面板的显示状态。
 */
function setResultPanelOpen(value: boolean) {
  isResultPanelOpen.value = value;
}

/**
 * 打开结果面板并展开指定分区。
 */
function openResultPanel(section?: ResultSectionKey) {
  isResultPanelOpen.value = true;
  if (section) {
    resultSections[section] = true;
  }
}

/**
 * 清空已生成的结果内容。
 */
function clearResults() {
  assistantSummary.value = "";
  assistantTitles.value = [];
  assistantTags.value = [];
  outline.value = [];
  continueOutput.value = "";
  polishOutput.value = "";
  translateOutput.value = "";
  isResultPanelOpen.value = false;
}

function resetChat() {
  chatMessages.value = [];
  chatInput.value = "";
  stopChatStream();
}

function stopChatStream() {
  if (chatController.value) {
    chatController.value.abort();
    chatController.value = null;
  }
  isChatting.value = false;
}

function clearOverrides() {
  aiBaseUrl.value = "";
  aiModel.value = "";
  aiApiKey.value = "";
  rememberKey.value = false;
}

function buildOverrides() {
  const baseUrl = aiBaseUrl.value.trim();
  const model = aiModel.value.trim();
  const apiKey = aiApiKey.value.trim();

  if (baseUrl && !/^https?:\/\//i.test(baseUrl)) {
    toast.error("Base URL 需以 http(s):// 开头");
    return null;
  }
  if (baseUrl && !apiKey) {
    toast.error("使用自定义 Base URL 需要 API Key");
    return null;
  }

  return {
    baseUrl: baseUrl || undefined,
    model: model || undefined,
    apiKey: apiKey || undefined,
  };
}

async function sendChat() {
  const content = chatInput.value.trim();
  if (!content || isChatting.value) return;

  const overrides = buildOverrides();
  if (!overrides) return;

  const nextMessages: AiMessage[] = [...chatMessages.value, { role: "user", content }];
  chatMessages.value = nextMessages;
  chatInput.value = "";

  const payload: AiChatRequest = {
    messages: nextMessages,
    temperature: Number.isFinite(chatTemperature.value)
      ? chatTemperature.value
      : undefined,
    maxTokens: Number.isFinite(chatMaxTokens.value)
      ? chatMaxTokens.value
      : undefined,
    provider: chatProvider.value || undefined,
    ...overrides,
  };

  if (useChatStream.value && !userStore.isLoggedIn) {
    useChatStream.value = false;
  }

  if (useChatStream.value) {
    chatMessages.value = [...chatMessages.value, { role: "assistant", content: "" }];
    const assistantIndex = chatMessages.value.length - 1;
    isChatting.value = true;
    chatController.value = aiApi.chatStream(payload, {
      onMessage: (chunk) => {
        chatMessages.value[assistantIndex].content += chunk;
        scrollChatToBottom();
      },
      onDone: () => {
        isChatting.value = false;
        chatController.value = null;
        scrollChatToBottom();
      },
      onError: (message) => {
        toast.error("流式对话失败", { description: message });
        isChatting.value = false;
        chatController.value = null;
      },
    });
    return;
  }

  isChatting.value = true;
  try {
    const response = await aiApi.chat(payload);
    chatMessages.value = [...chatMessages.value, { role: "assistant", content: response }];
  } catch (err: any) {
    toast.error("对话失败", {
      description: err.message || "请稍后重试",
    });
  } finally {
    isChatting.value = false;
  }
}

async function handleSummary() {
  if (!assistantContent.value.trim()) {
    toast.error("请输入文章内容");
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  assistantLoading.value.summary = true;
  try {
    assistantSummary.value = await aiApi.generateSummary({
      content: assistantContent.value,
      maxLength: assistantMaxLength.value || undefined,
      provider: assistantProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("summary");
  } catch (err: any) {
    toast.error("生成摘要失败", { description: err.message || "请稍后重试" });
  } finally {
    assistantLoading.value.summary = false;
  }
}

async function handleTitles() {
  if (!assistantContent.value.trim()) {
    toast.error("请输入文章内容");
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  assistantLoading.value.titles = true;
  try {
    assistantTitles.value = await aiApi.suggestTitles({
      content: assistantContent.value,
      count: assistantCount.value || undefined,
      provider: assistantProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("titles");
  } catch (err: any) {
    toast.error("推荐标题失败", { description: err.message || "请稍后重试" });
  } finally {
    assistantLoading.value.titles = false;
  }
}

async function handleTags() {
  if (!assistantContent.value.trim()) {
    toast.error("请输入文章内容");
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  assistantLoading.value.tags = true;
  try {
    assistantTags.value = await aiApi.suggestTags({
      content: assistantContent.value,
      count: assistantCount.value || undefined,
      provider: assistantProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("tags");
  } catch (err: any) {
    toast.error("推荐标签失败", { description: err.message || "请稍后重试" });
  } finally {
    assistantLoading.value.tags = false;
  }
}

async function handleOutline() {
  if (!outlineTopic.value.trim()) {
    toast.error("请输入文章主题");
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  writingLoading.value.outline = true;
  try {
    outline.value = await aiApi.generateOutline({
      topic: outlineTopic.value,
      provider: writingProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("outline");
  } catch (err: any) {
    toast.error("生成大纲失败", { description: err.message || "请稍后重试" });
  } finally {
    writingLoading.value.outline = false;
  }
}

async function handleContinue() {
  if (!writingContent.value.trim()) {
    toast.error("请输入文章内容");
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;

  if (useWritingStream.value && !userStore.isLoggedIn) {
    useWritingStream.value = false;
  }

  if (useWritingStream.value) {
    continueOutput.value = "";
    writingLoading.value.continue = true;
    openResultPanel("continuation");
    writingController.value = aiApi.continueWritingStream(
      {
        content: writingContent.value,
        provider: writingProvider.value || undefined,
        ...overrides,
      },
      {
        onMessage: (chunk) => {
          continueOutput.value += chunk;
        },
        onDone: () => {
          writingLoading.value.continue = false;
          writingController.value = null;
        },
        onError: (message) => {
          toast.error("续写失败", { description: message });
          writingLoading.value.continue = false;
          writingController.value = null;
        },
      },
    );
    return;
  }

  writingLoading.value.continue = true;
  try {
    continueOutput.value = await aiApi.continueWriting({
      content: writingContent.value,
      provider: writingProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("continuation");
  } catch (err: any) {
    toast.error("续写失败", { description: err.message || "请稍后重试" });
  } finally {
    writingLoading.value.continue = false;
  }
}

function stopWritingStream() {
  if (writingController.value) {
    writingController.value.abort();
    writingController.value = null;
  }
  writingLoading.value.continue = false;
}

onBeforeUnmount(() => {
  stopChatStream();
  stopWritingStream();
});

async function handlePolish() {
  if (!writingContent.value.trim()) {
    toast.error("请输入文章内容");
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  writingLoading.value.polish = true;
  try {
    polishOutput.value = await aiApi.polish({
      content: writingContent.value,
      provider: writingProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("polish");
  } catch (err: any) {
    toast.error("润色失败", { description: err.message || "请稍后重试" });
  } finally {
    writingLoading.value.polish = false;
  }
}

async function handleTranslate() {
  if (!writingContent.value.trim()) {
    toast.error("请输入文章内容");
    return;
  }
  if (!targetLanguage.value.trim()) {
    toast.error("请输入目标语言");
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  writingLoading.value.translate = true;
  try {
    translateOutput.value = await aiApi.translate({
      content: writingContent.value,
      targetLanguage: targetLanguage.value,
      provider: writingProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("translate");
  } catch (err: any) {
    toast.error("翻译失败", { description: err.message || "请稍后重试" });
  } finally {
    writingLoading.value.translate = false;
  }
}
</script>
