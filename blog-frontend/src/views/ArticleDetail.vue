<template>
  <div class="min-h-screen bg-background">
    <!-- 顶部导航栏 -->
    <AppNavbar />

    <main class="container mx-auto px-4 py-8">
      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center items-center py-20">
        <div
          class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"
        ></div>
      </div>

      <!-- 文章内容 -->
      <div v-else-if="article" class="max-w-4xl mx-auto">
        <!-- 面包屑导航 -->
        <nav
          v-if="article.category"
          class="mb-6 flex items-center text-sm text-muted-foreground"
        >
          <router-link to="/" class="hover:text-foreground">首页</router-link>
          <span class="mx-2">/</span>
          <router-link
            :to="`/categories/${article.category.id}`"
            class="hover:text-foreground"
          >
            {{ article.category.name }}
          </router-link>
          <span class="mx-2">/</span>
          <span class="text-foreground">{{ article.title }}</span>
        </nav>
        <nav
          v-else
          class="mb-6 flex items-center text-sm text-muted-foreground"
        >
          <router-link to="/" class="hover:text-foreground">首页</router-link>
          <span class="mx-2">/</span>
          <span class="text-foreground">{{ article.title }}</span>
        </nav>

        <!-- 文章头部 -->
        <Card class="mb-8">
          <CardHeader>
            <CardTitle class="text-4xl mb-4">{{ article.title }}</CardTitle>
            <div
              class="flex flex-wrap items-center gap-4 text-sm text-muted-foreground"
            >
              <div v-if="article.author" class="flex items-center gap-2">
                <Avatar class="h-8 w-8">
                  <AvatarImage
                    :src="article.author.avatar || ''"
                    :alt="article.author.username || ''"
                  />
                  <AvatarFallback>{{
                    article.author.username.charAt(0)
                  }}</AvatarFallback>
                </Avatar>
                <span>{{ article.author.username }}</span>
              </div>
              <span
                >发布于
                {{
                  formatDate(article.publishTime || article.createTime)
                }}</span
              >
              <span v-if="article.updateTime !== article.createTime">
                更新于 {{ formatDate(article.updateTime) }}
              </span>
            </div>
            <div
              class="flex items-center gap-4 mt-4 text-sm text-muted-foreground"
            >
              <span class="flex items-center gap-1">
                👁️ {{ article.readCount || 0 }} 阅读
              </span>
              <span class="flex items-center gap-1">
                ❤️ {{ article.likeCount || 0 }} 点赞
              </span>
              <span class="flex items-center gap-1">
                💬 {{ article.commentCount || 0 }} 评论
              </span>
            </div>
          </CardHeader>
        </Card>

        <!-- 封面图 -->
        <div v-if="article.coverUrl" class="mb-8">
          <img
            :src="article.coverUrl"
            :alt="article.title"
            class="w-full rounded-lg shadow-lg"
          />
        </div>

        <!-- 文章正文 -->
        <Card class="mb-8">
          <CardContent class="pt-6">
            <MarkdownRenderer
              v-if="article.contentMd"
              :content="article.contentMd"
            />
            <div v-else v-html="article.contentHtml"></div>
          </CardContent>
        </Card>

        <!-- 文章元信息 -->
        <Card class="mb-8">
          <CardContent class="pt-6">
            <div class="flex flex-wrap gap-4 justify-center">
              <div v-if="article.category">
                <span class="text-sm text-muted-foreground mr-2">分类：</span>
                <Badge
                  variant="secondary"
                  class="cursor-pointer"
                  @click="router.push(`/categories/${article.category.id}`)"
                >
                  {{ article.category.name }}
                </Badge>
              </div>
              <div v-else>
                <span class="text-sm text-muted-foreground mr-2">分类：</span>
                <Badge variant="secondary">未分类</Badge>
              </div>
              <div v-if="article.tags && article.tags.length > 0">
                <span class="text-sm text-muted-foreground mr-2">标签：</span>
                <Badge
                  v-for="tag in article.tags"
                  :key="tag.id"
                  variant="outline"
                  class="mr-2 cursor-pointer"
                  @click="router.push(`/tags/${tag.id}`)"
                >
                  # {{ tag.name }}
                </Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- 操作按钮 -->
        <Card class="mb-8">
          <CardContent class="pt-6">
            <div class="flex items-center justify-center gap-4">
              <Button
                :variant="isLiked ? 'default' : 'outline'"
                size="lg"
                @click="handleLike"
                :disabled="!userStore.isLoggedIn || liking"
              >
                <span class="mr-2">❤️</span>
                {{ liking ? "点赞中..." : isLiked ? "已点赞" : "点赞" }} ({{
                  article.likeCount
                }})
              </Button>
              <Button variant="outline" size="lg" @click="handleShare">
                <span class="mr-2">🔗</span>
                分享
              </Button>
            </div>
            <p
              v-if="!userStore.isLoggedIn"
              class="text-center text-sm text-muted-foreground mt-4"
            >
              <router-link to="/login" class="text-primary hover:underline">
                登录
              </router-link>
              后可点赞文章
            </p>
          </CardContent>
        </Card>

        <!-- 相关文章推荐（可选） -->
        <Card v-if="relatedArticles.length > 0" class="mb-8">
          <CardHeader>
            <CardTitle class="text-xl">相关推荐</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="space-y-4">
              <div
                v-for="relatedArticle in relatedArticles"
                :key="relatedArticle.id"
                class="flex items-start gap-3 cursor-pointer hover:bg-muted/50 p-3 rounded-md transition-colors"
                @click="router.push(`/articles/${relatedArticle.slug}`)"
              >
                <div class="flex-1">
                  <h4 class="font-medium hover:text-primary transition-colors">
                    {{ relatedArticle.title }}
                  </h4>
                  <p class="text-sm text-muted-foreground mt-1 line-clamp-2">
                    {{ relatedArticle.summary }}
                  </p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- 评论区域 -->
        <CommentList v-if="article" :article-id="article.id" />
      </div>

      <!-- 错误提示 -->
      <div v-else class="max-w-4xl mx-auto">
        <Card>
          <CardContent class="py-12 text-center">
            <p class="text-muted-foreground text-lg mb-4">
              {{ error || "文章不存在" }}
            </p>
            <Button @click="router.push('/')">返回首页</Button>
          </CardContent>
        </Card>
      </div>
    </main>

    <!-- 页脚 -->
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue-sonner";
import { articleApi } from "@/api/article";
import { useUserStore } from "@/stores/user";
import type { ArticleDetail, ArticleListItem } from "@/types/api";
import AppNavbar from "@/components/AppNavbar.vue";
import AppFooter from "@/components/AppFooter.vue";
import MarkdownRenderer from "@/components/Markdown/MarkdownRenderer.vue";
import CommentList from "@/components/Comment/CommentList.vue";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const article = ref<ArticleDetail | null>(null);
const relatedArticles = ref<ArticleListItem[]>([]);
const loading = ref(false);
const error = ref("");
const isLiked = ref(false);
const liking = ref(false); // 点赞操作中

