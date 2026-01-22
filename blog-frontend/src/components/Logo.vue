<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from "vue";

const props = defineProps<{
  collapsed?: boolean;
}>();

const logoClass = computed(() => {
  return props.collapsed ? "w-10 h-10" : "w-auto pr-4";
});

const targetText = "MuleHang";
const randomChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
const displayText = ref(targetText);
const intervalId = ref<number | null>(null);

const letters = computed(() => displayText.value.split(""));

const stopAnimation = () => {
  if (intervalId.value !== null) {
    window.clearInterval(intervalId.value);
    intervalId.value = null;
  }
};

const resetAnimation = () => {
  stopAnimation();
  displayText.value = targetText;
};

const startAnimation = () => {
  stopAnimation();
  let iteration = 0;
  intervalId.value = window.setInterval(() => {
    displayText.value = targetText
      .split("")
      .map((char, index) => {
        if (index < Math.floor(iteration)) {
          return char;
        }
        return randomChars[Math.floor(Math.random() * randomChars.length)];
      })
      .join("");
    iteration += 0.5;
    if (iteration >= targetText.length) {
      stopAnimation();
      displayText.value = targetText;
    }
  }, 40);
};

onMounted(() => {
  startAnimation();
});

onBeforeUnmount(() => {
  stopAnimation();
});
</script>

<template>
  <div
    :class="[
      'group/logo flex items-center gap-3 font-mono font-semibold transition-all duration-300',
      logoClass,
    ]"
    @mouseenter="startAnimation"
    @mouseleave="resetAnimation"
  >
    <!-- Logo Icon (JetBrains Inspired: Abstract Geometric) -->
    <div
      class="relative flex h-10 w-10 shrink-0 items-center justify-center transition-transform duration-500 ease-out group-hover/logo:scale-110 group-hover/logo:-rotate-3"
    >
      <!-- Colorful Abstract Shape -->
      <svg
        viewBox="0 0 40 40"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        class="w-full h-full drop-shadow-xl"
      >
        <defs>
          <linearGradient id="shape-grad-1" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="#FF3358" />
            <stop offset="100%" stop-color="#FF8947" />
          </linearGradient>
          <linearGradient id="shape-grad-2" x1="100%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stop-color="#6B57FF" />
            <stop offset="100%" stop-color="#087CFA" />
          </linearGradient>
          <linearGradient id="shape-grad-3" x1="0%" y1="100%" x2="100%" y2="0%">
            <stop offset="0%" stop-color="#FE2857" />
            <stop offset="100%" stop-color="#A855F7" />
          </linearGradient>
        </defs>

        <!-- Shape 1: Bottom Left / Main Base -->
        <path
          d="M4 36 L 20 8 L 36 36 Z"
          fill="url(#shape-grad-2)"
          class="opacity-90 group-hover/logo:opacity-100 transition-opacity duration-300"
          style="mix-blend-mode: normal"
        />

        <!-- Shape 2: Top / Overlay -->
        <path
          d="M20 8 L 36 36 L 38 12 Z"
          fill="url(#shape-grad-1)"
          class="opacity-80 group-hover/logo:opacity-100 transition-opacity duration-300"
          style="mix-blend-mode: hard-light"
        />

        <!-- Shape 3: Geometric Accent -->
        <rect
          x="4"
          y="24"
          width="16"
          height="12"
          transform="rotate(-15 12 30)"
          fill="url(#shape-grad-3)"
          class="opacity-70 group-hover/logo:opacity-90 transition-opacity duration-300 blur-[0.5px]"
          style="mix-blend-mode: screen"
        />
      </svg>

      <!-- Ambient Glow -->
      <div
        class="absolute inset-0 bg-purple-500/20 blur-xl rounded-full opacity-0 group-hover/logo:opacity-50 transition-opacity duration-500 -z-10"
      ></div>
    </div>

    <!-- Text Logo -->
    <div
      v-if="!collapsed"
      class="flex flex-col opacity-100 transition-opacity duration-300 overflow-hidden whitespace-nowrap pl-1"
    >
      <div
        class="flex items-center text-xl leading-none tracking-tight font-bold"
      >
        <!-- Opening Bracket, Text, Closing Bracket -->
        <div class="relative flex items-center">
          <!-- Left Bracket: < -->
          <span
            class="inline-block transition-all duration-500 ease-in-out transform opacity-0 -translate-x-2 max-w-0 overflow-hidden group-hover/logo:opacity-100 group-hover/logo:translate-x-0 group-hover/logo:max-w-5 group-hover/logo:mr-1 bg-linear-to-r from-purple-400 to-pink-500 bg-clip-text text-transparent whitespace-nowrap"
          >
            &lt;
          </span>

          <!-- Main Text Wrapper -->
          <div class="relative flex flex-col items-center">
            <span
              :style="{ fontFamily: 'JetBrains Mono, monospace' }"
              class="z-10 relative"
            >
              <span
                v-for="(letter, index) in letters"
                :key="`${letter}-${index}`"
                :class="[
                  'transition-colors duration-300',
                  index < 4
                    ? 'group-hover/logo:text-purple-400'
                    : 'group-hover/logo:text-pink-400',
                ]"
              >
                {{ letter }}
              </span>
            </span>

            <!-- Underline -->
            <span
              class="absolute -bottom-0.5 left-0 h-0.5 w-full bg-linear-to-r from-purple-500 via-pink-500 to-blue-500 transform scale-x-0 group-hover/logo:scale-x-100 transition-transform duration-500 ease-out origin-center"
            ></span>
          </div>

          <!-- Right Bracket: /> -->
          <span
            class="inline-block transition-all duration-500 ease-in-out transform opacity-0 translate-x-2 max-w-0 overflow-hidden group-hover/logo:opacity-100 group-hover/logo:translate-x-0 group-hover/logo:max-w-7.5 group-hover/logo:ml-1 bg-linear-to-r from-pink-500 to-blue-500 bg-clip-text text-transparent whitespace-nowrap"
          >
            /&gt;
          </span>
        </div>
      </div>

      <!-- Subtitle -->
      <span
        class="text-[0.6rem] text-muted-foreground uppercase tracking-[0.3em] transition-colors duration-300 pl-0.5 mt-0.5 group-hover/logo:text-primary/70"
      >
        SYSTEM
      </span>
    </div>
  </div>
</template>
