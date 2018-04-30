package org.ovirt.engine.core.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InjectorExtension.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectedMock {
}
