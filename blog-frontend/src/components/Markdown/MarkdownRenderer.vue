<template>
  <div class="markdown-body prose prose-lg max-w-none" v-html="renderedHtml"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

/**
 * 组件 Props
 */
const props = defineProps<{
  content: string
}>()

/**
 * 配置 Markdown-it 实例
 */
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
})

md.options.highlight = (str: string, lang?: string): string => {
  if (lang && hljs.getLanguage(lang)) {
    try {
      return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang }).value}</code></pre>`
    } catch (error) {
      console.error('代码高亮失败:', error)
    }
  }
  return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
}

/**
 * 渲染 Markdown 内容
 */
const renderedHtml = computed(() => {
  if (!props.content) return ''
  return md.render(props.content)
})
</script>

<style scoped>
@reference "../../style.css";

.markdown-body {
  @apply text-foreground leading-relaxed;
}

.markdown-body :deep(h1) {
  @apply text-3xl font-semibold mt-8 mb-4 pb-2 border-b border-border font-serif;
}

.markdown-body :deep(h2) {
  @apply text-2xl font-semibold mt-6 mb-3 pb-2 border-b border-border font-serif;
}

.markdown-body :deep(h3) {
  @apply text-xl font-semibold mt-5 mb-2 font-serif;
}

.markdown-body :deep(h4) {
  @apply text-lg font-semibold mt-4 mb-2 font-serif;
}

.markdown-body :deep(p) {
  @apply my-4;
}

.markdown-body :deep(a) {
  @apply text-primary hover:text-primary/80 underline;
}

.markdown-body :deep(ul) {
  @apply list-disc list-inside my-4 space-y-2;
}

.markdown-body :deep(ol) {
  @apply list-decimal list-inside my-4 space-y-2;
}

.markdown-body :deep(li) {
  @apply ml-4;
}

.markdown-body :deep(blockquote) {
  @apply border-l-4 border-muted pl-4 py-2 my-4 text-muted-foreground italic bg-muted;
}

.markdown-body :deep(code) {
  @apply bg-muted px-2 py-1 rounded text-sm font-mono text-primary;
}

.markdown-body :deep(pre) {
  @apply bg-card text-card-foreground p-4 rounded-xl overflow-x-auto my-4 border border-border;
}

.markdown-body :deep(pre code) {
  @apply bg-transparent text-inherit p-0;
}

.markdown-body :deep(table) {
  @apply w-full border-collapse my-4;
}

.markdown-body :deep(th) {
  @apply bg-muted border border-border px-4 py-2 text-left font-semibold;
}

.markdown-body :deep(td) {
  @apply border border-border px-4 py-2;
}

.markdown-body :deep(img) {
  @apply max-w-full h-auto rounded-lg shadow-md my-4;
}

.markdown-body :deep(hr) {
  @apply my-8 border-t border-border;
}
</style>
