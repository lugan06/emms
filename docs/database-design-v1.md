# EEMS 第一版数据库设计说明

## 1. 文档目的

本文档确定 EEMS 管理端与公共浏览端第一版的 MySQL 数据库设计，作为后续 Spring Boot、MyBatis-Plus、Vue 管理端和公共浏览端开发的统一数据基础。

本方案参考了以下公共端结构：

- 站点配置：标题、副标题、域名、Logo、SEO 信息、模板和页脚信息。
- 公司信息：公司名称、地址、联系人、电话、邮箱、微信图标和营业执照信息。
- 展会内容：展会简介、展会时间、地点、封面、展会描述和首页投放。
- 内容栏目：展会简介、平面图、参展范围、同期活动、展会相关、新闻资讯、联系我们、在线留言。
- 首页模块：当前展会、同期活动、参展范围、新闻资讯和展商动态等内容区。

第一版为单站点系统，但数据结构保留后续增加多届展会、首页更多展示位、内容模型和权限能力的空间。

## 2. 设计原则

1. 站点配置和公司信息按单站点单记录管理，避免重复配置。
2. 栏目和栏目内容分离：栏目负责导航和层级，内容负责文章、页面和其他正文。
3. 展会作为独立业务实体，不与普通文章混用，因为展会有开始时间、结束时间、发布状态和首页投放需求。
4. 内容和展会关联，支持新旧展会并存，避免每年覆盖历史数据。
5. 首页展示采用配置表控制，避免将首页内容写死在前端代码中。
6. 业务数据使用逻辑删除，公共端只读取启用、已发布且未删除的数据。

## 3. 数据库基础约定

- 数据库：MySQL 8.x。
- 存储引擎：InnoDB。
- 字符集：`utf8mb4`。
- 排序规则：`utf8mb4_0900_ai_ci`；如果部署环境不支持，使用 `utf8mb4_unicode_ci`。
- 主键：`BIGINT UNSIGNED AUTO_INCREMENT`。
- 时间：统一使用 `DATETIME`，业务解释为 Asia/Shanghai；接口统一使用明确的日期时间格式。
- 表名和字段名：小写下划线命名。
- 文件和图片只保存访问地址及文件元信息，实际文件由文件存储服务或服务器目录保存。
- 密码只保存 BCrypt 等不可逆哈希值，不保存明文密码。

所有业务表建议包含以下公共字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除，0 未删除，1 已删除 |

需要记录操作者的表额外增加 `created_by` 和 `updated_by`。

## 4. 表结构

### 4.1 `sys_user` 管理员账号

用于管理端登录、JWT 鉴权和记录业务操作人。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| username | VARCHAR(50) | 登录账号，唯一 |
| password_hash | VARCHAR(255) | 密码哈希 |
| nickname | VARCHAR(100) | 显示名称 |
| avatar_url | VARCHAR(500) | 头像地址，可为空 |
| role_code | VARCHAR(50) | 角色标识，第一版支持 `SUPER_ADMIN`、`EDITOR` |
| status | TINYINT | 1 启用，0 禁用 |
| last_login_at | DATETIME | 最近登录时间 |
| last_login_ip | VARCHAR(64) | 最近登录 IP |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

第一版使用 `role_code` 满足基础权限需求。后续需要细粒度菜单和操作权限时，再拆分为 `sys_role`、`sys_permission`、`sys_user_role` 和 `sys_role_permission`。

### 4.2 `site_config` 站点配置

对应后台的“站点信息”，第一版只保留一套默认配置。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| config_code | VARCHAR(50) | 配置标识，默认值 `default`，唯一 |
| current_exhibition_id | BIGINT UNSIGNED | 当前主展会，可为空 |
| site_title | VARCHAR(200) | 站点标题 |
| site_subtitle | VARCHAR(200) | 站点副标题 |
| domain | VARCHAR(255) | 站点域名 |
| logo_url | VARCHAR(500) | 站点 Logo |
| keywords | VARCHAR(500) | SEO 关键词 |
| description | TEXT | SEO 描述 |
| icp_number | VARCHAR(100) | 备案号 |
| template_code | VARCHAR(100) | 模板标识，例如 `coatingfair` |
| statistics_code | TEXT | 统计代码 |
| footer_info | TEXT | 页脚信息 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

`current_exhibition_id` 用于确定公共端当前展示哪一届展会。历史展会不删除，只切换当前主展会。

### 4.3 `company_profile` 公司信息

