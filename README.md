# 学生社团信息管理系统

基于 Vue 3 + Spring Boot 的学生社团信息整理系统，用于解决高校社团信息分散、管理效率低、数据统计难等实际问题。

## 项目概述

本系统旨在为高校学生社团提供一个综合性的信息管理平台，实现社团信息的统一管理、活动的高效组织以及数据的可视化统计。

## 主要功能

### 用户角色

1. **普通成员**
   - 浏览社团和活动信息
   - 申请加入社团
   - 报名参加活动
   - 查看通知和公告
   - 评价参与过的活动
   - 管理个人资料

2. **社团负责人**
   - 拥有普通成员所有权限
   - 管理社团基本信息
   - 审批成员加入申请
   - 创建和管理活动
   - 发布社团公告
   - 查看数据统计

### 核心模块

1. **社团管理**
   - 社团列表展示与搜索
   - 社团详情页面
   - 社团创建与信息维护
   - 成员管理与角色分配

2. **活动管理**
   - 活动发布与编辑
   - 活动报名与取消
   - 活动评价与反馈
   - 活动历史记录

3. **成员管理**
   - 入社申请审批
   - 成员角色管理
   - 成员列表查看

4. **数据统计**
   - 成员增长趋势
   - 活动参与统计
   - 评分分布分析
   - 社团活跃度排行

## 技术栈

### 后端

- **框架**: Spring Boot 3.2.0
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0 + Spring Data JPA
- **文档**: Swagger / OpenAPI 3.0
- **构建**: Maven

### 前端

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **UI组件**: Element Plus
- **HTTP客户端**: Axios

## 项目结构

```
CommunityManager/
├── backend/                      # 后端项目
│   ├── src/main/java/com/community/manager/
│   │   ├── config/              # 配置类
│   │   ├── controller/          # 控制器
│   │   ├── dto/                 # 数据传输对象
│   │   ├── entity/              # 实体类
│   │   ├── exception/           # 异常处理
│   │   ├── repository/          # 数据仓库
│   │   ├── security/            # 安全配置
│   │   └── service/             # 业务服务
│   └── src/main/resources/
│       └── application.yml      # 应用配置
├── frontend/                     # 前端项目
│   ├── src/
│   │   ├── api/                 # API接口
│   │   ├── assets/              # 静态资源
│   │   ├── components/          # 公共组件
│   │   ├── layouts/             # 布局组件
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # 状态管理
│   │   ├── utils/               # 工具函数
│   │   └── views/               # 页面组件
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 数据库配置

1. 创建数据库:
```sql
CREATE DATABASE community_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

API文档访问: http://localhost:8080/api/swagger-ui.html

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器将在 http://localhost:5173 启动

### 生产环境构建

**后端打包:**
```bash
cd backend
mvn clean package -DskipTests
java -jar target/community-manager-1.0.0.jar
```

**前端打包:**
```bash
cd frontend
npm run build
```

构建产物在 `frontend/dist` 目录

## 默认账户

系统启动时会自动创建测试账户:

- **管理员**: admin / admin123
- **测试用户**: user1 / user123

## API接口

### 认证接口
- POST `/api/auth/login` - 用户登录
- POST `/api/auth/register` - 用户注册
- GET `/api/auth/me` - 获取当前用户

### 社团接口
- GET `/api/clubs` - 获取社团列表
- GET `/api/clubs/{id}` - 获取社团详情
- POST `/api/clubs` - 创建社团
- PUT `/api/clubs/{id}` - 更新社团
- POST `/api/clubs/{id}/join` - 申请加入
- GET `/api/clubs/{id}/members` - 获取成员列表

### 活动接口
- GET `/api/activities` - 获取活动列表
- GET `/api/activities/{id}` - 获取活动详情
- POST `/api/activities` - 创建活动
- POST `/api/activities/{id}/register` - 报名活动
- POST `/api/activities/{id}/feedback` - 提交评价

### 通知接口
- GET `/api/notifications` - 获取通知列表
- PUT `/api/notifications/{id}/read` - 标记已读

## 许可证

MIT License

## 作者

学生社团信息管理系统开发团队
