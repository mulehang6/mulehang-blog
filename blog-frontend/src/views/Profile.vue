<template>
  <div class="min-h-screen bg-background">
    <!-- 导航栏 -->
    <AppNavbar />

    <main class="container mx-auto px-4 py-8 max-w-6xl">
      <!-- 用户信息卡片 -->
      <Card class="mb-8">
        <CardContent class="pt-6">
          <div class="flex flex-col md:flex-row items-center md:items-start gap-6">
            <!-- 头像 -->
            <Avatar class="h-24 w-24">
              <AvatarImage :src="userStore.userInfo?.avatar" :alt="userStore.userInfo?.nickname" />
              <AvatarFallback class="text-2xl">{{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}</AvatarFallback>
            </Avatar>

            <!-- 用户详细信息 -->
            <div class="flex-1 text-center md:text-left">
              <h1 class="text-3xl font-bold mb-2">{{ userStore.userInfo?.nickname }}</h1>
              <p class="text-muted-foreground mb-2">@{{ userStore.userInfo?.username }}</p>
              <p class="text-muted-foreground mb-4">{{ userStore.userInfo?.email }}</p>
              
              <!-- 个人简介 -->
              <p v-if="userStore.userInfo?.profile" class="text-foreground mb-4">
                {{ userStore.userInfo.profile }}
              </p>

              <!-- 角色标签 -->
              <div class="flex flex-wrap gap-2 justify-center md:justify-start">
                <Badge 
                  v-for="role in userStore.userInfo?.roles" 
                  :key="role"
                  :variant="role === 'ADMIN' ? 'default' : 'secondary'"
                >
                  {{ getRoleLabel(role) }}
                </Badge>
              </div>
            </div>

            <!-- 编辑按钮 -->
            <Button @click="router.push('/settings')" variant="outline">
              编辑资料
            </Button>
          </div>
        </CardContent>
      </Card>

      <!-- 统计信息 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <Card>
          <CardContent class="pt-6">
            <div class="text-center">
              <p class="text-3xl font-bold text-primary mb-2">{{ stats.articleCount }}</p>
              <p class="text-sm text-muted-foreground">发表文章</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent class="pt-6">
            <div class="text-center">
              <p class="text-3xl font-bold text-primary mb-2">{{ stats.commentCount }}</p>
              <p class="text-sm text-muted-foreground">发表评论</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent class="pt-6">
            <div class="text-center">
              <p class="text-3xl font-bold text-primary mb-2">{{ stats.likeCount }}</p>
              <p class="text-sm text-muted-foreground">获得点赞</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- 最近文章 -->
      <Card>
        <CardHeader>
          <CardTitle>最近文章</CardTitle>
        </CardHeader>
        <CardContent>
          <div v-if="loading" class="flex justify-center py-8">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
          </div>
          <div v-else-if="articles.length > 0" class="space-y-4">
            <div
              v-for="article in articles"
              :key="article.id"
              class="flex items-start gap-4 p-4 rounded-lg hover:bg-muted/50 cursor-pointer transition-colors"
              @click="router.push(`/articles/${article.slug}`)"
            >
              <img
                v-if="article.coverUrl"
                :src="article.coverUrl"
                :alt="article.title"
                class="w-20 h-20 rounded object-cover"
              />
              <div class="flex-1 min-w-0">
                <h3 class="font-semibold mb-1 hover:text-primary transition-colors">
                  {{ article.title }}
                </h3>
                <p class="text-sm text-muted-foreground line-clamp-2 mb-2">
                  {{ article.summary }}
                </p>
                <div class="flex items-center gap-4 text-xs text-muted-foreground">
                  <span>👁️ {{ article.readCount }}</span>
                  <span>❤️ {{ article.likeCount }}</span>
                  <span>💬 {{ article.commentCount }}</span>
                  <span>{{ formatDate(article.publishTime || article.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-muted-foreground">
            暂无文章
          </div>
        </CardContent>
      </Card>
    </main>

    <!-- 页脚 -->
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { articleApi } from '@/api/article'
import type { ArticleListItem } from '@/types/api'
import AppNavbar from '@/components/AppNavbar.vue'
import AppFooter from '@/components/AppFooter.vue'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar'

const router = useRouter()
const userStore = useUserStore()

const articles = ref<ArticleListItem[]>([])
const loading = ref(false)
const stats = ref({
  articleCount: 0,
  commentCount: 0,
  likeCount: 0
})

/**
 * 获取用户文章列表
 */
async function fetchUserArticles() {
  if (!userStore.userInfo) return
  
  loading.value = true
  try {
    const result = await articleApi.getList({
      authorId: userStore.userInfo.id,
      pageNo: 1,
      pageSize: 10
    })
    articles.value = result.list
    stats.value.articleCount = result.total
    
    // 计算总点赞数
    stats.value.likeCount = result.list.reduce((sum, article) => sum + article.likeCount, 0)
  } catch (err) {
    console.error('获取用户文章失败:', err)
  } finally {
    loading.value = false
  }
}

/**
 * 获取角色标签
 */
function getRoleLabel(role: string): string {
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'USER': '用户',
    'GUEST': '访客'
  }
  return roleMap[role] || role
}

/**
 * 格式化日期
 */
function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  // 如果未登录，跳转到登录页
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  fetchUserArticles()
})
</script>
