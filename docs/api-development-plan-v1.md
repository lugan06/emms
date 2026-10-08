# 分批接口开发清单

## 1. 开发规则

- 每批只开发清单内的接口。
- 每批开发完成后，执行编译、单元测试和接口验收。
- 验收通过后停止，不自动进入下一批。
- 等待明确指令后再开始下一批。
- 已完成并已提交：`POST /api/admin/login`。
- 第一批、第二批、第三批已经完成验收并提交。
- 第四批已经完成开发、验收并提交。
- 第五批已经完成开发、验收并提交。
- 第六批已经完成开发、验收并提交。

## 2. 当前进度

| 批次 | 范围 | 状态 |
| --- | --- | --- |
| 第一批 | 管理员基础与基础配置 | 已完成并提交 |
| 第二批 | 公共站点信息与文件资源 | 已完成并提交 |
| 第三批 | 展会管理 | 已完成并提交 |
| 第四批 | 公共展会接口 | 已完成并提交 |
| 第五批 | 栏目管理 | 已完成并提交 |
| 第六批 | 栏目内容管理 | 已完成并提交 |
| 第七批 | 公共内容与首页 | 待开发 |
| 第八批 | 在线留言 | 待开发 |
| 第九批 | 操作日志查询与统一审计 | 待开发 |

## 3. 第一批：管理员基础与基础配置

### 3.1 管理员接口

- `GET /api/admin/me`
  - 获取当前登录管理员信息。
  - 返回管理员 ID、用户名、昵称、头像和角色。
  - 未登录返回 `401`。
- `PUT /api/admin/password`
  - 请求原密码和新密码。
  - 使用 BCrypt 校验原密码。
  - 新密码重新进行 BCrypt 加密。
  - 修改成功后，前端清除旧 token 并重新登录。
  - 原密码错误返回业务错误。
  - 校验新密码长度。

### 3.2 站点配置接口

- `GET /api/admin/site`
  - 获取默认站点配置。
- `PUT /api/admin/site`
  - 更新默认站点配置。
  - 不开放新增和删除。
  - 仅 `SUPER_ADMIN` 可修改。

### 3.3 公司信息接口

- `GET /api/admin/company`
  - 获取默认公司信息。
- `PUT /api/admin/company`
  - 更新默认公司信息。
  - 不开放新增和删除。
  - 仅 `SUPER_ADMIN` 可修改。

### 3.4 验收重点

- JWT 用户身份能正确解析。
- `SUPER_ADMIN` 可以访问和修改配置。
- `EDITOR` 访问配置修改接口返回 `403`。
- 站点和公司配置按照 `default` 单例记录读取。
- 参数校验和统一错误响应正确。
- OpenAPI 文档包含所有第一批接口。

## 4. 第二批：公共站点信息与文件资源

### 4.1 公共站点接口

- `GET /api/public/site`
- `GET /api/public/company`

公共端只返回必要的公开字段，不返回内部配置、统计代码、后台操作人等敏感字段。

### 4.2 文件资源接口

- `POST /api/admin/files`
- `GET /api/admin/files`
- `DELETE /api/admin/files/{id}`

### 4.3 验收重点

- 文件使用 `multipart/form-data`。
- 校验文件类型和大小。
- 拒绝可执行文件。
- 生成安全的随机存储文件名。
- 文件元数据写入 `file_asset`。
- 删除使用逻辑删除。
- 公共端配置字段完成脱敏。

## 5. 第三批：展会管理

### 5.1 管理端接口

- `GET /api/admin/exhibitions`
- `GET /api/admin/exhibitions/{id}`
- `POST /api/admin/exhibitions`
- `PUT /api/admin/exhibitions/{id}`
- `DELETE /api/admin/exhibitions/{id}`
- `POST /api/admin/exhibitions/{id}/publish`
- `POST /api/admin/exhibitions/{id}/offline`
- `POST /api/admin/exhibitions/{id}/set-current`

### 5.2 业务规则

- 新增和编辑时，开始时间必须早于结束时间。
- 只有 `DRAFT` 展会可以发布。
- 只有 `PUBLISHED` 展会可以下架。
- 当前展会只能设置为 `PUBLISHED` 状态的展会。
- 删除使用逻辑删除，即 `deleted = 1`。
- 发布、下架、设置当前展会时同步写入操作日志。

### 5.3 验收重点

