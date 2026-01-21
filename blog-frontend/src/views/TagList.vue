<template>
  <div class="min-h-screen bg-gray-50">
    <AppNavbar />
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">文章标签</h1>
        <p class="mt-2 text-gray-600">浏览所有文章标签</p>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <div
          class="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"
        ></div>
      </div>

      <!-- 标签云 -->
      <div
        v-else-if="tags.length > 0"
        class="bg-white rounded-lg shadow-md p-8"
      >
        <div class="flex flex-wrap gap-3">
          <div
            v-for="tag in tags"
            :key="tag.id"
            class="inline-flex items-center px-4 py-2 rounded-full cursor-pointer transition-all duration-300"
            :style="{
              fontSize: getTagSize(getArticleCount(tag)) + 'px',
              backgroundColor: getTagColor(getArticleCount(tag)),
              color: 'white',
            }"
            @click="goToTagArticles(tag.id)"
          >
            <span class="mr-2">#{{ tag.name }}</span>
            <span class="text-xs opacity-80"
              >· {{ getArticleCount(tag) }} 篇</span
            >
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="text-center py-12">
        <p class="text-gray-500">暂无标签</p>
      </div>

      <!-- 错误提示 -->
      <div
        v-if="error"
        class="mt-4 p-4 bg-red-50 border border-red-200 rounded-md"
      >
        <p class="text-red-600">{{ error }}</p>
        <button
          @click="fetchTags"
          class="mt-2 text-sm text-red-800 hover:text-red-900 underline"
        >
          重新加载
        </button>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { tagApi } from "@/api/tag";
import type { Tag } from "@/types/api";
import AppNavbar from "@/components/AppNavbar.vue";
import AppFooter from "@/components/AppFooter.vue";

const router = useRouter();
const tags = ref<Tag[]>([]);
const loading = ref(false);
const error = ref("");

/**
 * 获取标签列表
 */
async function fetchTags() {
  loading.value = true;
  error.value = "";
  try {
    tags.value = await tagApi.getAll();
  } catch (err: any) {
    error.value = err.message || "获取标签列表失败";
    console.error("获取标签列表失败:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 根据文章数量计算标签大小
 */
function getTagSize(articleCount: number): number {
  const minSize = 14;
  const maxSize = 24;
  const maxCount = Math.max(...tags.value.map(getArticleCount), 1);
  return minSize + (articleCount / maxCount) * (maxSize - minSize);
}

/**
 * 根据文章数量计算标签颜色
 */
function getTagColor(articleCount: number): string {
  const maxCount = Math.max(...tags.value.map(getArticleCount), 1);
  const ratio = articleCount / maxCount;

  if (ratio > 0.7) return "#3b82f6"; // blue-500
  if (ratio > 0.4) return "#10b981"; // green-500
  return "#8b5cf6"; // purple-500
}

function getArticleCount(tag: Tag): number {
  return tag.articleCount ?? 0;
}

/**
 * 跳转到标签文章列表页
 */
function goToTagArticles(tagId: number) {
  router.push({ name: "TagArticles", params: { id: tagId } });
}

onMounted(() => {
  fetchTags();
});
</script>
