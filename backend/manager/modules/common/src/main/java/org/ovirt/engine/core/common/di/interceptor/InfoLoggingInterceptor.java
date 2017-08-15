package org.ovirt.engine.core.common.di.interceptor;

import static org.ovirt.engine.core.common.di.interceptor.InvocationLogger.Level.INFO;

import javax.interceptor.Interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InvocationLogger(INFO)
@Interceptor
public class InfoLoggingInterceptor extends LoggingInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(InfoLoggingInterceptor.class);

    @Override
    public void log(String msg, Object... args) {
        logger.info(msg, args);
    }
}
