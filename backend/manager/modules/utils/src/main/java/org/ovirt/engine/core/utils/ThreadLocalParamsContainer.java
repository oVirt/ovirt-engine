package org.ovirt.engine.core.utils;



public class ThreadLocalParamsContainer {

    /**
     * Identifies the correlation-id associated with the current thread
     */
    private static final ThreadLocal<String> correlationId = new ThreadLocal<String>();

    /**
     * Set the value of the correlation-ID of the current thread and the value to be printed in the logger and past to
     * VDSM
     *
     * @param correlation
     *            The value of the correlation-ID to be logged
     */
    public static void setCorrelationId(String correlation) {
        correlationId.set(correlation);
    }

    public static String getCorrelationId() {
        return correlationId.get();
    }

    public static void clean() {
        correlationId.remove();
    }

}
