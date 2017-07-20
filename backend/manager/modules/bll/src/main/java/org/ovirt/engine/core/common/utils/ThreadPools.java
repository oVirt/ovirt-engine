package org.ovirt.engine.core.common.utils;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, TYPE, METHOD })
public @interface ThreadPools {

    enum ThreadPoolType {CoCo, HostUpdatesChecker, EngineThreadPool}

    ThreadPoolType value();
}