对应后台的“公司信息”，第一版只保留一套默认公司信息。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| profile_code | VARCHAR(50) | 配置标识，默认值 `default`，唯一 |
| company_name | VARCHAR(200) | 公司名称 |
| address | VARCHAR(500) | 公司地址 |
| postal_code | VARCHAR(20) | 邮政编码 |
| contact_name | VARCHAR(100) | 联系人 |
| mobile | VARCHAR(30) | 手机号 |
| telephone | VARCHAR(30) | 电话 |
| fax | VARCHAR(30) | 传真 |
| email | VARCHAR(150) | 电子邮箱 |
| qq | VARCHAR(50) | QQ |
| wechat_image_url | VARCHAR(500) | 微信二维码或图标地址 |
| business_license_no | VARCHAR(100) | 营业执照号码 |
| other_info | TEXT | 其他信息 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 4.4 `exhibition` 展会信息

展会是第一版的核心业务实体，负责管理展会列表、详情、发布和首页投放。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| exhibition_code | VARCHAR(100) | 展会业务编码，唯一 |
| title | VARCHAR(200) | 展会标题 |
| subtitle | VARCHAR(200) | 副标题，可为空 |
| year | SMALLINT | 举办年份 |
| edition | VARCHAR(50) | 届次，例如第二十一届 |
| cover_url | VARCHAR(500) | 封面图 |
| summary | TEXT | 展会简介 |
| description | LONGTEXT | 展会详细描述，支持富文本 |
| venue | VARCHAR(200) | 展馆或举办地点 |
| address | VARCHAR(500) | 详细地址 |
| start_at | DATETIME | 开始时间 |
| end_at | DATETIME | 结束时间 |
| status | VARCHAR(20) | `DRAFT`、`PUBLISHED`、`OFFLINE` |
| show_on_home | TINYINT | 是否作为首页展会内容 |
| home_sort | INT | 首页排序值，越小越靠前 |
| published_at | DATETIME | 发布时间 |
| contact_name | VARCHAR(100) | 联系人 |
| contact_phone | VARCHAR(30) | 联系电话 |
| registration_url | VARCHAR(500) | 报名地址，可为空 |
| extra_data | JSON | 后续扩展属性 |
| created_by | BIGINT UNSIGNED | 创建人 |
| updated_by | BIGINT UNSIGNED | 修改人 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

“已结束”不单独保存为状态，由 `end_at` 和当前时间计算。只有草稿、已发布、已下架属于后台可维护状态。

### 4.5 `cms_category` 内容栏目

对应后台的栏目列表，使用 `parent_id` 建立树形结构。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| parent_id | BIGINT UNSIGNED | 父栏目 ID，顶级栏目为 0 |
| name | VARCHAR(100) | 栏目名称 |
| code | VARCHAR(100) | 栏目标识，例如 `news`、`products` |
| url_name | VARCHAR(150) | 公共端 URL 名称 |
| model_code | VARCHAR(50) | `PAGE`、`ARTICLE`、`PRODUCT`、`CASE`、`LINK`、`DIRECTORY` |
| list_template | VARCHAR(100) | 列表页模板 |
| detail_template | VARCHAR(100) | 详情页模板 |
| sort_order | INT | 栏目排序 |
| is_enabled | TINYINT | 是否启用 |
| show_in_nav | TINYINT | 是否显示在公共端导航 |
| link_url | VARCHAR(500) | `LINK` 类型栏目使用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

第一版初始化栏目建议如下：

```text
中国国际涂料博览会
├─ 展会简介
└─ 平面图
参展范围
├─ 原材料
├─ 绿色涂料
├─ 绿色涂料及智能制造
├─ 绿色供应链
├─ 表面处理
├─ 粉末涂料涂装
├─ 环氧专区
└─ 其他
同期活动
展会相关
├─ 展商新闻
├─ 媒体报道
└─ 精彩回顾
新闻资讯
├─ 展会动态
└─ 行业新闻
联系我们
└─ 在线留言
```

### 4.6 `cms_content` 栏目内容

