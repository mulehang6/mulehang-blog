<template>
  <div
    class="fixed inset-0 -z-10 overflow-hidden pointer-events-none transition-colors duration-500 bg-[#F5F8FC] dark:bg-[#030303]"
  >
    <!-- Use same SVG for both modes, adjust blending mode -->
    <svg
      class="absolute inset-0 w-full h-full"
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 1600 900"
      preserveAspectRatio="xMidYMid slice"
    >
      <defs>
        <!-- JetBrains Palette -->
        <linearGradient id="jb-purple" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color: #6b57ff; stop-opacity: 0.8" />
          <stop offset="100%" style="stop-color: #6b57ff; stop-opacity: 0" />
        </linearGradient>

        <linearGradient id="jb-blue" x1="100%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" style="stop-color: #087cfa; stop-opacity: 0.8" />
          <stop offset="100%" style="stop-color: #087cfa; stop-opacity: 0" />
        </linearGradient>

        <linearGradient id="jb-magenta" x1="0%" y1="100%" x2="100%" y2="0%">
          <stop offset="0%" style="stop-color: #ff3358; stop-opacity: 0.8" />
          <stop offset="100%" style="stop-color: #ff3358; stop-opacity: 0" />
        </linearGradient>

        <linearGradient id="jb-orange" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" style="stop-color: #ff8947; stop-opacity: 0.9" />
          <stop offset="100%" style="stop-color: #ff8947; stop-opacity: 0.1" />
        </linearGradient>

        <!-- Petal Contrasts (Sidebar accents moved global) -->
        <linearGradient id="petal-purple" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color: #a855f7; stop-opacity: 0.7" />
          <stop offset="100%" style="stop-color: #8b5cf6; stop-opacity: 0" />
        </linearGradient>
        <linearGradient id="petal-cyan" x1="0%" y1="100%" x2="100%" y2="0%">
          <stop offset="0%" style="stop-color: #00e5ff; stop-opacity: 0.7" />
          <stop offset="100%" style="stop-color: #2979ff; stop-opacity: 0" />
        </linearGradient>
        <linearGradient id="petal-orange" x1="0%" y1="0%" x2="100%" y2="50%">
          <stop offset="0%" style="stop-color: #ff3d00; stop-opacity: 0.75" />
          <stop offset="100%" style="stop-color: #ff9100; stop-opacity: 0" />
        </linearGradient>
        <linearGradient id="petal-pink" x1="50%" y1="0%" x2="50%" y2="100%">
          <stop offset="0%" style="stop-color: #ec4899; stop-opacity: 0.55" />
          <stop offset="100%" style="stop-color: #f43f5e; stop-opacity: 0" />
        </linearGradient>
      </defs>

      <!-- Composition: Full Screen Multi-layered Fan Waves (JetBrains Style) -->
      <g
        transform="scale(1.1) translate(-50, -50)"
        class="mix-blend-multiply dark:mix-blend-screen"
        style="transition: all 0.5s ease"
      >
        <!-- Base Filler -->
        <path
          d="M1800 1000 L -200 1000 L -200 -200 L 1800 -200 Z"
          fill="#E8EBFF"
          class="opacity-40 dark:opacity-5 dark:fill-[#0F0F1A]"
        />

        <!-- Layer 1: Deep Blue (Furthest) -->
        <path
          d="M-100 1000 C 200 600, 600 200, 1000 -200 L 2000 -200 L 2000 1000 Z"
          fill="url(#jb-blue)"
          class="opacity-15 dark:opacity-20"
        />

        <!-- Layer 2: Blue-Purple Transition -->
        <path
          d="M200 1000 C 500 650, 800 250, 1150 -200 L 2000 -200 L 2000 1000 Z"
          fill="url(#jb-blue)"
          class="opacity-20 dark:opacity-30"
        />

        <!-- Layer 3: Purple -->
        <path
          d="M500 1000 C 800 700, 1000 300, 1300 -200 L 2000 -200 L 2000 1000 Z"
          fill="url(#jb-purple)"
          class="opacity-25 dark:opacity-40"
        />

        <!-- Layer 4: Deep Magenta -->
        <path
          d="M800 1000 C 1050 700, 1200 350, 1450 -200 L 2000 -200 L 2000 1000 Z"
          fill="url(#jb-magenta)"
          class="opacity-30 dark:opacity-50"
        />

        <!-- Layer 5: Magenta-Red -->
        <path
          d="M1050 1000 C 1250 750, 1350 400, 1550 -200 L 2000 -200 L 2000 1000 Z"
          fill="url(#jb-magenta)"
          class="opacity-35 dark:opacity-60"
        />

        <!-- Layer 6: Red-Orange -->
        <path
          d="M1250 1000 C 1450 750, 1550 450, 1700 -200 L 2000 -200 L 2000 1000 Z"
          fill="url(#jb-orange)"
          class="opacity-40 dark:opacity-70"
        />

        <!-- Layer 7: Bright Orange Core -->
        <path
          d="M1450 1000 C 1600 800, 1700 500, 1850 -200 L 2000 -200 L 2000 1000 Z"
          fill="url(#jb-orange)"
          class="opacity-50 dark:opacity-80"
        />
      </g>

      <!-- Petal Contrast Layer (now global, behind sidebar + content) -->
      <g class="mix-blend-overlay dark:mix-blend-color-dodge" opacity="0.55">
        <path
          d="M-240 40 C 260 240, 520 480, 360 780 C 240 980, 380 1080, 80 1140 L -240 1140 Z"
          fill="url(#petal-cyan)"
          class="blur-3xl"
        />
        <path
          d="M-120 140 C 320 260, 640 460, 760 560 C 620 660, 300 860, -120 980 Z"
          fill="url(#petal-orange)"
          class="blur-2xl mix-blend-screen"
        />
        <path
          d="M-200 380 C 300 450, 600 640, 560 980 C 520 1120, 120 1120, -200 1040 Z"
          fill="url(#petal-purple)"
          class="blur-2xl"
        />
        <circle
          cx="180"
          cy="-60"
          r="440"
          fill="url(#petal-purple)"
          class="blur-3xl mix-blend-plus-lighter"
        />
        <path
          d="M60 240 C 360 280, 520 420, 460 580 C 380 760, 170 820, -60 900 L -140 620 Z"
          fill="url(#petal-pink)"
          class="blur-2xl mix-blend-soft-light"
        />
      </g>

      <!-- Glass overlay texture -->
      <image
        href="https://grainy-gradients.vercel.app/noise.svg"
        width="100%"
        height="100%"
        opacity="0.05"
      />
    </svg>
  </div>
</template>
