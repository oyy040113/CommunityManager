# 学生社团信息管理系统 - 前端

基于 Vue 3 + Element Plus 的学生社团信息管理系统前端项目。

## 技术栈

- Vue 3.4 (Composition API)
- Vite 5.0
- Element Plus 2.4
- Pinia 2.1
- Vue Router 4.2
- Axios 1.6

## 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口模块
│   │   ├── user.js        # 用户相关
│   │   ├── club.js        # 社团相关
│   │   ├── activity.js    # 活动相关
│   │   ├── notification.js # 通知相关
│   │   └── statistics.js  # 统计相关
│   ├── assets/            # 资源文件
│   ├── components/        # 公共组件
│   │   ├── ClubCard.vue   # 社团卡片
│   │   ├── ActivityCard.vue # 活动卡片
│   │   ├── StatCard.vue   # 统计卡片
│   │   ├── PageHeader.vue # 页面头部
│   │   └── EmptyState.vue # 空状态
│   ├── layouts/           # 布局组件
│   │   └── MainLayout.vue # 主布局
│   ├── router/            # 路由配置
│   │   └── index.js
│   ├── stores/            # 状态管理
│   │   └── user.js        # 用户状态
│   ├── utils/             # 工具函数
│   │   ├── request.js     # Axios封装
│   │   ├── format.js      # 格式化工具
│   │   ├── helpers.js     # 通用工具
│   │   ├── constants.js   # 常量定义
│   │   └── validators.js  # 验证规则
│   ├── views/             # 页面组件
│   │   ├── Home.vue       # 首页
│   │   ├── auth/          # 认证页面
│   │   ├── club/          # 社团页面
│   │   ├── activity/      # 活动页面
│   │   ├── profile/       # 个人中心
│   │   └── admin/         # 管理页面
│   ├── App.vue            # 根组件
│   ├── main.js            # 入口文件
│   └── main.css           # 全局样式
├── .env.development       # 开发环境配置
├── .env.production        # 生产环境配置
├── index.html             # HTML模板
├── package.json           # 依赖配置
└── vite.config.js         # Vite配置
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:5173

### 生产构建

```bash
npm run build
```

构建产物在 `dist` 目录

### 预览构建结果

```bash
npm run preview
```

## 页面路由

| 路径 | 页面 | 权限 |
|------|------|------|
| `/` | 首页 | 公开 |
| `/login` | 登录 | 游客 |
| `/register` | 注册 | 游客 |
| `/clubs` | 社团列表 | 公开 |
| `/clubs/:id` | 社团详情 | 公开 |
| `/activities` | 活动列表 | 公开 |
| `/activities/:id` | 活动详情 | 公开 |
| `/my-clubs` | 我的社团 | 登录 |
| `/my-activities` | 我的活动 | 登录 |
| `/profile` | 个人中心 | 登录 |
| `/notifications` | 通知中心 | 登录 |
| `/admin/club/:id` | 社团管理 | 负责人 |
| `/admin/activities` | 活动管理 | 负责人 |
| `/admin/statistics` | 数据统计 | 负责人 |

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `VITE_APP_TITLE` | 应用标题 | 学生社团信息管理系统 |
| `VITE_API_BASE_URL` | API基础地址 | http://localhost:8080/api |
| `VITE_APP_ENV` | 环境标识 | development |

## 开发规范

### 组件命名

- 使用 PascalCase 命名组件文件
- 页面组件放在 `views` 目录
- 公共组件放在 `components` 目录

### API调用

- 所有API请求统一在 `api` 目录定义
- 使用 `request.js` 封装的 axios 实例
- 请求错误统一处理

### 状态管理

- 使用 Pinia 进行状态管理
- Store 文件放在 `stores` 目录
- 命名使用 `use[Name]Store` 格式
