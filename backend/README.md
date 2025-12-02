# 学生社团信息管理系统 - 后端

基于 Spring Boot 3.2 的学生社团信息管理系统后端项目。

## 技术栈

- Spring Boot 3.2.0
- Spring Security 6
- Spring Data JPA
- MySQL 8.0
- JWT (jjwt 0.12.3)
- Swagger / OpenAPI 3.0

## 项目结构

```
backend/
├── src/main/java/com/community/
│   ├── CommunityManagerApplication.java  # 启动类
│   ├── config/                           # 配置类
│   │   ├── SecurityConfig.java           # 安全配置
│   │   ├── WebConfig.java                # Web配置
│   │   └── DataInitializer.java          # 数据初始化
│   ├── controller/                       # 控制器
│   │   ├── AuthController.java           # 认证控制器
│   │   ├── UserController.java           # 用户控制器
│   │   ├── ClubController.java           # 社团控制器
│   │   ├── ActivityController.java       # 活动控制器
│   │   ├── AnnouncementController.java   # 公告控制器
│   │   ├── NotificationController.java   # 通知控制器
│   │   ├── StatisticsController.java     # 统计控制器
│   │   └── FileController.java           # 文件控制器
│   ├── dto/                              # 数据传输对象
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── AuthResponse.java
│   │   ├── ClubDTO.java
│   │   ├── ActivityDTO.java
│   │   └── ...
│   ├── entity/                           # 实体类
│   │   ├── User.java
│   │   ├── Club.java
│   │   ├── ClubMember.java
│   │   ├── Activity.java
│   │   ├── ActivityRegistration.java
│   │   ├── ActivityFeedback.java
│   │   ├── Announcement.java
│   │   └── Notification.java
│   ├── exception/                        # 异常处理
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── repository/                       # 数据仓库
│   │   ├── UserRepository.java
│   │   ├── ClubRepository.java
│   │   └── ...
│   ├── security/                         # 安全组件
│   │   ├── UserPrincipal.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── JwtTokenProvider.java
│   │   └── JwtAuthenticationFilter.java
│   └── service/                          # 业务服务
│       ├── AuthService.java
│       ├── UserService.java
│       ├── ClubService.java
│       ├── ActivityService.java
│       ├── AnnouncementService.java
│       ├── NotificationService.java
│       ├── StatisticsService.java
│       └── FileUploadService.java
├── src/main/resources/
│   └── application.yml                   # 应用配置
└── pom.xml                               # Maven配置
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 数据库配置

1. 创建数据库:
```sql
CREATE DATABASE community_manager 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

2. 修改 `application.yml` 中的数据库连接信息:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/community_manager
    username: your_username
    password: your_password
```

### 启动服务

```bash
# 开发模式
mvn spring-boot:run

# 或者先编译再运行
mvn clean package -DskipTests
java -jar target/community-manager-1.0.0.jar
```

服务将在 http://localhost:8080 启动

### API文档

访问 http://localhost:8080/api/swagger-ui.html

## API接口

### 认证模块 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/login` | 用户登录 |
| POST | `/register` | 用户注册 |
| GET | `/me` | 获取当前用户 |
| POST | `/refresh` | 刷新Token |

### 用户模块 `/api/users`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/profile` | 获取个人资料 |
| PUT | `/profile` | 更新个人资料 |
| PUT | `/password` | 修改密码 |
| POST | `/avatar` | 上传头像 |

### 社团模块 `/api/clubs`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 获取社团列表 |
| GET | `/{id}` | 获取社团详情 |
| POST | `/` | 创建社团 |
| PUT | `/{id}` | 更新社团 |
| POST | `/{id}/join` | 申请加入 |
| DELETE | `/{id}/leave` | 退出社团 |
| GET | `/{id}/members` | 获取成员列表 |
| GET | `/{id}/activities` | 获取社团活动 |
| GET | `/{id}/announcements` | 获取社团公告 |

### 活动模块 `/api/activities`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 获取活动列表 |
| GET | `/{id}` | 获取活动详情 |
| POST | `/` | 创建活动 |
| PUT | `/{id}` | 更新活动 |
| DELETE | `/{id}` | 删除活动 |
| POST | `/{id}/register` | 报名活动 |
| DELETE | `/{id}/cancel` | 取消报名 |
| POST | `/{id}/feedback` | 提交评价 |
| GET | `/{id}/participants` | 获取参与者 |
| GET | `/{id}/feedbacks` | 获取评价 |

### 通知模块 `/api/notifications`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 获取通知列表 |
| PUT | `/{id}/read` | 标记已读 |
| PUT | `/read-all` | 全部已读 |
| DELETE | `/{id}` | 删除通知 |

## 配置说明

### JWT配置

```yaml
app:
  jwt:
    secret: your-secret-key  # 至少256位
    expiration: 604800000    # 7天（毫秒）
```

### 文件上传配置

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

app:
  upload:
    path: ./uploads
```

## 安全说明

- 使用 JWT 进行无状态认证
- 密码使用 BCrypt 加密存储
- 支持基于角色的访问控制 (RBAC)
- 所有敏感接口需要认证

## 开发规范

### 分层架构

1. **Controller层**: 处理HTTP请求，参数校验
2. **Service层**: 业务逻辑处理
3. **Repository层**: 数据访问
4. **Entity层**: 数据库实体映射

### 异常处理

- 使用 `BusinessException` 处理业务异常
- 全局异常处理器统一返回格式

### 响应格式

```json
{
  "success": true,
  "message": "操作成功",
  "data": { }
}
```
