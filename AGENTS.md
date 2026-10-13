# AGENTS.md

面向 AI 编程助手的项目指南。修改本仓库代码前请先阅读本文，尤其是「当前限制」一节。

## 项目概述

`mask-info-spring-boot-starter`（groupId: `com.github.acshmily`，当前版本 1.2）是一个基于 Spring AOP 的数据脱敏 starter。使用方式：

1. 使用方在启动类上标注 `@EnableInfoMask` 开启功能；
2. 在 Controller 方法（或类）上标注 `@Mask`；
3. 在返回 POJO 的 String 字段（含父类字段）上标注 `@MaskType(Strategy = ...)`；
4. 方法返回后，切面通过缓存的 getter/setter 反射调用，将字段值原地替换为脱敏后的字符串（如 `138*****000`、`张*丰`）。

无任何配置项、无 spring.factories / AutoConfiguration.imports——装配完全由 `@EnableInfoMask` 上的 `@Import(InfoMaskInterceptor.class)` 完成，因此所有 Spring 依赖均为 `provided` scope，由使用方的 classpath 提供。发布通道为 Central Publisher Portal（OSSRH 已停用）。

## 技术栈

- Java 8 语法（`maven.compiler.source/target=1.8`），**新增代码不得使用 Java 8 以上语法**（CI 在 JDK 8/17/21 矩阵上验证，测试代码同样受此约束：不要用 `List.of`、`var` 等 9+ API）。
- Spring 编译基线固定 2.7.18（provided），运行时依赖 `spring-boot-autoconfigure`、`spring-boot-starter-aop`；日志走 spring-jcl（`org.apache.commons.logging`）。
- spring-data-commons 仅 test scope（切面用 `Iterable` 统一处理 List/Set/Page，主代码不引用 `Page` 类）。
- 测试：JUnit 5 + AssertJ（spring-boot-starter-test 2.7.18）。

## 目录结构

```
src/main/java/com/github/acshmily/maskinfo/
├── announce/                      # 注解定义（包名 announce 是历史拼写，实为 annotation，已是公共 API 勿改名）
│   ├── EnableInfoMask.java        # 开关注解，@Import 导入切面 Bean
│   ├── Mask.java                  # 方法/类级脱敏开关注解（value 属性目前未使用）
│   └── MaskType.java              # 字段级注解 + Strategy 枚举（定义各策略保留长度）
├── interceptor/
│   └── InfoMaskInterceptor.java   # 切面：@annotation + @within 切点，@AfterReturning 脱敏
└── utils/
    ├── ClassCacheContainer.java   # 类 → 字段描述符列表缓存（computeIfAbsent 原子写入，含负缓存）
    ├── MaskFieldDescriptor.java   # 字段描述符：注解元数据 + 已解析 getter/setter，解析一次常驻缓存
    └── MaskUtils.java             # 字符串截取替换工具
src/test/java/                     # MaskUtilsTest（单测）+ InfoMaskInterceptorTest（Spring 上下文集成测试）
.github/workflows/ci.yml           # CI：JDK 8/17/21 矩阵 mvn verify
```

## 核心流程

`InfoMaskInterceptor`（`@Aspect` + `@Component`，经 `@EnableInfoMask` 导入）：

1. 切点：`@annotation(Mask) || @within(Mask)`——方法级与类级注解均生效。
2. `@AfterReturning` 整体 try/catch 兜底：任何脱敏失败只记 error 日志，绝不影响原方法返回值。
3. 返回值分发：`Iterable`（含 List、Set、Spring Data 的 Page）逐元素处理，null 元素跳过；其他按单对象处理。
4. `ClassCacheContainer.getMaskFields(clazz)` 首次解析并缓存（`computeIfAbsent`）：沿父类链收集带 `@MaskType` 的 String 字段，解析标准 getter/setter（缺失则 warn 并跳过该字段），之后热路径只剩 `Method#invoke`。
5. `MaskUtils.mask(s, startLen, endLen, mask)`：保留前 `startLen` 后 `endLen` 个字符，中间按原长度填充掩码。

`MaskType.Strategy` 枚举定义各策略（startLen, endLen）：

| 策略 | 保留 | 示例效果 |
|---|---|---|
| MOBILE | 前3后3 | `138*****000`（11 位手机号） |
| BANKCARD | 前4后4 | 前4 + `*` + 后4 |
| IDCARD | 前4后4 | 前4 + `*` + 后4 |
| IP | 前3后0 | `192**********` |
| NAME | 前1后1 | `张*丰`；两字姓名 `李*` |
| PASSWORD / DEFAULT | 0/0 | 全掩码（等长） |

## 当前限制（修改代码前必读）

以下为现状行为（多数是 1.2 已确认的设计取舍）：

- **只处理 String 字段**，且要求标准 `getXxx`/`setXxx` 访问器（Lombok `@Data` 兼容）；缺失访问器的字段 warn 后跳过。数字、嵌套对象不递归处理。
- **脱敏是原地修改（副作用）**：直接改写返回的对象实例；同一实例再次经过脱敏方法会二次脱敏。
- 返回值支持：单对象与 `Iterable`（List/Set/Page）。`Map`、数组、`Optional`、嵌套 POJO 不支持。
- `@within` 只匹配实现类上的类级 `@Mask`，注解在接口上不生效。
- 边界语义：null 字段保持 null，空串保持空串，长度 ≤ startLen 返回单个掩码字符。
- 子类与父类存在同名字段时，只处理最具体层级的那个。

## 构建与验证

```bash
mvn -B verify    # 编译 + 全量测试 + javadoc/sources jar（无需 GPG，本地与 CI 通用）
```

- GPG 签名与发布插件隔离在 `release` profile 中，日常构建完全不碰。
- Spring 依赖固定 2.7.18（provided），构建可复现；Boot 3/4 使用方在运行时提供自己的 Spring 版本。

## 测试约定

`mvn verify` 必须全绿后才可提交。新增功能/修复需同步补测试：
- 纯工具逻辑 → `MaskUtilsTest`（正常 + null/空/短串边界 + 自定义掩码字符）。
- 切面行为 → `InfoMaskInterceptorTest`（真实 Spring 上下文 + AOP 代理，fixture 为文件内静态内部类，注意 Java 8 语法）。

## 扩展指南

**新增脱敏策略**：在 `MaskType.Strategy` 枚举加一项 `STRATEGY_NAME(startLen, endLen, maskText)`，并在 `MaskUtilsTest` 补用例。

**新增返回类型支持**（如 `Map`、`Optional`）：在 `InfoMaskInterceptor.maskResult` 的分发逻辑中添加。

**发布**（仅维护者）：需要两个前置——本机 GPG 私钥（settings.xml 的 `gpg` server 存 passphrase）、`~/.m2/settings.xml` 中的 Central Portal token（`<server><id>central</id>`，在 central.sonatype.com 生成）。执行 `mvn -B clean deploy -Prelease`（签名 + sources/javadoc + Portal 上传自动发布）。修改版本号需同步 README 依赖示例与 Changelog。

## 代码风格约定

- 类注释采用固定头部格式（Author / description / date / modifyBy），修改时在 modifyBy 追加说明。
- 注释以中文为主。
- 公共 API（注解属性名、枚举名、包名 `announce`）已被外部使用，重命名即破坏兼容，需升主版本。