用于保存展会简介、平面图说明、参展范围、活动介绍、新闻和展商动态等内容。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| category_id | BIGINT UNSIGNED | 所属栏目 |
| exhibition_id | BIGINT UNSIGNED | 所属展会，可为空 |
| title | VARCHAR(200) | 标题 |
| slug | VARCHAR(150) | URL 标识，可为空 |
| cover_url | VARCHAR(500) | 封面图 |
| summary | TEXT | 摘要 |
| body | LONGTEXT | 正文内容，支持富文本 |
| author | VARCHAR(100) | 作者 |
| source | VARCHAR(200) | 来源 |
| status | VARCHAR(20) | `DRAFT`、`PUBLISHED`、`OFFLINE` |
| published_at | DATETIME | 发布时间 |
| sort_order | INT | 排序值 |
| is_top | TINYINT | 是否置顶 |
| is_recommend | TINYINT | 是否推荐 |
| view_count | INT UNSIGNED | 浏览量 |
| extra_data | JSON | 不适合单独建列的扩展属性 |
| created_by | BIGINT UNSIGNED | 创建人 |
| updated_by | BIGINT UNSIGNED | 修改人 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

`exhibition_id` 允许历史展会内容并存。与当前展会相关的公共内容通过 `site_config.current_exhibition_id` 过滤。

### 4.7 `home_section` 首页展示区

用于定义首页模块，例如展会主区域、同期活动、参展范围、新闻资讯和展商动态。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| section_code | VARCHAR(100) | 模块编码，唯一 |
| section_name | VARCHAR(100) | 模块名称 |
| section_type | VARCHAR(50) | `EXHIBITION`、`CONTENT_LIST`、`CATEGORY_LIST`、`CUSTOM` |
| sort_order | INT | 首页模块排序 |
| is_enabled | TINYINT | 是否启用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

### 4.8 `home_section_item` 首页展示项

用于配置某个首页展示区具体显示哪些展会或内容。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| section_id | BIGINT UNSIGNED | 所属首页展示区 |
| target_type | VARCHAR(30) | `EXHIBITION`、`CONTENT`、`CATEGORY` |
| target_id | BIGINT UNSIGNED | 对应业务数据 ID |
| title_override | VARCHAR(200) | 首页标题覆盖值，可为空 |
| image_override | VARCHAR(500) | 首页图片覆盖值，可为空 |
| sort_order | INT | 展示顺序 |
| start_at | DATETIME | 展示开始时间，可为空 |
| end_at | DATETIME | 展示结束时间，可为空 |
| is_enabled | TINYINT | 是否启用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

`target_id` 是多态引用，数据库不建立单一外键，由后端根据 `target_type` 校验目标是否存在。这样可以用同一套首页配置支持展会、文章和栏目。

### 4.9 `file_asset` 文件资源

统一记录 Logo、封面图、微信图标、正文图片等上传文件。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| original_name | VARCHAR(255) | 原始文件名 |
| storage_name | VARCHAR(255) | 存储文件名 |
| file_url | VARCHAR(500) | 文件访问地址 |
| file_type | VARCHAR(100) | MIME 类型或扩展名 |
| file_size | BIGINT UNSIGNED | 文件大小，单位字节 |
| file_hash | VARCHAR(100) | 文件哈希，可用于去重 |
| uploaded_by | BIGINT UNSIGNED | 上传人 |
| created_at | DATETIME | 上传时间 |
| deleted | TINYINT | 逻辑删除 |

### 4.10 `guestbook` 在线留言

对应公共端“在线留言”页面，管理端可查看、处理和回复。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| name | VARCHAR(100) | 留言人姓名 |
| phone | VARCHAR(30) | 联系电话 |
| email | VARCHAR(150) | 邮箱，可为空 |
| company_name | VARCHAR(200) | 公司名称，可为空 |
| message | TEXT | 留言内容 |
| status | VARCHAR(20) | `UNREAD`、`READ`、`REPLIED`、`CLOSED` |
| reply_content | TEXT | 回复内容，可为空 |
| replied_by | BIGINT UNSIGNED | 回复人，可为空 |
| replied_at | DATETIME | 回复时间，可为空 |
| ip_address | VARCHAR(64) | 提交 IP |
| created_at | DATETIME | 提交时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

手机号、邮箱和 IP 属于个人信息，管理端展示和日志记录应遵循最小化原则，并限制访问权限。

### 4.11 `operation_log` 操作日志

用于记录管理员的登录、发布、下架、删除、修改配置和处理留言等操作。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT UNSIGNED | 主键 |
| user_id | BIGINT UNSIGNED | 操作人 |
| username | VARCHAR(50) | 操作账号快照 |
| module | VARCHAR(50) | 所属模块 |
| operation | VARCHAR(100) | 操作名称 |
| request_method | VARCHAR(10) | HTTP 方法 |
| request_url | VARCHAR(500) | 请求地址 |
| request_ip | VARCHAR(64) | 请求 IP |
| request_params | JSON | 请求参数，敏感字段需脱敏 |
| result | VARCHAR(20) | `SUCCESS` 或 `FAILURE` |
| error_message | VARCHAR(500) | 失败原因，可为空 |
| created_at | DATETIME | 操作时间 |

