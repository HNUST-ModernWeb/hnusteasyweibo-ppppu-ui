<script setup>
import { computed } from 'vue'

// 直接导入所有 SVG 文件
import dianzanSvg from '@/assets/icons/dianzan.svg'
import pinglunSvg from '@/assets/icons/31pinglun.svg'
import zhuanfaSvg from '@/assets/icons/31zhuanfa.svg'
import tupianSvg from '@/assets/icons/shangchuantupian.svg'
import biaoqingSvg from '@/assets/icons/biaoqingbao.svg'
import jianjieSvg from '@/assets/icons/gerenjianjie.svg'

const props = defineProps({
  name: {
    type: String,
    required: true
  },
  size: {
    type: [Number, String],
    default: 20
  },
  color: {
    type: String,
    default: 'currentColor'
  },
  filled: {
    type: Boolean,
    default: false
  }
})

// SVG 文件映射表
const svgMap = {
  like: dianzanSvg,
  likeOutline: dianzanSvg,
  comment: pinglunSvg,
  repost: zhuanfaSvg,
  camera: tupianSvg,
  emoji: biaoqingSvg,
  bio: jianjieSvg,
}

// Emoji 映射表（作为回退）
const iconEmojis = {
  logo: '🐦',
  home: '🏠',
  user: '👤',
  back: '←',
  like: '❤️',
  likeOutline: '🤍',
  comment: '💬',
  repost: '🔄',
  edit: '✏️',
  delete: '🗑️',
  camera: '📷',
  emoji: '😊',
  bio: '📝',
  bell: '🔔',
  arrowDown: '▼',
  arrowUp: '▲',
  verified: '✓',
  lock: '🔒',
  email: '📧',
  more: '⋯'
}

const hasSvgFile = computed(() => !!svgMap[props.name])
const svgUrl = computed(() => svgMap[props.name] || null)
const iconContent = computed(() => iconEmojis[props.name] || props.name)
</script>

<template>
  <!-- 如果有SVG文件，使用img标签加载 -->
  <img 
    v-if="hasSvgFile && svgUrl"
    :src="svgUrl" 
    class="icon-svg"
    :class="{ 'is-filled': filled }"
    :style="{ 
      width: typeof size === 'number' ? size + 'px' : size,
      height: typeof size === 'number' ? size + 'px' : size,
      filter: filled ? 'invert(35%) sepia(98%) saturate(6687%) hue-rotate(325deg) brightness(96%) contrast(101%)' : 'none'
    }"
    alt=""
  />
  <!-- 否则使用emoji -->
  <span 
    v-else
    class="icon-emoji" 
    :style="{ 
      fontSize: typeof size === 'number' ? size + 'px' : size,
      color: color
    }"
  >
    {{ iconContent }}
  </span>
</template>

<style scoped>
.icon-svg {
  display: inline-block;
  vertical-align: middle;
  transition: filter 0.2s ease;
}

.icon-emoji {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
</style>
