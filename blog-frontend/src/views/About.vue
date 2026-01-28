<template>
  <div class="space-y-10">
    <div class="mx-auto w-full max-w-5xl space-y-8">
      <header class="text-center space-y-3">
        <h1 class="font-serif text-4xl font-medium text-ink">
          关于 Mulehang Blog
        </h1>
        <p class="text-ink-light">
          一个用于学习与分享的现代化博客系统
        </p>
      </header>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card class="border-ink/10 bg-paper-card shadow-soft">
          <CardHeader>
            <CardTitle class="font-serif text-2xl text-ink">
              项目简介
            </CardTitle>
          </CardHeader>
          <CardContent class="space-y-3">
            <p class="text-sm text-ink-light">
              项目以文章发布、标签分类与互动为核心，强调清晰的模块拆分与可扩展性。
            </p>
            <p class="text-sm text-ink-light">
              前端采用 Vue 3 与 Tailwind CSS，后端采用 Spring Boot
              并结合缓存与搜索方案。
            </p>
          </CardContent>
        </Card>

        <Card class="border-ink/10 bg-paper-card shadow-soft">
          <CardHeader>
            <CardTitle class="font-serif text-2xl text-ink">技术栈</CardTitle>
          </CardHeader>
          <CardContent class="grid grid-cols-2 gap-3 text-sm">
            <div class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink">
              Vue 3 + Vite
            </div>
            <div class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink">
              Pinia + Router
            </div>
            <div class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink">
              Tailwind CSS
            </div>
            <div class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink">
              shadcn-vue
            </div>
            <div class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink">
              Spring Boot 3
            </div>
            <div class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink">
              MyBatis-Plus
            </div>
            <div class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink">
              Redis + Caffeine
            </div>
            <div class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink">
              ElasticSearch
            </div>
          </CardContent>
        </Card>
      </div>

      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">功能亮点</CardTitle>
        </CardHeader>
        <CardContent class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div class="rounded-2xl border border-ink/10 bg-paper-dark px-4 py-3 text-ink">
            文章发布与 Markdown 渲染
          </div>
          <div class="rounded-2xl border border-ink/10 bg-paper-dark px-4 py-3 text-ink">
            标签与分类聚合浏览
          </div>
          <div class="rounded-2xl border border-ink/10 bg-paper-dark px-4 py-3 text-ink">
            站点统计与热门文章
          </div>
          <div class="rounded-2xl border border-ink/10 bg-paper-dark px-4 py-3 text-ink">
            缓存策略与热点数据优化
          </div>
        </CardContent>
      </Card>

      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">联系与反馈</CardTitle>
        </CardHeader>
        <CardContent class="space-y-2 text-sm text-ink-light">
          <p>如果你对项目有建议或想法，欢迎在 GitHub 留言。</p>
          <p>也可以通过文章评论与作者交流。</p>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
<script setup lang="ts">
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
</script>
