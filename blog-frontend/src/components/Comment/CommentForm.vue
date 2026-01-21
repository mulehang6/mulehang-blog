<template>
  <div class="comment-form">
    <Card>
      <CardHeader>
        <CardTitle class="text-lg">
          {{ parentComment ? `回复 @${parentComment.nickname}` : '发表评论' }}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <Textarea
          v-model="content"
          :placeholder="parentComment ? '写下你的回复...' : '写下你的评论...'"
          rows="4"
          class="mb-4"
          :maxlength="500"
        />
        <div class="flex items-center justify-between">
          <span class="text-sm text-muted-foreground">
            {{ content.length }}/500
          </span>
          <div class="flex gap-2">
            <Button v-if="parentComment" variant="outline" @click="handleCancel">
              取消
            </Button>
            <Button
              @click="handleSubmit"
              :disabled="!content.trim() || submitting"
            >
              {{ submitting ? '发送中...' : '发送' }}
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { commentApi } from '@/api/comment'
import type { CommentVO } from '@/types/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'

/**
 * 组件 Props
 */
const props = defineProps<{
  articleId: number
  parentComment?: CommentVO
}>()

/**
 * 组件 Emits
 */
const emit = defineEmits<{
  (e: 'success'): void
  (e: 'cancel'): void
}>()

const content = ref('')
const submitting = ref(false)

/**
 * 提交评论
 */
async function handleSubmit() {
  if (!content.value.trim()) return

  submitting.value = true
  try {
    await commentApi.create({
      articleId: props.articleId,
      content: content.value.trim(),
      parentId: props.parentComment?.id,
      replyToUserId: props.parentComment?.userId
    })

    content.value = ''
    emit('success')
  } catch (err: any) {
    console.error('发表评论失败:', err)
    alert(err.message || '发表评论失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

/**
 * 取消回复
 */
function handleCancel() {
  content.value = ''
  emit('cancel')
}
</script>
