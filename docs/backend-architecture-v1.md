# EEMS 第一版后端框架设计

## 1. 目标与技术基线

后端负责为管理端和公共浏览端提供 REST API，覆盖管理员登录、站点配置、公司信息、展会、栏目、内容、首页聚合数据、文件上传、在线留言和基础操作日志。

本文件按第一版 MVP 范围编写。首页动态模块编排、复杂权限和多站点能力只保留扩展边界，不进入第一版开发。

技术基线固定为：

| 项目 | 选择 |
| --- | --- |
| 语言 | Java 17 |
| Web 框架 | Spring Boot 3.x |
| 安全 | Spring Security 6.x + JWT |
| 持久层 | MyBatis-Plus 3.5.x |
| 数据库 | MySQL 8.x |
| 构建 | Maven 3.9.x |
| 接口风格 | RESTful JSON API |
| 时间类型 | `LocalDateTime` |
| 密码算法 | BCrypt |

Spring Boot 3.x 统一使用 `jakarta.*` 命名空间；不使用 `WebSecurityConfigurerAdapter`，采用 `SecurityFilterChain` 配置安全规则。

## 2. 后端职责边界

### 管理端 API

管理端负责需要身份认证的操作：

- 管理员登录、当前用户信息和密码修改。
- 站点配置和公司信息维护。
- 展会新增、编辑、发布、下架、删除、设置当前展会和首页展示开关。
- 栏目树维护和栏目启停、排序。
- 栏目内容维护和发布管理。
- 内容推荐、置顶和排序。
- 文件上传和资源查询。
- 在线留言查看、标记、回复和关闭。
- 操作日志查询。

### 公共端 API

公共端只提供读取和留言提交：

- 读取站点配置和公司信息。
- 读取当前展会、展会详情和已发布展会列表。
- 读取导航栏目树。
- 读取栏目内容列表和详情。
- 读取由当前展会和推荐内容组成的首页聚合数据。
- 提交在线留言。

公共端不得复用管理端查询方法直接绕过发布、启用和逻辑删除条件。

## 3. 工程目录

建议后端工程位于 `backend/`，包名使用 `com.eems`：

```text
backend/
├─ pom.xml
├─ src/
│  ├─ main/
│  │  ├─ java/com/eems/
│  │  │  ├─ EemsApplication.java
│  │  │  ├─ common/
│  │  │  │  ├─ api/              # ApiResponse、PageResult、错误码
│  │  │  │  ├─ exception/        # 业务异常、全局异常处理
│  │  │  │  ├─ model/            # PageQuery、用户上下文
│  │  │  │  └─ validation/       # 通用校验器
│  │  │  ├─ config/              # Jackson、MyBatis-Plus、Web、文件配置
│  │  │  ├─ security/            # JWT、过滤器、用户详情、安全配置
│  │  │  ├─ auth/                # 登录和当前用户
│  │  │  ├─ system/              # 用户、站点、公司、文件、日志
│  │  │  ├─ exhibition/          # 展会
│  │  │  ├─ content/              # 栏目和栏目内容
│  │  │  ├─ publicapi/            # 公共端聚合接口，调用展会和内容服务
│  │  │  └─ guestbook/            # 在线留言
│  │  │
│  │  │  └─ 每个业务模块内部统一采用：
│  │  │     ├─ controller/        # 接口层
│  │  │     ├─ service/           # 业务接口和实现
│  │  │     ├─ mapper/            # Mapper 接口
│  │  │     ├─ entity/            # 数据库实体
│  │  │     ├─ dto/               # 请求 DTO
│  │  │     ├─ vo/                # 返回 VO
│  │  │     └─ enums/             # 模块枚举
│  │  └─ resources/
│  │     ├─ application.yml
│  │     ├─ application-dev.yml
│  │     ├─ application-prod.yml
│  │     ├─ mapper/               # 仅在注解和 Wrapper 不足时使用 XML
│  │     └─ db/migration/         # 如果采用 Flyway，存放版本脚本
│  └─ test/java/com/eems/
└─ README.md
```

业务模块采用“按领域组织”，而不是把所有 Controller、Service、Mapper 分别集中到全局目录。首页第一版只是公共端的聚合查询，由 `publicapi` 调用展会、内容、站点和公司服务，不单独建立首页配置业务模块。

## 4. Maven 依赖分组

`pom.xml` 使用 Spring Boot parent 或 dependency management 统一管理 Spring 版本；业务依赖只声明用途，不在多个模块重复指定 Spring 版本。

