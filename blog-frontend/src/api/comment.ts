import { request } from './request'
import type { CommentVO, PageResult } from '@/types/api'

/**
 * 评论相关 API
 */
export const commentApi = {
  /**
   * 发表评论/回复
   */
  create(data: {
    articleId: number
    content: string
    parentId?: number
    replyToUserId?: number
  }): Promise<number> {
    return request.post('/api/v1/comments', data)
  },

  /**
   * 获取文章评论列表
   */
  getListByArticle(
    articleId: number,
    pageNo: number = 1,
    pageSize: number = 20
  ): Promise<PageResult<CommentVO>> {
    return request.get(`/api/v1/articles/${articleId}/comments`, {
      params: { pageNo, pageSize }
    })
  }
}
