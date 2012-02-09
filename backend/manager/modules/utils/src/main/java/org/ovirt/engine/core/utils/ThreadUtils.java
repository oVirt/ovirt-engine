package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;

public class ThreadUtils {

    private static LogCompat log = LogFactoryCompat.getLog(ThreadUtils.class);

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.warn("Interrupted", e);
        }

    }
}
