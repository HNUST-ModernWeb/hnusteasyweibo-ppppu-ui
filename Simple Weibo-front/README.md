# 简易版微博

一个基于 Vue 3 + Vite 的简易微博系统。

## 功能特性

- ✨ 用户注册/登录
- 📝 发布微博（280字限制）
- 💬 评论功能
- 👤 个人中心
- 📱 响应式设计
- 🎨 微博风格 UI

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **路由**: Vue Router 5
- **状态管理**: Pinia
- **HTTP 客户端**: Axios
- **代码格式化**: Prettier

## 项目结构

```
src/
├── api/              # API 接口
├── components/       # 公共组件
│   └── AuthDialog.vue    # 登录/注册对话框
├── router/           # 路由配置
├── store/            # 状态管理
│   └── user.js          # 用户状态
├── utils/            # 工具函数
│   └── request.js       # Axios 封装
├── views/            # 页面组件
│   ├── Home.vue         # 首页
│   ├── PostDetail.vue   # 文章详情
│   ├── CreatePost.vue   # 写文章
│   └── Profile.vue      # 个人中心
├── App.vue           # 根组件
└── main.js           # 入口文件
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发环境运行

```bash
npm run dev
```

### 生产环境构建

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 环境配置

创建 `.env` 文件配置 API 地址：

```env
VITE_API_BASE_URL=http://localhost:3000
```

## 页面说明

### 首页 (/)
- 展示微博时间线
- 登录后可直接发布微博
- 支持分页浏览
- 显示点赞数、评论数

### 微博详情 (/post/:id)
- 显示完整微博内容
- 评论列表和发表评论

### 个人中心 (/profile)
- 用户信息展示
- 我的微博列表
- 统计数据

## API 接口

后端 API 需要提供以下接口：

- `POST /api/login` - 用户登录
- `POST /api/register` - 用户注册
- `GET /api/user/:id` - 获取用户信息
- `GET /api/posts` - 获取文章列表
- `GET /api/post/:id` - 获取文章详情
- `POST /api/post` - 创建文章
- `GET /api/comments` - 获取评论列表
- `POST /api/comment` - 发表评论

## 开发建议

- 使用 VS Code + Vue (Official) 扩展
- 安装 Vue.js devtools 浏览器扩展
- 遵循 Vue 3 Composition API 最佳实践

## 浏览器支持

- Chrome (推荐)
- Edge
- Firefox
- Safari

## License

MIT

## 作者

彭琴