核心依赖：

```text
spring-boot-starter-web
spring-boot-starter-validation
spring-boot-starter-security
mybatis-plus-spring-boot3-starter
mysql-connector-j
jjwt-api
jjwt-impl
jjwt-jackson
springdoc-openapi-starter-webmvc-ui       # 可选，建议开发阶段启用
spring-boot-starter-test
spring-security-test
```

JWT 的 `jjwt-impl` 和 `jjwt-jackson` 设为 runtime 依赖，三件套必须使用同一版本。数据库连接、JWT 密钥、文件根目录和跨域来源全部从配置文件读取，不写死在 Java 代码中。

## 5. 分层职责

### Controller

- 接收 HTTP 请求并绑定 DTO。
- 执行 `jakarta.validation` 参数校验。
- 调用 Service，不直接访问 Mapper。
- 将 Service 返回的领域结果转换为 VO。
- 不在 Controller 中编写发布、权限、排序和状态流转规则。

### Service

- 负责业务规则、状态变更、事务和权限相关业务判断。
- 使用 `@Transactional` 包裹跨表写操作。
- 创建和更新时写入 `created_by`、`updated_by`。
- 发布、下架、删除、回复留言等动作使用明确的方法，不使用任意字段更新替代。

### Mapper

- 负责实体 CRUD、条件查询和分页。
- 简单查询优先使用 MyBatis-Plus Wrapper。
- 栏目树、首页聚合数据等跨表查询在 Mapper 层使用明确 SQL 或 XML。
- 公共端查询必须显式带上 `deleted = 0`、发布状态和启用条件。

### Entity、DTO、VO

- Entity 只对应数据库持久化模型，不直接作为接口入参和出参。
- DTO 区分新增、编辑、登录、密码修改、发布动作和分页查询。
- VO 只返回公共端或管理端需要的字段，避免直接暴露 `password_hash`、内部日志参数等敏感数据。

## 6. 数据库映射约定

### 公共字段

实体基类建议包含：

```text
id
createdAt
updatedAt
deleted
```

使用 MyBatis-Plus 的 `@TableLogic` 处理逻辑删除；`deleted` 统一使用 `0/1`。涉及操作者的实体在自身类中增加 `createdBy` 和 `updatedBy`，不强行塞进所有实体基类。

### 类型映射

| MySQL | Java |
| --- | --- |
| BIGINT UNSIGNED | Long |
| TINYINT | Integer 或 Boolean，项目内按字段统一 |
| SMALLINT | Integer |
| VARCHAR/TEXT/LONGTEXT | String |
| DATETIME | LocalDateTime |
| JSON | JsonNode 或 String，推荐使用 Jackson `JsonNode` |

数据库主键虽然是 `UNSIGNED`，Java 统一使用 `Long`，禁止使用 `int`。

### 单例配置

`site_config` 和 `company_profile` 通过 `config_code = 'default'`、`profile_code = 'default'` 保证单例。Service 提供 `getDefault` 和 `updateDefault`，不开放普通新增接口。

### 栏目树

`cms_category.parent_id` 为 0 表示顶级栏目。新增和移动栏目时必须校验：

- 不能把栏目移动到自身或自身子孙节点下。
- `code` 全局唯一。
- `LINK` 类型栏目可以没有内容，其他可发布类型需要按模型校验内容字段。
- 禁用父栏目时，公共端不显示其子树。

### 展会和内容关联

`site_config.current_exhibition_id` 指向当前主展会，`cms_content.exhibition_id` 指向内容所属展会。切换当前展会只更新站点配置，不覆盖历史展会或历史内容。

### 首页聚合

第一版不实现 `home_section` 和 `home_section_item` 的动态配置。首页由以下数据组合：

- 当前展会：通过 `site_config.current_exhibition_id` 获取。
- 推荐内容：按 `cms_content.is_recommend = 1`、`is_top` 和 `sort_order` 查询。
- 栏目内容：按栏目编码查询已发布内容，例如展会动态、行业新闻和展商新闻。

公共端的 `GET /api/public/home` 由 `PublicHomeService` 聚合上述数据，避免为固定首页增加额外配置表和管理页面。后续需要可视化调整首页模块时，再启用首页展示区和展示项模型。

## 7. API 规划

统一前缀：`/api`。

### 认证

```text
POST /api/auth/login
GET  /api/auth/me
PUT  /api/auth/password
POST /api/auth/logout       # 第一版由前端清除 Token，不做服务端撤销
```