- 支持关键字、年份、状态和分页查询。
- 开始时间必须早于结束时间。
- 只有草稿可以发布。
- 只有已发布展会可以下架。
- 当前展会只能设置为已发布展会。
- 删除使用逻辑删除。
- 发布、下架、设置当前展会状态流转正确。

## 6. 第四批：公共展会接口

- `GET /api/public/exhibitions`
- `GET /api/public/exhibitions/{id}`

### 验收重点

- 不返回草稿展会。
- 不返回已下架展会。
- 不返回逻辑删除数据。
- 只返回已到发布时间的数据。
- 详情不存在或不可公开时返回 `404` 或业务错误。
- 支持当前展会关联查询。

### 实现说明

- 公共接口无需 JWT 鉴权。
- 列表支持关键字、举办年份和分页查询。
- 公共查询固定过滤 `status = PUBLISHED`。
- 公共查询固定过滤 `published_at IS NULL OR published_at <= 当前时间`。
- MyBatis-Plus 逻辑删除机制自动过滤 `deleted = 1` 的记录。
- 公共响应使用独立的 `PublicExhibitionVO`，不返回创建人、更新人、扩展管理数据等内部字段。
- 返回 `isCurrent` 字段，通过 `site_config.current_exhibition_id` 判断当前展会。
- 不涉及数据库表结构变更，不需要执行 SQL。

### 第四批验收结果

- 后端测试：`mvn test`，共 52 个测试，0 个失败，0 个错误。
- 公共展会列表和详情 Controller 测试通过。
- 公共展会 Service 测试通过，覆盖当前展会标识和不可公开展会校验。
- `/v3/api-docs` 返回 OpenAPI `3.0.1`。
- OpenAPI 已包含 `GET /api/public/exhibitions` 和 `GET /api/public/exhibitions/{id}`。
- 已完成提交，等待下一批开发指令。

## 7. 第五批：栏目管理

### 7.1 管理端接口

- `GET /api/admin/categories/tree`
- `POST /api/admin/categories`
- `PUT /api/admin/categories/{id}`
- `DELETE /api/admin/categories/{id}`
- `POST /api/admin/categories/reorder`

### 7.2 验收重点

- 支持多级栏目树。
- `parent_id = 0` 表示顶级栏目。
- 栏目编码唯一。
- 禁止移动到自身或子孙节点。
- 有子栏目或内容时禁止删除。
- 支持启用、禁用、导航显示和排序。
- `LINK` 类型栏目允许没有正文。

### 7.3 实现说明

- 栏目接口统一使用 JWT 鉴权。
- 栏目树按 `parent_id` 构建，顶级栏目使用 `parent_id = 0`。
- 支持新增、编辑、逻辑删除和批量调整父栏目及排序值。
- 栏目编码在未删除数据中保持唯一。
- 新增和编辑时校验父栏目存在，禁止移动到自身或子孙节点。
- 删除前检查未删除子栏目和未删除栏目内容，存在关联数据时拒绝删除。
- 支持 `PAGE`、`ARTICLE`、`PRODUCT`、`CASE`、`LINK`、`DIRECTORY` 模型。
- 数据库约束异常通过统一错误响应返回，不暴露数据库内部信息。
- 本批次未修改数据库表结构，不需要执行 SQL。

### 7.4 第五批验收结果

- 后端测试：`mvn test`，共 62 个测试，0 个失败，0 个错误。
- Service 测试覆盖栏目树、编码唯一、循环移动、删除保护和排序更新。
- Controller 测试覆盖 JWT 鉴权、栏目树查询和新增栏目。
- `/v3/api-docs` 返回 OpenAPI `3.0.1`。
- OpenAPI 已包含 5 个栏目管理接口。
- 已完成提交，等待第六批开发指令。

## 8. 第六批：栏目内容管理

### 8.1 管理端接口

- `GET /api/admin/contents`
- `GET /api/admin/contents/{id}`
- `POST /api/admin/contents`
- `PUT /api/admin/contents/{id}`
- `DELETE /api/admin/contents/{id}`
- `POST /api/admin/contents/{id}/publish`
- `POST /api/admin/contents/{id}/offline`

### 8.2 验收重点

- 支持栏目、展会、标题和状态筛选。
- 支持置顶、推荐和排序。
- 只有草稿可以发布。
- 只有已发布内容可以下架。
- 富文本内容进行基础安全过滤。
- 公共端后续只能读取已发布内容。

