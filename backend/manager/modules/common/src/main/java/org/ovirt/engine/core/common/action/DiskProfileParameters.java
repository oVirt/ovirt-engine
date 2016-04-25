package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;

public class DiskProfileParameters extends ProfileParametersBase<DiskProfile> {
    private static final long serialVersionUID = 1303388881254823324L;


    public DiskProfileParameters() {

    }

    public DiskProfileParameters(DiskProfile diskProfile) {
        super(diskProfile);
    }

    public DiskProfileParameters(DiskProfile diskProfile, boolean addPermissions) {
        super(diskProfile, addPermissions);
    }
}
