package com.github.acshmily.maskinfo;

import com.github.acshmily.maskinfo.announce.EnableInfoMask;
import com.github.acshmily.maskinfo.announce.Mask;
import com.github.acshmily.maskinfo.announce.MaskType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 切面集成测试：真实 Spring 上下文 + AOP 代理（经 @EnableInfoMask 导入切面），
 * 覆盖正常路径、边界与异常路径
 */
@EnableInfoMask
class InfoMaskInterceptorTest {

    private AnnotationConfigApplicationContext context;

    private MaskUserService service;

    private ClassLevelMaskService classLevelService;

    @BeforeEach
    void setUp() {
        // 注册测试类本身以触发 @EnableInfoMask -> @Import(InfoMaskInterceptor)
        context = new AnnotationConfigApplicationContext(InfoMaskInterceptorTest.class, TestConfig.class);
        service = context.getBean(MaskUserService.class);
        classLevelService = context.getBean(ClassLevelMaskService.class);
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    // ---------- 正常路径 ----------

    @Test
    void singleObjectMasked() {
        UserDemo user = service.single();
        assertThat(user.getName()).isEqualTo("张*丰");
        assertThat(user.getPhone()).isEqualTo("138*****000");
        assertThat(user.getIp()).isEqualTo("192**********");
        assertThat(user.getBankCard()).isEqualTo("6225********6316");
        assertThat(user.getIdCard()).isEqualTo("3623************0034");
        assertThat(user.getPassword()).isEqualTo("******");
        // 无注解字段与非 String 注解字段保持原样
        assertThat(user.getAge()).isEqualTo("13");
        assertThat(user.getCoin()).isEqualTo(100);
    }

    @Test
    void listMasked() {
        List<UserDemo> users = service.list();
        assertThat(users).hasSize(2);
        for (UserDemo user : users) {
            assertThat(user.getPhone()).isEqualTo("138*****000");
        }
    }

    @Test
    void pageMasked() {
        Page<UserDemo> page = service.page();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getPhone()).isEqualTo("138*****000");
    }

    @Test
    void setMasked() {
        Set<UserDemo> users = service.setReturn();
        assertThat(users).hasSize(1);
        for (UserDemo user : users) {
            assertThat(user.getPhone()).isEqualTo("138*****000");
        }
    }

    @Test
    void inheritedFieldsMasked() {
        SubUser user = service.subUser();
        assertThat(user.getName()).isEqualTo("张*丰");
        assertThat(user.getPhone()).isEqualTo("138*****000");
        assertThat(user.getExtra()).isEqualTo("extra");
    }

    @Test
    void classLevelAnnotationMasked() {
        UserDemo user = classLevelService.single();
        assertThat(user.getPhone()).isEqualTo("138*****000");
    }

    @Test
    void repeatedCallsUseCache() {
        for (int i = 0; i < 3; i++) {
            UserDemo user = service.single();
            assertThat(user.getPhone()).isEqualTo("138*****000");
        }
    }

