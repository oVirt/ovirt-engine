package org.ovirt.engine.core.utils;

import org.slf4j.MDC;

public class CorrelationIdTracker {

    private static final String MDC_CORRELATION_ID = "ovirtCorrelationId";

    /**
     * Set the value of the correlation-ID of the current thread and the value to be printed in the logger and past to
     * VDSM
     *
     * @param correlation
     *            The value of the correlation-ID to be logged
     */
    public static void setCorrelationId(String correlation) {
        if (correlation == null) {
            MDC.remove(MDC_CORRELATION_ID);
        } else {
            MDC.put(MDC_CORRELATION_ID, correlation);
        }
    }

    public static String getCorrelationId() {
        return MDC.get(MDC_CORRELATION_ID);
    }

    public static void clean() {
        MDC.remove(MDC_CORRELATION_ID);
    }

}
