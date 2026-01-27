<template>
  <div class="space-y-6">
    <!-- 顶部标题区域 -->
    <div
      class="flex flex-col md:flex-row md:items-center justify-between gap-4"
    >
      <div>
        <h1 class="text-3xl font-bold tracking-tight">
          {{ localeStore.t.latestArticles }}
        </h1>
        <p class="text-muted-foreground mt-1">
          {{ localeStore.t.latestSubtitle }}
        </p>
      </div>
      <Button
        variant="outline"
        size="sm"
        @click="loadArticles"
        :disabled="loading"
        class="gap-2 glass-button"
      >
        <span :class="{ 'animate-spin': loading }">↻</span>
        {{ localeStore.t.refresh }}
      </Button>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8">
      <!-- 文章列表 (左侧) -->
      <div class="lg:col-span-8 space-y-6">
        <!-- 加载中状态 -->
        <div v-if="loading && articles.length === 0" class="space-y-6">
          <Card v-for="i in 3" :key="i" class="animate-pulse">
            <CardHeader>
              <div class="h-6 bg-muted rounded w-3/4"></div>
              <div class="h-4 bg-muted rounded w-1/2 mt-2"></div>
            </CardHeader>
            <CardContent>
              <div class="space-y-2">
                <div class="h-4 bg-muted rounded"></div>
                <div class="h-4 bg-muted rounded w-5/6"></div>
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- 文章列表 -->
        <div v-else-if="articles.length > 0" class="space-y-4">
          <Card
            v-for="article in articles"
            :key="article.id"
            class="group hover:border-primary/50 transition-all duration-300 cursor-pointer overflow-hidden border-border/60 hover-lift-sheen"
            @click="router.push(`/articles/${article.slug}`)"
          >
            <CardHeader class="pb-3">
              <div class="flex items-start justify-between gap-4">
                <div class="space-y-1">
                  <CardTitle
                    class="text-xl font-bold group-hover:text-primary transition-colors"
                  >
                    {{ article.title }}
                  </CardTitle>
                  <div
                    class="flex items-center gap-2 text-xs text-muted-foreground"
                  >
                    <Badge
                      v-if="article.category"
                      variant="secondary"
                      class="rounded-sm font-normal"
                    >
                      {{ article.category.name }}
                    </Badge>
                    <span v-if="article.author" class="flex items-center gap-1">
                      <span
                        class="w-1 h-1 rounded-full bg-muted-foreground"
                      ></span>
                      {{ article.author.username }}
                    </span>
                    <span class="flex items-center gap-1">
                      <span
                        class="w-1 h-1 rounded-full bg-muted-foreground"
                      ></span>
                      {{ formatDate(article.createTime) }}
                    </span>
                  </div>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <p
                class="text-muted-foreground text-sm line-clamp-2 leading-relaxed"
              >
                {{ article.summary }}
              </p>

              <div
                class="flex items-center justify-between mt-4 pt-4 border-t border-border/40"
              >
                <div class="flex items-center gap-2">
                  <Badge
                    v-for="tag in article.tags?.slice(0, 3)"
                    :key="tag.id"
                    variant="outline"
                    class="text-xs text-muted-foreground border-border/60"
                  >
                    #{{ tag.name }}
                  </Badge>
                </div>
                <div
                  class="flex items-center gap-4 text-xs text-muted-foreground"
                >
                  <span class="flex items-center gap-1"
                    >👁️ {{ article.readCount || 0 }}</span
                  >
                  <span class="flex items-center gap-1"
                    >❤️ {{ article.likeCount || 0 }}</span
                  >
                  <span class="flex items-center gap-1"
                    >💬 {{ article.commentCount || 0 }}</span
                  >
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- 空状态 -->
        <Card v-else class="border-dashed">
          <CardContent class="py-12 text-center">
            <div class="text-4xl mb-4">📭</div>
            <p class="text-muted-foreground text-lg">
              {{ localeStore.t.emptyArticles }}
            </p>
          </CardContent>
        </Card>

        <!-- 分页 -->
        <div
          v-if="totalPages > 1"
          class="flex items-center justify-center gap-2 pt-4"
        >
          <Button
            variant="outline"
            size="sm"
            :disabled="currentPage === 1"
            @click="changePage(currentPage - 1)"
          >
            {{ localeStore.t.previous }}
          </Button>
          <span class="text-sm text-muted-foreground px-2">
            Page {{ currentPage }} of {{ totalPages }}
          </span>
          <Button
            variant="outline"
            size="sm"
            :disabled="currentPage === totalPages"
            @click="changePage(currentPage + 1)"
          >
            {{ localeStore.t.next }}
          </Button>
        </div>
      </div>

      <!-- 侧边栏 (右侧) -->
      <div class="lg:col-span-4 space-y-6">
        <!-- 热门文章 -->
        <Card>
          <CardHeader>
            <CardTitle class="text-lg flex items-center gap-2">
              🔥 {{ localeStore.t.trending }}
            </CardTitle>
          </CardHeader>
          <CardContent class="grid gap-4">
            <div v-if="hotArticles.length > 0" class="space-y-3">
              <div
                v-for="(article, index) in hotArticles"
                :key="article.id"
                class="flex items-start gap-3 group cursor-pointer hover-lift-sheen-sm"
                @click="router.push(`/articles/${article.slug}`)"
              >
                <span
                  class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-muted text-xs font-medium text-muted-foreground group-hover:bg-primary group-hover:text-primary-foreground transition-colors"
                >
                  {{ index + 1 }}
                </span>
                <div class="space-y-1">
                  <p
                    class="text-sm font-medium leading-none group-hover:text-primary transition-colors line-clamp-2"
                  >
                    {{ article.title }}
                  </p>
                  <div
                    class="flex items-center gap-2 text-xs text-muted-foreground"
                  >
                    <span
                      >{{ article.readCount || 0 }}
                      {{ localeStore.t.reads }}</span
                    >
                    <span
                      >{{ article.likeCount || 0 }}
                      {{ localeStore.t.likes }}</span
                    >
                  </div>
                </div>
              </div>
            </div>
            <p v-else class="text-sm text-muted-foreground text-center py-4">
              {{ localeStore.t.noTrending }}
            </p>
          </CardContent>
        </Card>

        <!-- 文章分类 -->
        <CategoryWidget />

        <!-- 热门标签 -->
        <TagCloud />

        <!-- 统计信息 -->
        <Card class="hover-lift-sheen">
          <CardHeader>
            <CardTitle class="text-lg flex items-center gap-2">
              📊 {{ localeStore.t.statistics }}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1">
                <p class="text-xs text-muted-foreground">
                  {{ localeStore.t.statArticles }}
                </p>
                <p class="text-2xl font-bold">
                  {{ siteStats.totalArticles || 0 }}
                </p>
              </div>
              <div class="space-y-1">
                <p class="text-xs text-muted-foreground">
                  {{ localeStore.t.statReads }}
                </p>
                <p class="text-2xl font-bold">
                  {{ siteStats.totalReads || 0 }}
                </p>
              </div>
              <div class="space-y-1">
                <p class="text-xs text-muted-foreground">
                  {{ localeStore.t.statLikes }}
                </p>
                <p class="text-2xl font-bold">
                  {{ siteStats.totalLikes || 0 }}
                </p>
              </div>
              <div class="space-y-1">
                <p class="text-xs text-muted-foreground">
                  {{ localeStore.t.statTodayVisits }}
                </p>
                <p class="text-2xl font-bold">{{ siteStats.todayPV || 0 }}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { articleApi } from "@/api/article";
