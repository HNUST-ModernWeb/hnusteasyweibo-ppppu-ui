# 个人博客前端 API 接口文档

## 基础信息

- **Base URL**: `http://localhost:3000`
- **认证方式**: Bearer Token
- **请求格式**: JSON
- **响应格式**: 标准包装格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

## 认证相关

### 1. 用户登录

**POST** `/api/login`

**请求参数**:
```json
{
  "username": "string",
  "password": "string"
}
```

**响应数据**:
```json
{
  "token": "string",
  "user": {
    "id": 1,
    "username": "string",
    "avatar": "string",
    "bio": "string",
    "followers": 100,
    "following": 50
  }
}
```

### 2. 用户注册

**POST** `/api/register`

**请求参数**:
```json
{
  "username": "string",
  "password": "string",
  "avatar?": "string"
}
```

**响应数据**: 同登录接口

### 3. 获取用户信息

**GET** `/api/user/:id`

**响应数据**:
```json
{
  "id": 1,
  "username": "string",
  "avatar": "string",
  "bio": "string",
  "followers": 100,
  "following": 50
}
```

## 微博相关

### 4. 获取微博列表

**GET** `/api/posts`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| page | number | 否 | 页码，默认 1 |
| pageSize | number | 否 | 每页数量，默认 10 |
| authorId | number | 否 | 按作者筛选 |

**响应数据**:
```json
{
  "posts": [
    {
      "id": 1,
      "content": "string",
      "authorId": 1,
      "author": { ... },
      "images": ["string"],
      "likes": 23,
      "reposts": 5,
      "commentsCount": 5,
      "liked": true,
      "createdAt": "2026-05-07T10:30:00"
    }
  ],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

### 5. 获取微博详情

**GET** `/api/post/:id`

**响应数据**:
```json
{
  "id": 1,
  "content": "string",
  "authorId": 1,
  "author": { ... },
  "images": ["string"],
  "likes": 23,
  "reposts": 5,
  "commentsCount": 5,
  "liked": true,
  "views": 128,
  "createdAt": "2026-05-07T10:30:00"
}
```

### 6. 创建微博

**POST** `/api/post`

**请求头**: `Authorization: Bearer <token>`

**请求参数**:
```json
{
  "content": "string",
  "images?": ["string"]
}
```

**响应数据**: 创建的微博对象

### 7. 更新微博

**PUT** `/api/post/:id`

**请求头**: `Authorization: Bearer <token>`

**请求参数**:
```json
{
  "content": "string",
  "images?": ["string"]
}
```

**响应数据**: 更新后的微博对象

### 8. 删除微博

**DELETE** `/api/post/:id`

**请求头**: `Authorization: Bearer <token>`

**响应数据**:
```json
{
  "success": true
}
```

## 评论相关

### 9. 获取评论列表

**GET** `/api/comments`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| postId | number | 是 | 微博ID |
| page | number | 否 | 页码，默认 1 |
| pageSize | number | 否 | 每页数量，默认 10 |

**响应数据**:
```json
{
  "comments": [
    {
      "id": 1,
      "postId": 1,
      "content": "string",
      "authorId": 1,
      "author": { ... },
      "createdAt": "2026-05-07T10:30:00"
    }
  ],
  "total": 50
}
```

### 10. 创建评论

**POST** `/api/comment`

**请求头**: `Authorization: Bearer <token>`

**请求参数**:
```json
{
  "postId": 1,
  "content": "string"
}
```

**响应数据**: 创建的评论对象

## 互动相关

### 11. 点赞/取消点赞

**POST** `/api/post/:id/like`

**请求头**: `Authorization: Bearer <token>`

**响应数据**:
```json
{
  "liked": true,
  "likes": 24
}
```

## 文件上传

### 12. 上传单张图片

**POST** `/api/upload`

**请求头**: 
- `Authorization: Bearer <token>`
- `Content-Type: multipart/form-data`

**请求参数**:
| 参数 | 类型 | 说明 |
|-----|------|------|
| file | File | 图片文件 |

**响应数据**:
```json
{
  "url": "https://example.com/images/xxx.jpg"
}
```

### 13. 批量上传图片

**POST** `/api/upload/batch`

**请求头**: 
- `Authorization: Bearer <token>`
- `Content-Type: multipart/form-data`

**请求参数**:
| 参数 | 类型 | 说明 |
|-----|------|------|
| files | File[] | 图片文件数组 |

**响应数据**:
```json
{
  "urls": [
    "https://example.com/images/xxx1.jpg",
    "https://example.com/images/xxx2.jpg"
  ]
}
```

## 错误码说明

| 状态码 | 说明 |
|-------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或登录已过期 |
| 403 | 没有权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 数据模型

### User (用户)
```typescript
interface User {
  id: number
  username: string
  avatar: string
  bio: string
  followers: number
  following: number
}
```

### Post (微博)
```typescript
interface Post {
  id: number
  content: string
  authorId: number
  author: User
  images: string[]
  likes: number
  reposts: number
  commentsCount: number
  liked: boolean
  views: number
  createdAt: string
}
```

### Comment (评论)
```typescript
interface Comment {
  id: number
  postId: number
  content: string
  authorId: number
  author: User
  createdAt: string
}
```
