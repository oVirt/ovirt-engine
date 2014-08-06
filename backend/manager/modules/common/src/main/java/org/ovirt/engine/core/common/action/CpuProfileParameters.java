package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.compat.Guid;

public class CpuProfileParameters extends ProfileParametersBase<CpuProfile> {
    private static final long serialVersionUID = 1303388881332223324L;

    public CpuProfileParameters() {
    }

    public CpuProfileParameters(CpuProfile cpuProfile, Guid cpuProfileId) {
        super(cpuProfile, cpuProfileId);
    }
}
