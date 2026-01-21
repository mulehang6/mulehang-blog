<template>
  <div class="min-h-screen bg-background text-foreground">
    <!-- 顶部导航栏 -->
    <AppNavbar />

    <!-- 主要内容区域 -->
    <main class="container mx-auto px-4 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 文章列表 -->
        <div class="lg:col-span-2 space-y-6">
          <div class="flex items-center justify-between mb-6">
            <h1 class="text-3xl font-bold">最新文章</h1>
            <div class="flex items-center gap-2">
              <Button variant="outline" size="sm" @click="loadArticles">
                <span class="text-sm">刷新</span>
              </Button>
            </div>
          </div>

          <!-- 加载中状态 -->
          <div v-if="loading" class="space-y-6">
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
          <div v-else-if="articles.length > 0" class="space-y-6">
            <Card 
              v-for="article in articles" 
              :key="article.id" 
              class="hover:shadow-lg transition-shadow cursor-pointer group"
              @click="router.push(`/articles/${article.slug}`)"
            >
              <CardHeader>
                <CardTitle class="text-xl group-hover:text-primary transition-colors">
                  {{ article.title }}
                </CardTitle>
                <CardDescription class="line-clamp-2 text-base mt-2">
                  {{ article.summary }}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div class="flex items-center justify-between text-sm text-muted-foreground">
                  <div class="flex items-center gap-4">
                    <Badge v-if="article.category" variant="secondary">{{ article.category.name }}</Badge>
                    <Badge v-else variant="secondary">未分类</Badge>
                    <span v-if="article.author">{{ article.author.username }}</span>
                    <span>{{ formatDate(article.createTime) }}</span>
                  </div>
                  <div class="flex items-center gap-4">
                    <span>👁️ {{ article.readCount || 0 }}</span>
                    <span>❤️ {{ article.likeCount || 0 }}</span>
                    <span>💬 {{ article.commentCount || 0 }}</span>
                  </div>
                </div>
                <div v-if="article.tags && article.tags.length > 0" class="flex items-center gap-2 mt-3">
                  <Badge v-for="tag in article.tags" :key="tag.id" variant="outline" class="text-xs">
                    # {{ tag.name }}
                  </Badge>
                </div>
              </CardContent>
            </Card>
          </div>

          <!-- 空状态 -->
          <Card v-else>
            <CardContent class="py-12 text-center">
              <p class="text-muted-foreground text-lg">暂无文章</p>
            </CardContent>
          </Card>

          <!-- 分页 -->
          <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 mt-8">
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
        </div>

        <!-- 侧边栏 -->
        <div class="lg:col-span-1 space-y-6">
          <!-- 热门文章 -->
          <Card>
            <CardHeader>
              <CardTitle class="text-lg">🔥 热门文章</CardTitle>
            </CardHeader>
            <CardContent>
              <div v-if="hotArticles.length > 0" class="space-y-4">
                <div 
                  v-for="(article, index) in hotArticles" 
                  :key="article.id"
                  class="flex items-start gap-3 cursor-pointer hover:bg-muted/50 p-2 rounded-md transition-colors"
                  @click="router.push(`/articles/${article.slug}`)"
                >
                  <span class="flex-shrink-0 w-6 h-6 rounded-full bg-primary/10 text-primary flex items-center justify-center text-xs font-bold">
                    {{ index + 1 }}
                  </span>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium line-clamp-2 hover:text-primary transition-colors">
                      {{ article.title }}
                    </p>
                    <p class="text-xs text-muted-foreground mt-1">
                      👁️ {{ article.readCount || 0 }} · ❤️ {{ article.likeCount || 0 }}
                    </p>
                  </div>
                </div>
              </div>
              <p v-else class="text-sm text-muted-foreground text-center py-4">
                暂无热门文章
              </p>
            </CardContent>
          </Card>

          <!-- 文章分类 -->
          <CategoryWidget />

          <!-- 热门标签 -->
          <TagCloud />

          <!-- 统计信息 -->
          <Card>
            <CardHeader>
              <CardTitle class="text-lg">📊 博客统计</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="space-y-3">
                <div class="flex items-center justify-between">
                  <span class="text-sm text-muted-foreground">文章总数</span>
                  <span class="text-sm font-semibold">{{ siteStats.totalArticles || 0 }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-sm text-muted-foreground">总阅读量</span>
                  <span class="text-sm font-semibold">{{ siteStats.totalReads || 0 }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-sm text-muted-foreground">总点赞数</span>
                  <span class="text-sm font-semibold">{{ siteStats.totalLikes || 0 }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-sm text-muted-foreground">今日访问</span>
                  <span class="text-sm font-semibold">{{ siteStats.todayPV || 0 }} / {{ siteStats.todayUV || 0 }}</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { recordPageView, getSiteStats } from '@/api/stats'
import type { ArticleListItem, SiteStats } from '@/types/api'
import AppNavbar from '@/components/AppNavbar.vue'
import AppFooter from '@/components/AppFooter.vue'
import CategoryWidget from '@/components/Sidebar/CategoryWidget.vue'
import TagCloud from '@/components/Sidebar/TagCloud.vue'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'

const router = useRouter()

// 文章列表状态
const articles = ref<ArticleListItem[]>([])
const hotArticles = ref<ArticleListItem[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)

// 网站统计数据
const siteStats = ref<SiteStats>({
  totalArticles: 0,
  totalReads: 0,
  totalLikes: 0,
  totalComments: 0,
  todayPV: 0,
  todayUV: 0,
  totalPV: 0,
  totalUV: 0
})

/**
 * 加载文章列表
 */
async function loadArticles() {
  loading.value = true
  try {
    const response = await articleApi.getList({
      pageNo: currentPage.value,
      pageSize: pageSize.value
    })
    articles.value = response.list
    total.value = response.total
    totalPages.value = response.totalPages
  } catch (error) {
    console.error('加载文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载热门文章
 */
async function loadHotArticles() {
  try {
    hotArticles.value = await articleApi.getHotArticles(5)
  } catch (error) {
    console.error('加载热门文章失败:', error)
  }
}

/**
 * 加载网站统计数据
 */
async function loadSiteStats() {
  try {
    const stats = await getSiteStats()
    
    // 逐个赋值，确保响应式更新
    siteStats.value.todayPV = stats.todayPV || 0
    siteStats.value.todayUV = stats.todayUV || 0
    siteStats.value.totalPV = stats.totalPV || 0
    siteStats.value.totalUV = stats.totalUV || 0
    siteStats.value.totalArticles = stats.totalArticles || 0
    siteStats.value.totalReads = stats.totalReads || 0
    siteStats.value.totalLikes = stats.totalLikes || 0
    siteStats.value.totalComments = stats.totalComments || 0
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

/**
 * 切换页码
 */
function changePage(page: number) {
  currentPage.value = page
  loadArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 格式化日期
 */
function formatDate(dateString: string): string {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return `${minutes}分钟前`
    }
    return `${hours}小时前`
  }
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return date.toLocaleDateString('zh-CN')
}

// 页面加载时获取数据
onMounted(() => {
  // 记录页面访问
  recordPageView().catch(err => console.error('记录 PV 失败:', err))
  // 加载数据
  loadArticles()
  loadHotArticles()
  loadSiteStats()
})
</script>
