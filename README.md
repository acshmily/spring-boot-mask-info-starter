# Use Guide

## maven
```xml
<dependency>
  <groupId>com.github.acshmily</groupId>
  <artifactId>mask-info-spring-boot-starter</artifactId>
  <version>1.2</version>
</dependency>
```

## Enable Mask AOP
In your SpringBoot ApplicationRunner Class add announce to enable,like

```java
import com.github.acshmily.maskinfo.announce.EnableInfoMask;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableInfoMask // enable mask aop
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
```
## Modify your pojo
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
Fields declared on parent classes are masked too. Custom mask char via `@MaskType(Strategy = MaskType.Strategy.MOBILE, maskString = "#")`.

## Modify your Controller

```java
import com.github.acshmily.maskinfo.announce.Mask;

@RequestMapping
@RestController
public class Controller {
    @Mask
    @RequestMapping("test")
    public R<UserDemo> test(){
        UserDemo user = new UserDemo();
        user.setName("张三");
        user.setAge("13");
        user.setPhone("13800138000");
        user.setBankCard("622522000049631");
        user.setIp("127.0.0.1");
        user.setIdCard("36232920200101000034");
        return R.ok(user);
    }
}
```

`@Mask` can also be placed on the class — all methods of that class are then masked. Besides single objects, `List` / `Set` / Spring Data `Page` return values are masked element by element. Masking failures (e.g. missing getter/setter) are logged and skipped, never breaking your API response. `null` fields stay `null`.

## Changelog

### 1.2
- Fix: class-level `@Mask` now takes effect (`@annotation` + `@within` pointcuts)
- Fix: fields declared on parent classes are now masked
- Fix: missing getter/setter no longer throws and breaks the response — the field is skipped with a warn log
- Fix: `null` field values stay `null`, empty strings stay empty (previously both became a single `*`)
- Fix: field type check uses `field.getType() == String.class` instead of string comparison
- Perf: per-class reflection results (annotated fields + resolved getter/setter) cached via `computeIfAbsent`, negative caching for classes without annotated fields
- Build: Java 8 bytecode, verified on JDK 8 / 17 / 21 (CI matrix); reproducible build (Spring baseline pinned to 2.7.18, deps stay `provided`)
- Build: publishing migrated from the retired OSSRH to the Central Publisher Portal
- Note: `ClassCacheContainer` (internal utility) now caches `MaskFieldDescriptor` lists; its old `Field[]` / method-signature APIs were removed

### 1.1
- Page mask support

### 1.0
- Initial release

enjoy~~~
