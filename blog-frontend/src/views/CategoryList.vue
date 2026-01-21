<template>
  <div class="min-h-screen bg-gray-50">
    <AppNavbar />
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">文章分类</h1>
        <p class="mt-2 text-gray-600">浏览所有文章分类</p>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
      </div>

      <!-- 分类列表 -->
      <div v-else-if="categories.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="category in categories"
          :key="category.id"
          class="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-300 p-6 cursor-pointer"
          @click="goToCategoryArticles(category.id)"
        >
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{{ category.name }}</h2>
          <p class="text-gray-600 text-sm mb-4 line-clamp-2">
            {{ category.description || '暂无描述' }}
          </p>
          <div class="flex items-center justify-between text-sm text-gray-500">
            <span>{{ category.articleCount }} 篇文章</span>
            <span class="text-blue-600 hover:text-blue-800">查看更多 →</span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="text-center py-12">
        <p class="text-gray-500">暂无分类</p>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="mt-4 p-4 bg-red-50 border border-red-200 rounded-md">
        <p class="text-red-600">{{ error }}</p>
        <button
          @click="fetchCategories"
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { categoryApi } from '@/api/category'
import type { Category } from '@/types/api'
import AppNavbar from '@/components/AppNavbar.vue'
import AppFooter from '@/components/AppFooter.vue'

const router = useRouter()
const categories = ref<Category[]>([])
const loading = ref(false)
const error = ref('')

/**
 * 获取分类列表
 */
async function fetchCategories() {
  loading.value = true
  error.value = ''
  try {
    categories.value = await categoryApi.getAll()
  } catch (err: any) {
    error.value = err.message || '获取分类列表失败'
    console.error('获取分类列表失败:', err)
  } finally {
    loading.value = false
  }
}

/**
 * 跳转到分类文章列表页
 */
function goToCategoryArticles(categoryId: number) {
  router.push({ name: 'CategoryArticles', params: { id: categoryId } })
}

onMounted(() => {
  fetchCategories()
})
</script>
