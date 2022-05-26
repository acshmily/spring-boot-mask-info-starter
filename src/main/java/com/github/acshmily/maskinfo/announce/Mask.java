package com.github.acshmily.maskinfo.announce;

import java.lang.annotation.*;

/**
 * Author: Huanghz
 * description: 方法上注解
 * date:Created in 15:37 2022-3-4
 * modifyBy:
 **/
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mask {
    String value() default "";
}