### 8.3 实现说明

- 内容接口统一使用 JWT 鉴权。
- 列表支持栏目、展会、标题关键字、状态、置顶和推荐筛选，并按置顶、推荐和排序值返回。
- 新增内容默认状态为 `DRAFT`，只有草稿可以发布，只有已发布内容可以下架。
- 内容删除使用逻辑删除，公共端后续查询不会读取已删除数据。
- 新增和编辑时校验关联栏目及展会存在，`slug` 在未删除和已删除数据中均不允许重复。
- 使用 Jsoup 白名单清洗富文本正文，过滤脚本和不安全属性。
- 内容新增、编辑、删除、发布和下架只记录内容 ID，不记录正文或其他隐私参数。
- 扩展操作日志服务支持按业务模块记录，兼容已有展会日志。
- 本批次未修改数据库表结构，不需要执行 SQL。

### 8.4 第六批验收结果

- 后端测试：`mvn test`，共 79 个测试，0 个失败，0 个错误。
- Service 测试覆盖查询筛选、正文安全过滤、关联校验、状态流转、逻辑删除和日志记录。
- Controller 测试覆盖 JWT 鉴权、分页查询和新增内容。
- `/v3/api-docs` 返回 OpenAPI `3.0.1`。
- OpenAPI 已包含 7 个栏目内容管理接口。
- 已完成提交，等待第七批开发指令。

## 9. 第七批：公共内容与首页

### 9.1 公共内容接口

- `GET /api/public/categories/tree`
- `GET /api/public/categories/{code}/contents`
- `GET /api/public/contents/{id}`

### 9.2 公共首页接口

- `GET /api/public/home`

首页聚合内容：

- 站点配置。
- 公司信息。
- 当前展会。
- 展会简介。
- 同期活动。
- 参展范围。
- 展会动态。
- 行业新闻。
- 展商新闻。

### 9.3 验收重点

- 公共端只返回启用、已发布、未删除数据。
- 正确过滤发布时间。
- 当前展会通过 `site_config.current_exhibition_id` 获取。
- 首页聚合数据结构稳定。
- 不实现动态首页模块配置。

## 10. 第八批：在线留言

### 10.1 公共端接口

- `POST /api/public/guestbook`

### 10.2 管理端接口

- `GET /api/admin/guestbooks`
- `POST /api/admin/guestbooks/{id}/read`
- `POST /api/admin/guestbooks/{id}/reply`
- `POST /api/admin/guestbooks/{id}/close`

### 10.3 验收重点

- 公共端支持留言提交。
- 管理端支持分页和状态筛选。
- 状态包括 `UNREAD`、`READ`、`REPLIED`、`CLOSED`。
- 回复保存回复人和回复时间。
- 手机号、邮箱和 IP 按最小范围展示。
- 增加字段长度校验和基础防刷限制。

## 11. 第九批：操作日志查询与统一审计

### 11.1 管理端接口

- `GET /api/admin/logs`

### 11.2 日志覆盖范围

- 登录成功和失败。
- 修改密码。
- 修改站点配置。
- 修改公司信息。
- 展会新增、编辑、删除、发布、下架。
- 切换当前展会。
- 内容新增、编辑、删除、发布、下架。
- 文件上传和删除。
- 留言回复和关闭。

### 11.3 验收重点

- 支持操作人、模块、操作类型、结果和时间范围筛选。
- 支持分页。
- 密码不写入日志。
- 手机号、邮箱和留言内容脱敏。
- 失败操作记录错误原因。
- 日志接口仅 `SUPER_ADMIN` 可访问。

## 12. 每批统一交付内容

每批接口开发都应同步交付：

- Entity。
- DTO。
- VO。
- Mapper。
- Service。
- Controller。
- 参数校验。
- 权限校验。
- OpenAPI 注解。
- 单元测试。
- Controller 接口测试。
- 编译和测试结果。

## 13. 范围约束

- 第一批只开发管理员基础、站点配置和公司信息接口。
- 不在第一批实现文件、展会、栏目、内容和留言。
- `POST /api/admin/logout` 暂不实现服务端接口，继续由前端清除 JWT。
- 第一版不实现 Refresh Token、Token 黑名单、复杂 RBAC、审批流、预约报名和支付。
- 每批验收通过后停止，等待下一批开发指令。
