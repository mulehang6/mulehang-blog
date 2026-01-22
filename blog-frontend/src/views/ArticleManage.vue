<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold tracking-tight">
        {{ localeStore.t.articleManage }}
      </h1>
      <p class="text-muted-foreground mt-1">
        {{ localeStore.t.articleManageSubtitle }}
      </p>
    </div>

    <div class="flex flex-wrap items-center gap-2">
      <Button
        v-for="option in statusOptions"
        :key="option.key"
        size="sm"
        variant="outline"
        class="transition-all"
        :class="
          activeStatus === option.key
            ? 'border-primary/60 text-primary bg-primary/10'
            : 'border-border/60'
        "
        @click="setStatusFilter(option.key)"
      >
        {{ option.label }}
      </Button>
    </div>

    <div v-if="loading" class="flex justify-center items-center py-12">
      <div
        class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"
      ></div>
    </div>

    <div v-else-if="articles.length > 0" class="space-y-4">
      <Card
        v-for="article in articles"
        :key="article.id"
        class="group hover:border-primary/50 transition-all duration-300 cursor-pointer overflow-hidden border-border/60 hover-lift-sheen"
        @click="handleOpenArticle(article)"
      >
        <CardHeader class="pb-3">
          <div class="flex items-start justify-between gap-4">
            <div class="space-y-1">
              <div class="flex items-center gap-2">
                <CardTitle
                  class="text-xl font-bold group-hover:text-primary transition-colors"
                >
                  {{ article.title }}
                </CardTitle>
                <Badge
                  v-if="article.status !== undefined"
                  variant="secondary"
                  class="text-xs"
                >
                  {{ getStatusLabel(article.status) }}
                </Badge>
              </div>
              <div
                class="flex items-center gap-2 text-xs text-muted-foreground"
              >
                <span v-if="article.author" class="flex items-center gap-1">
                  <span class="w-1 h-1 rounded-full bg-muted-foreground"></span>
                  {{ article.author.username }}
                </span>
                <span class="flex items-center gap-1">
                  <span class="w-1 h-1 rounded-full bg-muted-foreground"></span>
                  {{ formatDate(article.createTime) }}
                </span>
              </div>
            </div>
            <div class="flex items-center gap-2 text-xs">
              <Button
                v-if="isDraft(article.status)"
                size="sm"
                variant="ghost"
                class="text-emerald-500"
                @click.stop="handlePublish(article.id)"
              >
                {{ localeStore.t.publishArticle }}
              </Button>
              <Button
                size="sm"
                variant="ghost"
                @click.stop="handleEdit(article.id)"
              >
                {{ localeStore.t.editArticle }}
              </Button>
              <Button
                size="sm"
                variant="ghost"
                class="text-destructive"
                @click.stop="handleDelete(article.id)"
              >
                {{ localeStore.t.deleteArticle }}
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <p class="text-muted-foreground text-sm line-clamp-2 leading-relaxed">
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
            <div class="flex items-center gap-4 text-xs text-muted-foreground">
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

    <Card v-else class="border-dashed">
      <CardContent class="py-12 text-center">
        <div class="text-4xl mb-4">🗂️</div>
        <p class="text-muted-foreground text-lg">
          {{ localeStore.t.emptyArticles }}
        </p>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { articleApi } from "@/api/article";
import type { ArticleListItem } from "@/types/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useUserStore } from "@/stores/user";
import { useLocaleStore } from "@/stores/locale";

const router = useRouter();
const userStore = useUserStore();
const localeStore = useLocaleStore();

const articles = ref<ArticleListItem[]>([]);
const loading = ref(false);
type StatusFilter = "all" | "published" | "draft";
const activeStatus = ref<StatusFilter>("all");

const statusOptions = computed(
  () =>
    [
      { key: "all", label: localeStore.t.articleManageAll },
      { key: "published", label: localeStore.t.articleManagePublished },
      { key: "draft", label: localeStore.t.articleManageDrafts },
    ] as const,
);

/**
 * 切换状态过滤条件
 */
function setStatusFilter(filter: StatusFilter) {
  if (activeStatus.value === filter) return;
  activeStatus.value = filter;
  fetchArticles();
}

/**
 * 获取文章列表
 */
async function fetchArticles() {
  loading.value = true;
  try {
    const params: Record<string, number | undefined> = {
      pageNo: 1,
      pageSize: 50,
    };
    if (!userStore.isAdmin) {
      params.authorId = userStore.userInfo?.id;
    }
    if (activeStatus.value !== "all") {
      params.status = activeStatus.value === "draft" ? 0 : 1;
    }
    const res = await articleApi.getList(params);
    articles.value = res.list;
  } catch (err) {
    console.error("获取文章列表失败:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 跳转到编辑页
 */
function handleEdit(id: number) {
  router.push({ name: "ArticleEdit", params: { id } });
}

/**
 * 打开文章详情
 */
function handleOpenArticle(article: ArticleListItem) {
  if (article.status === 1 && article.slug) {
    router.push({ name: "ArticleDetail", params: { slug: article.slug } });
    return;
  }
  router.push({ name: "ArticleEdit", params: { id: article.id } });
}

/**
 * 删除文章
 */
async function handleDelete(id: number) {
  const ok = window.confirm(localeStore.t.confirmDeleteArticle);
  if (!ok) return;
  try {
    await articleApi.delete(id);
    await fetchArticles();
  } catch (err) {
    console.error("删除文章失败:", err);
  }
}

/**
 * 发布草稿
 */
async function handlePublish(id: number) {
  const ok = window.confirm(localeStore.t.confirmPublishArticle);
  if (!ok) return;
  try {
    await articleApi.publish(id);
    await fetchArticles();
  } catch (err) {
    console.error("发布文章失败:", err);
  }
}

/**
 * 判断是否为草稿
 */
function isDraft(status?: number) {
  return status === 0;
}

/**
 * 获取状态文案
 */
function getStatusLabel(status?: number) {
  if (status === 0) return localeStore.t.articleStatusDraft;
  if (status === 1) return localeStore.t.articleStatusPublished;
  return "";
}

/**
 * 格式化日期
 */
function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString();
}

onMounted(() => {
  fetchArticles();
});
</script>
