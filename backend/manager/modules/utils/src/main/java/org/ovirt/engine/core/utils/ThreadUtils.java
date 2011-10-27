package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

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
