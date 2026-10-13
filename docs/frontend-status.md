# EEMS 前端现状说明

> 用途：提供给外部技术顾问分析当前前端工程现状。
>
> 说明：本文只记录当前代码、接口和已知问题，不包含解决方案或改造建议。
>
> 项目目录：`D:/HT/eems/frontend`
>
> 当前前端技术栈：Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus、Axios、pnpm。

## 一、项目结构

### 1.1 `src/` 目录结构

```text
src/
├── api/
│   ├── admin.spec.ts
│   ├── admin.ts
│   ├── auth.spec.ts
│   ├── auth.ts
│   ├── public.spec.ts
│   └── public.ts
├── components/
│   ├── admin/
│   │   └── ExhibitionStatusTag.vue
│   ├── FrameworkPlaceholder.vue
│   └── public/
│       ├── PublicCategoryNavigation.vue
│       ├── PublicContentSection.vue
│       └── PublicExhibitionCard.vue
├── layouts/
│   ├── AdminLayout.vue
│   ├── PublicLayout.vue
│   └── UserLayout.vue
├── router/
│   ├── admin.ts
│   ├── index.ts
│   └── user.ts
├── stores/
│   ├── index.ts
│   ├── public.ts
│   ├── user.spec.ts
│   └── user.ts
├── styles/
│   └── index.css
├── types/
│   ├── admin.ts
│   ├── api.ts
│   ├── auth.ts
│   └── public.ts
├── utils/
│   ├── asset.spec.ts
│   ├── asset.ts
│   ├── exhibition.ts
│   ├── format.ts
│   ├── http.ts
│   ├── storage.ts
│   ├── validation.spec.ts
│   └── validation.ts
├── views/
│   ├── admin/
│   │   ├── DashboardView.vue
│   │   ├── ExhibitionEditView.vue
│   │   ├── ExhibitionListView.vue
│   │   └── LoginView.vue
│   ├── user/
│   │   ├── CategoryContentView.vue
│   │   ├── ContentView.vue
│   │   ├── ExhibitionDetailView.vue
│   │   ├── ExhibitionListView.vue
│   │   └── HomeView.vue
│   └── NotFoundView.vue
├── App.vue
├── env.d.ts
└── main.ts
```

### 1.2 各目录文件说明

#### `src/api/`

| 文件 | 当前内容 |
| --- | --- |
| `auth.ts` | 管理端登录接口封装，调用 `/api/admin/login`。 |
| `auth.spec.ts` | 管理端登录 API 封装测试。 |
| `public.ts` | 公共首页、公共栏目树、公共展会、栏目内容列表和公共内容详情接口封装。 |
| `public.spec.ts` | 公共 API 封装测试，覆盖首页、展会列表、栏目内容列表和内容详情调用。 |

#### `src/components/`

| 文件 | 当前内容 |
| --- | --- |
| `admin/ExhibitionStatusTag.vue` | 管理端展会状态标签，将 `DRAFT`、`PUBLISHED`、`OFFLINE` 显示为中文状态。 |
| `FrameworkPlaceholder.vue` | 管理端占位页使用的通用占位组件。 |
| `public/PublicCategoryNavigation.vue` | 用户端顶部公共栏目导航，渲染栏目树、下拉菜单、内部链接和外部链接。 |
| `public/PublicContentSection.vue` | 首页内容模块卡片区域，用于展会简介、同期活动、参展范围、新闻等模块，并处理封面失败占位。 |
| `public/PublicExhibitionCard.vue` | 展会列表卡片，链接到展会详情页，并处理封面资源地址和加载失败占位。 |

#### `src/layouts/`

| 文件 | 当前内容 |
| --- | --- |
| `PublicLayout.vue` | 当前用户端实际使用的布局，包含顶部导航、内容区和页脚。 |
| `UserLayout.vue` | 旧的用户端简单布局，目前路由未使用。 |
| `AdminLayout.vue` | 管理端布局，包含侧边栏、后台标题和退出登录按钮。 |

