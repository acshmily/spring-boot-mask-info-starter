package com.github.acshmily.maskinfo.interceptor;

import com.github.acshmily.maskinfo.announce.MaskType;
import com.github.acshmily.maskinfo.utils.ClassCacheContainer;
import com.github.acshmily.maskinfo.utils.MaskUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: Huanghz
 * @description: AOP拦截器
 * @date:Created in 15:27 2022-3-4
 * @modifyBy:
 **/
@Aspect
@Component
public class InfoMaskInterceptor {
    // 定义类切点 有@Mask注解的并且还是Controller层的
    @Pointcut("@annotation(com.github.acshmily.maskinfo.announce.Mask)")
    public void classCut() {
    }


    @AfterReturning(value = "classCut()", returning = "result")
    public void doMask(JoinPoint joinPoint, Object result) throws Throwable {
        if (result instanceof List) {

            List temp1 = (List) result;
            for (Object obj : temp1) {
                maskField(obj);

            }
        } else {
            maskField(result);
        }
    }


    private void maskField(Object obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        if (Objects.nonNull(obj)) {
            // 需要缓存的字段
            List<Field> needCacheFields = new ArrayList<>();
            boolean fieldCheck = false;
            Field[] fields;
            if (ClassCacheContainer.existClassFields(obj.getClass())) {
                fields = ClassCacheContainer.getClassFields(obj.getClass());
            } else {
                fields = obj.getClass().getDeclaredFields();
            }
            for (Field field : fields) {
                MaskType annotation = field.getAnnotation(MaskType.class);
                if (annotation != null) {
                    fieldCheck = true;
                    if (field.getGenericType().toString().equals("class java.lang.String")) {
                        Method get = obj.getClass().getMethod("get" + getMethodName(field.getName()));
                        String val = (String) get.invoke(obj);
                        Method set = obj.getClass().getMethod("set" + getMethodName(field.getName()), String.class);
                        if (annotation.maskString() != "*") {
                            set.invoke(obj, MaskUtils.mask(val, annotation.Strategy(), annotation.maskString()));
                        } else {
                            set.invoke(obj, MaskUtils.mask(val, annotation.Strategy()));
                        }
                        needCacheFields.add(field);
                    }
                }

            }
            // 如果未缓存则缓存
            if (fieldCheck) {
                ClassCacheContainer.addClassFields(obj.getClass(), needCacheFields.toArray(new Field[0]));
            }
        }
    }

    /**
     * 获取Get方法名
     *
     * @param fieldName fieldName
     * @return string
     */
    private static String getMethodName(String fieldName) {
        byte[] items = fieldName.getBytes();
        items[0] = (byte) ((char) items[0] - 'a' + 'A');
        return new String(items);
    }

}
