package com.github.acshmily.maskinfo.interceptor;

import com.github.acshmily.maskinfo.announce.Mask;
import com.github.acshmily.maskinfo.utils.ClassCacheContainer;
import com.github.acshmily.maskinfo.utils.MaskFieldDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Author: Huanghz
 * description: 脱敏切面。方法或类上标注{@link Mask}即开启返回值脱敏。
 * 返回值为 Iterable 时逐元素处理（覆盖 List、Set、Spring Data 的 Page 等）；
 * 任意脱敏失败只记录日志，绝不影响原方法返回值或抛出异常。
 * date:Created in 2022-3-4
 * modifyBy: 2026-10 支持类级注解、Iterable 统一分发、整体异常兜底
 **/
@Aspect
@Component
public class InfoMaskInterceptor {

    private static final Log LOG = LogFactory.getLog(InfoMaskInterceptor.class);

    /**
     * 方法上的{@link Mask}注解切点
     */
    @Pointcut("@annotation(com.github.acshmily.maskinfo.announce.Mask)")
    public void methodCut() {
    }

    /**
     * 类上的{@link Mask}注解切点，标注在类上时其全部方法生效
     */
    @Pointcut("@within(com.github.acshmily.maskinfo.announce.Mask)")
    public void classCut() {
    }

    @AfterReturning(pointcut = "methodCut() || classCut()", returning = "result")
    public void doMask(JoinPoint joinPoint, Object result) {
        try {
            maskResult(result);
        } catch (Throwable t) {
            LOG.error("返回值脱敏失败, method=" + joinPoint.getSignature().toShortString(), t);
        }
    }

    private void maskResult(Object result) {
        if (result == null) {
            return;
        }
        if (result instanceof Iterable) {
            for (Object element : (Iterable<?>) result) {
                maskOne(element);
            }
        } else {
            maskOne(result);
        }
    }

    private void maskOne(Object obj) {
        if (obj == null) {
            return;
        }
        for (MaskFieldDescriptor descriptor : ClassCacheContainer.getMaskFields(obj.getClass())) {
            descriptor.applyTo(obj);
        }
    }
}
