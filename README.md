# mask-info-spring-boot-starter

[![Maven Central](https://img.shields.io/maven-central/v/com.github.acshmily/mask-info-spring-boot-starter.svg)](https://central.sonatype.com/artifact/com.github.acshmily/mask-info-spring-boot-starter)
[![CI](https://github.com/acshmily/spring-boot-mask-info-starter/actions/workflows/ci.yml/badge.svg)](https://github.com/acshmily/spring-boot-mask-info-starter/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

[中文说明](#中文说明) | [English](#english)

---

## 中文说明

基于 Spring AOP 的返回值数据脱敏 starter：在方法（或类）上标注 `@Mask`，在返回 POJO 的 String 字段上标注 `@MaskType`，方法返回后字段值被原地替换为脱敏结果（`13800138000` → `138*****000`，`张三丰` → `张*丰`）。

### 特性

- 7 种内置脱敏策略（手机号 / 银行卡 / 身份证 / IP / 姓名 / 密码 / 默认），支持自定义掩码字符
- `@Mask` 支持方法级与类级标注
- 返回值支持单对象与 `Iterable`（`List` / `Set` / Spring Data `Page`，逐元素处理）
- 父类声明的字段同样脱敏
- 反射结果按类缓存（含已解析的 getter/setter），热路径开销极小
- 脱敏失败（如缺少 getter/setter）只记 warn 日志并跳过，绝不影响原方法返回值
- 无配置项、无传递依赖（Spring 依赖均为 `provided`），装配仅由 `@EnableInfoMask` 完成

### 环境要求

- Java 8+（已在 JDK 8 / 17 / 21 上验证）
- Spring Boot 2.3.1+（2.x / 3.x / 4.x 均可，编译基线 2.7.18，运行时使用应用自身的 Spring 版本）

### 快速开始

**1. 引入依赖**

```xml
<dependency>
  <groupId>com.github.acshmily</groupId>
  <artifactId>mask-info-spring-boot-starter</artifactId>
  <version>1.2</version>
</dependency>
```

**2. 启动类开启脱敏**

```java
@SpringBootApplication
@EnableInfoMask // 开启脱敏 AOP
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**3. 标注 POJO 字段与接口方法**

```java
@Data
public class UserDemo {
    @MaskType(Strategy = MaskType.Strategy.NAME)
    private String name;
    private String age;
    @MaskType(Strategy = MaskType.Strategy.MOBILE)
    private String phone;
    @MaskType(Strategy = MaskType.Strategy.IP)
    private String ip;
    @MaskType(Strategy = MaskType.Strategy.BANKCARD)
    private String bankCard;
    @MaskType(Strategy = MaskType.Strategy.IDCARD)
    private String idCard;
}
```

```java
@RestController
public class Controller {
    @Mask
    @RequestMapping("test")
    public R<UserDemo> test() {
        ...
        return R.ok(user);
    }
}
```

自定义掩码字符：`@MaskType(Strategy = MaskType.Strategy.MOBILE, maskString = "#")` → `138#####000`

### 内置策略

| 策略 | 保留 | 示例效果 |
|---|---|---|
| MOBILE | 前3后3 | `138*****000` |
| BANKCARD | 前4后4 | `6225********6316` |
| IDCARD | 前4后4 | `3623************0034` |
| IP | 前3后0 | `192**********` |
| NAME | 前1后1 | `张*丰`；两字姓名 `李*` |
| PASSWORD / DEFAULT | 0/0 | 全掩码（等长） |

### 行为说明

- 脱敏为**原地修改**返回对象；同一实例再次经过脱敏方法会二次脱敏
- 只处理 String 字段，要求标准 `getXxx`/`setXxx` 访问器（Lombok `@Data` 兼容）
- null 字段保持 null，空串保持空串，长度不足保留长度时返回单个掩码字符
- 子类与父类存在同名字段时只处理最具体层级
- `Map`、数组、`Optional`、嵌套 POJO 暂不支持；类级 `@Mask` 需标注在实现类上（接口上不生效）

### 更新日志

#### 1.2
- 修复：类级 `@Mask` 生效（`@annotation` + `@within` 组合切点）
- 修复：父类字段可脱敏
- 修复：缺失 getter/setter 不再抛异常打断接口，改为 warn 日志跳过
- 修复：null 字段保持 null、空串保持空串（原均返回单个 `*`）
- 性能：反射结果（注解字段 + 已解析访问器）按类缓存，`computeIfAbsent` 原子写入，含负缓存
- 构建：Java 8 字节码，CI 在 JDK 8/17/21 矩阵验证；发布通道迁移 Central Portal
- 说明：内部工具 `ClassCacheContainer` 缓存内容升级为 `MaskFieldDescriptor`，旧的 `Field[]` / 方法签名 API 移除

#### 1.1
- 支持 Page 返回值脱敏

#### 1.0
- 初始版本

---

## English

A Spring AOP based data-masking starter for return values: annotate a method (or class) with `@Mask`, annotate String fields of the returned POJO with `@MaskType`, and field values are replaced in place after the method returns (`13800138000` → `138*****000`, `张三丰` → `张*丰`).

### Features

- 7 built-in masking strategies (mobile / bank card / ID card / IP / name / password / default), custom mask character supported
- `@Mask` works at both method and class level
- Return values: single objects and `Iterable` (`List` / `Set` / Spring Data `Page`, masked element by element)
- Fields declared on parent classes are masked too
- Per-class reflection caching (annotated fields + resolved getter/setter) keeps the hot path cheap
- Masking failures (e.g. missing getter/setter) are logged and skipped — never break your API response
- No configuration, no transitive dependencies (all Spring deps are `provided`); wiring is done solely by `@EnableInfoMask`

### Requirements

- Java 8+ (verified on JDK 8 / 17 / 21)
- Spring Boot 2.3.1+ (2.x / 3.x / 4.x; compile baseline 2.7.18, the application's own Spring version is used at runtime)

### Quick Start

**1. Add the dependency**

```xml
<dependency>
  <groupId>com.github.acshmily</groupId>
  <artifactId>mask-info-spring-boot-starter</artifactId>
  <version>1.2</version>
</dependency>
```

**2. Enable masking on your application class**

```java
@SpringBootApplication
@EnableInfoMask // enable mask aop
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**3. Annotate POJO fields and controller methods**

```java
@Data
public class UserDemo {
    @MaskType(Strategy = MaskType.Strategy.NAME)
    private String name;
    private String age;
    @MaskType(Strategy = MaskType.Strategy.MOBILE)
    private String phone;
    @MaskType(Strategy = MaskType.Strategy.IP)
    private String ip;
    @MaskType(Strategy = MaskType.Strategy.BANKCARD)
    private String bankCard;
    @MaskType(Strategy = MaskType.Strategy.IDCARD)
    private String idCard;
}
```

```java
@RestController
public class Controller {
    @Mask
    @RequestMapping("test")
    public R<UserDemo> test() {
        ...
        return R.ok(user);
    }
}
```

Custom mask char: `@MaskType(Strategy = MaskType.Strategy.MOBILE, maskString = "#")` → `138#####000`

### Built-in Strategies

| Strategy | Keeps | Example |
|---|---|---|
| MOBILE | first 3 / last 3 | `138*****000` |
| BANKCARD | first 4 / last 4 | `6225********6316` |
| IDCARD | first 4 / last 4 | `3623************0034` |
| IP | first 3 / last 0 | `192**********` |
| NAME | first 1 / last 1 | `张*丰`; two-char name `李*` |
| PASSWORD / DEFAULT | 0 / 0 | fully masked (same length) |

### Behavior Notes

- Masking mutates the returned object **in place**; passing the same instance through a masking method again masks it a second time
- Only String fields with standard `getXxx`/`setXxx` accessors are processed (Lombok `@Data` compatible)
- `null` fields stay `null`, empty strings stay empty; strings shorter than the keep-length become a single mask character
- When a subclass and its parent declare the same field name, only the most specific one is processed
- `Map`, arrays, `Optional` and nested POJOs are not supported yet; class-level `@Mask` must be on the implementation class (interfaces are not matched)

### Changelog

#### 1.2
- Fix: class-level `@Mask` now takes effect (`@annotation` + `@within` pointcuts)
- Fix: fields declared on parent classes are now masked
- Fix: missing getter/setter no longer throws and breaks the response — the field is skipped with a warn log
- Fix: `null` field values stay `null`, empty strings stay empty (previously both became a single `*`)
- Perf: per-class reflection results (annotated fields + resolved getter/setter) cached via `computeIfAbsent`, with negative caching
- Build: Java 8 bytecode verified on JDK 8 / 17 / 21 (CI matrix); publishing migrated to the Central Publisher Portal
- Note: `ClassCacheContainer` (internal utility) now caches `MaskFieldDescriptor` lists; its old `Field[]` / method-signature APIs were removed

#### 1.1
- Page mask support

#### 1.0
- Initial release

---

## License

[Apache License 2.0](LICENSE)

enjoy~~~
