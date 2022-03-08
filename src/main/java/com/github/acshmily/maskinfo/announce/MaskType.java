package com.github.acshmily.maskinfo.announce;

import java.lang.annotation.*;

/**
 * @Author: Huanghz
 * @description: 脱敏注解
 * @date:Created in 15:09 2022-3-4
 * @modifyBy:
 **/
@Documented
@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaskType {
    // 定义枚举
    enum Strategy{
        /**
         * 手机号只显示前三后三位
         * 131****224
         *
         * */
        MOBILE(3, 3,"*"),
        /**
         * 银行卡显示最后四位
         */
        BANKCARD(4, 4,"*"),
        /**
         *  身份证保留前二后二字符
         */
        IDCARD(4, 4,"*"),
        /**
         * ip地址 只展示一段
         *
         * */
        IP(3, 0,"*"),
        /** 名字 */
        NAME(1, 1,"*"),
        /** 密码 */
        PASSWORD(0, 0,"*"),
        /** 默认（全部mask） */
        DEFAULT(0, 0,"*");

        /** 开头保留的长度 */
        private int startLen;
        /** 结尾保留的长度 */
        private int endLen;
        private String maskText;
        Strategy(int startLen,int endLen,String text){
            this.startLen = startLen;
            this.endLen = endLen;
            this.maskText = text;
        }
        public int getStartLen(){
            return this.startLen;
        }
        public int getEndLen(){
            return this.endLen;
        }
        public String getMaskText(){
            return this.maskText;
        }
    };
    // 默认不加密
    Strategy Strategy() default Strategy.DEFAULT;
    String maskString() default "*";
}
