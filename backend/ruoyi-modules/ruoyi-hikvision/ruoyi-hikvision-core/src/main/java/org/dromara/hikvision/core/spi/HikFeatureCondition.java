package org.dromara.hikvision.core.spi;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * 特性开关条件：总开关 + 分特性开关。
 */
public class HikFeatureCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        if (!env.getProperty("hikvision.enabled", Boolean.class, Boolean.TRUE)) {
            return false;
        }
        Map<String, Object> attrs = metadata.getAnnotationAttributes(ConditionalOnHikFeature.class.getName());
        if (attrs == null) {
            return true;
        }
        String feature = (String) attrs.get("value");
        if (feature == null || feature.isBlank()) {
            return true;
        }
        return env.getProperty("hikvision.features." + feature, Boolean.class, Boolean.TRUE);
    }
}
