package org.ovirt.engine.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {

    private static final Logger log = LoggerFactory.getLogger(ThreadUtils.class);

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.warn("Interrupted: {}", e.getMessage());
            log.debug("Exception", e);
        }

    }
}
