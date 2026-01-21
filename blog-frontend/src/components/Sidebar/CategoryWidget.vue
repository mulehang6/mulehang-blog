<template>
  <div class="bg-white rounded-lg shadow-md p-6">
    <h3 class="text-lg font-semibold text-gray-900 mb-4">文章分类</h3>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center py-4">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
    </div>
    
    <!-- 分类列表 -->
    <ul v-else-if="categories.length > 0" class="space-y-2">
      <li
        v-for="category in categories"
        :key="category.id"
        class="flex items-center justify-between p-2 rounded hover:bg-gray-50 cursor-pointer transition-colors"
        @click="goToCategory(category.id)"
      >
        <span class="text-gray-700">{{ category.name }}</span>
        <span class="text-sm text-gray-500">{{ category.articleCount }}</span>
      </li>
    </ul>
    
    <!-- 查看全部 -->
    <div v-if="categories.length > 0" class="mt-4 text-center">
      <router-link
        to="/categories"
        class="text-sm text-blue-600 hover:text-blue-800"
      >
        查看全部分类 →
      </router-link>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="!loading" class="text-center py-4 text-gray-500 text-sm">
      暂无分类
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { categoryApi } from '@/api/category'
import type { Category } from '@/types/api'

const router = useRouter()
const categories = ref<Category[]>([])
const loading = ref(false)

/**
 * 获取分类列表
 */
async function fetchCategories() {
  loading.value = true
  try {
    const allCategories = await categoryApi.getAll()
    // 只显示前 8 个分类
    categories.value = allCategories.slice(0, 8)
  } catch (err) {
    console.error('获取分类列表失败:', err)
  } finally {
    loading.value = false
  }
}

/**
 * 跳转到分类文章列表页
 */
function goToCategory(categoryId: number) {
  router.push({ name: 'CategoryArticles', params: { id: categoryId } })
}

onMounted(() => {
  fetchCategories()
})
</script>
