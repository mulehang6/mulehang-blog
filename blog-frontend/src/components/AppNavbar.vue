<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from "vue";
import { useRoute, useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { Menu, Search, X } from "lucide-vue-next";
import { useUserStore } from "@/stores/user";
import { useLocaleStore } from "@/stores/locale";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import Logo from "@/components/Logo.vue";
import ModeToggle from "@/components/ModeToggle.vue";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const localeStore = useLocaleStore();
const { t } = storeToRefs(localeStore);

const isScrolled = ref(false);
const showMobileMenu = ref(false);
const showSearchDialog = ref(false);
const showUserMenu = ref(false);
const searchKeyword = ref("");
const userMenuRef = ref<HTMLElement | null>(null);

const localeLabel = computed(() =>
  localeStore.locale === "zh-CN" ? "中文" : "EN",
);
const registerLabel = computed(() =>
  localeStore.locale === "zh-CN" ? "注册" : "Register",
);

const navItems = computed(() => [
  { label: t.value.navHome, to: "/", exact: true },
  { label: t.value.navColumns, to: "/columns" },
  { label: t.value.navCategories, to: "/categories" },
  { label: t.value.navTags, to: "/tags" },
  { label: t.value.navAi, to: "/ai" },
  { label: t.value.about, to: "/about" },
]);

/**
 * 根据滚动状态切换导航栏样式。
 */
function handleScroll() {
  isScrolled.value = window.scrollY > 20;
}

/**
 * 判断当前路由是否匹配导航项。
 */
function isActive(path: string, exact = false) {
  if (exact) return route.path === path;
  return route.path.startsWith(path);
}

/**
 * 关闭用户菜单（点击外部）。
 */
function handleClickOutside(event: MouseEvent) {
  if (userMenuRef.value && !userMenuRef.value.contains(event.target as Node)) {
    showUserMenu.value = false;
  }
}

/**
 * 切换语言显示。
 */
function toggleLocale() {
  localeStore.toggleLocale();
}

/**
 * 打开或关闭移动端菜单。
 */
function toggleMobileMenu() {
  showMobileMenu.value = !showMobileMenu.value;
}

/**
 * 关闭移动端菜单。
 */
function closeMobileMenu() {
  showMobileMenu.value = false;
}

/**
 * 处理搜索并跳转。
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
 * 退出登录并回到首页。
 */
async function handleLogout() {
  await userStore.logout();
  showUserMenu.value = false;
  router.push("/");
}

onMounted(() => {
  handleScroll();
  window.addEventListener("scroll", handleScroll);
  document.addEventListener("click", handleClickOutside);
});

onBeforeUnmount(() => {
  window.removeEventListener("scroll", handleScroll);
  document.removeEventListener("click", handleClickOutside);
});
</script>

<template>
  <header
    id="app-navbar"
    :class="[
      'fixed left-0 right-0 top-0 z-50 transition-all duration-500 ease-[cubic-bezier(0.22,1,0.36,1)]',
      isScrolled
        ? 'bg-paper-bg border-b border-ink/10 py-3'
        : 'bg-transparent border-transparent py-5',
    ]"
  >
    <div class="mx-auto flex max-w-6xl items-center justify-between px-6">
      <Logo />

      <nav class="hidden items-center gap-8 md:flex">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="group relative text-[15px] font-medium transition-colors"
          :class="isActive(item.to, item.exact) ? 'text-ink' : 'text-ink-light'"
        >
          {{ item.label }}
          <span
            class="absolute -bottom-2 left-1/2 h-1 w-1 -translate-x-1/2 rounded-full bg-clay opacity-0 transition-all duration-300"
            :class="
              isActive(item.to, item.exact)
                ? 'opacity-100 scale-100'
                : 'scale-0 group-hover:opacity-100 group-hover:scale-100'
            "
          ></span>
        </router-link>
      </nav>

      <div class="flex items-center gap-3">
        <button
          class="hidden rounded-full p-2 text-ink-light transition-colors hover:text-clay md:flex"
          type="button"
          @click="showSearchDialog = true"
        >
          <Search :size="18" :stroke-width="2" />
        </button>

        <button
          class="hidden text-[12px] font-mono uppercase tracking-wider text-ink-light transition-colors hover:text-ink md:flex"
          type="button"
          @click="toggleLocale"
        >
          {{ localeLabel }}
        </button>

        <div class="hidden md:flex">
          <ModeToggle />
        </div>

        <template v-if="!userStore.isLoggedIn">
          <button
            type="button"
            class="text-[15px] font-medium text-ink-light transition-colors hover:text-ink"
            @click="router.push('/login')"
          >
            {{ t.navLogin }}
          </button>
          <button
            type="button"
            class="hidden h-10 inline-flex items-center justify-center rounded-xl bg-ink px-4 text-[14px] font-medium text-white shadow-sketch transition-all hover:bg-clay hover:shadow-none hover:translate-y-[2px] dark:bg-clay dark:text-paper-bg dark:hover:bg-clay/90 md:flex"
            @click="router.push('/register')"
          >
            {{ registerLabel }}
          </button>
        </template>

        <template v-else>
          <button
            type="button"
            class="hidden h-10 inline-flex items-center justify-center rounded-xl bg-ink px-4 text-[14px] font-medium text-white shadow-sketch transition-all hover:bg-clay hover:shadow-none hover:translate-y-[2px] dark:bg-clay dark:text-paper-bg dark:hover:bg-clay/90 md:flex"
            @click="router.push('/write')"
          >
            写文章
          </button>

          <div class="relative" ref="userMenuRef">
            <button
              type="button"
              class="rounded-full p-1 transition-transform hover:-translate-y-0.5"
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
            </button>

            <div
              v-if="showUserMenu"
              class="absolute right-0 mt-3 w-56 rounded-2xl border border-ink/10 bg-paper-card p-2 shadow-soft"
            >
              <div class="px-3 py-2">
                <p class="text-sm font-semibold text-ink">
                  {{ userStore.userInfo?.nickname }}
                </p>
                <p class="text-xs text-ink-light">
                  {{ userStore.userInfo?.email }}
                </p>
              </div>
              <div class="my-2 h-px bg-ink/10"></div>
              <button
                type="button"
                class="w-full rounded-lg px-3 py-2 text-left text-sm text-ink transition-colors hover:bg-paper-dark/60"
                @click="router.push('/articles/manage')"
              >
                {{ t.navArticleManage }}
              </button>
              <button
                type="button"
                class="w-full rounded-lg px-3 py-2 text-left text-sm text-ink transition-colors hover:bg-paper-dark/60"
                @click="router.push('/profile')"
              >
                {{ t.navProfile }}
              </button>
              <button
                type="button"
                class="w-full rounded-lg px-3 py-2 text-left text-sm text-ink transition-colors hover:bg-paper-dark/60"
                @click="router.push('/settings')"
              >
                {{ t.navSettings }}
              </button>
              <button
                type="button"
                class="mt-1 w-full rounded-lg px-3 py-2 text-left text-sm text-clay transition-colors hover:bg-paper-dark/60"
                @click="handleLogout"
              >
                {{ t.navLogout }}
              </button>
            </div>
          </div>
        </template>

        <button
          type="button"
          class="rounded-full p-2 text-ink transition-colors hover:text-clay md:hidden"
          @click="toggleMobileMenu"
        >
          <X v-if="showMobileMenu" :stroke-width="1.5" />
          <Menu v-else :stroke-width="1.5" />
        </button>
      </div>
    </div>

    <div
      v-if="showMobileMenu"
      class="border-t border-ink/10 bg-paper-bg md:hidden"
    >
      <nav class="mx-auto flex max-w-6xl flex-col gap-4 px-6 py-6">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="text-lg font-serif text-ink transition-colors hover:text-clay"
          @click="closeMobileMenu"
        >
          {{ item.label }}
        </router-link>
        <router-link
          v-if="userStore.isLoggedIn"
          to="/articles/manage"
          class="text-lg font-serif text-ink transition-colors hover:text-clay"
          @click="closeMobileMenu"
        >
          {{ t.navArticleManage }}
        </router-link>
        <div class="flex items-center gap-4 pt-2">
          <button
            type="button"
            class="rounded-full border border-ink/10 px-4 py-2 text-sm font-medium text-ink transition-colors hover:border-clay hover:text-clay"
            @click="toggleLocale"
          >
            {{ localeLabel }}
          </button>
          <ModeToggle />
        </div>
      </nav>
    </div>
  </header>

  <div
    v-if="showSearchDialog"
    class="fixed inset-0 z-50 bg-black/30"
    @click="showSearchDialog = false"
  >
    <div
      class="fixed left-1/2 top-1/2 z-50 w-full max-w-lg -translate-x-1/2 -translate-y-1/2 rounded-2xl border border-ink/10 bg-paper-card p-6 shadow-soft"
      @click.stop
    >
      <h2 class="mb-4 text-lg font-semibold text-ink">搜索文章</h2>
      <div class="flex gap-2">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="输入关键词..."
          class="flex h-10 w-full rounded-xl border border-ink/10 bg-transparent px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
          @keyup.enter="handleSearch"
          autofocus
        />
        <button
          type="button"
          class="inline-flex h-10 items-center justify-center rounded-xl bg-ink px-4 text-sm font-medium text-white transition-colors hover:bg-clay dark:bg-clay dark:text-paper-bg whitespace-nowrap leading-none"
          @click="handleSearch"
        >
          搜索
        </button>
      </div>
    </div>
  </div>
</template>
