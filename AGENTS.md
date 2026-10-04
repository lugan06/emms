
## 接口文档规范
- 所有 Controller 必须使用 Swagger 注解（@Tag, @Operation）。
- 所有 DTO / VO 的字段必须使用 @Schema(description = "xxx")。
- 每次新增或修改接口后，必须确保 `/v3/api-docs` 输出正确的 OpenAPI JSON。
- 前端或测试人员通过 Apifox 自动同步该地址获取接口文档。

## 安全与配置规范
- 严禁将数据库密码、JWT 密钥等敏感信息硬编码在代码中。
- 本地配置统一放在 `application-local.yml`，且该文件必须被 `.gitignore` 忽略。
- 所有环境相关的配置（如数据库连接）必须通过 Spring Profile 区分。

## 测试规范
- 改完代码必须同步写/更新测试：后端 JUnit（Service 单元测试 + Controller 集成测试），前端 Vitest（API 封装、Store、表单校验）。
- 交付前必须跑通所有测试，禁止跳过失败项。汇报时说明改动文件、测试结果。

## 测试环境
- 后端测试用独立测试库（H2 或 test 库），禁止连开发库。
- 前端测试用 mock，不依赖真实后端。