<template>
  <div class="min-h-screen flex items-center justify-center bg-transparent">
    <div class="text-center space-y-4">
      <h1 class="text-6xl font-bold text-muted-foreground">404</h1>
      <p class="text-xl text-muted-foreground">页面未找到</p>
      <router-link
        to="/"
        class="inline-block px-6 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90"
      >
        返回首页
      </router-link>
    </div>
  </div>
</template>
