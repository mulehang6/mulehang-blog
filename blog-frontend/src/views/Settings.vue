<template>
  <div class="min-h-screen bg-background">
    <!-- 导航栏 -->
    <AppNavbar />

    <main class="container mx-auto px-4 py-8 max-w-4xl">
      <h1 class="text-3xl font-bold mb-8">账户设置</h1>

      <!-- 基本信息 -->
      <Card class="mb-6">
        <CardHeader>
          <CardTitle>基本信息</CardTitle>
        </CardHeader>
        <CardContent>
          <form @submit.prevent="handleSubmit" class="space-y-6">
            <!-- 头像 -->
            <div class="flex items-center gap-4">
              <Avatar class="h-20 w-20">
                <AvatarImage :src="form.avatar" :alt="form.nickname" />
                <AvatarFallback class="text-xl">{{ form.nickname?.charAt(0) || 'U' }}</AvatarFallback>
              </Avatar>
              <div class="flex-1">
                <label class="block text-sm font-medium mb-2">头像 URL</label>
                <input
                  v-model="form.avatar"
                  type="url"
                  placeholder="https://example.com/avatar.jpg"
                  class="w-full px-3 py-2 border border-input rounded-md bg-background"
                />
              </div>
            </div>

            <!-- 用户名（只读） -->
            <div>
              <label class="block text-sm font-medium mb-2">用户名</label>
              <input
                :value="userStore.userInfo?.username"
                type="text"
                disabled
                class="w-full px-3 py-2 border border-input rounded-md bg-muted text-muted-foreground cursor-not-allowed"
              />
              <p class="text-xs text-muted-foreground mt-1">用户名不可修改</p>
            </div>

            <!-- 昵称 -->
            <div>
              <label class="block text-sm font-medium mb-2">昵称 *</label>
              <input
                v-model="form.nickname"
                type="text"
                required
                maxlength="50"
                placeholder="请输入昵称"
                class="w-full px-3 py-2 border border-input rounded-md bg-background"
              />
            </div>

            <!-- 邮箱 -->
            <div>
              <label class="block text-sm font-medium mb-2">邮箱</label>
              <input
                v-model="form.email"
                type="email"
                placeholder="your@email.com"
                class="w-full px-3 py-2 border border-input rounded-md bg-background"
              />
            </div>

            <!-- 个人简介 -->
            <div>
              <label class="block text-sm font-medium mb-2">个人简介</label>
              <textarea
                v-model="form.profile"
                rows="4"
                maxlength="200"
                placeholder="介绍一下自己吧..."
                class="w-full px-3 py-2 border border-input rounded-md bg-background resize-none"
              ></textarea>
              <p class="text-xs text-muted-foreground mt-1">{{ form.profile?.length || 0 }} / 200</p>
            </div>

            <!-- 提交按钮 -->
            <div class="flex gap-4">
              <Button type="submit" :disabled="saving">
                {{ saving ? '保存中...' : '保存修改' }}
              </Button>
              <Button type="button" variant="outline" @click="resetForm">
                取消
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <!-- 修改密码 -->
      <Card class="mb-6">
        <CardHeader>
          <CardTitle>修改密码</CardTitle>
        </CardHeader>
        <CardContent>
          <form @submit.prevent="handleChangePassword" class="space-y-6">
            <!-- 当前密码 -->
            <div>
              <label class="block text-sm font-medium mb-2">当前密码 *</label>
              <input
                v-model="passwordForm.currentPassword"
                type="password"
                required
                placeholder="请输入当前密码"
                class="w-full px-3 py-2 border border-input rounded-md bg-background"
              />
            </div>

            <!-- 新密码 -->
            <div>
              <label class="block text-sm font-medium mb-2">新密码 *</label>
              <input
                v-model="passwordForm.newPassword"
                type="password"
                required
                minlength="6"
                placeholder="请输入新密码（至少6位）"
                class="w-full px-3 py-2 border border-input rounded-md bg-background"
              />
            </div>

            <!-- 确认新密码 -->
            <div>
              <label class="block text-sm font-medium mb-2">确认新密码 *</label>
              <input
                v-model="passwordForm.confirmPassword"
                type="password"
                required
                minlength="6"
                placeholder="请再次输入新密码"
                class="w-full px-3 py-2 border border-input rounded-md bg-background"
              />
            </div>

            <!-- 提交按钮 -->
            <Button type="submit" variant="default" :disabled="changingPassword">
              {{ changingPassword ? '修改中...' : '修改密码' }}
            </Button>
          </form>
        </CardContent>
      </Card>

      <!-- 危险操作 -->
      <Card class="border-destructive">
        <CardHeader>
          <CardTitle class="text-destructive">危险操作</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 border border-destructive/50 rounded-md">
              <div>
                <h3 class="font-medium text-destructive">删除账户</h3>
                <p class="text-sm text-muted-foreground mt-1">
                  删除账户后，您的所有数据将被永久删除且无法恢复
                </p>
              </div>
              <Button variant="destructive" @click="handleDeleteAccount">
                删除账户
              </Button>
            </div>
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
import { toast } from 'vue-sonner'
import { useUserStore } from '@/stores/user'
import AppNavbar from '@/components/AppNavbar.vue'
import AppFooter from '@/components/AppFooter.vue'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar'

const router = useRouter()
const userStore = useUserStore()

const saving = ref(false)
const changingPassword = ref(false)

// 基本信息表单
const form = ref({
  nickname: '',
  email: '',
  avatar: '',
  profile: ''
})

// 密码修改表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

/**
 * 初始化表单
 */
function initForm() {
  console.log('initForm - userStore.userInfo:', userStore.userInfo)
  if (userStore.userInfo) {
    form.value = {
      nickname: userStore.userInfo.nickname || '',
      email: userStore.userInfo.email || '',
      avatar: userStore.userInfo.avatar || '',
      profile: userStore.userInfo.profile || ''
    }
    console.log('initForm - form.value:', form.value)
  }
}

/**
 * 重置表单
 */
function resetForm() {
  initForm()
}

/**
 * 提交基本信息修改
 */
async function handleSubmit() {
  if (!form.value.nickname.trim()) {
    toast.error('昵称不能为空')
    return
  }

  saving.value = true
  try {
    // TODO: 调用更新用户信息的 API
    console.log('更新用户信息:', form.value)
    
    // 模拟 API 调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新本地用户信息
    if (userStore.userInfo) {
      userStore.setUserInfo({
        ...userStore.userInfo,
        ...form.value
      })
    }
    
    toast.success('保存成功')
  } catch (err: any) {
    console.error('保存失败:', err)
    toast.error('保存失败', {
      description: err.message || '请稍后重试'
    })
  } finally {
    saving.value = false
  }
}

/**
 * 修改密码
 */
async function handleChangePassword() {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.error('两次输入的新密码不一致')
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    toast.error('新密码至少需見6位')
    return
  }

  changingPassword.value = true
  try {
    // TODO: 调用修改密码的 API
    console.log('修改密码')
    
    // 模拟 API 调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    toast.success('密码修改成功', {
      description: '请重新登录'
    })
    
    // 清空表单
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    
    // 退出登录
    await userStore.logout()
    router.replace('/login')
  } catch (err: any) {
    console.error('修改密码失败:', err)
    toast.error('修改密码失败', {
      description: err.message || '请稍后重试'
    })
  } finally {
    changingPassword.value = false
  }
}

/**
 * 删除账户
 */
function handleDeleteAccount() {
  toast.error('此功能暂未开放')
}

onMounted(() => {
  // 如果未登录，跳转到登录页
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  initForm()
})
</script>
