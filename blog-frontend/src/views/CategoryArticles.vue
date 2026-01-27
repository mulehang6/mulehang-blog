<template>
  <div class="min-h-screen bg-transparent text-foreground">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 面包屑导航 -->
      <nav class="mb-6 flex items-center text-sm text-muted-foreground">
        <router-link to="/" class="hover:text-foreground">首页</router-link>
        <span class="mx-2">/</span>
        <router-link to="/categories" class="hover:text-foreground"
          >分类</router-link
        >
        <span class="mx-2">/</span>
        <span class="text-foreground">{{ category?.name || "加载中..." }}</span>
      </nav>

      <!-- 分类信息 -->
      <div
        v-if="category"
        class="bg-card/70 backdrop-blur-md border border-border/70 rounded-xl shadow-sm p-6 mb-8"
      >
        <h1 class="text-3xl font-bold text-foreground mb-2">
          {{ category.name }}
        </h1>
        <p class="text-muted-foreground">
          {{ category.description || "暂无描述" }}
        </p>
        <p class="mt-2 text-sm text-muted-foreground">
          共 {{ category.articleCount }} 篇文章
        </p>
      </div>

      <!-- 文章列表 -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <div
          class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"
        ></div>
      </div>

      <div v-else-if="articles.length > 0" class="space-y-6">
        <div
          v-for="article in articles"
          :key="article.id"
          class="bg-card/70 backdrop-blur-md border border-border/70 rounded-xl shadow-sm hover:shadow-md transition-all duration-300 p-6 cursor-pointer"
          @click="goToArticle(article.slug)"
        >
          <h2
            class="text-xl font-semibold text-foreground mb-2 hover:text-primary"
          >
            {{ article.title }}
          </h2>
          <p class="text-muted-foreground text-sm mb-4 line-clamp-2">
            {{ article.summary }}
          </p>
          <div
            class="flex items-center justify-between text-sm text-muted-foreground"
          >
            <div class="flex items-center space-x-4">
              <span>{{
                article.author?.nickname || article.author?.username || "匿名"
              }}</span>
              <span>{{
                formatDate(article.publishTime || article.createTime)
              }}</span>
            </div>
            <div class="flex items-center space-x-4">
              <span>👁️ {{ article.readCount }}</span>
              <span>❤️ {{ article.likeCount }}</span>
              <span>💬 {{ article.commentCount }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-12">
        <p class="text-muted-foreground">该分类下暂无文章</p>
      </div>

      <!-- 分页 -->
      <div v-if="pagination.totalPages > 1" class="mt-8 flex justify-center">
        <div class="flex space-x-2">
          <button
            v-for="page in pagination.totalPages"
            :key="page"
            @click="goToPage(page)"
            class="px-4 py-2 rounded-md transition-colors"
            :class="
              page === pagination.pageNo
                ? 'bg-primary text-primary-foreground'
                : 'bg-card/70 text-foreground hover:bg-muted'
            "
          >
            {{ page }}
          </button>
        </div>
      </div>

      <!-- 错误提示 -->
      <div
        v-if="error"
        class="mt-4 p-4 bg-destructive/10 border border-destructive/30 rounded-md"
      >
        <p class="text-destructive">{{ error }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { categoryApi } from "@/api/category";
import { articleApi } from "@/api/article";
import type { Category, ArticleListItem } from "@/types/api";

const route = useRoute();
const router = useRouter();

const category = ref<Category | null>(null);
const articles = ref<ArticleListItem[]>([]);
const loading = ref(false);
const error = ref("");
const pagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  totalPages: 0,
});

/**
 * 获取分类信息
 */
async function fetchCategory() {
  const categoryId = Number(route.params.id);
  try {
    category.value = await categoryApi.getById(categoryId);
  } catch (err: any) {
    error.value = err.message || "获取分类信息失败";
    console.error("获取分类信息失败:", err);
  }
}

/**
 * 获取文章列表
 */
async function fetchArticles() {
  loading.value = true;
  error.value = "";
  const categoryId = Number(route.params.id);

  try {
    const result = await articleApi.getList({
      categoryId,
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
      status: 1,
    });

    articles.value = result.list;
    pagination.value.total = result.total;
    pagination.value.totalPages = Math.ceil(result.total / pagination.value.pageSize);
  } catch (err: any) {
    error.value = err.message || "获取文章列表失败";
    console.error("获取文章列表失败:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 跳转到文章详情页
 */
function goToArticle(slug: string) {
  router.push({ name: "ArticleDetail", params: { slug } });
}

/**
 * 跳转到指定页码
 */
function goToPage(page: number) {
  pagination.value.pageNo = page;
  fetchArticles();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

/**
 * 格式化日期
 */
function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
}

// 监听路由变化
watch(
  () => route.params.id,
  () => {
    if (route.name === "CategoryArticles") {
      pagination.value.pageNo = 1;
      fetchCategory();
      fetchArticles();
    }
  },
);

onMounted(() => {
  fetchCategory();
  fetchArticles();
});
</script>
