# 图标替换说明

## 目录结构
```
src/assets/icons/
├── README.md          # 本说明文件
├── logo.svg           # 网站Logo
├── home.svg           # 首页
├── user.svg           # 用户
├── back.svg           # 返回
├── like.svg           # 点赞（已点赞）
├── like-outline.svg   # 点赞（未点赞）
├── comment.svg        # 评论
├── repost.svg         # 转发
├── edit.svg           # 编辑
├── delete.svg         # 删除
├── camera.svg         # 相机/图片
├── emoji.svg          # 表情
├── bio.svg            # 简介/编辑
├── arrow-down.svg     # 向下箭头
├── arrow-up.svg       # 向上箭头
├── verified.svg       # 认证
├── lock.svg           # 锁定
├── email.svg          # 邮件
└── more.svg           # 更多
```

## 如何替换图标

### 方式1：使用 SVG 文件（推荐）

1. **下载 SVG 图标**
   - 访问 [Iconfont](https://www.iconfont.cn/) 或 [Heroicons](https://heroicons.com/)
   - 搜索你想要的图标（如：heart、comment、share）
   - 下载 SVG 格式

2. **放置图标**
   - 将下载的 `.svg` 文件放入此目录
   - 文件名参考上面的列表

3. **修改 Icon.vue 组件**
   - 打开 `src/components/Icon.vue`
   - 将对应图标的 emoji 替换为 SVG 引用

### 方式2：继续使用 Emoji

如果不想使用 SVG，可以直接修改 `src/components/Icon.vue` 中的 `icons` 对象，更换 emoji。

## 图标命名对应表

| 图标用途 | 文件名 | 当前 Emoji |
|---------|--------|-----------|
| 网站Logo | logo.svg | 🐦 |
| 首页 | home.svg | 🏠 |
| 用户 | user.svg | 👤 |
| 返回 | back.svg | ← |
| 点赞（已点）| like.svg | ❤️ |
| 点赞（未点）| like-outline.svg | 🤍 |
| 评论 | comment.svg | 💬 |
| 转发 | repost.svg | 🔄 |
| 编辑 | edit.svg | ✏️ |
| 删除 | delete.svg | 🗑️ |
| 相机/图片 | camera.svg | 📷 |
| 表情 | emoji.svg | 😊 |
| 简介 | bio.svg | 📝 |
| 向下箭头 | arrow-down.svg | ▼ |
| 向上箭头 | arrow-up.svg | ▲ |
| 认证 | verified.svg | ✓ |
| 锁定 | lock.svg | 🔒 |
| 邮件 | email.svg | 📧 |
| 更多 | more.svg | ⋯ |

## 使用示例

在组件中使用：
```vue
<template>
  <Icon name="like" :size="24" color="#ff4757" />
  <Icon name="comment" :size="20" />
  <Icon name="back" :size="16" />
</template>

<script setup>
import Icon from '@/components/Icon.vue'
</script>
```
