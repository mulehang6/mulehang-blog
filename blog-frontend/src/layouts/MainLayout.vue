<script setup lang="ts">
import { SidebarProvider, SidebarInset } from "@/components/ui/sidebar";
import AppSidebar from "@/components/AppSidebar.vue";
import AppFooter from "@/components/AppFooter.vue";
import ModeToggle from "@/components/ModeToggle.vue";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { useRouter } from "vue-router";
import { ref, onMounted, onBeforeUnmount, watch, computed } from "vue";
import { useUserStore } from "@/stores/user";
import { useLocaleStore } from "@/stores/locale";

const router = useRouter();
const userStore = useUserStore();
const localeStore = useLocaleStore();

const showUserMenu = ref(false);
const showSearchDialog = ref(false);
const searchKeyword = ref("");
const userMenuRef = ref<HTMLElement | null>(null);

// Global Opacity Control (0-100)
const opacityPercentValue = ref(40);
let glassRafId: number | null = null;
let pendingGlassValue = opacityPercentValue.value;

const MIN_GLASS_BLUR_PX = 12;
const GLASS_BLUR_RANGE_PX = 26;

const applyGlassVariables = (percentValue: number) => {
  const clamped = Math.min(Math.max(percentValue / 100, 0), 1);
  const strongOpacity = Math.min(clamped + 0.18, 0.9);
  const blurBase = Math.round(
    MIN_GLASS_BLUR_PX + clamped * GLASS_BLUR_RANGE_PX,
  );
  const blurStrong = blurBase + 8;
  const blurSoft = Math.max(blurBase - 6, 6);

  document.documentElement.style.setProperty(
    "--glass-opacity",
    clamped.toFixed(3),
  );
  document.documentElement.style.setProperty(
    "--glass-opacity-strong",
    strongOpacity.toFixed(3),
  );
  document.documentElement.style.setProperty("--glass-blur", `${blurBase}px`);
  document.documentElement.style.setProperty(
    "--glass-blur-strong",
    `${blurStrong}px`,
  );
  document.documentElement.style.setProperty(
    "--glass-blur-soft",
    `${blurSoft}px`,
  );
};

const scheduleGlassUpdate = (newVal: number) => {
  pendingGlassValue = newVal;
  if (glassRafId !== null) return;
  glassRafId = window.requestAnimationFrame(() => {
    applyGlassVariables(pendingGlassValue);
    glassRafId = null;
  });
};

// 将透明度与模糊度应用到根元素，使其全局生效
watch(opacityPercentValue, scheduleGlassUpdate, { immediate: true });

// 计算透明度百分比用于显示
const opacityPercent = computed(() => Math.round(opacityPercentValue.value));
const localeLabel = computed(() =>
  localeStore.locale === "zh-CN" ? "中文" : "EN",
);

function handleClickOutside(event: MouseEvent) {
  if (userMenuRef.value && !userMenuRef.value.contains(event.target as Node)) {
    showUserMenu.value = false;
  }
}

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
  if (glassRafId !== null) {
    window.cancelAnimationFrame(glassRafId);
    glassRafId = null;
  }
});

function handleSearch() {
  if (!searchKeyword.value.trim()) return;
  showSearchDialog.value = false;
  router.push({
    name: "Search",
    query: { keyword: searchKeyword.value.trim() },
  });
  searchKeyword.value = "";
}

async function handleLogout() {
  await userStore.logout();
  router.push("/login");
}
</script>

<template>
  <SidebarProvider>
    <AppSidebar />
    <SidebarInset>
      <div
        class="relative flex min-h-svh flex-1 flex-col bg-transparent transition-colors duration-300"
      >
        <header
          class="flex h-16 shrink-0 items-center justify-between gap-2 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12 sticky top-0 z-10 px-4 mt-2 mx-4 rounded-xl glass-panel"
        >
          <!-- Left: Opacity Controller (Premium Design) -->
          <div
            class="flex items-center gap-2.5 bg-linear-to-r from-black/3 to-black/6 dark:from-white/3 dark:to-white/6 px-3 py-1.5 rounded-full border border-black/6 dark:border-white/8 backdrop-blur-md group/opacity hover:from-black/6 hover:to-black/10 dark:hover:from-white/6 dark:hover:to-white/10 transition-all duration-300 shadow-sm"
          >
            <!-- Refined Opacity Icon: Layered Circles -->
            <div class="relative w-4 h-4 flex items-center justify-center">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                class="transition-transform duration-300 group-hover/opacity:scale-110"
              >
                <!-- Back circle (more transparent) -->
                <circle
                  cx="9"
                  cy="9"
                  r="7"
                  fill="currentColor"
                  class="opacity-20"
                />
                <!-- Middle circle -->
                <circle
                  cx="12"
                  cy="12"
                  r="7"
                  fill="currentColor"
                  class="opacity-40"
                />
                <!-- Front circle (more opaque) -->
                <circle
                  cx="15"
                  cy="15"
                  r="7"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  class="opacity-80"
                />
                <!-- Center highlight -->
                <circle
                  cx="12"
                  cy="12"
                  r="2"
                  fill="currentColor"
                  class="opacity-60"
                />
              </svg>
            </div>
            <span
              class="text-[10px] uppercase font-mono tracking-wider text-muted-foreground hidden sm:inline-block select-none"
              >{{ localeStore.t.opacity }}</span
            >
            <input
              type="range"
              min="0"
              max="100"
              step="1"
              v-model="opacityPercentValue"
              class="w-20 sm:w-24 h-1.5 bg-linear-to-r from-gray-200 to-gray-300 dark:from-gray-700 dark:to-gray-600 rounded-full appearance-none cursor-pointer accent-primary focus:outline-none focus:ring-2 focus:ring-primary/30 transition-all"
            />
            <span
              class="font-mono text-[11px] w-8 text-right tabular-nums text-primary font-medium"
              >{{ opacityPercent }}%</span
            >
          </div>

          <div class="flex items-center gap-2">
            <Button
              variant="ghost"
              size="sm"
              class="hidden md:flex px-2 text-xs font-mono"
              @click="localeStore.toggleLocale()"
            >
              {{ localeLabel }}
            </Button>
            <Button
              variant="ghost"
              size="icon"
              class="hidden md:flex hover:bg-black/5 dark:hover:bg-white/10"
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

            <template v-else>
              <Button
                @click="router.push('/write')"
                variant="default"
                size="sm"
              >
                写文章
              </Button>
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

                <div
                  v-if="showUserMenu"
                  class="absolute right-0 mt-2 w-56 rounded-md shadow-lg bg-card border no-text-glow"
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
                      class="w-full text-left px-4 py-2 text-sm transition-colors menu-item-hover-outline sidebar-nav-item"
                    >
                      个人主页
                    </button>
                    <button
                      @click="router.push('/settings')"
                      class="w-full text-left px-4 py-2 text-sm transition-colors menu-item-hover-outline sidebar-nav-item"
                    >
                      设置
                    </button>
                    <div class="border-t"></div>
                    <button
                      @click="handleLogout"
                      class="w-full text-left px-4 py-2 text-sm text-destructive transition-colors menu-item-hover-outline sidebar-nav-item"
                    >
                      退出登录
                    </button>
                  </div>
                </div>
              </div>
            </template>
            <ModeToggle />
          </div>
        </header>
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
        <div class="flex flex-1 flex-col">
          <main class="flex-1 p-4 pt-4">
            <slot />
          </main>
          <AppFooter />
        </div>
      </div>
    </SidebarInset>
  </SidebarProvider>
</template>
