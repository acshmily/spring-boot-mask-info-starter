package com.github.acshmily.maskinfo.utils;

import com.github.acshmily.maskinfo.announce.MaskType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Huanghz
 * description: 待脱敏字段描述符。解析一次后缓存（见{@link ClassCacheContainer}），
 * 调用路径上只保留 Method#invoke，避免每次请求重复反射查找方法。
 * date:Created in 2022-3-7
 * modifyBy: 2026-10 支持父类字段、缺失访问器容错、缓存原子化
 **/
public final class MaskFieldDescriptor {

    private static final Log LOG = LogFactory.getLog(MaskFieldDescriptor.class);

    private final Method getter;

    private final Method setter;

    private final MaskType.Strategy strategy;

    private final String maskString;

    private MaskFieldDescriptor(Method getter, Method setter, MaskType.Strategy strategy, String maskString) {
        this.getter = getter;
        this.setter = setter;
        this.strategy = strategy;
        this.maskString = maskString;
    }

    /**
     * 读取字段当前值，脱敏后原地写回。单个字段失败只告警，不影响其余字段与原方法返回值。
     *
     * @param target 目标对象
     */
    public void applyTo(Object target) {
        try {
            Object value = getter.invoke(target);
            setter.invoke(target, MaskUtils.mask((String) value, strategy, maskString));
        } catch (Exception e) {
            LOG.warn("字段脱敏失败, class=" + target.getClass().getName() + ", field getter=" + getter.getName(), e);
        }
    }

    /**
     * 解析一个类（含其父类）中所有带{@link MaskType}注解的 String 字段。
     * 解析不到任何可处理字段时返回空列表（负缓存，避免重复反射）。
     *
     * @param clazz 目标类
     * @return 描述符列表，可能为空
     */
    static List<MaskFieldDescriptor> resolveAll(Class<?> clazz) {
        List<MaskFieldDescriptor> descriptors = new ArrayList<>();
        Set<String> seenFieldNames = new HashSet<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                // 子类同名字段优先，父类的跳过
                if (!seenFieldNames.add(field.getName())) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                MaskType annotation = field.getAnnotation(MaskType.class);
                if (annotation == null || field.getType() != String.class) {
                    continue;
                }
                MaskFieldDescriptor descriptor = resolve(clazz, field, annotation);
                if (descriptor != null) {
                    descriptors.add(descriptor);
                }
            }
        }
        return descriptors.isEmpty() ? Collections.<MaskFieldDescriptor>emptyList() : descriptors;
    }

    private static MaskFieldDescriptor resolve(Class<?> clazz, Field field, MaskType annotation) {
        String suffix = capitalize(field.getName());
        try {
            Method getter = clazz.getMethod("get" + suffix);
            if (getter.getReturnType() != String.class) {
                LOG.warn("跳过脱敏字段(getter返回值非String): " + clazz.getName() + "." + field.getName());
                return null;
            }
            Method setter = clazz.getMethod("set" + suffix, String.class);
            getter.setAccessible(true);
            setter.setAccessible(true);
            return new MaskFieldDescriptor(getter, setter, annotation.Strategy(), annotation.maskString());
        } catch (NoSuchMethodException e) {
            LOG.warn("跳过脱敏字段(缺少标准getter/setter): " + clazz.getName() + "." + field.getName());
            return null;
        }
    }

    private static String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
