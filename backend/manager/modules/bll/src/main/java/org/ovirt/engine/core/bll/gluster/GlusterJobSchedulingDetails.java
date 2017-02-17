package org.ovirt.engine.core.bll.gluster;

public class GlusterJobSchedulingDetails {
    private String methodName;
    long delay;

    public GlusterJobSchedulingDetails(String methodName, long delay) {
        this.methodName = methodName;
        this.delay = delay;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getDelay() {
        return delay;
    }
}
