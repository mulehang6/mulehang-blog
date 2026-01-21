<template>
  <div class="comment-item">
    <div class="flex gap-3">
      <!-- 用户头像 -->
      <Avatar class="h-10 w-10 flex-shrink-0">
        <AvatarImage :src="comment.avatar || undefined" :alt="comment.nickname" />
        <AvatarFallback>{{ comment.nickname.charAt(0) }}</AvatarFallback>
      </Avatar>

      <div class="flex-1 min-w-0">
        <!-- 评论头部 -->
        <div class="flex items-center gap-2 mb-2">
          <span class="font-medium">{{ comment.nickname }}</span>
          <span v-if="comment.replyToUserId" class="text-sm text-muted-foreground">
            回复 @{{ comment.nickname }}
          </span>
          <span class="text-sm text-muted-foreground">
            {{ formatDate(comment.createdAt) }}
          </span>
        </div>

        <!-- 评论内容 -->
        <div class="mb-2 text-foreground whitespace-pre-wrap break-words">
          {{ comment.content }}
        </div>

        <!-- 操作按钮 -->
        <div class="flex items-center gap-4 text-sm">
          <button
            class="text-muted-foreground hover:text-foreground transition-colors"
            @click="showReplyForm = !showReplyForm"
          >
            💬 回复
          </button>
          <button
            class="text-muted-foreground hover:text-foreground transition-colors flex items-center gap-1"
            @click="handleLike"
          >
            ❤️ {{ comment.likeCount }}
          </button>
        </div>

        <!-- 回复表单 -->
        <div v-if="showReplyForm" class="mt-4">
          <CommentForm
            :article-id="comment.articleId"
            :parent-comment="comment"
            @success="handleReplySuccess"
            @cancel="showReplyForm = false"
          />
        </div>

        <!-- 子评论 -->
        <div v-if="comment.children && comment.children.length > 0" class="mt-4 space-y-4">
          <CommentItem
            v-for="child in comment.children"
            :key="child.id"
            :comment="child"
            @reply="$emit('reply', $event)"
            @refresh="$emit('refresh')"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { CommentVO } from '@/types/api'
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar'
import CommentForm from './CommentForm.vue'

/**
 * 组件 Props
 */
const props = defineProps<{
  comment: CommentVO
}>()

/**
 * 组件 Emits
 */
const emit = defineEmits<{
  (e: 'reply', comment: CommentVO): void
  (e: 'refresh'): void
}>()

const showReplyForm = ref(false)

/**
 * 处理点赞
 */
function handleLike() {
  // TODO: 实现评论点赞功能
  console.log('点赞评论:', props.comment.id)
}

/**
 * 处理回复成功
 */
function handleReplySuccess() {
  showReplyForm.value = false
  emit('refresh')
}

/**
 * 格式化日期
 */
function formatDate(dateStr: string): string {
  if (!dateStr) return '刚刚'
  
  const date = new Date(dateStr)
  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    return '日期解析错误'
  }
  
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 如果时间差为负数，说明是未来时间
  if (diff < 0) {
    return date.toLocaleString('zh-CN')
  }
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      if (minutes === 0) {
        return '刚刚'
      }
      return `${minutes}分钟前`
    }
    return `${hours}小时前`
  }
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}
</script>