import { recordPageView, getSiteStats } from "@/api/stats";
import { useLocaleStore } from "@/stores/locale";
import type { ArticleListItem, SiteStats } from "@/types/api";
import CategoryWidget from "@/components/Sidebar/CategoryWidget.vue";
import TagCloud from "@/components/Sidebar/TagCloud.vue";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

const router = useRouter();
const localeStore = useLocaleStore();

// 文章列表状态
const articles = ref<ArticleListItem[]>([]);
const hotArticles = ref<ArticleListItem[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const totalPages = ref(0);

// 网站统计数据
const siteStats = ref<SiteStats>({
  totalArticles: 0,
  totalReads: 0,
  totalLikes: 0,
  totalComments: 0,
  todayPV: 0,
  todayUV: 0,
  totalPV: 0,
  totalUV: 0,
});

/**
 * 加载文章列表
 */
async function loadArticles() {
  loading.value = true;
  try {
    const response = await articleApi.getList({
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      status: 1,
    });
    articles.value = response.list;
    total.value = response.total;
    totalPages.value = Math.ceil(response.total / pageSize.value);
  } catch (error) {
    console.error("加载文章列表失败:", error);
  } finally {
    loading.value = false;
  }
}

/**
 * 加载热门文章
 */
async function loadHotArticles() {
  try {
    hotArticles.value = await articleApi.getHotArticles(5);
  } catch (error) {
    console.error("加载热门文章失败:", error);
  }
}

/**
 * 加载网站统计数据
 */
async function loadSiteStats() {
  try {
    const stats = await getSiteStats();

    // 逐个赋值，确保响应式更新
    siteStats.value.todayPV = stats.todayPV || 0;
    siteStats.value.todayUV = stats.todayUV || 0;
    siteStats.value.totalPV = stats.totalPV || 0;
    siteStats.value.totalUV = stats.totalUV || 0;
    siteStats.value.totalArticles = stats.totalArticles || 0;
    siteStats.value.totalReads = stats.totalReads || 0;
    siteStats.value.totalLikes = stats.totalLikes || 0;
    siteStats.value.totalComments = stats.totalComments || 0;
  } catch (error) {
    console.error("加载统计数据失败:", error);
  }
}

/**
 * 切换页码
 */
function changePage(page: number) {
  currentPage.value = page;
  loadArticles();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

/**
 * 格式化日期
 */
function formatDate(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60));
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60));
      return `${minutes} mins ago`;
    }
    return `${hours} hours ago`;
  }
  if (days === 1) return "Yesterday";
  if (days < 7) return `${days} days ago`;
  return date.toLocaleDateString("zh-CN");
}

// 页面加载时获取数据
onMounted(() => {
  // 记录页面访问
  recordPageView().catch((err) => console.error("记录 PV 失败:", err));
  // 加载数据
  loadArticles();
  loadHotArticles();
  loadSiteStats();
});
</script>
