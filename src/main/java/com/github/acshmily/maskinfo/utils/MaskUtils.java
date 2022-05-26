package com.github.acshmily.maskinfo.utils;


import com.github.acshmily.maskinfo.announce.MaskType;

/**
 * Author: Huanghz
 * description: 脱敏工具类
 * date:Created in 10:23 2022-3-7
 * modifyBy:
 **/

public class MaskUtils {
    /**
     * 根据{@link MaskType.Strategy}进行截取替换
     *
     * @param s
     * @param  strategy strategy
     * @return
     */
    public static String mask(String s,  MaskType.Strategy strategy) {
        return mask(s, strategy.getStartLen(), strategy.getEndLen(),strategy.getMaskText());
    }

    /**
     * 根据{@link MaskType.Strategy}进行截取替换
     * @param s
     * @param strategy
     * @param maskText
     * @return
     */
    public static String mask(String s,  MaskType.Strategy strategy,String maskText) {
        return mask(s, strategy.getStartLen(), strategy.getEndLen(),maskText);
    }
    /**
     * 根据传入的mask规则，替换字符串
     *
     * @param obj
     * @param start
     * @param end
     * @param mask
     * @return
     */
    public static String mask(Object obj, int start, int end, String mask) {
        if (obj == null) {
            return mask;
        }
        return mask(obj.toString(), start, end, mask);
    }
    /**
     * 根据传入的mask规则，替换字符串
     *
     * @param s
     * @param start
     * @param end
     * @param mask
     * @return
     */
    public static String mask(String s, int start, int end, String mask) {
        if (s == null || s.length() == 0) {
            return mask;
        }
        int len = s.length();
        if (len <= start) {
            return mask;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < len - start - end;i++){
            stringBuilder.append(mask);
        }

        if (len <= start + end) {
            return s.substring(0, start) + mask;
        }

        return s.substring(0, start) + stringBuilder + s.substring(len - end);
    }
    private MaskUtils(){}

}
