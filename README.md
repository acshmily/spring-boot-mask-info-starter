# Use Guide

## maven
```xml
<dependency>
    <groupId>com.github.acshmily</groupId>
    <artifactId>mask-info-spring-boot-starter</artifactId>
    <version>1.0</version>
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

enjoy~~~