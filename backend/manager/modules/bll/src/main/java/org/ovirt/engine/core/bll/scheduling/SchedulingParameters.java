package org.ovirt.engine.core.bll.scheduling;

public class SchedulingParameters {
    private final boolean ignoreHardVmToVmAffinity;

    public SchedulingParameters() {
        this(false);
    }

    public SchedulingParameters(boolean ignoreHardVmToVmAffinity) {
        this.ignoreHardVmToVmAffinity = ignoreHardVmToVmAffinity;
    }

    public boolean isIgnoreHardVmToVmAffinity() {
        return ignoreHardVmToVmAffinity;
    }
}
