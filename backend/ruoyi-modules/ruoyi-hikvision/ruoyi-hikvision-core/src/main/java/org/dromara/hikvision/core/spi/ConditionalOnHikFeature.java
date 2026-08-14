package org.dromara.hikvision.core.spi;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 按特性名插拔：{@code hikvision.features.<value>=true} 时生效（缺省为 true）。
 * 同时受 {@code hikvision.enabled} 总开关控制。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(HikFeatureCondition.class)
public @interface ConditionalOnHikFeature {

    /**
     * 特性名，对应配置项 {@code hikvision.features.<value>}
     */
    String value();
}
