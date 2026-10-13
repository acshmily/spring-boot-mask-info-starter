package com.github.acshmily.maskinfo.utils;

import com.github.acshmily.maskinfo.announce.MaskType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MaskUtils 全量单测：各策略正常路径 + 边界与异常值
 */
class MaskUtilsTest {

    @Test
    void mobileStrategy() {
        assertThat(MaskUtils.mask("13800138000", MaskType.Strategy.MOBILE)).isEqualTo("138*****000");
    }

    @Test
    void nameStrategy() {
        assertThat(MaskUtils.mask("张三丰", MaskType.Strategy.NAME)).isEqualTo("张*丰");
        // 长度恰好等于 start+end 时保留前缀 + 单个掩码
        assertThat(MaskUtils.mask("李四", MaskType.Strategy.NAME)).isEqualTo("李*");
    }

    @Test
    void ipStrategy() {
        assertThat(MaskUtils.mask("192.168.1.100", MaskType.Strategy.IP)).isEqualTo("192**********");
    }

    @Test
    void bankCardStrategy() {
        assertThat(MaskUtils.mask("6225220000496316", MaskType.Strategy.BANKCARD)).isEqualTo("6225********6316");
    }

    @Test
    void idCardStrategy() {
        assertThat(MaskUtils.mask("36232920200101000034", MaskType.Strategy.IDCARD)).isEqualTo("3623************0034");
    }

    @Test
    void passwordStrategy() {
        assertThat(MaskUtils.mask("abc123", MaskType.Strategy.PASSWORD)).isEqualTo("******");
    }

    @Test
    void defaultStrategy() {
        assertThat(MaskUtils.mask("secret", MaskType.Strategy.DEFAULT)).isEqualTo("******");
    }

    @Test
    void customMaskChar() {
        assertThat(MaskUtils.mask("13800138000", MaskType.Strategy.MOBILE, "#")).isEqualTo("138#####000");
    }

    @Test
    void multiCharMaskText() {
        assertThat(MaskUtils.mask("13800138000", MaskType.Strategy.MOBILE, "**")).isEqualTo("138**********000");
    }

    @Test
    void nullInputReturnsNull() {
        assertThat(MaskUtils.mask((String) null, MaskType.Strategy.MOBILE)).isNull();
        assertThat(MaskUtils.mask((String) null, MaskType.Strategy.MOBILE, "#")).isNull();
        assertThat(MaskUtils.mask((Object) null, 1, 1, "*")).isNull();
        assertThat(MaskUtils.mask(null, 1, 1, "*")).isNull();
    }

    @Test
    void emptyInputReturnsEmpty() {
        assertThat(MaskUtils.mask("", MaskType.Strategy.MOBILE)).isEmpty();
    }

    @Test
    void inputShorterThanStart() {
        // 长度 <= start 时返回单个掩码字符
        assertThat(MaskUtils.mask("13", MaskType.Strategy.MOBILE)).isEqualTo("*");
    }

    @Test
    void inputNotLongerThanStartPlusEnd() {
        assertThat(MaskUtils.mask("138001", MaskType.Strategy.MOBILE)).isEqualTo("138*");
    }

    @Test
    void blankMaskTextFallsBackToStar() {
        assertThat(MaskUtils.mask("13800138000", MaskType.Strategy.MOBILE, "")).isEqualTo("138*****000");
        assertThat(MaskUtils.mask("13800138000", MaskType.Strategy.MOBILE, null)).isEqualTo("138*****000");
    }

    @Test
    void objectOverloadMatchesStringOverload() {
        assertThat(MaskUtils.mask((Object) "13800138000", 3, 3, "*"))
                .isEqualTo(MaskUtils.mask("13800138000", 3, 3, "*"));
    }

    @Test
    void strategyTwoArgDelegatesToDefaultMaskText() {
        assertThat(MaskUtils.mask("13800138000", MaskType.Strategy.MOBILE))
                .isEqualTo(MaskUtils.mask("13800138000", MaskType.Strategy.MOBILE, "*"));
    }
}