#### `src/router/`

| 文件 | 当前内容 |
| --- | --- |
| `index.ts` | 创建 Vue Router、合并用户端和管理端路由、执行登录相关全局守卫。 |
| `user.ts` | 用户端路由树。 |
| `admin.ts` | 管理端路由树。 |

#### `src/stores/`

| 文件 | 当前内容 |
| --- | --- |
| `index.ts` | 创建并导出 Pinia 实例。 |
| `public.ts` | 保存站点信息、公司信息、公共栏目树和导航加载状态。 |
| `user.ts` | 保存管理员登录 Token 和登录状态。 |
| `user.spec.ts` | 用户 Store 的 Token 恢复、登录和清除测试。 |

#### `src/styles/`

| 文件 | 当前内容 |
| --- | --- |
| `index.css` | 全局基础样式、页面基础颜色、字体和盒模型设置。 |

#### `src/types/`

| 文件 | 当前内容 |
| --- | --- |
| `admin.ts` | 管理端展会、分页查询、保存请求、状态和文件上传返回类型。 |
| `api.ts` | 统一接口响应体和接口错误类型。 |
| `auth.ts` | 管理端登录请求、登录结果和管理员用户类型。 |
| `public.ts` | 公共站点、公司、栏目、内容摘要、展会、首页聚合和分页类型。 |

#### `src/utils/`

| 文件 | 当前内容 |
| --- | --- |
| `http.ts` | Axios 实例、请求拦截器、统一响应体处理、401 处理和错误提示。 |
| `asset.ts` | 将后端返回的相对资源路径转换为可访问的资源地址，并保留绝对、`data:`、`blob:` 地址。 |
| `asset.spec.ts` | 资源地址转换测试。 |
| `format.ts` | 日期时间和日期范围格式化。 |
| `storage.ts` | 管理端 Access Token 的 Local Storage 读写。 |
| `validation.ts` | 登录表单基础校验。 |
| `validation.spec.ts` | 登录表单校验测试。 |

#### `src/views/`

| 文件 | 当前内容 |
| --- | --- |
| `user/HomeView.vue` | 用户端首页，展示当前展会封面；资源地址使用统一转换，加载失败时显示占位。 |
| `user/ExhibitionListView.vue` | 用户端展会列表页。 |
| `user/ExhibitionDetailView.vue` | 用户端展会详情页，使用统一资源地址转换展示封面。 |
| `user/CategoryContentView.vue` | 用户端栏目内容列表页，支持分页、内容跳转和封面失败占位。 |
| `user/ContentView.vue` | 用户端公共内容详情页，渲染富文本正文和内容封面。 |
| `admin/LoginView.vue` | 管理端登录页。 |
| `admin/DashboardView.vue` | 管理端后台首页占位页。 |
| `admin/ExhibitionListView.vue` | 管理端展会分页列表，支持关键字、状态、年份筛选和发布、下架、删除、编辑操作。 |
| `admin/ExhibitionEditView.vue` | 管理端新增/编辑展会表单，包含必填和时间校验、封面上传及 URL 手动填写。 |
| `admin/ModulePlaceholderView.vue` | 栏目管理和内容管理的基础占位页。 |
| `NotFoundView.vue` | 未匹配路由页面。 |

#### `src/` 根目录文件

| 文件 | 当前内容 |
| --- | --- |
| `App.vue` | 只渲染根级 `RouterView`。 |
| `main.ts` | 创建 Vue 应用，注册 Pinia、Vue Router 和 Element Plus。 |
| `env.d.ts` | Vite 环境变量类型声明。 |

## 二、路由配置

### 2.1 完整路由表

