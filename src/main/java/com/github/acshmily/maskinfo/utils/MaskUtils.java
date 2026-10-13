package com.github.acshmily.maskinfo.utils;


import com.github.acshmily.maskinfo.announce.MaskType;

/**
 * Author: Huanghz
 * description: 脱敏工具类。null 入参返回 null，空串返回空串（1.2 起不再返回单个掩码字符）。
 * date:Created in 2022-3-7
 * modifyBy: 2026-10 修正 null/空串语义、预分配容量
 **/

public final class MaskUtils {

    /**
     * 根据{@link MaskType.Strategy}进行截取替换
     *
     * @param s        原字符串
     * @param strategy 脱敏策略
     * @return 脱敏后的字符串
     */
    public static String mask(String s, MaskType.Strategy strategy) {
        return mask(s, strategy.getStartLen(), strategy.getEndLen(), strategy.getMaskText());
    }

    /**
     * 根据{@link MaskType.Strategy}进行截取替换，使用自定义掩码字符
     *
     * @param s        原字符串
     * @param strategy 脱敏策略
     * @param maskText 自定义掩码字符
     * @return 脱敏后的字符串
     */
    public static String mask(String s, MaskType.Strategy strategy, String maskText) {
        return mask(s, strategy.getStartLen(), strategy.getEndLen(), maskText);
    }

    /**
     * 根据传入的mask规则，替换字符串。null 返回 null
     *
     * @param obj   原对象
     * @param start 开头保留长度
     * @param end   结尾保留长度
     * @param mask  掩码字符
     * @return 脱敏后的字符串
     */
    public static String mask(Object obj, int start, int end, String mask) {
        if (obj == null) {
            return null;
        }
        return mask(obj.toString(), start, end, mask);
    }

    /**
     * 根据传入的mask规则，替换字符串。null 返回 null，空串返回空串，
     * 长度不足时返回单个掩码字符
     *
     * @param s     原字符串
     * @param start 开头保留长度
     * @param end   结尾保留长度
     * @param mask  掩码字符
     * @return 脱敏后的字符串
     */
    public static String mask(String s, int start, int end, String mask) {
        if (s == null) {
            return null;
        }
        int len = s.length();
        if (len == 0) {
            return s;
        }
        if (mask == null || mask.isEmpty()) {
            mask = "*";
        }
        if (len <= start) {
            return mask;
        }
        if (len <= start + end) {
            return s.substring(0, start) + mask;
        }
        int maskLen = len - start - end;
        StringBuilder builder = new StringBuilder(len + (mask.length() - 1) * maskLen);
        builder.append(s, 0, start);
        for (int i = 0; i < maskLen; i++) {
            builder.append(mask);
        }
        return builder.append(s, len - end, len).toString();
    }

    private MaskUtils() {
    }

}
