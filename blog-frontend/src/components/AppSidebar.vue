<script setup lang="ts">
import {
  Home,
  Tag,
  Hash,
  FileText,
  User,
  Settings,
  LogIn,
  LogOut,
} from "lucide-vue-next";
import Logo from "@/components/Logo.vue";
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarTrigger,
  SidebarSeparator,
  useSidebar,
} from "@/components/ui/sidebar";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { useLocaleStore } from "@/stores/locale";

const router = useRouter();
const userStore = useUserStore();
const localeStore = useLocaleStore();
const { state } = useSidebar();

const navMain = [
  {
    titleKey: "navDiscover",
    items: [
      {
        titleKey: "navHome",
        url: "/",
        icon: Home,
      },
      {
        titleKey: "navCategories",
        url: "/categories",
        icon: Tag,
      },
      {
        titleKey: "navTags",
        url: "/tags",
        icon: Hash,
      },
      {
        titleKey: "articleManage",
        url: "/articles/manage",
        icon: FileText,
        requiresAuth: true,
      },
    ],
  },
];

const navUser = [
  {
    titleKey: "navUser",
    items: [
      {
        titleKey: "navProfile",
        url: "/profile",
        icon: User,
      },
      {
        titleKey: "navSettings",
        url: "/settings",
        icon: Settings,
      },
    ],
  },
];

const handleLogout = () => {
  userStore.logout();
  router.push("/login");
};
</script>

<template>
  <Sidebar
    collapsible="icon"
    class="border-r-0 bg-transparent"
    variant="floating"
  >
    <!-- Custom Blur Backdrop for Sidebar Readability - Enhanced with glass opacity -->
    <div
      class="absolute inset-0 z-0 pointer-events-none rounded-lg border border-white/5"
      :style="{
        background: `rgba(var(--glass-rgb), calc(var(--glass-opacity, 0.4) * 0.6))`,
        backdropFilter: `blur(var(--glass-blur)) saturate(160%)`,
        WebkitBackdropFilter: `blur(var(--glass-blur)) saturate(160%)`,
      }"
    ></div>

    <SidebarHeader class="relative z-10">
      <div class="flex items-center gap-2 px-1 py-2 relative">
        <Logo :collapsed="state === 'collapsed'" />
      </div>
    </SidebarHeader>
    <SidebarContent class="overflow-x-hidden relative z-10">
      <SidebarGroup v-for="group in navMain" :key="group.titleKey">
        <SidebarGroupLabel>{{
          localeStore.t[group.titleKey]
        }}</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <template v-for="item in group.items" :key="item.titleKey">
              <SidebarMenuItem
                v-if="!item.requiresAuth || userStore.isLoggedIn"
              >
                <SidebarMenuButton
                  as-child
                  :tooltip="localeStore.t[item.titleKey]"
                >
                  <router-link
                    :to="item.url"
                    active-class="sidebar-nav-active"
                    class="sidebar-nav-item flex w-full items-center gap-2 overflow-hidden rounded-md p-2 text-left text-sm outline-none ring-sidebar-ring transition-all focus-visible:ring-2 disabled:pointer-events-none disabled:opacity-50 font-mono pl-3"
                  >
                    <component :is="item.icon" class="size-4 shrink-0" />
                    <div class="flex flex-1 overflow-hidden">
                      <div class="line-clamp-1 pr-6">
                        {{ localeStore.t[item.titleKey] }}
                      </div>
                    </div>
                  </router-link>
                </SidebarMenuButton>
              </SidebarMenuItem>
            </template>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarSeparator />

      <SidebarGroup v-if="userStore.isLoggedIn">
        <SidebarGroupLabel>{{ localeStore.t.navUser }}</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem
              v-for="item in navUser[0]!.items"
              :key="item.titleKey"
            >
              <SidebarMenuButton
                as-child
                :tooltip="localeStore.t[item.titleKey]"
              >
                <router-link
                  :to="item.url"
                  active-class="sidebar-nav-active"
                  class="sidebar-nav-item flex w-full items-center gap-2 overflow-hidden rounded-md p-2 text-left text-sm outline-none ring-sidebar-ring transition-all focus-visible:ring-2 disabled:pointer-events-none disabled:opacity-50 font-mono pl-3"
                >
                  <component :is="item.icon" class="size-4 shrink-0" />
                  <div class="flex flex-1 overflow-hidden">
                    <div class="line-clamp-1 pr-6">
                      {{ localeStore.t[item.titleKey] }}
                    </div>
                  </div>
                </router-link>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton
                @click="handleLogout"
                :tooltip="localeStore.t.navLogout"
                class="sidebar-nav-item font-mono transition-colors"
              >
                <LogOut />
                <span>{{ localeStore.t.navLogout }}</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarGroup v-else>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton as-child :tooltip="localeStore.t.navLogin">
                <router-link
                  to="/login"
                  class="sidebar-nav-item font-mono transition-colors"
                >
                  <LogIn />
                  <span>{{ localeStore.t.navLogin }}</span>
                </router-link>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>
    <SidebarFooter class="mt-auto relative z-10">
      <div class="flex items-center justify-start px-2 pb-2">
        <SidebarTrigger
          class="h-8 w-8 rounded-full border border-sidebar-border/60 bg-sidebar/60 text-sidebar-foreground shadow-sm backdrop-blur-md transition-all"
        />
      </div>
    </SidebarFooter>
    <!-- <SidebarRail /> -->
  </Sidebar>
</template>

<style scoped>
:deep([data-sidebar="sidebar"]) {
  background-color: transparent !important;
  border: none !important;
  box-shadow: none !important;
}
</style>