| 路径 | 路由名称 | 页面组件 | Layout | 是否需要登录 | 当前状态 |
| --- | --- | --- | --- | --- | --- |
| `/` | `user-home` | `src/views/user/HomeView.vue` | `PublicLayout.vue` | 否 | 已实现 |
| `/exhibitions` | `public-exhibitions` | `src/views/user/ExhibitionListView.vue` | `PublicLayout.vue` | 否 | 已实现 |
| `/exhibitions/:id` | `public-exhibition-detail` | `src/views/user/ExhibitionDetailView.vue` | `PublicLayout.vue` | 否 | 已实现，参数限定为数字 |
| `/category/:code` | `public-category-content` | `src/views/user/CategoryContentView.vue` | `PublicLayout.vue` | 否 | 已实现，调用栏目内容分页接口 |
| `/content/:id` | `public-content-detail` | `src/views/user/ContentView.vue` | `PublicLayout.vue` | 否 | 已实现，参数限定为数字 |
| `/admin` | `admin-dashboard` | `src/views/admin/DashboardView.vue` | `AdminLayout.vue` | 是 | 已配置基础后台首页 |
| `/admin/exhibitions` | `admin-exhibitions` | `src/views/admin/ExhibitionListView.vue` | `AdminLayout.vue` | 是 | 已实现展会列表、筛选、分页和状态操作 |
| `/admin/exhibitions/new` | `admin-exhibition-create` | `src/views/admin/ExhibitionEditView.vue` | `AdminLayout.vue` | 是 | 已实现新增展会表单 |
| `/admin/exhibitions/:id/edit` | `admin-exhibition-edit` | `src/views/admin/ExhibitionEditView.vue` | `AdminLayout.vue` | 是 | 已实现编辑展会表单 |
| `/admin/categories` | `admin-categories` | `src/views/admin/ModulePlaceholderView.vue` | `AdminLayout.vue` | 是 | 栏目管理占位页 |
| `/admin/contents` | `admin-contents` | `src/views/admin/ModulePlaceholderView.vue` | `AdminLayout.vue` | 是 | 内容管理占位页 |
| `/admin/login` | `admin-login` | `src/views/admin/LoginView.vue` | 无独立 Layout | 否，登录用户会被重定向 | 已实现基础登录页 |
| `/:pathMatch(.*)*` | `not-found` | `src/views/NotFoundView.vue` | 无 | 否 | 已实现兜底页 |

当前用户端路由配置代码如下：

```ts
export const userRoutes: RouteRecordRaw = {
  path: '/',
  component: () => import('@/layouts/PublicLayout.vue'),
  children: [
    { path: '', name: 'user-home', component: () => import('@/views/user/HomeView.vue') },
    { path: 'exhibitions', name: 'public-exhibitions', component: () => import('@/views/user/ExhibitionListView.vue') },
    {
      path: 'exhibitions/:id(\\d+)',
      name: 'public-exhibition-detail',
      component: () => import('@/views/user/ExhibitionDetailView.vue'),
    },
    {
      path: 'category/:code',
      name: 'public-category-content',
      component: () => import('@/views/user/CategoryContentView.vue'),
    },
    {
      path: 'content/:id(\\d+)',
      name: 'public-content-detail',
      component: () => import('@/views/user/ContentView.vue'),
    },
  ],
}
```

管理端路由统一挂载在 `/admin` 下，父路由使用 `AdminLayout.vue` 并设置 `meta.requiresAuth: true`：

```ts
export const adminRoutes: RouteRecordRaw = {
  path: '/admin',
  component: () => import('@/layouts/AdminLayout.vue'),
  meta: { requiresAuth: true },
  children: [
    { path: '', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue') },
    { path: 'exhibitions', name: 'admin-exhibitions', component: () => import('@/views/admin/ExhibitionListView.vue') },
    { path: 'exhibitions/new', name: 'admin-exhibition-create', component: () => import('@/views/admin/ExhibitionEditView.vue') },
    { path: 'exhibitions/:id(\\d+)/edit', name: 'admin-exhibition-edit', component: () => import('@/views/admin/ExhibitionEditView.vue') },
    { path: 'categories', name: 'admin-categories', component: () => import('@/views/admin/ModulePlaceholderView.vue') },
    { path: 'contents', name: 'admin-contents', component: () => import('@/views/admin/ModulePlaceholderView.vue') },
  ],
}
```

