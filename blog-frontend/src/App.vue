<template>
  <BeamBackground />
  <MainLayout v-if="!isStandalonePage">
    <router-view />
  </MainLayout>
  <div v-else class="min-h-screen bg-transparent text-foreground">
    <div class="min-h-screen flex flex-col">
      <router-view />
    </div>
  </div>
  <Toaster />
</template>

<script setup lang="ts">
import { Toaster } from "@/components/ui/sonner";
import MainLayout from "@/layouts/MainLayout.vue";
import BeamBackground from "@/components/BeamBackground.vue";
import { useRoute } from "vue-router";
import { computed } from "vue";

const route = useRoute();

const isStandalonePage = computed(() => {
  const name = route.name as string;
  return ["Login", "Register", "GitHubCallback"].includes(name);
});
</script>
