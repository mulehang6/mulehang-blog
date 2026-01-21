<template>
  <div class="min-h-screen bg-background">
    <AppNavbar />

    <main class="container mx-auto px-4 py-8">
      <!-- 搜索头部 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold mb-4">搜索结果</h1>
        <div class="flex items-center gap-4">
          <div class="flex-1 relative">
            <Input
              v-model="keyword"
              placeholder="搜索文章..."
              class="pr-10"
              @keyup.enter="handleSearch"
            />
            <Button
              variant="ghost"
              size="icon"
              class="absolute right-0 top-0"
              @click="handleSearch"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
            </Button>
          </div>
        </div>

        <!-- 搜索历史 -->
        <div v-if="searchHistory.length > 0" class="mt-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm text-muted-foreground">搜索历史</span>
            <Button variant="ghost" size="sm" @click="clearHistory">
              清除
            </Button>
          </div>
          <div class="flex flex-wrap gap-2">
            <Badge
              v-for="(item, index) in searchHistory"
              :key="index"
              variant="secondary"
              class="cursor-pointer"
              @click="handleHistoryClick(item)"
            >
              {{ item }}
            </Badge>
          </div>
        </div>
      </div>

      <!-- 搜索结果信息 -->
      <div v-if="currentKeyword" class="mb-4">
        <p class="text-muted-foreground">
          搜索 "<span class="text-foreground font-medium">{{ currentKeyword }}</span>"
          找到 <span class="text-foreground font-medium">{{ total }}</span> 篇文章
        </p>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center items-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>

      <!-- 搜索结果列表 -->
      <div v-else-if="articles.length > 0" class="space-y-6">
        <Card
          v-for="article in articles"
          :key="article.id"
          class="hover:shadow-lg transition-shadow cursor-pointer"
          @click="router.push(`/articles/${article.slug}`)"
        >
          <CardHeader>
            <CardTitle class="text-xl hover:text-primary transition-colors">
              {{ article.title }}
            </CardTitle>
            <CardDescription class="line-clamp-2 text-base mt-2">
              {{ article.summary }}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="flex items-center justify-between text-sm text-muted-foreground">
              <div class="flex items-center gap-4">
                <Badge variant="secondary">{{ article.category?.name || '未分类' }}</Badge>
                <span>{{ article.author?.nickname || article.author?.username || '匿名' }}</span>
                <span>{{ formatDate(article.publishTime || article.createTime) }}</span>
              </div>
              <div class="flex items-center gap-4">
                <span>👁️ {{ article.readCount }}</span>
                <span>❤️ {{ article.likeCount }}</span>
                <span>💬 {{ article.commentCount }}</span>
              </div>
            </div>
            <div v-if="article.tags.length > 0" class="flex items-center gap-2 mt-3">
              <Badge
                v-for="tag in article.tags"
                :key="tag.id"
                variant="outline"
                class="text-xs"
              >
                # {{ tag.name }}
              </Badge>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- 空状态 -->
      <Card v-else-if="currentKeyword">
        <CardContent class="py-12 text-center">
          <p class="text-muted-foreground text-lg mb-4">
            没有找到相关文章
          </p>
          <p class="text-sm text-muted-foreground mb-6">
            试试其他关键词或浏览
            <router-link to="/" class="text-primary hover:underline">
              所有文章
            </router-link>
          </p>
        </CardContent>
      </Card>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="mt-8 flex items-center justify-center gap-2">
        <Button
          variant="outline"
          size="sm"
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
        >
          上一页
        </Button>
        <span class="text-sm text-muted-foreground">
          第 {{ currentPage }} / {{ totalPages }} 页
        </span>
        <Button
          variant="outline"
          size="sm"
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
        >
          下一页
        </Button>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import type { ArticleListItem } from '@/types/api'
import AppNavbar from '@/components/AppNavbar.vue'
import AppFooter from '@/components/AppFooter.vue'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const currentKeyword = ref('')
const articles = ref<ArticleListItem[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)

/**
 * 从 localStorage 读取搜索历史
 */
const searchHistory = computed(() => {
  const history = localStorage.getItem('search_history')
  return history ? JSON.parse(history) : []
})

/**
 * 执行搜索
 */
async function performSearch(searchKeyword: string) {
  if (!searchKeyword.trim()) return

  loading.value = true
  currentKeyword.value = searchKeyword

  try {
    const result = await articleApi.search({
      keyword: searchKeyword,
      pageNo: currentPage.value,
      pageSize: pageSize.value
    })

    articles.value = result.list
    total.value = result.total
    totalPages.value = result.totalPages
  } catch (err) {
    console.error('搜索失败:', err)
  } finally {
    loading.value = false
  }
}

/**
 * 处理搜索
 */
function handleSearch() {
  if (!keyword.value.trim()) return

  // 保存搜索历史
  saveSearchHistory(keyword.value.trim())

  // 更新 URL
  router.push({
    name: 'Search',
    query: { keyword: keyword.value.trim() }
  })

  // 执行搜索
  currentPage.value = 1
  performSearch(keyword.value.trim())
}

/**
 * 保存搜索历史
 */
function saveSearchHistory(keyword: string) {
  const history = searchHistory.value
  const newHistory = [keyword, ...history.filter((item: string) => item !== keyword)].slice(0, 10)
  localStorage.setItem('search_history', JSON.stringify(newHistory))
}

/**
 * 清除搜索历史
 */
function clearHistory() {
  localStorage.removeItem('search_history')
}

/**
 * 点击搜索历史
 */
function handleHistoryClick(item: string) {
  keyword.value = item
  handleSearch()
}

/**
 * 切换页码
 */
function changePage(page: number) {
  currentPage.value = page
  performSearch(currentKeyword.value)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 格式化日期
 */
function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  const queryKeyword = route.query.keyword as string
  if (queryKeyword) {
    keyword.value = queryKeyword
    performSearch(queryKeyword)
  }
})
</script>
