package org.ovirt.engine.core.common.di.interceptor;

import static org.ovirt.engine.core.common.di.interceptor.InvocationLogger.Level.DEBUG;

import javax.interceptor.Interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InvocationLogger(DEBUG)
@Interceptor
public class DebugLoggingInterceptor extends LoggingInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(DebugLoggingInterceptor.class);

    @Override
    public void log(String msg, Object... args) {
        logger.debug(msg, args);
    }
}
