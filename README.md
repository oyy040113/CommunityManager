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
- **数据库**: SQLite + Spring Data JPA（轻量级，无需额外数据库服务）
- **文档**: Swagger / OpenAPI 3.0
- **构建**: Maven
- **运行时**: Eclipse Temurin JDK 21

### 前端

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **UI组件**: Element Plus
- **HTTP客户端**: Axios

### 部署

- **容器化**: Docker + Docker Compose
- **反向代理**: Nginx 1.27
- **镜像仓库**: 阿里云容器镜像服务

## 项目结构

```
CommunityManager/
├── backend/                      # 后端项目
│   ├── Dockerfile               # 后端多阶段构建镜像
│   ├── entrypoint.sh            # 容器启动脚本（处理卷权限）
│   ├── src/main/java/com/community/
│   │   ├── config/              # 配置类
│   │   ├── controller/          # 控制器
│   │   ├── dto/                 # 数据传输对象
│   │   ├── entity/              # 实体类
│   │   ├── exception/           # 异常处理
│   │   ├── repository/          # 数据仓库
│   │   ├── security/            # 安全配置
│   │   └── service/             # 业务服务
│   └── src/main/resources/
│       └── application.yml      # 应用配置（支持环境变量注入）
├── frontend/                     # 前端项目
│   ├── Dockerfile               # 前端构建镜像
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
├── nginx/                        # Nginx 反向代理
│   ├── Dockerfile               # Nginx 镜像（含前端构建产物）
│   └── nginx.conf               # Nginx 配置
├── data/                         # SQLite 数据库文件（挂载卷）
├── uploads/                      # 用户上传文件（挂载卷）
├── docker-compose.yml            # Docker Compose 编排文件
├── .env.example                  # 环境变量模板
└── README.md
```

## 快速开始

### 方式一：Docker Compose 部署（推荐）

这是推荐的生产环境部署方式，一条命令即可启动全部服务。

#### 环境要求

- Docker 20.10+
- Docker Compose v2+

#### 部署步骤

**1. 克隆项目并配置环境变量**

```bash
git clone <仓库地址>
cd CommunityManager
cp .env.example .env
```

**2. 编辑 `.env` 文件**

```bash
vi .env
```

重点修改以下配置：
```dotenv
# 生产环境务必修改为随机长字符串
JWT_SECRET=your_random_secret_at_least_256_bits

# 数据库路径（容器内路径，通过 volume 持久化到宿主机 ./data）
DB_URL=jdbc:sqlite:/data/community.db
```

**3. 启动服务**

```bash
docker compose up -d
```

服务启动后访问 http://your-server-ip 即可使用。

**4. 查看日志**

```bash
# 查看所有服务日志
docker compose logs -f

# 仅查看后端日志
docker compose logs -f backend
```

**5. 更新部署**

```bash
docker compose pull   # 拉取最新镜像
docker compose up -d  # 重新启动
```

#### Docker Compose 部署架构

```
┌──────────────────────────────────────────────────┐
│                   Host Machine                   │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │           Docker Compose Network           │  │
│  │                                            │  │
│  │  ┌──────────┐        ┌──────────────────┐  │  │
│  │  │  Nginx   │──API──▶│    Backend       │  │  │
│  │  │ :80      │        │    :8080         │  │  │
│  │  │ (前端静态 │        │  (Spring Boot)   │  │  │
│  │  │  + 反向   │        │                  │  │  │
│  │  │  代理)    │        └────────┬─────────┘  │  │
│  │  └──────────┘                 │             │  │
│  └───────────────────────────────┼─────────────┘  │
│                                  │                │
│              ┌───────────────────┼──────────┐     │
│              │   Volumes         │          │     │
│              │  ./data ◄─── SQLite DB       │     │
│              │  ./uploads ◄─ 上传文件        │     │
│              └──────────────────────────────┘     │
└──────────────────────────────────────────────────┘
```

#### 这种部署方式的优势

1. **一键部署，极致简单**
   - 只需 `docker compose up -d` 即可启动前后端 + 反向代理全部服务，无需手动安装 JDK、Node.js、MySQL 等依赖环境。

2. **无外部数据库依赖**
   - 采用 SQLite 替代 MySQL，数据库以文件形式存储在 `./data` 目录，无需部署维护额外的数据库服务，降低运维复杂度和资源消耗。

3. **环境一致性**
   - Docker 容器确保开发、测试、生产环境完全一致，杜绝"在我机器上能跑"的问题。

4. **数据持久化与安全**
   - 通过 Docker Volume 将数据库和上传文件挂载到宿主机，容器重建不丢失数据。
   - 后端容器使用非 root 用户运行，`entrypoint.sh` 在启动时自动修复卷文件权限，兼顾安全性与可用性。

5. **灵活的配置管理**
   - 所有配置通过 `.env` 文件集中管理，支持环境变量注入，不同环境只需切换 `.env` 文件即可。

6. **镜像预构建，部署秒级完成**
   - 镜像已推送至阿里云容器镜像仓库（`registry.cn-hangzhou.aliyuncs.com/fjrcn/club_manage_*`），生产服务器无需安装编译工具链，直接拉取镜像即可运行。

7. **前后端分离，独立扩展**
   - Nginx 负责静态资源服务与 API 反向代理，前后端完全解耦，可各自独立扩缩容和更新。

---

### 方式二：本地开发环境

适用于开发和调试场景。

#### 环境要求

- JDK 17+
- Node.js 18+
- Maven 3.8+

#### 后端启动

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

### 生产环境构建（手动）

> 推荐使用 Docker Compose 部署，以下仅供参考。

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

## 更新日志

### v0.1.1

- **Docker 容器化部署**：新增 `Dockerfile`（前端 / 后端 / Nginx）和 `docker-compose.yml`，支持一键 Docker Compose 部署。
- **数据库迁移至 SQLite**：从 MySQL 切换到 SQLite，消除外部数据库依赖，简化部署流程。
- **环境变量配置**：新增 `.env.example`，所有运行时配置通过环境变量注入，适配不同部署环境。
- **Nginx 反向代理**：新增 Nginx 配置，提供前端静态资源服务、SPA 路由回退、API 反向代理、Gzip 压缩和静态资源长缓存。
- **容器安全加固**：后端容器以非 root 用户运行，`entrypoint.sh` 启动脚本自动处理挂载卷的文件权限。
- **Bug 修复**：修复了多个已知问题，提升系统稳定性。

## 作者

学生社团信息管理系统开发团队
