package org.ovirt.engine.core.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockConfigExtension.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockedConfig {
    public static final String DEFAULT_METHOD_NAME = "mockConfiguration";
    String value() default DEFAULT_METHOD_NAME;
}