/**
 * 获取文章详情
 */
async function fetchArticle() {
  loading.value = true;
  error.value = "";
  const slug = route.params.slug as string;

  try {
    article.value = await articleApi.getBySlug(slug);
    // 获取相关文章（同分类）
    if (article.value.category?.id) {
      const result = await articleApi.getList({
        categoryId: article.value.category.id,
        pageSize: 5,
      });
      relatedArticles.value = result.list
        .filter((a) => a.id !== article.value!.id)
        .slice(0, 3);
    }
    // 查询用户是否已点赞
    if (userStore.isLoggedIn && userStore.userInfo) {
      isLiked.value = await articleApi.getLikeStatus(
        article.value.id,
        userStore.userInfo.id,
      );
    }
  } catch (err: any) {
    error.value = err.message || "获取文章失败";
    console.error("获取文章详情失败:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 处理点赞/取消点赞
 */
async function handleLike() {
  if (!userStore.isLoggedIn) {
    router.push("/login");
    return;
  }

  if (!article.value || !userStore.userInfo || liking.value) return;

  liking.value = true;
  try {
    if (isLiked.value) {
      // 已点赞，执行取消点赞
      const success = await articleApi.unlike(
        article.value.id,
        userStore.userInfo.id,
      );
      if (success) {
        article.value.likeCount--;
        isLiked.value = false;
        toast.success("已取消点赞");
      } else {
        toast.error("取消点赞失败");
      }
    } else {
      // 未点赞，执行点赞
      const success = await articleApi.like(
        article.value.id,
        userStore.userInfo.id,
      );
      if (success) {
        article.value.likeCount++;
        isLiked.value = true;
        toast.success("点赞成功");
      } else {
        toast.info("您已经点赞过了");
        isLiked.value = true;
      }
    }
  } catch (err: any) {
    console.error("点赞操作失败:", err);
    toast.error("操作失败", {
      description: err.message || "请稍后重试",
    });
  } finally {
    liking.value = false;
  }
}

/**
 * 分享文章
 */
function handleShare() {
  const url = window.location.href;
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard
      .writeText(url)
      .then(() => {
        toast.success("链接已复制到剪贴板");
      })
      .catch((err: any) => {
        toast.error("复制失败", {
          description: err?.message || "请稍后重试",
        });
      });
    return;
  }
  try {
    const textarea = document.createElement("textarea");
    textarea.value = url;
    textarea.style.position = "fixed";
    textarea.style.left = "-9999px";
    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();
    const success = document.execCommand("copy");
    document.body.removeChild(textarea);
    if (success) {
      toast.success("链接已复制到剪贴板");
    } else {
      toast.error("复制失败", {
        description: "请手动复制地址栏链接",
      });
    }
  } catch (err: any) {
    toast.error("复制失败", {
      description: err?.message || "请手动复制地址栏链接",
    });
  }
}

/**
 * 格式化日期
 */
function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

onMounted(() => {
  fetchArticle();
});
</script>
