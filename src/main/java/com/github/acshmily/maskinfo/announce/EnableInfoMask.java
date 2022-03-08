package com.github.acshmily.maskinfo.announce;

import com.github.acshmily.maskinfo.interceptor.InfoMaskInterceptor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author: Huanghz
 * @description: 开启字段加解密返回JSON脱敏
 * @date:Created in 14:38 2022-3-4
 * @modifyBy:
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({InfoMaskInterceptor.class})
public @interface EnableInfoMask {
}
