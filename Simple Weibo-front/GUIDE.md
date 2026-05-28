# 快速启动指南

## 项目说明

这是一个完整的个人博客前端项目，包含以下功能：

### ✨ 主要功能

1. **用户系统**
   - 用户注册/登录
   - 个人中心
   - 用户信息展示

2. **文章管理**
   - 浏览文章列表
   - 查看文章详情
   - 发布新文章
   - 文章统计（浏览量、评论数）

3. **评论系统**
   - 发表评论
   - 查看评论列表

4. **响应式设计**
   - 适配各种屏幕尺寸
   - 现代化 UI 设计

## 🚀 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

项目会在 `http://localhost:5173` 启动（端口可能不同）

### 3. 测试账号

项目内置了 Mock 数据，可以直接使用以下账号登录：

- **用户名**: admin
- **密码**: 123456

或者直接注册新账号进行测试。

## 📁 项目结构

```
src/
├── api/                    # API 接口定义
│   └── index.js
├── components/             # 公共组件
│   └── AuthDialog.vue      # 登录/注册对话框
├── router/                 # 路由配置
│   └── index.js
├── store/                  # 状态管理
│   └── user.js             # 用户状态
├── utils/                  # 工具函数
│   ├── request.js          # Axios 封装（含 Mock）
│   └── mockApi.js          # Mock 数据和 API
├── views/                  # 页面组件
│   ├── Home.vue            # 首页 - 文章列表
│   ├── PostDetail.vue      # 文章详情页
│   ├── CreatePost.vue      # 写文章页
│   └── Profile.vue         # 个人中心
├── App.vue                 # 根组件
└── main.js                 # 入口文件
```

## 🎯 功能说明

### 首页 (/)
- 展示所有博客文章
- 文章卡片显示标题、摘要、作者、统计信息
- 支持分页浏览
- 点击文章卡片进入详情页

### 文章详情 (/post/:id)
- 显示完整文章内容
- 作者信息和发布时间
- 文章统计（浏览量、评论数）
- 评论列表
- 发表评论（需登录）

### 写文章 (/create)
- 输入文章标题
- 输入文章摘要（可选）
- 编辑文章内容（支持 Markdown）
- 发布文章
- 需要登录才能访问

### 个人中心 (/profile)
- 显示用户信息
- 统计数据（文章数、粉丝数、关注数）
- 我的文章列表
- 退出登录
- 需要登录才能访问

## 🔧 开发模式

### Mock 数据模式（当前）

项目默认使用 Mock 数据，无需后端服务器即可运行。

在 `src/utils/request.js` 中：
```javascript
const USE_MOCK = true  // 使用 Mock 数据
```

Mock 数据包含：
- 1 个默认用户（admin）
- 2 篇示例文章
- 1 条示例评论

### 真实 API 模式

如果你有真实的后端服务器，可以切换到真实 API：

1. 修改 `src/utils/request.js`：
```javascript
const USE_MOCK = false  // 使用真实 API
```

2. 配置 `.env` 文件：
```env
VITE_API_BASE_URL=http://your-backend-url
```

## 📝 后端 API 接口规范

如果要连接真实后端，需要实现以下接口：

### 用户相关
- `POST /api/login` - 登录
- `POST /api/register` - 注册
- `GET /api/user/:id` - 获取用户信息

### 文章相关
- `GET /api/posts` - 获取文章列表（支持分页和筛选）
- `GET /api/post/:id` - 获取文章详情
- `POST /api/post` - 创建文章（需要认证）

### 评论相关
- `GET /api/comments` - 获取评论列表
- `POST /api/comment` - 发表评论（需要认证）

详细的接口参数和返回格式请参考 `src/utils/mockApi.js` 中的实现。

## 🎨 自定义配置

### 修改主题色

在 `src/App.vue` 和各个组件的 `<style>` 中修改颜色变量：
```css
/* 主色调 */
background: #1890ff;  /* 蓝色 */

/* 可以改为其他颜色，如 */
background: #52c41a;  /* 绿色 */
background: #722ed1;  /* 紫色 */
```

### 修改网站标题

在 `src/App.vue` 中修改：
```vue
<div class="logo" @click="goHome">
  <span class="icon">📝</span>
  <span class="text">我的博客</span>  <!-- 修改这里 -->
</div>
```

在 `index.html` 中修改页面标题：
```html
<title>我的博客</title>  <!-- 修改这里 -->
```

## 🛠️ 常用命令

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 代码格式化
npm run format
```

## 📦 部署

### 构建

```bash
npm run build
```

构建产物在 `dist` 目录。

### 部署到静态服务器

将 `dist` 目录的内容上传到任何静态文件服务器（如 Nginx、Apache、Vercel、Netlify 等）。

### 注意事项

由于使用了 Vue Router 的 HTML5 History 模式，需要配置服务器支持 SPA 路由：

**Nginx 配置示例：**
```nginx
location / {
  try_files $uri $uri/ /index.html;
}
```

## 🐛 常见问题

### 1. 端口被占用

修改 `vite.config.js`：
```javascript
export default defineConfig({
  server: {
    port: 3000  // 改为其他端口
  }
})
```

### 2. 登录后刷新页面退出登录

这是正常的，因为 Mock 模式下 token 不会真正验证。使用真实后端时不会有这个问题。

### 3. 图片不显示

默认头像使用了 dicebear API 生成。如果网络问题导致无法加载，会显示默认的 SVG 头像。

## 📄 License

MIT

## 👨‍💻 作者

彭琴

---

**祝你使用愉快！如有问题欢迎反馈。** 🎉
