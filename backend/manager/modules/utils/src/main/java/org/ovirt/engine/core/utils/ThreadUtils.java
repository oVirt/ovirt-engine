package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ThreadUtils {

    private static final Log log = LogFactory.getLog(ThreadUtils.class);

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.warn("Interrupted", e);
        }

    }
}
