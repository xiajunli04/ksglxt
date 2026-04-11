import { test, expect } from '@playwright/test'

test.describe('Landing Page', () => {
  test('should display landing page at root URL', async ({ page }) => {
    await page.goto('/')
    // Hero title should be visible
    await expect(page.locator('.hero-title')).toHaveText('广软课室申请系统')
    // Hero subtitle
    await expect(page.locator('.hero-subtitle')).toHaveText('让每一间课室都能被高效利用')
  })

  test('should display feature cards', async ({ page }) => {
    await page.goto('/')
    const features = page.locator('.feature-card')
    await expect(features).toHaveCount(4)
    await expect(page.locator('.feature-name').nth(0)).toHaveText('在线预约')
    await expect(page.locator('.feature-name').nth(1)).toHaveText('智能审批')
    await expect(page.locator('.feature-name').nth(2)).toHaveText('课室管理')
    await expect(page.locator('.feature-name').nth(3)).toHaveText('公告通知')
  })

  test('should display usage steps', async ({ page }) => {
    await page.goto('/')
    const steps = page.locator('.step-item')
    await expect(steps).toHaveCount(4)
    await expect(page.locator('.step-name').nth(0)).toHaveText('选择课室')
    await expect(page.locator('.step-name').nth(1)).toHaveText('提交申请')
    await expect(page.locator('.step-name').nth(2)).toHaveText('等待审批')
    await expect(page.locator('.step-name').nth(3)).toHaveText('预约完成')
  })

  test('should have Enter System button that redirects to login when not authenticated', async ({ page }) => {
    await page.goto('/')
    // Clear any existing tokens
    await page.evaluate(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    })
    await page.goto('/')

    const enterBtn = page.locator('.hero-actions .el-button--primary')
    await expect(enterBtn).toBeVisible()
    await enterBtn.click()
    await expect(page).toHaveURL(/\/login/)
  })

  test('should show header with logo and enter button', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('.logo-text')).toHaveText('广软课室申请系统')
    const headerBtn = page.locator('.landing-header .el-button--primary')
    await expect(headerBtn).toBeVisible()
  })

  test('should display footer', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('.landing-footer')).toContainText('广州软件学院')
  })
})

test.describe('Navigation', () => {
  test('should redirect /admin routes to login when not authenticated', async ({ page }) => {
    await page.evaluate(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    })
    await page.goto('/admin/dashboard')
    await expect(page).toHaveURL(/\/login/)
  })

  test('should show dashboard after login', async ({ page }) => {
    // Navigate to login page
    await page.goto('/login')

    // Fill login form
    await page.fill('input[placeholder*="用户名"]', 'admin')
    await page.fill('input[placeholder*="密码"]', 'admin123')

    // Submit
    await page.click('button:has-text("登录")')

    // Should redirect to dashboard
    await expect(page).toHaveURL(/\/admin\/dashboard/, { timeout: 10000 })

    // Dashboard should have stat cards
    const statCards = page.locator('.stat-card')
    await expect(statCards.first()).toBeVisible({ timeout: 5000 })
  })
})

test.describe('Dashboard', () => {
  // Login helper
  async function loginAs(page: any, username: string, password: string) {
    await page.goto('/login')
    await page.fill('input[placeholder*="用户名"]', username)
    await page.fill('input[placeholder*="密码"]', password)
    await page.click('button:has-text("登录")')
    await expect(page).toHaveURL(/\/admin\/dashboard/, { timeout: 10000 })
  }

  test('admin should see 4 stat cards with correct labels', async ({ page }) => {
    await loginAs(page, 'admin', 'admin123')

    const labels = page.locator('.stat-label')
    await expect(labels.nth(0)).toHaveText('课室总数')
    await expect(labels.nth(1)).toHaveText('待审批')
    await expect(labels.nth(2)).toHaveText('今日预约')
    await expect(labels.nth(3)).toHaveText('本月预约')
  })

  test('admin should see pending approvals and hot classrooms panels', async ({ page }) => {
    await loginAs(page, 'admin', 'admin123')

    const cardTitles = page.locator('.card-title')
    await expect(cardTitles.filter({ hasText: '待审批申请' })).toBeVisible()
    await expect(cardTitles.filter({ hasText: '热门课室' })).toBeVisible()
    await expect(cardTitles.filter({ hasText: '最新公告' })).toBeVisible()
  })

  test('admin should see sidebar with admin menus', async ({ page }) => {
    await loginAs(page, 'admin', 'admin123')

    const asideMenu = page.locator('.aside-menu')
    await expect(asideMenu).toBeVisible()
    await expect(page.locator('.aside-menu').getByText('首页')).toBeVisible()
    await expect(page.locator('.aside-menu').getByText('课室管理')).toBeVisible()
    await expect(page.locator('.aside-menu').getByText('审批管理')).toBeVisible()
    await expect(page.locator('.aside-menu').getByText('公告管理')).toBeVisible()
    await expect(page.locator('.aside-menu').getByText('用户管理')).toBeVisible()
  })

  test('should display announcements list on dashboard', async ({ page }) => {
    await loginAs(page, 'admin', 'admin123')

    // Announcements card should exist
    const announcementsCard = page.locator('.content-card').filter({ hasText: '最新公告' })
    await expect(announcementsCard).toBeVisible()

    // Should have "查看全部" link
    await expect(announcementsCard.locator('text=查看全部')).toBeVisible()
  })

  test('notification bell should be visible in header', async ({ page }) => {
    await loginAs(page, 'admin', 'admin123')

    const bellBtn = page.locator('.notification-badge .el-button')
    await expect(bellBtn).toBeVisible()
  })

  test('clicking notification bell should navigate to notifications', async ({ page }) => {
    await loginAs(page, 'admin', 'admin123')

    await page.locator('.notification-badge .el-button').click()
    await expect(page).toHaveURL(/\/admin\/notification/)
  })
})

test.describe('Landing to Login Flow', () => {
  test('should navigate from landing to login and back to dashboard', async ({ page }) => {
    // Start at landing
    await page.goto('/')
    await expect(page.locator('.hero-title')).toBeVisible()

    // Clear auth state
    await page.evaluate(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    })

    // Click enter system
    await page.locator('.hero-actions .el-button--primary').click()
    await expect(page).toHaveURL(/\/login/)

    // Login
    await page.fill('input[placeholder*="用户名"]', 'admin')
    await page.fill('input[placeholder*="密码"]', 'admin123')
    await page.click('button:has-text("登录")')

    // Should go to dashboard
    await expect(page).toHaveURL(/\/admin\/dashboard/, { timeout: 10000 })
    const statCards = page.locator('.stat-card')
    await expect(statCards.first()).toBeVisible({ timeout: 5000 })
  })
})
