<template>
  <header
    class="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-backdrop-filter:bg-background/60"
  >
    <div class="container mx-auto px-4 py-4">
      <div class="flex h-14 items-center justify-between">
        <!-- Logo 和导航菜单 -->
        <div class="flex items-center gap-8">
          <router-link to="/" class="flex items-center space-x-2">
            <span
              class="text-2xl font-bold bg-linear-to-r from-primary to-primary/60 bg-clip-text text-transparent"
            >
              Mulehang Blog
            </span>
          </router-link>

          <!-- 桌面端导航 -->
          <nav class="hidden md:flex items-center gap-6 text-sm font-medium">
            <router-link
              to="/"
              class="transition-colors hover:text-primary"
              :class="{ 'text-primary': $route.path === '/' }"
            >
              首页
            </router-link>
            <router-link
              to="/categories"
              class="transition-colors hover:text-primary text-muted-foreground"
              :class="{ 'text-primary': $route.path.startsWith('/categories') }"
            >
              分类
            </router-link>
            <router-link
              to="/tags"
              class="transition-colors hover:text-primary text-muted-foreground"
              :class="{ 'text-primary': $route.path.startsWith('/tags') }"
            >
              标签
            </router-link>
            <router-link
              to="/about"
              class="transition-colors hover:text-primary text-muted-foreground"
              :class="{ 'text-primary': $route.path === '/about' }"
            >
              关于
            </router-link>
          </nav>
        </div>

        <!-- 搜索和用户操作 -->
        <div class="flex items-center gap-4">
          <!-- 搜索按钮 -->
          <Button
            variant="ghost"
            size="icon"
            class="hidden md:flex"
            @click="showSearchDialog = true"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
          </Button>

          <!-- 主题切换按钮 -->
          <Button variant="ghost" size="icon" @click="toggleTheme">
            <svg
              v-if="!isDark"
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
              />
            </svg>
            <svg
              v-else
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
              />
            </svg>
          </Button>

          <!-- 未登录状态 -->
          <template v-if="!userStore.isLoggedIn">
            <Button @click="router.push('/login')" variant="ghost" size="sm">
              登录
            </Button>
            <Button
              @click="router.push('/register')"
              variant="default"
              size="sm"
              class="hidden md:flex"
            >
              注册
            </Button>
          </template>

          <!-- 已登录状态 -->
          <template v-else>
            <Button @click="router.push('/write')" variant="default" size="sm">
              写文章
            </Button>

            <!-- 用户菜单 -->
            <div class="relative" ref="userMenuRef">
              <Button
                variant="ghost"
                size="icon"
                class="rounded-full"
                @click.stop="showUserMenu = !showUserMenu"
              >
                <Avatar class="h-8 w-8">
                  <AvatarImage
                    :src="userStore.userInfo?.avatar || ''"
                    :alt="userStore.userInfo?.nickname || ''"
                  />
                  <AvatarFallback>{{
                    userStore.userInfo?.nickname?.charAt(0) || "U"
                  }}</AvatarFallback>
                </Avatar>
              </Button>

              <!-- 下拉菜单 -->
              <div
                v-if="showUserMenu"
                class="absolute right-0 mt-2 w-56 rounded-md shadow-lg bg-card border"
                @click="showUserMenu = false"
              >
                <div class="py-1">
                  <div class="px-4 py-3 border-b">
                    <p class="text-sm font-medium">
                      {{ userStore.userInfo?.nickname }}
                    </p>
                    <p class="text-xs text-muted-foreground">
                      {{ userStore.userInfo?.email }}
                    </p>
                  </div>
                  <button
                    @click="router.push('/profile')"
                    class="w-full text-left px-4 py-2 text-sm hover:bg-muted transition-colors"
                  >
                    个人主页
                  </button>
                  <button
                    @click="router.push('/settings')"
                    class="w-full text-left px-4 py-2 text-sm hover:bg-muted transition-colors"
                  >
                    设置
                  </button>
                  <div class="border-t"></div>
                  <button
                    @click="handleLogout"
                    class="w-full text-left px-4 py-2 text-sm text-destructive hover:bg-muted transition-colors"
                  >
                    退出登录
                  </button>
                </div>
              </div>
            </div>
          </template>

          <!-- 移动端菜单按钮 -->
          <Button
            variant="ghost"
            size="icon"
            class="md:hidden"
            @click="showMobileMenu = !showMobileMenu"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 6h16M4 12h16M4 18h16"
              />
            </svg>
          </Button>
        </div>
      </div>
    </div>

    <!-- 移动端菜单 -->
    <div v-if="showMobileMenu" class="md:hidden border-t">
      <nav class="container mx-auto px-4 py-4 flex flex-col gap-4">
        <router-link
          to="/"
          class="text-sm font-medium transition-colors hover:text-primary"
          @click="showMobileMenu = false"
        >
          首页
        </router-link>
        <router-link
          to="/categories"
          class="text-sm font-medium transition-colors hover:text-primary text-muted-foreground"
          @click="showMobileMenu = false"
        >
          分类
        </router-link>
        <router-link
          to="/tags"
          class="text-sm font-medium transition-colors hover:text-primary text-muted-foreground"
          @click="showMobileMenu = false"
        >
          标签
        </router-link>
        <router-link
          to="/about"
          class="text-sm font-medium transition-colors hover:text-primary text-muted-foreground"
          @click="showMobileMenu = false"
        >
          关于
        </router-link>
      </nav>
    </div>

    <!-- 搜索对话框 -->
    <div
      v-if="showSearchDialog"
      class="fixed inset-0 z-50 bg-background/80 backdrop-blur-sm"
      @click="showSearchDialog = false"
    >
      <div
        class="fixed left-[50%] top-[50%] z-50 w-full max-w-lg translate-x-[-50%] translate-y-[-50%] gap-4 border bg-background p-6 shadow-lg sm:rounded-lg"
        @click.stop
      >
        <h2 class="text-lg font-semibold mb-4">搜索文章</h2>
        <div class="flex gap-2">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="输入关键词..."
            class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
            @keyup.enter="handleSearch"
            autofocus
          />
          <Button @click="handleSearch">搜索</Button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { useAppStore } from "@/stores/app";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";

const router = useRouter();
const userStore = useUserStore();
const appStore = useAppStore();

// 菜单状态
const showUserMenu = ref(false);
const showMobileMenu = ref(false);
const showSearchDialog = ref(false);
const searchKeyword = ref("");

// 主题状态
const isDark = ref(false);

// 用户菜单引用
const userMenuRef = ref<HTMLElement | null>(null);

/**
 * 处理点击外部关闭菜单
 */
function handleClickOutside(event: MouseEvent) {
  if (userMenuRef.value && !userMenuRef.value.contains(event.target as Node)) {
    showUserMenu.value = false;
  }
}

/**
 * 添加和移除事件监听
 */
onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
});

/**
 * 切换主题
 */
function toggleTheme() {
  appStore.toggleDark();
  isDark.value = appStore.isDark;
}

/**
 * 处理搜索
 */
function handleSearch() {
  if (!searchKeyword.value.trim()) return;
  showSearchDialog.value = false;
  router.push({
    name: "Search",
    query: { keyword: searchKeyword.value.trim() },
  });
  searchKeyword.value = "";
}

/**
 * 退出登录
 */
async function handleLogout() {
  await userStore.logout();
  router.push("/login");
}
</script>
