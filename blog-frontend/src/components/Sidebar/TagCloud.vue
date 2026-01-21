<template>
  <div class="bg-white rounded-lg shadow-md p-6">
    <h3 class="text-lg font-semibold text-gray-900 mb-4">热门标签</h3>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center py-4">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
    </div>
    
    <!-- 标签云 -->
    <div v-else-if="tags.length > 0" class="flex flex-wrap gap-2">
      <span
        v-for="tag in tags"
        :key="tag.id"
        class="inline-flex items-center px-3 py-1 rounded-full cursor-pointer transition-all duration-300 text-sm"
        :style="{
          backgroundColor: getTagColor(tag.articleCount),
          color: 'white'
        }"
        @click="goToTag(tag.id)"
      >
        #{{ tag.name }}
        <span class="ml-1 text-xs opacity-80">({{ tag.articleCount }})</span>
      </span>
    </div>
    
    <!-- 查看全部 -->
    <div v-if="tags.length > 0" class="mt-4 text-center">
      <router-link
        to="/tags"
        class="text-sm text-blue-600 hover:text-blue-800"
      >
        查看全部标签 →
      </router-link>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="!loading" class="text-center py-4 text-gray-500 text-sm">
      暂无标签
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { tagApi } from '@/api/tag'
import type { Tag } from '@/types/api'

const router = useRouter()
const tags = ref<Tag[]>([])
const loading = ref(false)

/**
 * 获取标签列表
 */
async function fetchTags() {
  loading.value = true
  try {
    const allTags = await tagApi.getAll()
    // 按文章数量排序，只显示前 15 个
    tags.value = allTags
      .sort((a, b) => b.articleCount - a.articleCount)
      .slice(0, 15)
  } catch (err) {
    console.error('获取标签列表失败:', err)
  } finally {
    loading.value = false
  }
}

/**
 * 根据文章数量计算标签颜色
 */
function getTagColor(articleCount: number): string {
  const maxCount = Math.max(...tags.value.map(t => t.articleCount), 1)
  const ratio = articleCount / maxCount
  
  if (ratio > 0.7) return '#3b82f6' // blue-500
  if (ratio > 0.4) return '#10b981' // green-500
  return '#8b5cf6' // purple-500
}

/**
 * 跳转到标签文章列表页
 */
function goToTag(tagId: number) {
  router.push({ name: 'TagArticles', params: { id: tagId } })
}

onMounted(() => {
  fetchTags()
})
</script>