## 5. 关键关系

```text
site_config.current_exhibition_id ──> exhibition.id

cms_category.parent_id ──> cms_category.id
cms_content.category_id ──> cms_category.id
cms_content.exhibition_id ──> exhibition.id

home_section_item.section_id ──> home_section.id
home_section_item.target_id ──> exhibition.id / cms_content.id / cms_category.id

cms_content.created_by ──> sys_user.id
exhibition.created_by ──> sys_user.id
file_asset.uploaded_by ──> sys_user.id
guestbook.replied_by ──> sys_user.id
```

业务表可以建立普通索引或逻辑外键约束；多态的 `home_section_item.target_id` 由应用层校验。

## 6. 状态和公共端读取规则

### 内容发布状态

```text
DRAFT     草稿，只能管理端查看
PUBLISHED 已发布，公共端可读取
OFFLINE   已下架，公共端不可读取
```

公共端读取内容必须同时满足：

```text
status = PUBLISHED
deleted = 0
published_at 为空或 published_at <= 当前时间
```

### 展会首页读取

默认读取：

```text
exhibition.status = PUBLISHED
exhibition.show_on_home = 1
exhibition.deleted = 0
```

如果首页通过 `home_section_item` 配置具体内容，则以首页配置为准，并额外校验目标数据已经发布、启用且未删除。

### 当前展会读取

公共端默认从 `site_config.current_exhibition_id` 获取当前展会，再读取该展会关联的简介、活动、参展范围和新闻内容。

## 7. 重要索引

```text
site_config(config_code) UNIQUE
company_profile(profile_code) UNIQUE
sys_user(username) UNIQUE
exhibition(exhibition_code) UNIQUE
cms_category(code) UNIQUE
cms_category(parent_id, sort_order)
cms_content(category_id, status, published_at)
cms_content(exhibition_id, status, published_at)
cms_content(is_top, sort_order)
exhibition(status, start_at)
exhibition(show_on_home, home_sort)
home_section(section_code) UNIQUE
home_section_item(section_id, sort_order)
file_asset(file_hash)
guestbook(status, created_at)
operation_log(user_id, created_at)
```

## 8. 第一版管理端对应模块

```text
登录与账号
├─ 管理员登录
└─ 个人信息和密码修改

站点管理
├─ 站点信息
└─ 公司信息

展会管理
├─ 展会列表
├─ 新增/编辑展会
├─ 发布/下架
└─ 首页投放和排序

内容管理
├─ 栏目列表
├─ 栏目新增/编辑
├─ 内容列表
└─ 内容新增/编辑

首页管理
└─ 首页展示区和展示项排序

互动管理
└─ 在线留言

系统管理
├─ 文件资源
└─ 操作日志
```

## 9. 第一版不纳入的功能

以下功能保留扩展空间，但不进入第一版数据库必需范围：

- 多站点和多域名管理。
- 展商报名、展位预订和在线支付。
- 展会日程、讲师和会议报名。
- 展商企业独立账号和展商后台。
- 复杂 RBAC 权限、部门和数据权限。
- 多语言内容。
- 专门的产品属性表、案例属性表和行业词典。
- 内容版本管理和审核流程。

后续增加这些能力时，优先通过扩展表或关联表实现，不直接改变现有核心字段语义。

## 10. 第一版验收标准

1. 管理员可以登录并通过 JWT 访问管理接口。
2. 管理端可以维护站点信息和公司信息，公共端可以读取最新配置。
3. 管理端可以维护多级栏目，并控制栏目启用、导航显示和排序。
4. 管理端可以新增、编辑、发布、下架和删除展会。
5. 管理端可以设置当前展会及首页展示顺序。
6. 管理端可以维护展会简介、参展范围、同期活动和新闻内容。
7. 公共端只展示已发布、已启用、未删除的数据。
8. 公共端可以提交在线留言，管理端可以查看、标记和回复。
9. Logo、封面、微信图片和正文图片可以统一上传并记录资源信息。
10. 关键管理操作可以在操作日志中追踪到账号、时间、模块和结果。

