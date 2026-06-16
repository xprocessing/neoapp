<template>
  <div class="min-h-screen flex items-center justify-center bg-[var(--color-bg)] px-4">
    <div class="text-center">
      <h1 class="text-8xl font-bold text-[var(--color-primary)]/20 mb-4">404</h1>
      <p class="text-lg text-[var(--color-text-secondary)] mb-2">页面未找到</p>
      <p class="text-sm text-[var(--color-text-tertiary)] mb-8">您访问的页面不存在或已被移除</p>
      <t-button theme="primary" @click="$router.push('/dashboard')">
        <template #icon><Home class="w-4 h-4" /></template>
        返回首页
      </t-button>
    </div>
  </div>
</template>

<script setup>
import { Home } from 'lucide-vue-next'
</script>
