import { expect, test } from '@playwright/test'

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

async function disableNativeValidation(page: Parameters<typeof test>[0]['page']) {
  await page.locator('form').evaluate(form => {
    form.setAttribute('novalidate', 'true')
  })
}

test.describe('认证页面', () => {
  test('登录页应展示必填校验', async ({ page }) => {
    await page.route('**/api/v1/users/current', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 40100, msg: 'unauthorized', data: null })
      })
    })

    await page.goto('/login')
    await disableNativeValidation(page)
    await page.getByTestId('login-submit').click()
    await expect(page.getByTestId('login-error')).toContainText('请输入用户名')

    await page.getByTestId('login-username').fill('abc')
    await page.getByTestId('login-submit').click()
    await expect(page.getByTestId('login-error')).toContainText('请输入密码')
  })

  test('注册页应校验邮箱和密码一致性', async ({ page }) => {
    await page.route('**/api/v1/users/current', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 40100, msg: 'unauthorized', data: null })
      })
    })

    await page.goto('/register')
    await disableNativeValidation(page)
    await page.getByTestId('register-username').fill('playwright_user')
    await page.getByTestId('register-nickname').fill('PW 用户')
    await page.getByTestId('register-email').fill('invalid-email')
    await page.getByTestId('register-password').fill('123456')
    await page.getByTestId('register-confirm-password').fill('654321')
    await page.getByTestId('register-agree').check()
    await page.getByTestId('register-submit').click()

    await expect(page.getByTestId('register-error')).toContainText('请输入有效的邮箱地址')

    await page.getByTestId('register-email').fill('pw@example.com')
    await page.getByTestId('register-submit').click()
    await expect(page.getByTestId('register-error')).toContainText('两次输入的密码不一致')
  })

  test('登录成功后应跳转首页', async ({ page }) => {
    await page.route('**/api/v1/users/current', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 40100, msg: 'unauthorized', data: null })
      })
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
})