登录请求：

```json
{
  "username": "admin",
  "password": "******"
}
```

登录返回 JWT access token、过期时间和用户基本信息。第一版只实现短期 Access Token，不实现 Refresh Token；需要长期登录时再新增刷新令牌存储和撤销策略。

### 公共端

```text
GET  /api/public/site
GET  /api/public/company
GET  /api/public/home
GET  /api/public/exhibitions
GET  /api/public/exhibitions/{id}
GET  /api/public/categories/tree
GET  /api/public/categories/{code}/contents
GET  /api/public/contents/{id}
POST /api/public/guestbook
```

公共端接口不接受客户端传入 `status`、`deleted` 等内部筛选条件；状态条件由 Service 固定追加。

### 管理端

```text
GET/PUT  /api/admin/site
GET/PUT  /api/admin/company

GET      /api/admin/exhibitions
GET      /api/admin/exhibitions/{id}
POST     /api/admin/exhibitions
PUT      /api/admin/exhibitions/{id}
DELETE   /api/admin/exhibitions/{id}
POST     /api/admin/exhibitions/{id}/publish
POST     /api/admin/exhibitions/{id}/offline
POST     /api/admin/exhibitions/{id}/set-current

GET      /api/admin/categories/tree
POST     /api/admin/categories
PUT      /api/admin/categories/{id}
DELETE   /api/admin/categories/{id}
POST     /api/admin/categories/reorder

GET      /api/admin/contents
GET      /api/admin/contents/{id}
POST     /api/admin/contents
PUT      /api/admin/contents/{id}
DELETE   /api/admin/contents/{id}
POST     /api/admin/contents/{id}/publish
POST     /api/admin/contents/{id}/offline

POST     /api/admin/files
GET      /api/admin/files
GET      /api/admin/guestbooks
POST     /api/admin/guestbooks/{id}/read
POST     /api/admin/guestbooks/{id}/reply
POST     /api/admin/guestbooks/{id}/close
GET      /api/admin/logs
```

删除接口执行逻辑删除；发布、下架、设置当前展会使用动作接口，以便校验状态并记录日志。

## 8. 统一响应和错误处理

### 成功响应

```json
{
  "code": "OK",
  "message": "success",
  "data": {}
}
```

分页响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "records": [],
    "total": 0,
    "page": 1,
    "pageSize": 20
  }
}
```

### 错误响应

```json
{
  "code": "EXHIBITION_NOT_FOUND",
  "message": "展会不存在",
  "data": null,
  "traceId": "..."
}
```

全局异常处理器统一处理：

- 参数校验异常：HTTP 400。
- 未登录：HTTP 401。
- 无权限：HTTP 403。
- 业务异常：根据错误类型返回 400 或 409。
- 资源不存在：HTTP 404。
- 未预期异常：HTTP 500，响应不暴露堆栈。

## 9. JWT 和 Spring Security

### 请求流程

```text
请求
  -> CORS
  -> JwtAuthenticationFilter
  -> 校验 Bearer Token、签名、过期时间
  -> 写入 SecurityContext
  -> Controller
  -> Service
```

安全规则：

```text
/api/auth/login       permitAll
/api/public/**        permitAll
/api/admin/**         authenticated
/api/admin/** 的写操作 需要对应角色
其他路径                默认拒绝
```

第一版角色策略：

```text
SUPER_ADMIN  所有管理功能
EDITOR       展会、栏目、内容、文件和留言；不能管理管理员、站点配置和公司信息
```

JWT 只携带用户 ID、用户名和角色编码，不放入密码、手机号等敏感字段。密钥从环境变量读取，生产环境不得提交到仓库。

## 10. 关键业务事务

### 发布展会

1. 查询展会并确认未删除。
2. 校验标题、封面、地点、开始时间和结束时间。
3. 校验 `end_at > start_at`。
4. 设置 `status = PUBLISHED` 和 `published_at`。
5. 写入 `operation_log`。

### 切换当前展会

1. 查询目标展会并确认状态为 `PUBLISHED`。
2. 在事务中更新 `site_config.current_exhibition_id`。
3. 记录站点配置变更日志。

### 删除栏目

1. 查询栏目是否存在。
2. 检查是否存在未删除子栏目。
3. 检查是否存在未删除内容。
4. 默认拒绝直接删除有子项或内容的栏目，先要求迁移或清理关联数据。
5. 通过逻辑删除完成删除并记录日志。

### 生成首页聚合数据

1. 查询站点默认配置和当前展会。
2. 查询当前展会的基础信息。
3. 按栏目和推荐、置顶、排序条件批量查询已发布内容。
4. 组装首页 VO，不返回草稿、已下架或逻辑删除数据。
5. 后续如需后台拖拽配置首页，再增加 `home_section` 和 `home_section_item` 的管理流程。

### 回复在线留言

1. 查询留言并确认未删除。
2. 保存回复内容、回复人和回复时间。
3. 将状态更新为 `REPLIED`。
4. 操作日志中的请求参数必须脱敏，不记录完整手机号、邮箱和留言隐私内容。

## 11. 配置文件规划

`application.yml` 只放公共默认配置；环境敏感值放在环境变量或未提交的 profile 配置中：

```yaml
spring:
  application:
    name: eems-backend
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jackson:
    time-zone: Asia/Shanghai
    date-format: yyyy-MM-dd HH:mm:ss

mybatis-plus:
  mapper-locations: classpath:/mapper/**/*.xml
  type-aliases-package: com.eems.**.entity
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