### 2.2 公共端和管理端区分方式

- 公共端由 `src/router/user.ts` 定义，根路径为 `/`，使用 `PublicLayout.vue`。
- 管理端由 `src/router/admin.ts` 定义，根路径为 `/admin`，使用 `AdminLayout.vue`。
- 管理端父路由设置 `meta.requiresAuth: true`。
- `src/router/index.ts` 的全局 `beforeEach` 守卫读取 `userStore.isLoggedIn`。
- 未登录访问管理端路径时跳转到 `/admin/login`，并通过 `redirect` 查询参数保存原始路径。
- 已登录用户访问 `/admin/login` 时跳转到 `/admin`。
- 用户端页面不需要登录，也没有公共端登录态判断。

## 三、公共端页面清单

### 3.1 已配置的公共端页面

| 页面 | 路径 | 文件位置 | 功能 | 状态 |
| --- | --- | --- | --- | --- |
| 首页 | `/` | `src/views/user/HomeView.vue` | 调用 `/api/public/home`，展示站点信息、当前展会、展会简介、同期活动、参展范围、展会动态、行业新闻、展商新闻。 | 已完成 |
| 会展列表 | `/exhibitions` | `src/views/user/ExhibitionListView.vue` | 调用 `/api/public/exhibitions`，支持关键字、年份筛选、分页、加载态、空状态和错误重试。 | 已完成 |
| 会展详情 | `/exhibitions/:id` | `src/views/user/ExhibitionDetailView.vue` | 调用 `/api/public/exhibitions/{id}`，展示封面、标题、副标题、编码、时间、地点、描述、报名地址和展会明细信息。 | 已完成 |
| 栏目内容列表 | `/category/:code` | `src/views/user/CategoryContentView.vue` | 调用 `/api/public/categories/{code}/contents`，展示栏目内容摘要并支持分页。 | 已完成 |
| 公共内容详情 | `/content/:id` | `src/views/user/ContentView.vue` | 调用 `/api/public/contents/{id}`，展示标题、摘要、元信息、封面和富文本正文。 | 已完成 |

### 3.2 当前没有实现的公共端页面

栏目内容列表页和公共内容详情页已经实现。当前仍没有通用栏目目录页或预登记业务页：

| 预期页面类型 | 当前情况 |
| --- | --- |
| 栏目目录页 | 未实现，没有独立的通用栏目目录页面；内容型栏目直接进入内容列表页。 |
| 展馆与交通页 | 已纳入栏目内容列表链路，路径由栏目编码生成。 |
| 展会简介栏目页 | 已纳入栏目内容列表链路，路径为 `/category/exhibition-intro`。 |
| 参展范围栏目页 | 已纳入栏目内容列表链路，路径由栏目编码生成。 |
| 新闻中心各列表页 | 展会动态、行业资讯、媒体报道等内容型子栏目已纳入栏目内容列表链路。 |
| 同期活动栏目页 | 已纳入栏目内容列表链路，路径由栏目编码生成。 |
| 参展服务页面 | 未实现。 |
| 观众服务页面 | 未实现。 |
| 联系我们页面 | 未实现。 |

### 3.3 管理端页面现状

管理端已经具备基础布局和展会管理页面，栏目管理与内容管理仍为占位页面：

