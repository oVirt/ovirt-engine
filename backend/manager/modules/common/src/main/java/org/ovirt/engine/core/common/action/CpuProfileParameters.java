package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;

public class CpuProfileParameters extends ProfileParametersBase<CpuProfile> {
    private static final long serialVersionUID = 1303388881332223324L;

    public CpuProfileParameters() {
        super(false);
    }

    public CpuProfileParameters(CpuProfile cpuProfile) {
        super(cpuProfile, false);
    }
}