eems:
  jwt:
    secret: ${JWT_SECRET}
    access-token-ttl: ${JWT_ACCESS_TOKEN_TTL:7200}
  file:
    storage-type: ${FILE_STORAGE_TYPE:local}
    local-path: ${FILE_LOCAL_PATH:./data/uploads}
    public-url-prefix: ${FILE_PUBLIC_URL_PREFIX:/uploads}
```

生产环境必须提供强随机 JWT 密钥、数据库账号密码和文件存储配置；不允许使用示例值启动生产服务。

## 12. 文件上传设计

第一版使用本地文件目录即可，Service 层抽象 `FileStorageService`：

```text
LocalFileStorageService       开发和单机部署
ObjectStorageService          后续接入 OSS、S3 或 MinIO
```

上传流程：

1. 校验文件大小、MIME 类型和扩展名。
2. 生成不可预测的存储文件名，禁止使用原始文件名直接落盘。
3. 保存文件后写入 `file_asset`。
4. 返回文件访问地址和资源 ID。
5. 删除业务引用前检查是否仍被使用；第一版删除资源可以只做逻辑删除。

## 13. 操作日志和审计

通过 Spring AOP 或统一业务方法记录高价值操作：

- 登录成功和失败。
- 新增、编辑、删除业务数据。
- 发布、下架、设置当前展会和内容推荐调整。
- 修改站点信息和公司信息。
- 上传和删除文件。
- 处理在线留言。

日志不记录密码、JWT、完整手机号、完整邮箱及未经脱敏的敏感请求参数。

## 14. 测试和实现顺序

### 实现顺序

1. 初始化 Maven 工程、环境配置、数据库连接和统一响应。
2. 实现实体、Mapper-Plus、逻辑删除和分页基础能力。
3. 实现管理员登录、JWT 过滤器和权限规则。
4. 实现站点配置、公司信息和文件上传。
5. 实现展会 CRUD、发布、下架和当前展会切换。
6. 实现栏目树和栏目内容管理。
7. 实现公共端首页聚合、导航、列表和详情接口。
8. 实现在线留言和基础操作日志。
9. 补充 OpenAPI、集成测试和部署说明。

### 最小测试范围

- 登录成功、密码错误、禁用账号和过期 JWT。
- 管理端未登录返回 401，登录用户按角色返回 403。
- 展会开始和结束时间校验、发布/下架状态流转。
- 公共端不会返回草稿、已下架或逻辑删除的数据。
- 栏目不能移动到自身或子孙栏目下。
- 删除有子栏目或内容的栏目会被拒绝。
- 当前展会只能切换到已发布展会。
- 首页聚合不会返回草稿、已下架或逻辑删除的数据。
- 留言状态和回复人、回复时间正确保存。
- 上传类型、大小、文件名和访问地址校验。
- 关键操作写入日志且敏感字段脱敏。

## 15. 第一版明确不做的后端能力

- Refresh Token 和服务端 Token 黑名单。
- 复杂 RBAC、多部门和数据权限。
- 多站点、多租户和多语言。
- 展商账号、展位预订、支付和会议报名。
- 内容版本管理、审批流和定时发布。
- 首页展示区和展示项的动态配置。
- 分布式锁、消息队列和微服务拆分。

第一版采用单体 Spring Boot 应用即可。模块按领域隔离，保持 Controller、Service 和 Mapper 的边界，后续业务增长时再拆分基础设施或独立服务。
