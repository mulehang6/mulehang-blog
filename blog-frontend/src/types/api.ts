/**
 * API 统一响应结构
 */
export interface ApiResponse<T = any> {
  code: number
  msg?: string
  message?: string
  data: T
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[]
  total: number
  pageNo: number
  pageSize: number
  totalPages: number
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  avatar?: string
  profile?: string
  roles: string[]
  lastLoginTime?: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

/**
 * 文章列表项
 */
export interface ArticleListItem {
  id: number
  title: string
  slug: string
  summary: string
  coverUrl?: string
  status?: number
  author: {
    id: number
    username: string
    nickname: string
    avatar?: string
  } | null
  category: {
    id: number
    name: string
    slug: string
  } | null
  tags: Array<{
    id: number
    name: string
    slug: string
  }>
  readCount: number
  likeCount: number
  commentCount: number
  createTime: string
  updateTime: string
  publishTime?: string
}

/**
 * 文章详情
 */
export interface ArticleDetail {
  id: number
  title: string
  slug: string
  summary: string
  coverUrl?: string
  contentHtml: string
  contentMd?: string
  author: {
    id: number
    username: string
    nickname: string
    avatar?: string
  } | null
  category: {
    id: number
    name: string
    slug: string
  } | null
  column?: {
    id: number
    name: string
    slug: string
  }
  tags: Array<{
    id: number
    name: string
    slug: string
  }>
  wordCount?: number
  readCount: number
  likeCount: number
  commentCount: number
  status: number
  sourceType: number
  allowComment?: number
  isPinned?: number
  publishTime?: string
  createTime: string
  updateTime: string
}

/**
 * 分类
 */
export interface Category {
  id: number
  name: string
  slug: string
  description?: string
  articleCount: number
  creatorId?: number
}

/**
 * 标签
 */
export interface Tag {
  id: number
  name: string
  slug: string
  articleCount: number
  creatorId?: number
}

/**
 * 评论
 */
export interface CommentVO {
  id: number
  articleId: number
  userId: number
  username: string
  nickname: string
  avatar: string | null
  content: string
  parentId: number | null
  replyToUserId: number | null
  likeCount: number
  status: number
  createdAt: string
  children?: CommentVO[]
}

/**
 * 网站统计数据
 */
export interface SiteStats {
  todayPV: number
  todayUV: number
  totalPV: number
  totalUV: number
  totalArticles: number
  totalReads: number
  totalLikes: number
  totalComments: number
}

/**
 * 文件上传响应
 */
export interface UploadFile {
  url: string
  platform?: string
  path?: string
  filename?: string
  originalFilename?: string
  ext?: string
  size?: number
}
