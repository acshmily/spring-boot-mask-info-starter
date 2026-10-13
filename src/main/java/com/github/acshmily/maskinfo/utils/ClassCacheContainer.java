package com.github.acshmily.maskinfo.utils;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Huanghz
 * description: 类级脱敏字段缓存。key 为业务类，value 为已解析好访问器的脱敏字段描述符。
 * 无注解字段的类同样缓存空列表（负缓存）。2026-10 起：由 check-then-act 改为 computeIfAbsent 原子写入，
 * 原基于 Field[] 与方法签名的缓存 API 移除（内部工具类，见 changelog）。
 * date:Created in 2022-3-7
 * modifyBy: 2026-10 缓存原子化，缓存内容升级为字段描述符
 **/
public final class ClassCacheContainer {

    private static final ConcurrentHashMap<Class<?>, List<MaskFieldDescriptor>> CACHE
            = new ConcurrentHashMap<>();

    /**
     * 获取某类的待脱敏字段描述符，首次访问时解析并缓存
     *
     * @param clazz 业务类
     * @return 描述符列表，可能为空（表示该类无字段需要脱敏）
     */
    public static List<MaskFieldDescriptor> getMaskFields(Class<?> clazz) {
        return CACHE.computeIfAbsent(clazz, MaskFieldDescriptor::resolveAll);
    }

    private ClassCacheContainer() {
    }
}