| 页面 | 路径 | 文件位置 | 状态 |
| --- | --- | --- | --- |
| 管理员登录 | `/admin/login` | `src/views/admin/LoginView.vue` | 已有基础登录表单 |
| 管理首页 | `/admin` | `src/views/admin/DashboardView.vue` | 基础占位页 |
| 展会列表 | `/admin/exhibitions` | `src/views/admin/ExhibitionListView.vue` | 已实现筛选、分页、封面、状态和操作按钮 |
| 新增展会 | `/admin/exhibitions/new` | `src/views/admin/ExhibitionEditView.vue` | 已实现表单、校验和图片上传 |
| 编辑展会 | `/admin/exhibitions/:id/edit` | `src/views/admin/ExhibitionEditView.vue` | 已实现详情加载、编辑保存和图片预览 |
| 栏目管理 | `/admin/categories` | `src/views/admin/ModulePlaceholderView.vue` | 占位页，业务功能未实现 |
| 内容管理 | `/admin/contents` | `src/views/admin/ModulePlaceholderView.vue` | 占位页，业务功能未实现 |

## 四、接口封装

### 4.1 `src/api/auth.ts`

| 前端方法 | HTTP 方法 | 后端路径 | 用途 |
| --- | --- | --- | --- |
| `authApi.login(payload)` | `POST` | `/api/admin/login` | 管理员登录，返回 Token 和管理员信息。 |

代码形式：

```ts
export const authApi = {
  login(payload: LoginRequest): Promise<LoginResult> {
    return http.post<LoginResult, LoginResult>('/api/admin/login', payload)
  },
}
```

### 4.2 `src/api/admin.ts`

| 前端方法 | HTTP 方法 | 后端路径 | 用途 |
| --- | --- | --- | --- |
| `adminApi.exhibitions(params)` | `GET` | `/api/admin/exhibitions` | 管理端展会分页查询，参数包括 `keyword`、`status`、`year`、`page`、`pageSize`。 |
| `adminApi.exhibition(id)` | `GET` | `/api/admin/exhibitions/{id}` | 获取展会编辑详情。 |
| `adminApi.createExhibition(payload)` | `POST` | `/api/admin/exhibitions` | 新增展会。 |
| `adminApi.updateExhibition(id, payload)` | `PUT` | `/api/admin/exhibitions/{id}` | 更新展会。 |
| `adminApi.deleteExhibition(id)` | `DELETE` | `/api/admin/exhibitions/{id}` | 删除展会。 |
| `adminApi.publishExhibition(id)` | `POST` | `/api/admin/exhibitions/{id}/publish` | 发布草稿展会。 |
| `adminApi.offlineExhibition(id)` | `POST` | `/api/admin/exhibitions/{id}/offline` | 下架已发布展会。 |
| `adminApi.uploadFile(file)` | `POST` | `/api/admin/files` | 以 multipart/form-data 上传封面文件并取得文件 URL。 |

对应测试文件为 `src/api/admin.spec.ts`，覆盖列表查询、详情、状态操作、新增、编辑和文件上传请求。

### 4.3 `src/api/public.ts`

| 前端方法 | HTTP 方法 | 后端路径 | 用途 |
| --- | --- | --- | --- |
| `publicApi.home()` | `GET` | `/api/public/home` | 获取公共首页聚合数据。 |
| `publicApi.categoryTree()` | `GET` | `/api/public/categories/tree` | 获取启用且显示在导航中的公共栏目树。 |
| `publicApi.exhibitions(params)` | `GET` | `/api/public/exhibitions` | 获取公共展会列表，参数包括 `keyword`、`year`、`page`、`pageSize`。 |
| `publicApi.exhibition(id)` | `GET` | `/api/public/exhibitions/{id}` | 获取公共展会详情。 |
| `publicApi.categoryContents(code, params)` | `GET` | `/api/public/categories/{code}/contents` | 获取指定栏目下的公开内容，参数包括 `keyword`、`page`、`pageSize`。 |
| `publicApi.contentDetail(id)` | `GET` | `/api/public/contents/{id}` | 获取公开内容详情及富文本正文。 |

当前 `src/api/` 没有封装以下公共后端接口：

| 后端路径 | HTTP 方法 | 当前前端状态 |
| --- | --- | --- |
| `/api/public/guestbook` | `POST` | 未封装 |

### 4.4 统一请求封装

