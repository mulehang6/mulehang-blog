import { expect, test } from '@playwright/test'

const currentUserResponse = {
  code: 0,
  msg: 'ok',
  data: {
    id: 1,
    username: 'testuser',
    nickname: '测试用户',
    email: 'test@example.com',
    roles: ['USER']
  }
}

const articleListResponse = {
  code: 0,
  msg: 'ok',
  data: {
    list: [
      {
        id: 101,
        title: 'Playwright 自动化测试实践',
        slug: 'playwright-auto-test',
        summary: '用于课程设计自动化测试演示的示例文章。',
        status: 0,
        author: {
          id: 1,
          username: 'testuser',
          nickname: '测试用户'
        },
        category: {
          id: 1,
          name: '测试',
          slug: 'testing'
        },
        tags: [
          { id: 1, name: 'Playwright', slug: 'playwright' },
          { id: 2, name: '自动化测试', slug: 'automation' }
        ],
        readCount: 12,
        likeCount: 3,
        commentCount: 1,
        createTime: '2026-06-06T10:00:00',
        updateTime: '2026-06-06T10:00:00'
      }
    ],
    total: 1,
    pageNo: 1,
    pageSize: 50
  }
}

const mockLoginResponse = {
  code: 0,
  msg: 'ok',
  data: {
    token: 'mock-token',
    tokenType: 'Bearer',
    expiresIn: 86400,
    userInfo: {
      id: 1,
      username: 'testuser',
      nickname: '测试用户',
      email: 'test@example.com',
      roles: ['USER']
    }
  }
}

test.describe('文章管理页', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/api/v1/users/current', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(currentUserResponse)
      })
    })

    await page.route('**/api/v1/articles**', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(articleListResponse)
        })
        return
      }

      if (route.request().method() === 'DELETE') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ code: 0, msg: 'ok', data: null })
        })
        return
      }

      await route.fallback()
    })

    await page.route('**/api/v1/auth/login', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockLoginResponse)
      })
    })

    await page.goto('/login')
    await page.getByTestId('login-username').fill('testuser')
    await page.getByTestId('login-password').fill('password123')
    await page.getByTestId('login-submit').click()
    await expect(page).toHaveURL(/\/$/)
  })

  test('应展示文章列表并允许打开删除确认框', async ({ page }) => {
    await page.goto('/articles/manage')

    await expect(page.getByTestId('article-manage-title')).toBeVisible()
    await expect(page.getByTestId('article-card-101')).toContainText('Playwright 自动化测试实践')

    await page.getByTestId('article-delete-101').click()
    await expect(page.getByTestId('article-delete-dialog-title')).toContainText('确认删除')
    await expect(page.getByTestId('article-delete-confirm')).toBeVisible()
  })

  test('应支持切换草稿筛选', async ({ page }) => {
    await page.goto('/articles/manage')

    await page.getByTestId('article-filter-draft').click()
    await expect(page.getByTestId('article-card-101')).toBeVisible()
  })
})
