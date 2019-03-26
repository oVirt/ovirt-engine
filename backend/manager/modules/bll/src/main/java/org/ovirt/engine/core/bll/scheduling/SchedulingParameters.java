package org.ovirt.engine.core.bll.scheduling;

public class SchedulingParameters {
    private final boolean ignoreHardVmToVmAffinity;
    private final boolean doNotGroupVms;

    public SchedulingParameters() {
        this(false, false);
    }

    public SchedulingParameters(boolean ignoreHardVmToVmAffinity, boolean doNotGroupVms) {
        this.ignoreHardVmToVmAffinity = ignoreHardVmToVmAffinity;
        this.doNotGroupVms = doNotGroupVms;
    }

    public boolean isIgnoreHardVmToVmAffinity() {
        return ignoreHardVmToVmAffinity;
    }

    public boolean isDoNotGroupVms() {
        return doNotGroupVms;
    }
}