    @Test
    void concurrentCallsStayCorrect() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<UserDemo>> tasks = new ArrayList<>();
            for (int i = 0; i < 40; i++) {
                tasks.add(() -> service.single());
            }
            for (Future<UserDemo> future : pool.invokeAll(tasks)) {
                assertThat(future.get().getPhone()).isEqualTo("138*****000");
            }
        } finally {
            pool.shutdownNow();
        }
    }

    // ---------- 边界与异常路径 ----------

    @Test
    void nullReturnIsIgnored() {
        assertThat(service.nullReturn()).isNull();
    }

    @Test
    void listWithNullElementsHandled() {
        List<UserDemo> users = service.listWithNulls();
        assertThat(users).hasSize(3);
        assertThat(users.get(0).getPhone()).isEqualTo("138*****000");
        assertThat(users.get(1)).isNull();
        assertThat(users.get(2).getPhone()).isEqualTo("138*****000");
    }

    @Test
    void nullFieldStaysNull() {
        UserDemo user = service.nullFields();
        assertThat(user.getName()).isNull();
        assertThat(user.getPhone()).isNull();
    }

    @Test
    void plainObjectUntouched() {
        PlainUser user = service.plain();
        assertThat(user.getInfo()).isEqualTo("plain");
    }

    @Test
    void stringReturnUntouched() {
        assertThat(service.stringReturn()).isEqualTo("raw");
    }

    @Test
    void missingAccessorDoesNotBreakResponse() {
        BrokenUser user = service.broken();
        // 缺 setter 的字段被跳过且保持原样，其余字段正常脱敏
        assertThat(user.getSecret()).isEqualTo("topsecret");
        assertThat(user.getPhone()).isEqualTo("138*****000");
    }

    @Test
    void methodWithoutMaskAnnotationNotIntercepted() {
        UserDemo user = service.noAnnotation();
        assertThat(user.getPhone()).isEqualTo("13800138000");
    }

    @Test
    void targetExceptionPropagatesUntouched() {
        assertThatThrownBy(() -> service.boom())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    // ---------- 测试装置 ----------

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        public MaskUserService maskUserService() {
            return new MaskUserService();
        }

        @Bean
        public ClassLevelMaskService classLevelMaskService() {
            return new ClassLevelMaskService();
        }
    }

    static class UserDemo {

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

        @MaskType(Strategy = MaskType.Strategy.PASSWORD)
        private String password;

        @MaskType(Strategy = MaskType.Strategy.MOBILE)
        private Integer coin;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getBankCard() {
            return bankCard;
        }

        public void setBankCard(String bankCard) {
            this.bankCard = bankCard;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getCoin() {
            return coin;
        }

        public void setCoin(Integer coin) {
            this.coin = coin;
        }
    }

    static class SubUser extends UserDemo {

        private String extra;

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }
    }

    static class PlainUser {

        private String info;

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }

    static class BrokenUser {

        @MaskType(Strategy = MaskType.Strategy.MOBILE)
        private String phone;

        @MaskType(Strategy = MaskType.Strategy.NAME)
        private String secret;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getSecret() {
            return secret;
        }
    }

    static class MaskUserService {

        @Mask
        public UserDemo single() {
            return demoUser();
        }

        @Mask
        public List<UserDemo> list() {
            return Arrays.asList(demoUser(), demoUser());
        }

        @Mask
        public Page<UserDemo> page() {
            return new PageImpl<>(Arrays.asList(demoUser()), PageRequest.of(0, 2), 5);
        }

        @Mask
        public Set<UserDemo> setReturn() {
            Set<UserDemo> users = new HashSet<>();
            users.add(demoUser());
            return users;
        }

        @Mask
        public SubUser subUser() {
            SubUser user = new SubUser();
            fill(user);
            user.setExtra("extra");
            return user;
        }

        @Mask
        public UserDemo nullFields() {
            UserDemo user = new UserDemo();
            user.setName(null);
            user.setPhone(null);
            user.setAge("13");
            return user;
        }

        @Mask
        public UserDemo nullReturn() {
            return null;
        }

        @Mask
        public List<UserDemo> listWithNulls() {
            return Arrays.asList(demoUser(), null, demoUser());
        }

        @Mask
        public PlainUser plain() {
            PlainUser user = new PlainUser();
            user.setInfo("plain");
            return user;
        }

        @Mask
        public String stringReturn() {
            return "raw";
        }

        @Mask
        public BrokenUser broken() {
            BrokenUser user = new BrokenUser();
            user.setPhone("13800138000");
            fillSecret(user, "topsecret");
            return user;
        }

        @Mask
        public UserDemo boom() {
            throw new IllegalStateException("boom");
        }

        public UserDemo noAnnotation() {
            return demoUser();
        }

        private static UserDemo demoUser() {
            UserDemo user = new UserDemo();
            fill(user);
            return user;
        }

        private static void fill(UserDemo user) {
            user.setName("张三丰");
            user.setAge("13");
            user.setPhone("13800138000");
            user.setIp("192.168.1.100");
            user.setBankCard("6225220000496316");
            user.setIdCard("36232920200101000034");
            user.setPassword("abc123");
            user.setCoin(100);
        }

        private static void fillSecret(BrokenUser user, String secret) {
            try {
                java.lang.reflect.Field field = BrokenUser.class.getDeclaredField("secret");
                field.setAccessible(true);
                field.set(user, secret);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Mask
    static class ClassLevelMaskService {

        public UserDemo single() {
            UserDemo user = new UserDemo();
            user.setName("张三丰");
            user.setPhone("13800138000");
            return user;
        }
    }
}