`src/utils/http.ts` 创建 Axios 实例：

```ts
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: Number(import.meta.env.VITE_API_TIMEOUT ?? 10000),
})
```

当前环境配置中的后端地址为：

```env
VITE_API_BASE_URL=http://localhost:8080
```

请求拦截器从用户 Store 读取 Token，并追加：

```http
Authorization: Bearer <token>
```

响应拦截器将统一响应体 `{ code, message, data }` 解包为 `data`，支持的成功编码包括 `OK`、`0`、`200`、`SUCCESS`。业务编码为 `401` 或 `UNAUTHORIZED`，或者 HTTP 状态为 `401` 时，会清除登录状态并跳转管理端登录页。

## 五、顶部导航菜单实现说明

### 5.1 菜单数据来源

顶部导航由 `PublicLayout.vue` 引入：

```vue
<PublicCategoryNavigation
  :categories="publicStore.categories"
  :loading="publicStore.navigationLoading"
/>
```

`PublicLayout.vue` 挂载时调用 `publicStore.loadNavigation()`。

`src/stores/public.ts` 中的加载逻辑调用：

```ts
categories.value = await publicApi.categoryTree()
```

对应后端接口：

```http
GET /api/public/categories/tree
```

当前运行环境实际返回的栏目树包含：

```text
展会概览
├── 展会简介
├── 展馆与交通
└── 参展范围
新闻中心
├── 展会动态
├── 行业资讯
└── 媒体报道
同期活动
参展服务
├── 参展指南
└── 展位申请
观众服务
├── 参观指南
└── 观众预登记
联系我们
```

实际栏目对象包含 `id`、`parentId`、`name`、`code`、`urlName`、`modelCode`、`linkUrl`、`children` 等字段。

### 5.2 点击事件和渲染分支

顶部导航组件为 `src/components/public/PublicCategoryNavigation.vue`。

父栏目有子栏目时使用 Element Plus 下拉菜单：

```vue
<el-dropdown v-if="category.children.length > 0" trigger="hover">
  <span class="nav-item nav-dropdown-item">
    {{ category.name }}
    <el-icon><ArrowDown /></el-icon>
  </span>
  <template #dropdown>
    <el-dropdown-menu>
      <el-dropdown-item v-for="child in category.children" :key="child.id">
        ...
      </el-dropdown-item>
    </el-dropdown-menu>
  </template>
</el-dropdown>
```

当前子栏目有三种渲染分支：

```vue
<RouterLink v-if="getInternalPath(child)" :to="getInternalPath(child)!">
  {{ child.name }}
</RouterLink>
<a v-else-if="isExternalLink(child.linkUrl)" :href="child.linkUrl">
  {{ child.name }}
</a>
<button v-else type="button" class="nav-link" @click="handleUnavailableLink">
  {{ child.name }}
</button>
```

没有子栏目的根栏目也使用同样的判断逻辑：

```vue
<RouterLink v-else-if="getInternalPath(category)" :to="getInternalPath(category)!" class="nav-item">
  {{ category.name }}
</RouterLink>
<a v-else-if="isExternalLink(category.linkUrl)" :href="category.linkUrl" class="nav-item">
  {{ category.name }}
</a>
<button v-else type="button" class="nav-item nav-link" @click="handleUnavailableLink">
  {{ category.name }}
</button>
```

### 5.3 当前内部路由匹配逻辑

导航组件中的 `getInternalPath()` 当前识别展会浏览入口，以及 `PAGE`、`ARTICLE` 类型的内容栏目：

```ts
function getInternalPath(category: PublicCategory): string | null {
  const keys = [category.code, category.urlName]
    .filter((value): value is string => Boolean(value))
    .map((value) => value.toLowerCase())
  if (keys.some((key) => ['exhibition', 'exhibitions', 'exhibition-overview', 'overview'].includes(key))) {
    return '/exhibitions'
  }
  if (category.code && ['PAGE', 'ARTICLE'].includes(category.modelCode.toUpperCase())) {
    return `/category/${encodeURIComponent(category.code)}`
  }
  return null
}
```

