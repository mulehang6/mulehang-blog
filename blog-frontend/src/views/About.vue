<template>
  <div class="min-h-screen bg-background text-foreground">
    <AppNavbar />
    <main class="container mx-auto px-4 py-8">
      <div class="max-w-5xl mx-auto space-y-8">
        <div class="text-center">
          <h1 class="text-3xl font-bold">关于 Mulehang Blog</h1>
          <p class="text-muted-foreground mt-2">
            一个用于学习与分享的现代化博客系统
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Card>
            <CardHeader>
              <CardTitle>项目简介</CardTitle>
            </CardHeader>
            <CardContent class="space-y-3">
              <p class="text-sm text-muted-foreground">
                项目以文章发布、标签分类与互动为核心，强调清晰的模块拆分与可扩展性。
              </p>
              <p class="text-sm text-muted-foreground">
                前端采用 Vue 3 与 Tailwind CSS，后端采用 Spring Boot 并结合缓存与搜索方案。
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>技术栈</CardTitle>
            </CardHeader>
            <CardContent class="grid grid-cols-2 gap-3 text-sm">
              <div class="rounded-md border bg-card px-3 py-2">Vue 3 + Vite</div>
              <div class="rounded-md border bg-card px-3 py-2">Pinia + Router</div>
              <div class="rounded-md border bg-card px-3 py-2">Tailwind CSS</div>
              <div class="rounded-md border bg-card px-3 py-2">shadcn-vue</div>
              <div class="rounded-md border bg-card px-3 py-2">Spring Boot 3</div>
              <div class="rounded-md border bg-card px-3 py-2">MyBatis-Plus</div>
              <div class="rounded-md border bg-card px-3 py-2">Redis + Caffeine</div>
              <div class="rounded-md border bg-card px-3 py-2">ElasticSearch</div>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>功能亮点</CardTitle>
          </CardHeader>
          <CardContent class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
            <div class="rounded-md border bg-card px-4 py-3">
              文章发布与 Markdown 渲染
            </div>
            <div class="rounded-md border bg-card px-4 py-3">
              标签与分类聚合浏览
            </div>
            <div class="rounded-md border bg-card px-4 py-3">
              站点统计与热门文章
            </div>
            <div class="rounded-md border bg-card px-4 py-3">
              缓存策略与热点数据优化
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>联系与反馈</CardTitle>
          </CardHeader>
          <CardContent class="space-y-2 text-sm text-muted-foreground">
            <p>如果你对项目有建议或想法，欢迎在 GitHub 留言。</p>
            <p>也可以通过文章评论与作者交流。</p>
          </CardContent>
        </Card>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import AppNavbar from "@/components/AppNavbar.vue";
import AppFooter from "@/components/AppFooter.vue";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
</script>