当前运行环境的“展会概览”栏目使用 `code=exhibition-overview`、`urlName=overview`，因此现在会匹配到 `/exhibitions`。当前展会浏览入口同时来自动态栏目和布局中额外写死的按钮：

```vue
<RouterLink class="exhibition-link" to="/exhibitions">展会</RouterLink>
```

因此，动态栏目树中的“展会概览”虽然会匹配 `/exhibitions`，但由于父栏目优先进入下拉菜单分支，父节点本身仍不是路由链接。

### 5.4 当前无对应页面菜单的处理

当栏目没有有效内部路由，也没有可跳转的外部 `linkUrl` 时，组件渲染可点击按钮：

```vue
<button v-else type="button" class="nav-link" @click="handleUnavailableLink">
  {{ child.name }}
</button>
```

按钮点击调用 `handleUnavailableLink()`，显示 `ElMessage.info('该功能即将上线')`，不会发生路由变化，也不会进入空白页。当前这个兜底分支主要用于没有页面的内部链接。

根据当前栏目树，以下菜单项属于内容路由分支：

- 展会简介 -> `/category/exhibition-intro`
- 展馆与交通 -> `/category/venue-transport`
- 参展范围 -> `/category/exhibition-scope`
- 展会动态 -> `/category/exhibition-news`
- 行业资讯 -> `/category/industry-news`
- 媒体报道 -> `/category/media-reports`
- 同期活动 -> `/category/events`
- 参展指南 -> `/category/exhibitor-guide`
- 参观指南 -> `/category/visitor-guide`
- 联系我们 -> `/category/contact`

### 5.5 暂未实现内部链接的当前处理

“展位申请”和“观众预登记”携带站内 `linkUrl`，但前端路由表没有对应页面，因此会渲染为带点击处理的按钮：

```vue
<button v-else type="button" class="nav-link" @click="handleUnavailableLink">
  {{ child.name }}
</button>
```

当前后端返回的地址为：

```text
展位申请 -> /registration/exhibitor?exhibition=GCSE-2027
观众预登记 -> /registration/visitor?exhibition=GCSE-2027
```

前端路由表没有这两个路径对应的页面，因此现在点击时不再改变地址，而是调用：

```ts
function handleUnavailableLink(): void {
  ElMessage.info('该功能即将上线')
}
```

因此这些链接当前不会进入 `/registration/...`，也不会因为缺少对应页面而出现空白页；实际业务页面仍未实现。

内容型动态栏目已经共用 `CategoryContentView.vue`，不再为每个栏目单独创建页面文件；详情内容共用 `ContentView.vue`。

## 六、当前已知问题清单

### 6.1 顶部菜单点击无反应的具体菜单项

当前没有菜单项再渲染为无事件的 `span`。内容型栏目已经跳转到真实栏目列表页，当前仍会显示“该功能即将上线”的菜单项为：

| 菜单项 | 栏目类型 | 当前渲染结果 | 关联原因 |
| --- | --- | --- | --- |
| 暂无栏目内容且未配置可用路由的栏目 | 子栏目或根栏目 | `<button>` | 没有可用内部路由，也没有有效外部链接 |

### 6.2 点击后提示“即将上线”的具体菜单项

以下菜单项使用后端 `linkUrl`，但前端路由表没有对应业务页面。当前点击不会发生浏览器地址变化，而是显示 Element Plus 信息提示：

| 菜单项 | 当前 `linkUrl` | 前端路由是否存在 | 当前结果 |
| --- | --- | --- | --- |
| 展位申请 | `/registration/exhibitor?exhibition=GCSE-2027` | 否 | 显示“该功能即将上线” |
| 观众预登记 | `/registration/visitor?exhibition=GCSE-2027` | 否 | 显示“该功能即将上线” |

### 6.3 顶部导航与会展浏览链路的关系问题

- 会展列表页 `/exhibitions` 本身已经存在并且有可用的 `RouterLink` 入口。
- 顶部导航动态栏目树中的“展会概览”当前仍由下拉菜单父节点承载，父节点本身不是 `/exhibitions` 的路由链接。
- `getInternalPath()` 会同时检查 `code` 和 `urlName`，识别 `exhibition`、`exhibitions`、`exhibition-overview`、`overview`，统一返回 `/exhibitions`。
- 顶部右侧的“展会”按钮是单独写死的 `/exhibitions` 路由，与动态栏目树没有绑定关系。
- 内容型栏目现在通过 `/category/:code` 进入栏目内容列表页，内容条目通过 `/content/:id` 进入详情页。

### 6.4 其他已知问题

- `src/layouts/UserLayout.vue` 仍保留旧用户端布局文件，但当前用户端路由使用的是 `src/layouts/PublicLayout.vue`。
- `PublicContentSection.vue` 当前仍只展示首页内容摘要卡片，没有绑定内容详情路由；顶部栏目内容链路使用独立的栏目列表页和内容详情页。
- 栏目内容页的栏目标题取自列表接口返回的 `categoryName`；当列表为空时使用路由中的栏目编码作为标题。
- 内容详情页使用后端返回的安全过滤后的 `body` 字段进行富文本渲染。
- `PublicCategoryNavigation.vue` 对已识别的会展浏览路径使用 `RouterLink`，对有效外部 `linkUrl` 使用原生 `<a>`，对暂未实现的站内链接和无目标栏目使用按钮及信息提示。
- 顶部导航下拉菜单由 `trigger="hover"` 打开；父栏目本身不是路由链接，只有下拉菜单中的子项可能产生导航行为。
- 公共栏目树加载失败时，`src/stores/public.ts` 将 `categories` 置为空数组，顶部导航只显示其它固定内容，不展示错误提示。
- 当前项目的管理端已实现展会管理列表、新增和编辑页面；栏目管理、内容管理仍为占位页。
- 当前文档只描述前端代码现状；接口是否能返回数据还取决于后端服务、数据库数据和栏目配置状态。
- 当前未发现公共展会类型字段错位：后端 `PublicExhibitionVO` 返回的 `startAt`、`endAt`、`coverUrl`、`registrationUrl`、`isCurrent` 等字段与 `src/types/public.ts` 一致。
- 后端 `PublicExhibitionVO` 当前没有明细项目数组字段；详情页展示的是后端实际返回的展会字段，不存在额外明细项目渲染链路。
- 公共端图片字段可能返回 `/uploads/...` 形式的相对路径，`src/utils/asset.ts` 会拼接 `VITE_API_BASE_URL` 后再交给图片组件加载；公共端首页、展会卡片、详情页、栏目内容页和内容详情页均已使用该转换。
- 当前接口返回的站点 Logo 路径为 `/uploads/site/logo.png`，本地文件存储目录中不存在该文件；`PublicLayout.vue` 在 Logo 加载失败时隐藏破图并保留站点名称。首页当前展会封面路径对应实际文件，可正常请求。

## 七、当前会展浏览链路状态总结

当前已具备的会展浏览链路如下：

```text
首页 /
  └── “浏览全部展会”或顶部固定“展会”按钮
        └── 展会列表 /exhibitions
              └── 展会卡片
                    └── 展会详情 /exhibitions/:id
```

对应接口如下：

```text
GET /api/public/home
GET /api/public/exhibitions
GET /api/public/exhibitions/{id}
```

该链路中的三个页面和接口封装已经存在。公共栏目内容链路当前如下：

```text
首页 /
  └── 顶部栏目子项，例如“展会简介”
        └── 栏目内容列表 /category/:code
              └── 内容条目
                    └── 内容详情 /content/:id
```

对应接口如下：

```text
GET /api/public/categories/{code}/contents
GET /api/public/contents/{id}
```
